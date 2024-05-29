package org.anar.scheduling.operator.crossover;

import org.anar.scheduling.solution.SchedulingSolution;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

import java.util.*;

public class LocalSearchCrossover<S> implements CrossoverOperator<SchedulingSolution> {
    private static final double EPS = 1.0e-14;
    private int half;
    private double mutationProbability;
    private int numberOfNeighbors;
    private double pf;
    private List<Map<Integer, Double>>[] processTime;
    private RandomGenerator<Double> randomGenerator;

    public LocalSearchCrossover(int half, int numberOfNeighbors, double mutationProbability, List<Map<Integer, Double>>[] processTime) {
        this.half = half;
        this.numberOfNeighbors = numberOfNeighbors;
        this.mutationProbability = mutationProbability;
        this.processTime = processTime;
        this.randomGenerator = () -> JMetalRandom.getInstance().nextDouble();
    }


    @Override
    public double getCrossoverProbability() {
        return 1;
    }

    @Override
    public int getNumberOfRequiredParents() {
        return 1;
    }

    @Override
    public int getNumberOfGeneratedChildren() {
        return this.numberOfNeighbors;
    }

    @Override
    public List<SchedulingSolution> execute(List<SchedulingSolution> solutions) {
        Check.notNull(solutions);
        Check.that(solutions.size() == 1, "");
        return doCrossover(numberOfNeighbors, solutions.get(0));
    }

    public List<SchedulingSolution> doCrossover(int numberOfNeighbors, SchedulingSolution current) {

        List<SchedulingSolution> solutionList = new ArrayList<>(numberOfNeighbors);

        for (int k = 0; k < numberOfNeighbors; k++) {
            SchedulingSolution solution = (SchedulingSolution) current.copy();

            double rnd;
            int swap1, swap2, swap1m, swap2m;
            int y, job, index;
            int half = solution.variables().size() / 2;
            int[] counter = new int[solution.jobs().length];
            Random random = new Random();

            for (int i = 0; i < half - 1; i++) {
                if (randomGenerator.getRandomValue() <= mutationProbability) {
                    int j = i + 1;
                    swap1 = solution.variables().get(i);
                    swap2 = solution.variables().get(j);

                    if (swap1 != swap2) {
                        swap1m = solution.variables().get(i + half);
                        swap2m = solution.variables().get(j + half);

                        solution.variables().set(i, swap2);
                        solution.variables().set(i + half, swap2m);
                        solution.variables().set(j, swap1);
                        solution.variables().set(j + half, swap1m);
                    }
                }
            }

            for (int i = 0; i < half; i++) {
                job = solution.variables().get(i);
                if (randomGenerator.getRandomValue() <= mutationProbability) {
                    y = solution.variables().get(i + half);
                    Set<Integer> ys = processTime[job].get(counter[job]).keySet();
                    if (ys.size() > 1) {
                        int size = ys.size();
                        do {
//                        y = ListUtil.<Integer>randomPick(ys, random);
                            index = random.nextInt(size);
                        } while (index == solution.variables().get(i + half));
                        solution.variables().set(i + half, index);
                    }
                }
                ++counter[job];
            }

            solutionList.add(solution);
        }

        return solutionList;
    }

    public static void main(String[] args) {
        SchedulingSolution solution1 = new SchedulingSolution(18, 1, 0, 4);
        solution1.variables().clear();
        solution1.variables().addAll(List.of(2, 3, 1, 1, 2, 4, 3, 2, 4, 1, 1, 2, 1, 3, 2, 4, 2, 3));

        SchedulingSolution solution2 = new SchedulingSolution(18, 1, 0, 4);
        solution2.variables().clear();
        solution2.variables().addAll(List.of(1, 3, 1, 2, 4, 2, 3, 4, 2, 2, 1, 1, 3, 1, 2, 3, 2, 2));

        System.out.println(solution1.variables());
        System.out.println(solution2.variables());
        POXRPXCrossover crossover = new POXRPXCrossover(solution1.variables().size() / 2, 0.35, 0.35);
        for (int i = 0; i < 2500000; i++) {
            List<SchedulingSolution> child = crossover.execute(List.of(solution1, solution2));
        }
//        System.out.println(child.get(0).variables());
    }
}
