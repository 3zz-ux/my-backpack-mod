package com.backpackmod.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Inject(method = "getMaxItemCount()I", at = @At("HEAD"), cancellable = true)
    private void onGetMaxItemCount(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(5000);
    }

    @Inject(method = "getMaxItemCount(Lnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private void onGetMaxItemCountStack(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (!stack.isEmpty()) {
            cir.setReturnValue(stack.getMaxCount());
        }
    }
}
