package team.creative.solonion.client.gui;

import static team.creative.solonion.lib.Localization.localized;

import java.awt.Color;
import java.awt.Rectangle;

import team.creative.solonion.SOLOnion;
import team.creative.solonion.client.gui.elements.UIBox;
import team.creative.solonion.client.gui.elements.UIElement;
import team.creative.solonion.client.gui.elements.UILabel;
import team.creative.solonion.client.gui.screen.FoodBookScreen;

public class DiversityPage extends Page {
    public DiversityPage(double foodDiversity, int foodEaten, Rectangle frame) {
        super(frame, localized("gui", "food_book.stats"));
        
        // Dummy box to center the diversity display
        mainStack.addChild(new UIBox(new Rectangle(0, 0, 1, 35), new Color(0, 0, 0, 0)));
        
        UIElement diversityDisplay = statWithIcon(icon(FoodBookScreen.carrotImage), String.format("%.2f", foodDiversity), localized("gui", "food_book.stats.current_diversity"));
        mainStack.addChild(diversityDisplay);
        
        if (foodEaten < SOLOnion.CONFIG.minFoodsToActivate) {
            int diff = SOLOnion.CONFIG.minFoodsToActivate - foodEaten;
            mainStack.addChild(new UIBox(new Rectangle(0, 0, 1, 10), new Color(0, 0, 0, 0)));
            UILabel minFoodLabel1 = new UILabel(localized("gui", "food_book.stats.min_warning1", diff));
            minFoodLabel1.color = FoodBookScreen.inactiveRed;
            UILabel minFoodLabel2 = new UILabel(localized("gui", "food_book.stats.min_warning2", diff));
            minFoodLabel2.color = FoodBookScreen.inactiveRed;
            mainStack.addChild(minFoodLabel1);
            mainStack.addChild(minFoodLabel2);
        }
        
        updateMainStack();
    }
}
