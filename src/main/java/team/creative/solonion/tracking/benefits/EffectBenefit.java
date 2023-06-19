package team.creative.solonion.tracking.benefits;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

/** Each instance represents a specific potion effect. Handles logic for application to player. */
public final class EffectBenefit extends Benefit {
    private MobEffect effect;
    private final int DEFAULT_DURATION = 300;
    private final int REAPPLY_DURATION = 200;
    
    public EffectBenefit(String name, double value, double threshold, boolean detriment) {
        super("effect", name, value, threshold, detriment);
    }
    
    @Override
    public void applyTo(Player player) {
        if (!checkUsage() || player.level().isClientSide)
            return;
        
        EffectBenefitsCapability effectBenefits = EffectBenefitsCapability.get(player);
        effectBenefits.addEffectBenefitUnique(this);
    }
    
    public void onTick(Player player) {
        if (!checkUsage() || player.level().isClientSide)
            return;
        
        // Only refresh this effect when less than REAPPLY_DURATION ticks remaining
        MobEffectInstance currentEffect = player.getEffect(effect);
        if (currentEffect != null && currentEffect.getAmplifier() >= (int) value && currentEffect.getDuration() > REAPPLY_DURATION) {
            return;
        }
        
        player.addEffect(new MobEffectInstance(effect, DEFAULT_DURATION, (int) value, false, false));
    }
    
    @Override
    public void removeFrom(Player player) {
        if (!checkUsage() || player.level().isClientSide)
            return;
        
        EffectBenefitsCapability effectBenefits = EffectBenefitsCapability.get(player);
        effectBenefits.removeEffectBenefit(this);
    }
    
    private boolean checkUsage() {
        if (invalid) {
            return false;
        }
        
        if (effect == null) {
            createEffect();
            return !invalid;
        }
        
        return true;
    }
    
    private void createEffect() {
        IForgeRegistry<MobEffect> registry = RegistryManager.ACTIVE.getRegistry(Registries.MOB_EFFECT);
        try {
            effect = registry.getValue(new ResourceLocation(name));
        } catch (ResourceLocationException e) {
            markInvalid();
            return;
        }
        
        if (effect == null) {
            markInvalid();
            return;
        }
        
        if (value < 0 || value > 255) {
            markInvalid();
        }
    }
    
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        
        StringTag type = StringTag.valueOf(benefitType);
        StringTag n = StringTag.valueOf(name);
        DoubleTag v = DoubleTag.valueOf(value);
        DoubleTag thresh = DoubleTag.valueOf(threshold);
        ByteTag detr = ByteTag.valueOf((byte) (detriment ? 1 : 0));
        
        tag.put("type", type);
        tag.put("name", n);
        tag.put("value", v);
        tag.put("threshold", thresh);
        tag.put("detriment", detr);
        
        return tag;
    }
    
    public static EffectBenefit fromNBT(CompoundTag tag) {
        String type = tag.getString("type");
        if (!type.equals("effect")) {
            throw new RuntimeException("Mismatching benefit type");
        }
        String n = tag.getString("name");
        double v = tag.getDouble("value");
        double thresh = tag.getDouble("threshold");
        boolean detr = tag.getByte("detriment") == 1;
        
        return new EffectBenefit(n, v, thresh, detr);
    }
}
