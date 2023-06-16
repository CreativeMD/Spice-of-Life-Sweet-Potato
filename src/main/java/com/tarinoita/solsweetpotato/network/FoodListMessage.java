package com.tarinoita.solsweetpotato.network;

import com.tarinoita.solsweetpotato.tracking.FoodList;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;

public class FoodListMessage extends CreativePacket {
    
    private CompoundTag nbt;
    
    public FoodListMessage() {}
    
    public FoodListMessage(FoodList foodList) {
        this.nbt = foodList.serializeNBT();
    }
    
    @Override
    public void executeClient(Player player) {
        FoodList.get(player).deserializeNBT(nbt);
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
}
