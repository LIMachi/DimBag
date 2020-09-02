package com.limachi.dimensional_bags.common;

import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.recipes.RecipeList;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;
import static com.limachi.dimensional_bags.common.Registries.BAG_ENTITY;

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
        GlobalEntityTypeAttributes.put(BAG_ENTITY.get(), BagEntity.getAttributeMap().create());
    }

    @SubscribeEvent
    public static void registerRecipeSerializer(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        UpgradeManager.registerRecipes(event.getRegistry());
        RecipeList.registerRecipes(event.getRegistry());
    }
}
