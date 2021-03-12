package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.InventoryData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.inventory.PillarInventory;
import com.limachi.dimensional_bags.common.tileentities.PillarTileEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@StaticInit
public class Pillar extends ContainerBlock {

    public static final String NAME = "pillar";

    static {
        Registries.registerBlock(NAME, Pillar::new);
        Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);
    }

    public Pillar() {
        super(Properties.create(Material.PISTON).hardnessAndResistance(1.5f, 3600000f).sound(SoundType.STONE));
    }

    @Nullable
    @Override //this block can only be placed in a subroom
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        int eyeId = SubRoomsManager.getEyeId(context.getWorld(), context.getPos(), false);
        if (eyeId <= 0) return null;
        return super.getStateForPlacement(context);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }

    @Override //FIXME: crash on creative if a pillar is placed after being copied via Ctrl+MiddleClick (invalid nbt deserialization)
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof PillarTileEntity) {
            PillarTileEntity pillar = (PillarTileEntity) te;
            pillar.eyeId = SubRoomsManager.getEyeId(worldIn, pos, false);
            CompoundNBT tags = stack.getTag();
            PillarInventory inv = new PillarInventory();
            if (tags != null)
                inv.deserializeNBT(tags);
            pillar.invId = inv.getId();
            InventoryData.execute(pillar.eyeId, d->d.addPillar(inv));
        }
    }

    @Override //FIXME bug: if the pillar does not drop (creative break) the pillar is not removed from the bag
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity te = builder.get(LootParameters.BLOCK_ENTITY);
        ItemStack out = new ItemStack(Registries.getItem(Pillar.NAME));
        if (te instanceof PillarTileEntity) {
            PillarTileEntity pillar = (PillarTileEntity) te;
            PillarInventory inv = (PillarInventory)InventoryData.execute(pillar.eyeId, d->d.getPillarInventory(pillar.invId), null);
            if (out.getTag() == null)
                out.setTag(new CompoundNBT());
            out.getTag().merge(inv.serializeNBT());
            InventoryData.execute(pillar.eyeId, d->d.removePillar(pillar.invId));
        }
        ArrayList<ItemStack> list = new ArrayList<>();
        list.add(out);
        return list;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) { return Registries.getTileEntityType(PillarTileEntity.NAME).create(); }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        if (!DimBag.isServer(world)) return ActionResultType.SUCCESS;
        int eyeId = SubRoomsManager.getEyeId(world, pos, false);
        if (eyeId <= 0) return ActionResultType.SUCCESS;
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof PillarTileEntity)) return super.onBlockActivated(state, world, pos, player, hand, ray);
        ((PillarTileEntity) te).getInventory().open((ServerPlayerEntity) player);
        return ActionResultType.SUCCESS;
    }
}
