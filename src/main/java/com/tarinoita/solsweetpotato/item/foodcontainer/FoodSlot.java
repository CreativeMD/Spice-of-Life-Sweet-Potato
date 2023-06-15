package com.tarinoita.solsweetpotato.item.foodcontainer;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class FoodSlot extends SlotItemHandler {
    public FoodSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }
    
    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        if (!canHold(stack)) {
            return false;
        }
        return super.mayPlace(stack);
    }
    
    public static boolean canHold(@Nonnull ItemStack stack) {
        return stack.isEdible();
    }
}
