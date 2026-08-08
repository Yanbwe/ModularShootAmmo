# 更新记录 / Changelog

## [1.0.0] - 2026-08-08

### 新增 / Added

- **弹匣制弹药系统**：注册 `uses_ammo` / `infinite_ammo` 特性、`mag_ammo` / `reload_tick` / `reload_gun` 状态与 `mag_size` / `reload_time` 属性元数据（数据包驱动，`/reload` 热重载）。
- **射击扣弹**：服务端 `PreShootEvent` 按 `per_shot_cost` 扣弹（支持每发多颗弹壳），创造模式与无限弹药豁免；`ShootPredicate` 阻止空仓/换弹中射击。
- **换弹闭环**：R 键手动换弹 + 空仓扣扳机自动换弹；换弹倒计时支持切枪/收枪/死亡/登出中断；背包备弹跨堆叠扣取（支持 `reserve_limit` 上限）；换弹音效与节流动作栏提示。
- **客户端弹药 HUD**：主手枪显示 `弹匣/备弹`（弹药类型色、空弹匣红色警示、无限 ∞）与换弹进度条，支持配置界面调整。
- **Tooltip**：弹药物品显示归属弹药类型；枪械属性栏后显示弹药类型与每发消耗（双语翻译键）。
- **数据包注册表**：`modularshootammo:ammo_types`（4 种弹药，黄/红/绿/紫四色纹理）与 `modularshootammo:gun_ammo_bindings`（枪械→弹药绑定，网络同步客户端）。
- **Java API**：`ModularAmmoAPI` 支持代码绑定枪械→弹药（跨 reload 存活）。
- **调试命令**：`/modularammo info|ammo|fill|bind`。
- **演示内容**：手枪（12 发）+ 自动散弹枪（6 发 × 2 弹壳）+ 扩容弹匣插件，创造标签动态生成 demo 枪。

### 改进 / Improved

- HUD 配置迁移至 NeoForge 配置系统：偏移、缩放、四角锚点、显示项可实时调整。
- Demo 枪改由框架通用枪物品 + `gun_data` 组件动态生成，删除专属物品与纹理。
- 换弹数学与背包弹药扣取抽为纯逻辑层，附带单元测试（JUnit 5）。

### 修复 / Fixed

- 空仓扣扳机才触发自动换弹（去掉最后一发立即换弹）。
- 弹药属性默认值改为 0 哨兵（声明即显示 tooltip）。
- 备弹上限生效、创造模式不扣背包、补弹前确保 GunData 存在。
- `gun_ammo_bindings` 注册表随网络同步客户端（多人 HUD 绑定查询）。
- 换弹完成时弹药类型缺失的 WARN 日志文本。

### 移除 / Removed

- 删除全部配方（demo 内容改为创造标签获取）。
