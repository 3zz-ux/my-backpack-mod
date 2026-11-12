package com.backpackmod.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AutoFeedUpgradeItem extends Item {
    public AutoFeedUpgradeItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.backpackmod.autofeed_upgrade.desc").formatted(Formatting.GREEN));
        tooltip.add(Text.translatable("item.backpackmod.upgrade.how_to_use").formatted(Formatting.GRAY));
        super.appendTooltip(stack, world, tooltip, context);
    }
}

