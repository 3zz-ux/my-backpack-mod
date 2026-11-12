package com.backpackmod.client;

import com.backpackmod.client.render.BackpackFeatureRenderer;
import com.backpackmod.client.render.BackpackModel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class BackpackClientMod implements ClientModInitializer {
    public static final EntityModelLayer BACKPACK_LAYER = new EntityModelLayer(new Identifier("backpackmod", "backpack"), "main");

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(BACKPACK_LAYER, BackpackModel::getTexturedModelData);

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityType == EntityType.PLAYER && entityRenderer instanceof PlayerEntityRenderer playerEntityRenderer) {
                registrationHelper.register(new BackpackFeatureRenderer(
                        playerEntityRenderer,
                        new BackpackModel(context.getModelLoader().getModelPart(BACKPACK_LAYER))
                ));
            }
        });
    }
}

