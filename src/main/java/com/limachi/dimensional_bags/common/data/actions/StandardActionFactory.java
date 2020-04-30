package com.limachi.dimensional_bags.common.data.actions;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.dimensions.BagDimension;
import com.limachi.dimensional_bags.common.network.Network;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

public class StandardActionFactory {

    public static IBagAction inventoryGUI() {
        return new IBagAction() {
            @Override
            public String getName() {
                return "Open Inventory GUI";
            }

            @Override
            public void execute(ServerPlayerEntity player, Hand hand, EyeData data) {
                Network.openGUIEye(player, data, player.getHeldItem(hand).getTranslationKey());
            }

            @Override
            public ResourceLocation getIcon() {
                return new ResourceLocation(MOD_ID, "textures/items/bag.png");
            }
        };
    }

    public static IBagAction upgradesGUI() {
        return new IBagAction() {
            @Override
            public String getName() {
                return "Open Upgrades GUI";
            }

            @Override
            public void execute(ServerPlayerEntity player, Hand hand, EyeData data) {
                Network.openGUIUpgrades(player, data, player.getHeldItem(hand).getTranslationKey());
            }

            @Override
            public ResourceLocation getIcon() {
                return new ResourceLocation("textures/item/piston.png");
            }
        };
    }

    public static IBagAction actionsGUI() {
        return new IBagAction() {
            @Override
            public String getName() {
                return "Open actions GUI";
            }

            @Override
            public void execute(ServerPlayerEntity player, Hand hand, EyeData data) {
                Network.openGUIActions(player, data, player.getHeldItem(hand).getTranslationKey());
            }

            @Override
            public ResourceLocation getIcon() {
                return new ResourceLocation("textures/item/redstone.png");
            }
        };
    }

    public static IBagAction teleportPlayer() {
        return new IBagAction() {
            @Override
            public String getName() {
                return "Enter the bag room";
            }

            @Override
            public void execute(ServerPlayerEntity player, Hand hand, EyeData data) {
                BagDimension.teleportToRoom(player, data.getId().getId());
            }

            @Override
            public ResourceLocation getIcon() {
                return new ResourceLocation("textures/item/ender_eye.png");
            }
        };
    }
}
