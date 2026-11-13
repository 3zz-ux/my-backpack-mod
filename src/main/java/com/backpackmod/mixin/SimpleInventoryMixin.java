package com.backpackmod.mixin;

import net.minecraft.inventory.SimpleInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleInventory.class)
public abstract class SimpleInventoryMixin {
    @Inject(method = "getMaxCountPerStack", at = @At("HEAD"), cancellable = true)
    private void onGetMaxCountPerStack(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(5000);
    }
}
