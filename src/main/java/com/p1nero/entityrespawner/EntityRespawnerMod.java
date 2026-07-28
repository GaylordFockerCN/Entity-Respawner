package com.p1nero.entityrespawner;

import com.p1nero.entityrespawner.data.RespawnableEntityManager;
import com.p1nero.entityrespawner.entity.EntityRespawnerEntities;
import com.p1nero.entityrespawner.entity.SoulEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.jetbrains.annotations.Nullable;

@Mod(EntityRespawnerMod.MOD_ID)
public class EntityRespawnerMod {

    public static final String MOD_ID = "entityrespawner";

    public EntityRespawnerMod(IEventBus modEventBus) {
        EntityRespawnerEntities.REGISTRY.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(this::onLivingJoin);
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListener);
    }

    private void onLivingDeath(LivingDeathEvent event) {
        if(!event.isCanceled() && !event.getEntity().getPersistentData().getBoolean(MOD_ID + "mark")) {
            if(RespawnableEntityManager.isRespawnable(event.getEntity())){
                SoulEntity soulEntity = RespawnableEntityManager.createFrom(event.getEntity());
                if (soulEntity != null && event.getEntity().level() instanceof ServerLevel serverLevel) {
                    event.getEntity().getPersistentData().putBoolean(MOD_ID + "mark", true);
                    serverLevel.addFreshEntity(soulEntity);
                }
            }
        }
    }

    private void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(RespawnableEntityManager.getInstance());
    }

    private void onLivingJoin(EntityJoinLevelEvent event) {
        if(event.getLevel() instanceof ServerLevel serverLevel && event.loadedFromDisk() && event.getEntity() instanceof SoulEntity soulEntity && soulEntity.shouldRespawnWhenLoadFromDisk()) {
            soulEntity.respawnEntity(serverLevel);
        }
    }

    @Nullable
    public static SoulEntity createSoulEntity(LivingEntity living, RespawnableEntityManager.RespawnableEntityData data) {
        if(living.level() instanceof ServerLevel serverLevel) {
            SoulEntity soulEntity = RespawnableEntityManager.createFrom(living, data);
            if(soulEntity != null && serverLevel.addFreshEntity(soulEntity)){
                return soulEntity;
            }
        }
        return null;
    }

    @Nullable
    public static SoulEntity createSoulEntity(LivingEntity living, int timer, boolean soulEffect) {
        if(living.level() instanceof ServerLevel serverLevel) {
            SoulEntity soulEntity = new SoulEntity(serverLevel, timer, living);
            soulEntity.setUseSoulEffect(soulEffect);
            if(serverLevel.addFreshEntity(soulEntity)){
                return soulEntity;
            }
        }
        return null;
    }

}
