package com.limachi.dim_bag;

import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.StaticInitializer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.lang.ref.WeakReference;
import java.util.HashMap;

@StaticInitializer.Static
public class Constants {
    public static final String MOD_ID = "dim_bag";
    public static final String BAG_ID_TAG_KEY = "bag_id";
    public static final String BAG_CURIO_SLOT = "back";
    public static final ResourceKey<Level> BAG_DIM = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(MOD_ID, "bag"));
    public static final HashMap<Integer, GameProfile> FAKE_PLAYERS_ID = new HashMap<>();
    public static WeakReference<FakePlayer> getFakePlayer(int id, ServerLevel level) { return new WeakReference<>(FakePlayerFactory.get(level, FAKE_PLAYERS_ID.computeIfAbsent(id, i->new GameProfile(null, "[DimBag: " + i + "]")))); }
    public static final KeyMapController.GlobalKeyBinding ACTION_KEY = KeyMapController.registerMouseKeyBind("key.action", 3, "category.dim_bag");
}
