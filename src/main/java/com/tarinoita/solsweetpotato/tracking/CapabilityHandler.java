package com.tarinoita.solsweetpotato.tracking;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

import com.tarinoita.solsweetpotato.SOLSweetPotato;
import com.tarinoita.solsweetpotato.SOLSweetPotatoConfig;
import com.tarinoita.solsweetpotato.api.FoodCapability;
import com.tarinoita.solsweetpotato.communication.FoodListMessage;
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
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = SOLSweetPotato.MOD_ID)
public final class CapabilityHandler {
    private static final ResourceLocation FOOD = SOLSweetPotato.resourceLocation("food");
    private static final ResourceLocation EFFECT_BENEFITS = SOLSweetPotato.resourceLocation("effect_benefits");
    public static Capability<EffectBenefitsCapability> effectBenefitsCapability = CapabilityManager.get(new CapabilityToken<>() {});
    
    @Mod.EventBusSubscriber(modid = SOLSweetPotato.MOD_ID, bus = MOD)
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
        originalPlayer.revive(); // so we can access the capabilities; entity will get removed either way
        FoodList original = FoodList.get(originalPlayer);
        FoodList newInstance = FoodList.get(event.getEntity());
        newInstance.deserializeNBT(original.serializeNBT());
        // can't sync yet; client hasn't attached capabilities yet
        
        BenefitsHandler.updatePlayer(event.getEntity());
    }
    
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncFoodList(event.getEntity());
    }
    
    public static void syncFoodList(Player player) {
        if (player.level().isClientSide)
            return;
        
        ServerPlayer target = (ServerPlayer) player;
        SOLSweetPotato.channel.sendTo(new FoodListMessage(FoodList.get(player)), target.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
