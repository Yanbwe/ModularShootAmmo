package org.yanbwe.modularshootammo.ammo;

import org.yanbwe.modularshootammo.ModularShootAmmo;

import net.minecraft.resources.ResourceLocation;

/**
 * 框架状态注册表（{@code modularshoot:states}）中弹药相关状态条目的 ID 常量。
 *
 * <p>对应数据包 JSON（框架注册表命名空间目录）：{@code data/modularshootammo/modularshoot/states/}，
 * 条目 id 由数据包命名空间 + 文件名构成，即 {@code modularshootammo:mag_ammo} 等。</p>
 *
 * <p>写入状态必须经框架 {@code GunState}/{@code PlayerState} 的 setXxx API
 * （自动 markDirty 进节流同步），不要直接改组件。</p>
 */
public final class AmmoStateIds {
    private AmmoStateIds() {}

    /** 状态：枪械弹匣当前弹药数（per-gun，int）。 */
    public static final ResourceLocation MAG_AMMO =
            ResourceLocation.fromNamespaceAndPath(ModularShootAmmo.MODID, "mag_ammo");

    /** 状态：玩家换弹进度 tick（per-player，int）。 */
    public static final ResourceLocation RELOAD_TICK =
            ResourceLocation.fromNamespaceAndPath(ModularShootAmmo.MODID, "reload_tick");

    /** 状态：玩家正在换弹的枪械实例 uuid（per-player，uuid；无换弹时为 null）。 */
    public static final ResourceLocation RELOAD_GUN =
            ResourceLocation.fromNamespaceAndPath(ModularShootAmmo.MODID, "reload_gun");
}
