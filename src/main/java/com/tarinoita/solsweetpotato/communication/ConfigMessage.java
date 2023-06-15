package com.tarinoita.solsweetpotato.communication;

import java.util.function.Supplier;

import com.tarinoita.solsweetpotato.ConfigHandler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class ConfigMessage {
    private CompoundTag configNBT;
    
    public ConfigMessage() {
        this.configNBT = ConfigHandler.serializeConfig();
    }
    
    public ConfigMessage(FriendlyByteBuf buffer) {
        this.configNBT = buffer.readNbt();
    }
    
    public void write(FriendlyByteBuf buffer) {
        buffer.writeNbt(configNBT);
    }
    
    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ConfigMessage.Handler.handle(this, context));
    }
    
    private static class Handler {
        static void handle(ConfigMessage message, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> {
                ConfigHandler.deserializeConfig(message.configNBT);
            });
            context.get().setPacketHandled(true);
        }
    }
}
