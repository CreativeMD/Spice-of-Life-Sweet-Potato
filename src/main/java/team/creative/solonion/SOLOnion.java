package team.creative.solonion;

import static net.minecraft.commands.Commands.literal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.solonion.client.SOLOnionClient;
import team.creative.solonion.command.FoodListCommand;
import team.creative.solonion.item.SOLOnionItems;
import team.creative.solonion.network.ConfigMessage;
import team.creative.solonion.network.FoodListMessage;

@Mod(SOLOnion.MODID)
public final class SOLOnion {
    
    public static final String MODID = "solonion";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static CreativeNetwork NETWORK = new CreativeNetwork("1.0", LOGGER, new ResourceLocation(SOLOnion.MODID, "main"));
    
    public SOLOnion() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SOLOnionClient.load(FMLJavaModLoadingContext.get().getModEventBus()));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setUp);
        SOLOnionConfig.setUp();
        SOLOnionItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SOLOnionItems::registerTabs);
        MinecraftForge.EVENT_BUS.addListener(this::command);
    }
    
    public void setUp(FMLCommonSetupEvent event) {
        NETWORK.registerType(FoodListMessage.class, FoodListMessage::new);
        NETWORK.registerType(ConfigMessage.class, ConfigMessage::new);
    }
    
    public void command(RegisterCommandsEvent event) {
        event.getDispatcher()
                .register(literal(FoodListCommand.name).then(FoodListCommand.withPlayerArgumentOrSender(literal("sync"), FoodListCommand::syncFoodList))
                        .then(FoodListCommand.withPlayerArgumentOrSender(literal("clear"), FoodListCommand::clearFoodList))
                        .then(FoodListCommand.withPlayerArgumentOrSender(literal("diversity"), FoodListCommand::displayDiversity))
                        .then(FoodListCommand.withPlayerArgumentOrSender(literal("resetOrigin"), FoodListCommand::resetPlayerOrigin))
                        .then(FoodListCommand.withNoArgument(literal("resetAllOrigins"), FoodListCommand::resetAllOrigins)));
    }
    
}
