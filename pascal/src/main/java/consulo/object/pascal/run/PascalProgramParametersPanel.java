package consulo.object.pascal.run;

import com.siberika.idea.pascal.run.PascalRunConfigurationParams;
import consulo.application.Application;
import consulo.execution.ui.CommonProgramParametersLayout;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.object.pascal.localize.ObjectPascalLocalize;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.ComboBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.dialog.DialogService;
import consulo.ui.util.FormBuilder;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class PascalProgramParametersPanel extends CommonProgramParametersLayout<PascalRunConfigurationParams> {
    private final Project myProject;
    private @Nullable ComboBox<Module> myModulesComboBox;

    public PascalProgramParametersPanel(Project project) {
        super(Application.get().getInstance(DialogService.class));
        myProject = project;
    }

    @RequiredUIAccess
    @Override
    protected void addAfter(FormBuilder builder) {
        ComboBox<Module> modulesComboBox = ComboBox.create(List.of(ModuleManager.getInstance(myProject).getModules()));
        modulesComboBox.setRenderer((presentation, item) -> {
            Module module = item.getValue();
            if (module != null) {
                presentation.withIcon(PlatformIconGroup.nodesModule());
                presentation.append(module.getName());
            }
        });
        myModulesComboBox = modulesComboBox;

        builder.addLabeled(ObjectPascalLocalize.runConfigurationModuleLabel(), modulesComboBox);
    }

    @RequiredUIAccess
    @Override
    public void apply(PascalRunConfigurationParams configuration) {
        super.apply(configuration);

        ComboBox<Module> modulesComboBox = myModulesComboBox;
        Module selectedModule = modulesComboBox == null ? null : modulesComboBox.getValue();
        configuration.setModuleName(selectedModule == null ? null : selectedModule.getName());
    }

    @RequiredUIAccess
    @Override
    public void reset(PascalRunConfigurationParams configuration) {
        super.reset(configuration);

        String moduleName = configuration.getModuleName();
        Module module = moduleName != null ? ModuleManager.getInstance(myProject).findModuleByName(moduleName) : null;

        ComboBox<Module> modulesComboBox = myModulesComboBox;
        if (modulesComboBox != null) {
            modulesComboBox.setValue(module);
        }
    }
}
