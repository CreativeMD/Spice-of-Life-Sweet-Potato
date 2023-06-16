package com.tarinoita.solsweetpotato.tracking;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

import com.tarinoita.solsweetpotato.SOLSweetPotato;
import com.tarinoita.solsweetpotato.SOLSweetPotatoConfig;
import com.tarinoita.solsweetpotato.api.FoodCapability;
import com.tarinoita.solsweetpotato.network.FoodListMessage;
import com.tarinoita.solsweetpotato.tracking.benefits.BenefitsHandler;
import com.tarinoita.solsweetpotato.tracking.benefits.EffectBenefitsCapability;

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

@Mod.EventBusSubscriber(modid = SOLSweetPotato.MODID)
public final class CapabilityHandler {
    private static final ResourceLocation FOOD = new ResourceLocation(SOLSweetPotato.MODID, "food");
    private static final ResourceLocation EFFECT_BENEFITS = new ResourceLocation(SOLSweetPotato.MODID, "effect_benefits");
    public static Capability<EffectBenefitsCapability> effectBenefitsCapability = CapabilityManager.get(new CapabilityToken<>() {});
    
    @Mod.EventBusSubscriber(modid = SOLSweetPotato.MODID, bus = MOD)
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
        if (event.isWasDeath() && SOLSweetPotatoConfig.shouldResetOnDeath())
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
        
        SOLSweetPotato.NETWORK.sendToClient(new FoodListMessage(FoodList.get(player)), (ServerPlayer) player);
    }
}
