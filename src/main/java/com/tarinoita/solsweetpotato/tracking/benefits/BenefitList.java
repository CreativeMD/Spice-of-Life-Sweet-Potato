package com.tarinoita.solsweetpotato.tracking.benefits;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;

public class BenefitList {
    private List<List<Benefit>> benefits;
    
    public BenefitList(List<List<Benefit>> benefits) {
        this.benefits = benefits;
    }
    
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        
        int nthresholds = benefits.size();
        tag.put("nthresholds", IntTag.valueOf(nthresholds));
        int i = 0;
        
        for (List<Benefit> thresholdBenefits : benefits) {
            int nbenefits = thresholdBenefits.size();
            CompoundTag thresholdTag = new CompoundTag();
            thresholdTag.put("nbenefits", IntTag.valueOf(nbenefits));
            int j = 0;
            for (Benefit b : thresholdBenefits) {
                thresholdTag.put("benefit_" + j, b.serializeNBT());
                j++;
            }
            tag.put("threshold_" + i, thresholdTag);
            i++;
        }
        
        return tag;
    }
    
    public void deserializeNBT(CompoundTag tag) {
        List<List<Benefit>> newBenefits = new ArrayList<>();
        
        int nthresholds = tag.getInt("nthresholds");
        for (int i = 0; i < nthresholds; i++) {
            CompoundTag thresholdTag = tag.getCompound("threshold_" + i);
            int nbenefits = thresholdTag.getInt("nbenefits");
            List<Benefit> thresholdBenefits = new ArrayList<>();
            for (int j = 0; j < nbenefits; j++) {
                CompoundTag benefitTag = thresholdTag.getCompound("benefit_" + j);
                String benefitType = benefitTag.getString("type");
                if (benefitType.equals("attribute")) {
                    thresholdBenefits.add(AttributeBenefit.fromNBT(benefitTag));
                } else if (benefitType.equals("effect")) {
                    thresholdBenefits.add(EffectBenefit.fromNBT(benefitTag));
                } else {
                    throw new RuntimeException("Invalid benefit type: " + benefitType);
                }
            }
            newBenefits.add(thresholdBenefits);
        }
        
        benefits = newBenefits;
    }
    
    public List<List<Benefit>> getBenefits() {
        return benefits;
    }
}
