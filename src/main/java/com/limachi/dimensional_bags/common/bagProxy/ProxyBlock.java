package com.limachi.dimensional_bags.common.bagProxy;

import com.limachi.dimensional_bags.*;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagSlot.SlotContainer;
import com.limachi.dimensional_bags.lib.CuriosIntegration;
import com.limachi.dimensional_bags.lib.common.blocks.AbstractTileEntityBlock;
import com.limachi.dimensional_bags.lib.common.blocks.IGetUseSneakWithItemEvent;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.bag.BagItem;
import com.limachi.dimensional_bags.common.bag.GhostBagItem;
import com.limachi.dimensional_bags.common.upgrades.bag.ParadoxUpgrade;
import com.limachi.dimensional_bags.common.upgrades.BagUpgradeManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@StaticInit
public class ProxyBlock extends AbstractTileEntityBlock<ProxyTileEntity> implements IGetUseSneakWithItemEvent {

//    @ConfigManager.Config()
//    public static final String

    public static final Item OP_MODULE = Items.NETHER_STAR; //TODO: use another item instead

    public static final String NAME = "bag_proxy";

    public static final Supplier<ProxyBlock> INSTANCE = Registries.registerBlock(NAME, ProxyBlock::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public ProxyBlock() {
        super(NAME, Properties.of(Material.HEAVY_METAL).strength(1.5f, 3600000f).sound(SoundType.GLASS), ProxyTileEntity.class, ProxyTileEntity.NAME);
        bagOnlyPlacement = false;
    }
    
    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable IBlockReader worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public <B extends AbstractTileEntityBlock<ProxyTileEntity>> B getInstance() { return (B)INSTANCE.get(); }

    @Override
    public BlockItem getItemInstance() { return INSTANCE_ITEM.get(); }

    /**
     * behavior:
     * if op and hand contains bag -> overide the id of the proxy
     * if not op and hand contains bag -> consume bag and store id
     * if not op and hand contains op module -> consume module and set op (if id alrady set, give/drop bag with id)
     * other (not op, no valid item in hand) -> use the entity right click (simple click: open inventory, shift click: teleport inside)
     */
    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        TileEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof ProxyTileEntity) {
            ProxyTileEntity proxy = (ProxyTileEntity) te;
            ItemStack held = player.getItemInHand(handIn);
            boolean isBag = held.getItem() instanceof BagItem; //note: this might be a ghost bag
            if (proxy.isOP() && isBag) {
                proxy.setID(BagItem.getbagId(held));
                return ActionResultType.SUCCESS;
            } else {
                int eye = proxy.bagId();
                if (isBag) {
                    int id = BagItem.getbagId(held);
                    List<CuriosIntegration.ProxySlotModifier> res = CuriosIntegration.searchItem(player, BagItem.class, o->(!(o.getItem() instanceof GhostBagItem) && BagItem.getbagId(o) == id), true);
                    if (!res.isEmpty()) { //this might be empty if in hand we have a ghost bag but no bags (player in bag using the bag key)
                        proxy.setID(id);
                        res.get(0).set(ItemStack.EMPTY);
                    }
                    return ActionResultType.SUCCESS;
                } else if (held.getItem().equals(OP_MODULE)) {
                    proxy.setOP(true);
                    if (eye != 0)
                        BagItem.giveBag(eye, player);
                    held.shrink(1);
                    player.setItemInHand(handIn, held);
                    return ActionResultType.SUCCESS;
                }
            }
            int eye = proxy.bagId();
            if (eye > 0 && player instanceof ServerPlayerEntity) {
                if (KeyMapController.KeyBindings.SNEAK_KEY.getState(player))
                    SubRoomsManager.execute(eye, sm->sm.enterBag(player, !BagUpgradeManager.getUpgrade(ParadoxUpgrade.NAME).isActive(eye), true, true, false, true));
                else
                    SlotContainer.open(player, eye, null);
            }
        }
        return super.use(state, worldIn, pos, player, handIn, hit);
    }

    /**
     * behavior (while sneaking):
     * if not op and id set: give back bag and clear id
     * if op: give back OP item and clear id
     */
    @Override
    public void attack(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        TileEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof ProxyTileEntity && KeyMapController.KeyBindings.SNEAK_KEY.getState(player)) {
            ProxyTileEntity proxy = (ProxyTileEntity) te;
            if (proxy.isOP()) {
                ItemStack out = new ItemStack(OP_MODULE);
                if (!player.addItem(out))
                    player.drop(out, false);
                proxy.setOP(false);
                proxy.setID(0);
            } else {
                int eye = proxy.bagId();
                if (eye > 0) {
                    BagItem.giveBag(eye, player);
                    proxy.setID(0);
                }
            }
        }
        super.attack(state, worldIn, pos, player);
    }
}
