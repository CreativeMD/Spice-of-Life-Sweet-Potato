package team.creative.solonion.benefit;

import team.creative.creativecore.common.config.api.CreativeConfig;

public class BenefitThreshold implements Comparable<BenefitThreshold> {
    
    @CreativeConfig
    public double threshold;
    
    @CreativeConfig
    public Benefit benefit;
    
    public BenefitThreshold(double threshold, Benefit benefit) {
        this.threshold = threshold;
        this.benefit = benefit;
    }
    
    @Override
    public int compareTo(BenefitThreshold o) {
        return Double.compare(threshold, o.threshold);
    }
    
}