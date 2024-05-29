package org.anar.scheduling.solution;

import org.anar.scheduling.object.ProcessTime;
import org.uma.jmetal.solution.AbstractSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.*;
import java.util.stream.IntStream;

public class SchedulingSolution extends AbstractSolution<Integer> implements IntegerSolution {
    private Integer[] jobs;

    public SchedulingSolution(int numberOfVariables, int numberOfObjectives, int numberOfConstraints, int numberOfJobs) {
        super(numberOfVariables, numberOfObjectives, numberOfConstraints);
        jobs = new Integer[numberOfJobs];
        for (int i = 0; i < numberOfJobs; i++) {
            jobs[i] = i;
        }
    }

    protected SchedulingSolution(int numberOfVariables, int numberOfObjectives) {
        this(numberOfVariables, numberOfObjectives, 0, 1);
    }

    protected SchedulingSolution(SchedulingSolution solution) {
        this(solution.variables().size(), solution.objectives().length, solution.constraints().length, solution.jobs().length);

        IntStream.range(0, solution.variables().size()).forEach(i -> variables().set(i, solution.variables().get(i)));
        IntStream.range(0, solution.objectives().length).forEach(i -> objectives()[i] = solution.objectives()[i]);
        IntStream.range(0, solution.constraints().length).forEach(i -> constraints()[i] = solution.constraints()[i]);

        this.jobs = solution.jobs;

        attributes = new HashMap<>(solution.attributes);
    }

    public void randomInit(ProcessTime processTime) {
        int[] numberOfOperations = processTime.getNumberOfOperations();
        int job = 0;
        int number = 0;
        int half = variables().size() / 2;
        List<Integer> oList = new ArrayList<>(half);
        List<Integer> mList = new ArrayList<>(half);
        for (int i = 0; i < half; i++) {
            oList.add(job);
            ++number;
            if (number == numberOfOperations[job]) {
                ++job;
                number = 0;
                Collections.shuffle(oList);
            }
        }

        int[] counter = new int[jobs.length];
        Random random = new Random();

        for (int i = 0; i < half; i++) {
            job = oList.get(i);
            Set<Integer> ys = processTime.getAlternativeMachineSet(job, counter[job]);
            int size = ys.size();
            int index = random.nextInt(size);
            mList.add(index);
            ++counter[job];
        }

        variables().clear();
        variables().addAll(oList);
        variables().addAll(mList);
    }

    public Integer[] jobs() {
        return jobs;
    }

    @Override
    public Integer getLowerBound(int i) {
        return 1;
    }

    @Override
    public Integer getUpperBound(int i) {
        return 500;
    }

    @Override
    public List<Integer> variables() {
        return super.variables();
    }

    @Override
    public double[] objectives() {
        return super.objectives();
    }

    @Override
    public double[] constraints() {
        return super.constraints();
    }

    @Override
    public Map<Object, Object> attributes() {
        return super.attributes();
    }

    @Override
    public Solution<Integer> copy() {
        return new SchedulingSolution(this);
    }
}
