package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class Tunnel extends Block {
    public Tunnel() { super(Properties.create(Material.ROCK).hardnessAndResistance(-1f, 3600000f).sound(SoundType.STONE)); }

    @Override
    public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) { //'right-click' behavior (use/interact)
        if (!DimBag.isServer(world)) return super.onBlockActivated(state, world, pos, player, hand, ray);
        if (KeyMapController.getKey(player, KeyMapController.CROUCH_KEY))
            SubRoomsManager.execute(SubRoomsManager.getEyeId(world, pos, false), subRoomsManager -> subRoomsManager.tpIn(player));
        else
            SubRoomsManager.execute(SubRoomsManager.getEyeId(world, pos, false), subRoomsManager -> subRoomsManager.tpTunnel(player, pos));
        return ActionResultType.SUCCESS;
    }

    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) { //'left-click' behavior (punch/mine)
        if (!DimBag.isServer(worldIn)) {
            super.onBlockClicked(state, worldIn, pos, player);
            return;
        }
        if (KeyMapController.getKey(player, KeyMapController.CROUCH_KEY)) {//shift-left-click, remove the tunnel (replace it by a wall) and give back the tunnel placer (item)
            worldIn.setBlockState(pos, Registries.WALL_BLOCK.get().getDefaultState());
            SubRoomsManager data = SubRoomsManager.getInstance(SubRoomsManager.getEyeId(worldIn, pos, false));
            if (data == null) {
                super.onBlockClicked(state, worldIn, pos, player);
                return;
            }
            SubRoomsManager.tunnel((ServerWorld)worldIn, pos, player, false, true);
            player.addItemStackToInventory(new ItemStack(Registries.TUNNEL_ITEM.get()));
        }
    }
}
