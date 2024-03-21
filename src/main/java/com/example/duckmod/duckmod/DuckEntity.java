package com.example.duckmod.duckmod;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.AquaticMoveControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.AmphibiousSwimNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.gen.feature.SeaPickleFeature;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Random;

public class DuckEntity extends AnimalEntity {

    private static final Ingredient BREEDING_INGREDIENT = Ingredient.ofItems(new ItemConvertible[]{Items.BEETROOT_SEEDS, Items.PUMPKIN_SEEDS, Items.COD_BUCKET});
    public float flapProgress;
    public float maxWingDeviation;
    public float prevMaxWingDeviation;
    public float prevFlapProgress;
    public float flapSpeed = 1.0F;
    // Tracked data
    private static final TrackedData<Boolean> HAS_EGG;
    private static final TrackedData<Integer> VARIANT;
    private static final TrackedData<Boolean> DIGGING_SAND;
    int sandDiggingCounter;
    public DuckEntity(EntityType<? extends DuckEntity> entityType, World world) {
        super(entityType, world);
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
        this.moveControl = new DuckEntity.DuckMoveControl(this);
        this.setStepHeight(1.0F);
    }

    // Getters and setters
    public void setVariant(Integer variant) { this.dataTracker.set(VARIANT, variant); }
    public Integer getVariant() {return this.dataTracker.get(VARIANT);}
    public boolean hasEgg() {
        return (Boolean)this.dataTracker.get(HAS_EGG);
    }
    void setHasEgg(boolean hasEgg) {
        this.dataTracker.set(HAS_EGG, hasEgg);
    }
    public boolean isDiggingSand() {
        return (Boolean)this.dataTracker.get(DIGGING_SAND);
    }
    protected SoundEvent getAmbientSound() {
        return DuckMod.QUACK;
    }
    protected SoundEvent getHurtSound(DamageSource source) { return DuckMod.DUCKHURT; }

