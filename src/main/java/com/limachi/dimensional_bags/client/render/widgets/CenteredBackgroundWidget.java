package com.limachi.dimensional_bags.client.render.widgets;

import com.limachi.dimensional_bags.client.render.TextureCutout;
import net.minecraft.util.ResourceLocation;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class CenteredBackgroundWidget extends ImageWidget {

    public static final TextureCutout VANILLA_BACKGROUND = new TextureCutout(new ResourceLocation(MOD_ID, "textures/screens/vanilla/full_background.png"), 0, 0, 256, 256);

    public CenteredBackgroundWidget() {
        super(0, 0, 0, 0, VANILLA_BACKGROUND);
        application = TextureCutout.TextureApplicationPattern.MIDDLE_EXPANSION;
        renderStandardBackground = false;
        enableClickBehavior = false;
    }

    @Override
    public void init() {
        x = getScreen().getGuiLeft();
        y = getScreen().getGuiTop();
        width = getScreen().getXSize();
        height = getScreen().getYSize();
        super.init();
    }
}
