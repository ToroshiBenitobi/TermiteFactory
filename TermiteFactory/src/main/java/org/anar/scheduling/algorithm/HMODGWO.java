package org.anar.scheduling.algorithm;

import org.anar.scheduling.operator.crossover.LocalSearchCrossover;
import org.anar.scheduling.operator.crossover.POXRPXCrossover;
import org.anar.scheduling.operator.selection.GreyWolfRankingAndCrowdingSelection;
import org.anar.scheduling.util.compatator.SingleObjectComparator;
import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HMODGWO<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {
    protected final int maxEvaluations;

    protected final SolutionListEvaluator<S> evaluator;

    protected int evaluations;
    protected Comparator<S> dominanceComparator;
    protected Comparator<S> singleObjectComparator;
    protected CrossoverOperator<S> localSearchOperator;

    protected S alphaVariant;
    protected S betaVariant;
    protected S deltaVariant;
    private RandomGenerator<Double> randomGenerator;
    protected int matingPoolSize;
    protected int offspringPopulationSize;

    /**
     * Constructor
     */
    public HMODGWO(Problem<S> problem, int maxEvaluations, int populationSize,
                   int matingPoolSize, int offspringPopulationSize,
                   CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                   SelectionOperator<List<S>, S> selectionOperator, CrossoverOperator<S> localSearchOperator, SolutionListEvaluator<S> evaluator) {
        this(problem, maxEvaluations, populationSize, matingPoolSize, offspringPopulationSize,
                crossoverOperator, mutationOperator, selectionOperator, new DominanceComparator<S>(), evaluator, localSearchOperator);
    }

    /**
     * Constructor
     */
    public HMODGWO(Problem<S> problem, int maxEvaluations, int populationSize,
                   int matingPoolSize, int offspringPopulationSize,
                   CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                   SelectionOperator<List<S>, S> selectionOperator, Comparator<S> dominanceComparator,
                   SolutionListEvaluator<S> evaluator, CrossoverOperator<S> localSearchOperator) {
        super(problem);
        this.maxEvaluations = maxEvaluations;
        setMaxPopulationSize(populationSize);
        ;

        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = selectionOperator;
        this.localSearchOperator = localSearchOperator;

        this.evaluator = evaluator;
        this.dominanceComparator = dominanceComparator;
        this.singleObjectComparator = (Comparator<S>) new SingleObjectComparator();

        this.matingPoolSize = matingPoolSize;
        this.offspringPopulationSize = offspringPopulationSize;

        this.randomGenerator = () -> JMetalRandom.getInstance().nextDouble();
    }

    @Override
    protected void initProgress() {
        evaluations = 1;
    }

    @Override
    protected void updateProgress() {
        evaluations += 1;
    }

    @Override
    protected boolean isStoppingConditionReached() {
        return evaluations >= maxEvaluations;
    }

    @Override
    protected List<S> evaluatePopulation(List<S> population) {
        population = evaluator.evaluate(population, getProblem());
        return population;
    }

    @Override
    protected List<S> selection(List<S> population) {
        List<S> matingPopulation = new ArrayList<>(population.size() - 3);
//        population.sort(dominanceComparator);

        alphaVariant = population.get(0);
        betaVariant = population.get(1);
        deltaVariant = population.get(2);

        population.remove(0);
        population.remove(1);
        population.remove(2);

        matingPopulation.addAll(population);

        alphaVariant = localSearch(alphaVariant);
        betaVariant = localSearch(betaVariant);
        deltaVariant = localSearch(deltaVariant);

        return matingPopulation;
    }

    protected S localSearch(S current) {
        List<S> neighbors = localSearchOperator.execute(List.of(current));

        neighbors.add(current);

        neighbors = evaluator.evaluate(neighbors, getProblem());

        neighbors.sort(dominanceComparator);

        return neighbors.get(0);

    }

    @Override
    protected List<S> reproduction(List<S> matingPool) {
        int numberOfParents = crossoverOperator.getNumberOfRequiredParents();
//        checkNumberOfParents(matingPool, numberOfParents);
        double rand;

        List<S> offspringPopulation = new ArrayList<>(offspringPopulationSize);
        for (int i = 0; i < matingPool.size(); i++) {
            List<S> parents = new ArrayList<>(numberOfParents);
            parents.add(matingPool.get(i));
            rand = randomGenerator.getRandomValue();
            if (rand < 1 / 3.0) {
                parents.add(alphaVariant);
            } else if (rand > 2 / 3.0) {
                parents.add(betaVariant);
            } else {
                parents.add(deltaVariant);
            }

            ((POXRPXCrossover) crossoverOperator).computePf(evaluations, maxEvaluations);
            List<S> offspring = crossoverOperator.execute(parents);


            offspring = evaluator.evaluate(offspring, getProblem());

            if (dominanceComparator.compare(offspring.get(0), parents.get(0)) >= 0)
                mutationOperator.execute(offspring.get(0));

            offspringPopulation.add(offspring.get(0));
        }

        return offspringPopulation;
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        List<S> jointPopulation = new ArrayList<>();
//        jointPopulation.addAll(population);
        jointPopulation.addAll(offspringPopulation);
        jointPopulation.add(alphaVariant);
        jointPopulation.add(betaVariant);
        jointPopulation.add(deltaVariant);
        GreyWolfRankingAndCrowdingSelection selection = new GreyWolfRankingAndCrowdingSelection(maxPopulationSize);
        return selection.execute(jointPopulation);
    }

    @Override
    public List<S> getResult() {
        return SolutionListUtils.getNonDominatedSolutions(getPopulation());
//        return getPopulation();
    }

    @Override
    public String getName() {
        return "HMOGWO";
    }

    @Override
    public String getDescription() {
        return "hybrid multi-objective grey wolf optimizer";
    }
}
