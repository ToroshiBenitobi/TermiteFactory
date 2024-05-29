package org.anar.scheduling.util;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.fileoutput.FileOutputContext;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

public class SchedulingSolutionListOutput extends SolutionListOutput {
    public SchedulingSolutionListOutput(List<? extends Solution<?>> solutionList) {
        super(solutionList);
    }

}
