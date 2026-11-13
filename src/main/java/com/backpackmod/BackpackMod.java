package com.backpackmod;

import com.backpackmod.item.*;
import com.backpackmod.recipe.BackpackUpgradeRecipe;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackpackMod implements ModInitializer {
    public static final String MOD_ID = "backpackmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // 1. تسجيل مستويات الحقائب الخمسة
    public static final Item WOODEN_BACKPACK = registerItem("wooden_backpack",
            new BackpackItem(BackpackTier.WOODEN, new Item.Settings().maxCount(1)));
    public static final Item IRON_BACKPACK = registerItem("iron_backpack",
            new BackpackItem(BackpackTier.IRON, new Item.Settings().maxCount(1)));
    public static final Item GOLDEN_BACKPACK = registerItem("golden_backpack",
            new BackpackItem(BackpackTier.GOLDEN, new Item.Settings().maxCount(1)));
    public static final Item DIAMOND_BACKPACK = registerItem("diamond_backpack",
            new BackpackItem(BackpackTier.DIAMOND, new Item.Settings().maxCount(1)));
    public static final Item NETHERITE_BACKPACK = registerItem("netherite_backpack",
            new BackpackItem(BackpackTier.NETHERITE, new Item.Settings().maxCount(1)));

    // 2. تسجيل بطاقات الترقية الخاصة
    public static final Item MAGNET_UPGRADE = registerItem("magnet_upgrade",
            new MagnetUpgradeItem(new Item.Settings().maxCount(1)));
    public static final Item AUTOFEED_UPGRADE = registerItem("autofeed_upgrade",
            new AutoFeedUpgradeItem(new Item.Settings().maxCount(1)));

    // 3. تسجيل المطارق غير القابلة للكسر بتدرج المساحات (3x3 إلى 7x7)
    public static final Item WOODEN_HAMMER = registerItem("wooden_hammer",
            new HammerItem(ToolMaterials.WOOD, 3, 3, -3.0F, new Item.Settings()));
    public static final Item STONE_HAMMER = registerItem("stone_hammer",
            new HammerItem(ToolMaterials.STONE, 4, 4, -3.0F, new Item.Settings()));
    public static final Item IRON_HAMMER = registerItem("iron_hammer",
            new HammerItem(ToolMaterials.IRON, 5, 5, -3.0F, new Item.Settings()));
    public static final Item GOLDEN_HAMMER = registerItem("golden_hammer",
            new HammerItem(ToolMaterials.GOLD, 5, 3, -2.8F, new Item.Settings()));
    public static final Item DIAMOND_HAMMER = registerItem("diamond_hammer",
            new HammerItem(ToolMaterials.DIAMOND, 6, 6, -3.0F, new Item.Settings()));
    public static final Item NETHERITE_HAMMER = registerItem("netherite_hammer",
            new HammerItem(ToolMaterials.NETHERITE, 7, 7, -3.0F, new Item.Settings().fireproof()));

    // 4. مسجل وصفات الترقية للحقائب
    public static final RecipeSerializer<BackpackUpgradeRecipe> BACKPACK_UPGRADE_SERIALIZER = Registry.register(
            Registries.RECIPE_SERIALIZER,
            new Identifier(MOD_ID, "backpack_upgrade"),
            BackpackUpgradeRecipe.Serializer.INSTANCE
    );

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(MOD_ID, name), item);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Backpack, Ultimate Tools & 3x XP Mod initialized!");

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(WOODEN_BACKPACK);
            entries.add(IRON_BACKPACK);
            entries.add(GOLDEN_BACKPACK);
            entries.add(DIAMOND_BACKPACK);
            entries.add(NETHERITE_BACKPACK);

            entries.add(MAGNET_UPGRADE);
            entries.add(AUTOFEED_UPGRADE);

            entries.add(WOODEN_HAMMER);
            entries.add(STONE_HAMMER);
            entries.add(IRON_HAMMER);
            entries.add(GOLDEN_HAMMER);
            entries.add(DIAMOND_HAMMER);
            entries.add(NETHERITE_HAMMER);
        });
    }
}
