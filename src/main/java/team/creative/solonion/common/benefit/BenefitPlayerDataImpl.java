package team.creative.solonion.common.benefit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.jetbrains.annotations.UnknownNullability;

import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.Provider;
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
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import team.creative.solonion.api.BenefitPlayerData;
import team.creative.solonion.common.mod.FirstAidManager;

public class BenefitPlayerDataImpl implements BenefitPlayerData {
    
    private HashMap<Holder<Attribute>, AttributeModifier> appliedAttributes;
    private List<Holder<MobEffect>> appliedEffects;
    
    @Override
    public void onEffectRemove(MobEffectEvent.Remove event) {
        if (appliedEffects != null && appliedEffects.contains(event.getEffect()))
            event.setCanceled(true);
    }
    
    @Override
    public void updateStack(Player player, BenefitStack benefits) {
        if (appliedAttributes != null && !appliedAttributes.isEmpty()) {
            for (Entry<Holder<Attribute>, AttributeModifier> entry : appliedAttributes.entrySet())
                player.getAttribute(entry.getKey()).removeModifier(entry.getValue().id());
            appliedAttributes.clear();
        }
        
        if (appliedEffects != null && !appliedEffects.isEmpty()) {
            for (Holder<MobEffect> effect : appliedEffects)
                player.removeEffect(effect);
            appliedEffects.clear();
        }
        
        if (benefits.isEmpty())
            return;
        
        for (it.unimi.dsi.fastutil.objects.Object2DoubleMap.Entry<Holder<Attribute>> entry : benefits.attributes()) {
            var modi = new AttributeModifier(ResourceLocation.parse(entry.getKey().value().getDescriptionId()), entry.getDoubleValue(), Operation.ADD_VALUE);
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
        
        for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Holder<MobEffect>> entry : benefits.effects()) {
            var in = new MobEffectInstance(entry.getKey(), -1, entry.getIntValue(), false, false);
            if (player.addEffect(in)) {
                if (appliedEffects == null)
                    appliedEffects = new ArrayList<>();
                appliedEffects.add(entry.getKey());
            }
        }
    }
    
    @Override
    public @UnknownNullability CompoundTag serializeNBT(Provider provider) {
        CompoundTag nbt = new CompoundTag();
        if (appliedAttributes != null && !appliedAttributes.isEmpty()) {
            ListTag list = new ListTag();
            for (Entry<Holder<Attribute>, AttributeModifier> entry : appliedAttributes.entrySet()) {
                CompoundTag tag = new CompoundTag();
                tag.putString("att", entry.getKey().unwrapKey().get().location().toString());
                tag.put("mod", entry.getValue().save());
                list.add(tag);
            }
            nbt.put("att", list);
        }
        
        if (appliedEffects != null && !appliedEffects.isEmpty()) {
            ListTag list = new ListTag();
            for (Holder<MobEffect> effect : appliedEffects)
                list.add(StringTag.valueOf(effect.unwrapKey().get().location().toString()));
            nbt.put("eff", list);
        }
        return nbt;
    }
    
    @Override
    public void deserializeNBT(Provider provider, CompoundTag nbt) {
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
                Reference<Attribute> att = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse(tag.getString("att"))).get();
                if (att != null)
                    appliedAttributes.put(att, AttributeModifier.load(tag.getCompound("mod")));
            }
        }
        
        list = nbt.getList("eff", Tag.TAG_STRING);
        if (!list.isEmpty()) {
            appliedEffects = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                Reference<MobEffect> mob = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(list.getString(i))).get();
                if (mob != null)
                    appliedEffects.add(mob);
            }
        }
    }
    
}
