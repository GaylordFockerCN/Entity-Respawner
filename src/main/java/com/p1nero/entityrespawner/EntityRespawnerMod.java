package com.p1nero.entityrespawner;

import com.p1nero.entityrespawner.entity.EntityRespawnerEntities;
import com.p1nero.entityrespawner.entity.SoulEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.Nullable;

@Mod(EntityRespawnerMod.MOD_ID)
public class EntityRespawnerMod {

    public static final String MOD_ID = "entityrespawner";

    public EntityRespawnerMod(FMLJavaModLoadingContext context) {
        EntityRespawnerEntities.REGISTRY.register(context.getModEventBus());
        MinecraftForge.EVENT_BUS.addListener(this::onLivingDeath);
        MinecraftForge.EVENT_BUS.addListener(this::onLivingJoin);
        context.registerConfig(ModConfig.Type.COMMON, EntityRespawnerConfig.SPEC);
    }

    private void onLivingDeath(LivingDeathEvent event) {
        addToRespawn(event.getEntity());
    }

    private void onLivingJoin(EntityJoinLevelEvent event) {
        if(EntityRespawnerConfig.RESPAWN_WHEN_RELOAD.get()
                && event.getLevel() instanceof ServerLevel serverLevel
                && event.loadedFromDisk()
                && event.getEntity() instanceof SoulEntity soulEntity) {
            soulEntity.respawnEntity(serverLevel);
        }
    }

    @Nullable
    public static SoulEntity addToRespawn(LivingEntity living) {
        return addToRespawn(living, EntityRespawnerConfig.RESPAWN_TIMER.get(), EntityRespawnerConfig.SOUL_EFFECT.get());
    }

    @Nullable
    public static SoulEntity addToRespawn(LivingEntity living, int timer, boolean soulEffect) {
        //防止刷两只
        if(living.getPersistentData().getBoolean(MOD_ID + "mark")) {
            return null;
        }
        if(living.level() instanceof ServerLevel serverLevel && EntityRespawnerConfig.entities.contains(living.getType())) {
            SoulEntity soulEntity = new SoulEntity(serverLevel, timer, living);
            soulEntity.setUseSoulEffect(soulEffect);
            if(serverLevel.addFreshEntity(soulEntity)){
                living.getPersistentData().putBoolean(MOD_ID + "mark", true);
                return soulEntity;
            }
        }
        return null;
    }

}
