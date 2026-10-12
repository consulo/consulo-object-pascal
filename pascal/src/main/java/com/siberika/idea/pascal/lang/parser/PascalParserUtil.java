package com.siberika.idea.pascal.lang.parser;

import com.siberika.idea.pascal.PascalRTException;
import com.siberika.idea.pascal.lang.compiled.CompiledFileImpl;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalIdentDeclImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.stub.PasIdentStub;
import com.siberika.idea.pascal.sdk.BuiltinsParser;
import com.siberika.idea.pascal.util.PsiUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.impl.parser.GeneratedParserUtilBase;
import consulo.language.parser.PsiBuilder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.navigation.ItemPresentation;
import consulo.object.pascal.icon.ObjectPascalIconGroup;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * @author George Bakhtadze
 * @since 2012-12-09
 */
@SuppressWarnings("unchecked")
public class PascalParserUtil extends GeneratedParserUtilBase {
    private static final Logger LOG = Logger.getInstance(PascalParserUtil.class);

    public static final String UNIT_NAME_SYSTEM = "system";
    public static final Collection<String> EXPLICIT_UNITS = Arrays.asList(UNIT_NAME_SYSTEM, BuiltinsParser.UNIT_NAME);
    public static final int MAX_STRUCT_TYPE_RESOLVE_RECURSION = 1000;

    public static boolean parsePascal(PsiBuilder builder_, int level, Parser parser) {
        PsiFile file = builder_.getUserData(PsiFile.KEY);
        if ((file != null) && (file.getVirtualFile() != null)) {
            try {
                String name = file.getVirtualFile().getName();
                if (!name.startsWith("test") && !name.startsWith("$")) {
//                    throw new PascalRTException("===*** Parse: " + name);
                }
            }
            catch (PascalRTException e) {
//                e.printStackTrace();
//                System.out.println(e.getMessage());
            }
        }
        //builder_.setDebugMode(true);
        ErrorState state = ErrorState.get(builder_);
        boolean res = parseAsTree(state, builder_, level, DUMMY_BLOCK, true, parser, TRUE_CONDITION);
        return res;
    }

    private static boolean isSameAffectingScope(PsiElement innerSection, PsiElement outerSection) {
        for (int i = 0; i < 4; i++) {
            if (innerSection == outerSection) {
                return true;
            }
            if ((null == innerSection) || PsiUtil.isInstanceOfAny(
                innerSection,
                PasClassTypeDecl.class,
                PasClassHelperDecl.class,
                PasInterfaceTypeDecl.class,
                PasObjectDecl.class,
                PasRecordDecl.class,
                PasRecordHelperDecl.class,
                PasClosureExpr.class,
                PascalRoutine.class
            )) {
                return false;
            }
            innerSection = PsiUtil.getNearestAffectingDeclarationsRoot(innerSection);
        }
        return false;
    }

    /**
     * add used unit interface declarations to result
     *
     * @param result  list of declarations to add unit declarations to
     * @param current element which should be affected by a unit declaration in order to be added to result
     */
    @RequiredReadAction
    @SuppressWarnings("ConstantConditions")
    private static void addUsedUnitDeclarations(Collection<PascalNamedElement> result, PsiElement current, String name) {
        PsiFile file = current.getContainingFile();
        Module module = current.getModule();
        List<VirtualFile> unitFiles = PasReferenceUtil.findUnitFiles(current.getProject(), module);
        for (PascalQualifiedIdent usedUnitName : PsiUtil.getUsedUnits(PsiUtil.getModuleInterfaceUsesClause(file))) {
            addUnitDeclarations(result, current.getProject(), unitFiles, usedUnitName.getName(), name);
        }
        for (PascalQualifiedIdent usedUnitName : PsiUtil.getUsedUnits(PsiUtil.getModuleImplementationUsesClause(file))) {
            addUnitDeclarations(result, current.getProject(), unitFiles, usedUnitName.getName(), name);
        }
        for (String unitName : EXPLICIT_UNITS) {
            addUnitDeclarations(result, current.getProject(), unitFiles, unitName, name);
        }
    }

    @RequiredReadAction
    private static void addUnitDeclarations(
        Collection<PascalNamedElement> result,
        Project project,
        List<VirtualFile> unitFiles,
        String unitName,
        String name
    ) {
        PascalNamedElement usedUnit = PasReferenceUtil.findUnit(project, unitFiles, unitName);
        if (usedUnit != null) {
            addDeclarations(result, PsiUtil.getModuleInterfaceSection(usedUnit), name);
        }
    }

