package com.limachi.dimensional_bags.common.bagDimensionOnly;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.blocks.IGetUseSneakWithItemEvent;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SubRoomsManager;
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
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import java.util.function.Supplier;

@StaticInit
public class TunnelBlock extends Block implements IGetUseSneakWithItemEvent {

    public static final String NAME = "tunnel";

    public static final Supplier<TunnelBlock> INSTANCE = Registries.registerBlock(NAME, TunnelBlock::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public TunnelBlock() { super(Properties.of(Material.HEAVY_METAL).strength(-1f, 3600000f).sound(SoundType.GLASS)); }

    @Override
    public BlockRenderType getRenderShape(BlockState state) { return BlockRenderType.MODEL; }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) { //'right-click' behavior (use/interact)
        if (!DimBag.isServer(world)) return super.use(state, world, pos, player, hand, ray);
        if (KeyMapController.KeyBindings.SNEAK_KEY.getState(player))
            SubRoomsManager.execute(SubRoomsManager.getbagId(world, pos, false), subRoomsManager -> subRoomsManager.enterBag(player, false, false, false, false, false));
        else
            SubRoomsManager.execute(SubRoomsManager.getbagId(world, pos, false), subRoomsManager -> subRoomsManager.tpTunnel(player, pos));
        return ActionResultType.SUCCESS;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        ItemStack out = new ItemStack(TunnelPlacerItem.INSTANCE.get());
        if (TunnelPlacerItem.NERF_TUNNEL_PLACER && world instanceof ServerWorld) {
            CompoundNBT nbt = new CompoundNBT();
            SubRoomsManager.tunnel((ServerWorld)world, pos, player, false, false, nbt, false);
            if (!out.hasTag())
                out.setTag(new CompoundNBT());
            out.getTag().merge(nbt);
        }
        return out;
    }

    @Override
    public void attack(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) { //'left-click' behavior (punch/mine)
        if (!DimBag.isServer(worldIn)) {
            super.attack(state, worldIn, pos, player);
            return;
        }
        if (KeyMapController.KeyBindings.SNEAK_KEY.getState(player)) {//shift-left-click, remove the tunnel (replace it by a wall) and give back the tunnel placer (item)
            SubRoomsManager data = SubRoomsManager.getInstance(SubRoomsManager.getbagId(worldIn, pos, false));
            if (data == null) {
                super.attack(state, worldIn, pos, player);
                return;
            }
            CompoundNBT nbt = new CompoundNBT();
            if (SubRoomsManager.tunnel((ServerWorld)worldIn, pos, player, false, true, nbt, true)) {
                worldIn.setBlock(pos, Registries.getBlock(WallBlock.NAME).defaultBlockState(), Constants.BlockFlags.DEFAULT_AND_RERENDER);
                ItemStack out = new ItemStack(Registries.getItem(TunnelPlacerItem.NAME));
                if (TunnelPlacerItem.NERF_TUNNEL_PLACER) {
                    if (!out.hasTag())
                        out.setTag(new CompoundNBT());
                    out.getTag().merge(nbt);
                }
                player.addItem(out);
            }
        }
    }
}
