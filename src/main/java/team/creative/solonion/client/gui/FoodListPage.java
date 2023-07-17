package team.creative.solonion.client.gui;

import static team.creative.solonion.lib.Localization.localized;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import team.creative.solonion.client.gui.elements.UIFoodQueueItem;
import team.creative.solonion.client.gui.elements.UIItemStack;

public final class FoodListPage extends ItemListPage {
    
    private FoodListPage(Rectangle frame, String header, List<ItemStack> items) {
        super(frame, header, items);
        
        setHeaderTooltip(localized("gui", "food_book.queue.food_queue_tooltip"));
        
        int minX = (1 - itemsPerRow) * itemSpacing / 2;
        int minY = (1 - rowsPerPage) * itemSpacing / 2 - 4;
        
        for (int i = 0; i < items.size(); i++) {
            ItemStack itemStack = items.get(i);
            int x = minX + itemSpacing * (i % itemsPerRow);
            int y = minY + itemSpacing * ((i / itemsPerRow) % rowsPerPage);
            
            int lastEaten = i; // foodMap.get(new FoodInstance(itemStack.getItem()));
            UIItemStack view = new UIFoodQueueItem(itemStack, lastEaten);
            view.setCenterX(getCenterX() + x);
            view.setCenterY(getCenterY() + y);
            children.add(view);
        }
    }
    
    public static List<ItemListPage> pages(Rectangle frame, String header, List<ItemStack> items) {
        List<ItemListPage> pages = new ArrayList<>();
        for (int startIndex = 0; startIndex < items.size(); startIndex += ItemListPage.itemsPerPage) {
            int endIndex = Math.min(startIndex + ItemListPage.itemsPerPage, items.size());
            pages.add(new FoodListPage(frame, header, items.subList(startIndex, endIndex)));
        }
        return pages;
    }
}
