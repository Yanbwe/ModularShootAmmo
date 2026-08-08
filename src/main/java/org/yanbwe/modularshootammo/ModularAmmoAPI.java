package org.yanbwe.modularshootammo;

import java.util.Optional;

import org.yanbwe.modularshoot.plugin.TraitMergeService;
import org.yanbwe.modularshootammo.ammo.AmmoTraitIds;
import org.yanbwe.modularshootammo.registry.AmmoType;
import org.yanbwe.modularshootammo.registry.AmmoTypeRegistry;
import org.yanbwe.modularshootammo.registry.GunAmmoBindingRegistry;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * ModularShootAmmo 公开门面：弹药绑定、弹药类型查询与启用/豁免特性判断。
 */
public final class ModularAmmoAPI {
    private ModularAmmoAPI() {}

    /**
     * Java API 绑定（命令/其他模组调用），与数据包绑定共享查询接口；
     * Java 优先且跨 {@code /reload} 存活。
     *
     * @param gunId      枪械 id（{@code modularshoot:guns} 注册表）
     * @param ammoTypeId 弹药类型 id（{@code modularammo:ammo_types} 注册表）
     */
    public static void bindGun(ResourceLocation gunId, ResourceLocation ammoTypeId) {
        GunAmmoBindingRegistry.bind(gunId, ammoTypeId);
    }

    /**
     * 查询枪械绑定的弹药类型 id（Java 优先 + 数据包兜底）。
     *
     * @param ra    运行时注册表视图
     * @param gunId 枪械 id
     * @return 绑定命中时的弹药类型 id，否则 {@link Optional#empty()}
     */
    public static Optional<ResourceLocation> getAmmoTypeIdForGun(RegistryAccess ra, ResourceLocation gunId) {
        return GunAmmoBindingRegistry.get(ra, gunId);
    }

    /**
     * 按 id 查询弹药类型。
     *
     * @param ra         运行时注册表视图
     * @param ammoTypeId 弹药类型注册表 id
     * @return 命中时弹药类型，否则 {@link Optional#empty()}
     */
    public static Optional<AmmoType> getAmmoType(RegistryAccess ra, ResourceLocation ammoTypeId) {
        return AmmoTypeRegistry.get(ra, ammoTypeId);
    }

    /**
     * 枪械是否启用弹药系统（{@code modularshootammo:uses_ammo} 最终特性值，含插件合并）。
     *
     * @param gun 枪械物品
     * @param ra  运行时注册表视图
     * @return 未声明特性一律 {@code false}
     */
    public static boolean isUsesAmmo(ItemStack gun, RegistryAccess ra) {
        return TraitMergeService.computeTraits(gun, ra).getOrDefault(AmmoTraitIds.USES_AMMO, false);
    }

    /**
     * 枪械是否豁免扣弹（{@code modularshootammo:infinite_ammo} 最终特性值，含插件合并）。
     *
     * @param gun 枪械物品
     * @param ra  运行时注册表视图
     * @return 未声明特性一律 {@code false}
     */
    public static boolean isInfiniteAmmo(ItemStack gun, RegistryAccess ra) {
        return TraitMergeService.computeTraits(gun, ra).getOrDefault(AmmoTraitIds.INFINITE_AMMO, false);
    }
}
