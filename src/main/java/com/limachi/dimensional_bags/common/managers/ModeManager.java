package com.limachi.dimensional_bags.common.managers;

import com.google.common.collect.ImmutableMultimap;
import com.limachi.dimensional_bags.CuriosIntegration;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
//import com.limachi.dimensional_bags.common.data.EyeDataMK2.ClientDataManager;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.WorldSavedDataManager;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.GhostBag;
import com.limachi.dimensional_bags.common.managers.modes.*;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModeManager extends WorldSavedDataManager.EyeWorldSavedData {
    public static final Mode[] MODES = {
            new Default(),
            new Tank(),
            new Settings(),
            new Capture(),
            new Debug(),
            new Manual()
    };

    //should be changed to offer to modes to change the behavior
    public static boolean changeModeRequest(PlayerEntity splayer, int eye, boolean up, boolean testOnly) { //should iterate over players and change all bags
        ModeManager dataS = getInstance(eye);
        if (dataS == null) return false;
        if (ModeManager.getMode(dataS.selectedMode).onScroll(splayer, eye, up, testOnly)) return true; //scroll was processed by a mode
        if (splayer instanceof ClientPlayerEntity) return KeyMapController.KeyBindings.BAG_KEY.getState(splayer);
//        ClientDataManager dataC = ClientDataManager.getInstance(eye);
        ArrayList<String> modes = new ArrayList<>(dataS.getInstalledModes());
        for (int i = 0; i < modes.size(); ++i) {
            if (!modes.get(i).equals(dataS.getSelectedMode())) continue;
            dataS.selectMode((i + (up ? 1 : modes.size() - 1)) % modes.size());
//            dataC.getModeManager().selectMode(dataS.getSelectedMode());
            break;
        }
        for (PlayerEntity player : DimBag.getPlayers()) {
            List<CuriosIntegration.ProxySlotModifier> res = CuriosIntegration.searchItem(player, Item.class, s->{
                if ((s.getItem() instanceof Bag || s.getItem() instanceof GhostBag) && Bag.getEyeId(s) == eye) {
//                    dataC.store(s);
                    return true;
                }
                return false;
            }, true);
            if (res.size() != 0)
                player.displayClientMessage(new TranslationTextComponent("notification.bag.changed_mode", new TranslationTextComponent("bag.mode." + dataS.getSelectedMode())), true);
        }
        return true;
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
    private CompoundNBT installedModesData;

    public ModeManager(int id) {
        this("mode_manager", id, true);
    }

    public ModeManager(String suffix, int id, boolean client) {
        super(suffix, id, client, false);
        selectedMode = Default.ID;
        installedModesData = new CompoundNBT();
        for (Mode mode : MODES)
            if (mode.IS_INSTALLED_BY_DEFAULT)
                installedModesData.put(mode.NAME, new CompoundNBT());
    }

    static public ModeManager getInstance(int id) { return WorldSavedDataManager.getInstance(ModeManager.class, id); }

    static public <T> T execute(int id, Function<ModeManager, T> executable, T onErrorReturn) { return WorldSavedDataManager.execute(ModeManager.class, id, executable, onErrorReturn); }

    static public boolean execute(int id, Consumer<ModeManager> executable) { return WorldSavedDataManager.execute(ModeManager.class, id, data->{executable.accept(data); return true;}, false); }

    public void installMode(String name) {
        if (!installedModesData.contains(name))
            installedModesData.put(name, new CompoundNBT());
        setDirty();
    }

    public static void getAttributeModifiers(int eyeId, EquipmentSlotType slot, ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {
        ModeManager instance = getInstance(eyeId);
        if (instance != null)
            for (String mode : instance.getInstalledModes())
                ModeManager.getMode(mode).getAttributeModifiers(eyeId, mode.equals(instance.getSelectedMode()), slot, builder);
    }

    public String getSelectedMode() { return selectedMode; }

    public Set<String> getInstalledModes() { return installedModesData.getAllKeys(); }

    public String getInstalledMode(int i) { return (String)installedModesData.getAllKeys().toArray()[i]; }

    public void selectMode(int i) {
        if (i < 0 || i >= installedModesData.getAllKeys().size()) return;
        selectedMode = getInstalledMode(i);
        setDirty();
    }

    public void selectMode(String mode) {
        if (installedModesData.contains(mode))
            selectedMode = mode;
        setDirty();
    }

    public CompoundNBT save(CompoundNBT nbt) {
        nbt.putString("Selected", selectedMode);
        nbt.put("Installed", installedModesData);
        return nbt;
    }

    public void load(CompoundNBT nbt) {
        selectedMode = nbt.getString("Selected");
        if (selectedMode == null)
            selectedMode = "Default";
        installedModesData = nbt.getCompound("Installed");
    }

    public void onAddInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        ActionResultType res = getMode(selectedMode).onAddInformation(getEyeId(), stack, world, tooltip, flagIn);
        if (res != ActionResultType.PASS) return;
        for (int i = getInstalledModes().size() - 1; i >= 0; --i) {
            String name = getInstalledMode(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onAddInformation(getEyeId(), stack, world, tooltip, flagIn);
            if (res != ActionResultType.PASS) return;
        }
        getMode("Default").onAddInformation(getEyeId(), stack, world, tooltip, flagIn);
    }

    public void inventoryTick(World world, Entity player, boolean isSelected) {
        ActionResultType res = getMode(selectedMode).onEntityTick(getEyeId(), world, player, isSelected);
        if (res != ActionResultType.PASS) return;
        for (int i = getInstalledModes().size() - 1; i >= 0; --i) {
            String name = getInstalledMode(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onEntityTick(getEyeId(), world, player, isSelected);
            if (res != ActionResultType.PASS) return;
        }
        getMode("Default").onEntityTick(getEyeId(), world, player, isSelected);
    }

    public ActionResultType onItemUse(World world, PlayerEntity player, BlockRayTraceResult ray) {
        ActionResultType res = getMode(selectedMode).onItemUse(getEyeId(), world, player, ray);
        if (res != ActionResultType.PASS) return res;
        for (int i = getInstalledModes().size() - 1; i >= 0; --i) {
            String name = getInstalledMode(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onItemUse(getEyeId(), world, player, ray);
            if (res != ActionResultType.PASS) return res;
        }
        return getMode("Default").onItemUse(getEyeId(), world, player, ray);
    }

    public ActionResultType onItemRightClick(World world, PlayerEntity player) {
        ActionResultType res = getMode(selectedMode).onItemRightClick(getEyeId(), world, player);
        if (res != ActionResultType.PASS) return res;
        for (int i = getInstalledModes().size() - 1; i >= 0; --i) {
            String name = getInstalledMode(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onItemRightClick(getEyeId(), world, player);
            if (res != ActionResultType.PASS) return res;
        }
        return getMode("Default").onItemRightClick(getEyeId(), world, player);
    }

    public ActionResultType onAttack(PlayerEntity player, Entity entity) {
        ActionResultType res = getMode(selectedMode).onAttack(getEyeId(), player, entity);
        if (res != ActionResultType.PASS) return res;
        for (int i = getInstalledModes().size() - 1; i >= 0; --i) {
            String name = getInstalledMode(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onAttack(getEyeId(), player, entity);
            if (res != ActionResultType.PASS) return res;
        }
        return getMode("Default").onAttack(getEyeId(), player, entity);
    }

    public ActionResultType onActivateItem(PlayerEntity player) {
        ActionResultType res = getMode(selectedMode).onActivateItem(getEyeId(), player);
        if (res != ActionResultType.PASS) return res;
        for (int i = getInstalledModes().size() - 1; i >= 0; --i) {
            String name = getInstalledMode(i);
            if (name.equals(selectedMode) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onActivateItem(getEyeId(), player);
            if (res != ActionResultType.PASS) return res;
        }
        return getMode("Default").onActivateItem(getEyeId(), player);
    }
}
