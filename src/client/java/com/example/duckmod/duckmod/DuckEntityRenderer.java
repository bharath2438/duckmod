package com.example.duckmod.duckmod;


import net.minecraft.client.render.entity.ChickenEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.ChickenEntityModel;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class DuckEntityRenderer extends MobEntityRenderer<DuckEntity, DuckEntityModel<DuckEntity>> {
    public DuckEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new DuckEntityModel(context.getPart(DuckModClient.MODEL_DUCK_LAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(DuckEntity entity) {
        return entity.getVariant() == 0 ? new Identifier("duckmod", "textures/entity/duckmod/duckmod.png") : new Identifier("duckmod", "textures/entity/duckmod/duckmod_mallard.png");
    }

    protected float getAnimationProgress(DuckEntity duckEntity, float f) {
        float g = 0;
        float h = 0;
        if (!duckEntity.isTouchingWater()) {
            g = MathHelper.lerp(f, duckEntity.prevFlapProgress, duckEntity.flapProgress);
            h = MathHelper.lerp(f, duckEntity.prevMaxWingDeviation, duckEntity.maxWingDeviation);
        }
        return -(MathHelper.sin(g) + 1.0F) * h;
    }
}
