package com.example.duckmod.duckmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class DuckModClient implements ClientModInitializer {
	public static final EntityModelLayer MODEL_DUCK_LAYER = new EntityModelLayer(new Identifier("duckmod", "duck"), "main");
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(DuckMod.DUCK, (context) -> {
			return new DuckEntityRenderer(context);
		});
		EntityModelLayerRegistry.registerModelLayer(MODEL_DUCK_LAYER, DuckEntityModel::getTexturedModelData);
	}
}