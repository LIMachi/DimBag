package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.Tunnel;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.utils.WorldUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import com.limachi.dimensional_bags.StaticInit;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@StaticInit
public class TunnelPlacer extends Item implements IDimBagCommonItem {

    @Config(cmt = "does breaking a tunnel give back a tunnel placer that only work on the same wall (false: give back a normal tunnel placer that can be used on any wall)")
    public static boolean NERF_TUNNEL_PLACER = true;

    public static String NAME = "tunnel_placer";

    public static Supplier<TunnelPlacer> INSTANCE = Registries.registerItem(NAME, TunnelPlacer::new);

    public TunnelPlacer() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslationTextComponent("tooltip.items.tunnel_placer").withStyle(TextFormatting.YELLOW));
        }  else
            tooltip.add(new TranslationTextComponent("tooltip.shift_for_info"));
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) { //detect right clic on walls to transform them in tunnels, consuming 1 tunnel placer
        World world = context.getLevel();
        if (!(world instanceof ServerWorld) || context.getPlayer() == null) return ActionResultType.PASS;

        BlockPos pos = context.getClickedPos();
        if (!SubRoomsManager.isWall(world, pos)) return ActionResultType.PASS;

        ItemStack stack = context.getItemInHand();
        if (SubRoomsManager.tunnel((ServerWorld)world, pos, context.getPlayer(), true, false, stack.getTag(), true)) {
            if (!context.getPlayer().isCreative())
                stack.shrink(1);
            WorldUtils.replaceBlockAndGiveBack(pos, Registries.getBlock(Tunnel.NAME), context.getPlayer());
        }

        return ActionResultType.SUCCESS;
    }
}
