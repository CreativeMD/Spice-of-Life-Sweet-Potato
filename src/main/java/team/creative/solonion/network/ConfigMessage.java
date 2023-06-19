package team.creative.solonion.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.solonion.ConfigHandler;

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
