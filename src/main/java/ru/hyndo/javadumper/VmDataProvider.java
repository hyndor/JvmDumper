package ru.hyndo.javadumper;

import com.sun.tools.attach.VirtualMachineDescriptor;
import ru.hyndo.javadumper.outmode.OutMode;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.tools.Tool;
import sun.jvm.hotspot.tools.jcore.ClassDump;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class VmDataProvider extends Tool {

    private static Method startMethod;

    static {
        try {
            startMethod = Tool.class.getDeclaredMethod("start", String[].class);
            startMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private VirtualMachineDescriptor vm;
    private DumpMode dumpMode;
    private Set<String> classNames = new HashSet<>();
    private String classNameToDump;
    private Path tempDataOutput;
    private boolean parsed = false;

    VmDataProvider(VirtualMachineDescriptor vm, DumpMode dumpMode) {
        this.vm = vm;
        this.dumpMode = dumpMode;
    }

    VmDataProvider(VirtualMachineDescriptor vm, DumpMode dumpMode, String classNameToDump, Path tempDataOutput) {
        this.vm = vm;
        this.dumpMode = dumpMode;
        this.classNameToDump = classNameToDump;
        this.tempDataOutput = tempDataOutput;
    }

    private void startParsing(String PID) {
        try {
            //noinspection JavaReflectionInvocation
            startMethod.invoke(this, ((Object) (new String[]{PID})));
        } catch (Throwable e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        } finally {
            stop();
        }
    }

    Set<String> getClassNames() {
        if (!parsed) {
            startParsing(vm.id());
            parsed = true;
        }
        return classNames;
    }

    void dumpClass(OutMode outMode) {
        startParsing(vm.id());
        try {
            Files.walk(tempDataOutput)
                    .filter(file -> file.toString().endsWith(".class"))
                    .filter(file -> file.toFile().getName().equalsIgnoreCase(classNameToDump.substring(classNameToDump.lastIndexOf(".") + 1) + ".class"))
                    .forEach(classFile -> {
                        new Thread(() -> outMode.print(classFile, classNameToDump)).start();
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        VM.getVM().getSystemDictionary().classesDo(klass -> {
            String className = klass.getName().asString().replace('/', '.');
            if (dumpMode == DumpMode.CLASS_LIST) {
                classNames.add(className);
            } else {
                if (className.equals(classNameToDump)) {
                    String filter = className.substring(0, className.lastIndexOf("."));
                    ClassDump dumper = new ClassDump(null, filter);
                    dumper.setOutputDirectory(tempDataOutput.toAbsolutePath().toString());
                    dumper.run();
                }
            }
        });
    }

    public enum DumpMode {
        CLASS_LIST, DUMP_CLASS
    }

}
