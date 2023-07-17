package team.creative.solonion.food;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap.Entry;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import team.creative.creativecore.common.util.type.itr.ArrayOffsetIterator;
import team.creative.creativecore.common.util.type.itr.FilterIterator;
import team.creative.solonion.SOLOnion;
import team.creative.solonion.api.FoodCapability;
import team.creative.solonion.api.SOLOnionAPI;

public final class FoodCapabilityImpl implements FoodCapability {
    
    private static double calculateDiversity(Iterable<ItemStack> stacks) {
        Object2DoubleArrayMap<Item> types = new Object2DoubleArrayMap<>();
        int i = 0;
        for (ItemStack stack : stacks) {
            double d = SOLOnion.CONFIG.getDiversity(stack) * (1 - (i / (SOLOnion.CONFIG.trackCount + 1)));
            types.computeDouble(stack.getItem(), (x, y) -> {
                if (y == null)
                    return d;
                return Math.max(y, d);
            });
            i++;
        }
        double d = 0;
        for (Entry<Item> entry : types.object2DoubleEntrySet())
            d += entry.getDoubleValue();
        return d;
    }
    
    private ItemStack[] lastEaten = new ItemStack[SOLOnion.CONFIG.trackCount];
    private int startIndex = lastEaten.length - 1;
    private double diversityCache;
    
    private final LazyOptional<FoodCapabilityImpl> capabilityOptional = LazyOptional.of(() -> this);
    
    public FoodCapabilityImpl() {}
    
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        return capability == SOLOnionAPI.FOOD_CAP ? capabilityOptional.cast() : LazyOptional.empty();
    }
    
    private void updateDiversity() {
        diversityCache = calculateDiversity(this);
    }
    
    /** used for persistent storage */
    @Override
    public ListTag serializeNBT() {
        ListTag list = new ListTag();
        for (ItemStack stack : this)
            list.add(stack.save(new CompoundTag()));
        
        return list;
    }
    
    /** used for persistent storage */
    @Override
    public void deserializeNBT(ListTag tag) {
        if (tag == null)
            return;
        for (int i = 0; i < lastEaten.length; i++)
            lastEaten[i] = i < tag.size() ? ItemStack.of(tag.getCompound(i)) : null;
        startIndex = 0;
        
        updateDiversity();
    }
    
    @Override
    public void eat(ItemStack stack) {
        if (!SOLOnion.CONFIG.isAllowed(stack) && !SOLOnion.CONFIG.shouldExcludedCount)
            return;
        
        startIndex--;
        if (startIndex < 0)
            startIndex = lastEaten.length - 1;
        
        lastEaten[startIndex] = stack.copy();
        updateDiversity();
    }
    
    @Override
    public double simulateEat(ItemStack stack) {
        if (!SOLOnion.CONFIG.isAllowed(stack) && !SOLOnion.CONFIG.shouldExcludedCount)
            return 0.0;
        
        List<ItemStack> stacks = new ArrayList<>(SOLOnion.CONFIG.trackCount);
        for (ItemStack toAdd : stacks)
            stacks.add(toAdd);
        
        if (stacks.size() == SOLOnion.CONFIG.trackCount)
            stacks.remove(stacks.size() - 1);
        stacks.add(0, stack);
        return calculateDiversity(stacks) - diversityCache;
    }
    
    @Override
    public double foodDiversity() {
        return diversityCache;
    }
    
    @Override
    public int trackCount() {
        int count = 0;
        for (int i = 0; i < lastEaten.length; i++)
            if (lastEaten[i] != null)
                count++;
        return count;
    }
    
    @Override
    public int getLastEaten(ItemStack food) {
        if (!food.isEdible())
            return -1;
        
        double d = SOLOnion.CONFIG.getDiversity(food);
        int i = 0;
        for (ItemStack stack : this) {
            if (stack.getItem() == food.getItem() && SOLOnion.CONFIG.getDiversity(stack) == d)
                return i;
            i++;
        }
        return -1;
    }
    
    @Override
    public boolean hasEaten(ItemStack food) {
        if (!food.isEdible())
            return false;
        double d = SOLOnion.CONFIG.getDiversity(food);
        for (ItemStack stack : this)
            if (stack.getItem() == food.getItem() && SOLOnion.CONFIG.getDiversity(stack) == d)
                return true;
        return false;
    }
    
    @Override
    public void clearAll() {
        Arrays.fill(lastEaten, null);
        startIndex = lastEaten.length - 1;
        diversityCache = 0;
    }
    
    @Override
    public Iterator<ItemStack> iterator() {
        return new FilterIterator<>(new ArrayOffsetIterator<>(startIndex, lastEaten), x -> x != null);
    }
    
    @Override
    public void configChanged() {
        if (lastEaten.length != SOLOnion.CONFIG.trackCount) {
            ItemStack[] newLastEaten = new ItemStack[SOLOnion.CONFIG.trackCount];
            int i = 0;
            for (ItemStack stack : this) {
                newLastEaten[i] = stack;
                i++;
                if (i >= newLastEaten.length)
                    break;
            }
            startIndex = 0;
        }
        updateDiversity();
    }
}
