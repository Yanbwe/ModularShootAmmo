package org.yanbwe.modularshootammo.registry;

import java.util.Optional;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;

/**
 * 弹药类型查询层：从运行时注册表视图读取 {@code modularshootammo:ammo_types} 条目。
 */
public final class AmmoTypeRegistry {
    private AmmoTypeRegistry() {}

    /**
     * 按 id 查询弹药类型。
     *
     * @param ra         运行时注册表视图（数据包注册表在 reload 后重载）
     * @param ammoTypeId 弹药类型注册表 id
     * @return 命中时弹药类型，否则 {@link Optional#empty()}
     */
    public static Optional<AmmoType> get(RegistryAccess ra, ResourceLocation ammoTypeId) {
        return ra.registry(ModularAmmoRegistries.AMMO_TYPES_KEY)
                .flatMap(r -> r.getOptional(ammoTypeId));
    }

    /**
     * 查询弹药类型对应的弹药物品 id。
     *
     * @param ra         运行时注册表视图
     * @param ammoTypeId 弹药类型注册表 id
     * @return 命中时弹药物品 id，否则 {@link Optional#empty()}
     */
    public static Optional<ResourceLocation> resolveItem(RegistryAccess ra, ResourceLocation ammoTypeId) {
        return get(ra, ammoTypeId).map(AmmoType::item);
    }
}
