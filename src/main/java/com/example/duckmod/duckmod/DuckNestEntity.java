package com.example.duckmod.duckmod;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class DuckNestEntity extends BlockEntity {

    public static final BlockEntityType<DuckNestEntity> DUCK_NEST = BlockEntityType.Builder.create(DuckNestEntity::new, Registries.BLOCK.get(new Identifier("duckmod", "ducknest"))).build(null);

    private final List<Egg> eggs = Lists.newArrayList();

    public static void register() {
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("duckmod", "ducknest"), DUCK_NEST);
    }

    public DuckNestEntity(BlockPos pos, BlockState state) {
        super(DUCK_NEST, pos, state);
    }

    public void addEgg(NbtCompound nbtCompound, int hatchCounter) {
        this.eggs.add(new Egg(nbtCompound, hatchCounter));
    }

    public Egg removeEgg(NbtCompound nbtCompound) {
        return this.eggs.remove(0);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, DuckNestEntity blockEntity) {
        tickEggs(world, pos, state, blockEntity.eggs);
    }

    private static void tickEggs(World world, BlockPos pos, BlockState state, List<Egg> eggs) {
        boolean bl = false;
        //System.out.println(eggs.size());
        Egg egg;
        for(Iterator<Egg> iterator = eggs.iterator(); iterator.hasNext(); --egg.hatchCounter) {
            egg = (Egg)iterator.next();
            if (egg.hatchCounter < 0) {
                //System.out.println("Hatched!");
                world.syncWorldEvent(2001, pos, Block.getRawIdFromState(state));
                iterator.remove();
                world.setBlockState(pos, world.getBlockState(pos).getBlock().getStateManager().getDefaultState().with(DuckNest.DUCK_EGGS, state.get(DuckNest.DUCK_EGGS)-1));
                DuckEntity duckEntity = (DuckEntity) Registries.ENTITY_TYPE.get(new Identifier("duckmod", "duck")).create(world);
                if (duckEntity != null) {
                    if (duckEntity.getWorld().getBiome(pos).isIn(TagKey.of(RegistryKeys.BIOME, BiomeTags.SPAWNS_COLD_VARIANT_FROGS.id()))) {
                        duckEntity.setVariant(1);
                    } else {
                        duckEntity.setVariant(0);
                    }
                    duckEntity.setBreedingAge(-12000);
                    duckEntity.refreshPositionAndAngles((double)pos.getX() + duckEntity.getRandom().nextBetween(0, 1), (double)pos.getY(), (double)pos.getZ() + duckEntity.getRandom().nextBetween(0, 1), 0.0F, 0.0F);
                    world.spawnEntity(duckEntity);
                }
            }
        }

        /*if (bl) {
            markDirty(world, pos, state);
        }*/

    }

    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.eggs.clear();
        NbtList nbtList = nbt.getList("Eggs", 10);

        for(int i = 0; i < nbtList.size(); ++i) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            Egg egg = new Egg(nbtCompound.getCompound("EntityData").copy(), nbtCompound.getInt("HatchCounter"));
            this.eggs.add(egg);
        }
    }

    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("Eggs", this.getEggs());
    }

    public NbtList getEggs() {
        NbtList nbtList = new NbtList();
        Iterator var2 = this.eggs.iterator();

        while(var2.hasNext()) {
            Egg egg = (Egg)var2.next();
            NbtCompound nbtCompound = egg.entityData.copy();
            nbtCompound.remove("UUID");
            NbtCompound nbtCompound2 = new NbtCompound();
            nbtCompound2.put("EntityData", nbtCompound);
            nbtCompound2.putInt("HatchCounter", egg.hatchCounter);
            nbtList.add(nbtCompound2);
        }
        return nbtList;
    }

    static class Egg {
        int hatchCounter;
        final NbtCompound entityData;
        Egg(NbtCompound entityData, int hatchCounter) {
            this.entityData = entityData;
            this.hatchCounter = hatchCounter;
        }
    }
}
