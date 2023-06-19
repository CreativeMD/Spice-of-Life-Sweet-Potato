package team.creative.solonion.api;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import team.creative.solonion.tracking.CapabilityHandler;
import team.creative.solonion.tracking.FoodList;

/** Provides a stable API for interfacing with Spice of Life: Carrot Edition. */
public final class SOLOnionAPI {
    public static final Capability<FoodCapability> foodCapability = CapabilityManager.get(new CapabilityToken<>() {});;
    
    private SOLOnionAPI() {}
    
    /** Retrieves the {@link team.creative.solonion.api.FoodCapability} for the given player. */
    public static FoodCapability getFoodCapability(Player player) {
        return FoodList.get(player);
    }
    
    /** Synchronizes the food list for the given player to the client, updating their max health in the process. */
    public static void syncFoodList(Player player) {
        CapabilityHandler.syncFoodList(player);
    }
}
