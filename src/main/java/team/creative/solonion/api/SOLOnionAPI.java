package team.creative.solonion.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import team.creative.solonion.SOLOnion;

public final class SOLOnionAPI {
    
    public static final Capability<FoodCapability> FOOD_CAP = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<BenefitCapability> BENEFIT_CAP = CapabilityManager.get(new CapabilityToken<>() {});
    
    public static final ResourceLocation FOOD = new ResourceLocation(SOLOnion.MODID, "food");
    public static final ResourceLocation BENEFIT = new ResourceLocation(SOLOnion.MODID, "benefit");
    
    public static FoodCapability getFoodCapability(Player player) {
        return player.getCapability(FOOD_CAP).orElseThrow(() -> new RuntimeException("Player must have food capability attached, but none was found."));
    }
    
    public static BenefitCapability getBenefitCapability(Player player) {
        return player.getCapability(BENEFIT_CAP).orElseThrow(() -> new RuntimeException("Player must have benefit capability attached, but none was found."));
    }
    
    public static void syncFoodList(Player player) {
        SOLOnion.EVENT.syncFoodList(player);
    }
    
    private SOLOnionAPI() {}
    
}
