package com.example.autoledger.parser

import com.example.autoledger.data.Bill

/** 从一段文本中提取账单四要素（金额/商户/方向/时间）。 */
object TextParser {

    // 金额：支持 ¥12.34 / 12.34元 / 付款12.34 / -12.34
    private val AMOUNT_RE = Regex("""[¥￥\s]*(-?\d{1,7}(?:\.\d{1,2})?)\s*元?""")

    private val INCOME_WORDS = listOf("收入", "收款", "到账", "退还", "退款", "转入", "已收")
    private val EXPENSE_WORDS = listOf("支出", "付款", "支付", "消费", "扣款", "转出", "已付", "花费")
    // 噪声词：这些行里的数字通常不是消费金额
    private val NOISE_WORDS = listOf("余额", "零钱", "红包封面", "验证码", "积分")

    data class Parsed(
        val amountCents: Long,
        val direction: Int,
        val merchant: String,
        val confidence: Float,
        val ok: Boolean
    )

    fun parse(text: String): Parsed {
        val clean = text.replace(Regex("\\s+"), " ").trim()
        if (NOISE_WORDS.any { clean.contains(it) } && !clean.contains("¥")) {
            return Parsed(0, 0, "", 0f, false)
        }

        // 取所有像金额的数，选最像"实付"的那个（优先带¥、最大、非年份时间）
        val candidates = AMOUNT_RE.findAll(clean).mapNotNull { m ->
            val v = m.groupValues[1].toDoubleOrNull() ?: return@mapNotNull null
            if (v <= 0 || v > 1_000_000) return@mapNotNull null
            val markedYuan = clean.substring(maxOf(0, m.range.first - 1), m.range.last + 1).contains("¥")
            val isTimeLike = clean.contains(String.format("%02d:%02d", v.toInt(), 0)) ||
                    clean.contains("${v.toInt()}年") || clean.contains("${v.toInt()}月")
            Triple(m.groupValues[1], (v * 100).toLong(), markedYuan && !isTimeLike)
        }.toList()

        val chosen = candidates.maxWithOrNull(
            compareBy<Triple<String, Long, Boolean>> { it.second }.thenBy { it.third }
        ) ?: return Parsed(0, 0, "", 0f, false)

        val amountCents = chosen.second

        val direction = when {
            INCOME_WORDS.any { clean.contains(it) } -> 1
            EXPENSE_WORDS.any { clean.contains(it) } -> 0
            clean.contains("-") -> 0
            else -> 0 // 默认支出
        }

        val merchant = extractMerchant(clean)
        val confidence = when {
            chosen.third && merchant.isNotBlank() -> 0.9f
            merchant.isNotBlank() -> 0.7f
            else -> 0.4f
        }
        return Parsed(amountCents, direction, merchant, confidence, amountCents > 0)
    }

    /** 简易商户提取：取"在/至/给/商户/收款方"后的词组，或去掉金额后的首段名词。 */
    private fun extractMerchant(text: String): String {
        val markers = listOf("商户", "收款方", "付款方", "商家", "在", "给", "至")
        for (mk in markers) {
            val i = text.indexOf(mk)
            if (i >= 0 && i + mk.length < text.length) {
                val tail = text.substring(i + mk.length).trim()
                val name = tail.split(Regex("[，,。；;：:¥￥\\d]")).firstOrNull()?.trim()
                if (!name.isNullOrBlank() && name.length in 2..20) return name
            }
        }
        // 兜底：去掉常见前缀和金额后的第一段
        var s = text.replace(Regex("""(微信支付|支付宝|美团|支付成功|付款成功|交易成功|收款|付款)"""), " ")
        s = AMOUNT_RE.replace(s, " ")
        val first = s.trim().split(Regex("[，,。；;：:强势]")).firstOrNull()?.trim() ?: ""
        return if (first.length in 2..20) first else ""
    }
}
