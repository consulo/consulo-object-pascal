package com.siberika.idea.pascal.editor.structure;

import com.siberika.idea.pascal.lang.psi.PasModule;
import consulo.annotation.access.RequiredReadAction;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.language.editor.structureView.PsiTreeElementBase;
import consulo.localize.LocalizeValue;
import consulo.object.pascal.icon.ObjectPascalIconGroup;
import consulo.ui.image.Image;
import consulo.util.lang.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * @author George Bakhtadze
 * @since 2015-05-06
 */
public class PasModuleStructureTreeElement extends PsiTreeElementBase<PasModule> {
    protected PasModuleStructureTreeElement(PasModule psiElement) {
        super(psiElement);
    }

    @NotNull
    @Override
    @RequiredReadAction
    public Collection<StructureViewTreeElement> getChildrenBase() {
        return PasStructureViewTreeElement.collectChildren(getElement());
    }

    @Override
    @RequiredReadAction
    public @Nullable Image getIcon() {
        if (getElement() != null) {
            return switch (getElement().getModuleType()) {
                case PACKAGE, LIBRARY -> ObjectPascalIconGroup.library();
                case PROGRAM -> ObjectPascalIconGroup.program();
                case UNIT -> ObjectPascalIconGroup.unit();
            };
        }
        return null;
    }

    @Override
    @RequiredReadAction
    public LocalizeValue getPresentableText() {
        String name = getElement() != null ? getElement().getName() : "-";
        if (StringUtil.isEmpty(name)) {
            name = getElement().getContainingFile().getName();
        }
        return LocalizeValue.of(name);
    }
}
