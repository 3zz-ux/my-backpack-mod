package com.backpackmod;

import com.backpackmod.item.AutoFeedUpgradeItem;
import com.backpackmod.item.BackpackItem;
import com.backpackmod.item.BackpackTier;
import com.backpackmod.item.MagnetUpgradeItem;
import com.backpackmod.recipe.BackpackUpgradeRecipe;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
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

    // 3. مسجل وصفات الترقية (لنقل الأغراض المحفوظة في الـ NBT عند الترقية)
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
        LOGGER.info("Backpack Mod initialized with all tiers & upgrades!");

        // إضافة جميع الأدوات إلى قائمة الأدوات في وضع الكرييتف
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(WOODEN_BACKPACK);
            entries.add(IRON_BACKPACK);
            entries.add(GOLDEN_BACKPACK);
            entries.add(DIAMOND_BACKPACK);
            entries.add(NETHERITE_BACKPACK);
            entries.add(MAGNET_UPGRADE);
            entries.add(AUTOFEED_UPGRADE);
        });
    }
}
