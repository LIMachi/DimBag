package com.limachi.dimensional_bags.utils;

import net.minecraft.nbt.CollectionNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;

import com.limachi.dimensional_bags.ConfigManager.Config;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;

public class TextUtils {

    @Config(min = "16")
    private final static int PRETTY_NBT_MAX_WIDTH = 32;
    @Config(min = "0", max = "8")
    private final static int PRETTY_NBT_MARGIN = 1;

    private static IFormattableTextComponent lst(String v) {
        return new StringTextComponent(v).mergeStyle(TextFormatting.YELLOW);
    }

    private static IFormattableTextComponent obj(String v) {
        return new StringTextComponent(v).mergeStyle(TextFormatting.GOLD);
    }

    private static IFormattableTextComponent key(String v) {
        return new StringTextComponent(v).mergeStyle(TextFormatting.DARK_AQUA);
    }

    private static IFormattableTextComponent str(String v) {
        return new StringTextComponent(v).mergeStyle(TextFormatting.GREEN);
    }

    private static IFormattableTextComponent def(String v) {
        return new StringTextComponent(v).mergeStyle(TextFormatting.AQUA);
    }

    private static ArrayList<IFormattableTextComponent> prettyNBTInternal(INBT nbt, int depth) {
        ArrayList<IFormattableTextComponent> out = new ArrayList<>();
        int width = PRETTY_NBT_MAX_WIDTH - PRETTY_NBT_MARGIN * depth;
        String margin = "        ".substring(0, PRETTY_NBT_MARGIN);
        if (width <= 0) return out;
        if (nbt instanceof CollectionNBT) { //list style
            CollectionNBT<INBT> l = (CollectionNBT<INBT>)nbt;
            if (l.size() == 0)
                out.add(lst("[]"));
            else if (l.size() == 1) {
                ArrayList<IFormattableTextComponent> t = prettyNBTInternal(l.get(0), depth + 1);
                if (t.size() == 1)
                    out.add(lst("[ ").append(t.get(0)).append(lst(" ]")));
                else {
                    out.add(lst("[ ").append(t.get(0)));
                    for (int i = 1; i < t.size() - 1; ++i)
                        out.add(new StringTextComponent(margin).append(t.get(i)));
                    out.add(t.get(t.size() - 1).append(lst(" ]")));
                }
            } else {
                out.add(lst("["));
                for (int i = 0; i < l.size(); ++i) {
                    ArrayList<IFormattableTextComponent> t = prettyNBTInternal(l.get(i), depth + 1);
                    for (int j = 0; j < t.size() - 1; ++j)
                        out.add(new StringTextComponent(margin).append(t.get(j)));
                    if (i < l.size() - 1)
                        out.add(new StringTextComponent(margin).append(t.get(t.size() - 1)).append(lst(",")));
                    else
                        out.add(new StringTextComponent(margin).append(t.get(t.size() - 1)));
                }
                out.add(lst("]"));
            }
        } else if (nbt instanceof CompoundNBT) { //object style
            CompoundNBT c = (CompoundNBT)nbt;
            if (c.isEmpty())
                out.add(obj("{}"));
            else if (c.keySet().size() == 1) {
                String k = (String)c.keySet().toArray()[0];
                ArrayList<IFormattableTextComponent> t = prettyNBTInternal(c.get(k), depth + 1);
                if (t.size() == 1)
                    out.add(obj("{ ").append(key(k)).append(obj(" : ")).append(t.get(0)).append(obj(" }")));
                else {
                    out.add(obj("{ ").append(key(k)).append(obj(" : ")).append(t.get(0)));
                    for (int i = 1; i < t.size() - 1; ++i)
                        out.add(new StringTextComponent(margin).append(t.get(i)));
                    out.add(t.get(t.size() - 1).append(obj(" }")));
                }
            } else {
                out.add(obj("{"));
                String[] ks = c.keySet().toArray(new String[]{});
                for (int i = 0; i < ks.length; ++i) {
                    ArrayList<IFormattableTextComponent> t = prettyNBTInternal(c.get(ks[i]), depth + 1);
                    if (t.size() > 1) {
                        out.add(new StringTextComponent(margin).append(key(ks[i])).append(obj(" : ")).append(t.get(0)));
                        for (int j = 1; j < t.size() - 1; ++j)
                            out.add(new StringTextComponent(margin).append(t.get(j)));
                        if (i < c.size() - 1)
                            out.add(new StringTextComponent(margin).append(t.get(t.size() - 1)).append(obj(",")));
                        else
                            out.add(new StringTextComponent(margin).append(t.get(t.size() - 1)));
                    } else {
                        if (i < c.size() - 1)
                            out.add(new StringTextComponent(margin).append(key(ks[i])).append(obj(" : ")).append(t.get(0)).append(obj(",")));
                        else
                            out.add(new StringTextComponent(margin).append(key(ks[i])).append(obj(" : ")).append(t.get(0)));
                    }
                }
                out.add(obj("}"));
            }
        } else //default style
            out.add(nbt instanceof StringNBT ? str(nbt.toString()) : def(nbt.toString()));
        return out;
    }

    /**
     * formatting function to convert any INBT to a readable and colorful TextComponent
     * @param nbt
     * @return
     */

    public static IFormattableTextComponent prettyNBT(INBT nbt) {
        ArrayList<IFormattableTextComponent> list = prettyNBTInternal(nbt, 0);
        IFormattableTextComponent out = new StringTextComponent("");
        for (int i = 0; i < list.size() - 1; ++i)
            out.append(list.get(i)).appendString("\n");
        out.append(list.get(list.size() - 1));
        return out;
    }
}
