package team.creative.solonion.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import team.creative.solonion.common.SOLOnion;
import team.creative.solonion.common.benefit.BenefitCapabilityImpl;
import team.creative.solonion.common.food.FoodCapabilityImpl;

public final class SOLOnionAPI {
    
    public static final Capability<FoodCapability> FOOD_CAP = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<BenefitCapability> BENEFIT_CAP = CapabilityManager.get(new CapabilityToken<>() {});
    
    public static final ResourceLocation FOOD = new ResourceLocation(SOLOnion.MODID, "foodlist");
    public static final ResourceLocation BENEFIT = new ResourceLocation(SOLOnion.MODID, "benefit");
    
    public static FoodCapability getFoodCapability(Player player) {
        return player.getCapability(FOOD_CAP).orElseGet(() -> new FoodCapabilityImpl());
    }
    
    public static BenefitCapability getBenefitCapability(Player player) {
        return player.getCapability(BENEFIT_CAP).orElseGet(() -> new BenefitCapabilityImpl());
    }
    
    public static boolean isPresent(Player player) {
        return player.getCapability(FOOD_CAP).isPresent() && player.getCapability(BENEFIT_CAP).isPresent();
    }
    
    public static void syncFoodList(Player player) {
        SOLOnion.EVENT.syncFoodList(player);
    }
    
    private SOLOnionAPI() {}
    
}
