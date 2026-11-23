# AutoLedger

自动记账 APK —— 复刻咔皮免费自动记账能力，补齐淘宝闪购缺口。纯独立实现，数据本地，不破解、不绕过任何付费。

## P0 已完成功能

- 通知监听采集（微信/支付宝/美团/拼多多）
- 无障碍读屏采集（含淘宝包名，补咔皮缺口）
- 本地规则解析金额/商户/收支 + 关键词分类
- 四层去重（简化版：来源+金额+商户+分钟）
- Room 本地数据库
- CSV 导出到 `Download/DSH-Go/auto-ledger/`，对接 dsh 财务分析
- AI 接口预留（`BillAnalyzer` / `AnalyzerProvider`，P2 接自有 API）

## 云编译（推荐）

1. 把本目录推到 GitHub 新仓库（main 或 master 分支）。
2. GitHub Actions 会自动触发 `.github/workflows/build.yml`。
3. 编译完成后，在 Actions 运行页下载 artifact `autoledger-debug`，得到 APK。

也可手动触发：Actions → Build AutoLedger APK → Run workflow。

## 本地编译（需 x86_64 或可运行 ELF 的环境）

```sh
./gradlew assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

注意：手机 proot 环境无法执行 aapt2 等原生二进制，**不能本地编译**（详见 BUILD_STATUS.md）。

## 装机使用

1. 安装 APK 后打开，点「开启无障碍采集」→ 系统设置里打开 AutoLedger 服务。
2. 点「开启通知采集」→ 授权通知使用权。
3. 付款后自动记账；「刷新账单」查看，「导出 CSV」给 dsh 分析。

## 路线

- P1：规则可视化编辑、悬浮球/通知卡片触发
- P2：自有 AI API 识图（实现 `AiVisionAnalyzer` 并在 `AnalyzerProvider` 切换）
- P3：分摊/报销语义
