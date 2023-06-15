package com.tarinoita.solsweetpotato.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.Objects;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tarinoita.solsweetpotato.SOLSweetPotato;
import com.tarinoita.solsweetpotato.lib.Localization;
import com.tarinoita.solsweetpotato.tracking.CapabilityHandler;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import com.tarinoita.solsweetpotato.tracking.benefits.BenefitsHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SOLSweetPotato.MOD_ID)
public final class FoodListCommand {
    private static final String name = "solsweetpotato";
    
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher()
                .register(literal(name).then(withPlayerArgumentOrSender(literal("sync"), FoodListCommand::syncFoodList))
                        .then(withPlayerArgumentOrSender(literal("clear"), FoodListCommand::clearFoodList))
                        .then(withPlayerArgumentOrSender(literal("diversity"), FoodListCommand::displayDiversity))
                        .then(withPlayerArgumentOrSender(literal("resetOrigin"), FoodListCommand::resetPlayerOrigin))
                        .then(withNoArgument(literal("resetAllOrigins"), FoodListCommand::resetAllOrigins)));
    }
    
    @FunctionalInterface
    private interface CommandWithPlayer {
        int run(CommandContext<CommandSourceStack> context, Player target) throws CommandSyntaxException;
    }
    
    @FunctionalInterface
    private interface CommandWithoutArgs {
        int run(CommandContext<CommandSourceStack> context);
    }
    
    static ArgumentBuilder<CommandSourceStack, ?> withPlayerArgumentOrSender(ArgumentBuilder<CommandSourceStack, ?> base, CommandWithPlayer command) {
        String target = "target";
        return base.executes((context) -> command.run(context, context.getSource().getPlayerOrException()))
                .then(argument(target, EntityArgument.player()).executes((context) -> command.run(context, EntityArgument.getPlayer(context, target))));
    }
    
    static ArgumentBuilder<CommandSourceStack, ?> withNoArgument(ArgumentBuilder<CommandSourceStack, ?> base, CommandWithoutArgs command) {
        return base.executes((context) -> command.run(context));
    }
    
    static int displayDiversity(CommandContext<CommandSourceStack> context, Player target) {
        boolean isOp = context.getSource().hasPermission(2);
        boolean isTargetingSelf = isTargetingSelf(context, target);
        if (!isOp && !isTargetingSelf)
            throw new CommandRuntimeException(localizedComponent("no_permissions"));
        
        double diversity = FoodList.get(target).foodDiversity();
        MutableComponent feedback = localizedComponent("diversity_feedback", diversity);
        sendFeedback(context.getSource(), feedback);
        return Command.SINGLE_SUCCESS;
    }
    
    static int syncFoodList(CommandContext<CommandSourceStack> context, Player target) {
        CapabilityHandler.syncFoodList(target);
        
        sendFeedback(context.getSource(), localizedComponent("sync.success"));
        System.out.println(target.getMaxHealth());
        return Command.SINGLE_SUCCESS;
    }
    
    static int clearFoodList(CommandContext<CommandSourceStack> context, Player target) {
        boolean isOp = context.getSource().hasPermission(2);
        boolean isTargetingSelf = isTargetingSelf(context, target);
        if (!isOp && !isTargetingSelf)
            throw new CommandRuntimeException(localizedComponent("no_permissions"));
        
        FoodList.get(target).clearFood();
        FoodList.get(target).resetFoodsEaten();
        BenefitsHandler.removeAllBenefits(target);
        BenefitsHandler.updatePlayer(target);
        CapabilityHandler.syncFoodList(target);
        
        MutableComponent feedback = localizedComponent("clear.success");
        sendFeedback(context.getSource(), feedback);
        if (!isTargetingSelf) {
            target.displayClientMessage(applyFeedbackStyle(feedback), true);
        }
        
        return Command.SINGLE_SUCCESS;
    }
    
    static int resetPlayerOrigin(CommandContext<CommandSourceStack> context, Player target) {
        boolean isOp = context.getSource().hasPermission(2);
        boolean isTargetingSelf = isTargetingSelf(context, target);
        if (!isOp && !isTargetingSelf)
            throw new CommandRuntimeException(localizedComponent("no_permissions"));
        
        //Origins.cacheInvalidate(target);
        
        MutableComponent feedback;
        if (ModList.get().isLoaded("origins")) {
            feedback = localizedComponent("origin.invalidated");
        } else {
            feedback = localizedComponent("origin.inapplicable");
        }
        sendFeedback(context.getSource(), feedback);
        if (!isTargetingSelf) {
            target.displayClientMessage(applyFeedbackStyle(feedback), true);
        }
        
        return Command.SINGLE_SUCCESS;
    }
    
    static int resetAllOrigins(CommandContext<CommandSourceStack> context) {
        boolean isOp = context.getSource().hasPermission(2);
        if (!isOp)
            throw new CommandRuntimeException(localizedComponent("no_permissions"));
        
        //Origins.clearCache();
        
        MutableComponent feedback;
        if (ModList.get().isLoaded("origins")) {
            feedback = localizedComponent("origin.cleared");
        } else {
            feedback = localizedComponent("origin.inapplicable");
        }
        sendFeedback(context.getSource(), feedback);
        
        return Command.SINGLE_SUCCESS;
    }
    
    static void sendFeedback(CommandSourceStack source, MutableComponent message) {
        source.sendSuccess(() -> applyFeedbackStyle(message), true);
    }
    
    private static MutableComponent applyFeedbackStyle(MutableComponent text) {
        return text.withStyle(style -> style.applyFormat(ChatFormatting.DARK_AQUA));
    }
    
    static boolean isTargetingSelf(CommandContext<CommandSourceStack> context, Player target) {
        return target.is(Objects.requireNonNull(context.getSource().getEntity()));
    }
    
    static MutableComponent localizedComponent(String path, Object... args) {
        return Localization.localizedComponent("command", localizationPath(path), args);
    }
    
    static MutableComponent localizedQuantityComponent(String path, int number) {
        return Localization.localizedQuantityComponent("command", localizationPath(path), number);
    }
    
    static String localizationPath(String path) {
        return FoodListCommand.name + "." + path;
    }
}
