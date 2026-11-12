package com.backpackmod.client.render;

import com.backpackmod.item.BackpackHolder;
import com.backpackmod.item.BackpackItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class BackpackFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    private final BackpackModel backpackModel;

    public BackpackFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context, BackpackModel model) {
        super(context);
        this.backpackModel = model;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (player.isInvisible()) {
            return;
        }

        if (!(player instanceof BackpackHolder holder)) {
            return;
        }

        ItemStack backpackStack = holder.backpackmod$getBackpackInventory().getStack(0);
        if (backpackStack.isEmpty() || !(backpackStack.getItem() instanceof BackpackItem backpackItem)) {
            return;
        }

        matrices.push();
        this.getContextModel().body.rotate(matrices);
        matrices.translate(0.0, 0.28, 0.22);
        matrices.scale(0.85F, 0.85F, 0.85F);

        Identifier texture = new Identifier("backpackmod", "textures/entity/" + backpackItem.getTier().getName() + ".png");
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(texture));
        this.backpackModel.render(matrices, buffer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

        matrices.pop();
    }
}

