package team.creative.solonion.api;

import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public interface FoodCapability extends ICapabilitySerializable<ListTag>, Iterable<ItemStack> {
    
    public void eat(ItemStack stack);
    
    public double simulateEat(ItemStack stack);
    
    public double foodDiversity();
    
    public void clearAll();
    
    public boolean hasEaten(ItemStack food);
    
    public int getLastEaten(ItemStack food);
    
    public void configChanged();
    
    public int trackCount();
    
}
