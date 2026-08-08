package org.yanbwe.modularshootammo.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

/**
 * 枪械→弹药类型绑定（数据包注册表 {@code modularammo:gun_ammo_bindings} 的条目）。
 *
 * <p>JSON 格式：</p>
 * <pre>{@code
 * {
 *   "ammo_type": "modularshootammo:rifle_ammo"
 * }
 * }</pre>
 *
 * <p>条目 key 即枪械 id（如 {@code modularshootammo:demo_pistol}），表示该枪使用的
 * {@code modularshootammo:ammo_types} 弹药类型。条目随网络同步到客户端（HUD 查询绑定用）。</p>
 *
 * @param ammoType 绑定的弹药类型注册表 id
 */
public record GunAmmoBinding(ResourceLocation ammoType) {

    public static final Codec<GunAmmoBinding> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("ammo_type").forGetter(GunAmmoBinding::ammoType)
            ).apply(instance, GunAmmoBinding::new));
}
