package com.backpackmod.item;

import net.minecraft.screen.ScreenHandlerType;

public enum BackpackTier {
    WOODEN("wooden_backpack", 9, 1, ScreenHandlerType.GENERIC_9X1, false),
    IRON("iron_backpack", 18, 2, ScreenHandlerType.GENERIC_9X2, false),
    GOLDEN("golden_backpack", 27, 3, ScreenHandlerType.GENERIC_9X3, false),
    DIAMOND("diamond_backpack", 54, 6, ScreenHandlerType.GENERIC_9X6, false),
    NETHERITE("netherite_backpack", 54, 6, ScreenHandlerType.GENERIC_9X6, true);

    private final String name;
    private final int slots;
    private final int rows;
    private final ScreenHandlerType<?> screenHandlerType;
    private final boolean fireproof;

    BackpackTier(String name, int slots, int rows, ScreenHandlerType<?> screenHandlerType, boolean fireproof) {
        this.name = name;
        this.slots = slots;
        this.rows = rows;
        this.screenHandlerType = screenHandlerType;
        this.fireproof = fireproof;
    }

    public String getName() {
        return name;
    }

    public int getSlots() {
        return slots;
    }

    public int getRows() {
        return rows;
    }

    public ScreenHandlerType<?> getScreenHandlerType() {
        return screenHandlerType;
    }

    public boolean isFireproof() {
        return fireproof;
    }
}

