package com.limachi.dimensional_bags.common.managers;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.IDimBagCommonItem;
import com.limachi.dimensional_bags.common.recipes.IRecipe;
import com.limachi.dimensional_bags.common.recipes.Smithing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraft.nbt.*;

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

    public ActionResultType upgradeCrafted(EyeData data, ItemStack stack, World world, Entity crafter) { return ActionResultType.PASS; } //called when the upgrade is 'crafted' (when the 'apply' command is run on a bag)
    public ActionResultType upgradePlayerTick(EyeData data, ItemStack stack, World world, Entity player, int itemSlot, boolean isSelected) { return ActionResultType.PASS; } //called while the bag is ticking inside a player inventory
    public ActionResultType upgradeEntityTick(EyeData data, ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) { return ActionResultType.PASS; } //called every X ticks by the bag manager
    public ActionResultType onItemUse(EyeData data, ItemUseContext context) { return ActionResultType.PASS; } //called when the bag is right clicked on something, before the bag does anything
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) { return ActionResult.resultPass(player.getHeldItem(hand)); } //called when the bag is right clicked in the air or shift-right-clicked, before the bag does anything (except set the id if needed and accessing data)
    public ActionResultType onAttack(EyeData data, ItemStack stack, PlayerEntity player, Entity entity) { return ActionResultType.PASS; } //called when the bag is left-clicked on an entity

    public CompoundNBT write(CompoundNBT nbt, boolean isItem) { return nbt; } //called by both eyedata and the bag item to store data on the bag/eye
    public void read(CompoundNBT nbt, boolean isItem) {} //called by both eyedata and the bag item to get data from the bag/eye

    public final int getCount(EyeData data) { return getMemory(data).getInt("Count"); }
    public final void setCount(EyeData data, int count) {
        CompoundNBT nbt = getMemory(data);
        nbt.putInt("Count", count);
        setMemory(data, nbt);
    }

    public final int incrementInt(EyeData data, String id, int add) {
        int t;
        CompoundNBT nbt = getMemory(data);
        nbt.putInt(id, t = nbt.getInt(id) + add);
        setMemory(data, nbt);
        return t;
    }

    public CompoundNBT getMemory(EyeData data) { return data.getUpgradesNBT().getCompound(this.sId); }
    public void setMemory(EyeData data, CompoundNBT nbt) { data.getUpgradesNBT().put(this.sId, nbt); }
    public void shallowAddMemory(EyeData data, CompoundNBT nbt) {
        CompoundNBT t = getMemory(data);
        for (String key : nbt.keySet())
            t.put(key, nbt.get(key));
        setMemory(data, t);
    }
    private INBT deepAddNBTInternal(INBT to, INBT from) {
        if (from == null)
            return to;
        if (to == null
                || to.getType() != from.getType()
                || from.getType() == StringNBT.TYPE
                || from.getType() == ByteNBT.TYPE
                || from.getType() == DoubleNBT.TYPE
                || from.getType() == FloatNBT.TYPE
                || from.getType() == IntNBT.TYPE
                || from.getType() == LongNBT.TYPE
                || from.getType() == ShortNBT.TYPE)
            return from;
        if (from.getType() == CompoundNBT.TYPE) {
            CompoundNBT tc = (CompoundNBT) to;
            CompoundNBT fc = (CompoundNBT) from;
            for (String key : fc.keySet()) {
                INBT p = deepAddNBTInternal(tc.get(key), fc.get(key));
                if (p != null)
                    tc.put(key, p);
            }
            return to;
        }
        if (from.getType() == ByteArrayNBT.TYPE
                || from.getType() == IntArrayNBT.TYPE
                || from.getType() == ListNBT.TYPE
                || from.getType() == LongArrayNBT.TYPE) {
            int sf = ((CollectionNBT) from).size();
            int st = ((CollectionNBT) to).size();
            for (int i = 0; i < sf; ++i)
                if (i < st)
                    ((CollectionNBT) to).set(i, deepAddNBTInternal((INBT) ((CollectionNBT) to).get(i), (INBT) ((CollectionNBT) from).get(i)));
                else
                    ((CollectionNBT) to).add(i, (INBT) ((CollectionNBT) from).get(i));
                return to;
        }
        return null;
    }
    public void deepAddMemory(EyeData data, CompoundNBT nbt) {
        setMemory(data, (CompoundNBT)deepAddNBTInternal(getMemory(data), nbt));
    }

    public IRecipe getRecipe() {
        return new Smithing(
                new ResourceLocation(MOD_ID, sId + "_application"),
                ()->NonNullList.from(Ingredient.EMPTY, Ingredient.fromItems(Registries.BAG_ITEM.get()), Ingredient.fromItems(getItem())),
                NonNullList.create(),
                (IInventory inv, World world) -> {
                    if (inv.getStackInSlot(0).getItem() instanceof Bag && inv.getStackInSlot(1).getItem() == getItem()) {
                        EyeData data = EyeData.get(null, Bag.getId(inv.getStackInSlot(0)));
                        if (data == null)
                            return false;
                        return this.getCount(data) < this.limit;
                    }
                    return false;
                },
                (IInventory inv) -> IDimBagCommonItem.addToStringList(inv.getStackInSlot(0).copy(), Bag.onTickCommands, "cmd.upgrade." + sId));
    }
}
