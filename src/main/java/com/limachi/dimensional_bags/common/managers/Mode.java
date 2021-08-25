package com.limachi.dimensional_bags.common.managers;

import com.google.common.collect.ImmutableMultimap;
import com.limachi.dimensional_bags.common.Registries;
//import com.limachi.dimensional_bags.common.data.EyeDataMK2.ClientDataManager;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Map;

/*
 * used by the bag to change binding/actions on the fly (meant to be changed by the instalation of upgrades)
 */

public class Mode {

    public static final String SETTING_GROUP = "Modes";

    public final boolean CAN_BACKGROUND;
    public final String NAME;
    public final boolean IS_INSTALLED_BY_DEFAULT;

    public final SettingsData.SettingsReader settingsReader;

    public Mode(String name, boolean background, boolean installed) {
        this.CAN_BACKGROUND = background;
        this.NAME = name;
        this.IS_INSTALLED_BY_DEFAULT = installed;
        settingsReader = new SettingsData.SettingsReader(SETTING_GROUP, NAME, ()->{
            ItemStack out = new ItemStack(Registries.getItem(Bag.NAME));
            if (out.getTag() == null)
                out.setTag(new CompoundNBT());
            out.getTag().putFloat("OVERRIDE_MODE_PROPERTY", ModeManager.getModeIndex(modeName()));
            return out;
        });
        initSettings(settingsReader);
        settingsReader.build();
    }

    public <T> T getSetting(int eye, String label) { return settingsReader.get(label, SettingsData.getInstance(eye)); }

    public <T> void setSetting(int eye, String label, T value) { settingsReader.set(label, SettingsData.getInstance(eye), value); }

    /**
     * here you should use the settings method to populate the settings, reading and writing should be done elsewhere
     * @param settingsReader
     */
    public void initSettings(SettingsData.SettingsReader settingsReader) {}

    public String modeName() { return NAME; }

    public final void attach(Map<String, Mode> col) { col.put(this.NAME, this); }

    public void installMode(int eyeId, ItemStack stack/*, boolean preview*/) {
        ModeManager modeManager = null;
//        ClientDataManager clientDataManager = null;
//        if (preview) {
//            clientDataManager = ClientDataManager.getInstance(stack);
//            modeManager = clientDataManager.getModeManager();
//        }
//        else
            modeManager = ModeManager.getInstance(eyeId);
        if (modeManager != null) {
            modeManager.installMode(NAME);
//            if (preview)
//                clientDataManager.store(stack);
        }
    }

    public boolean onScroll(PlayerEntity player, int eye, boolean up, boolean testOnly) { return false; }

    public ActionResultType onAddInformation(int eye, ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) { return ActionResultType.PASS; }

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
