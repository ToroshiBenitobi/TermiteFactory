package org.anar.scheduling.problem;

import org.anar.scheduling.converter.SchedulingSolutionConverter;
import org.anar.scheduling.object.Operation;
import org.anar.scheduling.object.ProblemInstance;
import org.anar.scheduling.object.ProcessTime;
import org.anar.scheduling.solution.SchedulingSolution;
import org.anar.scheduling.util.SchedulingUtil;
import org.uma.jmetal.problem.AbstractGenericProblem;

import java.io.IOException;
import java.util.*;

public class SchedulingProblem extends AbstractGenericProblem<SchedulingSolution> {

    private static final Double NaN = Double.NaN;
    int numberOfJobs;
    int[] numberOfOperations;
    int[] nextJob;
    int numberOfMachines;
    Double startTime;
    int startDay;
    ProcessTime processTime;
    Double[][] jobTransportationTime;
    Double[][][] processingIntervalConstraint; // second 0~86400
    Boolean[][][] processingDayConstraint;
    Boolean[][] isPCO;
    Double[] staticWaitingRate;
    Double[] dynamicProcessingRate;
    Double[] rawMaterialCost;
    Double transportationRate;

    public SchedulingProblem(int numberOfVariables,
                             int numberOfObjectives,
                             int numberOfConstraints) {
        setName("Scheduling Problem");
        setNumberOfVariables(numberOfVariables);
        setNumberOfConstraints(numberOfConstraints);
        setNumberOfObjectives(numberOfObjectives);
    }

    public void loadInstance(ProblemInstance problem) {
        setStartTime(problem.getStartTime());
        setStartDay(problem.getStartDay());
        setProcessTime(problem.getProcessTime());
        setNumberOfJobs(processTime.getNumberOfJobs());
        setNumberOfOperations(problem.getNumberOfOperations());
        setNextJob(problem.getNextJob());
        setNumberOfMachines(problem.getJobTransportationTime().length);
        setJobTransportationTime(problem.getJobTransportationTime());
        setProcessingIntervalConstraint(problem.getProcessingIntervalConstraint());
        setProcessingDayConstraint(problem.getProcessingDayConstraint());
        setIsPCO(problem.getIsPCO());
        setStaticWaitingRate(problem.getStaticWaitingRate());
        setDynamicProcessingRate(problem.getDynamicProcessingRate());
        setRawMaterialCost(problem.getRawMaterialCost());
        setTransportationRate(problem.getTransportationRate());
    }

    public void setNumberOfJobs(int numberOfJobs) {
        this.numberOfJobs = numberOfJobs;
    }

    public void setNumberOfOperations(int[] numberOfOperations) {
        this.numberOfOperations = numberOfOperations;
    }

    public void setNumberOfMachines(int numberOfMachines) {
        this.numberOfMachines = numberOfMachines;
    }

    public void setProcessTime(ProcessTime processTime) {
        this.processTime = processTime;
    }

    public void setProcessTime(List<Map<Integer, Double>>[] processTime) {
        this.processTime = new ProcessTime(processTime);
    }

    public void setJobTransportationTime(Double[][] jobTransportationTime) {
        this.jobTransportationTime = jobTransportationTime;
    }

    public void setProcessingIntervalConstraint(Double[][][] processingIntervalConstraint) {
        this.processingIntervalConstraint = processingIntervalConstraint;
    }

    public void setIsPCO(Boolean[][] isPCO) {
        this.isPCO = isPCO;
    }

    public Double getStartTime() {
        return startTime;
    }

    public void setStartTime(Double startTime) {
        this.startTime = startTime;
    }

    public int getStartDay() {
        return startDay;
    }

    public void setStartDay(int startDay) {
        this.startDay = startDay;
    }

    public int getNumberOfJobs() {
        return numberOfJobs;
    }

    public int[] getNumberOfOperations() {
        return numberOfOperations;
    }

    public int getNumberOfMachines() {
        return numberOfMachines;
    }

