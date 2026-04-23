package com.p1nero.entityrespawner;

import com.p1nero.entityrespawner.data.RespawnableEntityManager;
import com.p1nero.entityrespawner.entity.EntityRespawnerEntities;
import com.p1nero.entityrespawner.entity.SoulEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.Nullable;

@Mod(EntityRespawnerMod.MOD_ID)
public class EntityRespawnerMod {

    public static final String MOD_ID = "entityrespawner";

    public EntityRespawnerMod(FMLJavaModLoadingContext context) {
        EntityRespawnerEntities.REGISTRY.register(context.getModEventBus());
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::onLivingDeath);
        MinecraftForge.EVENT_BUS.addListener(this::onLivingJoin);
        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListener);
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
