package com.limachi.dimensional_bags.common.bag.modes;

import com.google.common.collect.ImmutableMultimap;
import com.limachi.dimensional_bags.lib.CuriosIntegration;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
//import com.limachi.dimensional_bags.common.data.EyeDataMK2.ClientDataManager;
import com.limachi.dimensional_bags.lib.common.network.PacketHandler;
import com.limachi.dimensional_bags.lib.common.network.packets.PlayerPersistentDataAction;
import com.limachi.dimensional_bags.lib.common.network.packets.SetSlotPacket;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.WorldSavedDataManager;
import com.limachi.dimensional_bags.common.bag.BagItem;
import com.limachi.dimensional_bags.common.bag.GhostBagItem;
import com.limachi.dimensional_bags.lib.utils.NBTUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import net.minecraft.client.MainWindow;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModeManager extends WorldSavedDataManager.EyeWorldSavedData {
    public static final AbstractMode[] MODES = {
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
        if (ModeManager.getMode(/*dataS.selectedMode*/dataS.getSelectedMode(splayer)).onScroll(splayer, eye, up, testOnly)) return true; //scroll was processed by a mode
        if (splayer instanceof ClientPlayerEntity) return KeyMapController.KeyBindings.BAG_KEY.getState(splayer);
//        ClientDataManager dataC = ClientDataManager.getInstance(eye);
        ArrayList<String> modes = new ArrayList<>(dataS.getInstalledModes());
        for (int i = 0; i < modes.size(); ++i) {
            if (!modes.get(i).equals(dataS.getSelectedMode(splayer))) continue;
            dataS.selectMode(splayer, (i + (up ? 1 : modes.size() - 1)) % modes.size());
//            dataC.getModeManager().selectMode(dataS.getSelectedMode());
            break;
        }
        for (PlayerEntity player : DimBag.getPlayers()) {
            List<CuriosIntegration.ProxySlotModifier> res = CuriosIntegration.searchItem(player, Item.class, s->{
                if ((s.getItem() instanceof BagItem || s.getItem() instanceof GhostBagItem) && BagItem.getbagId(s) == eye) {
//                    dataC.store(s);
                    return true;
                }
                return false;
            }, true);
            if (res.size() != 0)
                player.displayClientMessage(new TranslationTextComponent("notification.bag.changed_mode", new TranslationTextComponent("bag.mode." + dataS.getSelectedMode(splayer))), true);
        }
        return true;
    }

    public static int getModeIndex(String name) {
        for (int i = 0; i < MODES.length; ++i)
            if (MODES[i].NAME.equals(name))
                return i;
         return -1;
    }

    public static AbstractMode getMode(String name) {
        int i;
        if ((i = getModeIndex(name)) == -1)
            return MODES[0];
        return MODES[i];
    }

//    private String selectedMode;
    private CompoundNBT installedModesData;

    public ModeManager(int id) {
        this("mode_manager", id, true);
    }

    public ModeManager(String suffix, int id, boolean client) {
        super(suffix, id, client, false);
//        selectedMode = Default.ID;
        installedModesData = new CompoundNBT();
        for (AbstractMode mode : MODES)
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
/*
    public static void getAttributeModifiers(int bagId, EquipmentSlotType slot, ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {
        ModeManager instance = getInstance(bagId);
        if (instance != null)
            for (String mode : instance.getInstalledModes())
                ModeManager.getMode(mode).getAttributeModifiers(bagId, mode.equals(instance.getSelectedMode()), slot, builder);
    }
*/
//    public String getSelectedMode() { return selectedMode; }

    public String getSelectedMode(Entity entity) {
        if (entity == null) return "Default";
        String t = entity.getPersistentData().getCompound("PlayerPersisted").getString("DimBagMode"); //PlayerPersisted is a special compound in player that is kept when the player respawn (copied from old player entity to the new respawned one), while getPersistentData only return a compound that is stored on the disk (FIXME: mode does not seem to be sync client side, resulting in the bag not showing in what mode it is)
        return installedModesData.contains(t) ? t : "Default";
    }

    public Set<String> getInstalledModes() { return installedModesData.getAllKeys(); }

    public String getInstalledMode(int i) { return (String)installedModesData.getAllKeys().toArray()[i]; }

    public void selectMode(Entity entity, int i) {
        if (i < 0 || i >= installedModesData.getAllKeys().size()) return;
//        selectedMode = getInstalledMode(i);
        if (!entity.getPersistentData().contains("PlayerPersisted"))
            entity.getPersistentData().put("PlayerPersisted", new CompoundNBT());
        entity.getPersistentData().getCompound("PlayerPersisted").putString("DimBagMode", getInstalledMode(i));
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity)entity;
            PacketHandler.toClient(player, new PlayerPersistentDataAction(PlayerPersistentDataAction.Actions.MERGE, NBTUtils.toCompoundNBT("PlayerPersisted", NBTUtils.toCompoundNBT("DimBagMode", getInstalledMode(i)))));
        }
//        setDirty();
    }

    public void selectMode(Entity entity, String mode) {
        if (installedModesData.contains(mode))
//            selectedMode = mode;
            if (!entity.getPersistentData().contains("PlayerPersisted"))
                entity.getPersistentData().put("PlayerPersisted", new CompoundNBT());
            entity.getPersistentData().getCompound("PlayerPersisted").putString("DimBagMode", mode);
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity)entity;
            PacketHandler.toClient(player, new PlayerPersistentDataAction(PlayerPersistentDataAction.Actions.MERGE, NBTUtils.toCompoundNBT("PlayerPersisted", NBTUtils.toCompoundNBT("DimBagMode", mode))));
        }
