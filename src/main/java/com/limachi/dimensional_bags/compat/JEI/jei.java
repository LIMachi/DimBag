package com.limachi.dimensional_bags.compat.JEI;

import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

@JeiPlugin
public class jei implements mezz.jei.api.IModPlugin {

    public static final ResourceLocation PLUGIN_ID = new ResourceLocation(MOD_ID);

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void registerItemSubtypes(final ISubtypeRegistration subtypeRegistry) { //for now, unusable as the informations of a bag item requires an access to the server to retrieve the data of the eye
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void registerRecipes(final IRecipeRegistration registry) {

    }
}
