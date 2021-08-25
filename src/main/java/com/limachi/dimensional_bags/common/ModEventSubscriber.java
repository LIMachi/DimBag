package com.limachi.dimensional_bags.common;

import com.google.common.collect.ImmutableSet;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.recipes.RecipeList;
import com.limachi.dimensional_bags.utils.ReflectionUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.lang.reflect.Field;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

//    @ObjectHolder(BagRiftDimension.REG_ID)
//    public static final ModDimension bagDimension = null;

    /*
    @SubscribeEvent
    public static void onDimensionRegistry(RegistryEvent.Register<ModDimension> event) {
        event.getRegistry().register(new BagRiftDimension.BagRiftModDimension().setRegistryName(BagRiftDimension.REG_ID));
    }
     */

    @SubscribeEvent
    public static void registerEntity(RegistryEvent.Register<EntityType<?>> event) {
        GlobalEntityTypeAttributes.put(Registries.getEntityType(BagEntity.NAME), BagEntity.getAttributeMap().build());
    }

    @SubscribeEvent
    public static void registerRecipeSerializer(RegistryEvent.Register<IRecipeSerializer<?>> event) {
//        UpgradeManager.registerRecipes(event.getRegistry());
        RecipeList.registerRecipes(event.getRegistry());
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
    }
}
