package com.example.autoledger.pipeline

import android.content.Context
import com.example.autoledger.analyzer.AnalyzerProvider
import com.example.autoledger.classify.CategoryEngine
import com.example.autoledger.data.AppDatabase
import com.example.autoledger.data.Bill
import com.example.autoledger.parser.TextParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 采集 → 分析 → 去重 → 分类 → 入库 的统一入口。 */
class BillRepository(context: Context) {
    private val dao = AppDatabase.get(context).billDao()

    /** 处理一条捕获到的文本（来自通知或无障碍）。返回是否真正入库。 */
    suspend fun ingest(rawText: String, source: String, happenedAt: Long = System.currentTimeMillis()): Boolean =
        withContext(Dispatchers.IO) {
            val parsed: TextParser.Parsed = AnalyzerProvider.get().analyze(rawText)
            if (!parsed.ok) return@withContext false

            val category = CategoryEngine.classify(parsed.merchant, rawText)
            // 去重键：来源 + 金额 + 商户 +  minute（同一分钟内同金额同商户视为同一笔）
            val minute = happenedAt / 60_000
            val key = "$source|${parsed.amountCents}|${parsed.merchant}|$minute"

            if (dao.bySourceKey(key) != null) return@withContext false

            dao.insert(
                Bill(
                    amountCents = parsed.amountCents,
                    direction = parsed.direction,
                    merchant = parsed.merchant,
                    category = category,
                    source = source,
                    sourceKey = key,
                    rawText = rawText.take(500),
                    happenedAt = happenedAt
                )
            )
            true
        }
}
