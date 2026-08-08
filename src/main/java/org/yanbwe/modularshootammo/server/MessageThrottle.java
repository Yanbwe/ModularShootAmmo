package org.yanbwe.modularshootammo.server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;

/**
 * 动作栏提示节流（服务端）。
 *
 * <p>以 {@link ServerPlayer} 所在 level 的 {@code gameTime} 为时间轴：
 * 同一玩家同一 key 在 {@code intervalTicks} 内不重复放行。登出时由
 * {@link AmmoReloadTickHandler#onPlayerLoggedOut} 调用 {@link #clear} 清理记录。</p>
 */
public final class MessageThrottle {

    /** 玩家 UUID → (提示 key → 上次放行的 gameTime) */
    private static final Map<UUID, Map<String, Integer>> LAST_SENT_TICK = new HashMap<>();

    private MessageThrottle() {}

    /**
     * 判断该提示是否应发送：intervalTicks 内同 key 不重复放行。
     *
     * @param player        目标玩家（服务端）
     * @param key           提示键，形如 {@code "reload_in_progress"}、
     *                      {@code "lang:modularshootammo.reload.start"}
     * @param intervalTicks 最小间隔（tick）；放行时记录当前 gameTime
     * @return {@code true} 表示应发送（并记录本次发送时间）
     */
    public static boolean shouldSend(ServerPlayer player, String key, int intervalTicks) {
        Map<String, Integer> byKey = LAST_SENT_TICK.computeIfAbsent(player.getUUID(), uuid -> new HashMap<>());
        int now = (int) player.level().getGameTime();
        Integer last = byKey.get(key);
        if (last != null && now - last < intervalTicks) {
            return false;
        }
        byKey.put(key, now);
        return true;
    }

    /** 清理某玩家的全部节流记录（登出/死亡时调用）。 */
    public static void clear(UUID playerId) {
        LAST_SENT_TICK.remove(playerId);
    }
}
