package com.backpackmod.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class BackpackModel extends Model {
    private final ModelPart mainPart;

    public BackpackModel(ModelPart root) {
        super(RenderLayer::getEntityCutoutNoCull);
        this.mainPart = root.getChild("main");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        root.addChild("main",
                ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-4.0F, -5.0F, -2.0F, 8.0F, 10.0F, 4.0F)
                        .uv(0, 14).cuboid(-3.0F, -1.0F, 2.0F, 6.0F, 5.0F, 2.0F)
                        .uv(16, 14).cuboid(-3.5F, -6.0F, -2.0F, 7.0F, 1.0F, 4.0F),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F)
        );

        return TexturedModelData.of(modelData, 32, 32);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        mainPart.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}

