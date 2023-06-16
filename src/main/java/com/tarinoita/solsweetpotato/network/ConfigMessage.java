package com.tarinoita.solsweetpotato.network;

import com.tarinoita.solsweetpotato.ConfigHandler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;

public class ConfigMessage extends CreativePacket {
    
    private CompoundTag nbt;
    
    public ConfigMessage() {}
    
    public ConfigMessage(CompoundTag nbt) {
        this.nbt = nbt;;
    }
    
    @Override
    public void executeClient(Player player) {
        ConfigHandler.deserializeConfig(nbt);
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
}
