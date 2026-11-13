package com.backpackmod.block;

import net.minecraft.block.*;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
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
                case WOODEN -> buildWoodenHouse(world, pos);
                case STONE -> buildStoneHouse(world, pos);
                case CASTLE -> buildCastle(world, pos);
            }
        }
    }

    // 1. كوخ خشبي ريفي فخم (مقتبس من الصور) 7x7 مع سقف قرميد مدرج، فوانيس، شبابيك، وفرش كامل
    private void buildWoodenHouse(World world, BlockPos center) {
        // تفريغ وتأسيس الأرضية
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                world.setBlockState(center.add(dx, -1, dz), Blocks.COBBLESTONE.getDefaultState());
                world.setBlockState(center.add(dx, 0, dz), Blocks.SPRUCE_PLANKS.getDefaultState());
                for (int dy = 1; dy <= 7; dy++) {
                    world.setBlockState(center.add(dx, dy, dz), Blocks.AIR.getDefaultState());
                }
            }
        }

        // بناء الجدران وأعمدة الخشب الدائرية (Stripped Oak Logs)
        for (int dy = 1; dy <= 4; dy++) {
            for (int i = -3; i <= 3; i++) {
                world.setBlockState(center.add(i, dy, -3), Blocks.OAK_PLANKS.getDefaultState());
                world.setBlockState(center.add(i, dy, 3), Blocks.OAK_PLANKS.getDefaultState());
                world.setBlockState(center.add(-3, dy, i), Blocks.OAK_PLANKS.getDefaultState());
                world.setBlockState(center.add(3, dy, i), Blocks.OAK_PLANKS.getDefaultState());
            }
            // أعمدة الزوايا
            world.setBlockState(center.add(-3, dy, -3), Blocks.STRIPPED_OAK_LOG.getDefaultState());
            world.setBlockState(center.add(3, dy, -3), Blocks.STRIPPED_OAK_LOG.getDefaultState());
            world.setBlockState(center.add(-3, dy, 3), Blocks.STRIPPED_OAK_LOG.getDefaultState());
            world.setBlockState(center.add(3, dy, 3), Blocks.STRIPPED_OAK_LOG.getDefaultState());
        }

        // شبابيك زجاجية واسعة
        world.setBlockState(center.add(-1, 2, -3), Blocks.GLASS_PANE.getDefaultState());
        world.setBlockState(center.add(0, 2, -3), Blocks.GLASS_PANE.getDefaultState());
        world.setBlockState(center.add(1, 2, -3), Blocks.GLASS_PANE.getDefaultState());

        world.setBlockState(center.add(-3, 2, 0), Blocks.GLASS_PANE.getDefaultState());
        world.setBlockState(center.add(3, 2, 0), Blocks.GLASS_PANE.getDefaultState());

        // الباب الأمامي ودرج المدخل
        world.setBlockState(center.add(0, 1, 3), Blocks.SPRUCE_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(0, 2, 3), Blocks.SPRUCE_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(0, 0, 4), Blocks.COBBLESTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH));

        // سقف جمالوني مدرج (A-Frame Roof) من درجات خشب الصنوبر
        for (int dz = -3; dz <= 3; dz++) {
            world.setBlockState(center.add(-4, 4, dz), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST));
            world.setBlockState(center.add(-3, 5, dz), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST));
            world.setBlockState(center.add(-2, 6, dz), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST));

            world.setBlockState(center.add(4, 4, dz), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST));
            world.setBlockState(center.add(3, 5, dz), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST));
            world.setBlockState(center.add(2, 6, dz), Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST));

            world.setBlockState(center.add(-1, 6, dz), Blocks.SPRUCE_SLAB.getDefaultState());
            world.setBlockState(center.add(0, 7, dz), Blocks.SPRUCE_SLAB.getDefaultState());
            world.setBlockState(center.add(1, 6, dz), Blocks.SPRUCE_SLAB.getDefaultState());
        }

        // مدخنة مع دخان طبيعي دافئ (Campfire)
        world.setBlockState(center.add(2, 5, -2), Blocks.BRICKS.getDefaultState());
        world.setBlockState(center.add(2, 6, -2), Blocks.BRICKS.getDefaultState());
        world.setBlockState(center.add(2, 7, -2), Blocks.CAMPFIRE.getDefaultState().with(CampfireBlock.LIT, true));

        // أثاث الكوخ الداخلي (سرير مزدوج، صناديق، أفران، طاولة صنع، مكتبة وفوانيس)
        world.setBlockState(center.add(-2, 1, -2), Blocks.RED_BED.getDefaultState().with(BedBlock.PART, BedPart.HEAD).with(BedBlock.FACING, Direction.NORTH));
        world.setBlockState(center.add(-2, 1, -1), Blocks.RED_BED.getDefaultState().with(BedBlock.PART, BedPart.FOOT).with(BedBlock.FACING, Direction.NORTH));
        world.setBlockState(center.add(-1, 1, -2), Blocks.RED_BED.getDefaultState().with(BedBlock.PART, BedPart.HEAD).with(BedBlock.FACING, Direction.NORTH));
        world.setBlockState(center.add(-1, 1, -1), Blocks.RED_BED.getDefaultState().with(BedBlock.PART, BedPart.FOOT).with(BedBlock.FACING, Direction.NORTH));

        world.setBlockState(center.add(2, 1, -2), Blocks.CHEST.getDefaultState());
        world.setBlockState(center.add(2, 1, -1), Blocks.CHEST.getDefaultState());
        world.setBlockState(center.add(2, 1, 0), Blocks.CRAFTING_TABLE.getDefaultState());
        world.setBlockState(center.add(2, 1, 1), Blocks.FURNACE.getDefaultState());
        world.setBlockState(center.add(2, 1, 2), Blocks.BLAST_FURNACE.getDefaultState());

        world.setBlockState(center.add(-2, 1, 1), Blocks.BOOKSHELF.getDefaultState());
        world.setBlockState(center.add(-2, 1, 2), Blocks.BOOKSHELF.getDefaultState());

        // فانوس مضيء معلق في السقف
        world.setBlockState(center.add(0, 5, 0), Blocks.LANTERN.getDefaultState().with(LanternBlock.HANGING, true));

        world.playSound(null, center, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    // 2. منزل حجري كبير وراقي 9x9 (مقتبس من صور المنازل الحجرية الكبيرة)
    private void buildStoneHouse(World world, BlockPos center) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                world.setBlockState(center.add(dx, -1, dz), Blocks.POLISHED_ANDESITE.getDefaultState());
                world.setBlockState(center.add(dx, 0, dz), Blocks.SMOOTH_STONE.getDefaultState());
                for (int dy = 1; dy <= 8; dy++) {
                    world.setBlockState(center.add(dx, dy, dz), Blocks.AIR.getDefaultState());
                }
            }
        }

        // جدران طوب حجري مع أعمدة مزخرفة
        for (int dy = 1; dy <= 5; dy++) {
            for (int i = -4; i <= 4; i++) {
                world.setBlockState(center.add(i, dy, -4), Blocks.STONE_BRICKS.getDefaultState());
                world.setBlockState(center.add(i, dy, 4), Blocks.STONE_BRICKS.getDefaultState());
                world.setBlockState(center.add(-4, dy, i), Blocks.STONE_BRICKS.getDefaultState());
                world.setBlockState(center.add(4, dy, i), Blocks.STONE_BRICKS.getDefaultState());
            }
            world.setBlockState(center.add(-4, dy, -4), Blocks.CHISELED_STONE_BRICKS.getDefaultState());
            world.setBlockState(center.add(4, dy, -4), Blocks.CHISELED_STONE_BRICKS.getDefaultState());
            world.setBlockState(center.add(-4, dy, 4), Blocks.CHISELED_STONE_BRICKS.getDefaultState());
            world.setBlockState(center.add(4, dy, 4), Blocks.CHISELED_STONE_BRICKS.getDefaultState());
        }

        // شبابيك كبيرة
        for (int dx = -2; dx <= 2; dx++) {
            world.setBlockState(center.add(dx, 2, -4), Blocks.GLASS_PANE.getDefaultState());
            world.setBlockState(center.add(dx, 3, -4), Blocks.GLASS_PANE.getDefaultState());
        }

        // الباب الأمامي
        world.setBlockState(center.add(0, 1, 4), Blocks.DARK_OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(0, 2, 4), Blocks.DARK_OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(0, 0, 5), Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH));

        // سقف قرميد مدرج من الحجر الأسود المصفح
        for (int dz = -4; dz <= 4; dz++) {
            for (int step = 0; step <= 3; step++) {
                world.setBlockState(center.add(-5 + step, 5 + step, dz), Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST));
                world.setBlockState(center.add(5 - step, 5 + step, dz), Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST));
            }
            world.setBlockState(center.add(0, 8, dz), Blocks.STONE_BRICK_SLAB.getDefaultState());
        }

        // مدخنة رئيسية
        world.setBlockState(center.add(3, 7, -2), Blocks.STONE_BRICKS.getDefaultState());
        world.setBlockState(center.add(3, 8, -2), Blocks.STONE_BRICKS.getDefaultState());
        world.setBlockState(center.add(3, 9, -2), Blocks.CAMPFIRE.getDefaultState().with(CampfireBlock.LIT, true));

        // الفرش الداخلي الفاخر
        world.setBlockState(center.add(-3, 1, -3), Blocks.BLUE_BED.getDefaultState().with(BedBlock.PART, BedPart.HEAD).with(BedBlock.FACING, Direction.NORTH));
        world.setBlockState(center.add(-3, 1, -2), Blocks.BLUE_BED.getDefaultState().with(BedBlock.PART, BedPart.FOOT).with(BedBlock.FACING, Direction.NORTH));

        world.setBlockState(center.add(3, 1, -3), Blocks.ENCHANTING_TABLE.getDefaultState());
        world.setBlockState(center.add(3, 1, -2), Blocks.BOOKSHELF.getDefaultState());
        world.setBlockState(center.add(3, 2, -2), Blocks.BOOKSHELF.getDefaultState());

        world.setBlockState(center.add(3, 1, 2), Blocks.ANVIL.getDefaultState());
        world.setBlockState(center.add(3, 1, 3), Blocks.CHEST.getDefaultState());
        world.setBlockState(center.add(2, 1, 3), Blocks.CHEST.getDefaultState());

        // سجادة حمراء وفاخرة بالمنتصف
        for (int dz = -1; dz <= 2; dz++) {
            world.setBlockState(center.add(0, 1, dz), Blocks.RED_CARPET.getDefaultState());
        }

        world.setBlockState(center.add(0, 6, 0), Blocks.LANTERN.getDefaultState().with(LanternBlock.HANGING, true));

        world.playSound(null, center, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    // 3. قلعة حصينة متكاملة 9x9 مع شرفات وأبراج مراقبة وباب حديدي
    private void buildCastle(World world, BlockPos center) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                world.setBlockState(center.add(dx, -1, dz), Blocks.DEEPSLATE_TILES.getDefaultState());
                world.setBlockState(center.add(dx, 0, dz), Blocks.POLISHED_DEEPSLATE.getDefaultState());
                for (int dy = 1; dy <= 8; dy++) {
                    world.setBlockState(center.add(dx, dy, dz), Blocks.AIR.getDefaultState());
                }
            }
        }

        for (int dy = 1; dy <= 6; dy++) {
            for (int i = -4; i <= 4; i++) {
                world.setBlockState(center.add(i, dy, -4), Blocks.DEEPSLATE_BRICKS.getDefaultState());
                world.setBlockState(center.add(i, dy, 4), Blocks.DEEPSLATE_BRICKS.getDefaultState());
                world.setBlockState(center.add(-4, dy, i), Blocks.DEEPSLATE_BRICKS.getDefaultState());
                world.setBlockState(center.add(4, dy, i), Blocks.DEEPSLATE_BRICKS.getDefaultState());
            }
            // أبراج الزوايا المصفحة
            world.setBlockState(center.add(-4, dy, -4), Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState());
            world.setBlockState(center.add(4, dy, -4), Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState());
            world.setBlockState(center.add(-4, dy, 4), Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState());
            world.setBlockState(center.add(4, dy, 4), Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState());
        }

        // سقف القلعة (منصة مراقبة مع شرفات قتالية Crenellations)
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                world.setBlockState(center.add(dx, 6, dz), Blocks.DEEPSLATE_TILES.getDefaultState());
            }
        }

        for (int i = -4; i <= 4; i += 2) {
            world.setBlockState(center.add(i, 7, -4), Blocks.DEEPSLATE_BRICK_WALL.getDefaultState());
            world.setBlockState(center.add(i, 7, 4), Blocks.DEEPSLATE_BRICK_WALL.getDefaultState());
            world.setBlockState(center.add(-4, 7, i), Blocks.DEEPSLATE_BRICK_WALL.getDefaultState());
            world.setBlockState(center.add(4, 7, i), Blocks.DEEPSLATE_BRICK_WALL.getDefaultState());
        }

        // أبراج الزوايا أعلى مع مشاعل حراسة
        world.setBlockState(center.add(-4, 7, -4), Blocks.DEEPSLATE_BRICK_WALL.getDefaultState());
        world.setBlockState(center.add(-4, 8, -4), Blocks.SOUL_TORCH.getDefaultState());
        world.setBlockState(center.add(4, 7, -4), Blocks.DEEPSLATE_BRICK_WALL.getDefaultState());
        world.setBlockState(center.add(4, 8, -4), Blocks.SOUL_TORCH.getDefaultState());
        world.setBlockState(center.add(-4, 7, 4), Blocks.DEEPSLATE_BRICK_WALL.getDefaultState());
        world.setBlockState(center.add(-4, 8, 4), Blocks.SOUL_TORCH.getDefaultState());
        world.setBlockState(center.add(4, 7, 4), Blocks.DEEPSLATE_BRICK_WALL.getDefaultState());
        world.setBlockState(center.add(4, 8, 4), Blocks.SOUL_TORCH.getDefaultState());

        // بوابة حديدية محصنة مع أزرار حجرية
        world.setBlockState(center.add(0, 1, 4), Blocks.IRON_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(0, 2, 4), Blocks.IRON_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(1, 1, 5), Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(1, 1, 3), Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACING, Direction.NORTH));

        // سلم للصعود إلى شرفة القلعة
        for (int dy = 1; dy <= 6; dy++) {
            world.setBlockState(center.add(-3, dy, 3), Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.NORTH));
        }

        // أثاث الحصن الحربي
        world.setBlockState(center.add(-3, 1, -3), Blocks.BLACK_BED.getDefaultState().with(BedBlock.PART, BedPart.HEAD).with(BedBlock.FACING, Direction.NORTH));
        world.setBlockState(center.add(-3, 1, -2), Blocks.BLACK_BED.getDefaultState().with(BedBlock.PART, BedPart.FOOT).with(BedBlock.FACING, Direction.NORTH));
        world.setBlockState(center.add(3, 1, -3), Blocks.ANVIL.getDefaultState());
        world.setBlockState(center.add(3, 1, -2), Blocks.SMITHING_TABLE.getDefaultState());
        world.setBlockState(center.add(3, 1, -1), Blocks.GRINDSTONE.getDefaultState());
        world.setBlockState(center.add(3, 1, 1), Blocks.CHEST.getDefaultState());
        world.setBlockState(center.add(3, 1, 2), Blocks.CHEST.getDefaultState());

        world.setBlockState(center.add(0, 5, 0), Blocks.LANTERN.getDefaultState().with(LanternBlock.HANGING, true));

        world.playSound(null, center, SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        tooltip.add(Text.translatable("tooltip.backpackmod.instant_house").formatted(Formatting.GRAY));
    }
}
