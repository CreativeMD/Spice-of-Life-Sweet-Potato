package team.creative.solonion.tracking.benefits;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import team.creative.solonion.SOLOnion;

public abstract class Benefit {
    protected final String benefitType;
    protected final String name;
    protected final double value;
    protected boolean invalid = false;
    protected boolean detriment = false;
    protected final double threshold;
    
    public Benefit(String benefitType, String name, double value, double threshold, boolean detriment) {
        this.benefitType = benefitType;
        this.name = name;
        this.value = value;
        this.threshold = threshold;
        this.detriment = detriment;
    }
    
    public abstract void applyTo(Player player);
    
    public abstract void removeFrom(Player player);
    
    public boolean isInvalid() {
        return invalid;
    }
    
    public boolean isDetriment() {
        return detriment;
    }
    
    protected void markInvalid() {
        SOLOnion.LOGGER.warn("Invalid attribute specified in config: " + name);
        invalid = true;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return benefitType;
    }
    
    public double getValue() {
        return value;
    }
    
    public abstract CompoundTag serializeNBT();
    
    @Override
    public String toString() {
        return "[" + benefitType + ", " + name + ", " + value + "]";
    }
    
    @Override
    public int hashCode() {
        int r = 31 * name.hashCode() + Double.valueOf(value).hashCode();
        return 31 * r + Double.valueOf(threshold).hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Benefit)) {
            return false;
        }
        Benefit b = (Benefit) other;
        
        return (benefitType.equals(b.benefitType) && name.equals(b.name) && value == b.value && b.threshold == threshold);
    }
}
