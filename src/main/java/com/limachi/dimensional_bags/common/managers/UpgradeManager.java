package com.limachi.dimensional_bags.common.managers;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.WorldUtils;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.ClientDataManager;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.WorldSavedDataManager;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.managers.upgrades.*;
import com.limachi.dimensional_bags.common.recipes.Smithing;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class UpgradeManager extends WorldSavedDataManager.EyeWorldSavedData {

    public static final Map<String, Upgrade> UPGRADES = new HashMap<>();

    static {
        new RowUpgrade().attach(UPGRADES);
        new ColumnUpgrade().attach(UPGRADES);
        new RadiusUpgrade().attach(UPGRADES);
        new SlotUpgrade(1).attach(UPGRADES);
        new SlotUpgrade(4).attach(UPGRADES);
        new SlotUpgrade(9).attach(UPGRADES);
        new ElytraUpgrade().attach(UPGRADES);
        new TankUpgrade().attach(UPGRADES);
        new TankCapacityUpgrade().attach(UPGRADES);
        new EnergyUpgrade().attach(UPGRADES);
        new KineticGeneratorUpgrade().attach(UPGRADES);
    }

    public static Upgrade getUpgrade(String id) { return UPGRADES.get(id); }

    public static Upgrade getUpgrade(Item item) {
        for (String key : UPGRADES.keySet())
            if (UPGRADES.get(key).getItem().equals(item))
                return UPGRADES.get(key);
        return null;
    }

    public static void bakeConfig() {
        for (String key : UPGRADES.keySet())
            UPGRADES.get(key).bakeConfig();
    }

    public static void buildConfig(ForgeConfigSpec.Builder builder) {
        for (String key : UPGRADES.keySet())
            UPGRADES.get(key).buildConfig(builder);
    }

    public static void registerItems(DeferredRegister<Item> itemsReg) {
        for (String key : UPGRADES.keySet())
            UPGRADES.get(key).register(itemsReg);
    }

    public static Item[] allUpgradesAsItem() {
        Item[] out = new Item[UPGRADES.keySet().size()];
        int i = 0;
        for (String key : UPGRADES.keySet())
            out[i++] = UPGRADES.get(key).getItem();
        return out;
    }

    public static void registerRecipes(IForgeRegistry<IRecipeSerializer<?>> registry) {
        new Smithing(
                new ResourceLocation(MOD_ID, "upgrade_application"),
                ()-> NonNullList.from(Ingredient.EMPTY, Ingredient.fromItems(Registries.BAG_ITEM.get()), Ingredient.fromItems(allUpgradesAsItem())),
                NonNullList.create(),
                (IInventory inv, World world) -> {
                    if (inv.getStackInSlot(0).getItem() instanceof Bag && Bag.getEyeId(inv.getStackInSlot(0)) > 0 && inv.getStackInSlot(1).getItem() instanceof Upgrade.UpgradeItem) {
                        Upgrade upgrade = UpgradeManager.getUpgrade(inv.getStackInSlot(1).getItem());
                        if (upgrade == null) return false;
                        UpgradeManager manager = UpgradeManager.getInstance(null, Bag.getEyeId(inv.getStackInSlot(0)));
                        if (manager == null) return false;
                        return manager.upgradesCount.get(upgrade.getId()) < upgrade.getLimit();
                    }
                    return false;
                },
                (IInventory inv) -> {
                    ItemStack out = inv.getStackInSlot(0).copy();
//                    int eyeId = Bag.getEyeId(out);
                    Upgrade upgrade = UpgradeManager.getUpgrade(inv.getStackInSlot(1).getItem());
//                    ClientDataManager clientDataManager = ClientDataManager.getInstance(inv.getStackInSlot(1));
//                    UpgradeManager manager = UpgradeManager.getInstance(null, eyeId);
//                    manager.installUpgrade(upgrade.getId(), out, 1);
                    installUpgrade(upgrade.getId(), out, 1, true);
                    return out;
                }).register(registry);
    }

    private CompoundNBT upgradesNBT;
    private HashMap<String, Integer> upgradesCount;
//    private int id;

    public UpgradeManager(int id) {
        this("upgrade_manager", id, true);
    }

    public UpgradeManager(String suffix, int id, boolean client) {
        super(suffix, id, client);
//        super(DimBag.MOD_ID + "_eye_" + id + "_upgrade_manager");
        upgradesNBT = new CompoundNBT();
        upgradesCount = new HashMap<>();
//        this.id = id;
        for (String key : UPGRADES.keySet())
            upgradesCount.put(key, UPGRADES.get(key).getStart());
    }

    public CompoundNBT getUpgradesNBT() { return upgradesNBT; }

    public static Set<String> getUpgradesNames() { return UPGRADES.keySet(); }

    public ArrayList<String> getInstalledUpgrades() {
        ArrayList<String> out = new ArrayList<>();
        for (String key : UPGRADES.keySet())
            if (UPGRADES.get(key).getCount(upgradesCount) > 0)
                out.add(key);
        return out;
    }

    public HashMap<String, Integer> getUpgradesCountMap() { return upgradesCount; }

    public int getUpgradeCount(String name) {
        Upgrade upgrade = getUpgrade(name);
        if (upgrade != null)
            return upgrade.getCount(upgradesCount);
        return 0;
    }

    public static void installUpgrade(String name, ItemStack stack, int amount, boolean preview) {
        int id;
        if (getUpgradesNames().contains(name) && (id = Bag.getEyeId(stack)) > 0) {
            Upgrade upgrade = getUpgrade(name);
            upgrade.installUpgrade(id, stack, amount, preview);
            UpgradeManager upgradeManager = null;
            ClientDataManager clientDataManager = null;
            if (preview) {
                clientDataManager = ClientDataManager.getInstance(stack);
                upgradeManager = clientDataManager.getUpgradeManager();
            } else
                upgradeManager = UpgradeManager.getInstance(null, id);
            if (upgradeManager != null) {
                upgrade.changeCount(upgradeManager.upgradesCount, upgrade.getCount(upgradeManager.upgradesCount) + amount);
                if (preview)
                    clientDataManager.store(stack);
                else
                    upgradeManager.markDirty();
            }
        }
    }

    @Override
    public void read(CompoundNBT nbt) {
        upgradesNBT = nbt.getCompound("UpgradesNBT");
        upgradesCount = new HashMap<>();
        for (String key : UPGRADES.keySet())
            upgradesCount.put(key, nbt.getInt(key + "_count"));
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.put("UpgradesNBT", upgradesNBT);
        for (String key : upgradesCount.keySet())
            nbt.putInt(key + "_count", upgradesCount.get(key));
        return nbt;
    }

//    static public UpgradeManager getInstance(@Nullable ServerWorld world, int id) {
//        if (id <= 0) return null;
//        if (world == null)
//            world = WorldUtils.getOverWorld();
//        if (world != null)
//            return world.getSavedData().getOrCreate(()->new UpgradeManager(id), DimBag.MOD_ID + "_eye_" + id + "_upgrade_manager");
//        return null;
//    }

    static public UpgradeManager getInstance(@Nullable ServerWorld world, int id) {
        return WorldSavedDataManager.getInstance(UpgradeManager.class, world, id);
    }
}