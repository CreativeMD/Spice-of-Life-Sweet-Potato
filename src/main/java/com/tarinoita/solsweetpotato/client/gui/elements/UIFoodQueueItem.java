package com.tarinoita.solsweetpotato.client.gui.elements;

import java.util.ArrayList;
import java.util.List;

import com.tarinoita.solsweetpotato.client.SOLSweetPotatoClient;
import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import com.tarinoita.solsweetpotato.tracking.FoodList;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Renders an ItemStack representing a food in the FoodList. Has a unique tooltip that displays that food item's
 * contribution to the food diversity. */
public class UIFoodQueueItem extends UIItemStack {
    private final int lastEaten;
    
    public UIFoodQueueItem(ItemStack itemStack, int lastEaten) {
        super(itemStack);
        
        this.lastEaten = lastEaten;
    }
    
    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        List<Component> tooltip = getFoodQueueTooltip();
        graphics.renderComponentTooltip(mc.font, tooltip, mouseX, mouseY);
    }
    
    private List<Component> getFoodQueueTooltip() {
        Component foodName = Component.translatable(itemStack.getItem().getDescriptionId(itemStack)).withStyle(itemStack.getRarity().color);
        
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(foodName);
        
        Component space = Component.literal("");
        tooltip.add(space);
        
        double contribution = FoodList.calculateDiversityContribution(new FoodInstance(itemStack.getItem()), lastEaten);
        
        SOLSweetPotatoClient.addDiversityInfoTooltips(tooltip, contribution, lastEaten);
        
        return tooltip;
    }
}
