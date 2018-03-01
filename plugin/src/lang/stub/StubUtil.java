package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.containers.SmartHashSet;
import com.intellij.util.io.StringRef;
import com.siberika.idea.pascal.lang.stub.struct.PasClassDeclStub;

import java.io.IOException;
import java.util.Set;

public class StubUtil {
    public static final String ENUM_NULL = "-";

    public static <T extends Enum<T>> T readEnum(StubInputStream dataStream, Class<T> clazz) throws IOException {
        String name = readName(dataStream);
        return (name != null) && !name.equals(ENUM_NULL) ? T.valueOf(clazz, name) : null;
    }

    public static String readName(StubInputStream dataStream) throws IOException {
        StringRef ref = dataStream.readName();
        return ref != null ? ref.getString() : null;
    }

    public static void printStub(String msg, StubElement stub) {
        String stubStr = "?";
        if (stub instanceof PasModuleStub) {
            stubStr = "[M]" + ((PasModuleStub) stub).getName();
        } else if (stub instanceof PasExportedRoutineStub) {
            stubStr = "[R]" + ((PasExportedRoutineStub) stub).getName();
        } else if (stub instanceof PasIdentStub) {
            stubStr = "[I]" + ((PasIdentStub) stub).getName();
        } else if (stub instanceof PasClassDeclStub) {
            stubStr = "[C]" + ((PasClassDeclStub) stub).getName();
        }
        StubElement parent = stub.getParentStub();
        String parentStr = " - ";
        if (parent instanceof PasModuleStub) {
            parentStr = "[M]" + ((PasModuleStub) parent).getName();
        } else if (parent instanceof PasExportedRoutineStub) {
            parentStr = "[R]" + ((PasExportedRoutineStub) parent).getName();
        } else if (parent instanceof PasClassDeclStub) {
            parentStr = "[C]" + ((PasClassDeclStub) parent).getName();
        }
        //System.out.println(String.format(msg + ": %s ^ %s", stubStr, parentStr));
    }

    public static void writeStringSet(StubOutputStream dataStream, Set<String> set) throws IOException {
        dataStream.writeInt(set.size());
        for (String entry : set) {
            dataStream.writeName(entry);
        }
    }

    public static Set<String> readStringSet(StubInputStream dataStream) throws IOException {
        int size = dataStream.readInt();
        Set<String> result = new SmartHashSet<String>(size);
        for (int i = 0; i < size; i++) {
            StringRef ref = dataStream.readName();
            if (ref != null) {
                result.add(ref.getString());
            }
        }
        return result;
    }
}
