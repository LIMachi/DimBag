package com.limachi.dimensional_bags.client.render.widget;

import com.limachi.dimensional_bags.client.render.screen.BaseScreen;

public class RootWidget extends BaseWidget {
    public RootWidget(BaseScreen<?> screen) {
        super(0, 0, screen.width, screen.height);
        attachToScreen(screen);
        setVisible(false);
        getWritableArea(true);
    }
}
