package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.items.TunnelPlacer;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.function.Supplier;

@StaticInit
public class Tunnel extends Block {

    public static final String NAME = "tunnel";

    public static final Supplier<Tunnel> INSTANCE = Registries.registerBlock(NAME, Tunnel::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public Tunnel() { super(Properties.create(Material.ROCK).hardnessAndResistance(-1f, 3600000f).sound(SoundType.STONE)); }

    @Override
    public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) { //'right-click' behavior (use/interact)
        if (!DimBag.isServer(world)) return super.onBlockActivated(state, world, pos, player, hand, ray);
        if (KeyMapController.KeyBindings.SNEAK_KEY.getState(player))
            SubRoomsManager.execute(SubRoomsManager.getEyeId(world, pos, false), subRoomsManager -> subRoomsManager.enterBag(player, false, false, false, false));
        else
            SubRoomsManager.execute(SubRoomsManager.getEyeId(world, pos, false), subRoomsManager -> subRoomsManager.tpTunnel(player, pos));
        return ActionResultType.SUCCESS;
    }

    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) { //'left-click' behavior (punch/mine)
        if (!DimBag.isServer(worldIn)) {
            super.onBlockClicked(state, worldIn, pos, player);
            return;
        }
        if (KeyMapController.KeyBindings.SNEAK_KEY.getState(player)) {//shift-left-click, remove the tunnel (replace it by a wall) and give back the tunnel placer (item)
            SubRoomsManager data = SubRoomsManager.getInstance(SubRoomsManager.getEyeId(worldIn, pos, false));
            if (data == null) {
                super.onBlockClicked(state, worldIn, pos, player);
                return;
            }
            CompoundNBT nbt = new CompoundNBT();
            if (SubRoomsManager.tunnel((ServerWorld)worldIn, pos, player, false, true, nbt)) {
                worldIn.setBlockState(pos, Registries.getBlock(Wall.NAME).getDefaultState());
//                worldIn.setBlockState(pos, Blocks.BROWN_WOOL.getDefaultState()); //TODO
                ItemStack out = new ItemStack(Registries.getItem(TunnelPlacer.NAME));
                if (TunnelPlacer.NERF_TUNNEL_PLACER) {
                    if (!out.hasTag())
                        out.setTag(new CompoundNBT());
                    out.getTag().merge(nbt);
                }
                player.addItemStackToInventory(out);
            }
        }
    }
}
