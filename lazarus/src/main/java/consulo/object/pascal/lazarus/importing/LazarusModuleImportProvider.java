package consulo.object.pascal.lazarus.importing;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.importing.AbstractExternalModuleImportProvider;
import consulo.externalSystem.importing.ExternalModuleImportContext;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.service.project.ProjectData;
import consulo.externalSystem.service.project.manage.ProjectDataManager;
import consulo.object.pascal.icon.ObjectPascalIconGroup;
import consulo.object.pascal.lazarus.externalSystem.LazarusConstants;
import consulo.project.Project;
import consulo.ui.image.Image;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

/**
 * @author VISTALL
 * @since 2026-10-06
 */
@ExtensionImpl
public class LazarusModuleImportProvider extends AbstractExternalModuleImportProvider {
    @Inject
    public LazarusModuleImportProvider(ProjectDataManager dataManager) {
        super(dataManager, LazarusConstants.SYSTEM_ID);
    }

    @Override
    public Image getIcon() {
        return ObjectPascalIconGroup.lazarus();
    }

    @Override
    public boolean canImport(Path fileOrDirectory) {
        return findProjectFile(fileOrDirectory.toFile()) != null;
    }

    @Override
    protected void doPrepare(ExternalModuleImportContext context) {
    }

    @Override
    protected void beforeCommit(DataNode<ProjectData> dataNode, Project project) {
    }

    @Override
    protected File getExternalProjectConfigToUse(File file) {
        File projectFile = findProjectFile(file);
        return projectFile != null ? projectFile : file;
    }

    @Override
    protected void applyExtraSettings(ExternalModuleImportContext context) {
    }

    @Nullable
    private static File findProjectFile(File fileOrDirectory) {
        if (!fileOrDirectory.isDirectory()) {
            return isProjectFile(fileOrDirectory) ? fileOrDirectory : null;
        }
        File[] children = fileOrDirectory.listFiles(LazarusModuleImportProvider::isProjectFile);
        if (children == null || children.length == 0) {
            return null;
        }
        Arrays.sort(children, Comparator.comparing(File::getName));
        return children[0];
    }

    private static boolean isProjectFile(File file) {
        return file.isFile() && file.getName().toLowerCase(Locale.ROOT).endsWith("." + LazarusConstants.PROJECT_EXTENSION);
    }
}
