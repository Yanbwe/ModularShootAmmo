package org.yanbwe.modularshootammo.registry;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.yanbwe.modularshoot.ModularShootAPI;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;

/**
 * 枪械→弹药绑定查询层，Java API 与数据包双路合并（仿框架 GunRegistry 双路模式）。
 *
 * <p>Java 绑定存于静态 {@link ConcurrentHashMap}，跨 {@code /reload} 存活，且在查询时
 * 优先于数据包条目；未命中的回退查数据包注册表（数据包条目随 reload 热重载）。</p>
 */
public final class GunAmmoBindingRegistry {
    private static final Map<ResourceLocation, ResourceLocation> JAVA_BINDINGS = new ConcurrentHashMap<>();

    private GunAmmoBindingRegistry() {}

    /**
     * 注册 Java API 绑定：枪械 → 弹药类型。
     *
     * <p>同时经 {@link ModularShootAPI#markJavaApiRegistered} 认领注册表条目，
     * 使数据包中同 id 条目在查询层被 Java 绑定遮蔽。</p>
     *
     * @param gunId      枪械 id（{@code modularshoot:guns} 注册表）
     * @param ammoTypeId 弹药类型 id（{@code modularshootammo:ammo_types} 注册表）
     */
    public static void bind(ResourceLocation gunId, ResourceLocation ammoTypeId) {
        JAVA_BINDINGS.put(gunId, ammoTypeId);
        ModularShootAPI.markJavaApiRegistered(ModularAmmoRegistries.GUN_AMMO_BINDINGS_KEY, gunId);
    }

    /**
     * 查询枪械绑定的弹药类型 id：Java 条目优先，未命中查数据包注册表
     * （{@code /reload} 重载存活）。
     *
     * @param ra    运行时注册表视图
     * @param gunId 枪械 id
     * @return 绑定命中时的弹药类型 id，否则 {@link Optional#empty()}
     */
    public static Optional<ResourceLocation> get(RegistryAccess ra, ResourceLocation gunId) {
        ResourceLocation java = JAVA_BINDINGS.get(gunId);
        if (java != null) {
            return Optional.of(java);
        }
        return ra.registry(ModularAmmoRegistries.GUN_AMMO_BINDINGS_KEY)
                .flatMap(r -> r.getOptional(gunId))
                .map(GunAmmoBinding::ammoType);
    }
}
