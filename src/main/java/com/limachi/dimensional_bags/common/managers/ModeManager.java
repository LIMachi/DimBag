package com.limachi.dimensional_bags.common.managers;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.managers.modes.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import java.util.*;

public class ModeManager {
    public static final Mode[] MODES = {
        new Default(),
        new Debug(),
        new PokeBall(),
        new Elytra(),
        new Tank()
    };

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
    private EyeData data;

    public ModeManager(EyeData data) {
        selectedMode = Default.ID;
        installedModes = new ArrayList<>();
        for (Mode mode : MODES)
            if (mode.IS_INSTALED_BY_DEFAULT)
                installedModes.add(mode.NAME);
        this.data = data;
    }

    public void installMode(String name) {
        if (!installedModes.contains(name))
            installedModes.add(name);
    }

    public String getSelectedMode() { return selectedMode; }
    public ArrayList<String> getInstalledModes() { return installedModes; }
    public void selectMode(int i) {
        if (i < 0 || i >= installedModes.size()) return;
        selectedMode = installedModes.get(i);
    }
    public void selectMode(String mode) {
        if (installedModes.contains(mode))
            selectedMode = mode;
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
        ActionResultType res = getMode(selectedMode).onEntityTick(data, stack, world, player, itemSlot, isSelected);
        if (res != ActionResultType.PASS) return;
        for (int i = installedModes.size() - 1; i >= 0; --i) {
            String name = installedModes.get(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onEntityTick(data, stack, world, player, itemSlot, isSelected);
            if (res != ActionResultType.PASS) return;
        }
        getMode("Default").onEntityTick(data, stack, world, player, itemSlot, isSelected);
    }

    public ActionResultType onItemUse(World world, PlayerEntity player, int slot, BlockRayTraceResult ray) {
        ActionResultType res = getMode(selectedMode).onItemUse(data, world, player, slot, ray);
        if (res != ActionResultType.PASS) return res;
        for (int i = installedModes.size() - 1; i >= 0; --i) {
            String name = installedModes.get(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onItemUse(data, world, player, slot, ray);
            if (res != ActionResultType.PASS) return res;
        }
        return getMode("Default").onItemUse(data, world, player, slot, ray);
    }

    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, int slot) {
        ActionResult<ItemStack> res = getMode(selectedMode).onItemRightClick(data, world, player, slot);
        if (res.getType() != ActionResultType.PASS) return res;
        for (int i = installedModes.size() - 1; i >= 0; --i) {
            String name = installedModes.get(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onItemRightClick(data, world, player, slot);
            if (res.getType() != ActionResultType.PASS) return res;
        }
        return getMode("Default").onItemRightClick(data, world, player, slot);
    }

    public ActionResultType onAttack(ItemStack stack, PlayerEntity player, Entity entity) {
        ActionResultType res = getMode(selectedMode).onAttack(data, stack, player, entity);
        if (res != ActionResultType.PASS) return res;
        for (int i = installedModes.size() - 1; i >= 0; --i) {
            String name = installedModes.get(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onAttack(data, stack, player, entity);
            if (res != ActionResultType.PASS) return res;
        }
        return getMode("Default").onAttack(data, stack, player, entity);
    }

    public ActionResultType onActivateItem(ItemStack stack, PlayerEntity player) {
        ActionResultType res = getMode(selectedMode).onActivateItem(data, stack, player);
        if (res != ActionResultType.PASS) return res;
        for (int i = installedModes.size() - 1; i >= 0; --i) {
            String name = installedModes.get(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onActivateItem(data, stack, player);
            if (res != ActionResultType.PASS) return res;
        }
        return getMode("Default").onActivateItem(data, stack, player);
    }
}
