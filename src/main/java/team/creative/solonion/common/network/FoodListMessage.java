package team.creative.solonion.common.network;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.solonion.api.FoodPlayerData;
import team.creative.solonion.api.SOLOnionAPI;

public class FoodListMessage extends CreativePacket {
    
    public ListTag list;
    
    public FoodListMessage() {}
    
    public FoodListMessage(Provider provider, FoodPlayerData foodList) {
        this.list = foodList.serializeNBT(provider);
    }
    
    @Override
    public void executeClient(Player player) {
        SOLOnionAPI.getFoodCapability(player).deserializeNBT(player.registryAccess(), list);
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
}
