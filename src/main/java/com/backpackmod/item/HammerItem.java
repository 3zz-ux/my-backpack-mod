package com.backpackmod.item;

import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HammerItem extends PickaxeItem {
    private static final ThreadLocal<Boolean> IS_MINING = ThreadLocal.withInitial(() -> false);
    private final int size; // 3 = 3x3, 4 = 4x4, 5 = 5x5, 6 = 6x6, 7 = 7x7

    public HammerItem(ToolMaterial material, int size, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings.maxCount(1));
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    // السماح بتطوير الهامر في طاولة التطوير (Enchanting Table) والأنفيل (Anvil) كالبيكاكس تماماً
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantability() {
        // قابلية تطوير عالية للحصول على أقوى التطويرات (Efficiency, Fortune, Silk Touch, إلخ)
        return Math.max(super.getEnchantability(), 15);
    }

    // متانة لانهائية مع إمكانية قبول تطويرات البيكاكس
    @Override
    public boolean isDamageable() {
        return true;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return false; // إخفاء شريط الضرر نهائياً لأن الهامر لا ينكسر
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        stack.setDamage(0); // تصفير أي ضرر فوراً للحفاظ على المتانة الأبدية
        if (!world.isClient && miner instanceof ServerPlayerEntity player) {
            if (this.isSuitableFor(state) && !IS_MINING.get()) {
                mineArea(world, player, pos, stack);
            }
        }
        return true;
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.setDamage(0); // تصفير أي ضرر عند ضرب الوحوش
        return true;
    }

    private void mineArea(World world, ServerPlayerEntity player, BlockPos centerPos, ItemStack hammerStack) {
        IS_MINING.set(true);
        try {
            List<BlockPos> blocks = getAreaPositions(player, centerPos, size);
            for (BlockPos targetPos : blocks) {
                if (targetPos.equals(centerPos)) {
                    continue;
                }

                BlockState targetState = world.getBlockState(targetPos);
                if (targetState.getHardness(world, targetPos) >= 0 && this.isSuitableFor(targetState)) {
                    player.interactionManager.tryBreakBlock(targetPos);
                }
            }
        } finally {
            IS_MINING.set(false);
        }
    }

    private static List<BlockPos> getAreaPositions(PlayerEntity player, BlockPos center, int size) {
        List<BlockPos> positions = new ArrayList<>();
        float pitch = player.getPitch();
        Direction facing = player.getHorizontalFacing();

        int minOffset = -(size / 2);
        int maxOffset = minOffset + size - 1;

        int minX = 0, maxX = 0, minY = 0, maxY = 0, minZ = 0, maxZ = 0;

        if (pitch > 55.0F || pitch < -55.0F) {
            minX = minOffset; maxX = maxOffset;
            minZ = minOffset; maxZ = maxOffset;
        } else if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            minX = minOffset; maxX = maxOffset;
            minY = minOffset; maxY = maxOffset;
        } else {
            minZ = minOffset; maxZ = maxOffset;
            minY = minOffset; maxY = maxOffset;
        }

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    positions.add(center.add(x, y, z));
                }
            }
        }

        return positions;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.backpackmod.hammer.area", size, size).formatted(Formatting.GOLD));
        tooltip.add(Text.translatable("item.backpackmod.hammer.unbreakable").formatted(Formatting.AQUA));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
