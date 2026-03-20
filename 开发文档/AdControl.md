## AutoFlow Ad Frequency Control

### 1. 目标
保持广告可持续变现的同时不破坏无障碍/主流程体验，核心原则是：**冷启动展示，热启动抑制** + **按类型配置冷却** + **友盟等平台失败自动容灾**。

### 2. 激励 + 开屏 + 插屏 + 悬浮球的启用策略
- **激励视频**：用户主动触发、可多次展示，默认不做冷却但需要置于明确奖励场景。
- **开屏广告**：冷启动直接加载，热启动也可以触发但加一个 10 分钟的冷却，且一天不超过 10 次（由 `SplashAdCooldownManager` 维护）；默认热启动还是跳过，避免频繁打扰。
- **插屏广告**：在流程间隙（如任务完成、跳转前）展示，建议每次任务最多一次，同一 `AdType` 冷却 5~10 分钟。
- **悬浮球广告**：作为辅助曝光，放在测试页或浮窗功能中，频率低，可在 `AdPreferenceStore` 中按需开启/关闭。

### 3. 实施细节
1. `AdPreferenceStore`（`platform/ad/AdPreferenceStore.kt`）统一保存每类广告的 `enabled` flag 与 `cooldown`（毫秒）。默认 `SPLASH` 冷却 24h，其他广告默认 0。
2. `SplashAdCooldownManager` 读取 `AdPreferenceStore.getCooldown(AdType.SPLASH)` + 本地 `lastShownAt` 判断当前是否允许加载。
3. `MainActivity` 启动前先判断：  
   - 是否冷启动（`isTaskRoot`、或记录进程中已有 Activity）  
   - `shouldShowSplash()` 返回 true  
   只有两个条件都满足才调用 `AdService.getAdManager().loadSplashAd(...)`；失败或关闭后立即 `markSplashShown()`.
4. `AdRouter` 检查 `preferenceStore.isEnabled(adType)`，若关闭则回调 `onAdFailed`；加载失败时自动切换已注册的其他 `AdPlatformConfig`（按 `priority` 排序）。

### 4. 可调度配置与运行时控制
- `AdPreferenceStore` 可在 Debug/开发环境中通过 `AdService.preferenceStore()` 获取并调整：  
  ```kotlin
  val store = AdService.preferenceStore()
  store.setCooldown(AdType.SPLASH, 24 * 60 * 60 * 1000L)
  store.setEnabled(AdType.SPLASH, true)
  ```
- 把配置能力扩展到远程 JSON 也可以：只需在启动时更新 `sharedPrefs` 并重新构建 `AdRouter`。
- 冷启动时除时间冷却，还可在 `SplashAdCooldownManager` 前加一层 `MaxPerDay` 限制（自己再加一个 `Date` key 即可）。

### 5. 审核友好文本
在隐私弹窗中说明：  
“AutoFlow 会收集必要的设备信息和使用情况，以便保证核心功能流畅、持续优化体验，并在可信赖的合作伙伴协同时尊重相关法规。继续使用即表示已阅读并同意《隐私政策》《用户协议》。”  
表示所有广告在用户同意才会加载且按冷却策略控制，有助于通过平台（如小米）的审查。

### 6. 后续扩展
- 若接入第二平台，只需新建对应包实现 `AdManager`，在 `AdService` 初始化时再注册一个 `AdPlatformConfig`。`AdRouter` 会自动按`priority`尝试并在一个平台失败时 fallback。
- 插屏/悬浮球等可以再细化 `cooldown`（比如 `AdPreferenceStore.setCooldown(AdType.INTERSTITIAL, 5*60*1000L)`），也可以通过远端配置热更新控制频次。

文档如有变更请同步更新 `AdPreferenceStore` 或 `SplashAdCooldownManager`，确保实现与策略一致。
