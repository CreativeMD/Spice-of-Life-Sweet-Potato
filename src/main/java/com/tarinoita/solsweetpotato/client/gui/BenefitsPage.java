package com.tarinoita.solsweetpotato.client.gui;

import static com.tarinoita.solsweetpotato.lib.Localization.localized;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.tarinoita.solsweetpotato.client.gui.elements.UILabel;
import com.tarinoita.solsweetpotato.tracking.benefits.BenefitInfo;
import com.tarinoita.solsweetpotato.utils.RomanNumber;

import net.minecraft.ResourceLocationException;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class BenefitsPage extends Page {
    private static final int BENEFITS_PER_PAGE = 3;
    private final Color activeColor;
    
    private BenefitsPage(Rectangle frame, String header, List<BenefitInfo> benefitInfo, Color activeColor) {
        super(frame, header);
        this.activeColor = activeColor;
        
        for (BenefitInfo info : benefitInfo) {
            addBenefitInfo(info);
        }
    }
    
    public static List<BenefitsPage> pages(Rectangle frame, String header, List<BenefitInfo> benefitInfo, Color activeColor) {
        List<BenefitsPage> pages = new ArrayList<>();
        for (int startIndex = 0; startIndex < benefitInfo.size(); startIndex += BENEFITS_PER_PAGE) {
            int endIndex = Math.min(startIndex + BENEFITS_PER_PAGE, benefitInfo.size());
            pages.add(new BenefitsPage(frame, header, benefitInfo.subList(startIndex, endIndex), activeColor));
        }
        return pages;
    }
    
    private void addBenefitInfo(BenefitInfo info) {
        String thresh = "" + info.threshold;
        String name = info.name;
        double value = info.value;
        
        if (info.type.equals("effect")) {
            name = getEffectName(name);
            int amplifier = (int) value;
            name = name + " " + RomanNumber.toRoman(amplifier + 1);
        } else if (info.type.equals("attribute")) {
            name = getAttributeName(name);
            String op = "+";
            if (value < 0) {
                op = "-";
            }
            String modifierValue = op + Math.abs(value);
            name = name + " " + modifierValue;
        }
        
        UILabel thresholdLabel = new UILabel(localized("gui", "food_book.benefits.threshold_label") + ": " + thresh);
        thresholdLabel.color = activeColor;
        
        if (activeColor.equals(FoodBookScreen.activeGreen)) {
            thresholdLabel.tooltip = localized("gui", "food_book.benefits.active_tooltip");
        } else if (activeColor.equals((FoodBookScreen.inactiveRed))) {
            thresholdLabel.tooltip = localized("gui", "food_book.benefits.inactive_tooltip");
        }
        
        UILabel nameLabel = new UILabel(name);
        nameLabel.color = FoodBookScreen.lessBlack;
        
        mainStack.addChild(thresholdLabel);
        mainStack.addChild(nameLabel);
        mainStack.addChild(makeSeparatorLine());
        updateMainStack();
    }
    
    private String getAttributeName(String name) {
        Attribute attribute;
        try {
            attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(name));
        } catch (ResourceLocationException e) {
            return "Invalid: " + name;
        }
        
        if (attribute == null) {
            return "Invalid: " + name;
        }
        
        return I18n.get(attribute.getDescriptionId());
    }
    
    private String getEffectName(String name) {
        MobEffect effect;
        IForgeRegistry<MobEffect> registry = RegistryManager.ACTIVE.getRegistry(Registries.MOB_EFFECT);
        try {
            effect = registry.getValue(new ResourceLocation(name));
        } catch (ResourceLocationException e) {
            return "Invalid: " + name;
        }
        
        if (effect == null) {
            return "Invalid: " + name;
        }
        
        return I18n.get(effect.getDisplayName().getString());
    }
}
