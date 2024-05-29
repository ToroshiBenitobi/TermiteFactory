package org.anar.scheduling.object;

import java.util.ArrayList;
import java.util.List;

public class Allocation {
    List<Machine> machines;

    public Allocation( List<Machine> machines) {
        this.machines = machines;
    }

    public Allocation() {
        this(new ArrayList<>());
    }

    public void addMachine(Machine machine) {
        machines.add(machine);
    }

    public List<Machine> getMachines() {
        return machines;
    }
}