    /**
     * Add all declarations of entities with matching names from the specified section to result
     *
     * @param result  list of declarations to add declarations to
     * @param section section containing declarations
     * @param name    name which a declaration should match
     */
    @RequiredReadAction
    private static void addDeclarations(Collection<PascalNamedElement> result, PsiElement section, String name) {
        if (section != null) {
            result.addAll(retrieveEntitiesFromSection(
                section,
                name,
                getEndOffset(section),
                PasNamedIdent.class,
                PasNamedIdentDecl.class,
                PasGenericTypeIdent.class,
                PasNamespaceIdent.class
            ));
        }
    }

    @RequiredReadAction
    private static int getEndOffset(PsiElement section) {
        return section != null ? section.getTextRange().getEndOffset() : -1;
    }

    /**
     * Returns type scope from type declaration in type section
     *
     * @param typeIdent      name of declaring type
     * @param recursionCount to prevent infinite recursion
     * @return scope if type is structured
     */
    @RequiredReadAction
    public static @Nullable PasEntityScope getStructTypeByIdent(@NotNull PascalNamedElement typeIdent, int recursionCount) {
        if (recursionCount > MAX_STRUCT_TYPE_RESOLVE_RECURSION) {
            return PsiUtil.getElementPasModule(typeIdent);
        }
        if (PsiUtil.isTypeDeclPointingToSelf(typeIdent)) {
            return PsiUtil.getElementPasModule(typeIdent);
        }
        if (typeIdent.getParent() instanceof PasGenericTypeIdent) {  // resolved from stub
            typeIdent = (PascalNamedElement) typeIdent.getParent();
        }
        PasTypeDecl typeDecl = PsiTreeUtil.getNextSiblingOfType(typeIdent, PasTypeDecl.class);
        if (typeDecl != null) {
            PasEntityScope strucTypeDecl = PsiTreeUtil.findChildOfType(typeDecl, PasEntityScope.class, true);
            if (strucTypeDecl != null) {   // structured type
                return strucTypeDecl;
            }
            else {                       // regular type
                PasFullyQualifiedIdent typeId = PsiTreeUtil.findChildOfType(typeDecl, PasFullyQualifiedIdent.class, true);
                return getStructTypeByTypeIdent(typeId, recursionCount);
            }
        }
        return null;
    }

    @RequiredReadAction
    private static @Nullable PasEntityScope getStructTypeByTypeIdent(@Nullable PascalQualifiedIdent typeId, int recursionCount) {
        if (typeId != null) {
            PsiElement section = PsiUtil.getNearestAffectingDeclarationsRoot(typeId);
            Collection<PascalNamedElement> entities =
                retrieveEntitiesFromSection(section, typeId.getName(), getEndOffset(section), PasGenericTypeIdent.class);
            addUsedUnitDeclarations(entities, typeId, typeId.getName());
            for (PascalNamedElement element : entities) {
                return getStructTypeByIdent(element, recursionCount + 1);
            }
        }
        return null;
    }

    @NotNull
    @RequiredReadAction
    private static <T extends PascalNamedElement> Collection<PascalNamedElement> retrieveEntitiesFromSection(
        PsiElement section,
        String key,
        int maxOffset,
        Class<? extends T>... classes
    ) {
        final Set<PascalNamedElement> result = new LinkedHashSet<>();
        if (section != null) {
            for (PascalNamedElement namedElement : PsiTreeUtil.findChildrenOfAnyType(section, classes)) {
                if (null == key || key.equalsIgnoreCase(namedElement.getName())) {
                    if (namedElement.getTextRange().getStartOffset() < maxOffset
                        && isSameAffectingScope(PsiUtil.getNearestAffectingDeclarationsRoot(namedElement), section)) {
                        result.remove(namedElement);
                        result.add(namedElement);
                    }
                }
            }
            result.addAll(retrieveEntitiesFromSection(PsiUtil.getNearestAffectingDeclarationsRoot(section), key, maxOffset, classes));
        }
        return result;
    }

