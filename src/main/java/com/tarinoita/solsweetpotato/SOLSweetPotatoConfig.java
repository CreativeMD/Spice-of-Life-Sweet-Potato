package com.tarinoita.solsweetpotato;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.tarinoita.solsweetpotato.tracking.CapabilityHandler;
import com.tarinoita.solsweetpotato.tracking.benefits.BenefitsHandler;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = SOLSweetPotato.MOD_ID)
public final class SOLSweetPotatoConfig {
    private static String localizationPath(String path) {
        return "config." + SOLSweetPotato.MOD_ID + "." + path;
    }
    
    public static final Server SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;
    
    static {
        Pair<Server, ForgeConfigSpec> specPair = new Builder().configure(Server::new);
        SERVER = specPair.getLeft();
        SERVER_SPEC = specPair.getRight();
    }
    
    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;
    
    static {
        Pair<Client, ForgeConfigSpec> specPair = new Builder().configure(Client::new);
        CLIENT = specPair.getLeft();
        CLIENT_SPEC = specPair.getRight();
    }
    
    public static void setUp() {
        ModLoadingContext context = ModLoadingContext.get();
        context.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }
    
    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null)
            return;
        
        PlayerList players = currentServer.getPlayerList();
        for (Player player : players.getPlayers()) {
            BenefitsHandler.removeAllBenefits(player);
            BenefitsHandler.updatePlayer(player);
            CapabilityHandler.syncFoodList(player);
        }
    }
    
    public static List<String> getBlacklist() {
        return new ArrayList<>(SERVER.blacklist.get());
    }
    
    public static List<String> getWhitelist() {
        return new ArrayList<>(SERVER.whitelist.get());
    }
    
    public static List<String> getBenefitsUnparsed() {
        return new ArrayList<>(SERVER.benefitsUnparsed.get());
    }
    
    public static List<String> getComplexityUnparsed() {
        return new ArrayList<>(SERVER.complexityUnparsed.get());
    }
    
    public static List<Double> getThresholds() {
        return new ArrayList<>(SERVER.thresholds.get());
    }
    
    public static boolean shouldResetOnDeath() {
        return SERVER.shouldResetOnDeath.get();
    }
    
    public static boolean limitProgressionToSurvival() {
        return SERVER.limitProgressionToSurvival.get();
    }
    
    public static boolean shouldForbiddenCount() {
        return SERVER.shouldForbiddenCount.get();
    }
    
    public static Integer size() {
        return SERVER.queueSize.get();
    }
    
    public static Integer endDecay() {
        return SERVER.endDecay.get();
    }
    
    public static Integer startDecay() {
        return SERVER.startDecay.get();
    }
    
    public static Double minContribution() {
        return SERVER.minContribution.get();
    }
    
    public static Double defaultContribution() {
        return SERVER.defaultContribution.get();
    }
    
    public static Integer minFoodsToActivate() {
        return SERVER.minFoodsToActivate.get();
    }
    
    public static class Server {
        public final ConfigValue<List<? extends String>> blacklist;
        public final ConfigValue<List<? extends String>> whitelist;
        
        public final IntValue queueSize;
        
        public final BooleanValue shouldResetOnDeath;
        public final BooleanValue limitProgressionToSurvival;
        
        public final ConfigValue<List<? extends Double>> thresholds;
        public final ConfigValue<List<? extends String>> benefitsUnparsed;
        public final IntValue minFoodsToActivate;
        
        public final DoubleValue minContribution;
        public final DoubleValue defaultContribution;
        public final IntValue endDecay;
        public final IntValue startDecay;
        
        public final BooleanValue shouldForbiddenCount;
        
        public final ConfigValue<List<? extends String>> complexityUnparsed;
        
        Server(Builder builder) {
            builder.push("Benefits");
            
            thresholds = builder.translation(localizationPath("thresholds"))
                    .comment(" A list of diversity value thresholds, in ascending order. When the player's food diversity reaches a threshold,\n" + " they will get the benefits associated with that threshold.\n" + "\n")
                    .defineList("thresholds", Lists.newArrayList(3.0, 5.0, 7.0, 10.0, 13.0, 18.0, 25.0, 31.0), e -> e instanceof Double);
            
            benefitsUnparsed = builder.translation(localizationPath("benefits_unparsed"))
                    .comment(" \n Define custom benefits here. Each entry in the list corresponds to a benefit that will be obtained\n" + " at the corresponding diversity threshold defined the list above. For example, the first entry in\n" + " this list will be applied when the player's food diversity reaches the number in the first entry in\n" + " the threshold list above.\n" + " A benefit can also be marked as a detriment. In that case, its activation is reversed.\n" + " A detriment is applied while the player has less diversity than the threshold,\n" + " and will be removed when the threshold is reached.\n" + " Each benefit is a string with the following form: [+/-][type],[registry name],[value] (without the brackets)\n" + " A leading plus (or the of a symbol) denotes a benefit, while a minus denotes a detriment.\n" + " The type can either be 'attribute' for attribute modifiers or 'effect' for potion effects\n" + " Registry names for common vanila attributes are \n" + " generic.max_health, generic.knockback_resistance, generic.movement_speed, generic.luck, \n" + " generic.attack_damage, generic.attack_speed, generic.armor, generic.armor_toughness \n" + " The value of attributes is the numerical number that will be added to that attribute\n" + " Use a negative number for subtraction. Multiplicative modifiers are not supported.\n" + " For potion effects, the value is an integer and is the potion effect amplifier. Note\n" + " that the amplifier is 0 indexed, so minecraft:strength,1 corresponds to Strength II\n" + "\n" + " To add multiple benefits to the same threshold, separate them by a semicolon ';'\n" + " Make sure that you have NO SPACES!\n" + " As an example, 'attribute,generic.max_health,2;effect,strength,1' will give both +2 max hp\n" + " and Strength II at the corresponding threshold.\n" + " 'attribute,generic.attack_damage,1;-effect,slowness,0' will give +1 attack damage at the corresponding threshold\n" + " and Slowness I below the corresponding threshold.\n" + "\n")
                    .defineList("benefitsUnparsed", Lists
                            .newArrayList("attribute,generic.max_health,2", "attribute,generic.max_health,2;effect,strength,0", "attribute,generic.max_health,2;effect,regeneration,0", "attribute,generic.max_health,2;effect,speed,0", "attribute,generic.max_health,2;attribute,generic.armor_toughness,2", "attribute,generic.armor_toughness,2;effect,strength,1", "attribute,generic.max_health,4", "attribute,generic.max_health,6"), e -> e instanceof String);
            
            minFoodsToActivate = builder.translation(localizationPath("min_foods_to_activate"))
                    .comment(" The minimum number of foods a player needs to eat before any benefits are applied.\n" + "\n").defineInRange("minFoodsToActivate", 0, 0, 1000);
            
            builder.pop();
            builder.push("Filtering");
            
            blacklist = builder.translation(localizationPath("blacklist")).comment(" Foods in this list won't contribute to food diversity.\n" + "\n")
                    .defineList("blacklist", Lists.newArrayList(), e -> e instanceof String);
            
            whitelist = builder.translation(localizationPath("whitelist"))
                    .comment("\n When this list contains anything, the blacklist is ignored and instead only foods from here count.\n" + "\n")
                    .defineList("whitelist", Lists.newArrayList(), e -> e instanceof String);
            
            builder.pop();
            builder.push("Miscellaneous");
            
            shouldResetOnDeath = builder.translation(localizationPath("reset_on_death"))
                    .comment(" Whether or not to reset food diversity on death, effectively losing all benefits.\n" + "\n").define("resetOnDeath", false);
            
            limitProgressionToSurvival = builder.translation(localizationPath("limit_progression_to_survival"))
                    .comment("\n If true, eating foods outside of survival mode (e.g. creative/adventure) is not tracked.\n" + "\n").define("limitProgressionToSurvival", false);
            
            queueSize = builder.translation(localizationPath("queue_size"))
                    .comment("\n How many foods should be tracked. I.e., how many food items eaten in the past should count toward food diversity.\n" + " Note that the larger this is, the higher your potential diversity value can be, so keep this mind\n" + " if you are defining custom thresholds/benefits above.\n" + " !!!If you update queueSize, and leave the other advanced options unchanged,\n" + " make sure you change endDecay (below) to match queueSize, or else nothing will change!!!\n" + "\n")
                    .defineInRange("queueSize", 32, 1, 1000);
            
            builder.pop();
            builder.push("Advanced");
            
            minContribution = builder.translation(localizationPath("min_contribution"))
                    .comment(" These config options all affect the technical details of how diversity is calculated.\n" + " Please look at the explanation on the wiki on the github to see how these values work.\n" + "\n" + " Lowest possible diversity contribution a food can give. This is a multiplier, not an\n" + " absolute value!\n" + "\n")
                    .defineInRange("minContribution", 0.0, 0.0, 1.0);
            
            defaultContribution = builder.translation(localizationPath("default_contribution"))
                    .comment("\n The default diversity value when you eat a food. There is little reason to ever change this.\n" + "\n")
                    .defineInRange("defaultContribution", 1.0, 0.0, 100.0);
            
            endDecay = builder.translation(localizationPath("end_decay"))
                    .comment("\n How many meals in the past should the diversity penalty stop from.\n" + " **Needs to be less than queueSize and greater than startDecay!!!**\n" + " Note that if you update queueSize, to retain the default behavior, you need to also\n" + " set endDecay equal to the queueSize\n" + "\n")
                    .defineInRange("endDecay", 32, 0, 1000);
            
            startDecay = builder.translation(localizationPath("start_decay"))
                    .comment("\n How many meals in the past should the diversity time penalty start to apply.\n" + " **Needs to be less than queueSize and less than or equal to endDecay!!!**\n" + "\n")
                    .defineInRange("startDecay", 0, 0, 1000);
            
            shouldForbiddenCount = builder.translation(localizationPath("should_forbidden_count"))
                    .comment("\n Whether blacklisted foods should still take a spot in the queue, even if they don't contribute any diversity.\n" + "\n")
                    .define("shouldForbiddenCount", true);
            
            builder.pop();
            builder.push("Complexity");
            
            complexityUnparsed = builder.translation(localizationPath("complexity_unparsed"))
                    .comment(" Define custom complexity values for individual foods here.\n" + " The complexity value of a food is how much diversity points it gives. \n" + " The base diversity value of foods not defined here is equal to defaultContribution.\n" + " Each entry in the list should be a string defining one food, and the format is [registry name],[value]\n" + " Note that tags are NOT currently supported.\n" + "\n")
                    .defineList("complexityUnparsed", Lists
                            .newArrayList("minecraft:golden_carrot,2", "minecraft:golden_apple,2", "minecraft:enchanted_golden_apple,5"), e -> e instanceof String);
            
            builder.pop();
        }
    }
    
    public static boolean isFoodTooltipEnabled() {
        return CLIENT.isFoodTooltipEnabled.get();
    }
    
    public static boolean shouldShowInactiveBenefits() {
        return CLIENT.shouldShowInactiveBenefits.get();
    }
    
    public static class Client {
        public final BooleanValue isFoodTooltipEnabled;
        public final BooleanValue shouldShowInactiveBenefits;
        
        Client(Builder builder) {
            builder.push("miscellaneous");
            
            isFoodTooltipEnabled = builder.translation(localizationPath("is_food_tooltip_enabled"))
                    .comment(" If true, foods indicate in their tooltips the last time they've been eaten, and their current diversity contribution." + "\n")
                    .define("isFoodTooltipEnabled", true);
            
            shouldShowInactiveBenefits = builder.translation(localizationPath("should_show_inactive_benefits"))
                    .comment("\n If true, the food book lists benefits that you haven't acquired yet, in addition to the ones you have.\n" + "\n")
                    .define("shouldShowInactiveBenefits", true);
            
            builder.pop();
        }
    }
    
    // TODO: investigate performance of all these get() calls
    
    public static boolean hasWhitelist() {
        return !SERVER.whitelist.get().isEmpty();
    }
    
    public static boolean isAllowed(Item food) {
        String id = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(food)).toString();
        if (hasWhitelist()) {
            return matchesAnyPattern(id, SERVER.whitelist.get());
        } else {
            return !matchesAnyPattern(id, SERVER.blacklist.get());
        }
    }
    
    public static boolean shouldCount(Item food) {
        return isAllowed(food);
    }
    
    private static boolean matchesAnyPattern(String query, Collection<? extends String> patterns) {
        for (String glob : patterns) {
            StringBuilder pattern = new StringBuilder(glob.length());
            for (String part : glob.split("\\*", -1)) {
                if (!part.isEmpty()) { // not necessary
                    pattern.append(Pattern.quote(part));
                }
                pattern.append(".*");
            }
            
            // delete extraneous trailing ".*" wildcard
            pattern.delete(pattern.length() - 2, pattern.length());
            
            if (Pattern.matches(pattern.toString(), query)) {
                return true;
            }
        }
        return false;
    }
}
