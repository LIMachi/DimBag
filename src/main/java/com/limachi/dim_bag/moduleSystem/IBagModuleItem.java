package com.limachi.dim_bag.moduleSystem;

import com.limachi.dim_bag.Constants;
import com.limachi.dim_bag.rooms.Rooms;
import com.limachi.lim_lib.World;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;

public interface IBagModuleItem {
    default void installIn(Player player, InteractionHand hand, int id) {
            BlockPos pos = Rooms.getNewModulePlacement(id);
            ModuleManager mm = ModuleManager.getInstance(id);
            if (player.getItemInHand(hand).getItem() instanceof BlockItem bi && bi.getBlock() instanceof IBagModuleBlock bb && mm != null && pos != null && mm.installModuleAt(pos, bb.getModuleId())) {
//                World.replaceBlockAndGiveBack(World.getLevel(Constants.BAG_DIM), pos, bi.getBlock(), player);
                World.getLevel(Constants.BAG_DIM).setBlock(pos, bi.getBlock().defaultBlockState(), 3);
                if (!player.isCreative())
                    player.getItemInHand(hand).shrink(1);
            }
    }
}
