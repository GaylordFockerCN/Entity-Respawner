package com.p1nero.entityrespawner.client;

import com.p1nero.entityrespawner.entity.SoulEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class EmptyRenderer extends EntityRenderer<SoulEntity> {
    protected EmptyRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(SoulEntity entity, Frustum frustum, double p_114493_, double p_114494_, double p_114495_) {
        return false;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull SoulEntity soulEntity) {
        return ResourceLocation.parse("");
    }
}
