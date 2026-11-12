package com.backpackmod.recipe;

import com.backpackmod.BackpackMod;
import com.backpackmod.item.BackpackItem;
import com.google.gson.JsonObject;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

public class BackpackUpgradeRecipe extends ShapedRecipe {
    public BackpackUpgradeRecipe(ShapedRecipe recipe) {
        super(recipe.getId(), recipe.getGroup(), recipe.getCategory(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getOutput(DynamicRegistryManager.EMPTY));
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        ItemStack output = super.craft(inventory, registryManager);

        // البحث عن الحقيبة القديمة داخل شبكة الكرافتنج ونقل كل أغراضها (NBT) للحقيبة الجديدة
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack inputStack = inventory.getStack(i);
            if (inputStack.getItem() instanceof BackpackItem && inputStack.hasNbt()) {
                output.setNbt(inputStack.getNbt().copy());
                break;
            }
        }

        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BackpackMod.BACKPACK_UPGRADE_SERIALIZER;
    }

    public static class Serializer implements RecipeSerializer<BackpackUpgradeRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public BackpackUpgradeRecipe read(Identifier id, JsonObject json) {
            ShapedRecipe shapedRecipe = RecipeSerializer.SHAPED.read(id, json);
            return new BackpackUpgradeRecipe(shapedRecipe);
        }

        @Override
        public BackpackUpgradeRecipe read(Identifier id, PacketByteBuf buf) {
            ShapedRecipe shapedRecipe = RecipeSerializer.SHAPED.read(id, buf);
            return new BackpackUpgradeRecipe(shapedRecipe);
        }

        @Override
        public void write(PacketByteBuf buf, BackpackUpgradeRecipe recipe) {
            RecipeSerializer.SHAPED.write(buf, recipe);
        }
    }
}
