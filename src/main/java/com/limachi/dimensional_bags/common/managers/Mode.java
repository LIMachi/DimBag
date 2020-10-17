package com.limachi.dimensional_bags.common.managers;

import com.google.common.collect.ImmutableMultimap;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.ClientDataManager;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;

/*
 * used by the bag to change binding/actions on the fly (meant to be changed by the instalation of upgrades)
 */

public class Mode {

    public final boolean CAN_BACKGROUND;
    public final String NAME;
    public final boolean IS_INSTALED_BY_DEFAULT;

    public Mode(String name, boolean background, boolean installed) {
        this.CAN_BACKGROUND = background;
        this.NAME = name;
        this.IS_INSTALED_BY_DEFAULT = installed;
    }

    public final void attach(Map<String, Mode> col) { col.put(this.NAME, this); }

    public void installMode(int eyeId, ItemStack stack, boolean preview) {
        ModeManager modeManager = null;
        ClientDataManager clientDataManager = null;
        if (preview) {
            clientDataManager = ClientDataManager.getInstance(stack);
            modeManager = clientDataManager.getModeManager();
        }
        else
            modeManager = ModeManager.getInstance(eyeId);
        if (modeManager != null) {
            modeManager.installMode(NAME);
            if (preview)
                clientDataManager.store(stack);
        }
    }

    public void getAttributeModifiers(int eyeId, boolean selected, EquipmentSlotType slot, ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {}
//    public ActionResultType onPlayerTick(int eyeId, ItemStack stack, World world, Entity player, int itemSlot, boolean isSelected) { return ActionResultType.CONSUME; } //called while the bag is ticking inside a player inventory
    public ActionResultType onEntityTick(int eyeId, World world, Entity entity, boolean isSelected) { return ActionResultType.PASS; } //called every X ticks by the bag manager
    public ActionResultType onItemUse(int eyeId, World world, PlayerEntity player, BlockRayTraceResult ray) { return ActionResultType.CONSUME; } //called when the bag is right clicked on something, before the bag does anything
    public ActionResultType onItemRightClick(int eyeId, World world, PlayerEntity player) { //called when the bag is right clicked in the air or shift-right-clicked, before the bag does anything (except set the id if needed and accessing data)
        return onActivateItem(eyeId, player);
    }
    public ActionResultType onAttack(int eyeId, PlayerEntity player, Entity entity) { return ActionResultType.CONSUME; } //called when the bag is left-clicked on an entity
    public ActionResultType onActivateItem(int eyeId, PlayerEntity playerEntity) { return ActionResultType.CONSUME; } //called when the client release the bag action key (or by default, right click the bag)
    @OnlyIn(Dist.CLIENT)
    public void onRenderHud(int eyeId, boolean isSelected, PlayerEntity player, MainWindow window, MatrixStack matrixStack, float partialTicks) {} //called each tick by the game overlay event
    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity> void onRenderEquippedBag(int eyeId, boolean isSelected, BipedModel<T> entityModel, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {}
    @OnlyIn(Dist.CLIENT)
    public void onRenderBagEntity(int eyeId, boolean isSelected, BagEntity entity, float yaw, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int packedLight) {}
    public boolean hasSettingsGUI() { return false; }
    @OnlyIn(Dist.CLIENT)
    public void initSettingsGUI() {}
    @OnlyIn(Dist.CLIENT)
    public void drawSettingsGUI() {}
}
