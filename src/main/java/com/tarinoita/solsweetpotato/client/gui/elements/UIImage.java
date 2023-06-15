package com.tarinoita.solsweetpotato.client.gui.elements;

import java.awt.Rectangle;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class UIImage extends UIElement {
    public Image image;
    public float alpha = 1;
    
    public UIImage(Image image) {
        this(new Rectangle(image.partOfTexture.getSize()), image);
    }
    
    public UIImage(Rectangle frame, Image image) {
        super(frame);
        
        this.image = image;
    }
    
    @Override
    protected void render(GuiGraphics graphics) {
        super.render(graphics);
        
        int imageWidth = image.partOfTexture.width;
        int imageHeight = image.partOfTexture.height;
        graphics.blit(image.textureLocation, frame.x + (int) Math.floor((frame.width - imageWidth) / 2d), frame.y + (int) Math
                .floor((frame.height - imageHeight) / 2d), 0, image.partOfTexture.x, image.partOfTexture.y, imageWidth, imageHeight, 256, 256);
    }
    
    public static class Image {
        public final ResourceLocation textureLocation;
        public final Rectangle partOfTexture;
        
        public Image(ResourceLocation textureLocation, Rectangle partOfTexture) {
            this.textureLocation = textureLocation;
            this.partOfTexture = partOfTexture;
        }
    }
}
