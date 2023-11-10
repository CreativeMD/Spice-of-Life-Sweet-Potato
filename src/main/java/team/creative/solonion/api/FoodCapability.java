package team.creative.solonion.api;

import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public interface FoodCapability extends ICapabilitySerializable<ListTag>, Iterable<ItemStack> {
    
    public void eat(LivingEntity entity, ItemStack stack);
    
    public double simulateEat(LivingEntity entity, ItemStack stack);
    
    public double foodDiversity(LivingEntity entity);
    
    public void clearAll();
    
    public boolean hasEaten(LivingEntity entity, ItemStack food);
    
    public int getLastEaten(LivingEntity entity, ItemStack food);
    
    public void configChanged();
    
    public int trackCount();
    
}
