package com.tarinoita.solsweetpotato;

import static net.minecraft.commands.Commands.literal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tarinoita.solsweetpotato.client.SOLSweetPotatoClient;
import com.tarinoita.solsweetpotato.command.FoodListCommand;
import com.tarinoita.solsweetpotato.item.SOLSweetPotatoItems;
import com.tarinoita.solsweetpotato.network.ConfigMessage;
import com.tarinoita.solsweetpotato.network.FoodListMessage;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import team.creative.creativecore.common.network.CreativeNetwork;

@Mod(SOLSweetPotato.MODID)
public final class SOLSweetPotato {
    
    public static final String MODID = "solsweetpotato";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static CreativeNetwork NETWORK = new CreativeNetwork("1.0", LOGGER, new ResourceLocation(SOLSweetPotato.MODID, "main"));
    
    public SOLSweetPotato() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SOLSweetPotatoClient.load(FMLJavaModLoadingContext.get().getModEventBus()));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setUp);
        SOLSweetPotatoConfig.setUp();
        SOLSweetPotatoItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SOLSweetPotatoItems::registerTabs);
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
