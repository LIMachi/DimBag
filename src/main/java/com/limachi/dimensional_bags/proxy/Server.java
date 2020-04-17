package com.limachi.dimensional_bags.proxy;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class Server implements ICommonProxy {

    public void onClientSetup(FMLClientSetupEvent event) {}

    public void registerModels(ModelRegistryEvent event) {}
}
