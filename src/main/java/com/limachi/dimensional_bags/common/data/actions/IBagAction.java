package com.limachi.dimensional_bags.common.data.actions;

import com.limachi.dimensional_bags.common.data.EyeData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;

public interface IBagAction {
    String getName();
    void execute(ServerPlayerEntity player, Hand hand, EyeData data);
    ResourceLocation getIcon();
}
