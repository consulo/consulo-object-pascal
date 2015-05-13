package com.siberika.idea.pascal.sdk;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.PascalException;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.sdk.PascalSdkUtil;
import com.siberika.idea.pascal.util.SysUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 10/01/2013
 */
public class DelphiSdkType extends BasePascalSdkType {

    public static final Logger LOG = Logger.getInstance(DelphiSdkType.class.getName());
    private static final String[] LIBRARY_DIRS = {"rtl", "rtl-objpas", "pthreads", "regexpr", "x11", "windows"};
    private static final String DELPHI_SDK_TYPE_ID = "DelphiSdkType";

    @NotNull
    public static DelphiSdkType getInstance() {
        return SdkType.findInstance(DelphiSdkType.class);
    }

    public DelphiSdkType() {
        super(DELPHI_SDK_TYPE_ID);
        InputStream definesStream = getClass().getClassLoader().getResourceAsStream("/defines.xml");
        if (definesStream != null) {
            DefinesParser.parse(definesStream);
        }
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return PascalIcons.GENERAL;
    }

    @NotNull
    @Override
    public Icon getIconForAddAction() {
        return getIcon();
    }

    private static final List<String> DEFAULT_SDK_LOCATIONS_UNIX = Arrays.asList(
            "/usr/lib/codetyphon/fpc/fpc32", "/usr/lib/codetyphon/fpc",
            "/usr/lib/fpc", "/usr/share/fpc", "/usr/local/lib/fpc");
    private static final List<String> DEFAULT_SDK_LOCATIONS_WINDOWS = Arrays.asList("c:\\codetyphon\\fpc\\fpc32", "c:\\codetyphon\\fpc", "c:\\fpc");

    @Nullable
    @Override
    public String suggestHomePath() {
        List<String> paths = DEFAULT_SDK_LOCATIONS_UNIX;
        if (SystemInfo.isWindows) {
            paths = DEFAULT_SDK_LOCATIONS_WINDOWS;
        }
        for (String path : paths) {
            if (new File(path).exists()) {
                return path;
            }
        }
        return null;
    }

    @Override
    public boolean isValidSdkHome(@NotNull final String path) {
        LOG.info("Checking SDK path: " + path);
        final File fpcExe = PascalSdkUtil.getCompilerExecutable(path);
        return fpcExe.isFile() && fpcExe.canExecute();
    }

    @NotNull
    public String suggestSdkName(@Nullable final String currentSdkName, @NotNull final String sdkHome) {
        String version = getVersionString(sdkHome);
        if (version == null) return "Delphi v. ?? at " + sdkHome;
        return "Delphi v. " + version + " | " + getTargetString(sdkHome);
    }

    @Nullable
    public String getVersionString(String sdkHome) {
        LOG.info("Getting version for SDK path: " + sdkHome);
        try {
            return SysUtils.runAndGetStdOut(sdkHome, PascalSdkUtil.getCompilerExecutable(sdkHome).getAbsolutePath(), PascalSdkUtil.FPC_PARAMS_VERSION_GET);
        } catch (PascalException e) {
            LOG.warn(e.getMessage(), e);
        }
        return null;
    }

    @Nullable
    public static String getTargetString(String sdkHome) {
        LOG.info("Getting version for SDK path: " + sdkHome);
        try {
            return SysUtils.runAndGetStdOut(sdkHome, PascalSdkUtil.getCompilerExecutable(sdkHome).getAbsolutePath(), PascalSdkUtil.FPC_PARAMS_TARGET_GET);
        } catch (PascalException e) {
            LOG.warn(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void saveAdditionalData(@NotNull final SdkAdditionalData additionalData, @NotNull final Element additional) {
        if (additionalData instanceof PascalSdkData) {
            Object val = ((PascalSdkData) additionalData).getValue(PascalSdkData.DATA_KEY_COMPILER_OPTIONS);
            additional.setAttribute(PascalSdkData.DATA_KEY_COMPILER_OPTIONS, val != null ? (String) val : "");
        }
    }

    @Nullable
    @Override
    public SdkAdditionalData loadAdditionalData(Element additional) {
        PascalSdkData result = new PascalSdkData();
        if (additional != null) {
            result.setValue(PascalSdkData.DATA_KEY_COMPILER_OPTIONS, additional.getAttributeValue(PascalSdkData.DATA_KEY_COMPILER_OPTIONS));
        }
        return result;
    }

    @NonNls
    @Override
    public String getPresentableName() {
        return "Delphi SDK";
    }

    @Override
    public void setupSdkPaths(@NotNull final Sdk sdk) {
        String target = getTargetString(sdk.getHomePath());
        configureSdkPaths(sdk, target);
        configureOptions(sdk, getAdditionalData(sdk), target);
    }

    @Override
    protected void configureOptions(@NotNull Sdk sdk, PascalSdkData data, String target) {
        super.configureOptions(sdk, data, target);
        StrBuilder sb = new StrBuilder();
        if (SystemUtils.IS_OS_WINDOWS) {
            sb.append("-dMSWINDOWS ");
        } else {
            sb.append("-dPOSIX ");
            if (SystemUtils.IS_OS_MAC_OSX) {
                sb.append("-dMACOS ");
            } else {
                sb.append("-dLINUX ");
            }
        }
        if (target.contains("_64")) {
            sb.append("-dCPUX64 ");
        } else {
            sb.append("-dCPUX86 ");
        }
        data.setValue(PascalSdkData.DATA_KEY_COMPILER_OPTIONS, sb.toString());
    }

    private static void configureSdkPaths(@NotNull final Sdk sdk, String target) {
        LOG.info("Setting up SDK paths for SDK at " + sdk.getHomePath());
        final SdkModificator[] sdkModificatorHolder = new SdkModificator[]{null};
        final SdkModificator sdkModificator = sdk.getSdkModificator();
        if (target != null) {
            target = target.replace(' ', '-');
            for (String dir : LIBRARY_DIRS) {
                VirtualFile vdir = getLibrary(sdk, target, dir);
                if (vdir != null) {
                    sdkModificator.addRoot(vdir, OrderRootType.CLASSES);
                }
            }
            sdkModificatorHolder[0] = sdkModificator;
            sdkModificatorHolder[0].commitChanges();
        }
    }

    private static VirtualFile getLibrary(Sdk sdk, String target, String name) {
        File rtlDir = new File(sdk.getHomePath() + File.separatorChar + "units" + File.separatorChar + target + File.separatorChar + name);
        if (!rtlDir.exists()) {
            rtlDir = new File(sdk.getHomePath() + File.separatorChar + sdk.getVersionString() + File.separatorChar + "units" + File.separatorChar + target + File.separatorChar + name);
        }
        return LocalFileSystem.getInstance().findFileByIoFile(rtlDir);
    }

    @Override
    public boolean isRootTypeApplicable(OrderRootType type) {
        return type.equals(OrderRootType.SOURCES) || type.equals(OrderRootType.CLASSES);
    }

    @Nullable
    @Override
    public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull final SdkModel sdkModel, @NotNull final SdkModificator sdkModificator) {
        return new PascalSdkConfigUI();
    }

}