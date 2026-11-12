package com.backpackmod.mixin;

import com.backpackmod.item.BackpackHolder;
import com.backpackmod.item.BackpackItem;
import com.backpackmod.item.BackpackSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin extends AbstractRecipeScreenHandler<PlayerInventory> {
    public PlayerScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/player/PlayerInventory;ZLnet/minecraft/entity/player/PlayerEntity;)V", at = @At("RETURN"))
    private void addBackpackSlot(PlayerInventory inventory, boolean onServer, PlayerEntity owner, CallbackInfo ci) {
        if (owner instanceof BackpackHolder holder) {
            // إضافة خانة الحقيبة تحت خانة اليد الثانية (Offhand) مباشرة (x = 77, y = 80)
            this.addSlot(new BackpackSlot(holder.backpackmod$getBackpackInventory(), 0, 77, 80, owner));
        }
    }

    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void onBackpackSlotRightClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (slotIndex >= 0 && slotIndex < this.slots.size()) {
            Slot slot = this.slots.get(slotIndex);
            if (slot instanceof BackpackSlot) {
                // button == 1 هو كليك يمين! وإذا كانت اليد فارغة، افتح الحقيبة مباشرة
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

