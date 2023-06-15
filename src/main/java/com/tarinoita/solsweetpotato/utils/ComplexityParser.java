package com.tarinoita.solsweetpotato.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tarinoita.solsweetpotato.SOLSweetPotato;
import com.tarinoita.solsweetpotato.tracking.FoodInstance;

import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class ComplexityParser {
    public static Map<FoodInstance, Double> parse(List<String> unparsed) {
        Map<FoodInstance, Double> complexityMap = new HashMap<>();
        
        for (String complexityString : unparsed) {
            String[] s = complexityString.split(",", 0);
            if (s.length != 2) {
                SOLSweetPotato.LOGGER.warn("Invalid complexity specification: " + complexityString);
                continue;
            }
            
            String foodString = s[0];
            double complexity = 1.0;
            try {
                complexity = Double.parseDouble(s[1]);
            } catch (NumberFormatException e) {
                SOLSweetPotato.LOGGER.warn("Second argument in complexity specification needs to be a number: " + complexityString);
                continue;
            }
            
            Item item;
            try {
                item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(foodString));
            } catch (ResourceLocationException e) {
                SOLSweetPotato.LOGGER.warn("Invalid item name: " + foodString);
                continue;
            }
            if (item == null) {
                SOLSweetPotato.LOGGER.warn("Invalid item name: " + foodString);
                continue;
            }
            
            if (!item.isEdible()) {
                SOLSweetPotato.LOGGER.warn("Item is not food: " + foodString);
                continue;
            }
            
            FoodInstance food = new FoodInstance(item);
            
            if (food.encode() == null) {
                SOLSweetPotato.LOGGER.warn("Item does not exist: " + foodString);
                continue;
            }
            
            complexityMap.put(food, complexity);
        }
        return complexityMap;
    }
}
