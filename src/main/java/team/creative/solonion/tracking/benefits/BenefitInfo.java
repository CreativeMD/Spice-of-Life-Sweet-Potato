package team.creative.solonion.tracking.benefits;

public class BenefitInfo {
    public String type;
    public String name;
    public double value;
    public double threshold;
    public boolean detriment;
    
    public BenefitInfo(String type, String name, double value, double threshold, boolean detriment) {
        this.type = type;
        this.name = name;
        this.value = value;
        this.threshold = threshold;
        this.detriment = detriment;
    }
}
