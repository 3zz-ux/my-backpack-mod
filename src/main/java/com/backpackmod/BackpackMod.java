package com.backpackmod;

import com.backpackmod.block.InstantHouseBlock;
import com.backpackmod.item.*;
import com.backpackmod.recipe.BackpackUpgradeRecipe;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
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

    // 4. تسجيل بلوكات البيوت الفورية الثلاثة
    public static final Block WOODEN_HOUSE_BLOCK = registerBlock("wooden_house",
            new InstantHouseBlock(InstantHouseBlock.HouseType.WOODEN, AbstractBlock.Settings.copy(Blocks.OAK_PLANKS)));
    public static final Block STONE_HOUSE_BLOCK = registerBlock("stone_house",
            new InstantHouseBlock(InstantHouseBlock.HouseType.STONE, AbstractBlock.Settings.copy(Blocks.STONE_BRICKS)));
    public static final Block MINI_CASTLE_BLOCK = registerBlock("mini_castle",
            new InstantHouseBlock(InstantHouseBlock.HouseType.CASTLE, AbstractBlock.Settings.copy(Blocks.DEEPSLATE_BRICKS)));

    // 5. مسجل وصفات الترقية للحقائب
    public static final RecipeSerializer<BackpackUpgradeRecipe> BACKPACK_UPGRADE_SERIALIZER = Registry.register(
            Registries.RECIPE_SERIALIZER,
            new Identifier(MOD_ID, "backpack_upgrade"),
            BackpackUpgradeRecipe.Serializer.INSTANCE
    );

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(MOD_ID, name), item);
    }

    private static Block registerBlock(String name, Block block) {
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, name), block);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, name), new BlockItem(block, new Item.Settings()));
        return block;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Backpack & Survival Expansion Mod initialized successfully!");

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

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
            entries.add(WOODEN_HOUSE_BLOCK);
            entries.add(STONE_HOUSE_BLOCK);
            entries.add(MINI_CASTLE_BLOCK);
        });
    }
}
