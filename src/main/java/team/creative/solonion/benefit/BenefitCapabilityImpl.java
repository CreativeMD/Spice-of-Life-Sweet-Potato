package team.creative.solonion.benefit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import team.creative.solonion.api.BenefitCapability;
import team.creative.solonion.api.SOLOnionAPI;
import team.creative.solonion.mod.FirstAidManager;

public class BenefitCapabilityImpl implements BenefitCapability {
    
    private final LazyOptional<BenefitCapabilityImpl> capabilityOptional = LazyOptional.of(() -> this);
    
    private HashMap<Attribute, AttributeModifier> appliedAttributes;
    private List<MobEffect> appliedEffects;
    
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        return capability == SOLOnionAPI.BENEFIT_CAP ? capabilityOptional.cast() : LazyOptional.empty();
    }
    
    @Override
    public void updateStack(Player player, BenefitStack benefits) {
        if (appliedAttributes != null && !appliedAttributes.isEmpty()) {
            for (Entry<Attribute, AttributeModifier> entry : appliedAttributes.entrySet())
                player.getAttribute(entry.getKey()).removeModifier(entry.getValue());
            appliedAttributes.clear();
        }
        
        if (appliedEffects != null && !appliedEffects.isEmpty()) {
            for (MobEffect effect : appliedEffects)
                player.removeEffect(effect);
            appliedEffects.clear();
        }
        
        if (benefits.isEmpty())
            return;
        
        for (Object2DoubleMap.Entry<Attribute> entry : benefits.attributes()) {
            var modi = new AttributeModifier(entry.getKey().getDescriptionId(), entry.getDoubleValue(), Operation.ADDITION);
            var att = player.getAttribute(entry.getKey());
            if (att != null) {
                float oldMax = player.getMaxHealth();
                att.addPermanentModifier(modi);
                if (appliedAttributes == null)
                    appliedAttributes = new HashMap<>();
                appliedAttributes.put(entry.getKey(), modi);
                if (entry.getKey() == Attributes.MAX_HEALTH && !FirstAidManager.INSTALLED) {
                    // increase current health proportionally
                    float newHealth = player.getHealth() * player.getMaxHealth() / oldMax;
                    player.setHealth(newHealth);
                }
            }
        }
        
        for (Object2IntMap.Entry<MobEffect> entry : benefits.effects()) {
            var in = new MobEffectInstance(entry.getKey(), -1, entry.getIntValue(), false, false);
            if (player.addEffect(in)) {
                if (appliedEffects == null)
                    appliedEffects = new ArrayList<>();
                appliedEffects.add(entry.getKey());
            }
        }
    }
    
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (appliedAttributes != null && !appliedAttributes.isEmpty()) {
            ListTag list = new ListTag();
            for (Entry<Attribute, AttributeModifier> entry : appliedAttributes.entrySet()) {
                CompoundTag tag = new CompoundTag();
                tag.putString("att", BuiltInRegistries.ATTRIBUTE.getKey(entry.getKey()).toString());
                tag.put("mod", entry.getValue().save());
                list.add(tag);
            }
            nbt.put("att", list);
        }
        
        if (appliedEffects != null && !appliedEffects.isEmpty()) {
            ListTag list = new ListTag();
            for (MobEffect effect : appliedEffects)
                list.add(StringTag.valueOf(BuiltInRegistries.MOB_EFFECT.getKey(effect).toString()));
            nbt.put("eff", list);
        }
        return nbt;
    }
    
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (appliedAttributes != null)
            appliedAttributes.clear();
        if (appliedEffects != null)
            appliedEffects.clear();
        
        if (nbt == null)
            return;
        
        ListTag list = nbt.getList("att", Tag.TAG_COMPOUND);
        if (!list.isEmpty()) {
            appliedAttributes = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                Attribute att = BuiltInRegistries.ATTRIBUTE.get(new ResourceLocation(tag.getString("att")));
                if (att != null)
                    appliedAttributes.put(att, AttributeModifier.load(tag.getCompound("mod")));
            }
        }
        
        list = nbt.getList("eff", Tag.TAG_STRING);
        if (!list.isEmpty()) {
            appliedEffects = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                MobEffect mob = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(list.getString(i)));
                if (mob != null)
                    appliedEffects.add(mob);
            }
        }
    }
    
}
