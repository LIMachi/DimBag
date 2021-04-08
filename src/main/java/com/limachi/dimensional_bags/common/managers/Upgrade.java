package com.limachi.dimensional_bags.common.managers;
import com.google.common.collect.ImmutableMultimap;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.utils.NBTUtils;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.IDimBagCommonItem;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import java.util.Map;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public abstract class Upgrade { //contain all information for config, item, upgrade trigger, etc...
    private final String sId;
    private int start;
    private int limit;
    private final TranslationTextComponent description;

    public int getStart() { return this.start; }
    public int getLimit() { return this.limit; }
    public String getId() { return this.sId; }

    public final void attach(Map<String, Upgrade> col) { col.put(this.sId, this); }

    private RegistryObject<Item> itemReg = null;

    protected boolean canConfig;
    private final String cId;
    private int min;
    private int max;
    private ForgeConfigSpec.IntValue cStart;
    private ForgeConfigSpec.IntValue cLimit;

    protected Upgrade(String id, boolean canConfig, int start, int limit, int min, int max) {
        this.sId = "upgrade_" + id;
        this.cId = MOD_ID + ".config.upgrade." + id;
        this.description = new TranslationTextComponent("inventory.upgrades." + id + ".description");
        this.canConfig = canConfig;
        this.start = start;
        this.min = min;
        this.limit = limit;
        this.max = max;
    }

    public ITextComponent getBaseName() { return new TranslationTextComponent("item." + MOD_ID + "." + sId); }

    public String getDescription() { return this.description./*getFormattedText()*/getString(); }

    public class UpgradeItem extends Item implements IDimBagCommonItem {
        public UpgradeItem(int stackLimit) { super(new Item.Properties().group(DimBag.ITEM_GROUP).maxStackSize(max)); }
    }

    void register(DeferredRegister<Item> itemRegister) { this.itemReg = itemRegister.register(this.sId, () -> new UpgradeItem(this.limit)); }

    public Item getItem() { return this.itemReg == null ? Items.AIR : this.itemReg.get(); }

    void buildConfig(ForgeConfigSpec.Builder builder) {
        if (!this.canConfig) return;
        this.cStart = builder.comment("initial amount of '" + this.sId + "' upgradesManager").translation(this.cId + ".start").defineInRange(this.cId + ".start", this.start, this.min, this.max);
        this.cLimit = builder.comment("maximum amount of '" + this.sId + "' upgradesManager").translation(this.cId + ".limit").defineInRange(this.cId + ".limit", this.limit, this.min, this.max);
    }

    void bakeConfig() {
        if (!this.canConfig) return;
        this.start = this.cStart.get();
        this.limit = this.cLimit.get();
    }

    public boolean hasSettingsGUI() { return false; }
    @OnlyIn(Dist.CLIENT)
    public void initSettingsGUI() {}
    @OnlyIn(Dist.CLIENT)
    public void drawSettingsGUI() {}

    public void getAttributeModifiers(int eyeId, EquipmentSlotType slot, ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {}
    public void installUpgrade(int eyeId, ItemStack stack, int amount, boolean preview) {}
    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity> void onRenderEquippedBag(int eyeId, BipedModel<T> entityModel, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {}
    @OnlyIn(Dist.CLIENT)
    public void onRenderHud(int eyeId, PlayerEntity player, MainWindow window, MatrixStack matrixStack, float partialTicks) {} //called each tick by the game overlay event
    @OnlyIn(Dist.CLIENT)
    public void onRenderBagEntity(int eyeId, BagEntity entity, float yaw, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int packedLight) {}
//    public ActionResultType upgradePlayerTick(EyeData data, ItemStack stack, World world, Entity player, int itemSlot, boolean isSelected) { return ActionResultType.PASS; } //called while the bag is ticking inside a player inventory
    public ActionResultType upgradeEntityTick(int eyeId, World world, Entity entity) { return ActionResultType.PASS; } //called every X ticks by the bag manager
//    public ActionResultType onItemUse(EyeData data, ItemUseContext context) { return ActionResultType.PASS; } //called when the bag is right clicked on something, before the bag does anything
//    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) { return ActionResult.resultPass(player.getHeldItem(hand)); } //called when the bag is right clicked in the air or shift-right-clicked, before the bag does anything (except set the id if needed and accessing data)
//    public ActionResultType onAttack(EyeData data, ItemStack stack, PlayerEntity player, Entity entity) { return ActionResultType.PASS; } //called when the bag is left-clicked on an entity
//    @OnlyIn(Dist.CLIENT)
//    public void onRenderHud(EyeData data, PlayerEntity player, MainWindow window, MatrixStack matrixStack, float partialTicks) {} //called each tick by the game overlay event
//    @OnlyIn(Dist.CLIENT)
//    public <T extends LivingEntity> void onRenderEquippedBag(EyeData data, BipedModel<T> entityModel, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {}

//    public CompoundNBT write(CompoundNBT nbt, boolean isItem) { return nbt; } //called by both eyedata and the bag item to store data on the bag/eye
//    public void read(CompoundNBT nbt, boolean isItem) {} //called by both eyedata and the bag item to get data from the bag/eye

//    public final int getCount(UpgradeManager manager) { return getMemory(manager).getInt("Count"); }

//    public final void setCount(UpgradeManager manager, int count) {
//        CompoundNBT nbt = getMemory(manager);
//        nbt.putInt("Count", count);
//        setMemory(manager, nbt);
//    }

//    public final int incrementInt(UpgradeManager manager, String id, int add) {
//        int t;
//        CompoundNBT nbt = getMemory(manager);
//        nbt.putInt(id, t = nbt.getInt(id) + add);
//        setMemory(manager, nbt);
//        return t;
//    }

//    public int getCount(UpgradeManager manager) {
//        if (manager == null) return 0;
//        return getCount(manager.getUpgradesCountMap());
//    }

//    public int getCount(HashMap<String, Integer> map) {
//        return map.get(sId);
//    }

//    public void changeCount(HashMap<String, Integer> map, int count) {
//        map.put(sId, count);
//    }

    public CompoundNBT getMemory(UpgradeManager manager) { return manager.getUpgradesNBT().getCompound(this.sId); }

    public void setMemory(UpgradeManager manager, CompoundNBT nbt) { manager.getUpgradesNBT().put(this.sId, nbt); manager.markDirty(); }

    public void shallowAddMemory(UpgradeManager manager, CompoundNBT nbt) {
        CompoundNBT t = getMemory(manager);
        for (String key : nbt.keySet())
            t.put(key, nbt.get(key));
        setMemory(manager, t);
    }

    public void deepAddMemory(UpgradeManager manager, CompoundNBT nbt) {
        setMemory(manager, (CompoundNBT) NBTUtils.deepMergeNBTInternal(getMemory(manager), nbt));
    }
}
