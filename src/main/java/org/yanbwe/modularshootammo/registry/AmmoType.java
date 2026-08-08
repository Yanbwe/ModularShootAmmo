package org.yanbwe.modularshootammo.registry;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

/**
 * 弹药类型定义（数据包注册表 {@code modularammo:ammo_types} 的条目）。
 *
 * <p>JSON 格式：</p>
 * <pre>{@code
 * {
 *   "name": "lang:modularshootammo.ammo_type.rifle_ammo",  // 显示名，支持 "lang:" 前缀
 *   "color": "#FFAA00",                                     // 标识颜色（"#RRGGBB" 字符串）
 *   "item": "modularshootammo:rifle_ammo",                  // 弹药物品 id（必填）
 *   "reserve_limit": 128,                                   // 可选：备弹上限
 *   "per_shot_cost": 1,                                     // 可选：每发消耗，默认 1
 *   "reload_sound": "modularshootammo:reload_start"         // 可选：换弹音效覆盖
 * }
 * }</pre>
 *
 * @param name         显示名，支持 {@code "lang:"} 前缀
 * @param color        标识颜色 ARGB int（JSON 中是 {@code "#RRGGBB"} 字符串）
 * @param item         弹药物品 id
 * @param reserveLimit 可选备弹上限；未声明为 {@code null}
 * @param perShotCost  每发消耗弹药数，默认 1
 * @param reloadSound  可选换弹音效覆盖；未声明为 {@code null}
 */
public record AmmoType(
        String name,
        int color,
        ResourceLocation item,
        @Nullable Integer reserveLimit,
        int perShotCost,
        @Nullable ResourceLocation reloadSound
) {
    public static final Codec<AmmoType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(AmmoType::name),
            Codec.STRING.xmap(AmmoType::parseHexColor, AmmoType::toHexColor)
                    .fieldOf("color").forGetter(AmmoType::color),
            ResourceLocation.CODEC.fieldOf("item").forGetter(AmmoType::item),
            Codec.INT.optionalFieldOf("reserve_limit")
                    .forGetter(t -> Optional.ofNullable(t.reserveLimit())),
            Codec.INT.optionalFieldOf("per_shot_cost", 1).forGetter(AmmoType::perShotCost),
            ResourceLocation.CODEC.optionalFieldOf("reload_sound")
                    .forGetter(t -> Optional.ofNullable(t.reloadSound()))
    ).apply(instance, (name, color, item, reserveLimit, perShotCost, reloadSound) ->
            new AmmoType(name, color, item, reserveLimit.orElse(null), perShotCost, reloadSound.orElse(null))));

    /**
     * 解析十六进制颜色字符串为 ARGB int（alpha 恒为 0xFF）。
     *
     * <p>接受 {@code "#RRGGBB"} 与 {@code "RRGGBB"} 两种格式；解析失败回退白色
     * （{@code 0xFFFFFFFF}），颜色错误永远不会导致崩溃（仿框架
     * TooltipUtils.parseHexColor，但写在 common 代码，不引用框架 client 包）。</p>
     *
     * @param hex 颜色字符串，可能为空或非法
     * @return ARGB 颜色值，失败时 {@code 0xFFFFFFFF}
     */
    public static int parseHexColor(String hex) {
        try {
            String clean = hex.startsWith("#") ? hex.substring(1) : hex;
            return 0xFF000000 | Integer.parseInt(clean, 16);
        } catch (NumberFormatException ex) {
            return 0xFFFFFFFF;
        }
    }

    /** CODEC 编码方向：ARGB int → {@code "#RRGGBB"} 字符串（忽略 alpha 通道）。 */
    private static String toHexColor(int argb) {
        return String.format("#%06X", argb & 0xFFFFFF);
    }
}
