package team.creative.solonion.common.item;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import team.creative.solonion.common.SOLOnion;
import team.creative.solonion.common.item.foodcontainer.FoodContainer;
import team.creative.solonion.common.item.foodcontainer.FoodContainerItem;

public final class SOLOnionItems {
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SOLOnion.MODID);
    
    public static final RegistryObject<Item> BOOK = ITEMS.register("food_book", () -> new FoodBookItem());
    public static final RegistryObject<Item> LUNCHBOX = ITEMS.register("lunchbox", () -> new FoodContainerItem(9, "lunchbox"));
    public static final RegistryObject<Item> LUNCHBAG = ITEMS.register("lunchbag", () -> new FoodContainerItem(5, "lunchbag"));
    public static final RegistryObject<Item> GOLDEN_LUNCHBOX = ITEMS.register("golden_lunchbox", () -> new FoodContainerItem(14, "golden_lunchbox"));
    
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, SOLOnion.MODID);
    public static final RegistryObject<MenuType<FoodContainer>> FOOD_CONTAINER = MENU_TYPES.register("food_container", () -> IForgeMenuType.create(
        ((windowId, inv, data) -> new FoodContainer(windowId, inv, inv.player))));
    
    public static void registerTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(BOOK);
            event.accept(LUNCHBOX);
            event.accept(LUNCHBAG);
            event.accept(GOLDEN_LUNCHBOX);
        }
    }
}
