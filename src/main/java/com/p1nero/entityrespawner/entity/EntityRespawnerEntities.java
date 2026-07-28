package com.p1nero.entityrespawner.entity;

import com.p1nero.entityrespawner.EntityRespawnerMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EntityRespawnerEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(Registries.ENTITY_TYPE, EntityRespawnerMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<SoulEntity>> SOUL_ENTITY = REGISTRY.register("soul_entity",
            () -> EntityType.Builder.<SoulEntity>of(SoulEntity::new, MobCategory.MISC).fireImmune().sized(0.25F, 0.25F)
                    .build(ResourceLocation.fromNamespaceAndPath(EntityRespawnerMod.MOD_ID, "soul_entity").toString()));

}