    public ProcessTime getProcessTime() {
        return processTime;
    }

    public Double[][] getJobTransportationTime() {
        return jobTransportationTime;
    }

    public Double[][][] getProcessingIntervalConstraint() {
        return processingIntervalConstraint;
    }

    public Boolean[][] getIsPCO() {
        return isPCO;
    }

    public Double[] getDynamicProcessingRate() {
        return dynamicProcessingRate;
    }

    public void setDynamicProcessingRate(Double[] dynamicProcessingRate) {
        this.dynamicProcessingRate = dynamicProcessingRate;
    }

    public Double[] getStaticWaitingRate() {
        return staticWaitingRate;
    }

    public void setStaticWaitingRate(Double[] staticWaitingRate) {
        this.staticWaitingRate = staticWaitingRate;
    }

    public Boolean[][][] getProcessingDayConstraint() {
        return processingDayConstraint;
    }

    public void setProcessingDayConstraint(Boolean[][][] processingDayConstraint) {
        this.processingDayConstraint = processingDayConstraint;
    }

    public Double[] getRawMaterialCost() {
        return rawMaterialCost;
    }

    public void setRawMaterialCost(Double[] rawMaterialCost) {
        this.rawMaterialCost = rawMaterialCost;
    }

    public Double getTransportationRate() {
        return transportationRate;
    }

    public void setTransportationRate(Double transportationRate) {
        this.transportationRate = transportationRate;
    }

    public int[] getNextJob() {
        return nextJob;
    }

    public void setNextJob(int[] nextJob) {
        this.nextJob = nextJob;
    }

    @Override
    public SchedulingSolution evaluate(SchedulingSolution solution) {
        List<Integer>[] allocation = SchedulingSolutionConverter.decode(solution, processTime, numberOfMachines);
        List<Operation>[] machineAllocation =
                SchedulingSolutionConverter.decodeToMachineAllocation(solution, processTime, numberOfMachines);
        List<Double>[] complicationTime = new List[solution.jobs().length];
        for (int i = 0; i < complicationTime.length; i++) {
            complicationTime[i] = new ArrayList<>();
        }
        List<Double>[] delays = new List[solution.jobs().length];
        for (int i = 0; i < delays.length; i++) {
            delays[i] = new ArrayList<>();
            delays[i].add(0.0);
        }
        double[] jobStartTime = new double[numberOfJobs];
        computeComplicationTime(complicationTime, jobStartTime, delays, solution, allocation, machineAllocation);
        solution.objectives()[0] = evaluateMakeSpan(solution, allocation, machineAllocation, jobStartTime, delays);
        solution.objectives()[1] = evaluateProductionCost(solution, allocation, machineAllocation, jobStartTime, delays);
        solution.objectives()[2] = evaluateAverageIdlingRate(solution, allocation, machineAllocation, jobStartTime, delays, complicationTime);
        return solution;
    }

    private double evaluateMakeSpan(SchedulingSolution solution,
                                    List<Integer>[] allocation,
                                    List<Operation>[] machineAllocation,
                                    double[] jobStartTime,
                                    List<Double>[] delays) {
        double maxCompletionTime = 0;
        for (int i = 0; i < numberOfJobs; i++) {
            double PS = 0;
            double TS = 0;
            double DS = 0;

            for (int j = 0; j < allocation[i].size(); j++) {
                PS += processTime.getProcessTime(i, j, allocation[i].get(j));
            }

            for (int j = 0; j < allocation[i].size() - 1; j++) {
                TS += jobTransportationTime[allocation[i].get(j)][allocation[i].get(j + 1)];
            }

            for (int j = 1; j < allocation[i].size(); j++) {
                DS += delays[i].get(j);
            }
            double S = jobStartTime[i] + PS + TS + DS;
            if (S > maxCompletionTime) maxCompletionTime = S;
        }

        return maxCompletionTime;
    }

