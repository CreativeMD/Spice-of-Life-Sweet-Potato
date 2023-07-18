package team.creative.solonion.common.benefit;

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
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BenefitThreshold thres)
            return benefit.equals(thres.benefit) && threshold == thres.threshold;
        return false;
    }
    
}