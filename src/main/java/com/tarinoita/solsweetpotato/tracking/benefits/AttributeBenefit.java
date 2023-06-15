package com.tarinoita.solsweetpotato.tracking.benefits;

import java.util.Objects;
import java.util.UUID;

import com.tarinoita.solsweetpotato.ConfigHandler;
import com.tarinoita.solsweetpotato.SOLSweetPotato;

import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

/** Each instance represents a specific attribute and modifier. Handles logic for application to player. */
public final class AttributeBenefit extends Benefit {
    private final AttributeModifier modifier;
    private final UUID id;
    private Attribute attribute;
    private final boolean isMaxHealth;
    
    public AttributeBenefit(String name, double value, double threshold, boolean detriment) {
        super("attribute", name, value, threshold, detriment);
        
        id = UUID.randomUUID();
        
        modifier = new AttributeModifier(id, name + "_modifier_" + value, value, AttributeModifier.Operation.ADDITION);
        
        isMaxHealth = name.equals("generic.max_health");
    }
    
    public AttributeBenefit(String name, double value, double threshold, String uuid, boolean detriment) {
        super("attribute", name, value, threshold, detriment);
        
        id = UUID.fromString(uuid);
        
        modifier = new AttributeModifier(id, name + "_modifier_" + value, value, AttributeModifier.Operation.ADDITION);
        
        isMaxHealth = name.equals("generic.max_health");
    }
    
    @Override
    public void applyTo(Player player) {
        if (!checkUsage() || player.level().isClientSide)
            return;
        
        float oldMax = player.getMaxHealth();
        
        AttributeInstance attr;
        try {
            attr = Objects.requireNonNull(player.getAttribute(attribute));
        } catch (NullPointerException e) {
            SOLSweetPotato.LOGGER.warn("ERROR: player does not have attribute: " + attribute.getDescriptionId());
            return;
        }
        
        attr.removeModifier(modifier);
        attr.addPermanentModifier(modifier);
        
        if (isMaxHealth && !ConfigHandler.isFirstAid) {
            // increase current health proportionally
            float newHealth = player.getHealth() * player.getMaxHealth() / oldMax;
            player.setHealth(newHealth);
        }
    }
    
    @Override
    public void removeFrom(Player player) {
        if (!checkUsage() || player.level().isClientSide)
            return;
        
        AttributeInstance attr;
        try {
            attr = Objects.requireNonNull(player.getAttribute(attribute));
        } catch (NullPointerException e) {
            SOLSweetPotato.LOGGER.warn("ERROR: player does not have attribute: " + attribute.getDescriptionId());
            return;
        }
        attr.removeModifier(modifier);
    }
    
    private boolean checkUsage() {
        if (invalid) {
            return false;
        }
        
        if (attribute == null) {
            createAttribute();
            return !invalid;
        }
        
        return true;
    }
    
    private void createAttribute() {
        try {
            attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(name));
        } catch (ResourceLocationException e) {
            markInvalid();
            return;
        }
        
        if (attribute == null || value == 0) {
            markInvalid();
        }
    }
    
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        
        StringTag type = StringTag.valueOf(benefitType);
        StringTag n = StringTag.valueOf(name);
        DoubleTag v = DoubleTag.valueOf(value);
        StringTag uuid = StringTag.valueOf(id.toString());
        DoubleTag thresh = DoubleTag.valueOf(threshold);
        ByteTag detr = ByteTag.valueOf((byte) (detriment ? 1 : 0));
        
        tag.put("type", type);
        tag.put("name", n);
        tag.put("value", v);
        tag.put("id", uuid);
        tag.put("threshold", thresh);
        tag.put("detriment", detr);
        
        return tag;
    }
    
    public static AttributeBenefit fromNBT(CompoundTag tag) {
        String type = tag.getString("type");
        if (!type.equals("attribute")) {
            throw new RuntimeException("Mismatching benefit type");
        }
        String n = tag.getString("name");
        double v = tag.getDouble("value");
        String uuid = tag.getString("id");
        double thresh = tag.getDouble("threshold");
        boolean detr = tag.getByte("detriment") == 1;
        
        return new AttributeBenefit(n, v, thresh, uuid, detr);
    }
}
