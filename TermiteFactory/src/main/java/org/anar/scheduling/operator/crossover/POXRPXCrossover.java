package org.anar.scheduling.operator.crossover;

import org.anar.scheduling.solution.SchedulingSolution;
import org.anar.scheduling.util.ListUtil;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

import java.util.*;

public class POXRPXCrossover implements CrossoverOperator<SchedulingSolution> {
    private static final double EPS = 1.0e-14;
    private int half;
    private double minProbability, maxProbability;
    private double pf;
    private RandomGenerator<Double> randomGenerator;

    public POXRPXCrossover(int half, double minProbability, double maxProbability) {
        this.half = half;
        this.minProbability = minProbability;
        this.maxProbability = maxProbability;
        pf = maxProbability;
        this.randomGenerator = () -> JMetalRandom.getInstance().nextDouble();
    }


    @Override
    public double getCrossoverProbability() {
        return 1;
    }

    @Override
    public int getNumberOfRequiredParents() {
        return 2;
    }

    @Override
    public int getNumberOfGeneratedChildren() {
        return 1;
    }

    public void computePf(int evaluations, int maxEvaluations) {
        pf = maxProbability - (maxProbability - minProbability) * (evaluations * 1.0 / maxEvaluations);
    }

    @Override
    public List<SchedulingSolution> execute(List<SchedulingSolution> solutions) {
        Check.notNull(solutions);
        Check.that(solutions.size() == 2, "");
        return doCrossover(1.0, solutions.get(0), solutions.get(1));
    }

    public List<SchedulingSolution> doCrossover(
            double probability, SchedulingSolution parent1, SchedulingSolution parent2) {

        List<SchedulingSolution> offspring = new ArrayList<>(1);

        SchedulingSolution child = (SchedulingSolution) parent1.copy();

        Set<Integer> set = ListUtil.<Integer>randomDivide2(parent1.jobs())[0];
        int j = 0;
        for (int i = 0; i < half; i++) {
            if (set.contains(parent1.variables().get(i))) {
                child.variables().set(i, parent1.variables().get(i));
            } else {
                while (set.contains(parent2.variables().get(j))) {
                    ++j;
                }
                child.variables().set(i, parent2.variables().get(j));
                ++j;
            }
        }

        Double[] r = new Double[half];
        int[] mark1 = new int[parent1.jobs().length];
        Queue<Integer>[] arr = new Queue[parent1.jobs().length];
        for (int i = 0; i < parent1.jobs().length; i++) {
            arr[i] = new LinkedList<>();
        }

        for (int i = 0; i < r.length; i++) {
            r[i] = randomGenerator.getRandomValue();
        }

        for (int i = 0; i < half; i++) {
            if (r[i] < pf)
                ++mark1[parent1.variables().get(i)];
        }

        int[] mark2 = Arrays.copyOf(mark1, mark1.length);

        for (int i = 0; i < half; i++) {
            int job = parent2.variables().get(i);
            if (mark2[job] > 0) {
                arr[job].add(parent2.variables().get(i+half));
                --mark2[job];
            }
        }
        for (int i = 0; i < half; i++) {
            int job = parent1.variables().get(i);
            if (mark1[job] > 0) {
                --mark1[job];
            } else {
                arr[job].add(parent1.variables().get(i+half));
            }
        }
        for (int i = 0; i < half; i++) {
            int job = child.variables().get(i);
            child.variables().set(i + half, arr[job].poll());
        }

        offspring.add(child);
        return offspring;
    }

    public static void main(String[] args) {
        SchedulingSolution solution1 = new SchedulingSolution(18, 1, 0, 4);
        solution1.variables().clear();
        solution1.variables().addAll(List.of(2,3,1,1,2,4,3,2,4,1,1,2,1,3,2,4,2,3));

        SchedulingSolution solution2 = new SchedulingSolution(18, 1, 0, 4);
        solution2.variables().clear();
        solution2.variables().addAll(List.of(1,3,1,2,4,2,3,4,2,2,1,1,3,1,2,3,2,2));

        System.out.println(solution1.variables());
        System.out.println(solution2.variables());
        POXRPXCrossover crossover = new POXRPXCrossover(solution1.variables().size() / 2, 0.35, 0.35);
        for (int i = 0; i < 2500000; i++) {
            List<SchedulingSolution> child = crossover.execute(List.of(solution1, solution2));
        }
//        System.out.println(child.get(0).variables());
    }
}
