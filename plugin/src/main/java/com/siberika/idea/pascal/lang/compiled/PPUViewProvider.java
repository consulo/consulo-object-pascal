package com.siberika.idea.pascal.lang.compiled;

import com.intellij.lang.Language;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.siberika.idea.pascal.DCUFileType;
import com.siberika.idea.pascal.PascalLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 14/11/2013
 */
public class PPUViewProvider extends SingleRootFileViewProvider implements FileViewProvider {
    public PPUViewProvider(@NotNull PsiManager manager, @NotNull VirtualFile virtualFile, boolean physical) {
        super(manager, virtualFile, physical);
    }

    // WTF in ascending method?
    @Override
    public PsiReference findReferenceAt(final int offset) {
        return findReferenceAt(offset, PascalLanguage.INSTANCE);
    }

    @Override
    protected PsiFile createFile(@NotNull final Project project, @NotNull final VirtualFile vFile, @NotNull final FileType fileType) {
        final FileIndexFacade fileIndex = ServiceManager.getService(project, FileIndexFacade.class);
        if (fileIndex.isInLibraryClasses(vFile) || !fileIndex.isInSource(vFile)) {
            if (fileType instanceof DCUFileType) {
                return new DCUFileImpl(getManager(), this);
            } else {
                return new PPUFileImpl(getManager(), this);
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Language getBaseLanguage() {
        return PascalLanguage.INSTANCE;
    }

}
