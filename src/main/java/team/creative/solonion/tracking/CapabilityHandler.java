package team.creative.solonion.tracking;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import team.creative.solonion.SOLOnion;
import team.creative.solonion.SOLOnionConfig;
import team.creative.solonion.api.FoodCapability;
import team.creative.solonion.network.FoodListMessage;
import team.creative.solonion.tracking.benefits.BenefitsHandler;
import team.creative.solonion.tracking.benefits.EffectBenefitsCapability;

@Mod.EventBusSubscriber(modid = SOLOnion.MODID)
public final class CapabilityHandler {
    private static final ResourceLocation FOOD = new ResourceLocation(SOLOnion.MODID, "food");
    private static final ResourceLocation EFFECT_BENEFITS = new ResourceLocation(SOLOnion.MODID, "effect_benefits");
    public static Capability<EffectBenefitsCapability> effectBenefitsCapability = CapabilityManager.get(new CapabilityToken<>() {});
    
    @Mod.EventBusSubscriber(modid = SOLOnion.MODID, bus = MOD)
    private static final class RegisterCapabilitiesSubscriber {
        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.register(FoodCapability.class);
            event.register(EffectBenefitsCapability.class);
        }
    }
    
    @SubscribeEvent
    public static void attachPlayerCapability(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player))
            return;
        
        event.addCapability(FOOD, new FoodList());
        event.addCapability(EFFECT_BENEFITS, new EffectBenefitsCapability());
    }
    
    @SubscribeEvent
    public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncFoodList(event.getEntity());
    }
    
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && SOLOnionConfig.shouldResetOnDeath())
            return;
        
        Player originalPlayer = event.getOriginal();
        originalPlayer.reviveCaps(); // so we can access the capabilities; entity will get removed either way
        FoodList original = FoodList.get(originalPlayer);
        FoodList newInstance = FoodList.get(event.getEntity());
        newInstance.deserializeNBT(original.serializeNBT());
        // can't sync yet; client hasn't attached capabilities yet
        
        BenefitsHandler.updatePlayer(event.getEntity());
        originalPlayer.invalidateCaps();
    }
    
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncFoodList(event.getEntity());
    }
    
    public static void syncFoodList(Player player) {
        if (player.level().isClientSide)
            return;
        
        SOLOnion.NETWORK.sendToClient(new FoodListMessage(FoodList.get(player)), (ServerPlayer) player);
    }
}
