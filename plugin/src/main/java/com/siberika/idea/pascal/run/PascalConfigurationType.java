package com.siberika.idea.pascal.run;

import com.intellij.execution.configurations.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.siberika.idea.pascal.PascalIcons;
import consulo.ui.image.Image;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 12/5/12
 */
public class PascalConfigurationType implements ConfigurationType, DumbAware {
    private final ConfigurationFactory myFactory;

    public PascalConfigurationType() {
        myFactory = new ConfigurationFactory(this) {
            @NotNull
            @Override
            public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
                return new PascalRunConfiguration(getDisplayName(), new RunConfigurationModule(project), this);
            }
        };
    }

    @NotNull
    public String getDisplayName() {
        return "Pascal";
    }

    public String getConfigurationTypeDescription() {
        return "Pascal run configuration";
    }

    public Image getIcon() {
        return PascalIcons.GENERAL;
    }

    @NotNull
    public String getId() {
        return "#com.siberika.idea.pascal.run.PascalConfigurationType";
    }

    public static PascalConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(PascalConfigurationType.class);
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{myFactory};
    }
}
