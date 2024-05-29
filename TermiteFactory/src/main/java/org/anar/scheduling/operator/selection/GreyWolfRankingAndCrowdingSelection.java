package org.anar.scheduling.operator.selection;

import org.anar.scheduling.util.ListUtil;
import org.anar.scheduling.util.compatator.SchedulingRankingAndCrowdingDistanceComparator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.densityestimator.impl.CrowdingDistanceDensityEstimator;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.ranking.Ranking;
import org.uma.jmetal.util.ranking.impl.FastNonDominatedSortRanking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GreyWolfRankingAndCrowdingSelection<S extends Solution<?>>
        implements SelectionOperator<List<S>, List<S>> {
    private final int topWolvesNumber;
    private final int individualNumber;
    private Comparator<S> dominanceComparator;


    /**
     * Constructor
     */
    public GreyWolfRankingAndCrowdingSelection(int topWolfNumber, int individualNumber, Comparator<S> dominanceComparator) {
        this.dominanceComparator = dominanceComparator;
        this.topWolvesNumber = topWolfNumber;
        this.individualNumber = individualNumber;
    }

    /**
     * Constructor
     */
    public GreyWolfRankingAndCrowdingSelection(int individualNumber) {
        this(3, individualNumber, new SchedulingRankingAndCrowdingDistanceComparator<>());
    }

    /* Getter */
    public int getNumberOfSolutionsToSelect() {
        return topWolvesNumber;
    }

    /**
     * Execute() method
     */
    public List<S> execute(List<S> solutionList) throws JMetalException {
        if (null == solutionList) {
            throw new JMetalException("The solution list is null");
        } else if (solutionList.isEmpty()) {
            throw new JMetalException("The solution list is empty");
        } else if (solutionList.size() < topWolvesNumber) {
            throw new JMetalException("The population size (" + solutionList.size() + ") is smaller than" +
                    "the solutions to selected (" + topWolvesNumber + ")");
        }

        Ranking<S> ranking = new FastNonDominatedSortRanking<S>(dominanceComparator);
        ranking.compute(solutionList);

        return crowdingDistanceSelection(ranking);
    }

    protected List<S> crowdingDistanceSelection(Ranking<S> ranking) {
        CrowdingDistanceDensityEstimator<S> crowdingDistance = new CrowdingDistanceDensityEstimator<>();
        List<S> individuals = new ArrayList<>(individualNumber);
        List<S> population = new ArrayList<>(individualNumber - topWolvesNumber);
        List<S> topWolves = new ArrayList<>(topWolvesNumber);
//        while (population.size() < individualNumber) {
//            if (subfrontFillsIntoThePopulation(ranking, rankingIndex, population)) {
//                crowdingDistance.compute(ranking.getSubFront(rankingIndex));
//                addRankedSolutionsToPopulation(ranking, rankingIndex, population);
//                rankingIndex++;
//            } else {
//                crowdingDistance.compute(ranking.getSubFront(rankingIndex));
//                addLastRankedSolutionsToPopulation(ranking, rankingIndex, population);
//            }
//        }
        int rankingIndex = 0;
        if (ranking.getSubFront(0).size() > topWolvesNumber) {
            topWolves.addAll(ListUtil.pickNRandomElements(ranking.getSubFront(0), topWolvesNumber));
            for (S topWolf : topWolves) {
                ranking.getSubFront(0).remove(topWolf);
            }
            for (int i = 0; i < ranking.getNumberOfSubFronts(); i++) {
                List<S> front;
                front = ranking.getSubFront(i);
                front.forEach(population::add);
            }
        } else {
            int remain = topWolvesNumber;
            while (remain > 0) {
                List<S> topFront = ranking.getSubFront(rankingIndex);
                int number = topFront.size() > remain ? remain : topFront.size();
                topWolves.addAll(ListUtil.pickNRandomElements(ranking.getSubFront(rankingIndex), topWolvesNumber));
                for (S topWolf : topWolves) {
                    ranking.getSubFront(rankingIndex).remove(topWolf);
                }
                rankingIndex++;
                remain -= number;
            }
            for (int i = 0; i < ranking.getNumberOfSubFronts(); i++) {
                List<S> front;
                front = ranking.getSubFront(i);
                front.forEach(population::add);
            }
        }

        individuals.addAll(topWolves);
        individuals.addAll(population);
        return individuals;
    }

}