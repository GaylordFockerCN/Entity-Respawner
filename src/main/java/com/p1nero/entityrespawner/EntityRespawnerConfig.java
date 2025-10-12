package com.p1nero.entityrespawner;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = EntityRespawnerMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityRespawnerConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue RESPAWN_TIMER = BUILDER.comment("Respawn ticks after die.").defineInRange("respawn_timer", 1200, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.BooleanValue RESPAWN_WHEN_RELOAD = BUILDER.comment("Respawn when chunk reload.").define("respawn_when_reload", true);
    public static final ForgeConfigSpec.BooleanValue SOUL_EFFECT = BUILDER.comment("Display soul particle").define("soul_effect", true);
    // a list of strings that are treated as resource locations for entities
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ENTITIES_STRINGS = BUILDER.comment("A list of entities to respawn.").defineListAllowEmpty("entities", List.of("minecraft:pig"), EntityRespawnerConfig::validateEntitiesName);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static Set<EntityType<?>> entities;

    private static boolean validateEntitiesName(final Object obj) {
        return obj instanceof final String entityName && ForgeRegistries.ENTITY_TYPES.containsKey(ResourceLocation.parse(entityName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // convert the list of strings into a set of entities
        entities = ENTITIES_STRINGS.get().stream().map(entityName -> ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(entityName))).collect(Collectors.toSet());
    }
}
