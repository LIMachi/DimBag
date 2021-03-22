package com.limachi.dimensional_bags.client.rootWidgets;

import com.limachi.dimensional_bags.client.widgets.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.UUID;

public class Settings implements RootProvider {

    public static final UUID id = UUID.fromString("219399f4-de9e-498a-aa9a-c3bdc21a17d7");

    @OnlyIn(Dist.CLIENT)
    private final Root root = new Root(0, 0, 128, 128, true);
    @OnlyIn(Dist.CLIENT)
    private final TextField textField = new TextField(root, 2, 2, 100, 20, "testField", (s1, s2)->true, s->{}); //the good test here would be to reflect the changes of the string and send them to the server
    @OnlyIn(Dist.CLIENT)
    private final Button button = new Button(root, 108, 2, 20, 20, in->{return false;}); //same here, clicking should do something

    static {
        Root.registerRoot(new Settings());
    }

    @Override
    public UUID getId() { return id; }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Root getRoot() { return root; }
}
