package org.anar.scheduling.object;

import java.util.ArrayList;
import java.util.List;

public class Machine {
    int number;
    String name;
    List<Operation> operations;

    public Machine(int number, List<Operation> operations) {
        this.number = number;
        this.operations = operations;
    }

    public Machine(int number, String name) {
        this(number, new ArrayList<>());
        this.name = name;
    }

    public void addOperation(Operation operation) {
        operations.add(operation);
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public List<Operation> getOperations() {
        return operations;
    }
}
