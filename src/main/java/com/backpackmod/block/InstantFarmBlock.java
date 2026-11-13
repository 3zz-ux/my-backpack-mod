package com.backpackmod.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.ChickenEntity;
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

    // =========================================================================
    // 1. مزرعة محاصيل مدرجة أوتوماتيكية مانعة للتسريب 100% (Watertight Automatic Farm)
    // ثلاث مدرجات (قمح، جزر، بطاطس) مع حواف مانعة للفيضان، ترطيب دائم، وقناة تجميع بصناديق
    // =========================================================================
    private void buildCropFarm(World world, BlockPos center) {
        // تفريغ وتنظيف مساحة المزرعة بالكامل (11 عرض × 16 طول × 8 ارتفاع)
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -1; dz <= 15; dz++) {
                for (int dy = 0; dy <= 7; dy++) {
                    world.setBlockState(center.add(dx, dy, dz), Blocks.AIR.getDefaultState());
                }
            }
        }

        // 1. الأساس والأرضية السفلية
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -1; dz <= 15; dz++) {
                world.setBlockState(center.add(dx, -1, dz), Blocks.STONE_BRICKS.getDefaultState());
            }
        }

        // 2. الجدران المحيطة المانعة للفيضان (Containment Walls) بارتفاع يمنع أي تسرب للمياه
        for (int dz = -1; dz <= 15; dz++) {
            // الجدار الأيسر (X = -5)
            world.setBlockState(center.add(-5, 0, dz), Blocks.STRIPPED_SPRUCE_LOG.getDefaultState());
            world.setBlockState(center.add(-5, 1, dz), Blocks.SPRUCE_PLANKS.getDefaultState());
            world.setBlockState(center.add(-5, 2, dz), Blocks.SPRUCE_PLANKS.getDefaultState());
            world.setBlockState(center.add(-5, 3, dz), Blocks.SPRUCE_SLAB.getDefaultState());

            // الجدار الأيمن (X = 5)
            world.setBlockState(center.add(5, 0, dz), Blocks.STRIPPED_SPRUCE_LOG.getDefaultState());
            world.setBlockState(center.add(5, 1, dz), Blocks.SPRUCE_PLANKS.getDefaultState());
            world.setBlockState(center.add(5, 2, dz), Blocks.SPRUCE_PLANKS.getDefaultState());
            world.setBlockState(center.add(5, 3, dz), Blocks.SPRUCE_SLAB.getDefaultState());
        }

        // الجدار الخلفي العازل (Z = -1)
        for (int dx = -5; dx <= 5; dx++) {
            world.setBlockState(center.add(dx, 0, -1), Blocks.STONE_BRICKS.getDefaultState());
            world.setBlockState(center.add(dx, 1, -1), Blocks.STONE_BRICKS.getDefaultState());
            world.setBlockState(center.add(dx, 2, -1), Blocks.STONE_BRICKS.getDefaultState());
            world.setBlockState(center.add(dx, 3, -1), Blocks.STONE_BRICKS.getDefaultState());
            world.setBlockState(center.add(dx, 4, -1), Blocks.STONE_BRICK_SLAB.getDefaultState());
        }

        // 3. المدرج الأول العلوي (قمح) Z: 1 إلى 4، ارتفاع Y: 3
        for (int dz = 1; dz <= 4; dz++) {
            for (int dx = -3; dx <= 3; dx++) {
                world.setBlockState(center.add(dx, 2, dz), Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, 7));
                world.setBlockState(center.add(dx, 3, dz), Blocks.WHEAT.getDefaultState().with(CropBlock.AGE, 7));
            }
            // قنوات الترطيب المخفية تحت الممشى الجانبي
            world.setBlockState(center.add(-4, 2, dz), Blocks.WATER.getDefaultState());
            world.setBlockState(center.add(-4, 3, dz), Blocks.SPRUCE_SLAB.getDefaultState());
            world.setBlockState(center.add(4, 2, dz), Blocks.WATER.getDefaultState());
            world.setBlockState(center.add(4, 3, dz), Blocks.SPRUCE_SLAB.getDefaultState());
        }

        // 4. المدرج الثاني الأوسط (جزر) Z: 5 إلى 8، ارتفاع Y: 2
        for (int dz = 5; dz <= 8; dz++) {
            for (int dx = -3; dx <= 3; dx++) {
                world.setBlockState(center.add(dx, 1, dz), Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, 7));
                world.setBlockState(center.add(dx, 2, dz), Blocks.CARROTS.getDefaultState().with(CropBlock.AGE, 7));
            }
            world.setBlockState(center.add(-4, 1, dz), Blocks.WATER.getDefaultState());
            world.setBlockState(center.add(-4, 2, dz), Blocks.SPRUCE_SLAB.getDefaultState());
            world.setBlockState(center.add(4, 1, dz), Blocks.WATER.getDefaultState());
            world.setBlockState(center.add(4, 2, dz), Blocks.SPRUCE_SLAB.getDefaultState());
        }

        // 5. المدرج الثالث السفلي (بطاطس) Z: 9 إلى 12، ارتفاع Y: 1
        for (int dz = 9; dz <= 12; dz++) {
            for (int dx = -3; dx <= 3; dx++) {
                world.setBlockState(center.add(dx, 0, dz), Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, 7));
                world.setBlockState(center.add(dx, 1, dz), Blocks.POTATOES.getDefaultState().with(CropBlock.AGE, 7));
            }
            world.setBlockState(center.add(-4, 0, dz), Blocks.WATER.getDefaultState());
            world.setBlockState(center.add(-4, 1, dz), Blocks.SPRUCE_SLAB.getDefaultState());
            world.setBlockState(center.add(4, 0, dz), Blocks.WATER.getDefaultState());
            world.setBlockState(center.add(4, 1, dz), Blocks.SPRUCE_SLAB.getDefaultState());
        }

        // 6. خزان التدفق المائي العلوي مع بوابات تراب دور للحصاد عند الرغبة (Z = 0)
        for (int dx = -3; dx <= 3; dx++) {
            world.setBlockState(center.add(dx, 2, 0), Blocks.STONE_BRICKS.getDefaultState());
            world.setBlockState(center.add(dx, 3, 0), Blocks.WATER.getDefaultState());
            world.setBlockState(center.add(dx, 4, 0), Blocks.SPRUCE_TRAPDOOR.getDefaultState().with(TrapdoorBlock.OPEN, false));
        }
        world.setBlockState(center.add(4, 3, 0), Blocks.LEVER.getDefaultState());

        // 7. قناة التجميع السفلية وهوبرات الصندوق المزدوج (Z = 13 إلى 14)
        for (int dx = -4; dx <= 4; dx++) {
            world.setBlockState(center.add(dx, 0, 13), Blocks.STONE_BRICKS.getDefaultState());
        }
        // هوبرات التجميع في المنتصف تتصل بصندوق مزدوج
        world.setBlockState(center.add(-1, 0, 13), Blocks.HOPPER.getDefaultState());
        world.setBlockState(center.add(0, 0, 13), Blocks.HOPPER.getDefaultState());
        world.setBlockState(center.add(1, 0, 13), Blocks.HOPPER.getDefaultState());

        world.setBlockState(center.add(-1, 0, 14), Blocks.CHEST.getDefaultState());
        world.setBlockState(center.add(0, 0, 14), Blocks.CHEST.getDefaultState());

        // حواف زجاجية فخمة أمامية
        for (int dx = -4; dx <= 4; dx++) {
            world.setBlockState(center.add(dx, 1, 15), Blocks.OAK_FENCE.getDefaultState());
        }
        world.setBlockState(center.add(-4, 2, 15), Blocks.LANTERN.getDefaultState());
        world.setBlockState(center.add(4, 2, 15), Blocks.LANTERN.getDefaultState());

        world.playSound(null, center, SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    // =========================================================================
    // 2. مزرعة الحيوانات الريفية الفاخرة (100% أرضية جافة بدون أي فيضان)
    // حظيرة خشبية مظللة، أحواض ماء للشرب (كولدورن)، بالات قش، صناديق أعلاف، وحيوانات متنوعة
    // =========================================================================
    private void buildAnimalFarm(World world, BlockPos center) {
        int r = 6; // مساحة واسعة 13x13

        // تفريغ المساحة وبناء أرضية عشبية نظيفة 100%
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                world.setBlockState(center.add(dx, -1, dz), Blocks.DIRT.getDefaultState());
                // ممرات ترابية ريفية لطيفة في الوسط وعشب نضر في باقي الحظيرة
                if (Math.abs(dx) <= 1 && dz >= 0) {
                    world.setBlockState(center.add(dx, 0, dz), Blocks.DIRT_PATH.getDefaultState());
                } else {
                    world.setBlockState(center.add(dx, 0, dz), Blocks.GRASS_BLOCK.getDefaultState());
                }

                for (int dy = 1; dy <= 6; dy++) {
                    world.setBlockState(center.add(dx, dy, dz), Blocks.AIR.getDefaultState());
                }

                // سياج المزرعة الخشبي المحيط
                if (Math.abs(dx) == r || Math.abs(dz) == r) {
                    world.setBlockState(center.add(dx, 1, dz), Blocks.SPRUCE_FENCE.getDefaultState());
                }
            }
        }

        // بوابات الدخول الأمامية (Z = r)
        world.setBlockState(center.add(0, 1, r), Blocks.SPRUCE_FENCE_GATE.getDefaultState().with(FenceGateBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(-1, 1, r), Blocks.SPRUCE_FENCE.getDefaultState());
        world.setBlockState(center.add(1, 1, r), Blocks.SPRUCE_FENCE.getDefaultState());

        // فوانيس إنارة على أعمدة السياج
        world.setBlockState(center.add(-r, 2, -r), Blocks.LANTERN.getDefaultState());
        world.setBlockState(center.add(r, 2, -r), Blocks.LANTERN.getDefaultState());
        world.setBlockState(center.add(-r, 2, r), Blocks.LANTERN.getDefaultState());
        world.setBlockState(center.add(r, 2, r), Blocks.LANTERN.getDefaultState());
        world.setBlockState(center.add(-1, 2, r), Blocks.LANTERN.getDefaultState());
        world.setBlockState(center.add(1, 2, r), Blocks.LANTERN.getDefaultState());

        // 2. حظيرة مظللة بسقف خشبي أنيق لحماية الحيوانات في الخلف (Z: -6 إلى -2)
        for (int dx = -4; dx <= 4; dx++) {
            // سقف الحظيرة المائل
            world.setBlockState(center.add(dx, 4, -4), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH));
            world.setBlockState(center.add(dx, 4, -5), Blocks.SPRUCE_PLANKS.getDefaultState());
            world.setBlockState(center.add(dx, 4, -6), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH));
        }
        // أعمدة دعم الحظيرة
        world.setBlockState(center.add(-4, 1, -4), Blocks.SPRUCE_FENCE.getDefaultState());
        world.setBlockState(center.add(-4, 2, -4), Blocks.SPRUCE_FENCE.getDefaultState());
        world.setBlockState(center.add(-4, 3, -4), Blocks.SPRUCE_FENCE.getDefaultState());

        world.setBlockState(center.add(4, 1, -4), Blocks.SPRUCE_FENCE.getDefaultState());
        world.setBlockState(center.add(4, 2, -4), Blocks.SPRUCE_FENCE.getDefaultState());
        world.setBlockState(center.add(4, 3, -4), Blocks.SPRUCE_FENCE.getDefaultState());

        // فوانيس معلقة داخل الحظيرة
        world.setBlockState(center.add(-2, 3, -5), Blocks.LANTERN.getDefaultState().with(LanternBlock.HANGING, true));
        world.setBlockState(center.add(2, 3, -5), Blocks.LANTERN.getDefaultState().with(LanternBlock.HANGING, true));

        // 3. أحواض شرب نظيفة ومحكمة (كولدورن مليء بالماء - لا يوجد ماء مسكوب على الأرض نهائياً!)
        world.setBlockState(center.add(-3, 1, -5), Blocks.CAULDRON.getDefaultState());
        world.setBlockState(center.add(-2, 1, -5), Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
        world.setBlockState(center.add(-1, 1, -5), Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));

        // 4. بالات قش متناسقة وصناديق لتخزين الأعلاف
        world.setBlockState(center.add(2, 1, -5), Blocks.HAY_BLOCK.getDefaultState());
        world.setBlockState(center.add(3, 1, -5), Blocks.HAY_BLOCK.getDefaultState());
        world.setBlockState(center.add(2, 2, -5), Blocks.HAY_BLOCK.getDefaultState());
        world.setBlockState(center.add(3, 1, -4), Blocks.CHEST.getDefaultState());

        // 5. رسبنة الحيوانات بصحة تامة على الأرض الجافة
        for (int i = 0; i < 3; i++) {
            CowEntity cow = EntityType.COW.create(world);
            if (cow != null) {
                cow.refreshPositionAndAngles(center.getX() - 2 + i * 2, center.getY() + 1, center.getZ() - 2, 0, 0);
                world.spawnEntity(cow);
            }

            SheepEntity sheep = EntityType.SHEEP.create(world);
            if (sheep != null) {
                sheep.refreshPositionAndAngles(center.getX() - 3 + i * 2, center.getY() + 1, center.getZ() + 1, 0, 0);
                world.spawnEntity(sheep);
            }

            PigEntity pig = EntityType.PIG.create(world);
            if (pig != null) {
                pig.refreshPositionAndAngles(center.getX() + 2, center.getY() + 1, center.getZ() + i, 0, 0);
                world.spawnEntity(pig);
            }

            ChickenEntity chicken = EntityType.CHICKEN.create(world);
            if (chicken != null) {
                chicken.refreshPositionAndAngles(center.getX() - 1, center.getY() + 1, center.getZ() + 2 + i, 0, 0);
                world.spawnEntity(chicken);
            }
        }

        world.playSound(null, center, SoundEvents.ENTITY_COW_AMBIENT, SoundCategory.NEUTRAL, 1.0F, 1.0F);
    }

    // =========================================================================
    // 3. برج صيد الموبات العملاق الأسطوري (Giant Spawner Mob Trap Tower)
    // عمود إسقاط 22 بلوكة للموت بضربة واحدة، غرفة رسبنة عملاقة 14x14 مع سبونرات زومبي وهياكل عظمية حقيقية!
    // =========================================================================
    private void buildMobTrap(World world, BlockPos center) {
        int towerHeight = 22; // الارتفاع الذهبي لإسقاط الوحوش لتتبقى على نصف قلب!

        // 1. غرفة الجمع السفلية الآمنة مع 4 هوبرات وصناديق مزدوجة
        for (int dx = -3; dx <= 4; dx++) {
            for (int dz = -3; dz <= 4; dz++) {
                world.setBlockState(center.add(dx, -1, dz), Blocks.POLISHED_DEEPSLATE.getDefaultState());
                for (int dy = 0; dy <= 4; dy++) {
                    world.setBlockState(center.add(dx, dy, dz), Blocks.AIR.getDefaultState());
                }
            }
        }

        // شبكة هوبرات 2x2 لتجميع اللوت بالكامل
        world.setBlockState(center.add(0, 0, 0), Blocks.HOPPER.getDefaultState());
        world.setBlockState(center.add(1, 0, 0), Blocks.HOPPER.getDefaultState());
        world.setBlockState(center.add(0, 0, 1), Blocks.HOPPER.getDefaultState());
        world.setBlockState(center.add(1, 0, 1), Blocks.HOPPER.getDefaultState());

        // صناديق تخزين الموارد في الأمام
        world.setBlockState(center.add(0, 0, -1), Blocks.CHEST.getDefaultState());
        world.setBlockState(center.add(1, 0, -1), Blocks.CHEST.getDefaultState());

        // نصف حجر علوي على الهوبرات لنقل السقوط وحماية اللاعب
        world.setBlockState(center.add(0, 1, 0), Blocks.SMOOTH_STONE_SLAB.getDefaultState());
        world.setBlockState(center.add(1, 1, 0), Blocks.SMOOTH_STONE_SLAB.getDefaultState());
        world.setBlockState(center.add(0, 1, 1), Blocks.SMOOTH_STONE_SLAB.getDefaultState());
        world.setBlockState(center.add(1, 1, 1), Blocks.SMOOTH_STONE_SLAB.getDefaultState());

        // فتحة أمان أمامية بالأسفل لضرب أقدام الموبات بدون أن تراك وتفجر الكريبر
        world.setBlockState(center.add(0, 2, -1), Blocks.STONE_BRICK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP));
        world.setBlockState(center.add(1, 2, -1), Blocks.STONE_BRICK_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP));

        // 2. عمود الإسقاط الحجري المحصن (Drop Chute 2x2) بارتفاع 22 بلوكة
        for (int dy = 1; dy <= towerHeight; dy++) {
            for (int dx = -1; dx <= 2; dx++) {
                for (int dz = -1; dz <= 2; dz++) {
                    // الجدران المحيطة
                    if (dx == -1 || dx == 2 || dz == -1 || dz == 2) {
                        // ترك فتحة الضرب في الأسفل عند dy = 1 و 2
                        if ((dy == 1 || dy == 2) && dz == -1 && (dx == 0 || dx == 1)) {
                            continue;
                        }
                        world.setBlockState(center.add(dx, dy, dz), Blocks.DEEPSLATE_BRICKS.getDefaultState());
                    } else {
                        // فتحة الهبوط المركزية 2x2 مفرغة
                        world.setBlockState(center.add(dx, dy, dz), Blocks.AIR.getDefaultState());
                    }
                }
            }
            // سلم خارجي للصعود لقمة البرج
            world.setBlockState(center.add(-2, dy, 0), Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.EAST));
        }

        // 3. قلعة الرسبنة العملاقة المظلمة في القمة (Spawning Fortress) 14x14
        BlockPos top = center.up(towerHeight);
        int roomRadius = 6; // مساحة واسعة من -6 إلى 7 (14x14)

        for (int dx = -roomRadius; dx <= roomRadius + 1; dx++) {
            for (int dz = -roomRadius; dz <= roomRadius + 1; dz++) {
                // أرضية وسقف غرفة الرسبنة لمنع أي ضوء
                world.setBlockState(top.add(dx, 0, dz), Blocks.STONE_BRICKS.getDefaultState());
                world.setBlockState(top.add(dx, 4, dz), Blocks.STONE_BRICKS.getDefaultState());

                // الجدران الخارجية المغلقة للغرفة
                for (int dy = 1; dy <= 3; dy++) {
                    if (dx == -roomRadius || dx == roomRadius + 1 || dz == -roomRadius || dz == roomRadius + 1) {
                        world.setBlockState(top.add(dx, dy, dz), Blocks.STONE_BRICKS.getDefaultState());
                    } else {
                        world.setBlockState(top.add(dx, dy, dz), Blocks.AIR.getDefaultState());
                    }
                }
            }
        }

        // مجاري الماء الدافعة نحو فتحة السقوط 2x2 في المنتصف
        world.setBlockState(top.add(-4, 1, 0), Blocks.WATER.getDefaultState());
        world.setBlockState(top.add(-4, 1, 1), Blocks.WATER.getDefaultState());
        world.setBlockState(top.add(5, 1, 0), Blocks.WATER.getDefaultState());
        world.setBlockState(top.add(5, 1, 1), Blocks.WATER.getDefaultState());
        world.setBlockState(top.add(0, 1, -4), Blocks.WATER.getDefaultState());
        world.setBlockState(top.add(1, 1, -4), Blocks.WATER.getDefaultState());
        world.setBlockState(top.add(0, 1, 5), Blocks.WATER.getDefaultState());
        world.setBlockState(top.add(1, 1, 5), Blocks.WATER.getDefaultState());

        // ترابدورز مفتوحة على حواف فتحة السقوط لتظن الوحوش أنها أرضية وتسقط فوراً
        world.setBlockState(top.add(-1, 1, 0), Blocks.OAK_TRAPDOOR.getDefaultState().with(TrapdoorBlock.OPEN, true));
        world.setBlockState(top.add(-1, 1, 1), Blocks.OAK_TRAPDOOR.getDefaultState().with(TrapdoorBlock.OPEN, true));
        world.setBlockState(top.add(2, 1, 0), Blocks.OAK_TRAPDOOR.getDefaultState().with(TrapdoorBlock.OPEN, true));
        world.setBlockState(top.add(2, 1, 1), Blocks.OAK_TRAPDOOR.getDefaultState().with(TrapdoorBlock.OPEN, true));
        world.setBlockState(top.add(0, 1, -1), Blocks.OAK_TRAPDOOR.getDefaultState().with(TrapdoorBlock.OPEN, true));
        world.setBlockState(top.add(1, 1, -1), Blocks.OAK_TRAPDOOR.getDefaultState().with(TrapdoorBlock.OPEN, true));
        world.setBlockState(top.add(0, 1, 2), Blocks.OAK_TRAPDOOR.getDefaultState().with(TrapdoorBlock.OPEN, true));
        world.setBlockState(top.add(1, 1, 2), Blocks.OAK_TRAPDOOR.getDefaultState().with(TrapdoorBlock.OPEN, true));

        // 4. وضع سبونرات حقيقية نشطة داخل الغرفة (سبونر زومبي + سبونر هياكل عظمية)
        BlockPos zombieSpawnerPos = top.add(-2, 2, -2);
        world.setBlockState(zombieSpawnerPos, Blocks.SPAWNER.getDefaultState());
        BlockEntity zBe = world.getBlockEntity(zombieSpawnerPos);
        if (zBe instanceof MobSpawnerBlockEntity spawner) {
            spawner.getLogic().setEntityId(EntityType.ZOMBIE, world, world.getRandom(), zombieSpawnerPos);
            spawner.markDirty();
            world.updateListeners(zombieSpawnerPos, Blocks.SPAWNER.getDefaultState(), Blocks.SPAWNER.getDefaultState(), 3);
        }

        BlockPos skeletonSpawnerPos = top.add(3, 2, 3);
        world.setBlockState(skeletonSpawnerPos, Blocks.SPAWNER.getDefaultState());
        BlockEntity sBe = world.getBlockEntity(skeletonSpawnerPos);
        if (sBe instanceof MobSpawnerBlockEntity spawner) {
            spawner.getLogic().setEntityId(EntityType.SKELETON, world, world.getRandom(), skeletonSpawnerPos);
            spawner.markDirty();
            world.updateListeners(skeletonSpawnerPos, Blocks.SPAWNER.getDefaultState(), Blocks.SPAWNER.getDefaultState(), 3);
        }

        // شرفات حربية وفوانيس في أعلى سطح البرج من الخارج
        for (int dx = -roomRadius; dx <= roomRadius + 1; dx += 2) {
            world.setBlockState(top.add(dx, 5, -roomRadius), Blocks.STONE_BRICK_WALL.getDefaultState());
            world.setBlockState(top.add(dx, 5, roomRadius + 1), Blocks.STONE_BRICK_WALL.getDefaultState());
        }
        for (int dz = -roomRadius; dz <= roomRadius + 1; dz += 2) {
            world.setBlockState(top.add(-roomRadius, 5, dz), Blocks.STONE_BRICK_WALL.getDefaultState());
            world.setBlockState(top.add(roomRadius + 1, 5, dz), Blocks.STONE_BRICK_WALL.getDefaultState());
        }

        world.playSound(null, center, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.BLOCKS, 0.7F, 1.0F);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        tooltip.add(Text.translatable("tooltip.backpackmod.instant_farm").formatted(Formatting.GREEN));
    }
}
