package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.limachi.dimensional_bags.common.data.IEyeIdHolder;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.entity.BagEntityItem;
import com.limachi.dimensional_bags.common.managers.ModeManager;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * this class, even if it has client in the name, is also partly used on the server to write nbt
 */
public class ClientDataManager {

    private final int id;
    private UpgradeManager localUpgrades;
    private ModeManager localMode;
    private String ownerName = "";

    public ClientDataManager(int id) {
        this.id = id;
    }

    public int getId() { return id; }

    public String getOwnerName() { return ownerName; }

    /**
     * actually called server side to prepare the nbt to be sent client side via itemstack/itementity/entity sync
     */
    public ClientDataManager(int id, UpgradeManager upgradeManager, ModeManager modeManager, OwnerData ownerData) {
        this.id = id;
        localUpgrades = new UpgradeManager(id);
        localUpgrades.read(upgradeManager.write(new CompoundNBT()));
        localMode = new ModeManager(id);
        localMode.read(modeManager.write(new CompoundNBT()));
        ownerName = ownerData.getPlayerName();
    }

    /**
     * actually called server side to prepare the nbt to be sent client side via itemstack/itementity/entity sync
     */
    public static ClientDataManager getInstance(int eyeId) {
        return new ClientDataManager(eyeId, UpgradeManager.getInstance(eyeId), ModeManager.getInstance(eyeId), OwnerData.getInstance(eyeId));
    }

    /**
     * sync the date from the stack to the worldsaveddate, overwritting and calling the upgrades that got changed
     */
    public void syncToServer(ItemStack bagItem) {
        UpgradeManager.execute(id, upgradeManager -> {
            for (String name : localUpgrades.getInstalledUpgrades()) {
                int d = localUpgrades.getUpgradeCount(name) - upgradeManager.getUpgradeCount(name);
                if (d != 0)
                    UpgradeManager.installUpgrade(name, bagItem, d, false);
            }
        });

    }

    /**
     * reload the data from the worldsaveddatas, would be like `this = new ClientDataManager(id, UpgradeManager.getInstance(null, id), ModeManager.getInstance(null, id), OwnerData.getInstance(null, id))`
     */
    public void syncFromServer() {

    }

    public static ClientDataManager getInstance(ItemStack bagItem) {
        ClientDataManager out = new ClientDataManager(Bag.getEyeId(bagItem));
        if (bagItem.getTag() != null)
            out.read(bagItem.getTag());
        return out;
    }

    public static ClientDataManager getInstance(BagEntityItem bagEntityItem) {
        return getInstance(bagEntityItem.getItem());
    }

    public static ClientDataManager getInstance(BagEntity bagEntity) {
        ClientDataManager out = new ClientDataManager(bagEntity.getEyeId());
        out.read(bagEntity.getPersistentData());
        return out;
    }

    public void store(ItemStack bagItem) {
        if (bagItem.getTag() == null)
            bagItem.setTag(new CompoundNBT());
        write(bagItem.getTag());
    }

    public void store(BagEntityItem bagEntityItem) {
        ItemStack stack = bagEntityItem.getItem();
        store(stack);
        bagEntityItem.setItem(stack);
    }

    public void store(BagEntity bagEntity) {
        write(bagEntity.getPersistentData());
    }

    public UpgradeManager getUpgradeManager() { return localUpgrades; }

    public ModeManager getModeManager() { return localMode; }

    private void read(CompoundNBT nbt) {
        localUpgrades = new UpgradeManager(id);
        localUpgrades.read(nbt.getCompound("LocalUpgradeManager"));
        localMode = new ModeManager(id);
        localMode.read(nbt.getCompound("LocalModeManager"));
        ownerName = nbt.getString("OwnerName");
    }

    private CompoundNBT write(CompoundNBT nbt) {
        nbt.putInt(IEyeIdHolder.EYE_ID_KEY, id);
        nbt.put("LocalUpgradeManager", localUpgrades.write(new CompoundNBT()));
        nbt.put("LocalModeManager", localMode.write(new CompoundNBT()));
        nbt.putString("OwnerName", ownerName);
        return nbt;
    }

    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity> void onRenderEquippedBag(BipedModel<T> entityModel, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (localUpgrades != null)
            for (String upgrade : localUpgrades.getInstalledUpgrades())
                UpgradeManager.getUpgrade(upgrade).onRenderEquippedBag(id, entityModel, matrixStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        if (localMode != null)
            for (String mode : localMode.getInstalledModes())
                ModeManager.getMode(mode).onRenderEquippedBag(id, localMode.getSelectedMode().equals(mode), entityModel, matrixStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
    }

    @OnlyIn(Dist.CLIENT)
    public void onRenderHud(ClientPlayerEntity player, MainWindow window, MatrixStack matrixStack, float partialTicks) {
        if (localUpgrades != null)
            for (String upgrade : localUpgrades.getInstalledUpgrades())
                UpgradeManager.getUpgrade(upgrade).onRenderHud(id, player, window, matrixStack, partialTicks);
        if (localMode != null)
            for (String mode : localMode.getInstalledModes())
                ModeManager.getMode(mode).onRenderHud(id, localMode.getSelectedMode().equals(mode), player, window, matrixStack, partialTicks);
    }

    @OnlyIn(Dist.CLIENT)
    public void onRenderBagEntity(BagEntity entity, float yaw, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int packedLight) {
        if (localUpgrades != null)
            for (String upgrade : localUpgrades.getInstalledUpgrades())
                UpgradeManager.getUpgrade(upgrade).onRenderBagEntity(id, entity, yaw, partialTicks, matrix, buffer, packedLight);
        if (localMode != null)
            for (String mode : localMode.getInstalledModes())
                ModeManager.getMode(mode).onRenderBagEntity(id, localMode.getSelectedMode().equals(mode), entity, yaw, partialTicks, matrix, buffer, packedLight);
    }
}
