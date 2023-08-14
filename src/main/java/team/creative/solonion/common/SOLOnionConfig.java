package team.creative.solonion.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.server.players.PlayerList;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.server.ServerLifecycleHooks;
import team.creative.creativecore.Side;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.api.ICreativeConfig;
import team.creative.creativecore.common.config.converation.ConfigTypeConveration;
import team.creative.creativecore.common.config.sync.ConfigSynchronization;
import team.creative.creativecore.common.util.ingredient.CreativeIngredientItem;
import team.creative.creativecore.common.util.type.list.SortingList;
import team.creative.solonion.api.SOLOnionAPI;
import team.creative.solonion.common.benefit.Benefit;
import team.creative.solonion.common.benefit.BenefitThreshold;
import team.creative.solonion.common.food.FoodProperty;

public final class SOLOnionConfig implements ICreativeConfig {
    
    static {
        ConfigTypeConveration.registerTypeCreator(BenefitThreshold.class, () -> new BenefitThreshold(3, Benefit.create(Attributes.MAX_HEALTH, 2)));
        ConfigTypeConveration.registerTypeCreator(FoodProperty.class, () -> new FoodProperty(new CreativeIngredientItem(Items.GOLDEN_CARROT), 2));
    }
    
    @CreativeConfig
    public SortingList foodItems = new SortingList(false);
    
    @CreativeConfig
    @CreativeConfig.IntRange(min = 1, max = 1000, slider = true)
    public int trackCount = 32;
    
    @CreativeConfig
    public boolean resetOnDeath = true;
    
    @CreativeConfig
    public boolean limitProgressionToSurvival = false;
    
    @CreativeConfig
    @CreativeConfig.IntRange(min = 0, max = 1000, slider = true)
    public int minFoodsToActivate = 0;
    
    @CreativeConfig
    public List<BenefitThreshold> benefits = Arrays.asList(new BenefitThreshold(3, Benefit.create(Attributes.MAX_HEALTH, 2)), new BenefitThreshold(5, Benefit.create(
        MobEffects.DAMAGE_BOOST, 0)), new BenefitThreshold(7, Benefit.create(MobEffects.REGENERATION, 0)), new BenefitThreshold(10, Benefit.create(MobEffects.MOVEMENT_SPEED, 0)),
        new BenefitThreshold(13, Benefit.create(Attributes.ARMOR_TOUGHNESS, 2)), new BenefitThreshold(18, Benefit.create(MobEffects.DAMAGE_BOOST, 1)),
        new BenefitThreshold(25, Benefit.create(Attributes.MAX_HEALTH, 4)), new BenefitThreshold(31, Benefit.create(Attributes.MAX_HEALTH, 6)));
    
    @CreativeConfig
    public boolean shouldExcludedCount = true;
    
    @CreativeConfig
    public double defaultDiversity = 1;
    
    @CreativeConfig
    public List<FoodProperty> foodDiversity = Arrays.asList(new FoodProperty(new CreativeIngredientItem(Items.GOLDEN_CARROT), 2),
        new FoodProperty(new CreativeIngredientItem(Items.GOLDEN_APPLE), 2), new FoodProperty(new CreativeIngredientItem(Items.ENCHANTED_GOLDEN_APPLE), 5));
    
    @CreativeConfig(type = ConfigSynchronization.CLIENT)
    public boolean isFoodTooltipEnabled = true;
    @CreativeConfig(type = ConfigSynchronization.CLIENT)
    public boolean shouldShowInactiveBenefits = true;
    
    @Override
    public void configured(Side side) {
        Collections.sort(benefits);
        
        if (side.isClient())
            return;
        
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            PlayerList players = ServerLifecycleHooks.getCurrentServer().getPlayerList();
            for (Player player : players.getPlayers()) {
                SOLOnionAPI.getFoodCapability(player).configChanged();
                SOLOnion.EVENT.updatePlayerBenefits(player);
                SOLOnion.EVENT.syncFoodList(player);
            }
        }
    }
    
    public boolean isAllowed(ItemStack food) {
        return foodItems.canPass(food);
    }
    
    public double getDiversity(ItemStack food) {
        for (FoodProperty property : foodDiversity)
            if (property.ingredient.is(food))
                return property.diversity;
        return defaultDiversity;
    }
}
