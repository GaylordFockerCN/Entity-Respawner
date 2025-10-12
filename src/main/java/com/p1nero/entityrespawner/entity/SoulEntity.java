package com.p1nero.entityrespawner.entity;

import com.p1nero.entityrespawner.EntityRespawnerConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoulEntity extends Entity {

    protected static final EntityDataAccessor<String> ENTITY_TO_RESPAWN_ID = SynchedEntityData.defineId(SoulEntity.class, EntityDataSerializers.STRING);

    protected static final EntityDataAccessor<Integer> TIMER = SynchedEntityData.defineId(SoulEntity.class, EntityDataSerializers.INT);

    private boolean soulEffect;

    public SoulEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        noPhysics = true;
    }

    public SoulEntity(Level level, int timer, Entity original) {
        super(EntityRespawnerEntities.SOUL_ENTITY.get(), level);
        this.setTimer(timer);
        this.setEntityToRespawn(original.getType());
        this.setPos(original.position().add(0, 1, 0));
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float value) {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(ENTITY_TO_RESPAWN_ID, "");
        this.getEntityData().define(TIMER, 0);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        int timer = tag.getInt("timer");
        String entityType = tag.getString("entity_type");
        this.setTimer(timer);
        this.setEntityToRespawn(entityType);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putString("entity_type", this.getEntityToRespawnId());
        tag.putInt("timer", this.getTimer());
    }

    public void enableSoulEffect() {
        this.soulEffect = true;
    }

    public void setUseSoulEffect(boolean soulEffect) {
        this.soulEffect = soulEffect;
    }

    public void setTimer(int timer) {
        this.getEntityData().set(TIMER, timer);
    }

    public int getTimer() {
        return this.getEntityData().get(TIMER);
    }

    public void setEntityToRespawn(String entityType) {
        this.getEntityData().set(ENTITY_TO_RESPAWN_ID, entityType);
    }

    public void setEntityToRespawn(EntityType<?> entityType) {
        ResourceLocation resourceLocation = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        if (resourceLocation != null) {
            setEntityToRespawn(resourceLocation.toString());
        }
    }

    public String getEntityToRespawnId() {
        return this.getEntityData().get(ENTITY_TO_RESPAWN_ID);
    }

    @Nullable
    public EntityType<?> getEntityToRespawn() {
        ResourceLocation resourceLocation = ResourceLocation.parse(this.getEntityToRespawnId());
        return ForgeRegistries.ENTITY_TYPES.getValue(resourceLocation);
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel serverLevel) {
            countdown(serverLevel);
        }
    }

    public void countdown(ServerLevel serverLevel) {
        if (this.getEntityToRespawnId().isEmpty()) {
            this.discard();
        }
        if (this.getTimer() > 0) {
            this.setTimer(this.getTimer() - 1);
            if (tickCount % 20 == 0 && soulEffect) {
                serverLevel.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0.01);
            }
            if (this.getTimer() == 1) {
                respawnEntity(serverLevel);
            }
        }
    }

    public void respawnEntity(ServerLevel serverLevel) {
        EntityType<?> entityType = this.getEntityToRespawn();
        if (entityType != null) {
            if (entityType.spawn(serverLevel, this.getOnPos(), MobSpawnType.MOB_SUMMONED) != null) {
                onSpawnSuccess();
            }
            discard();
        }
    }

    public void onSpawnSuccess() {

    }

}
