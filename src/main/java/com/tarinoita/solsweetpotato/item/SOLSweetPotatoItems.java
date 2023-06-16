package com.tarinoita.solsweetpotato.item;

import com.tarinoita.solsweetpotato.SOLSweetPotato;
import com.tarinoita.solsweetpotato.item.foodcontainer.FoodContainerItem;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SOLSweetPotatoItems {
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SOLSweetPotato.MODID);
    
    public static final RegistryObject<Item> BOOK = ITEMS.register("food_book", () -> new FoodBookItem());
    public static final RegistryObject<Item> LUNCHBOX = ITEMS.register("lunchbox", () -> new FoodContainerItem(9, "lunchbox"));
    public static final RegistryObject<Item> LUNCHBAG = ITEMS.register("lunchbag", () -> new FoodContainerItem(5, "lunchbag"));
    public static final RegistryObject<Item> GOLDEN_LUNCHBOX = ITEMS.register("golden_lunchbox", () -> new FoodContainerItem(14, "golden_lunchbox"));
    
    public static void registerTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(BOOK);
            event.accept(LUNCHBOX);
            event.accept(LUNCHBAG);
            event.accept(GOLDEN_LUNCHBOX);
        }
    }
}
