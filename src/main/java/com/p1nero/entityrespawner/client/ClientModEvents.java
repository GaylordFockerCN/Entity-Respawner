package com.p1nero.entityrespawner.client;

import com.p1nero.entityrespawner.EntityRespawnerMod;
import com.p1nero.entityrespawner.entity.EntityRespawnerEntities;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = EntityRespawnerMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(EntityRespawnerEntities.SOUL_ENTITY.get(), EmptyRenderer::new);
    }
}
