package com.limachi.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class TextUtils {
    private static final int PRETTY_TAG_MAX_WIDTH = 32;
    private static final int PRETTY_TAG_MARGIN = 1;

    private static MutableComponent lst(String v) {
        return new TextComponent(v).withStyle(ChatFormatting.YELLOW);
    }

    private static MutableComponent obj(String v) {
        return new TextComponent(v).withStyle(ChatFormatting.GOLD);
    }

    private static MutableComponent key(String v) {
        return new TextComponent(v).withStyle(ChatFormatting.DARK_AQUA);
    }

    private static MutableComponent str(String v) {
        return new TextComponent(v).withStyle(ChatFormatting.GREEN);
    }

    private static MutableComponent def(String v) {
        return new TextComponent(v).withStyle(ChatFormatting.AQUA);
    }

    private static ArrayList<MutableComponent> prettyTagInternal(Tag nbt, int depth, boolean tryUuid) {
        ArrayList<MutableComponent> out = new ArrayList<>();
        int width = PRETTY_TAG_MAX_WIDTH - PRETTY_TAG_MARGIN * depth;
        String margin = "        ".substring(0, PRETTY_TAG_MARGIN);
        if (width <= 0) return out;
        if (nbt instanceof CollectionTag) { //list style
            CollectionTag<Tag> l = (CollectionTag<Tag>)nbt;
            boolean mightBeArrayOfUUID = l.size() > 0 && l.stream().allMatch(n -> n.getType() == IntArrayTag.TYPE && ((IntArrayTag) n).getAsIntArray().length == 4);
            /*if (tryUuid && l.size() == 4 && nbt instanceof IntArrayTag) FIXME: investigate how UUID are stored nowadays
                out.add(str(UUIDCodec.uuidFromIntArray(((IntArrayTag)nbt).getAsIntArray()).toString()));
            else */if (l.size() == 0)
                out.add(lst("[]"));
            else if (l.size() == 1) {
                ArrayList<MutableComponent> t = prettyTagInternal(l.get(0), depth + 1, mightBeArrayOfUUID);
                if (t.size() == 1)
                    out.add(lst("[ ").append(t.get(0)).append(lst(" ]")));
                else {
                    out.add(lst("[ ").append(t.get(0)));
                    for (int i = 1; i < t.size() - 1; ++i)
                        out.add(new TextComponent(margin).append(t.get(i)));
                    out.add(t.get(t.size() - 1).append(lst(" ]")));
                }
            } else {
                out.add(lst("["));
                for (int i = 0; i < l.size(); ++i) {
                    ArrayList<MutableComponent> t = prettyTagInternal(l.get(i), depth + 1, mightBeArrayOfUUID);
                    for (int j = 0; j < t.size() - 1; ++j)
                        out.add(new TextComponent(margin).append(t.get(j)));
                    if (i < l.size() - 1)
                        out.add(new TextComponent(margin).append(t.get(t.size() - 1)).append(lst(",")));
                    else
                        out.add(new TextComponent(margin).append(t.get(t.size() - 1)));
                }
                out.add(lst("]"));
            }
        } else if (nbt instanceof CompoundTag) { //object style
            CompoundTag c = (CompoundTag)nbt;
            if (c.isEmpty())
                out.add(obj("{}"));
            else if (c.getAllKeys().size() == 1) {
                String k = (String)c.getAllKeys().toArray()[0];
                ArrayList<MutableComponent> t = prettyTagInternal(c.get(k), depth + 1, k.matches("[uU]{0,2}[iI][dD]|[Uu]nique[iI][dD]"));
                if (t.size() == 1)
                    out.add(obj("{ ").append(key(k)).append(obj(" : ")).append(t.get(0)).append(obj(" }")));
                else {
                    out.add(obj("{ ").append(key(k)).append(obj(" : ")).append(t.get(0)));
                    for (int i = 1; i < t.size() - 1; ++i)
                        out.add(new TextComponent(margin).append(t.get(i)));
                    out.add(t.get(t.size() - 1).append(obj(" }")));
                }
            } else {
                out.add(obj("{"));
                String[] ks = c.getAllKeys().toArray(new String[]{});
                for (int i = 0; i < ks.length; ++i) {
                    ArrayList<MutableComponent> t = prettyTagInternal(c.get(ks[i]), depth + 1, ks[i].matches("[uU]{0,2}[iI][dD]|[Uu]nique[iI][dD]"));
                    if (t.size() > 1) {
                        out.add(new TextComponent(margin).append(key(ks[i])).append(obj(" : ")).append(t.get(0)));
                        for (int j = 1; j < t.size() - 1; ++j)
                            out.add(new TextComponent(margin).append(t.get(j)));
                        if (i < c.size() - 1)
                            out.add(new TextComponent(margin).append(t.get(t.size() - 1)).append(obj(",")));
                        else
                            out.add(new TextComponent(margin).append(t.get(t.size() - 1)));
                    } else {
                        if (i < c.size() - 1)
                            out.add(new TextComponent(margin).append(key(ks[i])).append(obj(" : ")).append(t.get(0)).append(obj(",")));
                        else
                            out.add(new TextComponent(margin).append(key(ks[i])).append(obj(" : ")).append(t.get(0)));
                    }
                }
                out.add(obj("}"));
            }
        } else //default style
            out.add(nbt instanceof StringTag ? str(nbt.toString()) : def(nbt.toString()));
        return out;
    }

    public static void prettyTagTooltip(List<Component> tooltip, Tag nbt) {
        tooltip.addAll(prettyTagInternal(nbt, 0, false));
    }

    public static String prettyTagString(Tag nbt) {
        StringBuilder out = new StringBuilder();
        ArrayList<MutableComponent> list = prettyTagInternal(nbt, 0, false);
        for (MutableComponent c : list)
            out.append(c.getString()).append('\n');
        return out.deleteCharAt(out.length() - 1).toString();
    }

    public static TextComponent prettyTagText(Tag nbt) {
        TextComponent out = new TextComponent("");
        ArrayList<MutableComponent> list = prettyTagInternal(nbt, 0, false);
        for (int i = 0; i < list.size() - 1; ++i)
            out.append(list.get(i)).append("\n");
        return (TextComponent)out.append(list.get(list.size() - 1));
    }
}
