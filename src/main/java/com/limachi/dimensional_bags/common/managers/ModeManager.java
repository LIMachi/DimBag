package com.limachi.dimensional_bags.common.managers;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.managers.modes.Debug;
import com.limachi.dimensional_bags.common.managers.modes.Default;
import com.limachi.dimensional_bags.common.managers.modes.PokeBall;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.*;

public class ModeManager {
    public static final Map<String, Mode> MODES = new HashMap<>();
    static {
        new Default().attach(MODES);
        new Debug().attach(MODES);
        new PokeBall().attach(MODES);
    }

    private String selectedMode;
    private ArrayList<String> installedModes;
    private EyeData data;

    public ModeManager(EyeData data) {
        selectedMode = Default.ID;
        installedModes = new ArrayList<>();
        for (String key : MODES.keySet())
            if (MODES.get(key).IS_INSTALED_BY_DEFAULT)
                installedModes.add(key);
        this.data = data;
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
        ActionResultType res = MODES.get(selectedMode).onEntityTick(data, stack, world, player, itemSlot, isSelected);
        if (res.isSuccessOrConsume()) return;
        for (int i = installedModes.size() - 1; i >= 0; --i) {
            String name = installedModes.get(i);
            if (name.equals(selectedMode) || name.equals("Default") || !MODES.get(name).CAN_BACKGROUND) continue;
            res = MODES.get(name).onEntityTick(data, stack, world, player, itemSlot, isSelected);
            if (res.isSuccessOrConsume()) return;
        }
        MODES.get("Default").onEntityTick(data, stack, world, player, itemSlot, isSelected);
    }

    public ActionResultType onItemUse(ItemUseContext context) {
        ActionResultType res = MODES.get(selectedMode).onItemUse(data, context);
        if (res.isSuccessOrConsume()) return res;
        for (int i = installedModes.size() - 1; i >= 0; --i) {
            String name = installedModes.get(i);
            if (name.equals(selectedMode) || name.equals("Default") || !MODES.get(name).CAN_BACKGROUND) continue;
            res = MODES.get(name).onItemUse(data, context);
            if (res.isSuccessOrConsume()) return res;
        }
        return MODES.get("Default").onItemUse(data, context);
    }

    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ActionResult<ItemStack> res = MODES.get(selectedMode).onItemRightClick(data, world, player, hand);
        if (res.getType().isSuccessOrConsume()) return res;
        for (int i = installedModes.size() - 1; i >= 0; --i) {
            String name = installedModes.get(i);
            if (name.equals(selectedMode) || name.equals("Default") || !MODES.get(name).CAN_BACKGROUND) continue;
            res = MODES.get(name).onItemRightClick(data, world, player, hand);
            if (res.getType().isSuccessOrConsume()) return res;
        }
        return MODES.get("Default").onItemRightClick(data, world, player, hand);
    }

    public ActionResultType onAttack(ItemStack stack, PlayerEntity player, Entity entity) {
        ActionResultType res = MODES.get(selectedMode).onAttack(data, stack, player, entity);
        if (res.isSuccessOrConsume()) return res;
        for (int i = installedModes.size() - 1; i >= 0; --i) {
            String name = installedModes.get(i);
            if (name.equals(selectedMode) || name.equals("Default") || !MODES.get(name).CAN_BACKGROUND) continue;
            res = MODES.get(name).onAttack(data, stack, player, entity);
            if (res.isSuccessOrConsume()) return res;
        }
        return MODES.get("Default").onAttack(data, stack, player, entity);
    }
}
