package com.example.duckmod.duckmod;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.server.recipe.CookingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.item.Item;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DuckModDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		FabricDataGenerator.Pack pack = generator.createPack();

		pack.addProvider(MyTagGenerator::new);
		pack.addProvider(MyRecipeGenerator::new);
	}

	private static final TagKey<Block> GRAVEL = TagKey.of(RegistryKeys.BLOCK, new Identifier("duckmod:gravel"));


	private static class MyTagGenerator extends FabricTagProvider.BlockTagProvider {
		public MyTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
			super(output, completableFuture);
		}

		@Override
		protected void configure(RegistryWrapper.WrapperLookup arg) {
			getOrCreateTagBuilder(GRAVEL).add(Blocks.GRAVEL);
		}
	}

	private static class MyRecipeGenerator extends FabricRecipeProvider {
		private MyRecipeGenerator(FabricDataOutput generator) {
			super(generator);
			//Registry.register(Registries.ITEM, new Identifier("duckmod", "roasted_duck"), new Item(new Item.Settings().food(DuckMod.COOKED_DUCK_MEAT)));
		}

		@Override
		public void generate(RecipeExporter exporter) {
			//CookingRecipeJsonBuilder.createSmoking(Ingredient.ofItems(Registries.ITEM.get(new Identifier("duckmod", "rawduck"))), RecipeCategory.FOOD, new Item(new Item.Settings().food(DuckMod.COOKED_DUCK_MEAT)), 0.45F, 300).offerTo(exporter);
		}
	}
}


