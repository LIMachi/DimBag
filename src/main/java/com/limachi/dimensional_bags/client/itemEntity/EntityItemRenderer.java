package com.limachi.dimensional_bags.client.itemEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.ItemRenderer;

public class EntityItemRenderer extends ItemRenderer {

    public EntityItemRenderer(EntityRendererManager renderManager) {
        super(renderManager, Minecraft.getInstance().getItemRenderer());
    }
}