    private double evaluateProductionCost(SchedulingSolution solution,
                                          List<Integer>[] allocation,
                                          List<Operation>[] machineAllocation,
                                          double[] jobStartTime,
                                          List<Double>[] delays) {
        double totalTime = solution.objectives()[0] - startTime;
        double totalCost = 0;
        for (int i = 0; i < machineAllocation.length; i++) {
            double dynamicTime = 0;
            for (Operation operation : machineAllocation[i]) {
                dynamicTime += this.processTime.getProcessTime(operation.getJob(), operation.getNumber(), i);
            }
            double staticTime = totalTime - dynamicTime;
            totalCost += staticTime * staticWaitingRate[i] + dynamicTime * dynamicProcessingRate[i];
        }

        for (Double cost : rawMaterialCost) {
            totalCost += cost;
        }

        double transportationTime = 0;
        for (int i = 0; i < numberOfJobs; i++) {
            for (int j = 0; j < allocation[i].size() - 1; j++) {
                transportationTime += jobTransportationTime[allocation[i].get(j)][allocation[i].get(j + 1)];
            }
        }
        totalCost += transportationTime * transportationRate;

        return totalCost;
    }

    private double evaluateDelayTime(SchedulingSolution solution,
                                     List<Integer>[] allocation,
                                     List<Operation>[] machineAllocation,
                                     double[] jobStartTime,
                                     List<Double>[] delays) {
        double totalDelayTime = 0;
        for (int i = 0; i < numberOfJobs; i++) {
            for (int j = 1; j < allocation[i].size(); j++) {
                totalDelayTime += delays[i].get(j);
            }
        }
        return totalDelayTime;
    }

    private double evaluateAverageIdlingRate(SchedulingSolution solution,
                                             List<Integer>[] allocation,
                                             List<Operation>[] machineAllocation,
                                             double[] jobStartTime,
                                             List<Double>[] delays,
                                             List<Double>[] complicationTime
    ) {
        double[] rate = new double[numberOfMachines];
        for (int i = 0; i < numberOfMachines; i++) {
            if (machineAllocation[i].size() == 0) {
                rate[i] = 0;
                continue;
            }
            Operation lastOperation = machineAllocation[i].get(machineAllocation[i].size() - 1);
            Operation firstOperation = machineAllocation[i].get(0);
            double totalTime;
            // PCD
            boolean isPCD = true;
            for (int j = 0; j < machineAllocation[i].size(); j++) {
                isPCD &= isPCO[machineAllocation[i].get(j).getJob()][machineAllocation[i].get(j).getNumber()];
            }

            if (isPCD) {
                double finishTime =
                        complicationTime[lastOperation.getJob()].get(lastOperation.getNumber());
                List<Double> pcd0 = new ArrayList<>();
                List<Double> pcd1 = new ArrayList<>();
                for (int j = 0; j < machineAllocation[i].size(); j++) {
                    double pcd0i = processingIntervalConstraint[machineAllocation[i].get(j).getJob()][machineAllocation[i].get(j).getNumber()][0];
                    double pcd1i = processingIntervalConstraint[machineAllocation[i].get(j).getJob()][machineAllocation[i].get(j).getNumber()][1];
                    int day = startDay;
                    while (pcd0i < finishTime) {
                        if (processingDayConstraint[machineAllocation[i].get(j).getJob()][machineAllocation[i].get(j).getNumber()][day]) {
                            pcd0.add(pcd0i);
                            pcd1.add(pcd1i);
                        }
                        pcd0i += 86400;
                        pcd1i += 86400;
                        day = (day + 1) % 7;
                    }
                }
                totalTime = SchedulingUtil.computePCDTotalTime(pcd0, pcd1);
                if (totalTime == 0)
                    System.out.println(totalTime);
            } else {
                totalTime =
                        complicationTime[lastOperation.getJob()].get(lastOperation.getNumber())
                                - (complicationTime[firstOperation.getJob()].get(firstOperation.getNumber())
                                - processTime.getProcessTime(firstOperation.getJob(), firstOperation.getNumber(), i));
            }

            double processTime = 0;
            for (Operation operation : machineAllocation[i]) {
                processTime += this.processTime.getProcessTime(operation.getJob(), operation.getNumber(), i);
            }
            rate[i] = 1 - processTime / totalTime;
        }
        double sum = 0;
        for (double v : rate) {
            sum += v;
        }
        return sum / numberOfMachines;
    }

