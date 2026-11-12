package com.backpackmod.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public class BackpackSlot extends Slot {
    public static final Identifier EMPTY_BACKPACK_SLOT_TEXTURE = new Identifier("backpackmod", "item/empty_backpack_slot");
    private final PlayerEntity player;

    public BackpackSlot(Inventory inventory, int index, int x, int y, PlayerEntity player) {
        super(inventory, index, x, y);
        this.player = player;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return stack.getItem() instanceof BackpackItem;
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }

    @Override
    public Pair<Identifier, Identifier> getBackgroundSprite() {
        return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, EMPTY_BACKPACK_SLOT_TEXTURE);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (player instanceof BackpackHolder holder) {
            holder.backpackmod$syncBackpack(getStack());
        }
    }
}

