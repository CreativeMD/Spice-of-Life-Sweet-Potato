package team.creative.solonion.common.network;

import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.solonion.api.FoodPlayerData;
import team.creative.solonion.api.SOLOnionAPI;

public class FoodListMessage extends CreativePacket {
    
    public ListTag list;
    
    public FoodListMessage() {}
    
    public FoodListMessage(FoodPlayerData foodList) {
        this.list = foodList.serializeNBT();
    }
    
    @Override
    public void executeClient(Player player) {
        SOLOnionAPI.getFoodCapability(player).deserializeNBT(list);
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
}
