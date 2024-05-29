package org.anar.scheduling.operator.crossover;

import org.anar.scheduling.solution.SchedulingSolution;
import org.anar.scheduling.util.ListUtil;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class POXCrossover implements CrossoverOperator<SchedulingSolution> {
    private static final double EPS = 1.0e-14;
    private int start;
    private int end;
    private RandomGenerator<Double> randomGenerator;

    public POXCrossover(int start, int end) {
        this.start = start;
        this.end = end;
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
        int j = start;
        for (int i = start; i < end; i++) {
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

        offspring.add(child);
        return offspring;
    }

    public static void main(String[] args) {
        SchedulingSolution solution1 = new SchedulingSolution(18, 1, 0, 4);
        solution1.variables().clear();
        solution1.variables().addAll(List.of(1, 3, 2, 1, 2, 4, 3, 2, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1));

        SchedulingSolution solution2 = new SchedulingSolution(18, 1, 0, 4);
        solution2.variables().clear();
        solution2.variables().addAll(List.of(1, 2, 2, 1, 3, 3, 4, 2, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1));

        System.out.println(solution1.variables());
        System.out.println(solution2.variables());
        POXCrossover crossover = new POXCrossover(0, solution1.variables().size() / 2);
        List<SchedulingSolution> child = crossover.execute(List.of(solution1, solution2));
        System.out.println(child.get(0).variables());

        int[] count = new int[solution1.jobs().length];
        System.out.println(Arrays.toString(count));
    }
}
