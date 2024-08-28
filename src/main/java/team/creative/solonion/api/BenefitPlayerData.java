package team.creative.solonion.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import team.creative.solonion.common.benefit.BenefitStack;
import team.creative.solonion.common.benefit.BenefitType;

public interface BenefitPlayerData extends INBTSerializable<CompoundTag> {
    
    public void updateStack(Player player, BenefitStack benefits);
    
    public <T> T getApplied(BenefitType<?, ?, T> type);
    
}
