package com.p1nero.entityrespawner.client;

import com.p1nero.entityrespawner.EntityRespawnerMod;
import com.p1nero.entityrespawner.entity.EntityRespawnerEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = EntityRespawnerMod.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRespawnerEntities.SOUL_ENTITY.get(), EmptyRenderer::new);
    }
}