    public void computeComplicationTime(List<Double>[] complicationTime,
                                        double[] jobStartTime,
                                        List<Double>[] delays,
                                        SchedulingSolution solution,
                                        List<Integer>[] allocation,
                                        List<Operation>[] machineAllocation) {
        int half = solution.variables().size() / 2;
        int[] counter = new int[solution.jobs().length];
        boolean isComplete = false;
        boolean[] jobComplete = new boolean[solution.jobs().length];
        boolean[] jobStart = new boolean[solution.jobs().length];
        for (boolean b : jobComplete) {
            b = false;
        }
        for (boolean b : jobStart) {
            b = false;
        }

        while (!isComplete) {
            for (int i = 0; i < half; i++) {
                int job = solution.variables().get(i);

                if (jobComplete[job]) continue;

                int number = counter[job];
                int machine = allocation[job].get(counter[job]);

                double startTime = this.startTime;
                boolean continueFlag = false;
                for (int j = 0; j < numberOfJobs; j++) {
                    if (nextJob[j] == job) {
                        if (!jobComplete[j]) {
                            continueFlag = true;
                            break;
                        } else {
                            int machine1 = allocation[j].get(numberOfOperations[j] - 1);
                            double deliveredTime = complicationTime[j].get(numberOfOperations[j] - 1) + jobTransportationTime[machine1][machine];
                            if (deliveredTime > startTime) startTime = deliveredTime;
                        }
                    }
                }
                if (continueFlag) continue;

                double st = startTime;
                double ct;
                double nst;
                double pic0 = processingIntervalConstraint[job][number][0];
                double pic1 = processingIntervalConstraint[job][number][1];
                int day = startDay;
                double pt = processTime.getProcessTime(job, number, machine);
                if (machineAllocation[machine].indexOf(new Operation(job, number)) == 0) {
                    if (number > 0) {
                        if (!isPCO[job][number]) {
                            st = complicationTime[job].get(number - 1) + jobTransportationTime[allocation[job].get(counter[job] - 1)][machine];
                        } else {
                            nst = complicationTime[job].get(number - 1) + jobTransportationTime[allocation[job].get(counter[job] - 1)][machine];
                            while (pic0 < nst) {
                                pic0 += 86400;
                                pic1 += 86400;
                                day = (day + 1) % 7;
                            }
                            if (nst >= pic0 - 86400 && nst + pt < pic1 - 86400) {
                                day = day == 0 ? 6 : day - 1;
                                if (processingDayConstraint[job][number][day]) {
                                    st = nst;
                                } else {
                                    while (!processingDayConstraint[job][number][day]) {
                                        pic0 += 86400;
                                        pic1 += 86400;
                                        day = (day + 1) % 7;
                                    }
                                    st = pic0;
                                }
                            } else {
                                while (!processingDayConstraint[job][number][day]) {
                                    pic0 += 86400;
                                    pic1 += 86400;
                                    day = (day + 1) % 7;
                                }
                                st = pic0;
                            }
                        }
                    } else {
                        if (isPCO[job][number]) {
                            nst = startTime;
                            while (pic0 < nst) {
                                pic0 += 86400;
                                pic1 += 86400;
                                day = (day + 1) % 7;
                            }
                            if (nst >= pic0 - 86400 && nst + pt < pic1 - 86400) {
                                day = day == 0 ? 6 : day - 1;
                                if (processingDayConstraint[job][number][day]) {
                                    st = nst;
                                } else {
                                    while (!processingDayConstraint[job][number][day]) {
                                        pic0 += 86400;
                                        pic1 += 86400;
                                        day = (day + 1) % 7;
                                    }
                                    st = pic0;
                                }
                            } else {
                                while (!processingDayConstraint[job][number][day]) {
                                    pic0 += 86400;
                                    pic1 += 86400;
                                    day = (day + 1) % 7;
                                }
                                st = pic0;
                            }
                        }

                    }
                } else {
                    Operation p = machineAllocation[machine].get(
                            machineAllocation[machine].indexOf(new Operation(job, number)) - 1);

                    int ip = p.getJob();
                    int jp = p.getNumber();
                    double post = number > 0 ? Math.max(complicationTime[job].get(counter[job] - 1)
                            + jobTransportationTime[allocation[job].get(counter[job] - 1)][machine], startTime) : startTime;

                    // if pre not complete
                    if (complicationTime[ip].size() < jp + 1) continue;
                    nst = Math.max(complicationTime[ip].get(jp), post);
                    if (!isPCO[job][number]) {
                        st = nst;
                    } else {
                        while (pic0 < nst) {
                            pic0 += 86400;
                            pic1 += 86400;
                            day = (day + 1) % 7;
                        }
                        if (nst >= pic0 - 86400 && nst + pt < pic1 - 86400) {
                            day = day == 0 ? 6 : day - 1;
                            if (processingDayConstraint[job][number][day]) {
                                st = nst;
                            } else {
                                while (!processingDayConstraint[job][number][day]) {
                                    pic0 += 86400;
                                    pic1 += 86400;
                                    day = (day + 1) % 7;
                                }
                                st = pic0;
                            }
                        } else {
                            while (!processingDayConstraint[job][number][day]) {
                                pic0 += 86400;
                                pic1 += 86400;
                                day = (day + 1) % 7;
                            }
                            st = pic0;
                        }
                    }
                }
                ct = st + pt;
                complicationTime[job].add(ct);
                if (number == 0)                         // start job
                    jobStart[job] = true;

                if (number > 0) {
                    double delay = delay(job, counter[job], machine, allocation, machineAllocation, complicationTime);
                    delays[job].add(delay);
                } else {
                    jobStartTime[job] = st;
                }

                ++counter[job];

                if (counter[job] == numberOfOperations[job]) {
                    jobComplete[job] = true;
                }
                isComplete = true;
                for (boolean b : jobComplete) {
                    isComplete &= b;
                }
            }
        }


    }

