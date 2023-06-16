package com.tarinoita.solsweetpotato.command;

import static net.minecraft.commands.Commands.argument;

import java.util.Objects;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import net.minecraftforge.fml.ModList;

public final class FoodListCommand {
    
    public static final String name = "solsweetpotato";
    
    @FunctionalInterface
    public interface CommandWithPlayer {
        int run(CommandContext<CommandSourceStack> context, Player target) throws CommandSyntaxException;
    }
    
    @FunctionalInterface
    public interface CommandWithoutArgs {
        int run(CommandContext<CommandSourceStack> context);
    }
    
    public static ArgumentBuilder<CommandSourceStack, ?> withPlayerArgumentOrSender(ArgumentBuilder<CommandSourceStack, ?> base, CommandWithPlayer command) {
        String target = "target";
        return base.executes((context) -> command.run(context, context.getSource().getPlayerOrException()))
                .then(argument(target, EntityArgument.player()).executes((context) -> command.run(context, EntityArgument.getPlayer(context, target))));
    }
    
    public static ArgumentBuilder<CommandSourceStack, ?> withNoArgument(ArgumentBuilder<CommandSourceStack, ?> base, CommandWithoutArgs command) {
        return base.executes((context) -> command.run(context));
    }
    
    public static int displayDiversity(CommandContext<CommandSourceStack> context, Player target) {
        boolean isOp = context.getSource().hasPermission(2);
        boolean isTargetingSelf = isTargetingSelf(context, target);
        if (!isOp && !isTargetingSelf)
            throw new CommandRuntimeException(localizedComponent("no_permissions"));
        
        double diversity = FoodList.get(target).foodDiversity();
        MutableComponent feedback = localizedComponent("diversity_feedback", diversity);
        sendFeedback(context.getSource(), feedback);
        return Command.SINGLE_SUCCESS;
    }
    
    public static int syncFoodList(CommandContext<CommandSourceStack> context, Player target) {
        CapabilityHandler.syncFoodList(target);
        
        sendFeedback(context.getSource(), localizedComponent("sync.success"));
        System.out.println(target.getMaxHealth());
        return Command.SINGLE_SUCCESS;
    }
    
    public static int clearFoodList(CommandContext<CommandSourceStack> context, Player target) {
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
    
    public static int resetPlayerOrigin(CommandContext<CommandSourceStack> context, Player target) {
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
    
    public static int resetAllOrigins(CommandContext<CommandSourceStack> context) {
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
    
    public static void sendFeedback(CommandSourceStack source, MutableComponent message) {
        source.sendSuccess(() -> applyFeedbackStyle(message), true);
    }
    
    public static MutableComponent applyFeedbackStyle(MutableComponent text) {
        return text.withStyle(style -> style.applyFormat(ChatFormatting.DARK_AQUA));
    }
    
    public static boolean isTargetingSelf(CommandContext<CommandSourceStack> context, Player target) {
        return target.is(Objects.requireNonNull(context.getSource().getEntity()));
    }
    
    public static MutableComponent localizedComponent(String path, Object... args) {
        return Localization.localizedComponent("command", localizationPath(path), args);
    }
    
    public static MutableComponent localizedQuantityComponent(String path, int number) {
        return Localization.localizedQuantityComponent("command", localizationPath(path), number);
    }
    
    public static String localizationPath(String path) {
        return FoodListCommand.name + "." + path;
    }
}
