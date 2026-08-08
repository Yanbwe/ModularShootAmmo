package org.yanbwe.modularshootammo.ammo;

import org.yanbwe.modularshootammo.ModularShootAmmo;

import net.minecraft.resources.ResourceLocation;

/**
 * 框架特性注册表（{@code modularshoot:traits}）中弹药相关特性条目的 ID 常量。
 *
 * <p>对应数据包 JSON（框架注册表命名空间目录）：{@code data/modularshootammo/modularshoot/traits/}，
 * 条目 id 由数据包命名空间 + 文件名构成，即 {@code modularshootammo:uses_ammo} 等。</p>
 *
 * <p>特性值读取统一走 {@code TraitMergeService.computeTraits}（含插件合并）；
 * 枪械固有声明优先于默认值。</p>
 */
public final class AmmoTraitIds {
    private AmmoTraitIds() {}

    /** 特性：枪械使用弹匣弹药系统（启用弹药系统）。 */
    public static final ResourceLocation USES_AMMO =
            ResourceLocation.fromNamespaceAndPath(ModularShootAmmo.MODID, "uses_ammo");

    /** 特性：无限弹药，豁免扣弹。 */
    public static final ResourceLocation INFINITE_AMMO =
            ResourceLocation.fromNamespaceAndPath(ModularShootAmmo.MODID, "infinite_ammo");
}
