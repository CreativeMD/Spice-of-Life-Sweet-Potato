package team.creative.solonion.common;

import static net.minecraft.commands.Commands.literal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import team.creative.creativecore.common.config.holder.CreativeConfigRegistry;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.solonion.api.BenefitCapability;
import team.creative.solonion.api.FoodCapability;
import team.creative.solonion.api.SOLOnionAPI;
import team.creative.solonion.client.SOLOnionClient;
import team.creative.solonion.common.command.FoodListCommand;
import team.creative.solonion.common.event.SOLOnionEvent;
import team.creative.solonion.common.item.SOLOnionItems;
import team.creative.solonion.common.network.FoodListMessage;

@Mod(SOLOnion.MODID)
public final class SOLOnion {
    
    public static boolean isActive(Player player) {
        return (!SOLOnion.CONFIG.limitProgressionToSurvival || PlayerUtils.getGameType(player).isSurvival()) && SOLOnionAPI.isPresent(player);
    }
    
    public static final String MODID = "solonion";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static CreativeNetwork NETWORK = new CreativeNetwork(1, LOGGER, new ResourceLocation(SOLOnion.MODID, "main"));
    public static SOLOnionConfig CONFIG;
    public static SOLOnionEvent EVENT;
    
    public SOLOnion() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SOLOnionClient.load(FMLJavaModLoadingContext.get().getModEventBus()));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerCapabilities);
        SOLOnionItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        SOLOnionItems.MENU_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SOLOnionItems::registerTabs);
        MinecraftForge.EVENT_BUS.addListener(this::command);
    }
    
    public void setup(FMLCommonSetupEvent event) {
        NETWORK.registerType(FoodListMessage.class, FoodListMessage::new);
        
        MinecraftForge.EVENT_BUS.register(EVENT = new SOLOnionEvent());
        CreativeConfigRegistry.ROOT.registerValue(MODID, CONFIG = new SOLOnionConfig());
    }
    
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(FoodCapability.class);
        event.register(BenefitCapability.class);
    }
    
    public void command(RegisterCommandsEvent event) {
        event.getDispatcher().register(literal(FoodListCommand.name).then(FoodListCommand.withPlayerArgumentOrSender(literal("sync"), FoodListCommand::syncFoodList)).then(
            FoodListCommand.withPlayerArgumentOrSender(literal("clear"), FoodListCommand::clearFoodList)).then(FoodListCommand.withPlayerArgumentOrSender(literal("diversity"),
                FoodListCommand::displayDiversity)).then(FoodListCommand.withPlayerArgumentOrSender(literal("resetOrigin"), FoodListCommand::resetPlayerOrigin)).then(
                    FoodListCommand.withNoArgument(literal("resetAllOrigins"), FoodListCommand::resetAllOrigins)));
    }
    
}
