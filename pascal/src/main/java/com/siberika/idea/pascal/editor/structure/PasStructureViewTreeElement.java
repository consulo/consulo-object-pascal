package com.siberika.idea.pascal.editor.structure;

import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.language.editor.structureView.PsiTreeElementBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author George Bakhtadze
 * @since 2013-09-16
 */
public class PasStructureViewTreeElement extends PsiTreeElementBase<PsiElement> implements StructureViewTreeElement {
    private final PasField field;

    protected PasStructureViewTreeElement(PsiElement psiElement, PasField field) {
        super(field != null ? field.getElement() : psiElement);
        this.field = field;
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
        return Collections.emptyList();
    }

    @NotNull
    public static Collection<StructureViewTreeElement> collectChildren(@Nullable PasEntityScope element) {
        if (null == element) {
            return Collections.emptyList();
        }
        Collection<PasField> fields = element.getAllFields();
        Collection<StructureViewTreeElement> res = new ArrayList<StructureViewTreeElement>(fields.size());
        for (PasField field : fields) {
            if (PasField.TYPES_STRUCTURE.contains(field.fieldType)) {
                if (field.fieldType == PasField.FieldType.TYPE) {
                    PascalStructType structType = PasStructStructureTreeElement.getStructElement(field);
                    if (structType != null) {
                        res.add(new PasStructStructureTreeElement(structType));
                    }
                    else {
                        res.add(new PasStructureViewTreeElement(field.getElement(), field));
                    }
                }
                else {
                    res.add(new PasStructureViewTreeElement(field.getElement(), field));
                }
            }
        }
        return res;
    }

    public PasField getField() {
        return field;
    }

    @Override
    @RequiredReadAction
    public Image getIcon() {
        if (field != null) {
            if (field.fieldType == PasField.FieldType.VARIABLE) {
                return PlatformIconGroup.nodesVariable();
            }
            else if (field.fieldType == PasField.FieldType.CONSTANT) {
                return PlatformIconGroup.nodesConstant();
            }
            else if (field.fieldType == PasField.FieldType.TYPE) {
                return PlatformIconGroup.nodesType();
            }
            else if (field.fieldType == PasField.FieldType.PROPERTY) {
                return PlatformIconGroup.nodesProperty();
            }
            else if (field.fieldType == PasField.FieldType.ROUTINE) {
                return PlatformIconGroup.nodesFunction();
            }
        }
        return super.getIcon();
    }

    @Override
    @RequiredReadAction
    public LocalizeValue getPresentableText() {
        return switch (getElement()) {
            case PascalNamedElement namedElem -> LocalizeValue.of(PsiUtil.getFieldName(namedElem));
            case PsiFile file -> LocalizeValue.of(file.getName());
            case null, default -> LocalizeValue.localizeTODO("-");
        };
    }
}
