package team.creative.solonion.client.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.solonion.client.gui.elements.UIFoodQueueItem;
import team.creative.solonion.client.gui.elements.UIItemStack;
import team.creative.solonion.common.food.FoodCapabilityImpl;

public final class FoodListPage extends ItemListPage {
    
    private FoodListPage(Rectangle frame, String header, List<ItemStack> items) {
        super(frame, header, items);
        
        setHeaderTooltip(LanguageUtils.translate("gui.solonion.food_book.food_queue_tooltip"));
        
        int minX = (1 - itemsPerRow) * itemSpacing / 2;
        int minY = (1 - rowsPerPage) * itemSpacing / 2 - 4;
        
        int i = 0;
        for (Tuple<ItemStack, Double> tuple : FoodCapabilityImpl.calculateDiversityIndividualy(items)) {
            ItemStack itemStack = tuple.key;
            int x = minX + itemSpacing * (i % itemsPerRow);
            int y = minY + itemSpacing * ((i / itemsPerRow) % rowsPerPage);
            
            UIItemStack view = new UIFoodQueueItem(itemStack, tuple.value, i);
            view.setCenterX(getCenterX() + x);
            view.setCenterY(getCenterY() + y);
            children.add(view);
            i++;
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
