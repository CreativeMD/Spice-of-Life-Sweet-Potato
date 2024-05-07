package team.creative.solonion.common.benefit;

import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class BenefitStack {
    
    private final Object2DoubleArrayMap<Holder<Attribute>> attributes = new Object2DoubleArrayMap<>();
    private final Object2IntArrayMap<Holder<MobEffect>> effects = new Object2IntArrayMap<>();
    
    public BenefitStack() {}
    
    public BenefitStack(Iterable<Benefit> benefits) {
        addAll(benefits);
    }
    
    public void add(Benefit benefit) {
        if (benefit.property.value instanceof Attribute a) {
            attributes.compute(Holder.direct(a), (x, y) -> y != null ? Math.max(y, benefit.value) : benefit.value);
        } else if (benefit.property.value instanceof MobEffect m)
            effects.compute(Holder.direct(m), (x, y) -> y != null ? Math.max(y, (int) benefit.value) : (int) benefit.value);
    }
    
    public void addAll(Iterable<Benefit> benefits) {
        for (Benefit benefit : benefits)
            add(benefit);
    }
    
    public Iterable<Object2DoubleMap.Entry<Holder<Attribute>>> attributes() {
        return attributes.object2DoubleEntrySet();
    }
    
    public Iterable<Object2IntMap.Entry<Holder<MobEffect>>> effects() {
        return effects.object2IntEntrySet();
    }
    
    public boolean isEmpty() {
        return attributes.isEmpty() && effects.isEmpty();
    }
    
}
