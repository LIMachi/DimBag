package com.limachi.dimensional_bags.common.bagDimensionOnly.bagUserBlock;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.lib.common.inventory.EntityInventoryProxy;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@StaticInit
public class UserBlock extends ContainerBlock {

    public static final String NAME = "user_block";

    public static final Supplier<UserBlock> INSTANCE = Registries.registerBlock(NAME, UserBlock::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public UserBlock() { super(Properties.of(Material.HEAVY_METAL).strength(1.5f, 3600000f).sound(SoundType.STONE)); }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslationTextComponent("tooltip.blocks.user_pillar").withStyle(TextFormatting.YELLOW));
        }  else
            tooltip.add(new TranslationTextComponent("tooltip.shift_for_info"));
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override //standard drop, without going through the loot table (not a good behavior, I know)
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        ArrayList<ItemStack> list = new ArrayList<>();
        list.add(new ItemStack(INSTANCE_ITEM.get()));
        return list;
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(IBlockReader worldIn) { return Registries.getBlockEntityType(UserBlockTileEntity.NAME).create(); }

    @Override
    public BlockRenderType getRenderShape(BlockState state) { return BlockRenderType.MODEL; }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        if (!DimBag.isServer(world)) return super.use(state, world, pos, player, hand, ray);
        TileEntity te = world.getBlockEntity(pos);
        if (!(te instanceof UserBlockTileEntity)) return super.use(state, world, pos, player, hand, ray);
        EntityInventoryProxy inv = ((UserBlockTileEntity)te).getInvProxy();
        if (inv != null)
            UserBlockContainer.open(player, SubRoomsManager.getbagId(world, pos, false));
        return ActionResultType.SUCCESS;
    }
}
