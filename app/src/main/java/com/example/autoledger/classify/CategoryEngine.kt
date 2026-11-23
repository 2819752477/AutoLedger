package com.example.autoledger.classify

/** 关键词 → 分类。顺序匹配，先命中先用。规则可持续补充以修识图分类错误。 */
object CategoryEngine {

    private val RULES: List<Pair<String, List<String>>> = listOf(
        "餐饮" to listOf("外卖", "餐", "饭", "面", "粉", "奶茶", "咖啡", "火锅", "烧烤", "小吃", "肯德基", "麦当劳", "美团", "饿了么"),
        "买菜" to listOf("生鲜", "水果", "超市", "便利店", "菜", "盒马", "叮咚"),
        "交通" to listOf("打车", "地铁", "公交", "高铁", "动车", "火车", "机票", "加油", "停车", "单车", "哈啰", "滴滴"),
        "购物" to listOf("淘宝", "京东", "拼多多", "唯品会", "服饰", "鞋", "数码", "日用品"),
        "通讯" to listOf("话费", "流量", "宽带", "网费", "中国移动", "中国联通", "中国电信"),
        "居家" to listOf("房租", "水电", "电费", "水费", "物业", "燃气", "家具"),
        "娱乐" to listOf("电影", "游戏", "网咖", "KTV", "演出", "会员", "Steam"),
        "医疗" to listOf("医院", "药房", "药店", "诊所", "体检"),
        "学习" to listOf("书店", "图书", "课程", "学费", "培训"),
        "人情" to listOf("红包", "转账", "礼金", "份子钱")
    )

    fun classify(merchant: String, rawText: String): String {
        val text = "$merchant $rawText"
        for ((cat, kws) in RULES) {
            if (kws.any { text.contains(it) }) return cat
        }
        return "未分类"
    }
}
