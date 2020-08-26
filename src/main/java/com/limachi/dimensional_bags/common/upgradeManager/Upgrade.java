package com.limachi.dimensional_bags.common.upgradeManager;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.DimBagCommonItem;
import com.limachi.dimensional_bags.common.recipes.IRecipe;
import com.limachi.dimensional_bags.common.recipes.Recipe;
import com.limachi.dimensional_bags.common.recipes.Smithing;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public abstract class Upgrade { //contain all information for config, item, upgrade trigger, etc...
    private final String sId;
    private int start;
    private int limit;
    private final TranslationTextComponent description;

    int getStart() { return this.start; }
    int getLimit() { return this.limit; }
    String getId() { return this.sId; }

    private RegistryObject<Item> itemReg = null;

    private final boolean canConfig;
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

    public String getDescription() { return this.description./*getFormattedText()*/getString(); }

    public class UpgradeItem extends DimBagCommonItem {
        public UpgradeItem(int stackLimit) { super(new Item.Properties().maxStackSize(max)); }
    }

    void register(DeferredRegister<Item> itemRegister) { this.itemReg = itemRegister.register(this.sId, () -> new UpgradeItem(this.limit)); }

    Item getItem() { return this.itemReg == null ? Items.AIR : this.itemReg.get(); }

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

    protected abstract void applyUpgrade(EyeData data);

    public IRecipe getRecipe() {
        return new Smithing(
                new ResourceLocation(MOD_ID, sId + "_application"),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromItems(Registries.BAG_ITEM.get()), Ingredient.fromItems(getItem())),
                NonNullList.create(),
                (IInventory inv, World world) -> {
                    if (inv.getStackInSlot(0).getItem() instanceof Bag && inv.getStackInSlot(1).getItem() == getItem()) {
                        int id = UpgradeManager.getIdByStack(inv.getStackInSlot(1));
                        int qty = EyeData.get(null, Bag.getId(inv.getStackInSlot(0))).getUpgrade(id);
                        if (qty < UpgradeManager.getLimit(id))
                            return true;
                    }
                    return false;
                },
                (IInventory inv) -> Bag.addToStringList(Bag.addToStringList(inv.getStackInSlot(0).copy(), Bag.onCreateCommands, "cmd.upgrade." + sId), Bag.onTickCommands, "msg.Will apply a " + sId + "on the next player tick"));
    }
}
