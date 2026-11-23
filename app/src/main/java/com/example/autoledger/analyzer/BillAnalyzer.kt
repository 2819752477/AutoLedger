package com.example.autoledger.analyzer

import android.graphics.Bitmap
import com.example.autoledger.parser.TextParser

/**
 * 账单分析器抽象。
 * P0：LocalRuleAnalyzer（本地正则/关键词，纯本地、快、免费）。
 * P2：AiVisionAnalyzer（截图 + 自有 API，用户填 endpoint+key），实现同一接口即可切换，不改上层调用。
 */
interface BillAnalyzer {
    val name: String
    /** 从文本分析（通知/无障碍读屏） */
    fun analyze(text: String): TextParser.Parsed
    /** 从截图分析（P2 实现；P0 返回不可用） */
    fun analyzeImage(bitmap: Bitmap): TextParser.Parsed = TextParser.Parsed(0, 0, "", 0f, false)
}

/** P0 默认实现：纯本地规则。 */
class LocalRuleAnalyzer : BillAnalyzer {
    override val name = "本地规则"
    override fun analyze(text: String) = TextParser.parse(text)
}
