package com.backpackmod.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BackpackItem extends Item {
    private static final String NBT_KEY = "BackpackItems";
    private final BackpackTier tier;

    public BackpackItem(BackpackTier tier, Settings settings) {
        super(tier.isFireproof() ? settings.fireproof() : settings);
        this.tier = tier;
    }

    public BackpackTier getTier() {
        return tier;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        openScreen(world, user, stack);
        return TypedActionResult.success(stack, world.isClient());
    }

    // فتح شاشة الحقيبة (سواء كانت في اليد أو في خانة الظهر)
    public void openScreen(World world, PlayerEntity user, ItemStack stack) {
        if (!world.isClient) {
            world.playSound(
                    null,
                    user.getX(),
                    user.getY(),
                    user.getZ(),
                    SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
                    SoundCategory.PLAYERS,
                    1.0F,
                    1.0F
            );

            user.openHandledScreen(new NamedScreenHandlerFactory() {
                @Override
                public Text getDisplayName() {
                    return stack.getName();
                }

                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                    SimpleInventory inventory = new SimpleInventory(tier.getSlots()) {
                        @Override
                        public boolean isValid(int slot, ItemStack insertStack) {
                            return !(insertStack.getItem() instanceof BackpackItem);
                        }

                        @Override
                        public void markDirty() {
                            super.markDirty();
                            saveInventory(stack, this);
                        }

                        @Override
                        public void onClose(PlayerEntity player) {
                            super.onClose(player);
                            saveInventory(stack, this);
                        }
                    };

                    loadInventory(stack, inventory);

                    return new GenericContainerScreenHandler(tier.getScreenHandlerType(), syncId, playerInventory, inventory, tier.getRows()) {
                        @Override
                        public boolean canUse(PlayerEntity player) {
                            // الشاشة تظل مفتوحة طالما أن الحقيبة في يد اللاعب أو في خانة الظهر المخصصة
                            boolean isHeld = player.getMainHandStack() == stack || player.getOffHandStack() == stack;
                            boolean isWorn = false;
                            if (player instanceof BackpackHolder holder) {
                                isWorn = holder.backpackmod$getBackpackInventory().getStack(0) == stack;
                            }
                            return isHeld || isWorn;
                        }

                        @Override
                        public ItemStack quickMove(PlayerEntity player, int index) {
                            Slot slot = this.slots.get(index);
                            if (slot != null && slot.hasStack()) {
                                ItemStack slotStack = slot.getStack();
                                if (slotStack.getItem() instanceof BackpackItem && index >= tier.getSlots()) {
                                    return ItemStack.EMPTY;
                                }
                            }
                            return super.quickMove(player, index);
                        }
                    };
                }
            });
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient || !(entity instanceof PlayerEntity player)) {
            return;
        }

        if (player.age % 20 != 0) {
            return;
        }

        SimpleInventory inv = getInventory(stack);
        boolean hasMagnet = false;
        boolean hasAutoFeed = false;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack item = inv.getStack(i);
            if (item.getItem() instanceof MagnetUpgradeItem) {
                hasMagnet = true;
            } else if (item.getItem() instanceof AutoFeedUpgradeItem) {
                hasAutoFeed = true;
            }
        }

        if (hasMagnet) {
            Box box = player.getBoundingBox().expand(6.0);
            List<ItemEntity> nearbyItems = world.getEntitiesByClass(
                    ItemEntity.class,
                    box,
                    itemEntity -> itemEntity.isAlive() && !itemEntity.cannotPickup()
            );

            for (ItemEntity itemEntity : nearbyItems) {
                Vec3d target = player.getPos().add(0, 0.75, 0);
                Vec3d motion = target.subtract(itemEntity.getPos()).normalize().multiply(0.28);
                itemEntity.setVelocity(motion);
                itemEntity.velocityModified = true;
            }
        }

        if (hasAutoFeed && player.getHungerManager().getFoodLevel() <= 14) {
            for (int i = 0; i < inv.size(); i++) {
                ItemStack foodStack = inv.getStack(i);
                if (foodStack.isFood()) {
                    FoodComponent food = foodStack.getItem().getFoodComponent();
                    if (food != null) {
                        player.getHungerManager().eat(foodStack.getItem(), foodStack);
                        world.playSound(
                                null,
                                player.getX(),
                                player.getY(),
                                player.getZ(),
                                SoundEvents.ENTITY_GENERIC_EAT,
                                SoundCategory.PLAYERS,
                                0.5F,
                                1.0F
                        );
                        foodStack.decrement(1);
                        inv.setStack(i, foodStack);
                        saveInventory(stack, inv);
                        break;
                    }
                }
            }
        }
    }

    public static SimpleInventory getInventory(ItemStack stack) {
        int slots = 54;
        if (stack.getItem() instanceof BackpackItem backpackItem) {
            slots = backpackItem.getTier().getSlots();
        }
        SimpleInventory inv = new SimpleInventory(slots);
        loadInventory(stack, inv);
        return inv;
    }

    public static void saveInventory(ItemStack stack, Inventory inventory) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtList list = new NbtList();

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack slotStack = inventory.getStack(i);
            if (!slotStack.isEmpty()) {
                NbtCompound itemNbt = new NbtCompound();
                itemNbt.putInt("Slot", i);
                slotStack.writeNbt(itemNbt);
                list.add(itemNbt);
            }
        }

        nbt.put(NBT_KEY, list);
    }

    public static void loadInventory(ItemStack stack, Inventory inventory) {
        if (stack.hasNbt() && stack.getNbt().contains(NBT_KEY, NbtElement.LIST_TYPE)) {
            NbtList list = stack.getNbt().getList(NBT_KEY, NbtElement.COMPOUND_TYPE);

            for (int i = 0; i < list.size(); i++) {
                NbtCompound itemNbt = list.getCompound(i);
                int slot = itemNbt.getInt("Slot");
                if (slot >= 0 && slot < inventory.size()) {
                    inventory.setStack(slot, ItemStack.fromNbt(itemNbt));
                }
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        int filledSlots = 0;
        if (stack.hasNbt() && stack.getNbt().contains(NBT_KEY, NbtElement.LIST_TYPE)) {
            NbtList list = stack.getNbt().getList(NBT_KEY, NbtElement.COMPOUND_TYPE);
            filledSlots = list.size();
        }

        tooltip.add(Text.translatable("item.backpackmod." + tier.getName() + ".tier").formatted(Formatting.GOLD));
        tooltip.add(Text.translatable("item.backpackmod.backpack.slots", filledSlots, tier.getSlots()).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("item.backpackmod.backpack.desc").formatted(Formatting.YELLOW));

        if (tier.isFireproof()) {
            tooltip.add(Text.translatable("item.backpackmod.fireproof.desc").formatted(Formatting.RED));
        }

        super.appendTooltip(stack, world, tooltip, context);
    }
}
