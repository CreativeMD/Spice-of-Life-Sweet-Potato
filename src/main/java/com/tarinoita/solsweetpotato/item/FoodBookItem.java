package com.tarinoita.solsweetpotato.item;

import com.tarinoita.solsweetpotato.client.gui.FoodBookScreen;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class FoodBookItem extends Item {
    public FoodBookItem() {
        super(new Properties());
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (player.isLocalPlayer())
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FoodBookScreen.open(player));
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }
}
