package com.siberika.idea.pascal.editor.structure;

import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.language.editor.structureView.PsiTreeElementBase;
import consulo.localize.LocalizeValue;
import consulo.object.pascal.icon.ObjectPascalIconGroup;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author George Bakhtadze
 * @since 2015-05-06
 */
public class PasStructStructureTreeElement extends PsiTreeElementBase<PascalStructType> {
    protected PasStructStructureTreeElement(PascalStructType psiElement) {
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
    public Image getIcon() {
        return switch (getElement()) {
            case PasInterfaceTypeDecl _ -> PlatformIconGroup.nodesInterface();
            case PasClassTypeDecl _ -> PlatformIconGroup.nodesClass();
            case PasObjectDecl _ -> PlatformIconGroup.nodesAnonymousclass();
            case PasRecordDecl _ -> PlatformIconGroup.nodesRecord();
            case PasClassHelperDecl _, PasRecordHelperDecl _ -> ObjectPascalIconGroup.helper();
            case null, default -> null;
        };
    }

    @Override
    @RequiredReadAction
    public LocalizeValue getPresentableText() {
        return getElement() != null ? LocalizeValue.of(getElement().getName()) : LocalizeValue.localizeTODO("-");
    }

    public static PascalStructType getStructElement(PasField field) {
        PasTypeDecl typeDecl = PsiUtil.getTypeDeclaration(field.getElement());
        if (typeDecl != null) {
            if (typeDecl.getInterfaceTypeDecl() != null) {
                return typeDecl.getInterfaceTypeDecl();
            }
            if (typeDecl.getClassTypeDecl() != null) {
                return typeDecl.getClassTypeDecl();
            }
            if (typeDecl.getObjectDecl() != null) {
                return typeDecl.getObjectDecl();
            }
            if (typeDecl.getRecordDecl() != null) {
                return typeDecl.getRecordDecl();
            }
            if (typeDecl.getClassHelperDecl() != null) {
                return typeDecl.getClassHelperDecl();
            }
            if (typeDecl.getRecordHelperDecl() != null) {
                return typeDecl.getRecordHelperDecl();
            }
        }
        return null;
    }
}
