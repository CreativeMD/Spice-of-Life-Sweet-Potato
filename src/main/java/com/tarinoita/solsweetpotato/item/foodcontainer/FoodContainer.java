/**
 * Much of the following code was adapted from Cyclic's storage bag code.
 * Copyright for portions of the code are held by Samson Basset (Lothrazar)
 * as part of Cyclic, under the MIT license.
 */
package com.tarinoita.solsweetpotato.item.foodcontainer;

import com.tarinoita.solsweetpotato.client.ContainerScreenRegistry;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class FoodContainer extends AbstractContainerMenu {
    public static final int PLAYERSIZE = 4 * 9;
    
    public ItemStack containerItem;
    public int nslots;
    
    private Inventory playerInventory;
    
    public FoodContainer(int id, Inventory playerInventory, Player player) {
        super(ContainerScreenRegistry.FOOD_CONTAINER.get(), id);
        
        // When we hit the hotkey to open a food container, check held items first
        if (player.getMainHandItem().getItem() instanceof FoodContainerItem) {
            containerItem = player.getMainHandItem();
        } else if (player.getOffhandItem().getItem() instanceof FoodContainerItem) {
            containerItem = player.getOffhandItem();
        } else {
            for (ItemStack stack : playerInventory.items) {
                if (stack.getItem() instanceof FoodContainerItem) {
                    containerItem = stack;
                    break;
                }
            }
        }
        
        this.playerInventory = playerInventory;
        containerItem.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
            nslots = h.getSlots();
            int slotsPerRow = h.getSlots();
            if (h.getSlots() > 9) {
                slotsPerRow = h.getSlots() / 2;
            }
            int xStart = (2 * 8 + 9 * 18 - slotsPerRow * 18) / 2;
            int yStart = 17 + 18;
            if (h.getSlots() > 9) {
                yStart = 17 + (84 - 36 - 23) / 2;
            }
            for (int j = 0; j < h.getSlots(); j++) {
                int row = j / slotsPerRow;
                int col = j % slotsPerRow;
                int xPos = xStart + col * 18;
                int yPos = yStart + row * 18;
                this.addSlot(new FoodSlot(h, j, xPos, yPos));
            }
        });
        
        layoutPlayerInventorySlots(8, 84);
    }
    
    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (!(slotId < 0 || slotId >= this.slots.size())) {
            ItemStack clickedStack = this.slots.get(slotId).getItem();
            if (clickedStack.getItem() instanceof FoodContainerItem) {
                //lock the bag in place by quitting early
                return;
            }
        }
        
        super.clicked(slotId, dragType, clickTypeIn, player);
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        Slot clickedSlot = slots.get(slotId);
        if (clickedSlot == null || slotId < 0 || !clickedSlot.hasItem()) {
            return ItemStack.EMPTY;
        }
        
        ItemStack clickedStack = clickedSlot.getItem();
        if (!FoodSlot.canHold(clickedStack)) {
            return ItemStack.EMPTY;
        }
        
        final ItemStack unchangedCopy = clickedStack.copy();
        if (slotId < nslots) {
            // Item is in the FoodContainer, move it to inventory
            if (!moveItemStackTo(clickedStack, nslots, nslots + PLAYERSIZE, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Item is in the inventory, move it to the FoodContainer
            if (!moveItemStackTo(clickedStack, 0, nslots, false)) {
                return ItemStack.EMPTY;
            }
        }
        
        if (clickedStack.isEmpty()) {
            clickedSlot.set(ItemStack.EMPTY);
        } else {
            clickedSlot.setChanged();
        }
        
        if (clickedStack.getCount() == unchangedCopy.getCount()) {
            return ItemStack.EMPTY;
        }
        
        clickedSlot.onTake(player, clickedStack);
        return clickedStack;
    }
    
    private int addSlotRange(Inventory handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new Slot(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }
    
    private int addSlotBox(Inventory handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }
    
    protected void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);
        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }
    
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
