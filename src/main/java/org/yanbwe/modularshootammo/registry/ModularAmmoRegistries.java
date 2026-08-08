package org.yanbwe.modularshootammo.registry;

import org.yanbwe.modularshootammo.ModularShootAmmo;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

/**
 * 弹药模组两个动态数据包注册表的总声明（仿框架 ModularShootRegistries）：
 * <ul>
 *   <li>{@code modularammo:ammo_types}（弹药类型）——网络 codec 非空，内容同步客户端；
 *   <li>{@code modularammo:gun_ammo_bindings}（枪械→弹药绑定）——网络 codec 非空，
 *       同步客户端：HUD 需要查询绑定。
 * </ul>
 *
 * <p>经 {@link DataPackRegistryEvent.NewRegistry} 在 mod 总线注册，支持 {@code /reload}
 * 热重载；JSON 位于 {@code data/<datapack_namespace>/modularammo/<registry_path>/}。</p>
 */
@EventBusSubscriber(modid = ModularShootAmmo.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModularAmmoRegistries {
    private ModularAmmoRegistries() {}

    /** 注册表 key：{@code modularammo:ammo_types}（弹药类型，同步客户端）。 */
    public static final ResourceKey<Registry<AmmoType>> AMMO_TYPES_KEY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(ModularShootAmmo.MODID, "ammo_types"));

    /** 注册表 key：{@code modularammo:gun_ammo_bindings}（枪械→弹药绑定，同步客户端）。 */
    public static final ResourceKey<Registry<GunAmmoBinding>> GUN_AMMO_BINDINGS_KEY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(ModularShootAmmo.MODID, "gun_ammo_bindings"));

    @SubscribeEvent
    public static void onDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        // 网络 codec 非空 → 条目随连接同步到客户端（客户端 HUD 需要读取弹药类型）
        event.dataPackRegistry(AMMO_TYPES_KEY, AmmoType.CODEC, AmmoType.CODEC);
        // 网络 codec 非空 → 条目随连接同步到客户端（客户端 HUD 需要查询绑定）
        event.dataPackRegistry(GUN_AMMO_BINDINGS_KEY, GunAmmoBinding.CODEC, GunAmmoBinding.CODEC);
    }
}
