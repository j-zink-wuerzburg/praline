package de.uniwue.informatik.praline.layouting.layered.kieleraccess;

import de.uniwue.informatik.praline.datastructure.graphs.Graph;
import de.uniwue.informatik.praline.datastructure.utils.Serialization;
import de.uniwue.informatik.praline.layouting.layered.algorithm.SugiyamaLayouter;
import de.uniwue.informatik.praline.layouting.layered.algorithm.crossingreduction.CrossingMinimizationMethod;
import de.uniwue.informatik.praline.layouting.layered.algorithm.edgeorienting.DirectionMethod;
import de.uniwue.informatik.praline.layouting.layered.algorithm.layerassignment.LayerAssignmentMethod;
import de.uniwue.informatik.praline.layouting.layered.main.util.CrossingsCounting;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MainDrawKielerPackage {

    public static final String PATH_DATA_SET =
//            "Praline-Layouting/data/generated_2020-06-04_18-39-49";
//            "Praline-Layouting/data/generated_2020-08-20_04-42-39";
//            "Praline-Layouting/data/lc-praline-package-2020-05-18";
//            "Praline-Layouting/data/praline-package-2020-05-18";
//            "Praline-Layouting/data/generated_2021-08-06_17-27-03"; //based on "praline-package-2020-05-18"
//            "Praline-Layouting/data/praline-readable-2020-09-04";
//            "Praline-Layouting/data/5plansOriginalPseudo";
//            "Praline-Layouting/data/denkbares_08_06_2021/praline";
            "Praline-Layouting/data/generated_2021-08-07_15-24-08"; //based on "denkbares_08_06_2021/praline"


    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    public static final String PATH_RESULTS =
            "Praline-Layouting/results/all-svgs-kieler-" + DATE_FORMAT.format(new Date());

    private static final boolean CHECK_COMPLETENESS_OF_GRAPH = true;

    private static final DirectionMethod DIRECTION_METHOD = DirectionMethod.FORCE;

    private static final LayerAssignmentMethod LAYER_ASSIGNMENT_METHOD = LayerAssignmentMethod.NETWORK_SIMPLEX;

    private static final int NUMBER_OF_REPETITIONS_PER_GRAPH = 10; //5;

    private static final int NUMBER_OF_FORCE_DIRECTED_ITERATIONS = 1; //10;

    private static final int NUMBER_OF_PARALLEL_THREADS = 8;


    private static int progressCounter = 0;
    private static int totalSteps;

    private static synchronized int progress() {
        return ++progressCounter;
    }

    public static void main(String[] args) throws InterruptedException {
        List<File> files = new LinkedList<>();

        File dir = new File(PATH_DATA_SET);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.getName().endsWith(".json") &&
                        (!PATH_DATA_SET.endsWith("readable-2020-09-04") || child.getName().endsWith("-praline.json"))) {
                    files.add(child);
                }
            }
        }

        new File(PATH_RESULTS).mkdirs();

        List<Callable<String>> tasks = new ArrayList<>();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUMBER_OF_PARALLEL_THREADS);
        int jj = 0;
        progressCounter = 0;
        totalSteps = files.size();
        for (File file : files) {
            int k = jj;
            tasks.add(() -> {
                try {
                    parallelio(file);
                }
                catch (Exception e) {
                    System.out.println("Exception has been thrown!");
                    e.printStackTrace();
                }
                return null;
            });
            jj++;
        }
        executor.invokeAll(tasks);
        executor.shutdown();
    }

    public static void parallelio (File file) throws IOException {
        int numberOfVertices = -1;
        int bestNumberOfCrossings = Integer.MAX_VALUE;
        KielerLayouter bestRun = null;

        for (int i = 0; i < NUMBER_OF_REPETITIONS_PER_GRAPH; i++) {
            Graph graph = Serialization.read(file, Graph.class);

            numberOfVertices = graph.getVertices().size();

            KielerLayouter kielerLayouter =
                    new KielerLayouter(graph, DIRECTION_METHOD, LAYER_ASSIGNMENT_METHOD,
                            NUMBER_OF_FORCE_DIRECTED_ITERATIONS);

            kielerLayouter.computeLayout();
            Graph resultGraph = kielerLayouter.getGraph();

            int numberOfCrossings = CrossingsCounting.countNumberOfCrossings(resultGraph);

            if (bestNumberOfCrossings > numberOfCrossings) {
                bestNumberOfCrossings = numberOfCrossings;
                bestRun = kielerLayouter;
            }
        }

        if (CHECK_COMPLETENESS_OF_GRAPH) {
            Graph sameGraphReloaded = null;
            try {
                sameGraphReloaded = Serialization.read(file, Graph.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!bestRun.getGraph().equalLabeling(sameGraphReloaded)) {
                System.out.println("Warning! Drawn graph and input graph differ."); //TODO: seems to happen sometimes
                // -> check!!
            }
        }

        String filename = file.getName();
        filename = filename.substring(0, filename.length() - 5); //remove ".json"
        filename = "n" + numberOfVertices + "cr" + bestNumberOfCrossings + filename + ".svg";
        bestRun.generateSVG(PATH_RESULTS + File.separator + filename);
        System.out.println("Progress: " + progress() + "/" + totalSteps);
    }
}
