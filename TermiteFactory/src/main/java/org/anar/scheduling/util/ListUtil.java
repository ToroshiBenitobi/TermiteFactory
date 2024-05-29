package org.anar.scheduling.util;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class ListUtil {

    static public <T> Set<T>[] randomDivide2(T[] list) {
        Random randomGenerator = new Random();
        Double rnd;
        Set<T>[] sets = new Set[2];
        sets[0] = new HashSet<>();
        sets[1] = new HashSet<>();
        for (int i = 0; i < list.length; i++) {
            rnd = randomGenerator.nextDouble();
            if (rnd < 0.5) {
                sets[0].add(list[i]);
            } else {
                sets[1].add(list[i]);
            }
        }
        return sets;
    }

    static public <T> T randomPick(Set<T> set, Random randomGenerator) {
        int size = set.size();
        int item = randomGenerator.nextInt(size); // In real life, the Random object should be rather more shared than this
        T[] arrayNumbers = (T[]) set.toArray();
        System.out.println(arrayNumbers.length);
        return (T) arrayNumbers[item];
    }

    public static <E> List<E> pickNRandomElements(List<E> list, int n, Random r) {
        int length = list.size();

        if (length < n) return null;

        //We don't need to shuffle the whole list
        for (int i = length - 1; i >= length - n; --i) {
            Collections.swap(list, i, r.nextInt(i + 1));
        }
        return list.subList(length - n, length);
    }

    public static <E> List<E> pickNRandomElements(List<E> list, int n) {
        return pickNRandomElements(list, n, ThreadLocalRandom.current());
    }

    public static <E> List<E> popNRandomElements(List<E> list, int n, Random r) {
        int length = list.size();

        if (length < n) return null;

        //We don't need to shuffle the whole list
        for (int i = length - 1; i >= length - n; --i) {
            Collections.swap(list, i, r.nextInt(i + 1));
        }
        List<E> subList = list.subList(length - n, length);
        for (int i = length - 1; i > length - 1 - n; i--) {
            list.remove(i);
        }
        return subList;
    }

    public static <E> List<E> popNRandomElements(List<E> list, int n) {
        return popNRandomElements(list, n, ThreadLocalRandom.current());
    }
}
