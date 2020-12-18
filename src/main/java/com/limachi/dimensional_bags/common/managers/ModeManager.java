package com.limachi.dimensional_bags.common.managers;

import com.google.common.collect.ImmutableMultimap;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.ClientDataManager;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.WorldSavedDataManager;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.GhostBag;
import com.limachi.dimensional_bags.common.items.IDimBagCommonItem;
import com.limachi.dimensional_bags.common.managers.modes.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModeManager extends WorldSavedDataManager.EyeWorldSavedData {
    public static final Mode[] MODES = {
        new Default(),
        new Settings(),
        new PokeBall(),
        new Elytra(),
        new Tank(),
        new Automation()
    };

    public static void changeModeRequest(ServerPlayerEntity player, int slot, boolean up) {
        ItemStack stack = IDimBagCommonItem.getItemFromPlayer(player, slot);
        int id;
        if (stack == null || !(stack.getItem() instanceof Bag || stack.getItem() instanceof GhostBag) || (id = Bag.getEyeId(stack)) <= 0) return;
        ModeManager dataS = getInstance(id);
        if (dataS == null) return;
        ClientDataManager dataC = ClientDataManager.getInstance(stack);
        ArrayList<String> modes = dataS.getInstalledModes();
        for (int i = 0; i < modes.size(); ++i) {
            if (!modes.get(i).equals(dataS.getSelectedMode()) || dataS.getModesNBT().getCompound(modes.get(i)).getBoolean("Disabled")) continue;
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
    private CompoundNBT modesNBT;

    public ModeManager(int id) {
        this("mode_manager", id, true);
    }

    public ModeManager(String suffix, int id, boolean client) {
        super(suffix, id, client);
        selectedMode = Default.ID;
        modesNBT = new CompoundNBT();
        installedModes = new ArrayList<>();
        for (Mode mode : MODES)
            if (mode.IS_INSTALED_BY_DEFAULT)
                installModeNotDirty(mode.NAME);
    }

    public CompoundNBT getModesNBT() { return modesNBT; }

    static public ModeManager getInstance(int id) {
        return WorldSavedDataManager.getInstance(ModeManager.class, null, id);
    }

    static public <T> T execute(int id, Function<ModeManager, T> executable, T onErrorReturn) {
        return WorldSavedDataManager.execute(ModeManager.class, null, id, executable, onErrorReturn);
    }

    static public boolean execute(int id, Consumer<ModeManager> executable) {
        return WorldSavedDataManager.execute(ModeManager.class, null, id, data->{executable.accept(data); return true;}, false);
    }

    private void installModeNotDirty(String name) {
        if (!installedModes.contains(name))
            installedModes.add(name);
        CompoundNBT modeNBT = new CompoundNBT();
        modeNBT.putBoolean("Disabled", false);
        modesNBT.put(name, modeNBT);
    }

    public void installMode(String name) {
        installModeNotDirty(name);
        markDirty();
    }

    public static void getAttributeModifiers(int eyeId, EquipmentSlotType slot, ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {
        ModeManager instance = getInstance(eyeId);
        if (instance != null)
            for (String mode : instance.getInstalledModes())
                ModeManager.getMode(mode).getAttributeModifiers(eyeId, mode.equals(instance.getSelectedMode()), slot, builder);
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
        nbt.put("ModesNBT", modesNBT);
        nbt.putString("Selected", selectedMode);
        ListNBT list = new ListNBT();
        for (String entry : installedModes)
            list.add(StringNBT.valueOf(entry));
        nbt.put("Installed", list);
        return nbt;
    }

    public void read(CompoundNBT nbt) {
        modesNBT = nbt.getCompound("ModesNBT");
        selectedMode = nbt.getString("Selected");
        if (selectedMode == null)
            selectedMode = "Default";
        ListNBT list = nbt.getList("Installed", 8);
        installedModes = new ArrayList<>();
        for (int i = 0; i < list.size(); ++i)
            installedModes.add(list.getString(i));
    }

    public void inventoryTick(World world, Entity player, boolean isSelected) {
        ActionResultType res = getMode(selectedMode).onEntityTick(getEyeId(), world, player, isSelected);
        if (res != ActionResultType.PASS) return;
        for (int i = installedModes.size() - 1; i >= 0; --i) {
            String name = installedModes.get(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onEntityTick(getEyeId(), world, player, isSelected);
            if (res != ActionResultType.PASS) return;
        }
        getMode("Default").onEntityTick(getEyeId(), world, player, isSelected);
    }

    public ActionResultType onItemUse(World world, PlayerEntity player, BlockRayTraceResult ray) {
        ActionResultType res = getMode(selectedMode).onItemUse(getEyeId(), world, player, ray);
        if (res != ActionResultType.PASS) return res;
        for (int i = installedModes.size() - 1; i >= 0; --i) {
            String name = installedModes.get(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onItemUse(getEyeId(), world, player, ray);
            if (res != ActionResultType.PASS) return res;
        }
        return getMode("Default").onItemUse(getEyeId(), world, player, ray);
    }

    public ActionResultType onItemRightClick(World world, PlayerEntity player) {
        ActionResultType res = getMode(selectedMode).onItemRightClick(getEyeId(), world, player);
        if (res != ActionResultType.PASS) return res;
        for (int i = installedModes.size() - 1; i >= 0; --i) {
            String name = installedModes.get(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onItemRightClick(getEyeId(), world, player);
            if (res != ActionResultType.PASS) return res;
        }
        return getMode("Default").onItemRightClick(getEyeId(), world, player);
    }

    public ActionResultType onAttack(PlayerEntity player, Entity entity) {
        ActionResultType res = getMode(selectedMode).onAttack(getEyeId(), player, entity);
        if (res != ActionResultType.PASS) return res;
        for (int i = installedModes.size() - 1; i >= 0; --i) {
            String name = installedModes.get(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onAttack(getEyeId(), player, entity);
            if (res != ActionResultType.PASS) return res;
        }
        return getMode("Default").onAttack(getEyeId(), player, entity);
    }

    public ActionResultType onActivateItem(PlayerEntity player) {
        ActionResultType res = getMode(selectedMode).onActivateItem(getEyeId(), player);
        if (res != ActionResultType.PASS) return res;
        for (int i = installedModes.size() - 1; i >= 0; --i) {
            String name = installedModes.get(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onActivateItem(getEyeId(), player);
            if (res != ActionResultType.PASS) return res;
        }
        return getMode("Default").onActivateItem(getEyeId(), player);
    }
}
