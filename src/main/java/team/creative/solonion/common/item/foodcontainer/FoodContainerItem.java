package team.creative.solonion.common.item.foodcontainer;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import team.creative.creativecore.common.util.type.list.TupleList;
import team.creative.solonion.api.FoodCapability;
import team.creative.solonion.api.OnionFoodContainer;
import team.creative.solonion.api.SOLOnionAPI;
import team.creative.solonion.common.SOLOnion;
import team.creative.solonion.common.mod.OriginsManager;

public class FoodContainerItem extends Item implements OnionFoodContainer {
    
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
    public InteractionResult useOn(UseOnContext context) {
        var blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        if (blockEntity == null)
            return super.useOn(context);
        
        var handler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        if (handler == null)
            return super.useOn(context);
        
        ItemStackHandler inv = getInventory(context.getItemInHand());
        if (inv == null)
            return super.useOn(context);
        TupleList<Double, Integer> bestStacks = new TupleList<Double, Integer>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getFoodProperties(context.getPlayer()) != null && OriginsManager.isEdible(context.getPlayer(), stack)) {
                for (int j = 0; j < inv.getSlots(); j++) { // Fill up the slots which are already taken
                    var toBeStacked = inv.getStackInSlot(j);
                    if (ItemStack.isSameItem(stack, toBeStacked) && ItemStack.isSameItemSameTags(stack, toBeStacked)) {
                        int maxStackSize = Math.min(stack.getMaxStackSize(), inv.getSlotLimit(j));
                        if (!toBeStacked.isEmpty() && toBeStacked.getCount() < maxStackSize)
                            toBeStacked.grow(handler.extractItem(i, maxStackSize - toBeStacked.getCount(), false).getCount());
                        stack = handler.getStackInSlot(i);
                        if (stack.isEmpty())
                            break;
                    }
                }
                bestStacks.add(SOLOnion.CONFIG.getDiversity(context.getPlayer(), stack), i);
            }
        }
        
        bestStacks.sort((x, y) -> y.key.compareTo(x.key));
        
        for (int slot : bestStacks.values()) {
            var stack = handler.extractItem(slot, handler.getStackInSlot(slot).getCount(), false);
            var result = ItemHandlerHelper.insertItem(inv, stack, false);
            if (!result.isEmpty()) {
                handler.insertItem(slot, result, false);
                break;
            }
        }
        
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide && player.isCrouching())
            NetworkHooks.openScreen((ServerPlayer) player, new FoodContainerProvider(displayName), player.blockPosition());
        
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
            if (!stack.isEmpty() && stack.isEdible() && OriginsManager.isEdible(player, stack))
                return false;
        }
        return true;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item." + SOLOnion.MODID + ".container.open", Component.keybind("key.sneak"), Component.keybind("key.use")));
        super.appendHoverText(stack, level, tooltip, flag);
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
        if (bestFood.isEdible() && !bestFood.isEmpty() && OriginsManager.isEdible(player, foodCopy)) {
            ItemStack result = bestFood.finishUsingItem(world, entity);
            // put bowls/bottles etc. into player inventory
            if (!result.isEdible()) {
                handler.setStackInSlot(bestFoodSlot, ItemStack.EMPTY);
                Player playerEntity = (Player) entity;
                
                if (!playerEntity.getInventory().add(result))
                    playerEntity.drop(result, false);
            }
            
            if (!world.isClientSide)
                ForgeEventFactory.onItemUseFinish(player, foodCopy, 0, result);
        }
        
        return stack;
    }
    
    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }
    
    public static int getBestFoodSlot(ItemStackHandler handler, Player player) {
        FoodCapability foodList = SOLOnionAPI.getFoodCapability(player);
        
        double maxDiversity = -Double.MAX_VALUE;
        int bestFoodSlot = -1;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack food = handler.getStackInSlot(i);
            
            if (!food.isEdible() || food.isEmpty() || !OriginsManager.isEdible(player, food))
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
