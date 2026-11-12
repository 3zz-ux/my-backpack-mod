package com.backpackmod;

import com.backpackmod.item.BackpackItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackpackMod implements ModInitializer {
    public static final String MOD_ID = "backpackmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // تسجيل عنصر الحقيبة مع ضبط الحد الأقصى للتكديس على 1 (لا تتكدس الحقائب فوق بعضها)
    public static final Item BACKPACK_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "backpack"),
            new BackpackItem(new Item.Settings().maxCount(1))
    );

    @Override
    public void onInitialize() {
        LOGGER.info("Backpack Mod loaded successfully!");

        // إضافة الحقيبة إلى تبويب الأدوات في وضع الكرييتف (Creative Tab)
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(BACKPACK_ITEM);
        });
    }
}

