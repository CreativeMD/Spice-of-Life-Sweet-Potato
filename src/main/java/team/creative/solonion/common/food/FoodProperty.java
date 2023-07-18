package team.creative.solonion.common.food;

import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.util.ingredient.CreativeIngredient;

public class FoodProperty {
    
    @CreativeConfig
    public CreativeIngredient ingredient;
    
    @CreativeConfig
    public double diversity;
    
    public FoodProperty(CreativeIngredient ingredient, double diversity) {
        this.ingredient = ingredient;
        this.diversity = diversity;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FoodProperty prop)
            return diversity == prop.diversity && ingredient.equals(prop.ingredient);
        return super.equals(obj);
    }
    
}