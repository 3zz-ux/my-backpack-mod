package com.backpackmod.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PacketByteBuf.class)
public abstract class PacketByteBufMixin {
    @Inject(method = "writeItemStack", at = @At("HEAD"), cancellable = true)
    private void onWriteItemStack(ItemStack stack, CallbackInfoReturnable<PacketByteBuf> cir) {
        PacketByteBuf self = (PacketByteBuf)(Object)this;
        if (stack.isEmpty()) {
            self.writeBoolean(false);
        } else {
            self.writeBoolean(true);
            Item item = stack.getItem();
            self.writeVarInt(Registries.ITEM.getRawId(item));
            self.writeVarInt(stack.getCount());
            self.writeNbt(stack.getNbt());
        }
        cir.setReturnValue(self);
    }

    @Inject(method = "readItemStack", at = @At("HEAD"), cancellable = true)
    private void onReadItemStack(CallbackInfoReturnable<ItemStack> cir) {
        PacketByteBuf self = (PacketByteBuf)(Object)this;
        if (!self.readBoolean()) {
            cir.setReturnValue(ItemStack.EMPTY);
        } else {
            int rawId = self.readVarInt();
            Item item = Registries.ITEM.get(rawId);
            int count = self.readVarInt();
            ItemStack stack = new ItemStack(item == null ? Items.AIR : item, count);
            stack.setNbt(self.readNbt());
            cir.setReturnValue(stack);
        }
    }
}
