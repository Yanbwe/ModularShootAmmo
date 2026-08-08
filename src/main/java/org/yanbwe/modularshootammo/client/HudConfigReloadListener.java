package org.yanbwe.modularshootammo.client;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.yanbwe.modularshootammo.ModularShootAmmo;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/**
 * HUD 配置数据包重载监听：读取 {@code data/<ns>/modularammo/hud_config.json}
 * 并写入 {@link HudConfig} 单例（缺省字段用默认值，非法值兜底）。
 *
 * <p>监听 {@link AddReloadListenerEvent}（game bus，客户端），随数据包
 * {@code /reload} 热重载。</p>
 */
@EventBusSubscriber(modid = ModularShootAmmo.MODID, value = Dist.CLIENT)
public final class HudConfigReloadListener extends SimpleJsonResourceReloadListener {

    /** 配置文件 id 路径（{@code data/<ns>/modularammo/hud_config.json} → {@code <ns>:hud_config}）。 */
    private static final String CONFIG_FILE_PATH = "hud_config";

    private static final Gson GSON = new Gson();

    public HudConfigReloadListener() {
        super(GSON, "modularammo");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager,
                         ProfilerFiller profilerFiller) {
        // 目录下存在多个文件时按 id 路径匹配 hud_config（其他文件忽略）
        for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
            if (entry.getKey().getPath().equals(CONFIG_FILE_PATH)) {
                applyConfig(entry.getValue());
                return;
            }
        }
        // 配置文件缺失（如只装了服务端数据包）→ 回退默认值
        HudConfig.reset();
    }

    /** 解析单个配置文件；解析失败回退默认值，绝不抛异常影响 reload。 */
    private static void applyConfig(JsonElement element) {
        try {
            JsonObject obj = GsonHelper.convertToJsonObject(element, CONFIG_FILE_PATH);
            HudConfig.update(
                    GsonHelper.getAsInt(obj, "offset_x", 8),
                    GsonHelper.getAsInt(obj, "offset_y", 8),
                    GsonHelper.getAsDouble(obj, "scale", 1.0),
                    GsonHelper.getAsBoolean(obj, "show_reserve", true),
                    GsonHelper.getAsBoolean(obj, "show_reload_progress", true));
        } catch (RuntimeException ex) {
            ModularShootAmmo.LOGGER.error("Failed to parse hud_config.json, using defaults", ex);
            HudConfig.reset();
        }
    }

    /** 注册监听器（game bus；AddReloadListenerEvent 每次资源重载时触发）。 */
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new HudConfigReloadListener());
    }
}
