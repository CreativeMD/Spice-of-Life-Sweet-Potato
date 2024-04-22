package team.creative.solonion.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import team.creative.solonion.common.benefit.BenefitStack;

public interface BenefitCapability extends INBTSerializable<CompoundTag> {
    
    public void updateStack(Player player, BenefitStack benefits);
    
    public void onEffectRemove(MobEffectEvent.Remove event);
    
}
