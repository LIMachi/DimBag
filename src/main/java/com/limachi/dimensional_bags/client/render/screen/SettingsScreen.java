package com.limachi.dimensional_bags.client.render.screen;

import com.limachi.dimensional_bags.client.render.widgets.BaseWidget;
import com.limachi.dimensional_bags.client.render.widgets.ToggleWidget;
import com.limachi.dimensional_bags.common.container.SettingsContainer;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.common.items.upgrades.BaseUpgradeBag;
import com.limachi.dimensional_bags.common.managers.Mode;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import com.limachi.dimensional_bags.common.managers.modes.Default;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class SettingsScreen extends SimpleContainerScreen<SettingsContainer> {
    public SettingsScreen(SettingsContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        hideContainerTitle = true;
        hideTitle = true;
        int y = 10;
        SettingsData data = SettingsData.getInstance(menu.eyeId);
        UpgradeManager upgrades = UpgradeManager.getInstance(menu.eyeId);
        int i = 2;
        int x = 10;
        if (data != null)
            for (SettingsData.SettingsReader reader : SettingsData.getReaders()) {
                if (reader.group.equals(BaseUpgradeBag.SETTING_GROUP) && upgrades.getUpgradeCount(reader.name) == 0) continue; //skip uninstalled upgrades
                boolean default_page = reader.group.equals(Mode.SETTING_GROUP) && reader.name.equals(Default.ID);
                ArrayList<BaseWidget> widgets = reader.getWidgets(data, 10, y + 32, i);
                if (!widgets.isEmpty()) {
                    int finalI = i;
                    ToggleWidget.MutuallyExclusiveToggleWidget group_button = new ToggleWidget.MutuallyExclusiveToggleWidget(x, y, 16, 16, default_page, t->this.buttons.forEach(b -> {
                        if (b instanceof BaseWidget && ((BaseWidget)b).getGroup() > 1) {
                            BaseWidget widget = (BaseWidget)b;
                            if (widget.getGroup() == finalI)
                                widget.enable();
                            else
                                widget.disable();
                        }
                    }), 1){
                        @Override
                        public void renderButton(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
                            super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
                            getScreen().renderFloatingItem(reader.getIcon(), x(), y(), "");
                        }
                    }.appendTooltipProcessor(w->new TranslationTextComponent("settings." + reader.group + "." + reader.name + ".title"));
                    background.addChild(group_button);
                    for (BaseWidget widget : widgets) {
                        if (default_page)
                            widget.enable();
                        else
                            widget.disable();
                        background.addChild(widget);
                    }
                    ++i;
                    x += 16;
                }
            }
    }
}