    protected SoundEvent getDeathSound() {
        return DuckMod.DUCKDEATH;
    }
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_CHICKEN_STEP, 0.15F, 1.0F);
    }
    public Identifier getLootTableId(){
        return new Identifier("duckmod", "entities/duckmod");
    }
    protected void initGoals() {
        this.goalSelector.add(0, new EscapeDangerGoal(this, 0.65));
        this.goalSelector.add(1, new MateGoal(this, 0.5));
        this.goalSelector.add(1, new LayEggGoal(this, 0.5));
        this.goalSelector.add(2, new TemptGoal(this, 0.35F, BREEDING_INGREDIENT, false));
        this.goalSelector.add(3, new WanderAroundGoal(this, 0.35F, 48));
        this.goalSelector.add(4, new FollowParentGoal(this, 0.35F));
        this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(6, new LookAroundGoal(this));
    }
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(VARIANT, 0);
        this.dataTracker.startTracking(HAS_EGG, false);
        this.dataTracker.startTracking(DIGGING_SAND, false);
    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Variant", this.getVariant());
        nbt.putBoolean("HasEgg", this.hasEgg());
    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        this.setVariant(nbt.getInt("Variant"));
        super.readCustomDataFromNbt(nbt);
        this.setHasEgg(nbt.getBoolean("HasEgg"));
    }

    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        if (world.getBiome(this.getBlockPos()).isIn(TagKey.of(RegistryKeys.BIOME, new Identifier("duckmod", "warm_biomes")))) {
            this.setVariant(0);
        } else if (world.getBiome(this.getBlockPos()).isIn(TagKey.of(RegistryKeys.BIOME, new Identifier("duckmod", "cold_biomes")))) {
            this.setVariant(1);
        }
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    public static boolean canSpawn(EntityType<TurtleEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, net.minecraft.util.math.random.Random random) {
        BlockPos blockPos = null;
        Iterable<BlockPos> iterable = BlockPos.iterate(MathHelper.floor(pos.getX() - 2.0), MathHelper.floor(pos.getY() - 2.0), MathHelper.floor(pos.getZ() - 2.0), MathHelper.floor(pos.getX() + 2.0), pos.getY(), MathHelper.floor(pos.getZ() + 2.0));
        Iterator var3 = iterable.iterator();

        while(var3.hasNext()) {
            BlockPos blockPos2 = (BlockPos)var3.next();
            if (world.getFluidState(blockPos2).isIn(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }
    void setDiggingSand(boolean diggingSand) {
        this.sandDiggingCounter = diggingSand ? 1 : 0;
        this.dataTracker.set(DIGGING_SAND, diggingSand);
    }

    public EntityGroup getGroup() {
        return EntityGroup.AQUATIC;
    }
    public void tickMovement() {
        super.tickMovement();
        this.prevFlapProgress = this.flapProgress;
        this.prevMaxWingDeviation = this.maxWingDeviation;
        this.maxWingDeviation += (this.isOnGround() ? -1.0F : 4.0F) * 0.3F;
        this.maxWingDeviation = MathHelper.clamp(this.maxWingDeviation, 0.0F, 1.0F);
        if (!this.isOnGround() && this.flapSpeed < 1.0F) {
            this.flapSpeed = 1.0F;
        }

        this.flapSpeed *= 0.9F;
        Vec3d vec3d = this.getVelocity();
        if (!this.isOnGround() && vec3d.y <= 0.0) {
            //System.out.println("yes");
            this.setVelocity(vec3d.multiply(1.0, 0.6, 1.0));
        }
        this.flapProgress += this.flapSpeed * 1.0F;

        if (this.isAlive() && this.isDiggingSand() && this.sandDiggingCounter >= 1 && this.sandDiggingCounter % 5 == 0) {
            BlockPos blockPos = this.getBlockPos();
            if (this.isOnGround()) {
                this.getWorld().syncWorldEvent(2001, blockPos, Block.getRawIdFromState(this.getWorld().getBlockState(blockPos.down())));
                this.emitGameEvent(GameEvent.ENTITY_ACTION);
            }
        }
    }
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return this.isBaby() ? dimensions.height * 0.85F : dimensions.height * 0.92F;
    }
    public boolean isBreedingItem(ItemStack stack) {
        return BREEDING_INGREDIENT.test(stack);
    }

    @Nullable
    @Override
    public DuckEntity createChild(ServerWorld world, PassiveEntity entity) {
        return (DuckEntity) Registries.ENTITY_TYPE.get(new Identifier("duckmod", "duck")).create(world);
    }

    public void travel(Vec3d movementInput) {
        if (this.isLogicalSideForUpdatingMovement() && this.isTouchingWater()) {
            this.updateVelocity(0.1F, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.9));
            if (this.isSubmergedInWater()) {
                this.setVelocity(this.getVelocity().add(0.0, 0.02, 0.0));
            }

        } else {
            super.travel(movementInput);
        }
    }

    protected EntityNavigation createNavigation(World world) {
        return new AmphibiousSwimNavigation(this, world);
    }

    static class DuckMoveControl extends MoveControl {
        private final DuckEntity duck;

        private double previousG = 0;

        DuckMoveControl(DuckEntity duck) {
            super(duck);
            this.duck = duck;
        }

        private void updateVelocity() {
            if (this.duck.isTouchingWater()) {
                this.duck.setMovementSpeed(Math.max(this.duck.getMovementSpeed(), 0.08F));
            } else if (this.duck.isOnGround()) {
                this.duck.setMovementSpeed(Math.max(this.duck.getMovementSpeed(), 0.06F));
            }

        }

        public void tick() {
            this.updateVelocity();
            if (this.state == State.MOVE_TO && !this.duck.getNavigation().isIdle()) {
                double d = this.targetX - this.duck.getX();
                double e = this.targetY - this.duck.getY();
                double f = this.targetZ - this.duck.getZ();
                double g = Math.sqrt(d * d + e * e + f * f);
                if (g < 9.999999747378752E-6) {
                    this.entity.setMovementSpeed(0.0F);
                } else {
                    e /= g;
                    float h = (float)(MathHelper.atan2(f, d) * 57.2957763671875) - 90F;
                    if (Math.abs(previousG - g) >= 0.01F) {
                        this.duck.setYaw(this.wrapDegrees(this.duck.getYaw(), h, 45.0F));
                    }

                    this.duck.bodyYaw = this.duck.getYaw();
                    float i = (float)(this.speed * this.duck.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
                    this.duck.setMovementSpeed(MathHelper.lerp(0.125F, this.duck.getMovementSpeed(), i));
                    this.duck.setVelocity(this.duck.getVelocity().add(0.0, (double)this.duck.getMovementSpeed() * Math.max(0.0, Math.ceil(e)/2), 0.0));
                    previousG = g;
                }
            } else {
                this.duck.setMovementSpeed(0.0F);
            }
        }
    }
    private static class MateGoal extends AnimalMateGoal {
        private final DuckEntity duck;

        MateGoal(DuckEntity duck, double speed) {
            super(duck, speed);
            this.duck = duck;
        }

        public boolean canStart() {
            return super.canStart() && !this.duck.hasEgg();
        }

        protected void breed() {
            ServerPlayerEntity serverPlayerEntity = this.animal.getLovingPlayer();
            if (serverPlayerEntity == null && this.mate.getLovingPlayer() != null) {
                serverPlayerEntity = this.mate.getLovingPlayer();
            }

            if (serverPlayerEntity != null) {
                serverPlayerEntity.incrementStat(Stats.ANIMALS_BRED);
                Criteria.BRED_ANIMALS.trigger(serverPlayerEntity, this.animal, this.mate, (PassiveEntity)null);
            }

            this.duck.setHasEgg(true);
            this.animal.setBreedingAge(6000);
            this.mate.setBreedingAge(6000);
            this.animal.resetLoveTicks();
            this.mate.resetLoveTicks();
            this.duck.setHasEgg(true);
        }
    }

    private static class LayEggGoal extends MoveToTargetPosGoal {
        private final DuckEntity duck;

        LayEggGoal(DuckEntity duck, double speed) {
            super(duck, speed, 32);
            this.duck = duck;
        }

        public boolean canStart() {
            return this.duck.hasEgg() && super.canStart();
        }

        public boolean shouldContinue() {
            return super.shouldContinue() && this.duck.hasEgg();
        }

        public void tick() {
            super.tick();
            if (this.hasReached()) {
                if (this.duck.sandDiggingCounter < 1) {
                    this.duck.setDiggingSand(true);
                } else if (this.duck.sandDiggingCounter > this.getTickCount(200)) {
                    World world = this.duck.getWorld();
                    BlockPos blockPos2 = this.targetPos.up();
                    Random random = new Random();
                    int eggCount = random.nextInt(4) + 1;
                    BlockState blockState = Registries.BLOCK.get(new Identifier("duckmod", "ducknest")).getDefaultState().with(DuckNest.DUCK_EGGS, eggCount);
                    world.setBlockState(blockPos2, blockState, 3);
                    world.emitGameEvent(GameEvent.BLOCK_PLACE, blockPos2, GameEvent.Emitter.of(this.duck, blockState));
                    this.duck.setHasEgg(false);
                    this.duck.setDiggingSand(false);
                    this.duck.setLoveTicks(600);
                    if (this.duck.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
                        this.duck.getWorld().spawnEntity(new ExperienceOrbEntity(this.duck.getWorld(), this.duck.getX(), this.duck.getY(), this.duck.getZ(), random.nextInt(7) + 1));
                    }
                }

                if (this.duck.isDiggingSand()) {
                    ++this.duck.sandDiggingCounter;
                }
            }
        }
        protected boolean isTargetPos(WorldView world, BlockPos pos) {
            return world.isAir(pos.up()) && world.getBlockState(pos).isIn(TagKey.of(RegistryKeys.BLOCK, new Identifier("duckmod", "gravel")));
        }
    }
    public static DefaultAttributeContainer.Builder createDuckAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5);
    }

    static {
        HAS_EGG = DataTracker.registerData(DuckEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        DIGGING_SAND = DataTracker.registerData(DuckEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        VARIANT = DataTracker.registerData(DuckEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }
}
