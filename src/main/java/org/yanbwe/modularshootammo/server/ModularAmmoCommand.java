package org.yanbwe.modularshootammo.server;

import java.util.Optional;
import java.util.UUID;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import org.yanbwe.modularshoot.ModularShootAPI;
import org.yanbwe.modularshoot.state.GunState;
import org.yanbwe.modularshoot.state.PlayerState;
import org.yanbwe.modularshootammo.ModularAmmoAPI;
import org.yanbwe.modularshootammo.ModularShootAmmo;
import org.yanbwe.modularshootammo.ammo.AmmoInventoryHelper;
import org.yanbwe.modularshootammo.ammo.AmmoStateIds;
import org.yanbwe.modularshootammo.registry.AmmoType;
import org.yanbwe.modularshootammo.registry.AmmoTypeRegistry;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Registers the {@code /modularammo} debug command tree on the NeoForge game
 * event bus (设计文档 §调试命令 / 实施计划任务 9).
 *
 * <p>The root node is gated behind op permission level {@code 2} and exposes
 * four subcommands:</p>
 * <ul>
 *   <li>{@code info} — dumps the main-hand gun's ammo state: whether the ammo
 *       system is enabled ({@code uses_ammo}) / exempt ({@code infinite_ammo}),
 *       the bound ammo type id, magazine fill {@code mag}/{@code mag_size},
 *       reserve count, {@code reload_tick}/{@code reload_gun} and the final
 *       {@code reload_time} value.</li>
 *   <li>{@code ammo <ammoType> <count>} — gives the executor ammo items of the
 *       specified ammo type.</li>
 *   <li>{@code fill} — refills the main-hand gun's magazine to its final
 *       {@code mag_size}.</li>
 *   <li>{@code bind <gun> <ammoType>} — Java-binds a gun id to an ammo type id
 *       (in-memory, not persisted; effective immediately).</li>
 * </ul>
 */
@EventBusSubscriber(modid = ModularShootAmmo.MODID)
public final class ModularAmmoCommand {

    /** Op permission level required to run any {@code /modularammo} subcommand. */
    private static final int PERMISSION_LEVEL = 2;

    private ModularAmmoCommand() {
    }

