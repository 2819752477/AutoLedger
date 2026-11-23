package com.example.autoledger.analyzer

/**
 * 分析器切换点。P0 固定用 LocalRuleAnalyzer。
 * P2 接自有 AI 时：在这里根据设置里的 endpoint+key 返回 AiVisionAnalyzer。
 */
object AnalyzerProvider {
    @Volatile
    private var current: BillAnalyzer = LocalRuleAnalyzer()

    fun get(): BillAnalyzer = current

    fun useLocal() { current = LocalRuleAnalyzer() }

    // P2 预留：
    // fun useAi(endpoint: String, apiKey: String) { current = AiVisionAnalyzer(endpoint, apiKey) }
}
