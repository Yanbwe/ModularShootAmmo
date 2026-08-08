package org.yanbwe.modularshootammo.ammo;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * 文本解析小工具（仿框架 TooltipUtils.resolveText 同款逻辑，写在 common 层）。
 *
 * <p>{@code "lang:"} 前缀的文本解析为翻译组件（可带参数），否则按字面文本输出。</p>
 */
public final class AmmoText {

    private AmmoText() {}

    /**
     * 解析为聊天组件。
     *
     * @param text 原始文本，支持 {@code "lang:"} 前缀
     * @return 翻译组件或字面文本组件
     */
    public static MutableComponent resolve(String text) {
        return resolve(text, new Object[0]);
    }

    /**
     * 解析为聊天组件；翻译键可携带格式化参数（如 {@code %s}）。
     *
     * @param text 原始文本，支持 {@code "lang:"} 前缀
     * @param args 翻译键的格式化参数（字面文本时忽略）
     * @return 翻译组件或字面文本组件
     */
    public static MutableComponent resolve(String text, Object... args) {
        return text.startsWith("lang:")
                ? Component.translatable(text.substring(5), args)
                : Component.literal(text);
    }
}
