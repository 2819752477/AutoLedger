package com.example.autoledger.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.autoledger.pipeline.BillRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** 通道A：通知监听。微信/支付宝/美团/拼多多付款后都会发通知，含金额与商户。 */
class PayNotificationListener : NotificationListenerService() {
    private lateinit var repo: BillRepository
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "AutoLedgerNotif"
        private val PAY_PACKAGES = listOf(
            "com.tencent.mm",                 // 微信
            "com.eg.android.AlipayGphone",    // 支付宝
            "com.sankuai.meituan",            // 美团
            "com.xunmeng.pinduoduo"           // 拼多多
        )
        private val PAY_KEYWORDS = listOf("支付", "付款", "收款", "消费", "转账", "到账", "¥", "元")
    }

    override fun onCreate() {
        super.onCreate()
        repo = BillRepository(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        if (pkg !in PAY_PACKAGES) return
        val n: Notification = sbn.notification ?: return
        val extras = n.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val big = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
        val content = "$title $text $big".trim()
        if (content.isBlank()) return
        if (PAY_KEYWORDS.none { content.contains(it) }) return

        Log.d(TAG, "captured [$pkg] $content")
        scope.launch {
            val inserted = repo.ingest(content, "notification:$pkg", sbn.postTime)
            Log.d(TAG, "ingest=$inserted")
        }
    }
}
