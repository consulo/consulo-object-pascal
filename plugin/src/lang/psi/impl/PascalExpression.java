package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasDereferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExpression;
import com.siberika.idea.pascal.lang.psi.PasIndexExpr;
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import com.siberika.idea.pascal.lang.psi.PasReferenceExpr;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 03/02/2015
 */
public class PascalExpression extends ASTWrapperPsiElement implements PascalPsiElement {
    public PascalExpression(ASTNode node) {
        super(node);
    }

    public boolean isRoot() {
        return getParent() instanceof PasExpression;
    }

    @Nullable
    public PascalOperation getOperation() {
        return PsiUtil.findImmChildOfAnyType(this, PascalOperation.class);
    }

    public static List<PasField.ValueType> getType(PascalExpression expr) throws PasInvalidScopeException {
        List<PasField.ValueType> res = new SmartList<PasField.ValueType>();

        PsiElement chld = expr.getFirstChild();
        if (chld instanceof PascalExpression) {
            res = PascalExpression.getType((PascalExpression) chld);
        }

        if (expr instanceof PasReferenceExpr) {
            final Collection<PasField> references = PasReferenceUtil.resolve(retrieveScope(res),
                    NamespaceRec.fromElement(((PasReferenceExpr) expr).getFullyQualifiedIdent()), PasField.TYPES_ALL, true, 0);
            if (!references.isEmpty()) {
                PasField field = references.iterator().next();
                PasReferenceUtil.retrieveFieldTypeScope(field);
                res.add(field.getValueType());
            }
        } else if (expr instanceof PasDereferenceExpr) {
            res.add(new PasField.ValueType(null, PasField.Kind.POINTER, null, null));
        } else if (expr instanceof PasIndexExpr) {
            res.add(new PasField.ValueType(null, PasField.Kind.ARRAY, null, null));
        }

        return res;
    }

    public static PasEntityScope retrieveScope(List<PasField.ValueType> types) {
        PasField.ValueType newScope = null;
        for (PasField.ValueType type : types) {
            if (type.field != null) {
                newScope = type;
            }
        }

        return newScope != null ? newScope.getTypeScope() : null;
    }

}
