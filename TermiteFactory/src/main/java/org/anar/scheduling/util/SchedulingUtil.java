package org.anar.scheduling.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SchedulingUtil {
    public static double computePCDTotalTime(List<Double> pcd0, List<Double> pcd1) {
        List<Double> all = new ArrayList<>();
        for (int i = 0; i < pcd0.size(); i++) {
            all.add(pcd0.get(i));
            all.add(pcd1.get(i));
        }
        Collections.sort(all);
        int cover = 0;
        double length = 0.0;
        for (int i = 0; i < all.size(); i++) {
            if ((i + 1) < all.size()) {
                if (all.get(i + 1).equals(all.get(i))) continue;
            }
            cover += numberOf(pcd0, all.get(i));
            cover -= numberOf(pcd1, all.get(i));
            if (cover == 0) continue;
            length += all.get(i + 1) - all.get(i);
        }
        return length;
    }

    public static double numberOf(List<Double> list, Double b) {
        int num = 0;
        for (Double a : list) {
            if (a.equals(b)) ++num;
        }
        return num;
    }
}
