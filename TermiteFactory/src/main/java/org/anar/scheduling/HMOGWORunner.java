package org.anar.scheduling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.anar.scheduling.algorithm.HMODGWOBuilder;
import org.anar.scheduling.converter.SchedulingSolutionConverter;
import org.anar.scheduling.object.ProblemInstance;
import org.anar.scheduling.operator.crossover.LocalSearchCrossover;
import org.anar.scheduling.operator.crossover.POXRPXCrossover;
import org.anar.scheduling.operator.mutation.SchedulingMutation;
import org.anar.scheduling.operator.selection.NaryTournamentSelection;
import org.anar.scheduling.problem.SchedulingProblem;
import org.anar.scheduling.solution.SchedulingSolution;
import org.anar.scheduling.util.AlgorithmRunner;
import org.anar.scheduling.util.compatator.RankingAndCrowdingDistanceComparator;
import org.anar.scheduling.util.compatator.SchedulingRankingAndCrowdingDistanceComparator;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.*;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class HMOGWORunner extends AbstractAlgorithmRunner {
    public static final double NaN = Double.NaN;

    public static String printFinalSolutionSet(List<SchedulingSolution> population, SchedulingProblem problem, JsonNode nameMaps) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        ArrayNode root = mapper.createArrayNode();

        for (SchedulingSolution solution : population) {
            ObjectNode node = mapper.createObjectNode();
            node.put("makeSpan", solution.objectives()[0]);
            node.put("productionCost", solution.objectives()[1]);
            node.put("averageIdlingRate", solution.objectives()[2]);
            node.put("allocation", mapper.readTree(SchedulingSolutionConverter.convertToJson(solution, problem, nameMaps)));
            root.add(node);
        }

        return mapper.writeValueAsString(root);
    }

    public static String run(String input) throws JMetalException, IOException {
        SchedulingProblem problem;
        Algorithm<List<SchedulingSolution>> algorithm;
        CrossoverOperator<SchedulingSolution> crossover;
        CrossoverOperator<SchedulingSolution> localSearch;
        MutationOperator<SchedulingSolution> mutation;
        SelectionOperator<List<SchedulingSolution>, SchedulingSolution> selection;
        Comparator<SchedulingSolution> comparator;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode nameMaps = mapper.readTree(input).get("nameMaps");

        ProblemInstance problemInstance = new ProblemInstance();
        problemInstance.readJson(input);
        problem = new SchedulingProblem(problemInstance.getProcessTimeTable().getVariables(), 3, 0);
        problem.loadInstance(problemInstance);

        double crossoverProbability = 0.9;
        crossover = new POXRPXCrossover(problem.getNumberOfVariables() / 2, 0.3, 0.6);

        localSearch = new LocalSearchCrossover<>(problem.getNumberOfVariables() / 2, 10,
                0.8, problemInstance.getProcessTime());

        double mutationProbability = 1.0 / problem.getNumberOfVariables();
        double mutationDistributionIndex = 20.0;
        mutation = new SchedulingMutation(0.5, problemInstance.getProcessTime());

        selection = new NaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());

        comparator = new SchedulingRankingAndCrowdingDistanceComparator<>();

        int populationSize = 300;
        algorithm =
                new HMODGWOBuilder<>(problem, crossover, mutation, populationSize)
                        .setSelectionOperator(selection)
                        .setMaxEvaluations(200)
                        .setDominanceComparator(comparator)
                        .setLocalSearchOperator(localSearch)
                        .build();

        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
        List<SchedulingSolution> population = algorithm.getResult();
        long computingTime = algorithmRunner.getComputingTime();

        JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");


        return printFinalSolutionSet(population, problem, nameMaps);
    }


    public static void main(String[] args) throws JMetalException, IOException {
        SchedulingProblem problem;
        Algorithm<List<SchedulingSolution>> algorithm;
        CrossoverOperator<SchedulingSolution> crossover;
        CrossoverOperator<SchedulingSolution> localSearch;
        MutationOperator<SchedulingSolution> mutation;
        SelectionOperator<List<SchedulingSolution>, SchedulingSolution> selection;
        Comparator<SchedulingSolution> comparator;

        ProblemInstance problemInstance = new ProblemInstance();
        problemInstance.readJsonFile("INPUT.json");
        problem = new SchedulingProblem(problemInstance.getProcessTimeTable().getVariables(), 3, 0);
        problem.loadInstance(problemInstance);

        double crossoverProbability = 0.9;
        crossover = new POXRPXCrossover(problem.getNumberOfVariables() / 2, 0.3, 0.6);

        localSearch = new LocalSearchCrossover<>(problem.getNumberOfVariables() / 2, 10,
                0.8, problemInstance.getProcessTime());

        double mutationProbability = 1.0 / problem.getNumberOfVariables();
        double mutationDistributionIndex = 20.0;
        mutation = new SchedulingMutation(0.5, problemInstance.getProcessTime());

        selection = new NaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());

        comparator = new SchedulingRankingAndCrowdingDistanceComparator<>();

        int populationSize = 300;
        algorithm =
                new HMODGWOBuilder<>(problem, crossover, mutation, populationSize)
                        .setSelectionOperator(selection)
                        .setMaxEvaluations(200)
                        .setDominanceComparator(comparator)
                        .setLocalSearchOperator(localSearch)
                        .build();

        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
        List<SchedulingSolution> population = algorithm.getResult();
        long computingTime = algorithmRunner.getComputingTime();

        JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

        printFinalSolutionSet(population);

        BufferedWriter bufferedWriter = new DefaultFileOutputContext("ALL.json").getFileWriter();

        for (SchedulingSolution solution : population) {
            bufferedWriter.write(SchedulingSolutionConverter.convertToJson(solution, problem, null));
            bufferedWriter.write("\n");
        }
        bufferedWriter.close();
    }

}
