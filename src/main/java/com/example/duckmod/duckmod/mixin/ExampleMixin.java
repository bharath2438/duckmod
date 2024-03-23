package com.example.duckmod.duckmod.mixin;

import com.example.duckmod.duckmod.DuckEntity;
import com.example.duckmod.duckmod.DuckMod;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.biome.v1.BiomeModification;
import net.fabricmc.fabric.impl.biome.modification.BiomeModificationImpl;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressLogger;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.*;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Map;

@Mixin(VillagerEntity.class)
public class ExampleMixin {
        @Inject(method="fillRecipes", at=@At("HEAD"), cancellable = true)
        private void fillCustomTrades(CallbackInfo ci) {
                VillagerData villagerData = ((VillagerEntity)(Object)this).getVillagerData();
                Int2ObjectMap int2ObjectMap2;
                if (((VillagerEntity)(Object)this).getWorld().getEnabledFeatures().contains(FeatureFlags.TRADE_REBALANCE)) {
                        Int2ObjectMap<TradeOffers.Factory[]> int2ObjectMap = (Int2ObjectMap)TradeOffers.REBALANCED_PROFESSION_TO_LEVELED_TRADE.get(villagerData.getProfession());
                        int2ObjectMap2 = int2ObjectMap != null ? int2ObjectMap : (Int2ObjectMap)TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(villagerData.getProfession());
                } else if (villagerData.getProfession().equals(VillagerProfession.BUTCHER)){
                        int2ObjectMap2 = new Int2ObjectOpenHashMap(ImmutableMap.of(1, new TradeOffers.Factory[]{new TradeOffers.BuyItemFactory(Registries.ITEM.get(new Identifier("duckmod", "rawduck")), 14, 16, 2), new TradeOffers.BuyItemFactory(Items.PORKCHOP, 7, 16, 2), new TradeOffers.BuyItemFactory(Items.RABBIT, 4, 16, 2), new TradeOffers.SellItemFactory(Registries.ITEM.get(new Identifier("duckmod", "duckeggstew")), 1, 1, 1)}, 2, new TradeOffers.Factory[]{new TradeOffers.BuyItemFactory(Items.COAL, 15, 16, 2), new TradeOffers.SellItemFactory(Items.COOKED_PORKCHOP, 1, 5, 16, 5), new TradeOffers.SellItemFactory(Registries.ITEM.get(new Identifier("duckmod", "roasted_duck")), 1, 8, 16, 5)}, 3, new TradeOffers.Factory[]{new TradeOffers.BuyItemFactory(Items.MUTTON, 7, 16, 20), new TradeOffers.BuyItemFactory(Items.BEEF, 10, 16, 20)}, 4, new TradeOffers.Factory[]{new TradeOffers.BuyItemFactory(Items.DRIED_KELP_BLOCK, 10, 12, 30)}, 5, new TradeOffers.Factory[]{new TradeOffers.BuyItemFactory(Items.SWEET_BERRIES, 10, 12, 30)}));
                } else {
                        int2ObjectMap2 = (Int2ObjectMap)TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(villagerData.getProfession());
                }

                if (int2ObjectMap2 != null && !int2ObjectMap2.isEmpty()) {
                        TradeOffers.Factory[] factorys = (TradeOffers.Factory[])int2ObjectMap2.get(villagerData.getLevel());
                        if (factorys != null) {
                                TradeOfferList tradeOfferList = ((VillagerEntity)(Object)this).getOffers();
                                //((VillagerEntity)(Object)this).fillRecipesFromPool(tradeOfferList, factorys, 2);
                                ArrayList<TradeOffers.Factory> arrayList = Lists.newArrayList(factorys);
                                int i = 0;

                                while(i < 2 && !arrayList.isEmpty()) {
                                        TradeOffer tradeOffer = ((TradeOffers.Factory)arrayList.remove(Random.create().nextInt(arrayList.size()))).create(((VillagerEntity)(Object)this), Random.create());
                                        if (tradeOffer != null) {
                                                tradeOfferList.add(tradeOffer);
                                                ++i;
                                        }
                                }
                        }
                }
                ci.cancel();
        }
}