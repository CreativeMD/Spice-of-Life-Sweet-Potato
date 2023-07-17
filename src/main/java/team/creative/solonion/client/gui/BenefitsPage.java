package team.creative.solonion.client.gui;

import static team.creative.solonion.lib.Localization.localized;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.solonion.benefit.BenefitThreshold;
import team.creative.solonion.client.gui.elements.UILabel;
import team.creative.solonion.client.gui.screen.FoodBookScreen;
import team.creative.solonion.utils.RomanNumber;

public class BenefitsPage extends Page {
    private static final int BENEFITS_PER_PAGE = 3;
    private final Color activeColor;
    
    private BenefitsPage(Rectangle frame, String header, List<BenefitThreshold> benefitInfo, Color activeColor) {
        super(frame, header);
        this.activeColor = activeColor;
        
        for (BenefitThreshold info : benefitInfo) {
            addBenefitInfo(info);
        }
    }
    
    public static List<BenefitsPage> pages(Rectangle frame, String header, List<BenefitThreshold> benefitInfo, Color activeColor) {
        List<BenefitsPage> pages = new ArrayList<>();
        for (int startIndex = 0; startIndex < benefitInfo.size(); startIndex += BENEFITS_PER_PAGE) {
            int endIndex = Math.min(startIndex + BENEFITS_PER_PAGE, benefitInfo.size());
            pages.add(new BenefitsPage(frame, header, benefitInfo.subList(startIndex, endIndex), activeColor));
        }
        return pages;
    }
    
    private void addBenefitInfo(BenefitThreshold info) {
        String thresh = "" + info.threshold;
        String name = "";
        double value = info.benefit.value;
        
        if (info.benefit.property.value instanceof MobEffect m) {
            name = LanguageUtils.translate(m.getDescriptionId());
            int amplifier = (int) value;
            name = name + " " + RomanNumber.toRoman(amplifier + 1);
        } else if (info.benefit.property.value instanceof Attribute a) {
            name = LanguageUtils.translate(a.getDescriptionId());
            String op = "+";
            if (value < 0) {
                op = "-";
            }
            String modifierValue = op + Math.abs(value);
            name = name + " " + modifierValue;
        }
        
        UILabel thresholdLabel = new UILabel(localized("gui", "food_book.benefits.threshold_label") + ": " + thresh);
        thresholdLabel.color = activeColor;
        
        if (activeColor.equals(FoodBookScreen.activeGreen))
            thresholdLabel.tooltip = localized("gui", "food_book.benefits.active_tooltip");
        else if (activeColor.equals((FoodBookScreen.inactiveRed)))
            thresholdLabel.tooltip = localized("gui", "food_book.benefits.inactive_tooltip");
        
        UILabel nameLabel = new UILabel(name);
        nameLabel.color = FoodBookScreen.lessBlack;
        
        mainStack.addChild(thresholdLabel);
        mainStack.addChild(nameLabel);
        mainStack.addChild(makeSeparatorLine());
        updateMainStack();
    }
}
