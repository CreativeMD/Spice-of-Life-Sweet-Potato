package team.creative.solonion.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

/** Provides a stable, (strongly) simplified view of the food list. */
public interface FoodCapability extends ICapabilitySerializable<CompoundTag> {
    /** @return whether or not the given food is being tracked. */
    boolean hasEaten(Item item);
    
    /** @return the food diversity score of the current food queue. */
    double foodDiversity();
}
