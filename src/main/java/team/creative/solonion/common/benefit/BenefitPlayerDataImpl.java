package team.creative.solonion.common.benefit;

import java.util.HashMap;
import java.util.Map.Entry;

import org.jetbrains.annotations.UnknownNullability;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import team.creative.solonion.api.BenefitPlayerData;

public class BenefitPlayerDataImpl implements BenefitPlayerData {
    
    private HashMap<BenefitType, Object> applied = new HashMap<>();
    
    @Override
    public void updateStack(Player player, BenefitStack benefits) {
        for (BenefitType type : BenefitType.types()) {
            var app = applied.get(type);
            var stack = benefits.get(type);
            if (app == null)
                if (stack == null)
                    continue;
                else
                    applied.put(type, app = type.createApplied());
            if (type.apply(player, app, stack))
                applied.remove(type);
        }
    }
    
    @Override
    public @UnknownNullability CompoundTag serializeNBT(Provider provider) {
        CompoundTag nbt = new CompoundTag();
        for (Entry<BenefitType, Object> entry : applied.entrySet()) {
            var tag = entry.getKey().saveApplied(entry.getValue());
            if (tag != null)
                nbt.put(entry.getKey().getId(), tag);
        }
        
        return nbt;
    }
    
    @Override
    public void deserializeNBT(Provider provider, CompoundTag nbt) {
        for (Entry<BenefitType, Object> entry : applied.entrySet())
            entry.getKey().clearApplied(entry.getValue());
        
        if (nbt == null)
            return;
        
        for (BenefitType type : BenefitType.types()) {
            if (nbt.contains(type.getId())) {
                var app = applied.get(type);
                if (app == null)
                    applied.put(type, app = type.createApplied());
                type.loadApplied(app, nbt.get(type.getId()));
            } else
                applied.remove(type);
        }
    }
    
    @Override
    public <T> T getApplied(BenefitType<?, ?, T> type) {
        return (T) applied.get(type);
    }
    
}
