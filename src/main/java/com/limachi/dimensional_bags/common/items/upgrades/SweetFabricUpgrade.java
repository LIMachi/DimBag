package com.limachi.dimensional_bags.common.items.upgrades;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.utils.WorldUtils;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
@StaticInit
public class SweetFabricUpgrade extends BaseUpgrade {

    public static final String NAME = "sweet_fabric_upgrade";

    @Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;

    @Config(cmt = "what items/blocks should have their placement prevented if the soft fabric upgrade is not installed (ResourceLocation regex style notation, 'minecraft:.+_bed' would be any vanilla bed)")
    public static String[] BLACK_LIST_UNLESS_SOFT_FABRIC = {"minecraft:.+_bed"};

    static {
        UpgradeManager.registerUpgrade(NAME, SweetFabricUpgrade::new);
    }

    public SweetFabricUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }

    @Override
    public String upgradeName() { return NAME; }

    @Override
    public int installUpgrade(UpgradeManager manager, int qty) { return qty; }

    @SubscribeEvent
    public static void onItemRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getWorld().getDimensionKey().equals(WorldUtils.DimBagRiftKey)) return; //if this event is not in our world, just ignore it
        ResourceLocation res = event.getItemStack().getItem().getRegistryName();
        if (res != null) { //detected potentially invalid item usage, must now check for validity in this room
            boolean found_match = false;
            String toTest = res.toString();
            for (String r : BLACK_LIST_UNLESS_SOFT_FABRIC)
                if (toTest.matches(r)) {
                    found_match = true;
                    break;
                }
            if (found_match) {
                int bag = SubRoomsManager.getEyeId(event.getWorld(), event.getPos(), false);
                if (bag > 0 && UpgradeManager.isUpgradeInstalled(bag, NAME).isPresent() && UpgradeManager.getUpgrade(NAME).isActive(bag))
                    return;
                event.setCanceled(true);
            }

        }
    }

}
