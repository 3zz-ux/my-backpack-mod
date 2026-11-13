package com.backpackmod.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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

    @Unique
    private static final ThreadLocal<Integer> SAVED_TOOL_DAMAGE = ThreadLocal.withInitial(() -> -1);

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

        // حفظ متانة الأداة لمنع انكسارها أو استهلاك Durability عند التكسير بالشفت
        if ((heldItem.getItem() instanceof AxeItem && originalState.isIn(BlockTags.LOGS))
                || (heldItem.getItem() instanceof PickaxeItem && isOreBlock(originalState))) {
            SAVED_TOOL_DAMAGE.set(heldItem.getDamage());
        }

        // 1. أي فأس + شفت + خشب = كسر الشجرة بالكامل فقط إذا كانت شجرة طبيعية بأوراقها (حماية لبيوت الخشب)
        if (heldItem.getItem() instanceof AxeItem && originalState.isIn(BlockTags.LOGS)) {
            if (isNaturalTree(pos)) {
                chainBreakBlocks(pos, originalState.getBlock(), 128, true);
            }
        }
        // 2. أي بيكاكس + شفت + أور = كسر عرق الأورات المتصل بالكامل
        else if (heldItem.getItem() instanceof PickaxeItem && isOreBlock(originalState)) {
            chainBreakBlocks(pos, originalState.getBlock(), 64, false);
        }
    }

    @Inject(method = "tryBreakBlock", at = @At("TAIL"))
    private void onTryBreakBlockTail(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (IS_CHAIN_BREAKING.get()) {
            return;
        }

        int saved = SAVED_TOOL_DAMAGE.get();
        if (saved >= 0) {
            if (!player.getMainHandStack().isEmpty()) {
                player.getMainHandStack().setDamage(saved);
            }
            SAVED_TOOL_DAMAGE.set(-1);
        }
    }

    // فحص لمعرفة إذا كانت البلوكة من أنواع الأورات (فحم، حديد، ألماس، ذهب، إلخ)
    @Unique
    private static boolean isOreBlock(BlockState state) {
        return state.isIn(BlockTags.COAL_ORES)
                || state.isIn(BlockTags.IRON_ORES)
                || state.isIn(BlockTags.COPPER_ORES)
                || state.isIn(BlockTags.GOLD_ORES)
                || state.isIn(BlockTags.REDSTONE_ORES)
                || state.isIn(BlockTags.LAPIS_ORES)
                || state.isIn(BlockTags.DIAMOND_ORES)
                || state.isIn(BlockTags.EMERALD_ORES)
                || state.isOf(Blocks.NETHER_QUARTZ_ORE)
                || state.isOf(Blocks.NETHER_GOLD_ORE)
                || state.isOf(Blocks.ANCIENT_DEBRIS);
    }

    // مطابقة الأور مع النسخة العادية والديب سليت لنفس العرق
    @Unique
    private static boolean isMatchingOre(BlockState state, Block target) {
        if (state.isOf(target)) return true;
        if (target == Blocks.DIAMOND_ORE && state.isOf(Blocks.DEEPSLATE_DIAMOND_ORE)) return true;
        if (target == Blocks.DEEPSLATE_DIAMOND_ORE && state.isOf(Blocks.DIAMOND_ORE)) return true;
        if (target == Blocks.IRON_ORE && state.isOf(Blocks.DEEPSLATE_IRON_ORE)) return true;
        if (target == Blocks.DEEPSLATE_IRON_ORE && state.isOf(Blocks.IRON_ORE)) return true;
        if (target == Blocks.COAL_ORE && state.isOf(Blocks.DEEPSLATE_COAL_ORE)) return true;
        if (target == Blocks.DEEPSLATE_COAL_ORE && state.isOf(Blocks.COAL_ORE)) return true;
        if (target == Blocks.GOLD_ORE && state.isOf(Blocks.DEEPSLATE_GOLD_ORE)) return true;
        if (target == Blocks.DEEPSLATE_GOLD_ORE && state.isOf(Blocks.GOLD_ORE)) return true;
        if (target == Blocks.COPPER_ORE && state.isOf(Blocks.DEEPSLATE_COPPER_ORE)) return true;
        if (target == Blocks.DEEPSLATE_COPPER_ORE && state.isOf(Blocks.COPPER_ORE)) return true;
        if (target == Blocks.REDSTONE_ORE && state.isOf(Blocks.DEEPSLATE_REDSTONE_ORE)) return true;
        if (target == Blocks.DEEPSLATE_REDSTONE_ORE && state.isOf(Blocks.REDSTONE_ORE)) return true;
        if (target == Blocks.LAPIS_ORE && state.isOf(Blocks.DEEPSLATE_LAPIS_ORE)) return true;
        if (target == Blocks.DEEPSLATE_LAPIS_ORE && state.isOf(Blocks.LAPIS_ORE)) return true;
        if (target == Blocks.EMERALD_ORE && state.isOf(Blocks.DEEPSLATE_EMERALD_ORE)) return true;
        if (target == Blocks.DEEPSLATE_EMERALD_ORE && state.isOf(Blocks.EMERALD_ORE)) return true;
        return false;
    }

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
                        return true;
                    }
                }
            }
        }
        return false;
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
                                boolean matches = isTree ? state.isIn(BlockTags.LOGS) : isMatchingOre(state, targetBlock);

                                if (matches) {
                                    queue.add(neighbor);
                                    ((ServerPlayerInteractionManager)(Object)this).tryBreakBlock(neighbor);
                                    brokenCount++;

                                    // الحفاظ الدائم على متانة الأداة عند كسر كل بلوكة بالسلسلة
                                    int saved = SAVED_TOOL_DAMAGE.get();
                                    if (saved >= 0 && !player.getMainHandStack().isEmpty()) {
                                        player.getMainHandStack().setDamage(saved);
                                    }

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
