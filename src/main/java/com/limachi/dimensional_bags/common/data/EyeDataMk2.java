package com.limachi.dimensional_bags.common.data;

import com.limachi.dimensional_bags.common.data.EyeDataMK2.*;
import com.limachi.dimensional_bags.common.managers.ModeManager;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraftforge.energy.EnergyStorage;

import java.lang.ref.WeakReference;

//TODO: clearly define what data should be sync client-side (and what data dosen't need to be in this group at all, like the current mode of the bag)
public class EyeDataMk2 { //WIP: reworking the eye data to no longer be a humongous object fetched/updated too many times for nothing, this is a fake class
    public final int id = 0;
    WeakReference<IDManager> idManagerRef;
    OwnerData ownerData;
    HolderData holderData;
    SubRoomsManager subRoomsManager;
    UpgradeManager upgradeManager;
    ModeManager modeManager;
    InventoryData inventory;
    TankData tanks;
    EnergyData energyStorage;
//    ICapabilityProvider capabilityProvider; //TODO: move the capability provider to the items/tileentities/entities
}