    public static ItemPresentation getPresentation(final PascalNamedElement element) {
        return new ItemPresentation() {
            @Override
            @RequiredReadAction
            public LocalizeValue getPresentableText() {
                if (element instanceof PascalRoutine && isASTAvailable(element)) {
                    return LocalizeValue.of(element.getText());
                }
                String typeString = "";
                if (element instanceof PascalIdentDecl idDecl) {
                    typeString = idDecl.getTypeString();
                    if (!StringUtil.isEmpty(typeString)) {
                        typeString = ": " + typeString;
                    }
                }
                return LocalizeValue.of(ResolveUtil.cleanupName(element.getName()) + typeString);
            }

            @Override
            @RequiredReadAction
            public @Nullable String getLocationString() {
                return getType(element) + (element.getContainingFile() != null ? element.getContainingFile().getName() : "-");
            }

            @Override
            public @Nullable Image getIcon(boolean unused) {
                if (element instanceof PascalIdentDeclImpl idDecl) {
                    PasIdentStub stub = idDecl.retrieveStub();
                    PasField.FieldType type = stub != null ? stub.getType() : PsiUtil.getFieldType(element);
                    switch (type) {
                        case VARIABLE:
                            return PlatformIconGroup.nodesVariable();
                        case TYPE:
                            return PlatformIconGroup.nodesType();
                        case CONSTANT:
                            return PlatformIconGroup.nodesConstant();
                        case ROUTINE:
                            return PlatformIconGroup.nodesFunction();
                    }
                }
                else if (element instanceof PasClassTypeDecl) {
                    return PlatformIconGroup.nodesClass();
                }
                else if (element instanceof PasRecordDecl) {
                    return PlatformIconGroup.nodesRecord();
                }
                else if (element instanceof PasInterfaceTypeDecl) {
                    return PlatformIconGroup.nodesInterface();
                }
                else if (element instanceof PasObjectDecl) {
                    return PlatformIconGroup.nodesAnonymousclass();
                }
                else if (element instanceof PascalRoutine) {
                    return PlatformIconGroup.nodesFunction();
                }
                else if (element instanceof PasGenericTypeIdent) {
                    return PlatformIconGroup.nodesType();
                }
                else if (element instanceof PascalVariableDeclaration) {
                    return PlatformIconGroup.nodesVariable();
                }
                else if (element instanceof PasConstDeclaration) {
                    return PlatformIconGroup.nodesConstant();
                }
                else if (element instanceof PasClassProperty || element.getParent() instanceof PasClassProperty) {
                    return PlatformIconGroup.nodesProperty();
                }
                else if (element instanceof PasModule module) {
                    PascalModule.ModuleType moduleType = module.getModuleType();
                    if (moduleType == PascalModule.ModuleType.PROGRAM) {
                        return ObjectPascalIconGroup.program();
                    }
                    else if (moduleType == PascalModule.ModuleType.LIBRARY) {
                        return ObjectPascalIconGroup.library();
                    }
                    else {
                        return ObjectPascalIconGroup.unit();
                    }
                }
                return ObjectPascalIconGroup.pascal();
            }
        };
    }

    private static boolean isASTAvailable(PascalNamedElement element) {
        return !(element.getContainingFile() instanceof CompiledFileImpl);
    }

    private static LocalizeValue getType(PascalNamedElement item) {
        if (item instanceof PascalIdentDeclImpl idDecl) {
            PasIdentStub stub = idDecl.retrieveStub();
            PasField.FieldType type = stub != null ? stub.getType() : PsiUtil.getFieldType(item);
            switch (type) {
                case VARIABLE:
                    return LocalizeValue.localizeTODO("[var] ");
                case TYPE:
                    return LocalizeValue.localizeTODO("[type] ");
                case CONSTANT:
                    return LocalizeValue.localizeTODO("[const] ");
                case ROUTINE:
                    return LocalizeValue.localizeTODO("[routine] ");
            }
        }
        else if (item instanceof PasClassTypeDecl) {
            return LocalizeValue.localizeTODO("[class] ");
        }
        else if (item instanceof PasInterfaceTypeDecl) {
            return LocalizeValue.localizeTODO("[interface] ");
        }
        else if (item instanceof PasRecordDecl) {
            return LocalizeValue.localizeTODO("[record] ");
        }
        else if (item instanceof PasObjectDecl) {
            return LocalizeValue.localizeTODO("[object] ");
        }
        else if (item instanceof PascalRoutine) {
            return LocalizeValue.localizeTODO("[routine] ");
        }
        else if (item instanceof PasClassHelperDecl) {
            return LocalizeValue.localizeTODO("[class helper] ");
        }
        else if (item instanceof PasRecordHelperDecl) {
            return LocalizeValue.localizeTODO("[record helper] ");
        }
        else if (item instanceof PasVarDeclaration) {
            return LocalizeValue.localizeTODO("[var] ");
        }
        else if (item instanceof PasClassField) {
            return LocalizeValue.localizeTODO("[field] ");
        }
        else if (item instanceof PasConstDeclaration) {
            return LocalizeValue.localizeTODO("[const] ");
        }
        else if (item instanceof PasTypeDecl) {
            return LocalizeValue.localizeTODO("[type] ");
        }
        else if (item instanceof PasClassProperty) {
            return LocalizeValue.localizeTODO("[property] ");
        }
        return LocalizeValue.empty();
    }
}
