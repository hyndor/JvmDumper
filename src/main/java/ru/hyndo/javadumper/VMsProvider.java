package ru.hyndo.javadumper;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.util.List;
import java.util.stream.Collectors;

import static ru.hyndo.javadumper.JavaDumperApplication.APPLICATION_NAME;

public interface VMsProvider {

    default List<VirtualMachineDescriptor> getVMs() {
        return VirtualMachine
                .list()
                .stream()
                .filter(vm -> !vm.displayName().contains(APPLICATION_NAME))
                .collect(Collectors.toList());
    }

}
