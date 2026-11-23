package com.example.autoledger.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.autoledger.R
import com.example.autoledger.data.AppDatabase
import com.example.autoledger.data.CsvExporter
import com.example.autoledger.service.BillAccessibilityService
import com.example.autoledger.service.PayNotificationListener
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var tvStats: TextView
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        tvStats = TextView(this).apply { textSize = 18f; text = "加载中…" }
        tvStatus = TextView(this).apply { textSize = 14f }
        val btnA11y = Button(this).apply { text = "开启无障碍采集" }
        val btnNotif = Button(this).apply { text = "开启通知采集" }
        val btnExport = Button(this).apply { text = "导出 CSV 给 dsh" }
        val btnRefresh = Button(this).apply { text = "刷新账单" }

        root.addView(tvStats)
        root.addView(tvStatus)
        root.addView(btnA11y)
        root.addView(btnNotif)
        root.addView(btnRefresh)
        root.addView(btnExport)
        setContentView(root)

        btnA11y.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        btnNotif.setOnClickListener {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }
        btnRefresh.setOnClickListener { refresh() }
        btnExport.setOnClickListener {
            lifecycleScope.launch {
                val bills = AppDatabase.get(this@MainActivity).billDao().all()
                val f = CsvExporter.export(this@MainActivity, bills)
                Toast.makeText(this@MainActivity, "已导出: ${f.path}", Toast.LENGTH_LONG).show()
            }
        }
        refresh()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val a11yOn = isServiceEnabled(BillAccessibilityService::class.java)
        val notifOn = isNotificationListenerEnabled()
        tvStatus.text = "无障碍采集: ${if (a11yOn) "已开启" else "未开启"}\n通知采集: ${if (notifOn) "已开启" else "未开启"}"
    }

    private fun isServiceEnabled(cls: Class<*>): Boolean {
        val expected = ComponentName(this, cls).flattenToString()
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
        return enabled.split(':').any { it.equals(expected, true) || it.contains(cls.simpleName) }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: ""
        return flat.contains(packageName)
    }

    private fun refresh() {
        lifecycleScope.launch {
            val bills = AppDatabase.get(this@MainActivity).billDao().all()
            val total = bills.sumOf { it.amountCents }
            val expense = bills.filter { it.direction == 0 }.sumOf { it.amountCents }
            tvStats.text = "共 ${bills.size} 笔\n总支出 ¥${String.format("%.2f", expense / 100.0)}\n(含收入合计 ¥${String.format("%.2f", total / 100.0)})"
        }
    }
}