//        setDirty();
    }

    public CompoundNBT save(CompoundNBT nbt) {
//        nbt.putString("Selected", selectedMode);
        nbt.put("Installed", installedModesData);
        return nbt;
    }

    public void load(CompoundNBT nbt) {
//        selectedMode = nbt.getString("Selected");
//        if (selectedMode == null)
//            selectedMode = "Default";
        installedModesData = nbt.getCompound("Installed");
    }

    /*
    public void onAddInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        ActionResultType res = getMode(getSelectedMode(player)).onAddInformation(getbagId(), stack, world, tooltip, flagIn);
        if (res != ActionResultType.PASS) return;
        for (int i = getInstalledModes().size() - 1; i >= 0; --i) {
            String name = getInstalledMode(i);
            if (name.equals(getSelectedMode(player)) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onAddInformation(getbagId(), stack, world, tooltip, flagIn);
            if (res != ActionResultType.PASS) return;
        }
        getMode("Default").onAddInformation(getbagId(), stack, world, tooltip, flagIn);
    }*/

    public void inventoryTick(World world, Entity player) {
        String mode = player instanceof PlayerEntity ? getSelectedMode((PlayerEntity) player) : "Default";
        ActionResultType res = getMode(mode).onEntityTick(getbagId(), world, player);
        if (res != ActionResultType.PASS) return;
        for (int i = getInstalledModes().size() - 1; i >= 0; --i) {
            String name = getInstalledMode(i);
            if (name.equals("Default")) continue;
            res = getMode(name).onEntityTick(getbagId(), world, player);
            if (res != ActionResultType.PASS) return;
        }
        getMode("Default").onEntityTick(getbagId(), world, player);
    }

    public ActionResultType onItemUse(World world, PlayerEntity player, BlockRayTraceResult ray) {
        ActionResultType res = getMode(getSelectedMode(player)).onItemUse(getbagId(), world, player, ray);
        if (res != ActionResultType.PASS) return res;
        for (int i = getInstalledModes().size() - 1; i >= 0; --i) {
            String name = getInstalledMode(i);
            if (name.equals(getSelectedMode(player)) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onItemUse(getbagId(), world, player, ray);
            if (res != ActionResultType.PASS) return res;
        }
        return getMode("Default").onItemUse(getbagId(), world, player, ray);
    }

    public ActionResultType onItemRightClick(World world, PlayerEntity player) {
        ActionResultType res = getMode(getSelectedMode(player)).onItemRightClick(getbagId(), world, player);
        if (res != ActionResultType.PASS) return res;
        for (int i = getInstalledModes().size() - 1; i >= 0; --i) {
            String name = getInstalledMode(i);
            if (name.equals(getSelectedMode(player)) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onItemRightClick(getbagId(), world, player);
            if (res != ActionResultType.PASS) return res;
        }
        return getMode("Default").onItemRightClick(getbagId(), world, player);
    }

    public ActionResultType onAttack(PlayerEntity player, Entity entity) {
        ActionResultType res = getMode(getSelectedMode(player)).onAttack(getbagId(), player, entity);
        if (res != ActionResultType.PASS) return res;
        for (int i = getInstalledModes().size() - 1; i >= 0; --i) {
            String name = getInstalledMode(i);
            if (name.equals(getSelectedMode(player)) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onAttack(getbagId(), player, entity);
            if (res != ActionResultType.PASS) return res;
        }
        return getMode("Default").onAttack(getbagId(), player, entity);
    }

    public ActionResultType onActivateItem(PlayerEntity player) {
        ActionResultType res = getMode(getSelectedMode(player)).onActivateItem(getbagId(), player);
        if (res != ActionResultType.PASS) return res;
        for (int i = getInstalledModes().size() - 1; i >= 0; --i) {
            String name = getInstalledMode(i);
            if (name.equals(getSelectedMode(player)) || name.equals("Default") || !getMode(name).CAN_BACKGROUND) continue;
            res = getMode(name).onActivateItem(getbagId(), player);
            if (res != ActionResultType.PASS) return res;
        }
        return getMode("Default").onActivateItem(getbagId(), player);
    }

    public void onRenderHud(PlayerEntity player, MainWindow window, MatrixStack matrixStack, float partialTick) {
        for (int i = getInstalledModes().size() - 1; i >= 0; --i) {
            String name = getInstalledMode(i);
            getMode(name).onRenderHud(getbagId(), name.equals(getSelectedMode(player)), player, window, matrixStack, partialTick);
        }
    }
}
