package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeData;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class Tunnel extends Block {
    public Tunnel() { super(Properties.create(Material.ROCK).hardnessAndResistance(-1f, 3600000f).sound(SoundType.STONE)); }

    @Override
    public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) { //'right-click' behavior (use/interact)
        if (!DimBag.isServer(world)) return super.onBlockActivated(state, world, pos, player, hand, ray);
        EyeData data = EyeData.getEyeData(world, pos, false);
        if (data == null) //invalid tunnel,could not find a valid eye
            return ActionResultType.FAIL;
        if (player.isCrouching())
            data.tpIn(player); //if crouching, go back to the main room
        else
            data.tpTunnel(player, pos); //try to go to the next room through the tunnel
        return ActionResultType.SUCCESS;
    }

    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) { //'left-click' behavior (punch/mine)
        if (!DimBag.isServer(worldIn)) {
            super.onBlockClicked(state, worldIn, pos, player);
            return;
        }
        EyeData data = EyeData.getEyeData(worldIn, pos, false);
        if (data == null) {
            super.onBlockClicked(state, worldIn, pos, player);
            return;
        }
        if (player.isCrouching()) {//shift-left-click, remove the tunnel (replace it by a wall) and give back the tunnel placer (item)
            worldIn.setBlockState(pos, Registries.WALL_BLOCK.get().getDefaultState());
            player.addItemStackToInventory(new ItemStack(Registries.TUNNEL_ITEM.get()));
        }
    }
}
