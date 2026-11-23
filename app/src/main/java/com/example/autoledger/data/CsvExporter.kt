package com.example.autoledger.data

import android.content.Context
import android.os.Environment
import java.io.File

/** 导出 CSV 给 dsh 财务分析流程（财务/inbox/）。 */
object CsvExporter {
    fun export(context: Context, bills: List<Bill>): File {
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "DSH-Go/auto-ledger")
        dir.mkdirs()
        val file = File(dir, "autoledger.csv")
        val sb = StringBuilder("日期,时间,金额,商户,分类,收支,来源,原始文本\n")
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        for (b in bills) {
            val d = java.util.Date(b.happenedAt)
            val parts = fmt.format(d).split(" ")
            val amt = String.format("%.2f", b.amountCents / 100.0)
            sb.append(parts[0]).append(',')
                .append(parts[1]).append(',')
                .append(amt).append(',')
                .append(csv(b.merchant)).append(',')
                .append(csv(b.category)).append(',')
                .append(if (b.direction == 0) "支出" else "收入").append(',')
                .append(b.source).append(',')
                .append(csv(b.rawText.replace("\n", " ")))
                .append('\n')
        }
        file.writeText("\uFEFF" + sb.toString(), Charsets.UTF_8)
        return file
    }

    private fun csv(s: String) = "\"${s.replace("\"", "\"\"")}\""
}
