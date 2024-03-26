package team.creative.solonion.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface OnionFoodContainer {
    
    public ItemStack getActualFood(Player player, ItemStack stack);
    
}
