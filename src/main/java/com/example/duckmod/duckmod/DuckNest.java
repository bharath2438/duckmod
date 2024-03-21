package com.example.duckmod.duckmod;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.impl.registry.sync.FabricRegistryInit;
import net.minecraft.block.*;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.MoveIntoWaterGoal;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.RegistryWorldView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DuckNest extends BlockWithEntity {

    public static final IntProperty DUCK_EGGS = IntProperty.of("eggs", 0, 4);

    public static boolean eggHatched = false;
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(DUCK_EGGS);
    }

    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        Random random = new Random();
        int eggCount = state.get(DUCK_EGGS);
        DuckNestEntity duckNestEntity = new DuckNestEntity(pos, state.with(DuckNest.DUCK_EGGS, eggCount));
        NbtCompound nbtCompound = new NbtCompound();
        for (int i = 0; i < eggCount; ++i) {
            duckNestEntity.addEgg(nbtCompound, random.nextInt(2000, 2500) + 4);
        }
        return duckNestEntity;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : validateTicker(type, (BlockEntityType<DuckNestEntity>) Registries.BLOCK_ENTITY_TYPE.get(new Identifier("duckmod", "ducknest")), DuckNestEntity::serverTick);
    }

    public static final MapCodec<DuckNest> CODEC = createCodec(DuckNest::new);

    public MapCodec<DuckNest> getCodec() {
        return CODEC;
    }
    public DuckNest(Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(DUCK_EGGS,0)));
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0);
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

//    @Override
//    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
////        this.spawnBreakParticles(world, player, pos, state);
////        if (state.isIn(BlockTags.GUARDED_BY_PIGLINS)) {
////            PiglinBrain.onGuardedBlockInteracted(player, false);
////        }
////
////        world.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(player, state));
////        return state;
//        super.onBreak(world, pos, state, player);
//        dropStack(world, pos, this.asItem().getDefaultStack());
//        return state;
//    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            int i = state.get(DUCK_EGGS);
            NbtCompound nbtCompound = new NbtCompound();
            if (player.getEquippedStack(EquipmentSlot.MAINHAND).isOf(Registries.ITEM.get(new Identifier("duckmod", "duckegg"))) && i < 4) {
                addEgg(world, pos, i);
                BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof DuckNestEntity) {
                    Random random = new Random();
                    ((DuckNestEntity) entity).addEgg(nbtCompound, random.nextInt(2000, 2500) + 4);
                }
                player.getEquippedStack(EquipmentSlot.MAINHAND).decrement(1);
            }
            else if (i > 0) {
                BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof DuckNestEntity) {
                    removeEgg(world, pos, i);
                    DuckEgg egg = (DuckEgg) Registries.ITEM.get(new Identifier("duckmod", "duckegg"));
                    egg.setCounter(((DuckNestEntity) entity).removeEgg(nbtCompound).hatchCounter);
                    dropStack(world, pos, new ItemStack(egg));
                }
                /*removeEgg(world, pos, i);
                BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof DuckNestEntity) {
                    System.out.println("Hello");
                    NbtList eggs = ((DuckNestEntity) entity).getEggs();
                    eggs.remove(eggs.size() - 1);
                }*/
                //dropStack(world, pos, new ItemStack(eggs.get(0)));
            }

            return ActionResult.CONSUME;
        }
    }
    public void removeEgg(World world, BlockPos pos, int value){
        world.setBlockState(pos, (BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(DUCK_EGGS,value-1)));
    }

    public void addEgg(World world, BlockPos pos, int value){
        world.setBlockState(pos, (BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(DUCK_EGGS,value+1)));
    }
}

