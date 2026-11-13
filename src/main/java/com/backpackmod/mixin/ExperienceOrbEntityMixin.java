package com.backpackmod.mixin;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin {
    @Shadow
    private int amount;

    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDI)V", at = @At("RETURN"))
    private void onInit(World world, double x, double y, double z, int amount, CallbackInfo ci) {
        // مضاعفة نسبة الـ XP الناتجة عن قتل الموبات وتكسير البلوكات والأورات إلى 6 أضعاف (مضاعفة إضافية)
        this.amount = amount * 6;
    }
}
