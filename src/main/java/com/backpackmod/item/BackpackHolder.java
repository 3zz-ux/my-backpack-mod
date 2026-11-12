package com.backpackmod.item;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

public interface BackpackHolder {
    SimpleInventory backpackmod$getBackpackInventory();
    void backpackmod$syncBackpack(ItemStack stack);
}

