package com.limachi.dimensional_bags.common.items.upgrades.bag;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.common.items.upgrades.BaseUpgradeBag;
import com.limachi.dimensional_bags.utils.WorldUtils;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
@StaticInit
public class SweetFabricUpgrade extends BaseUpgradeBag<SweetFabricUpgrade> {

    public static final String NAME = "sweet_fabric_upgrade";

    @Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;

    @Config(cmt = "what items/blocks should have their placement prevented if the soft fabric upgrade is not installed (ResourceLocation regex style notation, 'minecraft:.+_bed' would be any vanilla bed, 'minecraft:respawn_anchor' is exactly the respawn anchor)")
    public static String[] BLACK_LIST_UNLESS_SOFT_FABRIC = {"minecraft:.+_bed", "minecraft:respawn_anchor"};

    static {
        UpgradeManager.registerUpgrade(NAME, SweetFabricUpgrade::new);
    }

    public SweetFabricUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }

    @Override
    public String upgradeName() { return NAME; }

    @SubscribeEvent
    public static void preventFallDamageInsideTheBag(LivingFallEvent event) {
        int eye = SubRoomsManager.getEyeId(event.getEntity().level, event.getEntity().blockPosition(), false);
        if (eye > 0 && getInstance(NAME).isActive(eye))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void preventBlockPlacementInsideBag(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getWorld().dimension().equals(WorldUtils.DimBagRiftKey)) return; //if this event is not in our world, just ignore it
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
