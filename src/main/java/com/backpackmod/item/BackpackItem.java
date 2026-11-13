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
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
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

    // فتح شاشة الحقيبة (سواء كانت في اليد، أو في خانة الظهر، أو في الإنفتوري)
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
                            // الشاشة تظل مفتوحة طالما أن الحقيبة في يد اللاعب أو في خانة الظهر المخصصة أو في الإنفتوري
                            boolean isHeld = player.getMainHandStack() == stack || player.getOffHandStack() == stack;
                            boolean isWorn = false;
                            if (player instanceof BackpackHolder holder) {
                                isWorn = holder.backpackmod$getBackpackInventory().getStack(0) == stack;
                            }
                            boolean isInInventory = player.getInventory().contains(stack);
                            return isHeld || isWorn || isInInventory;
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

        // فحص كل تيكين (2 ticks) لسرعة استجابة المغناطيس وسحب الموارد فوراً إلى داخل الحقيبة
        if (player.age % 2 != 0) {
            return;
        }

        // إذا كان اللاعب فاتح شاشة الحقيبة حالياً، ننتظر حتى يقفلها لمنع تداخل ترتيب الموارد
        if (player.currentScreenHandler instanceof GenericContainerScreenHandler) {
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

        // 1. ميزة المغناطيس: سحب الموارد مباشرة إلى داخل خانات الحقيبة وحفظها فيها دائماً
        if (hasMagnet) {
            Box box = player.getBoundingBox().expand(8.0);
            List<ItemEntity> nearbyItems = world.getEntitiesByClass(
                    ItemEntity.class,
                    box,
                    itemEntity -> itemEntity.isAlive() && !itemEntity.cannotPickup()
            );

            boolean inventoryChanged = false;

            for (ItemEntity itemEntity : nearbyItems) {
                ItemStack groundStack = itemEntity.getStack();
                if (groundStack.isEmpty() || groundStack.getItem() instanceof BackpackItem) {
                    continue;
                }

                int originalCount = groundStack.getCount();
                ItemStack remaining = insertIntoInventory(inv, groundStack);

                // تم تخزين المورد داخل الحقيبة بنجاح
                if (remaining.getCount() < originalCount) {
                    inventoryChanged = true;
                    if (remaining.isEmpty()) {
                        itemEntity.discard();
                    } else {
                        itemEntity.setStack(remaining);
                    }
                    world.playSound(
                            null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            SoundEvents.ENTITY_ITEM_PICKUP,
                            SoundCategory.PLAYERS,
                            0.2F,
                            (world.random.nextFloat() - world.random.nextFloat()) * 0.2F + 1.0F
                    );
                }
            }

            if (inventoryChanged) {
                saveInventory(stack, inv);
                if (player instanceof BackpackHolder holder) {
                    if (holder.backpackmod$getBackpackInventory().getStack(0) == stack) {
                        holder.backpackmod$syncBackpack(stack);
                        holder.backpackmod$getBackpackInventory().markDirty();
                    }
                }
                player.getInventory().markDirty();
            }
        }

        // 2. ميزة التغذية الذاتية: إطعام اللاعب تلقائياً من الأكل الموجود داخل الحقيبة
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

    // دالة مساعدة لإدخال الأغراض مباشرة إلى داخل الحقيبة بذكاء (دمج بالخانات الممتلئة أولاً ثم الفارغة)
    private static ItemStack insertIntoInventory(SimpleInventory inv, ItemStack stack) {
        ItemStack toInsert = stack.copy();

        // أولاً: محاولة الدمج مع الخانات التي تحتوي على نفس نوع المورد
        for (int i = 0; i < inv.size(); i++) {
            ItemStack slotStack = inv.getStack(i);
            if (!slotStack.isEmpty() && ItemStack.canCombine(slotStack, toInsert)) {
                int maxCount = Math.min(inv.getMaxCountPerStack(), slotStack.getMaxCount());
                int space = maxCount - slotStack.getCount();
                if (space > 0) {
                    int toAdd = Math.min(space, toInsert.getCount());
                    slotStack.increment(toAdd);
                    toInsert.decrement(toAdd);
                    inv.setStack(i, slotStack);
                    if (toInsert.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        // ثانياً: إذا تبقى موارد، وضعها في أول خانة فارغة متاحة
        for (int i = 0; i < inv.size(); i++) {
            ItemStack slotStack = inv.getStack(i);
            if (slotStack.isEmpty()) {
                int maxCount = Math.min(inv.getMaxCountPerStack(), toInsert.getMaxCount());
                int toAdd = Math.min(maxCount, toInsert.getCount());
                ItemStack newSlotStack = toInsert.copy();
                newSlotStack.setCount(toAdd);
                inv.setStack(i, newSlotStack);
                toInsert.decrement(toAdd);
                if (toInsert.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        return toInsert;
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
