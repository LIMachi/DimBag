package com.limachi.dimensional_bags.client.render.screen;

import com.limachi.dimensional_bags.common.container.SettingsContainer;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.common.items.upgrades.BaseUpgrade;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class SettingsScreen extends SimpleContainerScreen<SettingsContainer> {
    public SettingsScreen(SettingsContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void init() {
        super.init();
        int y = 10;
        SettingsData data = SettingsData.getInstance(container.eyeId);
        UpgradeManager upgrades = UpgradeManager.getInstance(container.eyeId);
        if (data != null)
            for (SettingsData.SettingsReader reader : SettingsData.getReaders()) {
                if (reader.group.equals(BaseUpgrade.SETTING_GROUP) && upgrades.getUpgradeCount(reader.name) == 0) continue; //skip uninstalled upgrades
                ArrayList<Widget> widgets = reader.getWidgets(data, 10, y);
                for (Widget widget : widgets)
                    addButton(widget);
                if (widgets.size() > 0)
                    y = widgets.get(widgets.size() - 1).y + 25;
            }
//        addButton(new CheckboxButton(10, 10, 150, 20, new StringTextComponent("should_show_energy"), ModeManager.getMode(Default.ID).getSetting(container.eyeId, "should_show_energy")){
//            @Override
//            public void onPress() {
//                super.onPress();
//                ModeManager.getMode(Default.ID).setSetting(container.eyeId, "should_show_energy", isChecked());
//            }
//        });
    }
}
