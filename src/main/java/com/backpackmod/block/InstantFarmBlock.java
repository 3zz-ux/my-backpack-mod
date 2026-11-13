package com.backpackmod.block;

import net.minecraft.block.*;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InstantFarmBlock extends Block {
    public enum FarmType {
        CROPS,
        ANIMALS,
        MOB_TRAP
    }

    private final FarmType type;

    public InstantFarmBlock(FarmType type, Settings settings) {
        super(settings);
        this.type = type;
    }

    public FarmType getType() {
        return type;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        if (!world.isClient) {
            switch (type) {
                case CROPS -> buildCropFarm(world, pos);
                case ANIMALS -> buildAnimalFarm(world, pos);
                case MOB_TRAP -> buildMobTrap(world, pos);
            }
        }
    }

    // 1. مزرعة محاصيل مدرجة أوتوماتيكية (مقتبسة طبق الأصل من الصورتين 5 و 6)
    // 3 مدرجات قمح وجزر وبطاطس، مجرى ماء علوي للغسيل والحصاد، مسار ريدستون مع رافعة، وهوبرات بصندوق تجميع بالأسفل
    private void buildCropFarm(World world, BlockPos center) {
        // تفريغ المساحة 9 عرض × 14 طول × 8 ارتفاع
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = 0; dz <= 14; dz++) {
                for (int dy = 0; dy <= 6; dy++) {
                    world.setBlockState(center.add(dx, dy, dz), Blocks.AIR.getDefaultState());
                }
            }
        }

        // جدران الحواف الخشبية
        for (int dz = 0; dz <= 14; dz++) {
            world.setBlockState(center.add(-4, 0, dz), Blocks.STRIPPED_OAK_WOOD.getDefaultState());
            world.setBlockState(center.add(4, 0, dz), Blocks.STRIPPED_OAK_WOOD.getDefaultState());
            world.setBlockState(center.add(-4, 1, dz), Blocks.OAK_PLANKS.getDefaultState());
            world.setBlockState(center.add(4, 1, dz), Blocks.OAK_PLANKS.getDefaultState());
        }

        // المدرج الأول (العلوي Z=1 إلى 4، ارتفاع 3) - قمح
        for (int dz = 1; dz <= 4; dz++) {
            for (int dx = -3; dx <= 3; dx++) {
                world.setBlockState(center.add(dx, 2, dz), Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, 7));
                world.setBlockState(center.add(dx, 3, dz), Blocks.WHEAT.getDefaultState().with(CropBlock.AGE, 7));
            }
        }

        // المدرج الثاني (الأوسط Z=5 إلى 8، ارتفاع 2) - جزر
        for (int dz = 5; dz <= 8; dz++) {
            for (int dx = -3; dx <= 3; dx++) {
                world.setBlockState(center.add(dx, 1, dz), Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, 7));
                world.setBlockState(center.add(dx, 2, dz), Blocks.CARROTS.getDefaultState().with(CropBlock.AGE, 7));
            }
        }

        // المدرج الثالث (السفلي Z=9 إلى 12، ارتفاع 1) - بطاطس
        for (int dz = 9; dz <= 12; dz++) {
            for (int dx = -3; dx <= 3; dx++) {
                world.setBlockState(center.add(dx, 0, dz), Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, 7));
                world.setBlockState(center.add(dx, 1, dz), Blocks.POTATOES.getDefaultState().with(CropBlock.AGE, 7));
            }
        }

        // خزان الماء العلوي للحصاد الفوري (Z=0)
        for (int dx = -3; dx <= 3; dx++) {
            world.setBlockState(center.add(dx, 3, 0), Blocks.WATER.getDefaultState());
            world.setBlockState(center.add(dx, 2, 0), Blocks.STONE_BRICKS.getDefaultState());
        }
        world.setBlockState(center.add(4, 2, 0), Blocks.LEVER.getDefaultState());

        // قناة التجميع وهوبرات الصندوق في الأسفل (Z=13)
        for (int dx = -3; dx <= 1; dx++) {
            world.setBlockState(center.add(dx, 0, 13), Blocks.WATER.getDefaultState());
        }
        world.setBlockState(center.add(2, 0, 13), Blocks.HOPPER.getDefaultState());
        world.setBlockState(center.add(3, 0, 13), Blocks.CHEST.getDefaultState());
        world.setBlockState(center.add(3, 0, 14), Blocks.CHEST.getDefaultState());

        world.playSound(null, center, SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    // 2. حظيرة حيوانات متطورة 9x9 مع أحواض وقش وأعلاف وأبقار وخراف وخنازير
    private void buildAnimalFarm(World world, BlockPos center) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                world.setBlockState(center.add(dx, -1, dz), Blocks.GRASS_BLOCK.getDefaultState());
                for (int dy = 0; dy <= 4; dy++) {
                    world.setBlockState(center.add(dx, dy, dz), Blocks.AIR.getDefaultState());
                }

                if (Math.abs(dx) == 4 || Math.abs(dz) == 4) {
                    world.setBlockState(center.add(dx, 0, dz), Blocks.OAK_FENCE.getDefaultState());
                }
            }
        }

        // البوابة وصناديق التغذية
        world.setBlockState(center.add(0, 0, 4), Blocks.OAK_FENCE_GATE.getDefaultState());
        world.setBlockState(center.add(1, 0, 4), Blocks.CHEST.getDefaultState());

        // حزم قش ومشارب ماء وفوانيس
        world.setBlockState(center.add(-3, 0, -3), Blocks.HAY_BLOCK.getDefaultState());
        world.setBlockState(center.add(-2, 0, -3), Blocks.HAY_BLOCK.getDefaultState());
        world.setBlockState(center.add(-3, 1, -3), Blocks.HAY_BLOCK.getDefaultState());

        world.setBlockState(center.add(3, 0, -3), Blocks.CAULDRON.getDefaultState());
        world.setBlockState(center.add(2, 0, -3), Blocks.WATER.getDefaultState());

        world.setBlockState(center.add(-4, 1, -4), Blocks.LANTERN.getDefaultState());
        world.setBlockState(center.add(4, 1, -4), Blocks.LANTERN.getDefaultState());
        world.setBlockState(center.add(-4, 1, 4), Blocks.LANTERN.getDefaultState());
        world.setBlockState(center.add(4, 1, 4), Blocks.LANTERN.getDefaultState());

        // رسبنة الحيوانات
        for (int i = 0; i < 3; i++) {
            CowEntity cow = EntityType.COW.create(world);
            if (cow != null) {
                cow.refreshPositionAndAngles(center.getX() - 1 + i, center.getY(), center.getZ() - 1, 0, 0);
                world.spawnEntity(cow);
            }

            SheepEntity sheep = EntityType.SHEEP.create(world);
            if (sheep != null) {
                sheep.refreshPositionAndAngles(center.getX() + 1, center.getY(), center.getZ() + i - 1, 0, 0);
                world.spawnEntity(sheep);
            }

            PigEntity pig = EntityType.PIG.create(world);
            if (pig != null) {
                pig.refreshPositionAndAngles(center.getX() - 2, center.getY(), center.getZ() + i, 0, 0);
                world.spawnEntity(pig);
            }
        }

        world.playSound(null, center, SoundEvents.ENTITY_COW_AMBIENT, SoundCategory.NEUTRAL, 1.0F, 1.0F);
    }

    // 3. برج صيد الموبات الكلاسيكي المرتفع (مقتبس طبق الأصل من الصورتين 7 و 8)
    // عمود إسقاط 22 بلوكة للموت بالضربة الواحدة، غرفة رسبنة مظلمة علوية 8x8، وهوبرات وصناديق بالأسفل
    private void buildMobTrap(World world, BlockPos center) {
        int towerHeight = 22; // الارتفاع المثالي لإسقاط الموبات لتبقى على نصف قلب ضربة واحدة!

        // 1. غرفة القتل والتجميع السفلية (Hoppers & Double Chests)
        world.setBlockState(center.add(0, -1, 0), Blocks.HOPPER.getDefaultState());
        world.setBlockState(center.add(1, -1, 0), Blocks.HOPPER.getDefaultState());
        world.setBlockState(center.add(0, -2, 0), Blocks.CHEST.getDefaultState());
        world.setBlockState(center.add(1, -2, 0), Blocks.CHEST.getDefaultState());

        // نصف حجر فوق الهوبر ليسمح بضرب أرجل الموبات دون أن تراك
        world.setBlockState(center.add(0, 0, 0), Blocks.SMOOTH_STONE_SLAB.getDefaultState());
        world.setBlockState(center.add(1, 0, 0), Blocks.SMOOTH_STONE_SLAB.getDefaultState());

        // 2. عمود الإسقاط العمودي الحجري المصفح (Drop Chute) بارتفاع 22 بلوكة
        for (int dy = 0; dy <= towerHeight; dy++) {
            // الجدران المحيطة بعمود الإسقاط 2x2
            world.setBlockState(center.add(-1, dy, -1), Blocks.COBBLESTONE.getDefaultState());
            world.setBlockState(center.add(0, dy, -1), Blocks.COBBLESTONE.getDefaultState());
            world.setBlockState(center.add(1, dy, -1), Blocks.COBBLESTONE.getDefaultState());
            world.setBlockState(center.add(2, dy, -1), Blocks.COBBLESTONE.getDefaultState());

            world.setBlockState(center.add(-1, dy, 2), Blocks.COBBLESTONE.getDefaultState());
            world.setBlockState(center.add(0, dy, 2), Blocks.COBBLESTONE.getDefaultState());
            world.setBlockState(center.add(1, dy, 2), Blocks.COBBLESTONE.getDefaultState());
            world.setBlockState(center.add(2, dy, 2), Blocks.COBBLESTONE.getDefaultState());

            world.setBlockState(center.add(-1, dy, 0), Blocks.COBBLESTONE.getDefaultState());
            world.setBlockState(center.add(-1, dy, 1), Blocks.COBBLESTONE.getDefaultState());
            world.setBlockState(center.add(2, dy, 0), Blocks.COBBLESTONE.getDefaultState());
            world.setBlockState(center.add(2, dy, 1), Blocks.COBBLESTONE.getDefaultState());

            // فتحة الهبوط في المركز
            world.setBlockState(center.add(0, dy, 0), Blocks.AIR.getDefaultState());
            world.setBlockState(center.add(1, dy, 0), Blocks.AIR.getDefaultState());
            world.setBlockState(center.add(0, dy, 1), Blocks.AIR.getDefaultState());
            world.setBlockState(center.add(1, dy, 1), Blocks.AIR.getDefaultState());

            // سلم خارجي للصعود لقمة البرج
            world.setBlockState(center.add(-1, dy, 3), Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.NORTH));
        }

        // فتحة أمان أمامية بالأسفل لضرب الموبات وسحب الإكس بي
        world.setBlockState(center.add(0, 1, -1), Blocks.COBBLESTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, net.minecraft.block.enums.SlabType.TOP));
        world.setBlockState(center.add(1, 1, -1), Blocks.COBBLESTONE_SLAB.getDefaultState().with(SlabBlock.TYPE, net.minecraft.block.enums.SlabType.TOP));

        // 3. منصة الرسبنة المظلمة العلوية في قمة البرج (Spawning Chamber) 10x10
        BlockPos top = center.up(towerHeight);
        for (int dx = -4; dx <= 5; dx++) {
            for (int dz = -4; dz <= 5; dz++) {
                // أرضية وسقف الغرفة المظلمة
                world.setBlockState(top.add(dx, 0, dz), Blocks.COBBLESTONE.getDefaultState());
                world.setBlockState(top.add(dx, 4, dz), Blocks.COBBLESTONE.getDefaultState());

                for (int dy = 1; dy <= 3; dy++) {
                    if (Math.abs(dx) == 4 || Math.abs(dz) == 4 || dx == 5 || dz == 5) {
                        world.setBlockState(top.add(dx, dy, dz), Blocks.COBBLESTONE.getDefaultState());
                    } else {
                        world.setBlockState(top.add(dx, dy, dz), Blocks.AIR.getDefaultState());
                    }
                }
            }
        }

        // مجاري الماء الأربعة في قمة البرج لدفع الموبات تلقائياً نحو حفرة السقوط
        world.setBlockState(top.add(-3, 1, 0), Blocks.WATER.getDefaultState());
        world.setBlockState(top.add(4, 1, 0), Blocks.WATER.getDefaultState());
        world.setBlockState(top.add(0, 1, -3), Blocks.WATER.getDefaultState());
        world.setBlockState(top.add(0, 1, 4), Blocks.WATER.getDefaultState());

        world.playSound(null, center, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.BLOCKS, 0.5F, 1.0F);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        tooltip.add(Text.translatable("tooltip.backpackmod.instant_farm").formatted(Formatting.GREEN));
    }
}
