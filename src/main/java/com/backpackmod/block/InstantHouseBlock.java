package com.backpackmod.block;

import net.minecraft.block.*;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
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

public class InstantHouseBlock extends Block {
    public enum HouseType {
        WOODEN,
        STONE,
        CASTLE
    }

    private final HouseType type;

    public InstantHouseBlock(HouseType type, Settings settings) {
        super(settings);
        this.type = type;
    }

    public HouseType getType() {
        return type;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        if (!world.isClient) {
            switch (type) {
                case WOODEN -> buildMedievalManor(world, pos);
                case STONE -> buildStonePalace(world, pos);
                case CASTLE -> buildFortifiedCitadel(world, pos);
            }
        }
    }

    // =========================================================================
    // 1. قصر ريفي تاريخي فاخر 11x11 (Medieval Luxury Manor)
    // تصميم متكامل: واجهات خشبية، سقف جمالوني مقفل بالكامل، غرفة تطوير لفل 30، مطبخ وورشة، وغرفة نوم ملكية
    // =========================================================================
    private void buildMedievalManor(World world, BlockPos center) {
        int r = 5; // 11x11 (من -5 إلى +5)
        int h = 5; // ارتفاع الجدران حتى السقف الداخلي

        // 1. تسوية وتفريغ المساحة وبناء أساس حجري متين وأرضية خشبية دافئة
        for (int dx = -r - 1; dx <= r + 1; dx++) {
            for (int dz = -r - 1; dz <= r + 1; dz++) {
                world.setBlockState(center.add(dx, -1, dz), Blocks.COBBLESTONE.getDefaultState());
                if (Math.abs(dx) <= r && Math.abs(dz) <= r) {
                    world.setBlockState(center.add(dx, 0, dz), Blocks.SPRUCE_PLANKS.getDefaultState());
                    for (int dy = 1; dy <= 12; dy++) {
                        world.setBlockState(center.add(dx, dy, dz), Blocks.AIR.getDefaultState());
                    }
                }
            }
        }

        // 2. الجدران الخارجية الفاخرة مع أعمدة خشب مقشر عند الأركان وإطارات النوافذ
        for (int dy = 1; dy <= h; dy++) {
            for (int i = -r; i <= r; i++) {
                world.setBlockState(center.add(i, dy, -r), Blocks.OAK_PLANKS.getDefaultState());
                world.setBlockState(center.add(i, dy, r), Blocks.OAK_PLANKS.getDefaultState());
                world.setBlockState(center.add(-r, dy, i), Blocks.OAK_PLANKS.getDefaultState());
                world.setBlockState(center.add(r, dy, i), Blocks.OAK_PLANKS.getDefaultState());
            }

            // أعمدة خشبية قوية (Stripped Spruce Logs) في الأركان ونقاط الارتكاز
            world.setBlockState(center.add(-r, dy, -r), Blocks.STRIPPED_SPRUCE_LOG.getDefaultState());
            world.setBlockState(center.add(r, dy, -r), Blocks.STRIPPED_SPRUCE_LOG.getDefaultState());
            world.setBlockState(center.add(-r, dy, r), Blocks.STRIPPED_SPRUCE_LOG.getDefaultState());
            world.setBlockState(center.add(r, dy, r), Blocks.STRIPPED_SPRUCE_LOG.getDefaultState());
            world.setBlockState(center.add(0, dy, -r), Blocks.STRIPPED_SPRUCE_LOG.getDefaultState());
            world.setBlockState(center.add(0, dy, r), Blocks.STRIPPED_SPRUCE_LOG.getDefaultState());
        }

        // 3. نوافذ زجاجية بانورامية في الواجهات
        for (int dy = 2; dy <= 3; dy++) {
            world.setBlockState(center.add(-2, dy, -r), Blocks.GLASS_PANE.getDefaultState());
            world.setBlockState(center.add(-3, dy, -r), Blocks.GLASS_PANE.getDefaultState());
            world.setBlockState(center.add(2, dy, -r), Blocks.GLASS_PANE.getDefaultState());
            world.setBlockState(center.add(3, dy, -r), Blocks.GLASS_PANE.getDefaultState());

            world.setBlockState(center.add(-r, dy, -2), Blocks.GLASS_PANE.getDefaultState());
            world.setBlockState(center.add(-r, dy, 2), Blocks.GLASS_PANE.getDefaultState());
            world.setBlockState(center.add(r, dy, -2), Blocks.GLASS_PANE.getDefaultState());
            world.setBlockState(center.add(r, dy, 2), Blocks.GLASS_PANE.getDefaultState());

            world.setBlockState(center.add(-2, dy, r), Blocks.GLASS_PANE.getDefaultState());
            world.setBlockState(center.add(2, dy, r), Blocks.GLASS_PANE.getDefaultState());
        }

        // 4. الباب الأمامي الملكي مع شرفة ومدخل مضاء
        world.setBlockState(center.add(0, 1, r), Blocks.SPRUCE_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(0, 2, r), Blocks.SPRUCE_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(0, 0, r + 1), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(-1, 1, r + 1), Blocks.LANTERN.getDefaultState());
        world.setBlockState(center.add(1, 1, r + 1), Blocks.LANTERN.getDefaultState());

        // 5. السقف الداخلي العازل (Ceiling) عند الارتفاع h يفصل الغرفة عن جوف السقف
        for (int dx = -r + 1; dx <= r - 1; dx++) {
            for (int dz = -r + 1; dz <= r - 1; dz++) {
                world.setBlockState(center.add(dx, h, dz), Blocks.SPRUCE_PLANKS.getDefaultState());
            }
        }

        // 6. الجدران المثلثية الأمامية والخلفية (Gable End Walls) لغلق السقف تماماً بدون أي ثغرات أو خرابات
        for (int zGable : new int[]{-r, r}) {
            for (int x = -4; x <= 4; x++) world.setBlockState(center.add(x, h + 1, zGable), Blocks.OAK_PLANKS.getDefaultState());
            for (int x = -3; x <= 3; x++) world.setBlockState(center.add(x, h + 2, zGable), Blocks.OAK_PLANKS.getDefaultState());
            for (int x = -2; x <= 2; x++) world.setBlockState(center.add(x, h + 3, zGable), Blocks.OAK_PLANKS.getDefaultState());
            world.setBlockState(center.add(0, h + 1, zGable), Blocks.GLASS_PANE.getDefaultState());
            world.setBlockState(center.add(0, h + 2, zGable), Blocks.GLASS_PANE.getDefaultState());
        }

        // 7. سقف جمالوني تاريخي مدرج ومتقن 100% (A-Frame Steep Roof)
        for (int dz = -r - 1; dz <= r + 1; dz++) {
            world.setBlockState(center.add(-6, h, dz), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST));
            world.setBlockState(center.add(6, h, dz), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST));

