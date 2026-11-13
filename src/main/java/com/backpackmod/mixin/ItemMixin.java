package com.backpackmod.mixin;

import com.backpackmod.item.BackpackItem;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Shadow
    public abstract boolean isDamageable();

    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private void modifyMaxCount(CallbackInfoReturnable<Integer> cir) {
        Item self = (Item)(Object)this;
        // زيادة حجم الستاك إلى 5000 لجميع الموارد والأدوات القابلة للتكديس مع استثناء الحقائب والأدوات ذات المتانة
        if (!this.isDamageable() && !(self instanceof BackpackItem)) {
            cir.setReturnValue(5000);
        }
    }
}
