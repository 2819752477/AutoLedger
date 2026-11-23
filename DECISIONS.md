# AutoLedger 决策记录

生成时间：2026-11-23

## 已定决策

1. **工具链**：完整安装（JDK17 + Android SDK35 + Gradle8.9）——已完成。
   - JDK: /opt/jdk17
   - SDK: /opt/android-sdk
   - Gradle: /opt/gradle/gradle-8.9
   - 注意：adb 二进制在 proot 下 Exec format error，编译不依赖它。
2. **P0 采集通道**：通知监听 + 无障碍读屏双通道（用户选择"推荐"）。
3. **P0 识别与分类**：本地规则引擎（正则 + 关键词）为主，**同时预留自有 AI API 接口**（用户纠正后的选择）。
   - P0：本地规则跑通采集→提取→分类→入库→CSV 全流程。
   - 接口设计：`BillAnalyzer` 抽象出 `analyze(text)` / `analyze(image)`，P0 用 `LocalRuleAnalyzer` 实现；P2 新增 `AiVisionAnalyzer`（用户填 endpoint+key）即可切换，不改上层。

## 延后项

- P2：自有 AI API 识图分析（用户明确可晚实现）。
- P1：规则可视化编辑、悬浮球/通知卡片触发。
- P3：分摊/报销语义（对齐 dsh 财务流程口径）。

## 约束

- 不破解咔皮、不绕过付费；纯独立实现。
- 数据本地优先；CSV 导出对接 dsh 财务分析流程。
