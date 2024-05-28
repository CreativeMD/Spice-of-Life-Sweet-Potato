package team.creative.solonion.common.item.foodcontainer;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.items.ItemStackHandler;
import team.creative.solonion.api.FoodPlayerData;
import team.creative.solonion.api.OnionFoodContainer;
import team.creative.solonion.api.SOLOnionAPI;
import team.creative.solonion.common.SOLOnion;
import team.creative.solonion.common.mod.OriginsManager;

public class FoodContainerItem extends Item implements OnionFoodContainer {
    
    private String displayName;
    public final int nslots;
    
    public FoodContainerItem(int nslots, String displayName) {
        super(new Properties().stacksTo(1).setNoRepair().food(new FoodProperties.Builder().build()));
        
        this.displayName = displayName;
        this.nslots = nslots;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide && player.isCrouching())
            ((ServerPlayer) player).openMenu(new FoodContainerProvider(displayName), player.blockPosition());
        
        if (!player.isCrouching())
            return processRightClick(world, player, hand);
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
    
    private InteractionResultHolder<ItemStack> processRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isInventoryEmpty(player, stack))
            return InteractionResultHolder.pass(stack);
        
        if (player.canEat(false)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }
    
    private static boolean isInventoryEmpty(Player player, ItemStack container) {
        ItemStackHandler handler = getInventory(container);
        if (handler == null)
            return true;
        
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getFoodProperties(player) != null && OriginsManager.isEdible(player, stack))
                return false;
        }
        return true;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item." + SOLOnion.MODID + ".container.open"));
        super.appendHoverText(stack, level, tooltip, flag);
    }
    
    @Nullable
    public static ItemStackHandler getInventory(ItemStack bag) {
        return (ItemStackHandler) bag.getCapability(Capabilities.ItemHandler.ITEM);
    }
    
    @Override
    public ItemStack getActualFood(Player player, ItemStack stack) {
        ItemStackHandler handler = getInventory(stack);
        if (handler == null)
            return ItemStack.EMPTY;
        
        int bestFoodSlot = getBestFoodSlot(handler, player);
        if (bestFoodSlot < 0)
            return ItemStack.EMPTY;
        return handler.getStackInSlot(bestFoodSlot).copy();
    }
    
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (!(entity instanceof Player))
            return stack;
        
        Player player = (Player) entity;
        ItemStackHandler handler = getInventory(stack);
        if (handler == null)
            return stack;
        
        int bestFoodSlot = getBestFoodSlot(handler, player);
        if (bestFoodSlot < 0)
            return stack;
        
        ItemStack bestFood = handler.getStackInSlot(bestFoodSlot);
        ItemStack foodCopy = bestFood.copy();
        if (bestFood.getFoodProperties(player) != null && !bestFood.isEmpty() && OriginsManager.isEdible(player, foodCopy)) {
            ItemStack result = bestFood.finishUsingItem(world, entity);
            // put bowls/bottles etc. into player inventory
            if (result.getFoodProperties(player) == null) {
                handler.setStackInSlot(bestFoodSlot, ItemStack.EMPTY);
                Player playerEntity = (Player) entity;
                
                if (!playerEntity.getInventory().add(result))
                    playerEntity.drop(result, false);
            }
            
            if (!world.isClientSide)
                EventHooks.onItemUseFinish(player, foodCopy, 0, result);
        }
        
        return stack;
    }
    
    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }
    
    public static int getBestFoodSlot(ItemStackHandler handler, Player player) {
        FoodPlayerData foodList = SOLOnionAPI.getFoodCapability(player);
        
        double maxDiversity = -Double.MAX_VALUE;
        int bestFoodSlot = -1;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack food = handler.getStackInSlot(i);
            
            if (food.getFoodProperties(player) == null || food.isEmpty() || !OriginsManager.isEdible(player, food))
                continue;
            
            double diversityChange = foodList.simulateEat(player, food);
            if (diversityChange > maxDiversity) {
                maxDiversity = diversityChange;
                bestFoodSlot = i;
            }
        }
        
        return bestFoodSlot;
    }
}
