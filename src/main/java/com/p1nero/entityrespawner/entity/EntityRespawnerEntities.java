package com.p1nero.entityrespawner.entity;

import com.p1nero.entityrespawner.EntityRespawnerMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = EntityRespawnerMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityRespawnerEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EntityRespawnerMod.MOD_ID);

    public static final RegistryObject<EntityType<SoulEntity>> SOUL_ENTITY = REGISTRY.register("soul_entity",
            () -> EntityType.Builder.<SoulEntity>of(SoulEntity::new, MobCategory.MISC).fireImmune().sized(0.25F, 0.25F)
                    .build(ResourceLocation.fromNamespaceAndPath(EntityRespawnerMod.MOD_ID, "soul_entity").toString()));

}
