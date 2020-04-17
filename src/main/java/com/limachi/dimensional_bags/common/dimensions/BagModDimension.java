package com.limachi.dimensional_bags.common.dimensions;

import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.ModDimension;
import java.util.function.BiFunction;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

public class BagModDimension extends ModDimension {

    public static final String STR_ID = "bag_rift";
    public static final String REG_ID = MOD_ID + ":" + STR_ID;

    @Override
    public BiFunction<World, DimensionType, ? extends Dimension> getFactory() {
        return BagDimension::new;
    }
}
