package com.backpackmod.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BackpackItem extends Item {
    // حجم التخزين: 54 خانة مثل الصندوق الكبير (Double Chest)
    // إذا كنت تفضل صندوق صغير عادي، غير الرقم إلى 27 ونوع الشاشة إلى GENERIC_9X3 مع 3 صفوف
    public static final int INVENTORY_SIZE = 54;
    private static final String NBT_KEY = "BackpackItems";

    public BackpackItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // فتح الشاشة على السيرفر فقط (Server-side)
        if (!world.isClient) {
            // تشغيل صوت لطيف كأنك تفتح حقيبة جلدية
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
                    SimpleInventory inventory = new SimpleInventory(INVENTORY_SIZE) {
                        @Override
                        public boolean isValid(int slot, ItemStack insertStack) {
                            // منع وضع حقيبة داخل حقيبة أخرى لتجنب المشاكل والتكرار (Dupes)
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

                    // تحميل الأغراض المخزنة سابقاً في الحقيبة
                    loadInventory(stack, inventory);

                    // استخدام حاوية الـ 54 خانة المدمجة في ماينكرافت (Double Chest)
                    return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, inventory, 6) {
                        @Override
                        public boolean canUse(PlayerEntity player) {
                            // إغلاق الشاشة تلقائياً إذا رمى اللاعب الحقيبة أو غيّر مكانها من يده
                            return player.getMainHandStack() == stack || player.getOffHandStack() == stack;
                        }

                        @Override
                        public ItemStack quickMove(PlayerEntity player, int index) {
                            Slot slot = this.slots.get(index);
                            if (slot != null && slot.hasStack()) {
                                ItemStack slotStack = slot.getStack();
                                // منع النقل السريع (Shift+Click) للحقيبة داخل نفسها
                                if (slotStack.getItem() instanceof BackpackItem && index >= INVENTORY_SIZE) {
                                    return ItemStack.EMPTY;
                                }
                            }
                            return super.quickMove(player, index);
                        }
                    };
                }
            });
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    // حفظ الأغراض داخل الـ NBT للـ ItemStack
    private static void saveInventory(ItemStack stack, Inventory inventory) {
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

    // استرجاع الأغراض من الـ NBT للـ ItemStack
    private static void loadInventory(ItemStack stack, Inventory inventory) {
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

        tooltip.add(Text.translatable("item.backpackmod.backpack.slots", filledSlots, INVENTORY_SIZE).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("item.backpackmod.backpack.desc").formatted(Formatting.GOLD));
        super.appendTooltip(stack, world, tooltip, context);
    }
}

