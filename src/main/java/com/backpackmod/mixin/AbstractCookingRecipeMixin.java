package com.backpackmod.mixin;

import net.minecraft.recipe.AbstractCookingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractCookingRecipe.class)
public abstract class AbstractCookingRecipeMixin {
    // تسريع وقت طبخ وصهر جميع الأشياء في الفرن إلى 5 تيك فقط (ربع ثانية لكل حبة!)
    @Inject(method = "getCookTime", at = @At("RETURN"), cancellable = true)
    private void makeFurnaceSuperFast(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(5);
    }
}
