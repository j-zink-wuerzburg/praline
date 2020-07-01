package de.uniwue.informatik.praline.layouting.layered.main;

import de.uniwue.informatik.praline.datastructure.graphs.Graph;
import de.uniwue.informatik.praline.datastructure.utils.Serialization;
import de.uniwue.informatik.praline.layouting.layered.algorithm.Sugiyama;
import de.uniwue.informatik.praline.layouting.layered.algorithm.crossingreduction.CrossingMinimizationMethod;
import de.uniwue.informatik.praline.layouting.layered.algorithm.edgeorienting.DirectionMethod;

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

public class Test3_visual {

    public static final String PATH_DATA_SET =
//            "Praline-Layouting/data/generated_2020-06-04_18-39-49";
//            "Praline-Layouting/data/lc-praline-package-2020-05-18";
            "Praline-Layouting/data/topology-zoo";


    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    public static final String PATH_RESULTS =
            "Praline-Layouting/results/all-svgs-" + DATE_FORMAT.format(new Date());

    private static final int NUMBER_OF_REPETITIONS_PER_GRAPH = 20;

    private static final int NUMBER_OF_CROSSING_REDUCTION_ITERATIONS = 10;



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
                if (child.getName().endsWith(".json")) {
                    files.add(child);
                }
            }
        }

        new File(PATH_RESULTS).mkdirs();

        List<Callable<String>> tasks = new ArrayList<>();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(16);
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
        Sugiyama bestRunSugy = null;

        for (int i = 0; i < NUMBER_OF_REPETITIONS_PER_GRAPH; i++) {
            Graph graph = Serialization.read(file, Graph.class);

            numberOfVertices = graph.getVertices().size();

            Sugiyama sugy = new Sugiyama(graph);

            sugy.construct();
            sugy.assignDirections(DirectionMethod.FORCE);
            sugy.assignLayers();
            sugy.createDummyNodes();
            sugy.crossingMinimization(CrossingMinimizationMethod.PORTS, NUMBER_OF_CROSSING_REDUCTION_ITERATIONS);

            int numberOfCrossings = sugy.getNumberOfCrossings();

            sugy.nodePositioning();
            sugy.edgeRouting();
            sugy.prepareDrawing();

            if (bestNumberOfCrossings > numberOfCrossings) {
                bestNumberOfCrossings = numberOfCrossings;
                bestRunSugy = sugy;
            }
        }


        String filename = file.getName();
        filename = filename.substring(0, filename.length() - 5); //remove ".json"
        filename = "n" + numberOfVertices + "cr" + bestNumberOfCrossings + filename + ".svg";
        bestRunSugy.drawResult(PATH_RESULTS + File.separator + filename);
        System.out.println("Progress: " + progress() + "/" + totalSteps);
    }
}
