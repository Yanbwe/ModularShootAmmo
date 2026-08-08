# ModularShootAmmo（模块化射击·弹药 / Modular Shoot Ammo）

一个基于 **NeoForge 1.21.1** 的**弹药 addon 模组**，为 [ModularShoot](https://github.com/Yanbwe/ModularShoot)（模块化射击框架）添加弹匣制弹药、换弹与弹药 HUD 系统。

An **ammo addon mod** for **NeoForge 1.21.1** that brings magazine-based ammunition, reloading, and an ammo HUD to the [ModularShoot](https://github.com/Yanbwe/ModularShoot) framework.

# 特色 / Features

## 弹匣制弹药 / Magazine-Based Ammo

枪械通过数据包声明 `uses_ammo` 特性即启用弹药系统，弹匣余量、容量与换弹时间全部走框架的属性和状态管线，可被插件修饰（如扩容弹匣 +15 弹容量）。

A gun enables the ammo system by declaring the `uses_ammo` feature via data pack. Magazine count, capacity, and reload time all flow through the framework's attribute and state pipeline, and can be modified by plugins (e.g., an extended magazine adds +15 capacity).

## 弹药类型 / Ammo Types

支持注册任意多种弹药类型（`ammo_types` 注册表）：颜色、每发消耗（霰弹枪每发 2 弹壳）、备弹上限、专属换弹音效均可配置。

Supports registering any number of ammo types (the `ammo_types` registry): color, per-shot cost (a shotgun spends 2 shells per shot), reserve ammo cap, and dedicated reload sounds are all configurable.

## 弹药 HUD / Ammo HUD

主手枪在屏幕角落显示弹药相关信息，可以通过配置调整。

While holding a gun, ammo-related info is shown in a corner of the screen, adjustable through configuration.

## 数据包驱动 / Data-Driven

和模块化射击一样。

Just like ModularShoot.

## 缺点 / Drawbacks

- 一把枪只能绑一种弹药；
- 预置的纹理很丑。

- A gun can only be bound to one ammo type;
- The built-in textures are ugly.

# 依赖 / Requirements

- **[ModularShoot](https://github.com/Yanbwe/ModularShoot) 0.1.0+**（框架模组，必需 / required）

# 使用文档 / Documentation

[Yanbwe's WIKI](https://yanbwe.github.io/Yanbwe-Wiki/modularshootammo/)

# 常见问题 / FAQ

**Q1：怎么换弹？我没看到这个模组添加的按键。**
**A1**：本模组没有添加按键，而是直接使用模块化射击提供的“动作键”。

**Q1: How do I reload? I don't see any keybind added by this mod.**
**A1**: This mod doesn't add any keybinds; it uses the “action key” provided by ModularShoot directly.

**Q2：这下总有可以玩的东西了吧？**
**A2**：有四种弹药和两把枪，仅作演示作用（可能还有统一作用）。

**Q2: So now there's finally something to play with?**
**A2**: Yes — four ammo types and two guns, for demonstration purposes (and perhaps for unification).

**Q3：为什么不直接内置进模块化射击里面？**
**A3**：一是我不喜欢这个东西；二是证明模块化射击的能力——本模组没有用 mixin。

**Q3: Why not just build it into ModularShoot?**
**A3**: First, I don't like it; second, to prove ModularShoot's capabilities — this mod uses no mixins.

# 许可证 / License

GPLv3
