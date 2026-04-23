package com.p1nero.entityrespawner.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.p1nero.entityrespawner.entity.SoulEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RespawnableEntityManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DIRECTORY = "entity_to_respawn";
    private static final RespawnableEntityManager INSTANCE = new RespawnableEntityManager();
    private static final Codec<Vec3> POS_OVERRIDE_CODEC = Codec.DOUBLE.listOf().comapFlatMap(
            values -> {
                if (values.size() != 3) {
                    return DataResult.error(() -> "pos_override must contain exactly 3 numbers");
                }
                return DataResult.success(new Vec3(values.get(0), values.get(1), values.get(2)));
            },
            vec3 -> List.of(vec3.x, vec3.y, vec3.z)
    );
    private static final Codec<CompoundTag> COMPOUND_TAG_CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> {
                Tag tag = dynamic.convert(NbtOps.INSTANCE).getValue();
                return tag instanceof CompoundTag compoundTag ? compoundTag : new CompoundTag();
            },
            compoundTag -> new Dynamic<>(NbtOps.INSTANCE, compoundTag).convert(JsonOps.INSTANCE)
    );

    private static final Map<ResourceLocation, RespawnableEntityData> RESPAWNABLE_ENTITY_DATA = new HashMap<>();

    public RespawnableEntityManager() {
        super(GSON, DIRECTORY);
    }

    public static RespawnableEntityManager getInstance() {
        return INSTANCE;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ResourceLocation, RespawnableEntityData> loadedData = new HashMap<>();
        jsonMap.forEach((resourceId, jsonElement) -> RespawnableEntityData.CODEC.parse(JsonOps.INSTANCE, jsonElement).resultOrPartial(error ->
                LOGGER.error("Failed to parse respawnable entity data {}: {}", resourceId, error)
        ).ifPresent(data -> loadedData.put(resourceId, data)));
        RESPAWNABLE_ENTITY_DATA.clear();
        RESPAWNABLE_ENTITY_DATA.putAll(loadedData);
        LOGGER.info("Loaded {} respawnable entity definitions", RESPAWNABLE_ENTITY_DATA.size());
    }

    public static boolean isRespawnable(Entity entity) {
        return getRespawnableData(entity).isPresent();
    }

    public static boolean shouldRespawnWhenLoad(Entity entity) {
        return getRespawnableData(entity).filter(RespawnableEntityData::shouldRespawnWhenReloadFromDisk).isPresent();
    }

    @Nullable
    public static SoulEntity createFrom(Entity entity) {
        RespawnableEntityData data = getRespawnableData(entity).orElse(null);
        return createFrom(entity, data);
    }

    @Nullable
    public static SoulEntity createFrom(Entity entity, RespawnableEntityData data) {
        if (data == null) {
            return null;
        }
        SoulEntity soulEntity = new SoulEntity(entity.level(), data.getRespawnTimer(), entity);
        soulEntity.setUseSoulEffect(data.hasSoulEffect());
        soulEntity.setRespawnWhenLoadFromDisk(data.shouldRespawnWhenReloadFromDisk());
        if (data.getPosOverride() != null) {
            soulEntity.setPos(data.getPosOverride());
        }
        CompoundTag entityData = null;
        if (data.shouldKeepNbt()) {
            entityData = entity.saveWithoutId(new CompoundTag());
            entityData.remove("Pos");
            entityData.remove("Motion");
            entityData.remove("Rotation");
            entityData.remove("UUID");
        }
        if (data.getNbtOverride() != null) {
            if (entityData == null) {
                entityData = data.getNbtOverride().copy();
            } else {
                entityData.merge(data.getNbtOverride().copy());
            }
        }
        if (entityData != null && !entityData.isEmpty()) {
            soulEntity.setEntityToRespawnData(entityData);
        }

        if(data.shouldBroadcast()) {
            if(entity.level() instanceof ServerLevel serverLevel) {
                serverLevel.players().forEach(serverPlayer -> serverPlayer.displayClientMessage(Component.translatable("message.entityrespawner.entity_to_respawn_added", entity.getDisplayName(), data.getRespawnTimer()), false));
            }
        }

        return soulEntity;
    }

    public static Optional<RespawnableEntityData> getRespawnableData(Entity entity) {
        return Optional.ofNullable(getRespawnableData(entity.getType()));
    }

    @Nullable
    public static RespawnableEntityData getRespawnableData(EntityType<?> entityType) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        return entityId == null ? null : getRespawnableData(entityId);
    }

    @Nullable
    public static RespawnableEntityData getRespawnableData(ResourceLocation entityId) {
        return RESPAWNABLE_ENTITY_DATA.get(entityId);
    }

    public static Map<ResourceLocation, RespawnableEntityData> getAllRespawnableData() {
        return Map.copyOf(RESPAWNABLE_ENTITY_DATA);
    }

    public static class RespawnableEntityData {

        public static final Codec<RespawnableEntityData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("respawn_timer", 0).forGetter(RespawnableEntityData::getRespawnTimer),
                Codec.BOOL.optionalFieldOf("respawn_when_reload_from_disk", false).forGetter(RespawnableEntityData::shouldRespawnWhenReloadFromDisk),
                Codec.BOOL.optionalFieldOf("soul_effect", false).forGetter(RespawnableEntityData::hasSoulEffect),
                Codec.BOOL.optionalFieldOf("broadcast", false).forGetter(RespawnableEntityData::shouldBroadcast),
                Codec.BOOL.optionalFieldOf("keep_nbt", false).forGetter(RespawnableEntityData::shouldKeepNbt),
                POS_OVERRIDE_CODEC.optionalFieldOf("pos_override").forGetter(data -> java.util.Optional.ofNullable(data.getPosOverride())),
                COMPOUND_TAG_CODEC.optionalFieldOf("nbt_override").forGetter(data -> java.util.Optional.ofNullable(data.getNbtOverride()))
        ).apply(instance, (respawnTimer, respawnWhenReloadFromDisk, soulEffect, broadcast, keepNbt, posOverride, nbtOverride) -> {
            RespawnableEntityData data = new RespawnableEntityData();
            data.setRespawnTimer(respawnTimer);
            data.setRespawnWhenReloadFromDisk(respawnWhenReloadFromDisk);
            data.setSoulEffect(soulEffect);
            data.setBroadcast(broadcast);
            data.setKeepNbt(keepNbt);
            data.setPosOverride(posOverride.orElse(null));
            data.setNbtOverride(nbtOverride.map(CompoundTag::copy).orElse(null));
            return data;
        }));

        private int respawnTimer;
        private boolean respawnWhenReloadFromDisk;
        private boolean soulEffect;
        private boolean broadcast;
        private boolean keepNbt;
        @Nullable
        private Vec3 posOverride;
        @Nullable
        private CompoundTag nbtOverride;

        public RespawnableEntityData() {
        }

        public int getRespawnTimer() {
            return respawnTimer;
        }

        public void setRespawnTimer(int respawnTimer) {
            this.respawnTimer = respawnTimer;
        }

        public boolean shouldRespawnWhenReloadFromDisk() {
            return respawnWhenReloadFromDisk;
        }

        public void setRespawnWhenReloadFromDisk(boolean respawnWhenReloadFromDisk) {
            this.respawnWhenReloadFromDisk = respawnWhenReloadFromDisk;
        }

        public boolean hasSoulEffect() {
            return soulEffect;
        }

        public void setSoulEffect(boolean soulEffect) {
            this.soulEffect = soulEffect;
        }

        public boolean shouldBroadcast() {
            return broadcast;
        }

        public void setBroadcast(boolean broadcast) {
            this.broadcast = broadcast;
        }

        public boolean shouldKeepNbt() {
            return keepNbt;
        }

        public void setKeepNbt(boolean keepNbt) {
            this.keepNbt = keepNbt;
        }

        @Nullable
        public Vec3 getPosOverride() {
            return posOverride;
        }

        public void setPosOverride(@Nullable Vec3 posOverride) {
            this.posOverride = posOverride;
        }

        @Nullable
        public CompoundTag getNbtOverride() {
            return nbtOverride == null ? null : nbtOverride.copy();
        }

        public void setNbtOverride(@Nullable CompoundTag nbtOverride) {
            this.nbtOverride = nbtOverride == null ? null : nbtOverride.copy();
        }
    }
}
