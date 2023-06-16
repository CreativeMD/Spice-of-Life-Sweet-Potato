package com.tarinoita.solsweetpotato.client.gui.screen;

import static com.tarinoita.solsweetpotato.lib.Localization.localized;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.tarinoita.solsweetpotato.SOLSweetPotato;
import com.tarinoita.solsweetpotato.SOLSweetPotatoConfig;
import com.tarinoita.solsweetpotato.client.gui.BenefitsPage;
import com.tarinoita.solsweetpotato.client.gui.DiversityPage;
import com.tarinoita.solsweetpotato.client.gui.FoodListPage;
import com.tarinoita.solsweetpotato.client.gui.Page;
import com.tarinoita.solsweetpotato.client.gui.PageFlipButton;
import com.tarinoita.solsweetpotato.client.gui.elements.UIElement;
import com.tarinoita.solsweetpotato.client.gui.elements.UIImage;
import com.tarinoita.solsweetpotato.client.gui.elements.UILabel;
import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import com.tarinoita.solsweetpotato.tracking.benefits.BenefitInfo;
import com.tarinoita.solsweetpotato.tracking.benefits.BenefitsHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class FoodBookScreen extends Screen implements PageFlipButton.Pageable {
    private static final ResourceLocation texture = new ResourceLocation(SOLSweetPotato.MODID, "textures/gui/food_book.png");
    private static final UIImage.Image bookImage = new UIImage.Image(texture, new Rectangle(0, 0, 186, 192));
    public static final UIImage.Image carrotImage = new UIImage.Image(texture, new Rectangle(0, 240, 16, 16));
    
    public static final Color fullBlack = Color.BLACK;
    public static final Color lessBlack = new Color(0, 0, 0, 128);
    public static final Color leastBlack = new Color(0, 0, 0, 64);
    public static final Color activeGreen = new Color(29, 104, 29, 255);
    public static final Color inactiveRed = new Color(104, 29, 29, 255);
    
    private final List<UIElement> elements = new ArrayList<>();
    private UIImage background;
    private UILabel pageNumberLabel;
    
    private PageFlipButton nextPageButton;
    private PageFlipButton prevPageButton;
    
    private Player player;
    private Set<Map.Entry<FoodInstance, Integer>> foodData;
    
    private final List<Page> pages = new ArrayList<>();
    private int currentPageNumber = 0;
    
    public static void open(Player player) {
        Minecraft.getInstance().setScreen(new FoodBookScreen(player));
    }
    
    public FoodBookScreen(Player player) {
        super(Component.literal(""));
        this.player = player;
    }
    
    @Override
    public void init() {
        super.init();
        
        foodData = FoodList.get(player).getData();
        
        background = new UIImage(bookImage);
        background.setCenterX(width / 2);
        background.setCenterY(height / 2);
        
        elements.clear();
        
        // page number
        pageNumberLabel = new UILabel("1");
        pageNumberLabel.setCenterX(background.getCenterX());
        pageNumberLabel.setMinY(background.getMinY() + 156);
        elements.add(pageNumberLabel);
        
        initPages();
        
        int pageFlipButtonSpacing = 50;
        prevPageButton = addRenderableWidget(new PageFlipButton(background.getCenterX() - pageFlipButtonSpacing / 2 - PageFlipButton.width, background
                .getMinY() + 152, PageFlipButton.Direction.BACKWARD, this));
        nextPageButton = addRenderableWidget(new PageFlipButton(background.getCenterX() + pageFlipButtonSpacing / 2, background
                .getMinY() + 152, PageFlipButton.Direction.FORWARD, this));
        
        updateButtonVisibility();
    }
    
    private void initPages() {
        pages.clear();
        
        double foodDiversity = FoodList.foodDiversity(foodData);
        int foodEaten = FoodList.get(player).getFoodsEaten();
        pages.add(new DiversityPage(foodDiversity, foodEaten, background.frame));
        
        List<Map.Entry<FoodInstance, Integer>> dataList = new ArrayList<>(foodData);
        dataList.sort(Map.Entry.comparingByValue());
        List<Item> foods = dataList.stream().map(Map.Entry::getKey).map(FoodInstance::getItem).collect(Collectors.toList());
        addPages("food_queue_label", foods);
        
        Pair<List<BenefitInfo>, List<BenefitInfo>> benefits = BenefitsHandler.getBenefitInfo(foodDiversity, foodEaten);
        List<BenefitInfo> activeBenefits = benefits.getKey().stream().filter(bi -> !bi.detriment).toList();
        List<BenefitInfo> inactiveDetriments = benefits.getKey().stream().filter(bi -> bi.detriment).toList();
        List<BenefitInfo> inactiveBenefits = benefits.getValue().stream().filter(bi -> !bi.detriment).toList();
        List<BenefitInfo> activeDetriments = benefits.getValue().stream().filter(bi -> bi.detriment).toList();
        
        addPages("active_detriments_header", activeDetriments, inactiveRed);
        addPages("active_benefits_header", activeBenefits, activeGreen);
        
        if (SOLSweetPotatoConfig.shouldShowInactiveBenefits()) {
            addPages("inactive_detriments_header", inactiveDetriments, activeGreen);
            addPages("inactive_benefits_header", inactiveBenefits, inactiveRed);
        }
    }
    
    private void addPages(String headerLocalizationPath, List<BenefitInfo> benefitInfoList, Color activeColor) {
        String header = localized("gui", "food_book." + headerLocalizationPath);
        
        pages.addAll(BenefitsPage.pages(background.frame, header, benefitInfoList, activeColor));
    }
    
    private void addPages(String headerLocalizationPath, List<Item> items) {
        String header = localized("gui", "food_book." + headerLocalizationPath, items.size());
        List<ItemStack> stacks = items.stream().map(ItemStack::new).collect(Collectors.toList());
        Map<FoodInstance, Integer> foodMap = new HashMap<>();
        for (Map.Entry<FoodInstance, Integer> entry : foodData)
            foodMap.put(entry.getKey(), entry.getValue());
        
        pages.addAll(FoodListPage.pages(background.frame, header, stacks, foodMap));
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
        
        UIElement.render(graphics, background, mouseX, mouseY);
        
        super.render(graphics, mouseX, mouseY, partialTicks);
        
        if (!pages.isEmpty()) { // might not be loaded yet; race condition
            // current page
            UIElement.render(graphics, elements, mouseX, mouseY);
            UIElement.render(graphics, pages.get(currentPageNumber), mouseX, mouseY);
        }
    }
    
    @Override
    public void switchToPage(int pageNumber) {
        if (!isWithinRange(pageNumber))
            return;
        
        currentPageNumber = pageNumber;
        updateButtonVisibility();
        
        pageNumberLabel.text = "" + (currentPageNumber + 1);
    }
    
    @Override
    public int getCurrentPageNumber() {
        return currentPageNumber;
    }
    
    @Override
    public boolean isWithinRange(int pageNumber) {
        return pageNumber >= 0 && pageNumber < pages.size();
    }
    
    private void updateButtonVisibility() {
        prevPageButton.updateState();
        nextPageButton.updateState();
    }
}
