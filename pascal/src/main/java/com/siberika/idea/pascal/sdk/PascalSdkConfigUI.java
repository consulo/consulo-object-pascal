package com.siberika.idea.pascal.sdk;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.siberika.idea.pascal.DCUFileType;
import com.siberika.idea.pascal.PPUFileType;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import consulo.application.ApplicationManager;
import consulo.configurable.ConfigurationException;
import consulo.content.bundle.AdditionalDataConfigurable;
import consulo.content.bundle.Sdk;
import consulo.disposer.Disposable;
import consulo.document.FileDocumentManager;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileEditor.util.FileContentUtil;
import consulo.language.editor.WriteCommandAction;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.FileTypeIndex;
import consulo.language.psi.stub.FileBasedIndex;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.project.Project;
import consulo.project.ProjectManager;
import consulo.ui.ex.JBColor;
import consulo.ui.ex.awt.*;
import consulo.ui.ex.awtUnsafe.TargetAWT;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.event.VirtualFileEvent;
import consulo.virtualFileSystem.event.VirtualFileListener;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 18/01/2013
 */
public class PascalSdkConfigUI implements AdditionalDataConfigurable {
    private TextFieldWithBrowseButton compilerCommandEdit;

    private Sdk sdk;
    private JTextField namespacesEdit;
    private JTextField compilerOptionsEdit;
    private JTextField compilerOptionsDebugEdit;
    private TextFieldWithBrowseButton decompilerCommandEdit;
    private ComboBox syntaxCheckModeCBox;
    private final Map<String, JComponent> keyComponentMap = new HashMap<String, JComponent>();

    @Override
    public consulo.ui.Component createUIComponent(Disposable disposable) {
        JPanel panel = createGeneralOptionsPanel();

        keyComponentMap.clear();
        keyComponentMap.put(PascalSdkData.Keys.COMPILER_COMMAND.getKey(), compilerCommandEdit);
        keyComponentMap.put(PascalSdkData.Keys.COMPILER_NAMESPACES.getKey(), namespacesEdit);
        keyComponentMap.put(PascalSdkData.Keys.COMPILER_OPTIONS.getKey(), compilerOptionsEdit);
        keyComponentMap.put(PascalSdkData.Keys.COMPILER_OPTIONS_DEBUG.getKey(), compilerOptionsDebugEdit);
        keyComponentMap.put(PascalSdkData.Keys.DECOMPILER_COMMAND.getKey(), decompilerCommandEdit);
        keyComponentMap.put(PascalSdkData.Keys.SYNTAX_CHECK_MODE.getKey(), syntaxCheckModeCBox);

        return TargetAWT.wrap(panel);
    }

