package team.creative.solonion.client.gui.elements;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import team.creative.solonion.client.SOLOnionClient;

/** Renders an ItemStack representing a food in the FoodList. Has a unique tooltip that displays that food item's
 * contribution to the food diversity. */
public class UIFoodQueueItem extends UIItemStack {
    private final int lastEaten;
    private final double diversity;
    
    public UIFoodQueueItem(ItemStack itemStack, double diversity, int lastEaten) {
        super(itemStack);
        this.lastEaten = lastEaten;
        this.diversity = diversity;
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
        
        SOLOnionClient.addDiversityInfoTooltips(tooltip, diversity, lastEaten);
        
        return tooltip;
    }
}
