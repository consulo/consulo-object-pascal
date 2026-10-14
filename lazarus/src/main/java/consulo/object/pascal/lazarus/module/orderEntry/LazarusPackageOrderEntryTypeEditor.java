package consulo.object.pascal.lazarus.module.orderEntry;

import consulo.annotation.component.ExtensionImpl;
import consulo.ide.setting.module.CustomOrderEntryTypeEditor;
import consulo.module.content.layer.orderEntry.CustomOrderEntry;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.ex.ColoredTextContainer;
import consulo.ui.ex.SimpleTextAttributes;

import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ExtensionImpl
public class LazarusPackageOrderEntryTypeEditor implements CustomOrderEntryTypeEditor<LazarusPackageOrderEntryModel> {
    @Override
    public Consumer<ColoredTextContainer> getRenderer(CustomOrderEntry<LazarusPackageOrderEntryModel> orderEntry, LazarusPackageOrderEntryModel model) {
        return container -> {
            container.setIcon(PlatformIconGroup.nodesPplib());
            container.append(model.getPresentableName(), SimpleTextAttributes.SYNTHETIC_ATTRIBUTES);
        };
    }

    @Override
    public String getOrderTypeId() {
        return LazarusPackageOrderEntryType.ID;
    }
}
