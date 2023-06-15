package com.tarinoita.solsweetpotato.item.foodcontainer;

import javax.annotation.Nullable;

import com.tarinoita.solsweetpotato.tracking.FoodList;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;

public class FoodContainerItem extends Item {
    private String displayName;
    private int nslots;
    
    public FoodContainerItem(int nslots, String displayName) {
        super(new Properties().stacksTo(1).setNoRepair());
        
        this.displayName = displayName;
        this.nslots = nslots;
    }
    
    @Override
    public boolean isEdible() {
        return true;
    }
    
    @Override
    public FoodProperties getFoodProperties() {
        return new FoodProperties.Builder().build();
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide && player.isCrouching()) {
            NetworkHooks.openScreen((ServerPlayer) player, new FoodContainerProvider(displayName), player.blockPosition());
        }
        
        if (!player.isCrouching()) {
            return processRightClick(world, player, hand);
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
    
    private InteractionResultHolder<ItemStack> processRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isInventoryEmpty(stack) || (ModList.get().isLoaded("origins")/* && Origins.hasRestrictedDiet(player)*/)) {
            return InteractionResultHolder.pass(stack);
        }
        
        if (player.canEat(false)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }
    
    private static boolean isInventoryEmpty(ItemStack container) {
        ItemStackHandler handler = getInventory(container);
        if (handler == null) {
            return true;
        }
        
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.isEdible()) {
                return false;
            }
        }
        return true;
    }
    
    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new FoodContainerCapabilityProvider(stack, nslots);
    }
    
    @Nullable
    public static ItemStackHandler getInventory(ItemStack bag) {
        if (bag.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent())
            return (ItemStackHandler) bag.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().get();
        return null;
    }
    
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (!(entity instanceof Player)) {
            return stack;
        }
        
        Player player = (Player) entity;
        ItemStackHandler handler = getInventory(stack);
        if (handler == null) {
            return stack;
        }
        
        int bestFoodSlot = getBestFoodSlot(handler, player);
        if (bestFoodSlot < 0) {
            return stack;
        }
        
        ItemStack bestFood = handler.getStackInSlot(bestFoodSlot);
        ItemStack foodCopy = bestFood.copy();
        if (bestFood.isEdible() && !bestFood.isEmpty()) {
            ItemStack result = bestFood.finishUsingItem(world, entity);
            // put bowls/bottles etc. into player inventory
            if (!result.isEdible()) {
                handler.setStackInSlot(bestFoodSlot, ItemStack.EMPTY);
                Player playerEntity = (Player) entity;
                
                if (!playerEntity.getInventory().add(result)) {
                    playerEntity.drop(result, false);
                }
            }
            
            if (!world.isClientSide) {
                // Fire an event instead of directly updating the food list, so that
                // SoL: Carrot Edition registers the eaten food too.
                ForgeEventFactory.onItemUseFinish(player, foodCopy, 0, result);
            }
        }
        
        return stack;
    }
    
    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }
    
    public static int getBestFoodSlot(ItemStackHandler handler, Player player) {
        FoodList foodList = FoodList.get(player);
        
        double maxDiversity = -Double.MAX_VALUE;
        int bestFoodSlot = -1;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack food = handler.getStackInSlot(i);
            
            if (!food.isEdible() || food.isEmpty())
                continue;
            double diversityChange = foodList.simulateFoodAdd(food.getItem());
            if (diversityChange > maxDiversity) {
                maxDiversity = diversityChange;
                bestFoodSlot = i;
            }
        }
        
        return bestFoodSlot;
    }
}
