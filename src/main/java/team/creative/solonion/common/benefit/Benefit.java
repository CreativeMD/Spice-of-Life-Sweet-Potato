package team.creative.solonion.common.benefit;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.Side;
import team.creative.creativecore.common.config.converation.ConfigTypeConveration;
import team.creative.creativecore.common.config.gui.IGuiConfigParent;
import team.creative.creativecore.common.config.holder.ConfigKey.ConfigKeyField;
import team.creative.creativecore.common.config.premade.RegistryObjectConfig;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.event.GuiEvent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.text.TextListBuilder;
import team.creative.creativecore.common.util.text.TextMapBuilder;

public class Benefit<T> {
    
    static {
        ConfigTypeConveration.registerTypeCreator(Benefit.class, () -> createAttribute(Attributes.MAX_HEALTH, 2));
        
        ConfigTypeConveration.registerType(Benefit.class, new ConfigTypeConveration<Benefit>() {
            
            @Override
            public Benefit readElement(Provider provider, Benefit defaultValue, boolean loadDefault, boolean ignoreRestart, JsonElement element, Side side, @Nullable ConfigKeyField key) {
                if (element.isJsonObject()) {
                    JsonObject object = element.getAsJsonObject();
                    if (object.has("attribute"))
                        return createAttribute(new ResourceLocation(object.get("attribute").getAsString()), object.get("value").getAsDouble());
                    return createMobEffect(new ResourceLocation(object.get("effect").getAsString()), object.get("value").getAsDouble());
                }
                return defaultValue;
            }
            
            @Override
            public JsonElement writeElement(Provider provider, Benefit value, Benefit defaultValue, boolean saveDefault, boolean ignoreRestart, Side side, @Nullable ConfigKeyField key) {
                JsonObject object = new JsonObject();
                if (value.property.registry == BuiltInRegistries.ATTRIBUTE)
                    object.addProperty("attribute", value.property.location.toString());
                else
                    object.addProperty("effect", value.property.location.toString());
                object.addProperty("value", value.value);
                return object;
            }
            
            @Override
            @OnlyIn(Dist.CLIENT)
            @Environment(EnvType.CLIENT)
            public void createControls(GuiParent parent, IGuiConfigParent configParent, ConfigKeyField key, Class clazz) {
                parent.flow = GuiFlow.STACK_Y;
                parent.add(new GuiStateButton("state", 0, new TextListBuilder().addTranslated("config.solonion.", "attribute", "effect")) {
                    
                    @Override
                    public void raiseEvent(GuiEvent event) {
                        super.raiseEvent(event);
                        GuiComboBoxMapped<ResourceLocation> box = (GuiComboBoxMapped<ResourceLocation>) parent.get("elements");
                        Registry registry = getState() == 0 ? BuiltInRegistries.ATTRIBUTE : BuiltInRegistries.MOB_EFFECT;
                        box.setLines(new TextMapBuilder<ResourceLocation>().addComponent(registry.keySet(), value -> {
                            if (value.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE))
                                return Component.literal(value.getPath());
                            return Component.literal(value.toString());
                        }));
                    }
                    
                });
                parent.add(new GuiComboBoxMapped<ResourceLocation>("elements", new TextMapBuilder<ResourceLocation>()).setSearchbar(true));
                parent.add(new GuiTextfield("value").setFloatOnly().setDim(20, 6));
            }
            
            @Override
            @OnlyIn(Dist.CLIENT)
            @Environment(EnvType.CLIENT)
            public void loadValue(Benefit value, GuiParent parent, IGuiConfigParent configParent, ConfigKeyField key) {
                GuiStateButton state = parent.get("state");
                state.setState(value.property.registry == BuiltInRegistries.ATTRIBUTE ? 0 : 1);
                state.raiseEvent(new GuiControlChangedEvent(state));
                
                GuiComboBoxMapped<ResourceLocation> box = (GuiComboBoxMapped<ResourceLocation>) parent.get("elements");
                box.select(value.property.location);
                
                GuiTextfield text = parent.get("value");
                text.setText(value.value + "");
            }
            
            @Override
            @OnlyIn(Dist.CLIENT)
            @Environment(EnvType.CLIENT)
            protected Benefit saveValue(GuiParent parent, IGuiConfigParent configParent, Class clazz, ConfigKeyField key) {
                GuiStateButton state = parent.get("state");
                GuiComboBoxMapped<ResourceLocation> box = (GuiComboBoxMapped<ResourceLocation>) parent.get("elements");
                GuiTextfield text = parent.get("value");
                double value = text.parseDouble();
                
                if (state.getState() == 0)
                    return createAttribute(box.getSelected(), value);
                return createMobEffect(box.getSelected(), value);
            }
            
            @Override
            public Benefit set(ConfigKeyField key, Benefit value) {
                return value;
            }
            
        });
    }
    
    public static Benefit<Attribute> createAttribute(Holder<Attribute> attribute, double value) {
        return new Benefit<>(new RegistryObjectConfig<>(BuiltInRegistries.ATTRIBUTE, attribute.unwrapKey().get().location()), value);
    }
    
    public static Benefit<Attribute> createAttribute(ResourceLocation location, double value) {
        return new Benefit<>(new RegistryObjectConfig<>(BuiltInRegistries.ATTRIBUTE, location), value);
    }
    
    public static Benefit<MobEffect> createMobEffect(Holder<MobEffect> mob, double value) {
        return new Benefit<>(new RegistryObjectConfig<>(BuiltInRegistries.MOB_EFFECT, mob.unwrapKey().get().location()), value);
    }
    
    public static Benefit<MobEffect> createMobEffect(ResourceLocation location, double value) {
        return new Benefit<>(new RegistryObjectConfig<>(BuiltInRegistries.MOB_EFFECT, location), value);
    }
    
    public final RegistryObjectConfig<T> property;
    
    public double value;
    
    public Benefit(RegistryObjectConfig<T> property, double value) {
        this.property = property;
        this.value = value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Benefit benefit)
            return property.equals(benefit.property) && value == benefit.value;
        return false;
    }
    
}
