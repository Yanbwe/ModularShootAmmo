package org.yanbwe.modularshootammo.creative;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.yanbwe.modularshoot.registry.ModularShootRegistries;
import org.yanbwe.modularshoot.registry.gun.GunRegistry;
import org.yanbwe.modularshootammo.ModularShootAmmo;
import org.yanbwe.modularshootammo.item.ModularAmmoItems;

/**
 * 模组创造模式标签页：收纳全部默认物品（弹药、插件与 demo 枪）。
 *
 * <p>demo 枪不注册专属物品，统一使用框架的通用枪物品
 * （{@code modularshoot:gun}），枪型由栈上的 {@code gun_data} 组件决定。
 * 标签页中的枪物品通过 {@link BuildCreativeModeTabContentsEvent} 动态填充
 * （仿框架 {@code ModularShootCreativeTabs} 的模式）：枚举
 * {@code modularshoot:guns} 注册表中命名空间为 {@code modularshootammo}
 * 的枪定义，为每把枪生成一个携带 {@code gun_data} 组件的枪物品栈。</p>
 */
@EventBusSubscriber(modid = ModularShootAmmo.MODID)
public final class ModularAmmoCreativeTabs {

    /** 创造标签页注册表。 */
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModularShootAmmo.MODID);

    /**
     * 模组创造标签页的 {@link ResourceKey}。
     *
     * <p>既用作 {@link DeferredRegister} 的注册名（{@code ammo}），也用作
     * {@link #onBuildCreativeTab(BuildCreativeModeTabContentsEvent)} 中的身份
     * 判断依据。</p>
     */
    public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(ModularShootAmmo.MODID, "ammo"));

    /** 模组主标签页，图标为步枪弹药。 */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            TABS.register("ammo", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modularshootammo"))
                    .icon(() -> new ItemStack(ModularAmmoItems.RIFLE_AMMO))
                    .displayItems((params, output) -> {
                        output.accept(ModularAmmoItems.PISTOL_AMMO.get());
                        output.accept(ModularAmmoItems.RIFLE_AMMO.get());
                        output.accept(ModularAmmoItems.SHOTGUN_AMMO.get());
                        output.accept(ModularAmmoItems.SNIPER_AMMO.get());
                        output.accept(ModularAmmoItems.DEMO_EXTENDED_MAG.get());
                    })
                    .build());

    private ModularAmmoCreativeTabs() {
    }

    /**
     * 在创造标签页构建事件中动态填充本模组的 demo 枪。
     *
     * <p>demo 枪不注册专属物品，统一使用框架的通用枪物品
     * {@code modularshoot:gun}，枪型由栈上的 {@code gun_data} 组件决定
     * （仿框架 {@code ModularShootCreativeTabs#onBuildCreativeTab} 模式）。
     * 事件触发时遍历 {@code modularshoot:guns} 动态注册表中命名空间为
     * {@code modularshootammo} 的枪定义，为每把枪生成一个携带
     * {@code gun_data} 组件的枪物品栈，并刷新属性修饰符（否则枪械统计为 0、
     * 无法开火）。该事件为 mod bus 事件（{@code IModBusEvent}），
     * 会由 {@code @EventBusSubscriber} 默认的 GAME bus 自动路由。</p>
     *
     * @param event 创造标签页内容构建事件
     */
    @SubscribeEvent
    public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        // 只处理本模组的标签页。
        if (!event.getTabKey().equals(TAB_KEY)) {
            return;
        }
        // 事件参数中的 holders provider 在运行时是 RegistryAccess
        // （创造物品栏传入 localPlayer.level().registryAccess()）。
        if (!(event.getParameters().holders() instanceof RegistryAccess registryAccess)) {
            return;
        }
        // 遍历框架 guns 注册表中属于本模组命名空间的枪。
        registryAccess.registry(ModularShootRegistries.GUNS_KEY).ifPresent(guns ->
                guns.keySet().stream()
                        .filter(gunId -> gunId.getNamespace().equals(ModularShootAmmo.MODID))
                        .forEach(gunId -> {
                            ItemStack gunStack = GunRegistry.createGunStack(gunId, registryAccess);
                            event.accept(gunStack);
                        }));
    }
}
