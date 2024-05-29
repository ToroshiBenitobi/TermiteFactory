package org.anar.scheduling.operator.mutation;

import org.anar.scheduling.converter.SchedulingSolutionConverter;
import org.anar.scheduling.object.ProcessTime;
import org.anar.scheduling.solution.SchedulingSolution;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.problem.doubleproblem.DoubleProblem;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

import java.util.*;

public class SchedulingMutation implements MutationOperator<SchedulingSolution> {
    private static final double DEFAULT_PROBABILITY = 0.01;
    private double mutationProbability;
    private List<Map<Integer, Double>>[] processTime;
    private RandomGenerator<Double> randomGenerator;

    /**
     * Constructor
     */
    public SchedulingMutation() {
        this(DEFAULT_PROBABILITY);
    }

    /**
     * Constructor
     */
    public SchedulingMutation(
            DoubleProblem problem, RandomGenerator<Double> randomGenerator) {
        this(1.0 / problem.getNumberOfVariables(), null);
        this.randomGenerator = randomGenerator;
    }

    /**
     * Constructor
     */
    public SchedulingMutation(
            double mutationProbability,
            List<Map<Integer, Double>>[] processTime) {
        this(
                mutationProbability,
                processTime,
                () -> JMetalRandom.getInstance().nextDouble());
    }

    /**
     * Constructor
     */
    public SchedulingMutation(
            double mutationProbability) {
        this(
                mutationProbability,
                null,
                () -> JMetalRandom.getInstance().nextDouble());
    }

    /**
     * Constructor
     */
    public SchedulingMutation(
            double mutationProbability,
            List<Map<Integer, Double>>[] processTime,
            RandomGenerator<Double> randomGenerator) {
        Check.probabilityIsValid(mutationProbability);
        this.mutationProbability = mutationProbability;
        this.processTime = processTime;
        this.randomGenerator = randomGenerator;
    }

    /* Getters */
    @Override
    public double getMutationProbability() {
        return mutationProbability;
    }

    public List<Map<Integer, Double>>[] getProcessTime() {
        return processTime;
    }

    /* Setters */
    public void setMutationProbability(double probability) {
        this.mutationProbability = probability;
    }

    public void setProcessTime(List<Map<Integer, Double>>[] processTime) {
        this.processTime = processTime;
    }

    /**
     * Execute() method
     */
    @Override
    public SchedulingSolution execute(SchedulingSolution solution) throws JMetalException {
        Check.notNull(solution);
        Check.notNull(processTime);

        doMutation(solution);

        return solution;
    }

    /**
     * Perform the mutation operation
     */
    private void doMutation(SchedulingSolution solution) {
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


//        for (int i = 0; i < half - 1; i++) {
//            if (randomGenerator.getRandomValue() <= mutationProbability) {
//                int j = i;
//                while (j == i) {
//                    j = random.nextInt(half - 1);
//                }
//                swap1 = solution.variables().get(i);
//                swap1m = solution.variables().get(i + half);
//                swap2 = solution.variables().get(j);
//                swap2m = solution.variables().get(j + half);
//
//                solution.variables().set(i, swap2);
//                solution.variables().set(i + half, swap2m);
//                solution.variables().set(j, swap1);
//                solution.variables().set(j + half, swap1m);
//            }
//        }

//        if (randomGenerator.getRandomValue() <= mutationProbability) {
//            int i = random.nextInt(half - 1);
//            int j = i;
//            while (j == i) {
//                j = random.nextInt(half - 1);
//            }
//            swap1 = solution.variables().get(i);
//            swap1m = solution.variables().get(i + half);
//            swap2 = solution.variables().get(j);
//            swap2m = solution.variables().get(j + half);
//
//            solution.variables().set(i, swap2);
//            solution.variables().set(i + half, swap2m);
//            solution.variables().set(j, swap1);
//            solution.variables().set(j + half, swap1m);
//        }


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
    }

//            [0, 0, 2, 2, 0, 1, 1, 2, 1,
//            0, 2, 0, 1, 1, 1, 0, 0, 0]
//


//
//            [0, 0, 2, 2, 0, 1, 1, 2, 1,
//            0, 1, 0, 1, 2, 1, 1, 0, 2]


    public static void main(String[] args) {
        SchedulingSolution solution = new SchedulingSolution(18, 1, 0, 3);
        solution.variables().clear();
        solution.variables().addAll(List.of(0, 0, 2, 2, 0, 1, 1, 2, 1,
                0, 2, 0, 1, 1, 1, 0, 0, 0));
        System.out.println(solution.variables());
        List<Map<Integer, Double>>[] processTime = new List[3];
        processTime[0] = List.of(
                Map.of(0, 8.0, 1, 6.0),
                Map.of(2, 4.0, 3, 6.0, 4, 5.0),
                Map.of(5, 5.0, 6, 3.0)
        );
        processTime[1] = List.of(
                Map.of(0, 7.0, 1, 6.0),
                Map.of(2, 4.0, 3, 4.0, 4, 5.0),
                Map.of(5, 5.0, 6, 6.0)
        );
        processTime[2] = List.of(
                Map.of(0, 6.0, 1, 9.0),
                Map.of(2, 6.0, 3, 3.0, 4, 5.0),
                Map.of(5, 3.0, 6, 4.0)
        );
//        Set<Integer> ys = processTime[1].get(1).keySet();
//        Integer[] alterNumbers = ys.toArray(new Integer[ys.size()]);
//        Arrays.sort(alterNumbers);
//        for (Integer alterNumber : alterNumbers) {
//            System.out.println(alterNumber);
//        }
        ProcessTime processTimeTable = new ProcessTime(processTime);
        SchedulingMutation schedulingMutation = new SchedulingMutation(0.5, processTime);
        List<Integer>[] allocations = SchedulingSolutionConverter.decode(solution, processTimeTable, 7);
        for (List<Integer> operations : allocations) {
            System.out.println(operations);
        }
        for (int i = 0; i < 100; i++) {
            schedulingMutation.execute(solution);

            System.out.println(solution.variables());

            List<Integer>[] allocation = SchedulingSolutionConverter.decode(solution, processTimeTable, 7);
            for (List<Integer> operations : allocation) {
                System.out.println(operations);
            }
        }
    }
}