    private double delay(int job, int number, int machine,
                         List<Integer>[] allocation,
                         List<Operation>[] machineAllocation,
                         List<Double>[] complicationTime) {

        double startTime = complicationTime[job].get(number) - processTime.getProcessTime(job, number, machine);
        double preComplicationTime = complicationTime[job].get(number - 1);
        double transportTime = jobTransportationTime[allocation[job].get(number - 1)][machine];
        double delay = startTime - preComplicationTime - transportTime;
        return delay;
//        if (machineAllocation[machine].get(0).getJob() == job &&
//                machineAllocation[machine].get(0).getNumber() == number
//        ) {
//            if (number > 0 && isPCO[job][number]) {
//                double st;
//                double nst;
//                st = complicationTime[job].get(number - 1) + jobTransportationTime[allocation[job].get(number - 1)][machine];
//                if (st <= processingIntervalConstraint[job][number][0]) {
//                    nst = processingIntervalConstraint[job][number][0];
//                } else if (processingIntervalConstraint[job][number][1] < st) {
//                    nst = processingIntervalConstraint[job][number][0] + 86400;
//                } else {
//                    nst = st;
//                }
//                delay = nst - st;
//            } else {
//                delay = 0;
//            }
//        } else if (!isPCO[job][number]) {
//            Operation p = machineAllocation[machine].get(
//                    machineAllocation[machine].indexOf(new Operation(job, number)) - 1);
//            int ip = p.getJob();
//            int jp = p.getNumber();
//
////            double cmp = complicationTime[ip].get(jp);
////            double cjp = complicationTime[job].get(number - 1);
////            double jtt = jobTransportationTime[allocation[job].get(number - 1)][machine];
//
//            if (complicationTime[ip].get(jp) <= complicationTime[job].get(number - 1)
//                    + jobTransportationTime[allocation[job].get(number - 1)][machine]) {
//                delay = 0;
//            } else {
//                delay = complicationTime[ip].get(jp) -
//                        complicationTime[job].get(number - 1) -
//                        jobTransportationTime[allocation[job].get(number - 1)][machine];
//            }
//        } else {
//            Operation p = machineAllocation[machine].get(
//                    machineAllocation[machine].indexOf(new Operation(job, number)) - 1);
//
//            int ip = p.getJob();
//            int jp = p.getNumber();
//            double nst = Math.max(complicationTime[ip].get(jp), complicationTime[job].get(number - 1)
//                    + jobTransportationTime[allocation[job].get(number - 1)][machine]);
//
//            if (nst <= processingIntervalConstraint[job][number][0]) {
//                delay = processingIntervalConstraint[job][number][0] - complicationTime[job].get(number - 1)
//                        - jobTransportationTime[allocation[job].get(number - 1)][machine];
//            } else if (processingIntervalConstraint[job][number][0] < complicationTime[ip].get(jp) &&
//                    complicationTime[ip].get(jp) < processingIntervalConstraint[job][number][1] &&
//                    complicationTime[ip].get(jp) >= complicationTime[job].get(number - 1)
//                            + jobTransportationTime[allocation[job].get(number - 1)][machine]) {
//                delay = complicationTime[ip].get(jp) -
//                        complicationTime[job].get(number - 1) -
//                        jobTransportationTime[allocation[job].get(number - 1)][machine];
//            } else if (processingIntervalConstraint[job][number][0] < complicationTime[job].get(number - 1)
//                    + jobTransportationTime[allocation[job].get(number - 1)][machine] &&
//                    complicationTime[job].get(number - 1) + jobTransportationTime[allocation[job].get(number - 1)][machine] < processingIntervalConstraint[job][number][1] &&
//                    complicationTime[ip].get(jp) >= complicationTime[job].get(number - 1)
//                            + jobTransportationTime[allocation[job].get(number - 1)][machine]) {
//                delay = 0;
//            } else {
//                delay = processingIntervalConstraint[job][number][0] + 86400
//                        - complicationTime[job].get(number - 1)
//                        - jobTransportationTime[allocation[job].get(number - 1)][machine];
//            }
//        }
    }

