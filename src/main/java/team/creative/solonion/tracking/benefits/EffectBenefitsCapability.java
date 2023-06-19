package team.creative.solonion.tracking.benefits;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import team.creative.solonion.tracking.CapabilityHandler;

public class EffectBenefitsCapability implements ICapabilitySerializable<CompoundTag>, Iterable<EffectBenefit> {
    private final Set<EffectBenefit> effectBenefits = new HashSet<>();
    
    private final LazyOptional<EffectBenefitsCapability> capabilityOptional = LazyOptional.of(() -> this);
    private static final String NBT_KEY_EFFECT_BENEFITS = "effect_benefits";
    
    public static EffectBenefitsCapability get(Player player) {
        return player.getCapability(CapabilityHandler.effectBenefitsCapability).orElseThrow(EffectBenefitsCapability.EffectsBenefitsNotFoundException::new);
    }
    
    public void addEffectBenefit(EffectBenefit b) {
        effectBenefits.add(b);
    }
    
    public void addEffectBenefitUnique(EffectBenefit b) {
        effectBenefits.removeIf(other -> other.getName().equals(b.getName()));
        addEffectBenefit(b);
    }
    
    public void removeEffectBenefit(EffectBenefit b) {
        effectBenefits.remove(b);
    }
    
    public void clear() {
        effectBenefits.clear();
    }
    
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        return capability == CapabilityHandler.effectBenefitsCapability ? capabilityOptional.cast() : LazyOptional.empty();
    }
    
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        
        ListTag list = new ListTag();
        effectBenefits.stream().map(EffectBenefit::serializeNBT).forEach(list::add);
        tag.put(NBT_KEY_EFFECT_BENEFITS, list);
        
        return tag;
    }
    
    @Override
    public void deserializeNBT(CompoundTag tag) {
        ListTag list = tag.getList(NBT_KEY_EFFECT_BENEFITS, Tag.TAG_COMPOUND);
        
        effectBenefits.clear();
        list.stream().map(nbt -> (CompoundTag) nbt).map(EffectBenefit::fromNBT).forEach(effectBenefits::add);
    }
    
    @Nonnull
    @Override
    public Iterator<EffectBenefit> iterator() {
        return effectBenefits.iterator();
    }
    
    public static class EffectsBenefitsNotFoundException extends RuntimeException {
        public EffectsBenefitsNotFoundException() {
            super("Player must have effect benefits capability attached, but none was found.");
        }
    }
}