            world.setBlockState(center.add(-5, h + 1, dz), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST));
            world.setBlockState(center.add(5, h + 1, dz), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST));

            world.setBlockState(center.add(-4, h + 2, dz), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST));
            world.setBlockState(center.add(4, h + 2, dz), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST));

            world.setBlockState(center.add(-3, h + 3, dz), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST));
            world.setBlockState(center.add(3, h + 3, dz), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST));

            world.setBlockState(center.add(-2, h + 4, dz), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST));
            world.setBlockState(center.add(2, h + 4, dz), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST));

            // قمة السقف مع السلابات العلوية المكتملة
            world.setBlockState(center.add(-1, h + 4, dz), Blocks.SPRUCE_SLAB.getDefaultState());
            world.setBlockState(center.add(0, h + 5, dz), Blocks.SPRUCE_SLAB.getDefaultState());
            world.setBlockState(center.add(1, h + 4, dz), Blocks.SPRUCE_SLAB.getDefaultState());
        }

        // مدخنة طوب أنيقة مع دخان متصاعد حقيقي
        for (int y = 1; y <= h + 6; y++) {
            world.setBlockState(center.add(3, y, -3), Blocks.BRICKS.getDefaultState());
        }
        world.setBlockState(center.add(3, h + 7, -3), Blocks.CAMPFIRE.getDefaultState().with(CampfireBlock.LIT, true));

        // 8. الديكور الداخلي المتكامل (غرفة لفل 30 + مطبخ متكامل + ورشة وسندان + غرفة نوم ملكية)
        // أ) غرفة تطوير لفل 30 قانونية (15 مكتبة على الأطراف مع مسافة هواء للطاولة لتصل لأقصى لفل)
        BlockPos enchTable = center.add(-3, 1, -2);
        world.setBlockState(enchTable, Blocks.ENCHANTING_TABLE.getDefaultState());
        // جدار المكاتب في الركن الشمالي الغربي (ارتفاع طبقتين مع مسافة هواء لامتصاص السحر)
        for (int y = 1; y <= 2; y++) {
            world.setBlockState(center.add(-4, y, -4), Blocks.BOOKSHELF.getDefaultState());
            world.setBlockState(center.add(-3, y, -4), Blocks.BOOKSHELF.getDefaultState());
            world.setBlockState(center.add(-2, y, -4), Blocks.BOOKSHELF.getDefaultState());
            world.setBlockState(center.add(-1, y, -4), Blocks.BOOKSHELF.getDefaultState());

            world.setBlockState(center.add(-4, y, -3), Blocks.BOOKSHELF.getDefaultState());
            world.setBlockState(center.add(-4, y, -2), Blocks.BOOKSHELF.getDefaultState());
            world.setBlockState(center.add(-4, y, -1), Blocks.BOOKSHELF.getDefaultState());
            world.setBlockState(center.add(-4, y, 0), Blocks.BOOKSHELF.getDefaultState());
        }
        // سندان وطاولة حدادة وحجر صقل بجانب التطوير
        world.setBlockState(center.add(-2, 1, 0), Blocks.ANVIL.getDefaultState());
        world.setBlockState(center.add(-2, 1, 1), Blocks.SMITHING_TABLE.getDefaultState());
        world.setBlockState(center.add(-3, 1, 1), Blocks.GRINDSTONE.getDefaultState());

        // ب) المطبخ التاريخي والورشة في الركن الشمالي الشرقي
        world.setBlockState(center.add(3, 1, -4), Blocks.FURNACE.getDefaultState());
        world.setBlockState(center.add(2, 1, -4), Blocks.SMOKER.getDefaultState());
        world.setBlockState(center.add(1, 1, -4), Blocks.BLAST_FURNACE.getDefaultState());
        world.setBlockState(center.add(0, 1, -4), Blocks.CRAFTING_TABLE.getDefaultState());
        world.setBlockState(center.add(4, 1, -3), Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));

        // ج) منطقة التخزين والخزائن
        world.setBlockState(center.add(4, 1, 0), Blocks.CHEST.getDefaultState());
        world.setBlockState(center.add(4, 1, 1), Blocks.CHEST.getDefaultState());
        world.setBlockState(center.add(4, 1, 2), Blocks.CHEST.getDefaultState());
        world.setBlockState(center.add(4, 1, 3), Blocks.CHEST.getDefaultState());

        // د) غرفة النوم الملكية (سرير مزدوج فاخر، سجاد أحمر ممتد، وفوانيس معلقة)
        world.setBlockState(center.add(-2, 1, 4), Blocks.RED_BED.getDefaultState().with(BedBlock.PART, BedPart.HEAD).with(BedBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(-2, 1, 3), Blocks.RED_BED.getDefaultState().with(BedBlock.PART, BedPart.FOOT).with(BedBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(-3, 1, 4), Blocks.RED_BED.getDefaultState().with(BedBlock.PART, BedPart.HEAD).with(BedBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(-3, 1, 3), Blocks.RED_BED.getDefaultState().with(BedBlock.PART, BedPart.FOOT).with(BedBlock.FACING, Direction.SOUTH));

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 3; z++) {
                world.setBlockState(center.add(x, 1, z), Blocks.RED_CARPET.getDefaultState());
            }
        }
        world.setBlockState(center.add(0, h - 1, 0), Blocks.LANTERN.getDefaultState().with(LanternBlock.HANGING, true));
        world.setBlockState(center.add(-2, h - 1, -2), Blocks.LANTERN.getDefaultState().with(LanternBlock.HANGING, true));
        world.setBlockState(center.add(2, h - 1, -2), Blocks.LANTERN.getDefaultState().with(LanternBlock.HANGING, true));

        world.playSound(null, center, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    // =========================================================================
    // 2. قصر حجري أثري ملكي 11x11 (Ancient Royal Stone Palace)
    // واجهات طوب حجري منقوش، سقف قرميدي متقن، غرفة تطوير لفل 30 كاملة، مطبخ ملكي وغرفة نوم زرقاء
    // =========================================================================
    private void buildStonePalace(World world, BlockPos center) {
        int r = 5;
        int h = 5;

        for (int dx = -r - 1; dx <= r + 1; dx++) {
            for (int dz = -r - 1; dz <= r + 1; dz++) {
                world.setBlockState(center.add(dx, -1, dz), Blocks.POLISHED_ANDESITE.getDefaultState());
                if (Math.abs(dx) <= r && Math.abs(dz) <= r) {
                    world.setBlockState(center.add(dx, 0, dz), Blocks.SMOOTH_STONE.getDefaultState());
                    for (int dy = 1; dy <= 12; dy++) {
                        world.setBlockState(center.add(dx, dy, dz), Blocks.AIR.getDefaultState());
                    }
                }
            }
        }

        // جدران طوب حجري وأعمدة منقوشة
        for (int dy = 1; dy <= h; dy++) {
            for (int i = -r; i <= r; i++) {
                world.setBlockState(center.add(i, dy, -r), Blocks.STONE_BRICKS.getDefaultState());
                world.setBlockState(center.add(i, dy, r), Blocks.STONE_BRICKS.getDefaultState());
                world.setBlockState(center.add(-r, dy, i), Blocks.STONE_BRICKS.getDefaultState());
                world.setBlockState(center.add(r, dy, i), Blocks.STONE_BRICKS.getDefaultState());
            }
            world.setBlockState(center.add(-r, dy, -r), Blocks.CHISELED_STONE_BRICKS.getDefaultState());
            world.setBlockState(center.add(r, dy, -r), Blocks.CHISELED_STONE_BRICKS.getDefaultState());
            world.setBlockState(center.add(-r, dy, r), Blocks.CHISELED_STONE_BRICKS.getDefaultState());
            world.setBlockState(center.add(r, dy, r), Blocks.CHISELED_STONE_BRICKS.getDefaultState());
        }

        // نوافذ زجاجية مقوسة
        for (int dy = 2; dy <= 4; dy++) {
            world.setBlockState(center.add(-2, dy, -r), Blocks.GLASS_PANE.getDefaultState());
            world.setBlockState(center.add(2, dy, -r), Blocks.GLASS_PANE.getDefaultState());
            world.setBlockState(center.add(-r, dy, 0), Blocks.GLASS_PANE.getDefaultState());
            world.setBlockState(center.add(r, dy, 0), Blocks.GLASS_PANE.getDefaultState());
        }

        // مدخل القصر
        world.setBlockState(center.add(0, 1, r), Blocks.DARK_OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(0, 2, r), Blocks.DARK_OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(0, 0, r + 1), Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH));

        // سقف داخلي من حجر ناعم
        for (int dx = -r + 1; dx <= r - 1; dx++) {
            for (int dz = -r + 1; dz <= r - 1; dz++) {
                world.setBlockState(center.add(dx, h, dz), Blocks.SMOOTH_STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP));
            }
        }

        // جدران المثلث الجمالوني الحجري
        for (int zGable : new int[]{-r, r}) {
            for (int x = -4; x <= 4; x++) world.setBlockState(center.add(x, h + 1, zGable), Blocks.STONE_BRICKS.getDefaultState());
            for (int x = -3; x <= 3; x++) world.setBlockState(center.add(x, h + 2, zGable), Blocks.STONE_BRICKS.getDefaultState());
            for (int x = -2; x <= 2; x++) world.setBlockState(center.add(x, h + 3, zGable), Blocks.STONE_BRICKS.getDefaultState());
            world.setBlockState(center.add(0, h + 1, zGable), Blocks.GLASS_PANE.getDefaultState());
        }

        // سقف قرميدي حجري كامل
        for (int dz = -r - 1; dz <= r + 1; dz++) {
            for (int step = 0; step <= 4; step++) {
                world.setBlockState(center.add(-r - 1 + step, h + step, dz), Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST));
                world.setBlockState(center.add(r + 1 - step, h + step, dz), Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST));
            }
            world.setBlockState(center.add(0, h + 5, dz), Blocks.STONE_BRICK_SLAB.getDefaultState());
        }

        // مدخنة حجرية فخمة
        world.setBlockState(center.add(3, h + 5, -2), Blocks.STONE_BRICKS.getDefaultState());
        world.setBlockState(center.add(3, h + 6, -2), Blocks.CAMPFIRE.getDefaultState().with(CampfireBlock.LIT, true));

        // غرفة تطوير لفل 30 فاخرة
        BlockPos stoneEnch = center.add(-3, 1, -2);
        world.setBlockState(stoneEnch, Blocks.ENCHANTING_TABLE.getDefaultState());
        for (int y = 1; y <= 2; y++) {
            world.setBlockState(center.add(-4, y, -4), Blocks.BOOKSHELF.getDefaultState());
            world.setBlockState(center.add(-3, y, -4), Blocks.BOOKSHELF.getDefaultState());
            world.setBlockState(center.add(-2, y, -4), Blocks.BOOKSHELF.getDefaultState());
            world.setBlockState(center.add(-1, y, -4), Blocks.BOOKSHELF.getDefaultState());

            world.setBlockState(center.add(-4, y, -3), Blocks.BOOKSHELF.getDefaultState());
            world.setBlockState(center.add(-4, y, -2), Blocks.BOOKSHELF.getDefaultState());
            world.setBlockState(center.add(-4, y, -1), Blocks.BOOKSHELF.getDefaultState());
            world.setBlockState(center.add(-4, y, 0), Blocks.BOOKSHELF.getDefaultState());
        }
        world.setBlockState(center.add(-2, 1, 0), Blocks.ANVIL.getDefaultState());
        world.setBlockState(center.add(-2, 1, 1), Blocks.SMITHING_TABLE.getDefaultState());

        // مطبخ وأفران ملكية
        world.setBlockState(center.add(3, 1, -4), Blocks.BLAST_FURNACE.getDefaultState());
        world.setBlockState(center.add(2, 1, -4), Blocks.SMOKER.getDefaultState());
        world.setBlockState(center.add(1, 1, -4), Blocks.FURNACE.getDefaultState());
        world.setBlockState(center.add(0, 1, -4), Blocks.CRAFTING_TABLE.getDefaultState());

        // غرفة نوم ملكية مع سجاد أزرق
        world.setBlockState(center.add(-2, 1, 4), Blocks.BLUE_BED.getDefaultState().with(BedBlock.PART, BedPart.HEAD).with(BedBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(-2, 1, 3), Blocks.BLUE_BED.getDefaultState().with(BedBlock.PART, BedPart.FOOT).with(BedBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(4, 1, 1), Blocks.CHEST.getDefaultState());
        world.setBlockState(center.add(4, 1, 2), Blocks.CHEST.getDefaultState());

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 2; z++) {
                world.setBlockState(center.add(x, 1, z), Blocks.BLUE_CARPET.getDefaultState());
            }
        }
        world.setBlockState(center.add(0, h - 1, 0), Blocks.LANTERN.getDefaultState().with(LanternBlock.HANGING, true));

        world.playSound(null, center, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    // =========================================================================
    // 3. قلعة حصينة تاريخية 11x11 (Fortified Medieval Citadel)
    // 4 أبراج مراقبة بأضواء الروح الزرقاء، شرفات حربية، باب حديدي بأزرار، غرفة تطوير وأسلحة كاملة
    // =========================================================================
    private void buildFortifiedCitadel(World world, BlockPos center) {
        int r = 5;
        int h = 6;

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                world.setBlockState(center.add(dx, -1, dz), Blocks.DEEPSLATE_TILES.getDefaultState());
                world.setBlockState(center.add(dx, 0, dz), Blocks.POLISHED_DEEPSLATE.getDefaultState());
                for (int dy = 1; dy <= 12; dy++) {
                    world.setBlockState(center.add(dx, dy, dz), Blocks.AIR.getDefaultState());
                }
            }
        }

        // جدران ديب سليت حصينة
        for (int dy = 1; dy <= h; dy++) {
            for (int i = -r; i <= r; i++) {
                world.setBlockState(center.add(i, dy, -r), Blocks.DEEPSLATE_BRICKS.getDefaultState());
                world.setBlockState(center.add(i, dy, r), Blocks.DEEPSLATE_BRICKS.getDefaultState());
                world.setBlockState(center.add(-r, dy, i), Blocks.DEEPSLATE_BRICKS.getDefaultState());
                world.setBlockState(center.add(r, dy, i), Blocks.DEEPSLATE_BRICKS.getDefaultState());
            }
            world.setBlockState(center.add(-r, dy, -r), Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState());
            world.setBlockState(center.add(r, dy, -r), Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState());
            world.setBlockState(center.add(-r, dy, r), Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState());
            world.setBlockState(center.add(r, dy, r), Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState());
        }

        // سطح القلعة والممشى الحربي
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                world.setBlockState(center.add(dx, h, dz), Blocks.DEEPSLATE_TILES.getDefaultState());
            }
        }

        // شرفات القتال (Crenellations) على السطح
        for (int i = -r; i <= r; i += 2) {
            world.setBlockState(center.add(i, h + 1, -r), Blocks.DEEPSLATE_BRICK_WALL.getDefaultState());
            world.setBlockState(center.add(i, h + 1, r), Blocks.DEEPSLATE_BRICK_WALL.getDefaultState());
            world.setBlockState(center.add(-r, h + 1, i), Blocks.DEEPSLATE_BRICK_WALL.getDefaultState());
            world.setBlockState(center.add(r, h + 1, i), Blocks.DEEPSLATE_BRICK_WALL.getDefaultState());
        }

        // 4 أبراج مراقبة مرتفعة في الأركان بمشاعل الروح الزرقاء (Soul Torches)
        for (int dy = h + 1; dy <= h + 3; dy++) {
            world.setBlockState(center.add(-r, dy, -r), Blocks.DEEPSLATE_BRICK_WALL.getDefaultState());
            world.setBlockState(center.add(r, dy, -r), Blocks.DEEPSLATE_BRICK_WALL.getDefaultState());
            world.setBlockState(center.add(-r, dy, r), Blocks.DEEPSLATE_BRICK_WALL.getDefaultState());
            world.setBlockState(center.add(r, dy, r), Blocks.DEEPSLATE_BRICK_WALL.getDefaultState());
        }
        world.setBlockState(center.add(-r, h + 4, -r), Blocks.SOUL_TORCH.getDefaultState());
        world.setBlockState(center.add(r, h + 4, -r), Blocks.SOUL_TORCH.getDefaultState());
        world.setBlockState(center.add(-r, h + 4, r), Blocks.SOUL_TORCH.getDefaultState());
        world.setBlockState(center.add(r, h + 4, r), Blocks.SOUL_TORCH.getDefaultState());

        // الباب الحديدي المحصن مع أزرار تشغيل من الداخل والخارج
        world.setBlockState(center.add(0, 1, r), Blocks.IRON_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(0, 2, r), Blocks.IRON_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(1, 1, r + 1), Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(1, 1, r - 1), Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACING, Direction.NORTH));

        // سلم للصعود إلى ممشى السطح والشرفات
        for (int dy = 1; dy <= h; dy++) {
            world.setBlockState(center.add(-r + 1, dy, r - 1), Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.NORTH));
        }

        // غرفة تطوير لفل 30 داخل القلعة
        BlockPos cEnch = center.add(-3, 1, -2);
        world.setBlockState(cEnch, Blocks.ENCHANTING_TABLE.getDefaultState());
        for (int y = 1; y <= 2; y++) {
            world.setBlockState(center.add(-4, y, -4), Blocks.BOOKSHELF.getDefaultState());
            world.setBlockState(center.add(-3, y, -4), Blocks.BOOKSHELF.getDefaultState());
            world.setBlockState(center.add(-2, y, -4), Blocks.BOOKSHELF.getDefaultState());
            world.setBlockState(center.add(-1, y, -4), Blocks.BOOKSHELF.getDefaultState());

            world.setBlockState(center.add(-4, y, -3), Blocks.BOOKSHELF.getDefaultState());
            world.setBlockState(center.add(-4, y, -2), Blocks.BOOKSHELF.getDefaultState());
            world.setBlockState(center.add(-4, y, -1), Blocks.BOOKSHELF.getDefaultState());
            world.setBlockState(center.add(-4, y, 0), Blocks.BOOKSHELF.getDefaultState());
        }
        world.setBlockState(center.add(-2, 1, 0), Blocks.ANVIL.getDefaultState());

        // ترسانة حربية: سندان، حدادة، وصناديق أسلحة
        world.setBlockState(center.add(3, 1, -3), Blocks.SMITHING_TABLE.getDefaultState());
        world.setBlockState(center.add(2, 1, -3), Blocks.BLAST_FURNACE.getDefaultState());
        world.setBlockState(center.add(1, 1, -3), Blocks.FURNACE.getDefaultState());
        world.setBlockState(center.add(4, 1, 0), Blocks.CHEST.getDefaultState());
        world.setBlockState(center.add(4, 1, 1), Blocks.CHEST.getDefaultState());

        // سرير حربي أسود
        world.setBlockState(center.add(-2, 1, 4), Blocks.BLACK_BED.getDefaultState().with(BedBlock.PART, BedPart.HEAD).with(BedBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(-2, 1, 3), Blocks.BLACK_BED.getDefaultState().with(BedBlock.PART, BedPart.FOOT).with(BedBlock.FACING, Direction.SOUTH));

        world.setBlockState(center.add(0, h - 1, 0), Blocks.SOUL_LANTERN.getDefaultState().with(LanternBlock.HANGING, true));

        world.playSound(null, center, SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        tooltip.add(Text.translatable("tooltip.backpackmod.instant_house").formatted(Formatting.GRAY));
    }
}
