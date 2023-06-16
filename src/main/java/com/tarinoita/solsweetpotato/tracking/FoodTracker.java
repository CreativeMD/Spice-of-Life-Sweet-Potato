package com.tarinoita.solsweetpotato.tracking;

import com.tarinoita.solsweetpotato.SOLSweetPotato;
import com.tarinoita.solsweetpotato.item.foodcontainer.FoodContainerItem;
import com.tarinoita.solsweetpotato.tracking.benefits.BenefitsHandler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = SOLSweetPotato.MODID)
public final class FoodTracker {
    
    @SubscribeEvent
    public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        if (!BenefitsHandler.checkEvent(event)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        Item usedItem = event.getItem().getItem();
        if (!usedItem.isEdible() && usedItem != Items.CAKE)
            return;
        if (usedItem instanceof FoodContainerItem)
            return;
        
        updateFoodList(usedItem, player);
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCakeBlockEaten(PlayerInteractEvent.RightClickBlock event) {
        // Canceled means some other mod already resolved this event,
        // e.g. Farmer's Delight cut off a slice with a knife.
        if (event.isCanceled())
            return;
        
        BlockState state = event.getLevel().getBlockState(event.getPos());
        Block clickedBlock = state.getBlock();
        Player player = event.getEntity();
        
        Item eatenItem = Items.CAKE;
        // If Farmer's Delight is installed, replace "cake" with FD's "cake slice"
        if (ModList.get().isLoaded("farmersdelight")) {
            eatenItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("farmersdelight:cake_slice"));
        }
        ItemStack eatenItemStack = new ItemStack(eatenItem);
        
        if (clickedBlock == Blocks.CAKE && player.canEat(false) && event.getHand() == InteractionHand.MAIN_HAND && !event.getLevel().isClientSide) {
            // Fire an event instead of directly updating the food list, so that
            // SoL: Carrot Edition registers the eaten food too.
            ForgeEventFactory.onItemUseFinish(player, eatenItemStack, 0, ItemStack.EMPTY);
        }
    }
    
    public static void updateFoodList(Item food, Player player) {
        FoodList foodList = FoodList.get(player);
        foodList.addFood(food);
        
        double diversity = foodList.foodDiversity();
        BenefitsHandler.updateBenefits(player, diversity);
        
        CapabilityHandler.syncFoodList(player);
    }
    
    private FoodTracker() {}
}
