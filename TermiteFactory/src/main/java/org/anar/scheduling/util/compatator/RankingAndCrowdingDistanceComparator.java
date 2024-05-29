package org.anar.scheduling.util.compatator;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.densityestimator.impl.CrowdingDistanceDensityEstimator;
import org.uma.jmetal.util.ranking.Ranking;
import org.uma.jmetal.util.ranking.impl.FastNonDominatedSortRanking;

import java.io.Serializable;
import java.util.Comparator;

public class RankingAndCrowdingDistanceComparator<S extends Solution<?>>
        implements Comparator<S>, Serializable {
    private final Comparator<S> rankComparator;
    private final Comparator<S> crowdingDistanceComparator;

    public RankingAndCrowdingDistanceComparator(Ranking<S> ranking) {
        this.rankComparator = Comparator.comparing(ranking::getRank);
        CrowdingDistanceDensityEstimator<S> crowdingDistanceDensityEstimator =
                new CrowdingDistanceDensityEstimator<>();
        this.crowdingDistanceComparator = crowdingDistanceDensityEstimator.getComparator();
    }

    public RankingAndCrowdingDistanceComparator() {
        this(new FastNonDominatedSortRanking<>());
    }

    @Override
    public int compare(S o1, S o2) {
        int result = rankComparator.compare(o1, o2);
        if (result == 0) {
            result = crowdingDistanceComparator.compare(o1, o2);
        }
        return result;
    }
}
