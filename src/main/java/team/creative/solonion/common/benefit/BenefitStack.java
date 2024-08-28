package team.creative.solonion.common.benefit;

import java.util.HashMap;

public class BenefitStack {
    
    private final HashMap<BenefitType, Object> typeStack = new HashMap<>();
    
    public BenefitStack() {}
    
    public BenefitStack(Iterable<Benefit> benefits) {
        addAll(benefits);
    }
    
    public void add(Benefit benefit) {
        var type = BenefitType.getType(benefit);
        var stack = typeStack.get(type);
        if (stack == null)
            typeStack.put(type, stack = type.createStack());
        type.addToStack(benefit, stack);
    }
    
    public void addAll(Iterable<Benefit> benefits) {
        for (Benefit benefit : benefits)
            add(benefit);
    }
    
    public boolean isEmpty() {
        for (var entry : typeStack.entrySet())
            if (!entry.getKey().isEmpty(entry.getValue()))
                return false;
        return false;
    }
    
    public <T> T get(BenefitType<?, T, ?> type) {
        return (T) typeStack.get(type);
    }
    
}