    @Override
    public SchedulingSolution createSolution() {
        SchedulingSolution solution = new SchedulingSolution(getNumberOfVariables(), getNumberOfObjectives(), getNumberOfConstraints(), getNumberOfJobs());
        solution.randomInit(processTime);
        return solution;
    }

    public static void main(String[] args) {


        ProblemInstance problemInstance = new ProblemInstance();
        try {
            problemInstance.readJsonFile("INPUT.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
        SchedulingProblem
                problem = new SchedulingProblem(problemInstance.getProcessTimeTable().getVariables(), 1, 0);
        problem.loadInstance(problemInstance);

        SchedulingSolution solution = new SchedulingSolution(problemInstance.getProcessTimeTable().getVariables(), 1, 0, 7);
        solution.variables().clear();
        solution.variables().addAll(List.of(1, 2, 6, 1, 4, 2, 3, 5, 4, 6, 0, 3, 2, 5, 5, 6, 1, 3, 6, 4, 0, 0, 5, 1, 0, 1, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 2, 1, 0));
        problem.evaluate(solution);
        System.out.println(solution.objectives()[0]);
//
//        System.out.println(SchedulingSolutionConverter.convertToJson(solution, processTimeTable, jobTransportationTime, processingIntervalConstraint, isPCO));

    }
}
