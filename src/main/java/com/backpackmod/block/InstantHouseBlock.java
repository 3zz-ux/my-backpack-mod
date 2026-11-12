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

    private void buildWoodenHouse(World world, BlockPos center) {
        // كوخ خشبي 5x5
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                world.setBlockState(center.add(dx, -1, dz), Blocks.OAK_PLANKS.getDefaultState());
                for (int dy = 0; dy <= 3; dy++) {
                    world.setBlockState(center.add(dx, dy, dz), Blocks.AIR.getDefaultState());
                }
                world.setBlockState(center.add(dx, 4, dz), Blocks.OAK_PLANKS.getDefaultState());
            }
        }

        for (int dy = 0; dy <= 3; dy++) {
            for (int i = -2; i <= 2; i++) {
                world.setBlockState(center.add(i, dy, -2), Blocks.OAK_PLANKS.getDefaultState());
                world.setBlockState(center.add(i, dy, 2), Blocks.OAK_PLANKS.getDefaultState());
                world.setBlockState(center.add(-2, dy, i), Blocks.OAK_PLANKS.getDefaultState());
                world.setBlockState(center.add(2, dy, i), Blocks.OAK_PLANKS.getDefaultState());
            }
            world.setBlockState(center.add(-2, dy, -2), Blocks.OAK_LOG.getDefaultState());
            world.setBlockState(center.add(2, dy, -2), Blocks.OAK_LOG.getDefaultState());
            world.setBlockState(center.add(-2, dy, 2), Blocks.OAK_LOG.getDefaultState());
            world.setBlockState(center.add(2, dy, 2), Blocks.OAK_LOG.getDefaultState());
        }

        world.setBlockState(center.add(0, 1, -2), Blocks.GLASS.getDefaultState());
        world.setBlockState(center.add(-2, 1, 0), Blocks.GLASS.getDefaultState());
        world.setBlockState(center.add(2, 1, 0), Blocks.GLASS.getDefaultState());

        world.setBlockState(center.add(0, 0, 2), Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(0, 1, 2), Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.SOUTH));

        world.setBlockState(center.add(-1, 0, -1), Blocks.RED_BED.getDefaultState().with(BedBlock.PART, BedPart.HEAD).with(BedBlock.FACING, Direction.NORTH));
        world.setBlockState(center.add(-1, 0, 0), Blocks.RED_BED.getDefaultState().with(BedBlock.PART, BedPart.FOOT).with(BedBlock.FACING, Direction.NORTH));
        world.setBlockState(center.add(1, 0, -1), Blocks.CHEST.getDefaultState());
        world.setBlockState(center.add(1, 0, 0), Blocks.CRAFTING_TABLE.getDefaultState());
        world.setBlockState(center.add(1, 0, 1), Blocks.FURNACE.getDefaultState());
        world.setBlockState(center.add(0, 2, -1), Blocks.TORCH.getDefaultState());

        world.playSound(null, center, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    private void buildStoneHouse(World world, BlockPos center) {
        // منزل حجري 7x7 مصفح مع سقف مدرج
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                world.setBlockState(center.add(dx, -1, dz), Blocks.SMOOTH_STONE.getDefaultState());
                for (int dy = 0; dy <= 4; dy++) {
                    world.setBlockState(center.add(dx, dy, dz), Blocks.AIR.getDefaultState());
                }
                world.setBlockState(center.add(dx, 4, dz), Blocks.STONE_BRICKS.getDefaultState());
            }
        }

        for (int dy = 0; dy <= 3; dy++) {
            for (int i = -3; i <= 3; i++) {
                world.setBlockState(center.add(i, dy, -3), Blocks.STONE_BRICKS.getDefaultState());
                world.setBlockState(center.add(i, dy, 3), Blocks.STONE_BRICKS.getDefaultState());
                world.setBlockState(center.add(-3, dy, i), Blocks.STONE_BRICKS.getDefaultState());
                world.setBlockState(center.add(3, dy, i), Blocks.STONE_BRICKS.getDefaultState());
            }
            world.setBlockState(center.add(-3, dy, -3), Blocks.CHISELED_STONE_BRICKS.getDefaultState());
            world.setBlockState(center.add(3, dy, -3), Blocks.CHISELED_STONE_BRICKS.getDefaultState());
            world.setBlockState(center.add(-3, dy, 3), Blocks.CHISELED_STONE_BRICKS.getDefaultState());
            world.setBlockState(center.add(3, dy, 3), Blocks.CHISELED_STONE_BRICKS.getDefaultState());
        }

        world.setBlockState(center.add(0, 1, -3), Blocks.GLASS.getDefaultState());
        world.setBlockState(center.add(-3, 1, 0), Blocks.GLASS.getDefaultState());
        world.setBlockState(center.add(3, 1, 0), Blocks.GLASS.getDefaultState());

        world.setBlockState(center.add(0, 0, 3), Blocks.IRON_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(0, 1, 3), Blocks.IRON_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(1, 0, 3), Blocks.STONE_BUTTON.getDefaultState());

        world.setBlockState(center.add(-2, 0, -2), Blocks.BLUE_BED.getDefaultState().with(BedBlock.PART, BedPart.HEAD).with(BedBlock.FACING, Direction.NORTH));
        world.setBlockState(center.add(-2, 0, -1), Blocks.BLUE_BED.getDefaultState().with(BedBlock.PART, BedPart.FOOT).with(BedBlock.FACING, Direction.NORTH));
        world.setBlockState(center.add(2, 0, -2), Blocks.CHEST.getDefaultState());
        world.setBlockState(center.add(2, 0, -1), Blocks.CHEST.getDefaultState());
        world.setBlockState(center.add(2, 0, 0), Blocks.BLAST_FURNACE.getDefaultState());
        world.setBlockState(center.add(2, 0, 1), Blocks.CRAFTING_TABLE.getDefaultState());
        world.setBlockState(center.add(0, 2, 0), Blocks.LANTERN.getDefaultState());

        world.playSound(null, center, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    private void buildCastle(World world, BlockPos center) {
        // قلعة حجرية مع شرفات وأبراج 7x7 بارتفاع 6 بلوكات
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                world.setBlockState(center.add(dx, -1, dz), Blocks.POLISHED_DEEPSLATE.getDefaultState());
                for (int dy = 0; dy <= 5; dy++) {
                    world.setBlockState(center.add(dx, dy, dz), Blocks.AIR.getDefaultState());
                }
                world.setBlockState(center.add(dx, 5, dz), Blocks.COBBLESTONE.getDefaultState());
            }
        }

        for (int dy = 0; dy <= 5; dy++) {
            for (int i = -3; i <= 3; i++) {
                world.setBlockState(center.add(i, dy, -3), Blocks.DEEPSLATE_BRICKS.getDefaultState());
                world.setBlockState(center.add(i, dy, 3), Blocks.DEEPSLATE_BRICKS.getDefaultState());
                world.setBlockState(center.add(-3, dy, i), Blocks.DEEPSLATE_BRICKS.getDefaultState());
                world.setBlockState(center.add(3, dy, i), Blocks.DEEPSLATE_BRICKS.getDefaultState());
            }
        }

        // شرفات القلعة فوق السطح (Battlements)
        for (int i = -3; i <= 3; i += 2) {
            world.setBlockState(center.add(i, 6, -3), Blocks.COBBLESTONE_WALL.getDefaultState());
            world.setBlockState(center.add(i, 6, 3), Blocks.COBBLESTONE_WALL.getDefaultState());
            world.setBlockState(center.add(-3, 6, i), Blocks.COBBLESTONE_WALL.getDefaultState());
            world.setBlockState(center.add(3, 6, i), Blocks.COBBLESTONE_WALL.getDefaultState());
        }

        world.setBlockState(center.add(0, 0, 3), Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.SOUTH));
        world.setBlockState(center.add(0, 1, 3), Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.SOUTH));

        world.setBlockState(center.add(-2, 0, -2), Blocks.BLACK_BED.getDefaultState().with(BedBlock.PART, BedPart.HEAD).with(BedBlock.FACING, Direction.NORTH));
        world.setBlockState(center.add(-2, 0, -1), Blocks.BLACK_BED.getDefaultState().with(BedBlock.PART, BedPart.FOOT).with(BedBlock.FACING, Direction.NORTH));
        world.setBlockState(center.add(2, 0, -2), Blocks.BARREL.getDefaultState());
        world.setBlockState(center.add(2, 0, -1), Blocks.BARREL.getDefaultState());
        world.setBlockState(center.add(2, 0, 0), Blocks.ANVIL.getDefaultState());
        world.setBlockState(center.add(0, 3, 0), Blocks.SOUL_LANTERN.getDefaultState());

        world.playSound(null, center, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        switch (type) {
            case WOODEN -> tooltip.add(Text.translatable("block.backpackmod.wooden_house.desc").formatted(Formatting.GOLD));
            case STONE -> tooltip.add(Text.translatable("block.backpackmod.stone_house.desc").formatted(Formatting.GRAY));
            case CASTLE -> tooltip.add(Text.translatable("block.backpackmod.mini_castle.desc").formatted(Formatting.DARK_PURPLE));
        }
        super.appendTooltip(stack, world, tooltip, options);
    }
}
