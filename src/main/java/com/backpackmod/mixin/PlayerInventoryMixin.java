package com.backpackmod.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Inject(method = "canStackAddMore", at = @At("HEAD"), cancellable = true)
    private void onCanStackAddMore(ItemStack existingStack, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        boolean canAdd = !existingStack.isEmpty()
                && ItemStack.canCombine(existingStack, stack)
                && existingStack.isStackable()
                && existingStack.getCount() < existingStack.getMaxCount();
        cir.setReturnValue(canAdd);
    }
}
