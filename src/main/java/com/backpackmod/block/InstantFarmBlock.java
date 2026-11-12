package com.backpackmod.block;

import net.minecraft.block.*;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
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

    // 1. مزرعة قمح ومزروعات أوتوماتيكية 9x9 محروثة ومسقية ومزروعة بالكامل مع أسوار وصندوق للبذور
    private void buildCropFarm(World world, BlockPos center) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                BlockPos floor = center.add(dx, -1, dz);
                BlockPos crop = center.add(dx, 0, dz);

                // تفريغ الهواء فوق المزرعة
                world.setBlockState(crop, Blocks.AIR.getDefaultState());
                world.setBlockState(crop.up(), Blocks.AIR.getDefaultState());

                if (dx == 0 && dz == 0) {
                    // منبع ماء في المركز
                    world.setBlockState(floor, Blocks.WATER.getDefaultState());
                    world.setBlockState(crop, Blocks.WATER.getDefaultState());
                } else if (Math.abs(dx) == 4 || Math.abs(dz) == 4) {
                    // الأسوار المحيطة بالمزرعة
                    world.setBlockState(floor, Blocks.OAK_PLANKS.getDefaultState());
                    world.setBlockState(crop, Blocks.OAK_FENCE.getDefaultState());
                } else {
                    // تربة محروثة رطبة مع قمح مزروع
                    world.setBlockState(floor, Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, 7));
                    world.setBlockState(crop, Blocks.WHEAT.getDefaultState().with(CropBlock.AGE, 7)); // قمح مكتمل النمو جاهز للحصاد!
                }
            }
        }

        // بوابة الدخول وصندوق الحصاد
        world.setBlockState(center.add(0, 0, 4), Blocks.OAK_FENCE_GATE.getDefaultState());
        world.setBlockState(center.add(1, 0, 4), Blocks.CHEST.getDefaultState());
        world.setBlockState(center.add(0, 1, 0), Blocks.LANTERN.getDefaultState());

        world.playSound(null, center, SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    // 2. حظيرة حيوانات 7x7 مجهزة بأعلاف وقش ومحاطة بأسوار مع بقر وخراف
    private void buildAnimalFarm(World world, BlockPos center) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                BlockPos floor = center.add(dx, -1, dz);
                BlockPos air = center.add(dx, 0, dz);

                world.setBlockState(floor, Blocks.GRASS_BLOCK.getDefaultState());
                world.setBlockState(air, Blocks.AIR.getDefaultState());
                world.setBlockState(air.up(), Blocks.AIR.getDefaultState());

                // الأسوار
                if (Math.abs(dx) == 3 || Math.abs(dz) == 3) {
                    world.setBlockState(air, Blocks.OAK_FENCE.getDefaultState());
                }
            }
        }

        // قش علف وماء للحيوانات
        world.setBlockState(center.add(-2, 0, -2), Blocks.HAY_BLOCK.getDefaultState());
        world.setBlockState(center.add(-2, 0, -1), Blocks.HAY_BLOCK.getDefaultState());
        world.setBlockState(center.add(2, 0, -2), Blocks.CAULDRON.getDefaultState());

        // بوابة الحظيرة
        world.setBlockState(center.add(0, 0, 3), Blocks.OAK_FENCE_GATE.getDefaultState());

        // توليد زوج من الأبقار وزوج من الخراف تلقائياً
        CowEntity cow1 = EntityType.COW.create(world);
        if (cow1 != null) { cow1.refreshPositionAndAngles(center.getX() - 1, center.getY(), center.getZ(), 0, 0); world.spawnEntity(cow1); }
        CowEntity cow2 = EntityType.COW.create(world);
        if (cow2 != null) { cow2.refreshPositionAndAngles(center.getX() - 1, center.getY(), center.getZ() + 1, 0, 0); world.spawnEntity(cow2); }

        SheepEntity sheep1 = EntityType.SHEEP.create(world);
        if (sheep1 != null) { sheep1.refreshPositionAndAngles(center.getX() + 1, center.getY(), center.getZ(), 0, 0); world.spawnEntity(sheep1); }
        SheepEntity sheep2 = EntityType.SHEEP.create(world);
        if (sheep2 != null) { sheep2.refreshPositionAndAngles(center.getX() + 1, center.getY(), center.getZ() + 1, 0, 0); world.spawnEntity(sheep2); }

        world.playSound(null, center, SoundEvents.ENTITY_COW_AMBIENT, SoundCategory.NEUTRAL, 1.0F, 1.0F);
    }

    // 3. مصيدة وحوش (Mob Trap) مجهزة ببرج هبوط وهوبرات لجمع الدروبز وتلفيل الإكسبيريانس
    private void buildMobTrap(World world, BlockPos center) {
        // قمع تجميع في الأسفل مع صناديق
        world.setBlockState(center.add(0, -1, 0), Blocks.CHEST.getDefaultState());
        world.setBlockState(center, Blocks.HOPPER.getDefaultState());

        // جدران أنبوب الإسقاط العالي لقتل الوحوش وتركها بضربة واحدة
        for (int dy = 1; dy <= 22; dy++) {
            world.setBlockState(center.add(1, dy, 0), Blocks.COBBLESTONE.getDefaultState());
            world.setBlockState(center.add(-1, dy, 0), Blocks.COBBLESTONE.getDefaultState());
            world.setBlockState(center.add(0, dy, 1), Blocks.COBBLESTONE.getDefaultState());
            world.setBlockState(center.add(0, dy, -1), Blocks.COBBLESTONE.getDefaultState());
            world.setBlockState(center.add(0, dy, 0), Blocks.AIR.getDefaultState());
        }

        // غرفة التوليد العلوية المظلمة
        BlockPos top = center.up(23);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                world.setBlockState(top.add(dx, 0, dz), Blocks.COBBLESTONE.getDefaultState());
                world.setBlockState(top.add(dx, 3, dz), Blocks.COBBLESTONE.getDefaultState());
            }
        }

        world.playSound(null, center, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        switch (type) {
            case CROPS -> tooltip.add(Text.translatable("block.backpackmod.crop_farm.desc").formatted(Formatting.GREEN));
            case ANIMALS -> tooltip.add(Text.translatable("block.backpackmod.animal_farm.desc").formatted(Formatting.GOLD));
            case MOB_TRAP -> tooltip.add(Text.translatable("block.backpackmod.mob_trap.desc").formatted(Formatting.RED));
        }
        super.appendTooltip(stack, world, tooltip, options);
    }
}
