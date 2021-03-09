package de.uniwue.informatik.praline.layouting.layered.kieleraccess;

import de.uniwue.informatik.praline.datastructure.graphs.Graph;
import de.uniwue.informatik.praline.datastructure.utils.Serialization;
import de.uniwue.informatik.praline.layouting.layered.algorithm.SugiyamaLayouter;
import de.uniwue.informatik.praline.layouting.layered.algorithm.crossingreduction.CrossingMinimizationMethod;
import de.uniwue.informatik.praline.layouting.layered.algorithm.edgeorienting.DirectionMethod;
import de.uniwue.informatik.praline.layouting.layered.algorithm.layerassignment.LayerAssignmentMethod;
import de.uniwue.informatik.praline.layouting.layered.main.util.BendsCounting;
import de.uniwue.informatik.praline.layouting.layered.main.util.CrossingsCounting;

import java.io.File;
import java.io.IOException;

public class MainDrawKielerPlan {

    private static final String SOURCE_PATH =
            "Praline-Layouting/data/generated_2020-08-20_04-42-39/praline-pseudo-plan-a7118c7293f015c8.json";

    public static final String TARGET_PATH = "Praline-Layouting/results/testKIELER.svg";

    private static final boolean CHECK_COMPLETENESS_OF_GRAPH = true;

    private static final DirectionMethod DIRECTION_METHOD = DirectionMethod.FORCE;

    private static final LayerAssignmentMethod LAYER_ASSIGNMENT_METHOD = LayerAssignmentMethod.NETWORK_SIMPLEX;

    private static final int NUMBER_OF_REPETITIONS_PER_GRAPH = 1; //5;

    private static final int NUMBER_OF_FORCE_DIRECTED_ITERATIONS = 1; //10;

    public static void main(String[] args) {
        KielerLayouter bestRun = null;
        int fewestCrossings = Integer.MAX_VALUE;

        for (int i = 0; i < NUMBER_OF_REPETITIONS_PER_GRAPH; i++) {
            File file = new File(SOURCE_PATH);
            Graph graph = null;
            try {
                graph = Serialization.read(file, Graph.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Read graph " + SOURCE_PATH);
            System.out.println();

            KielerLayouter kielerLayouter =
                    new KielerLayouter(graph, DIRECTION_METHOD, LAYER_ASSIGNMENT_METHOD,
                            NUMBER_OF_FORCE_DIRECTED_ITERATIONS);

            kielerLayouter.computeLayout();
            Graph resultGraph = kielerLayouter.getGraph();

            int crossings = CrossingsCounting.countNumberOfCrossings(resultGraph);
            System.out.println("Computed drawing with " + crossings + " crossings " +
                    "and " + BendsCounting.countNumberOfBends(resultGraph) + " bends.");
            System.out.println();

            if (crossings < fewestCrossings) {
                bestRun = kielerLayouter;
                fewestCrossings = crossings;
            }

            if (i == NUMBER_OF_REPETITIONS_PER_GRAPH - 1) {
                bestRun.generateSVG(TARGET_PATH);

                if (i > 1) {
                    System.out.println();
                    System.out.println("Best run had " + fewestCrossings + " crossings -> to be saved as svg");
                }
                System.out.println("Created svg " + TARGET_PATH);
                System.out.println();
            }

            if (CHECK_COMPLETENESS_OF_GRAPH) {
                Graph sameGraphReloaded = null;
                try {
                    sameGraphReloaded = Serialization.read(file, Graph.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!resultGraph.equalLabeling(sameGraphReloaded)) {
                    System.out.println("Warning! Drawn graph and input graph differ.");
                }
                else {
                    System.out.println("Checked: drawn graph contains the same objects as the input graph");
                }
            }
            System.out.println();
            System.out.println();
        }
    }
}
