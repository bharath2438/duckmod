package com.example.duckmod.duckmod;

import com.example.duckmod.duckmod.mixin.ExampleMixin;
import com.mojang.datafixers.types.templates.Tag;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.biome.v1.BiomeModification;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.impl.datagen.ForcedTagEntry;
import net.minecraft.advancement.criterion.VillagerTradeCriterion;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.CatVariant;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.*;
import net.minecraft.recipe.CookingRecipeSerializer;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.BiomeSources;
import net.minecraft.registry.tag.TagKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class DuckMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("duckmod");
	public static final EntityType<DuckEntity> DUCK = Registry.register(
			Registries.ENTITY_TYPE,
			new Identifier("duckmod", "duck"),
			FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, DuckEntity::new).dimensions(EntityDimensions.fixed(0.65f, 0.65f)).build()
	);

	public static final Block DUCK_NEST = new DuckNest(FabricBlockSettings.create().nonOpaque().strength(0.75F));
	public static final Item DUCK_EGG = new DuckEgg(new FabricItemSettings());

	public static final Item DUCK_MEAT = new Item(new Item.Settings().food((new FoodComponent.Builder()).hunger(3).saturationModifier(0.4F).meat().build()));
	public static final Item COOKED_DUCK_MEAT = new Item(new Item.Settings().food((new FoodComponent.Builder()).hunger(9).saturationModifier(0.8F).meat().build()));
	public static final Item DUCK_STEW = new Item(new Item.Settings().maxCount(4).food((new FoodComponent.Builder()).hunger(11).saturationModifier(0.5F).build()));
	public static final Item DUCK_SPAWN_EGG = new SpawnEggItem(DUCK, 0xb4d4e1, 0xec8822, new FabricItemSettings());

	public static final SoundEvent QUACK = SoundEvent.of(new Identifier("duckmod", "quack"));
	public static final SoundEvent DUCKHURT = SoundEvent.of(new Identifier("duckmod", "hurt"));
	public static final SoundEvent DUCKDEATH = SoundEvent.of(new Identifier("duckmod", "death"));

	@Override
	public void onInitialize() {
		FabricDefaultAttributeRegistry.register(DUCK, DuckEntity.createMobAttributes());
		Registry.register(Registries.BLOCK, new Identifier("duckmod", "ducknest"), DUCK_NEST);
		Registry.register(Registries.ITEM, new Identifier("duckmod", "ducknest"), new BlockItem(DUCK_NEST, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("duckmod", "duckegg"), DUCK_EGG);
		Registry.register(Registries.ITEM, new Identifier("duckmod", "rawduck"), DUCK_MEAT);
		Registry.register(Registries.ITEM, new Identifier("duckmod", "roasted_duck"), COOKED_DUCK_MEAT);
		Registry.register(Registries.ITEM, new Identifier("duckmod", "duckeggstew"), DUCK_STEW);
		Registry.register(Registries.ITEM, new Identifier("duckmod", "duckspawnegg"), DUCK_SPAWN_EGG);
		Registry.register(Registries.SOUND_EVENT, new Identifier("duckmod", "quack"), QUACK);
		Registry.register(Registries.SOUND_EVENT, new Identifier("duckmod", "hurt"), DUCKHURT);
		Registry.register(Registries.SOUND_EVENT, new Identifier("duckmod", "death"), DUCKDEATH);
		DuckNestEntity.register();

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> {
			content.addAfter(Items.FROGSPAWN, DUCK_NEST);
		});

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(content -> {
			content.addAfter(Items.EGG, DUCK_EGG);
		});

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(content -> {
			content.addAfter(Items.CHICKEN, DUCK_MEAT);
		});

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(content -> {
			content.addAfter(Items.COOKED_CHICKEN, COOKED_DUCK_MEAT);
		});

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(content -> {
			content.addAfter(Items.RABBIT_STEW, DUCK_STEW);
		});

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(content -> {
			content.addAfter(Items.CHICKEN_SPAWN_EGG, DUCK_SPAWN_EGG);
		});

		BiomeModifications.addSpawn(BiomeSelectors.tag(TagKey.of(RegistryKeys.BIOME, new Identifier("duckmod", "warm_biomes"))), SpawnGroup.CREATURE, DUCK, 4, 2, 4);
		BiomeModifications.addSpawn(BiomeSelectors.tag(TagKey.of(RegistryKeys.BIOME, new Identifier("duckmod", "cold_biomes"))), SpawnGroup.CREATURE, DUCK, 6, 2, 4);

	}
}