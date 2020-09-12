package com.limachi.dimensional_bags.client.render.widget;

import com.limachi.dimensional_bags.client.render.TextureCutout;

public class StaticImageWidget extends BaseWidget {
    public StaticImageWidget(int x, int y, int width, int height, TextureCutout texture) {
        super(x, y, width, height);
        this.background = texture;
        setListening(false);
    }
}
