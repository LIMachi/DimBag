package com.limachi.dimensional_bags.proxy;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public interface ICommonProxy {
    void onClientSetup(FMLClientSetupEvent event);
    void registerModels(ModelRegistryEvent event);
}