    /**
     * Fired on the NeoForge game bus whenever the command dispatcher is rebuilt.
     *
     * @param event the register-commands event carrying the dispatcher
     */
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("modularammo")
                .requires(src -> src.hasPermission(PERMISSION_LEVEL))
                .then(Commands.literal("info").executes(ctx -> info(ctx.getSource())))
                .then(Commands.literal("ammo")
                        .then(Commands.argument("ammoType", ResourceLocationArgument.id())
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 9999))
                                        .executes(ctx -> giveAmmo(ctx,
                                                ResourceLocationArgument.getId(ctx, "ammoType"),
                                                IntegerArgumentType.getInteger(ctx, "count"))))))
                .then(Commands.literal("fill").executes(ctx -> fill(ctx.getSource())))
                .then(Commands.literal("bind")
                        .then(Commands.argument("gun", ResourceLocationArgument.id())
                                .then(Commands.argument("ammoType", ResourceLocationArgument.id())
                                        .executes(ctx -> bind(ctx,
                                                ResourceLocationArgument.getId(ctx, "gun"),
                                                ResourceLocationArgument.getId(ctx, "ammoType")))))));
    }

    // ------------------------------------------------------------------
    // info
    // ------------------------------------------------------------------

    /**
     * Reports the main-hand gun's ammo state line by line; fails when the
     * main hand does not hold a gun.
     */
    private static int info(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack gun = player.getMainHandItem();
        RegistryAccess ra = player.registryAccess();
        if (!ModularShootAPI.isGun(gun, ra)) {
            source.sendFailure(Component.literal("Main hand is not a gun").withStyle(ChatFormatting.RED));
            return 0;
        }
        GunState gs = ModularShootAPI.getState(gun, player);
        PlayerState ps = ModularShootAPI.getPlayerState(player);

        boolean usesAmmo = ModularAmmoAPI.isUsesAmmo(gun, ra);
        boolean infinite = ModularAmmoAPI.isInfiniteAmmo(gun, ra);
        Optional<ResourceLocation> typeId = AmmoService.resolveAmmoTypeId(gun, ra, player.level());
        Optional<AmmoType> type = typeId.flatMap(id -> AmmoTypeRegistry.get(ra, id));
        int mag = gs == null ? 0 : gs.getInt(AmmoStateIds.MAG_AMMO);
        int magSize = AmmoService.magSizeOf(player, ra);
        int reserve = type.map(AmmoType::item)
                .map(id -> AmmoInventoryHelper.countAmmo(player.getInventory().items,
                        BuiltInRegistries.ITEM.get(id)))
                .orElse(0);
        int reloadTick = ps.getInt(AmmoStateIds.RELOAD_TICK);
        UUID reloadGun = ps.getUuid(AmmoStateIds.RELOAD_GUN);
        int reloadTime = AmmoService.reloadTimeOf(player, ra);

        source.sendSuccess(() -> Component.literal("[ModularAmmo] " + gun.getHoverName().getString())
                .withStyle(ChatFormatting.AQUA), false);
        source.sendSuccess(() -> Component.literal("  uses_ammo: " + usesAmmo), false);
        source.sendSuccess(() -> Component.literal("  infinite_ammo: " + infinite), false);
        source.sendSuccess(() -> Component.literal("  ammo_type: " + typeId.map(ResourceLocation::toString).orElse("<none>")), false);
        source.sendSuccess(() -> Component.literal("  mag: " + mag + " / " + magSize), false);
        source.sendSuccess(() -> Component.literal("  reserve: " + reserve), false);
        source.sendSuccess(() -> Component.literal("  reload_tick: " + reloadTick), false);
        source.sendSuccess(() -> Component.literal("  reload_gun: " + (reloadGun == null ? "<none>" : reloadGun)), false);
        source.sendSuccess(() -> Component.literal("  reload_time: " + reloadTime), false);
        return 1;
    }

    // ------------------------------------------------------------------
    // ammo
    // ------------------------------------------------------------------

    /**
     * Gives the executor {@code count} ammo items of the given ammo type;
     * fails when the ammo type is not registered.
     */
    private static int giveAmmo(CommandContext<CommandSourceStack> context, ResourceLocation ammoTypeId, int count)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        Optional<AmmoType> type = AmmoTypeRegistry.get(player.registryAccess(), ammoTypeId);
        if (type.isEmpty()) {
            source.sendFailure(Component.literal("Unknown ammo type: " + ammoTypeId).withStyle(ChatFormatting.RED));
            return 0;
        }
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(type.get().item()), count);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        source.sendSuccess(() -> Component.literal("Gave " + count + " x " + ammoTypeId)
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    // ------------------------------------------------------------------
    // fill
    // ------------------------------------------------------------------

    /**
     * Refills the main-hand gun's magazine to its final {@code mag_size};
     * fails when the main hand does not hold a gun.
     */
    private static int fill(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack gun = player.getMainHandItem();
        RegistryAccess ra = player.registryAccess();
        if (!ModularShootAPI.isGun(gun, ra)) {
            source.sendFailure(Component.literal("Main hand is not a gun").withStyle(ChatFormatting.RED));
            return 0;
        }
        GunState gs = ModularShootAPI.getState(gun, player);
        if (gs == null) {
            source.sendFailure(Component.literal("Gun has no state data").withStyle(ChatFormatting.RED));
            return 0;
        }
        gs.setInt(AmmoStateIds.MAG_AMMO, AmmoService.magSizeOf(player, ra));
        source.sendSuccess(() -> Component.literal("Magazine refilled to " + AmmoService.magSizeOf(player, ra))
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    // ------------------------------------------------------------------
    // bind
    // ------------------------------------------------------------------

    /**
     * Java-binds a gun id to an ammo type id (in-memory only, not persisted;
     * effective immediately for existing guns of that id).
     */
    private static int bind(CommandContext<CommandSourceStack> context, ResourceLocation gunId,
                            ResourceLocation ammoTypeId) throws CommandSyntaxException {
        ModularAmmoAPI.bindGun(gunId, ammoTypeId);
        context.getSource().sendSuccess(
                () -> Component.literal("Bound " + gunId + " -> " + ammoTypeId).withStyle(ChatFormatting.GREEN),
                false);
        return 1;
    }
}