    private JPanel createGeneralOptionsPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new LineBorder(JBColor.border()));
        panel.setLayout(new GridLayoutManager(7, 2, JBUI.emptyInsets(), -1, -1));

        int row = 0;
        addLabel(panel, PascalBundle.message("ui.sdkSettings.compiler.command"), row);
        compilerCommandEdit = addFileFieldWithBrowse(panel, row++);

        addLabel(panel, PascalBundle.message("ui.sdkSettings.compiler.namespaces"), row);
        namespacesEdit = new JTextField();
        panel.add(namespacesEdit, new GridConstraints(row++, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null, 0, false));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.compiler.options"), row);
        compilerOptionsEdit = new JTextField();
        panel.add(compilerOptionsEdit, new GridConstraints(row++, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.compiler.options.debug"), row);
        compilerOptionsDebugEdit = new JTextField();
        panel.add(compilerOptionsDebugEdit, new GridConstraints(row++, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.decompiler.command"), row);
        decompilerCommandEdit = addFileFieldWithBrowse(panel, row++);

        addLabel(panel, PascalBundle.message("ui.sdkSettings.syntax.check.mode"), row);
        syntaxCheckModeCBox = new ComboBox(PascalSdkData.SYNTAX_CHECK_MODES);
        panel.add(syntaxCheckModeCBox, new GridConstraints(row++, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        JLabel statusLabel = new JLabel();
        panel.add(statusLabel, new GridConstraints(row, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        if (BasePascalSdkType.getAdditionalData(sdk).getBoolean(PascalSdkData.Keys.DELPHI_IS_STARTER)) {
            statusLabel.setText(PascalBundle.message("ui.sdkSettings.delphi.starter.warning"));
        }
        return panel;
    }

    private void addLabel(JPanel panel, String caption, int row) {
        final JLabel label1 = new JLabel(caption);
        panel.add(label1, new GridConstraints(row, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
    }

    private TextFieldWithBrowseButton addFileFieldWithBrowse(JPanel panel, int row) {
        FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleLocalFileDescriptor();
        fileChooserDescriptor.setTitle(PascalBundle.message("title.choose.file"));
        TextFieldWithBrowseButton field = new TextFieldWithBrowseButton();
        field.addBrowseFolderListener(new TextBrowseFolderListener(fileChooserDescriptor));
        panel.add(field, new GridConstraints(row, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null, 0, false));
        return field;
    }

    public boolean isModified() {
        for (Map.Entry<String, JComponent> entry : keyComponentMap.entrySet()) {
            if (!getValue(entry.getValue()).equals(BasePascalSdkType.getAdditionalData(sdk).getValue(entry.getKey()))) {
                return true;
            }
        }
        return false;
    }

    public void apply() throws ConfigurationException {
        if ((decompilerCommandEdit != null) &&
                !getValue(keyComponentMap.get(PascalSdkData.Keys.DECOMPILER_COMMAND.getKey())).equals(
                        BasePascalSdkType.getAdditionalData(sdk).getValue(PascalSdkData.Keys.DECOMPILER_COMMAND.getKey()))
        ) {
            BasePascalSdkType.getAdditionalData(sdk).setValue(PascalSdkData.Keys.DECOMPILER_CACHE.getKey(), null);
            invalidateCompiledCache();
        }
        for (Map.Entry<String, JComponent> entry : keyComponentMap.entrySet()) {
            BasePascalSdkType.getAdditionalData(sdk).setValue(entry.getKey(), getValue(keyComponentMap.get(entry.getKey())));
        }
        BasePascalSdkType.invalidateSdkCaches();
    }

    private Object getValue(JComponent control) {
        if (control instanceof TextFieldWithBrowseButton) {
            return ((TextFieldWithBrowseButton) control).getText();
        } else if (control instanceof JTextField) {
            return ((JTextField) control).getText();
        } else if (control instanceof JCheckBox) {
            return ((JCheckBox) control).isSelected() ? PascalSdkData.SDK_DATA_TRUE : "0";
        } else if (control instanceof ComboBox) {
            return ((ComboBox) control).getSelectedItem();
        } else {
            throw new IllegalStateException("getValue: Invalid control: " + ((control != null) ? control.getClass() : "null"));
        }
    }

    private void invalidateCompiledCache() {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                Project[] projects = ProjectManager.getInstance().getOpenProjects();
                final FileDocumentManager documentManager = FileDocumentManager.getInstance();
                for (final Project project : projects) {
                    final Module[] modules = ModuleManager.getInstance(project).getModules();
                    WriteCommandAction.runWriteCommandAction(project, new Runnable() {
                        @Override
                        public void run() {
                            for (Module module : modules) {
                                Collection<VirtualFile> files = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, PPUFileType.INSTANCE, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
                                files.addAll(FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, DCUFileType.INSTANCE, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)));
                                for (VirtualFile virtualFile : files) {
                                    ((VirtualFileListener) documentManager).contentsChanged(new VirtualFileEvent(null, virtualFile, virtualFile.getParent(), 0, 0));
                                }
                                FileContentUtil.reparseFiles(project, files, true);
                            }
                        }
                    });
                }
            }
        });
    }

    public void reset() {
        for (Map.Entry<String, JComponent> entry : keyComponentMap.entrySet()) {
            JComponent control = keyComponentMap.get(entry.getKey());
            if (control.isVisible()) {
                setValue(control, BasePascalSdkType.getAdditionalData(sdk).getValue(entry.getKey()));
            }
        }
    }

    private void setValue(JComponent control, Object value) {
        if (control instanceof TextFieldWithBrowseButton) {
            ((TextFieldWithBrowseButton) control).setText((String) value);
        } else if (control instanceof JTextField) {
            ((JTextField) control).setText((String) value);
        } else if (control instanceof JCheckBox) {
            ((JCheckBox) control).setSelected(PascalSdkData.SDK_DATA_TRUE.equals(value));
        } else if (control instanceof ComboBox) {
            ((ComboBox) control).setSelectedItem(value);
        } else {
            throw new IllegalStateException("setValue: Invalid control: " + ((control != null) ? control.getClass() : "null"));
        }
    }

    public void disposeUIResources() {
    }

    @Override
    public void setSdk(Sdk sdk) {
        this.sdk = sdk;
    }
}

