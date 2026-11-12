package com.backpackmod.mixin;

import com.backpackmod.item.BackpackItem;
import com.backpackmod.item.BackpackSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    @Shadow
    public abstract ItemStack getCursorStack();

    @Shadow
    @Final
    public DefaultedList<Slot> slots;

    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void onBackpackSlotRightClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (slotIndex >= 0 && slotIndex < this.slots.size()) {
            Slot slot = this.slots.get(slotIndex);
            if (slot instanceof BackpackSlot) {
                // عند الضغط كليك يمين واليد فارغة، تفتح الحقيبة مباشرة
                if (button == 1 && actionType == SlotActionType.PICKUP && this.getCursorStack().isEmpty()) {
                    if (slot.hasStack()) {
                        ItemStack stack = slot.getStack();
                        if (stack.getItem() instanceof BackpackItem backpackItem) {
                            backpackItem.openScreen(player.getWorld(), player, stack);
                            ci.cancel();
                        }
                    }
                }
            }
        }
    }
}

