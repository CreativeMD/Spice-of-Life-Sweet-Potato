package team.creative.solonion.common.benefit;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.UnknownNullability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import team.creative.solonion.api.BenefitCapability;
import team.creative.solonion.api.SOLOnionAPI;

public class BenefitPlayerDataImpl implements BenefitCapability {
    
    private final LazyOptional<BenefitCapability> capabilityOptional = LazyOptional.of(() -> this);
    
    private HashMap<BenefitType, Object> applied = new HashMap<>();
    
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        return capability == SOLOnionAPI.BENEFIT_CAP ? capabilityOptional.cast() : LazyOptional.empty();
    }
    
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
    public @UnknownNullability CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        for (Entry<BenefitType, Object> entry : applied.entrySet()) {
            var tag = entry.getKey().saveApplied(entry.getValue());
            if (tag != null)
                nbt.put(entry.getKey().getId(), tag);
        }
        
        return nbt;
    }
    
    @Override
    public void deserializeNBT(CompoundTag nbt) {
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
