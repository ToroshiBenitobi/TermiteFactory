package org.anar.scheduling.util.compatator;

import org.anar.scheduling.solution.SchedulingSolution;

import java.io.Serializable;
import java.util.Comparator;

public class SingleObjectComparator implements Comparator<SchedulingSolution>, Serializable {
    @Override
    public int compare(SchedulingSolution o1, SchedulingSolution o2) {
        return Double.compare(o1.objectives()[0], o2.objectives()[0]);
    }
}
