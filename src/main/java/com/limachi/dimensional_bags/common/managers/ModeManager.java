package com.limachi.dimensional_bags.common.managers;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.WorldUtils;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.ClientDataManager;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.WorldSavedDataManager;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.IDimBagCommonItem;
import com.limachi.dimensional_bags.common.managers.modes.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;
import java.util.*;

public class ModeManager extends WorldSavedDataManager.EyeWorldSavedData {
    public static final Mode[] MODES = {
        new Default(),
        new Settings(),
//        new Debug(),
        new PokeBall(),
        new Elytra(),
        new Tank()
    };

    public static void changeModeRequest(ServerPlayerEntity player, int slot, boolean up) {
        ItemStack stack = IDimBagCommonItem.getItemFromPlayer(player, slot);
        int id;
        if (stack == null || !(stack.getItem() instanceof Bag) || (id = Bag.getEyeId(stack)) <= 0) return;
        ModeManager dataS = getInstance(null, id);
        if (dataS == null) return;
        ClientDataManager dataC = ClientDataManager.getInstance(stack);
        ArrayList<String> modes = dataS.getInstalledModes();
        for (int i = 0; i < modes.size(); ++i) {
            if (!modes.get(i).equals(dataS.getSelectedMode())) continue;
            dataS.selectMode((i + (up ? 1 : modes.size() - 1)) % modes.size());
            dataC.getModeManager().selectMode(dataS.getSelectedMode());
            dataC.store(stack);
            player.sendStatusMessage(new TranslationTextComponent("notification.bag.changed_mode", new TranslationTextComponent("bag.mode." + dataS.getSelectedMode())), true);
            return;
        }
    }

    public static int getModeIndex(String name) {
        for (int i = 0; i < MODES.length; ++i)
            if (MODES[i].NAME.equals(name))
                return i;
         return -1;
    }

    public static Mode getMode(String name) {
        int i;
        if ((i = getModeIndex(name)) == -1)
            return MODES[0];
        return MODES[i];
    }

    private String selectedMode;
    private ArrayList<String> installedModes;
//    private final int id;

    public ModeManager(int id) {
        this("mode_manager", id, true);
    }

    public ModeManager(String suffix, int id, boolean client) {
        super(suffix, id, client);
//        super(DimBag.MOD_ID + "_eye_" + id + "_mode_manager");
        selectedMode = Default.ID;
        installedModes = new ArrayList<>();
        for (Mode mode : MODES)
            if (mode.IS_INSTALED_BY_DEFAULT)
                installedModes.add(mode.NAME);
//        this.id = id;
    }

//    static public ModeManager getInstance(@Nullable ServerWorld world, int id) {
//        if (id <= 0) return null;
//        if (world == null)
//            world = WorldUtils.getOverWorld();
//        if (world != null)
//            return world.getSavedData().getOrCreate(()->new ModeManager(id), DimBag.MOD_ID + "_eye_" + id + "_mode_manager");
//        return null;
//    }

    static public ModeManager getInstance(@Nullable ServerWorld world, int id) {
        return WorldSavedDataManager.getInstance(ModeManager.class, world, id);
    }

    public void installMode(String name) {
        if (!installedModes.contains(name))
            installedModes.add(name);
        markDirty();
    }

    public String getSelectedMode() { return selectedMode; }

    public ArrayList<String> getInstalledModes() { return installedModes; }

    public void selectMode(int i) {
        if (i < 0 || i >= installedModes.size()) return;
        selectedMode = installedModes.get(i);
        markDirty();
    }

    public void selectMode(String mode) {
        if (installedModes.contains(mode))
            selectedMode = mode;
        markDirty();
    }

    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putString("Selected", selectedMode);
        ListNBT list = new ListNBT();
        for (String entry : installedModes)
            list.add(StringNBT.valueOf(entry));
        nbt.put("Installed", list);
        return nbt;
    }

    public void read(CompoundNBT nbt) {
        selectedMode = nbt.getString("Selected");
        if (selectedMode == null)
            selectedMode = "Default";
        ListNBT list = nbt.getList("Installed", 8);
        installedModes = new ArrayList<>();
        for (int i = 0; i < list.size(); ++i)
            installedModes.add(list.getString(i));
    }

    public void inventoryTick(ItemStack stack, World world, Entity player, int itemSlot, boolean isSelected) {
        ActionResultType res = getMode(selectedMode).onEntityTick(getEyeId(), stack, world, player, itemSlot, isSelected);
        if (res != ActionResultType.PASS) return;
        for (int i = installedModes.size() - 1; i >= 0; --i) {
            String name = installedModes.get(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onEntityTick(getEyeId(), stack, world, player, itemSlot, isSelected);
            if (res != ActionResultType.PASS) return;
        }
        getMode("Default").onEntityTick(getEyeId(), stack, world, player, itemSlot, isSelected);
    }

    public ActionResultType onItemUse(World world, PlayerEntity player, int slot, BlockRayTraceResult ray) {
        ActionResultType res = getMode(selectedMode).onItemUse(getEyeId(), world, player, slot, ray);
        if (res != ActionResultType.PASS) return res;
        for (int i = installedModes.size() - 1; i >= 0; --i) {
            String name = installedModes.get(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onItemUse(getEyeId(), world, player, slot, ray);
            if (res != ActionResultType.PASS) return res;
        }
        return getMode("Default").onItemUse(getEyeId(), world, player, slot, ray);
    }

    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, int slot) {
        ActionResult<ItemStack> res = getMode(selectedMode).onItemRightClick(getEyeId(), world, player, slot);
        if (res.getType() != ActionResultType.PASS) return res;
        for (int i = installedModes.size() - 1; i >= 0; --i) {
            String name = installedModes.get(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onItemRightClick(getEyeId(), world, player, slot);
            if (res.getType() != ActionResultType.PASS) return res;
        }
        return getMode("Default").onItemRightClick(getEyeId(), world, player, slot);
    }

    public ActionResultType onAttack(ItemStack stack, PlayerEntity player, Entity entity) {
        ActionResultType res = getMode(selectedMode).onAttack(getEyeId(), stack, player, entity);
        if (res != ActionResultType.PASS) return res;
        for (int i = installedModes.size() - 1; i >= 0; --i) {
            String name = installedModes.get(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onAttack(getEyeId(), stack, player, entity);
            if (res != ActionResultType.PASS) return res;
        }
        return getMode("Default").onAttack(getEyeId(), stack, player, entity);
    }

    public ActionResultType onActivateItem(ItemStack stack, PlayerEntity player) {
        ActionResultType res = getMode(selectedMode).onActivateItem(getEyeId(), stack, player);
        if (res != ActionResultType.PASS) return res;
        for (int i = installedModes.size() - 1; i >= 0; --i) {
            String name = installedModes.get(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onActivateItem(getEyeId(), stack, player);
            if (res != ActionResultType.PASS) return res;
        }
        return getMode("Default").onActivateItem(getEyeId(), stack, player);
    }
}
