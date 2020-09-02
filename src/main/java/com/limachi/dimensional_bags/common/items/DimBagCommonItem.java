package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class DimBagCommonItem extends Item {
    public static final String onTickCommands = "pending";
    public static final String onCreateCommands = "crafting";

    public DimBagCommonItem(Properties properties) { super(properties.group(DimBag.ITEM_GROUP)); }

    public static String[] getStringList(ItemStack stack, String key) {
        if (!stack.hasTag())
            return new String[]{""};
        ListNBT list = stack.getTag().getList(key, 8);
        String[] out = new String[list.size()];
        for (int i = 0; i < list.size(); ++i)
            out[i] = list.get(i).getString();
        return out;
    }

    public static ItemStack addToStringList(ItemStack stack, String key, String toAdd) {
        if (!stack.hasTag())
            stack.setTag(new CompoundNBT());
        ListNBT list = stack.getTag().getList(key, 8);
        list.add(StringNBT.valueOf(toAdd));
        stack.getTag().put(key, list);
        return stack;
    }

    public static void resetStringList(ItemStack stack, String key) {
        if (!stack.hasTag()) return;
        stack.getTag().put(key, new ListNBT());
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (DimBag.isServer(worldIn) && entityIn instanceof ServerPlayerEntity) {
            String[] pending = getStringList(stack, onTickCommands);
            for (String s : pending)
                if (s.startsWith("cmd."))
                    executePendingCommand(s.substring(4), stack, worldIn, entityIn, itemSlot, isSelected);
            resetStringList(stack, onTickCommands);
        }
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    public static void executePendingCommand(String s, ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (worldIn.isRemote()) return;
        if (s.startsWith("upgrade.") && stack.getItem() instanceof Bag) {
            EyeData data = EyeData.get(worldIn.getServer(), Bag.getId(stack));
//            Integer id = UpgradeManager.getIdByName(s.substring(8));
//            if (id != null)
//                UpgradeManager.applyUpgrade(id, data);
            UpgradeManager.getUpgrade(s.substring(8)).upgradeCrafted(data, stack, worldIn, entityIn);
        }
        if (s.startsWith("add.") && entityIn instanceof ServerPlayerEntity) {
            int qty = 0;
            if (s.substring(4).startsWith("random.")) { //format: random.9:27 random.<min>:<max>
                String[] ss = s.substring(11).split(":");
                double r1 = Integer.parseInt(ss[0]);
                double r2 = Integer.parseInt(ss[1]);
                qty = (int) (Math.random() * (r2 - r1 + 1D) + r1);
            } else
                qty = Integer.parseInt(s.substring(4));
            if (qty > 0) {
                ItemStack cpy = stack.copy();
                cpy.setCount(qty);
                resetStringList(cpy, onCreateCommands);
                ((ServerPlayerEntity) entityIn).addItemStackToInventory(cpy);
            }
        }
        if (s.startsWith("multiply.") && entityIn instanceof ServerPlayerEntity) {
            int qty = 0;
            if (s.substring(9).startsWith("random.")) { //format: random.9:27 random.<min>:<max>
                String[] ss = s.substring(16).split(":");
                double r1 = Integer.parseInt(ss[0]);
                double r2 = Integer.parseInt(ss[1]);
                qty = (int) (Math.random() * (r2 - r1 + 1D) + r1);
            } else
                qty = Integer.parseInt(s.substring(9));
            qty = (qty - 1) * stack.getCount();
            if (qty > 0) {
                ItemStack cpy = stack.copy();
                cpy.setCount(qty);
                resetStringList(cpy, onCreateCommands);
                ((ServerPlayerEntity) entityIn).addItemStackToInventory(cpy);
            }
        }
    }

    @Override
    public void onCreated(ItemStack stack, World worldIn, PlayerEntity playerIn) {
        if (DimBag.isServer(worldIn)) {
            String[] pending = getStringList(stack, onCreateCommands);
            for (String s : pending)
                if (s.startsWith("cmd."))
                    executePendingCommand(s.substring(4), stack, worldIn, playerIn, -1, false);
        }
        resetStringList(stack, onCreateCommands);
        super.onCreated(stack, worldIn, playerIn);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        String[] pending = getStringList(stack, onTickCommands);
        for (String s : pending)
            if (s.startsWith("msg.")) {
                if (s.substring(4).startsWith("translate."))
                    tooltip.add(new TranslationTextComponent(s.substring(14)));
                else
                    tooltip.add(new StringTextComponent(s.substring(4)));
            }
        super.addInformation(stack, world, tooltip, flagIn);
    }
}
