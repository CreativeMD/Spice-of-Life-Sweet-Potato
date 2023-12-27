package team.creative.solonion.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.EntityCapability;
import team.creative.solonion.common.SOLOnion;

public final class SOLOnionAPI {
    
    public static final ResourceLocation FOOD = new ResourceLocation(SOLOnion.MODID, "foodlist");
    public static final ResourceLocation BENEFIT = new ResourceLocation(SOLOnion.MODID, "benefit");
    public static final EntityCapability<FoodCapability, Void> FOOD_CAP = EntityCapability.createVoid(FOOD, FoodCapability.class);
    public static final EntityCapability<BenefitCapability, Void> BENEFIT_CAP = EntityCapability.createVoid(BENEFIT, BenefitCapability.class);
    
    public static FoodCapability getFoodCapability(Player player) {
        return player.getCapability(FOOD_CAP);
    }
    
    public static BenefitCapability getBenefitCapability(Player player) {
        return player.getCapability(BENEFIT_CAP);
    }
    
    public static boolean isPresent(Player player) {
        return player.getCapability(FOOD_CAP) != null && player.getCapability(BENEFIT_CAP) != null;
    }
    
    public static void syncFoodList(Player player) {
        SOLOnion.EVENT.syncFoodList(player);
    }
    
    private SOLOnionAPI() {}
    
}
