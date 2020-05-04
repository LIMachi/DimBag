package com.limachi.dimensional_bags.common.upgradeManager;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.data.EyeData;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public abstract class Upgrade { //contain all information for config, item, upgrade trigger, etc...
    private final String sId;
    private int start;
    private int limit;

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
        this.canConfig = canConfig;
        this.start = start;
        this.min = min;
        this.limit = limit;
        this.max = max;
    }

    public class UpgradeItem extends Item {
        public UpgradeItem(int stackLimit) {
            super(new Item.Properties().group(DimBag.ITEM_GROUP).maxStackSize(stackLimit));
        }
    }

    void register(DeferredRegister<Item> itemRegister) {
        this.itemReg = itemRegister.register(this.sId, () -> new UpgradeItem(this.limit));
    }

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

    protected abstract void applyUpgrade(int countBefore, int countAfter, EyeData data); //must accept a countBefore of 0 for first call
}
