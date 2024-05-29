package org.anar.scheduling.operator.selection;

import org.anar.scheduling.util.compatator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.errorchecking.Check;

import java.util.Comparator;
import java.util.List;

public class NaryTournamentSelection<S extends Solution<?>>
        implements SelectionOperator<List<S>, S> {
    private Comparator<S> comparator;
    private int tournamentSize;

    public NaryTournamentSelection() {
        this(new RankingAndCrowdingDistanceComparator<>(), 2);
    }

    public NaryTournamentSelection(Comparator<S> comparator) {
        this(comparator, 2);
    }

    public NaryTournamentSelection(Comparator<S> comparator, int tournamentSize) {
        this.comparator = comparator;
        this.tournamentSize = tournamentSize;
    }

    @Override
    public S execute(List<S> sList) {
        Check.notNull(sList);
        Check.collectionIsNotEmpty(sList);
        Check.that(sList.size() >= tournamentSize, "");

        S result;
        if (sList.size() == 1) {
            result = sList.get(0);
        } else {
            List<S> selectedSList = SolutionListUtils.selectNRandomDifferentSolutions(
                    tournamentSize, sList
            );
            result = SolutionListUtils.findBestSolution(selectedSList, comparator);
        }

        return result;
    }

    public int getTournamentSize() {
        return tournamentSize;
    }
}
