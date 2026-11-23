package com.example.autoledger.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityEvent
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.example.autoledger.pipeline.BillRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** 通道B：无障碍读屏。支付成功页/账单详情页的"实付款 ¥XX"文字，覆盖淘宝等任意页面。 */
class BillAccessibilityService : AccessibilityService() {
    private lateinit var repo: BillRepository
    private val scope = CoroutineScope(Dispatchers.IO)
    private var lastFingerprint = ""
    private var lastTime = 0L

    companion object {
        private const val TAG = "AutoLedgerA11y"
        // 页面特征：出现这些词且含金额，才认为是支付页
        private val PAGE_MARKERS = listOf("支付成功", "交易成功", "付款成功", "实付款", "账单详情", "交易单号", "收款方", "已支付")
        private val MONEY_HINT = listOf("¥", "￥", "元")
    }

    override fun onCreate() {
        super.onCreate()
        repo = BillRepository(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return

        val root = rootInActiveWindow ?: return
        val text = StringBuilder()
        collectText(root, text, 0)
        val page = text.toString().trim()
        if (page.length < 4 || page.length > 4000) return

        // 必须有页面特征 + 金额符号
        if (PAGE_MARKERS.none { page.contains(it) }) return
        if (MONEY_HINT.none { page.contains(it) }) return

        val pkg = event.packageName?.toString() ?: "unknown"
        // 30 秒指纹去重，防止同一页面反复触发
        val fp = "$pkg|${page.hashCode()}"
        val now = System.currentTimeMillis()
        if (fp == lastFingerprint && now - lastTime < 30_000) return
        lastFingerprint = fp
        lastTime = now

        Log.d(TAG, "captured [$pkg] ${page.take(200)}")
        scope.launch {
            val inserted = repo.ingest(page, "accessibility:$pkg", now)
            Log.d(TAG, "ingest=$inserted")
        }
    }

    private fun collectText(node: AccessibilityNodeInfo?, sb: StringBuilder, depth: Int) {
        if (node == null || depth > 30) return
        val t = node.text?.toString()
        if (!t.isNullOrBlank()) sb.append(t).append(' ')
        val d = node.contentDescription?.toString()
        if (!d.isNullOrBlank()) sb.append(d).append(' ')
        for (i in 0 until node.childCount) {
            collectText(node.getChild(i), sb, depth + 1)
        }
    }

    override fun onInterrupt() {}
}
