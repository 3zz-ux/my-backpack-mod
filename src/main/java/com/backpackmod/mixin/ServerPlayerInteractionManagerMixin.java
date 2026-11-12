package com.backpackmod.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {
    @Shadow @Final protected ServerPlayerEntity player;
    @Shadow protected ServerWorld world;

    @Unique
    private static final ThreadLocal<Boolean> IS_CHAIN_BREAKING = ThreadLocal.withInitial(() -> false);

    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void onTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (IS_CHAIN_BREAKING.get()) {
            return;
        }

        // فقط عند الضغط والاستمرار على زر الشفت (Sneak)
        if (!player.isSneaking()) {
            return;
        }

        ItemStack heldItem = player.getMainHandStack();
        BlockState originalState = world.getBlockState(pos);

        // 1. أي فأس + شفت + خشب = كسر الشجرة فقط إذا كانت شجرة طبيعية بأوراقها (حماية لبيوت الخشب)
        if (heldItem.getItem() instanceof AxeItem && originalState.isIn(BlockTags.LOGS)) {
            if (isNaturalTree(pos)) {
                chainBreakBlocks(pos, originalState.getBlock(), 128, true);
            }
        }
        // 2. أي بيكاكس + شفت + أور (Ore) = كسر عرق الأورات المتصل بالكامل
        else if (heldItem.getItem() instanceof PickaxeItem && originalState.isIn(BlockTags.ORES)) {
            chainBreakBlocks(pos, originalState.getBlock(), 64, false);
        }
    }

    // فحص ذكي: التأكد أن الخشب تابع لشجرة حقيقية بوجود أوراق شجر (Leaves) حوله، لحماية بيوت اللاعب الخشبية من التكسير بالخطأ
    @Unique
    private boolean isNaturalTree(BlockPos startPos) {
        for (int dy = 0; dy <= 30; dy++) {
            BlockPos checkPos = startPos.up(dy);
            BlockState state = world.getBlockState(checkPos);

            if (!state.isIn(BlockTags.LOGS) && !state.isIn(BlockTags.LEAVES) && !state.isAir()) {
                break;
            }

            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    if (world.getBlockState(checkPos.add(dx, 0, dz)).isIn(BlockTags.LEAVES)) {
                        return true; // وجدت أوراق شجر: هذه شجرة طبيعية!
                    }
                }
            }
        }
        return false; // لا توجد أوراق: هذا بيت خشب أو بناء ولن يتم تكسيره!
    }

    @Unique
    private void chainBreakBlocks(BlockPos startPos, Block targetBlock, int maxBlocks, boolean isTree) {
        IS_CHAIN_BREAKING.set(true);
        try {
            Queue<BlockPos> queue = new ArrayDeque<>();
            Set<BlockPos> visited = new HashSet<>();

            queue.add(startPos);
            visited.add(startPos);

            int brokenCount = 0;

            while (!queue.isEmpty() && brokenCount < maxBlocks) {
                BlockPos current = queue.poll();

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = (isTree ? -1 : -1); dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;

                            BlockPos neighbor = current.add(dx, dy, dz);
                            if (visited.add(neighbor)) {
                                BlockState state = world.getBlockState(neighbor);
                                boolean matches = isTree ? state.isIn(BlockTags.LOGS) : (state.isOf(targetBlock) || state.isIn(BlockTags.ORES));

                                if (matches) {
                                    queue.add(neighbor);
                                    ((ServerPlayerInteractionManager)(Object)this).tryBreakBlock(neighbor);
                                    brokenCount++;
                                    if (player.getMainHandStack().isEmpty()) {
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            IS_CHAIN_BREAKING.set(false);
        }
    }
}
