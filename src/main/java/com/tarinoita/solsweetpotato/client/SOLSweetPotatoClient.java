package com.tarinoita.solsweetpotato.client;

import static com.tarinoita.solsweetpotato.lib.Localization.localized;
import static com.tarinoita.solsweetpotato.lib.Localization.localizedComponent;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.tarinoita.solsweetpotato.SOLSweetPotato;
import com.tarinoita.solsweetpotato.SOLSweetPotatoConfig;
import com.tarinoita.solsweetpotato.client.gui.screen.FoodBookScreen;
import com.tarinoita.solsweetpotato.client.gui.screen.FoodContainerScreen;
import com.tarinoita.solsweetpotato.item.foodcontainer.FoodContainer;
import com.tarinoita.solsweetpotato.lib.Localization;
import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import com.tarinoita.solsweetpotato.tracking.FoodList;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SOLSweetPotatoClient {
    
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, SOLSweetPotato.MODID);
    public static final RegistryObject<MenuType<FoodContainer>> FOOD_CONTAINER = MENU_TYPES
            .register("food_container", () -> IForgeMenuType.create(((windowId, inv, data) -> new FoodContainer(windowId, inv, inv.player))));
    
    public static KeyMapping OPEN_FOOD_BOOK;
    
    public static void load(IEventBus bus) {
        bus.addListener(SOLSweetPotatoClient::setupClient);
        bus.addListener(SOLSweetPotatoClient::registerKeybinds);
        MinecraftForge.EVENT_BUS.addListener(SOLSweetPotatoClient::handleKeypress);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, SOLSweetPotatoClient::onItemTooltip);
        MENU_TYPES.register(bus);
    }
    
    public static void setupClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(FOOD_CONTAINER.get(), FoodContainerScreen::new));
    }
    
    public static void registerKeybinds(RegisterKeyMappingsEvent event) {
        event.register(OPEN_FOOD_BOOK = new KeyMapping(Localization.localized("key", "open_food_book"), InputConstants.UNKNOWN.getValue(), Localization
                .localized("key", "category")));
    }
    
    public static void handleKeypress(ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null)
            return;
        
        if (OPEN_FOOD_BOOK != null && OPEN_FOOD_BOOK.isDown())
            FoodBookScreen.open(player);
    }
    
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!SOLSweetPotatoConfig.isFoodTooltipEnabled())
            return;
        
        Player player = event.getEntity();
        if (player == null)
            return;
        
        Item food = event.getItemStack().getItem();
        if (!food.isEdible())
            return;
        
        FoodList foodList = FoodList.get(player);
        boolean hasBeenEaten = foodList.hasEaten(food);
        boolean isAllowed = SOLSweetPotatoConfig.isAllowed(food);
        
        List<Component> tooltip = event.getToolTip();
        if (!isAllowed)
            tooltip.add(localizedTooltip("disabled", ChatFormatting.DARK_GRAY));
        else {
            if (hasBeenEaten) {
                int lastEaten = foodList.getLastEaten(food);
                double contribution = FoodList.calculateDiversityContribution(new FoodInstance(food), lastEaten);
                
                addDiversityInfoTooltips(tooltip, contribution, lastEaten);
            }
        }
    }
    
    private static Component localizedTooltip(String path, ChatFormatting color) {
        return localizedComponent("tooltip", path).withStyle(style -> style.applyFormat(color));
    }
    
    public static List<Component> addDiversityInfoTooltips(List<Component> tooltip, double contribution, int lastEaten) {
        String contribution_path = "food_book.queue.tooltip.contribution_label";
        tooltip.add(Component.literal(localized("gui", contribution_path) + ": " + String.format("%.2f", contribution)).withStyle(ChatFormatting.GRAY));
        String last_eaten_path = "food_book.queue.tooltip.last_eaten_label";
        if (lastEaten == 1)
            last_eaten_path = "food_book.queue.tooltip.last_eaten_label_singular";
        tooltip.add(Component.literal(localized("gui", last_eaten_path, lastEaten)).withStyle(ChatFormatting.GRAY));
        return tooltip;
    }
}
