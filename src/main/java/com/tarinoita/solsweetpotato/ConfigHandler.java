package com.tarinoita.solsweetpotato;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tarinoita.solsweetpotato.communication.ConfigMessage;
import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import com.tarinoita.solsweetpotato.tracking.benefits.Benefit;
import com.tarinoita.solsweetpotato.tracking.benefits.BenefitList;
import com.tarinoita.solsweetpotato.utils.BenefitsParser;
import com.tarinoita.solsweetpotato.utils.ComplexityParser;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = SOLSweetPotato.MOD_ID)
public class ConfigHandler {
    public static Map<FoodInstance, Double> complexityMap = new HashMap<>();
    public static List<Double> thresholds = new ArrayList<>();
    public static BenefitList benefitsList = new BenefitList(new ArrayList<>());
    
    public final static String COMPLEXITY_MAP_KEY = "complexity_map";
    public final static String THRESHOLDS_KEY = "thresholds";
    public final static String BENEFITS_KEY = "benefits_list";
    public final static String FOOD_KEY = "food";
    public final static String COMPLEXITY_VALUE_KEY = "complexity";
    public final static String ENTRY_KEY = "entries";
    
    public static boolean isFirstAid = false;
    
    public static CompoundTag serializeComplexityMap() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Map.Entry<FoodInstance, Double> entry : complexityMap.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            String encoded = entry.getKey().encode();
            entryTag.put(FOOD_KEY, StringTag.valueOf(encoded));
            entryTag.put(COMPLEXITY_VALUE_KEY, DoubleTag.valueOf(entry.getValue()));
            list.add(entryTag);
        }
        tag.put(ENTRY_KEY, list);
        return tag;
    }
    
    public static ListTag serializeThresholds() {
        ListTag tag = new ListTag();
        
        for (double t : thresholds) {
            tag.add(DoubleTag.valueOf(t));
        }
        
        return tag;
    }
    
    public static CompoundTag serializeBenefitsList() {
        return benefitsList.serializeNBT();
    }
    
    public static CompoundTag serializeConfig() {
        CompoundTag tag = new CompoundTag();
        tag.put(COMPLEXITY_MAP_KEY, serializeComplexityMap());
        tag.put(THRESHOLDS_KEY, serializeThresholds());
        tag.put(BENEFITS_KEY, serializeBenefitsList());
        return tag;
    }
    
    public static void deserializeConfig(CompoundTag tag) {
        deserializeComplexityMap(tag.getCompound(COMPLEXITY_MAP_KEY));
        deserializeThresholds(tag.getList(THRESHOLDS_KEY, Tag.TAG_DOUBLE));
        deserializeBenefitsList(tag.getCompound(BENEFITS_KEY));
    }
    
    public static void deserializeBenefitsList(CompoundTag tag) {
        benefitsList.deserializeNBT(tag);
    }
    
    public static void deserializeComplexityMap(CompoundTag tag) {
        ListTag list = tag.getList(ENTRY_KEY, Tag.TAG_COMPOUND);
        Map<FoodInstance, Double> newComplexityMap = new HashMap<>();
        for (Tag nbt : list) {
            CompoundTag cnbt = (CompoundTag) nbt;
            String foodString = cnbt.getString(FOOD_KEY);
            FoodInstance food = FoodInstance.decode(foodString);
            double complexity = cnbt.getDouble(COMPLEXITY_VALUE_KEY);
            newComplexityMap.put(food, complexity);
        }
        complexityMap = newComplexityMap;
    }
    
    public static void deserializeThresholds(ListTag tag) {
        List<Double> newThresholds = new ArrayList<>();
        tag.stream().map(nbt -> (DoubleTag) nbt).map(DoubleTag::getAsDouble).forEach(newThresholds::add);
        thresholds = newThresholds;
    }
    
    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {
        complexityMap = ComplexityParser.parse(SOLSweetPotatoConfig.getComplexityUnparsed());
        thresholds = SOLSweetPotatoConfig.getThresholds();
        List<List<Benefit>> benefits = BenefitsParser.parse(SOLSweetPotatoConfig.getBenefitsUnparsed());
        benefitsList = new BenefitList(benefits);
        
        isFirstAid = ModList.get().isLoaded("firstaid");
    }
    
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        syncConfig(event.getEntity());
    }
    
    public static List<List<Benefit>> getBenefitsList() {
        return benefitsList.getBenefits();
    }
    
    public static void syncConfig(Player player) {
        if (player.level().isClientSide)
            return;
        
        ServerPlayer target = (ServerPlayer) player;
        SOLSweetPotato.channel.sendTo(new ConfigMessage(), target.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
