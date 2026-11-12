package com.backpackmod.mixin;

import com.backpackmod.item.BackpackHolder;
import com.backpackmod.item.BackpackItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements BackpackHolder {
    @Unique
    private static final TrackedData<ItemStack> BACKPACK_TRACKED = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);

    @Unique
    private final SimpleInventory backpackmod_inventory = new SimpleInventory(1) {
        @Override
        public void markDirty() {
            super.markDirty();
            backpackmod$syncBackpack(getStack(0));
        }
    };

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("RETURN"))
    private void initBackpackTracker(CallbackInfo ci) {
        this.dataTracker.startTracking(BACKPACK_TRACKED, ItemStack.EMPTY);
    }

    @Override
    public SimpleInventory backpackmod$getBackpackInventory() {
        return this.backpackmod_inventory;
    }

    @Override
    public void backpackmod$syncBackpack(ItemStack stack) {
        this.dataTracker.set(BACKPACK_TRACKED, stack.copy());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickEquippedBackpack(CallbackInfo ci) {
        ItemStack stack = this.backpackmod_inventory.getStack(0);
        if (!stack.isEmpty() && stack.getItem() instanceof BackpackItem) {
            stack.inventoryTick(this.getWorld(), this, -1, false);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void saveBackpackNbt(NbtCompound nbt, CallbackInfo ci) {
        ItemStack stack = this.backpackmod_inventory.getStack(0);
        if (!stack.isEmpty()) {
            nbt.put("BackpackMod_Equipped", stack.writeNbt(new NbtCompound()));
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void loadBackpackNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("BackpackMod_Equipped", NbtElement.COMPOUND_TYPE)) {
            ItemStack stack = ItemStack.fromNbt(nbt.getCompound("BackpackMod_Equipped"));
            this.backpackmod_inventory.setStack(0, stack);
            backpackmod$syncBackpack(stack);
        }
    }

    @Inject(method = "dropInventory", at = @At("TAIL"))
    private void dropBackpackOnDeath(CallbackInfo ci) {
        if (!this.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            ItemStack stack = this.backpackmod_inventory.getStack(0);
            if (!stack.isEmpty()) {
                this.dropItem(stack, true, false);
                this.backpackmod_inventory.setStack(0, ItemStack.EMPTY);
                backpackmod$syncBackpack(ItemStack.EMPTY);
            }
        }
    }
}

