package de.uniwue.informatik.jung.layouting.forcedirectedwspd.main.util;

import de.uniwue.informatik.jung.layouting.forcedirectedwspd.main.TestConfig;
import de.uniwue.informatik.jung.layouting.forcedirectedwspd.main.csv.CSVSpecification;
import de.uniwue.informatik.jung.layouting.forcedirectedwspd.main.objectManager.GraphReference;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class FileTranslation {
	
	
	/**
	 * Translates all .json files generated by the older German version
	 * to the current English version (make them compatible).
	 * <br>
	 * These files are translated (only content, no file-name but German file-names are kept compatible):
	 * <ul>
	 *  <li>
	 *   {@link ComputationMetaData} (Berechnungsmetadaten)
	 *  </li>
	 *  <li>
	 *   {@link ComputationData} (Berechnungswerte)
	 *  </li>
	 *  <li>
	 *   {@link GraphReference} (Graphreferenz)
	 *  </li>
	 *  <li>
	 *   {@link TestConfig} (TestConfig)
	 *  </li>
	 *  <li>
	 *   {@link CSVSpecification} (CSVSpezifizierung)
	 *  </li>
	 * </ul>
	 * 
	 * @param paths
	 * All files and directories specified here are gone through and
	 * special .json-files are translated.
	 * All files in all specified directories and in all subdirectories
	 * of them are checked.
	 */
	public static void translateGerman2EnglishJsonFiles(String[] paths){
		for(String path: paths){
			File file = new File(path);
			File[] files = {file};
			translateGerman2EnglishJsonFiles(files);
		}
	}
		
	public static void translateGerman2EnglishJsonFiles(File[] files){
		translateJsonFiles(files, true, null, null);
	}
	
	public static void adjustPathsInCSVSpecifictationJsonFiles(String oldPathToComputationData, String newPathToComputationData){
		File file = new File(newPathToComputationData);
		File[] files = {file};
		translateJsonFiles(files, false, oldPathToComputationData, newPathToComputationData);
	}
	
	private static void translateJsonFiles(File[] files, boolean trueGerman2EnglishFalseCSVSpecification,
			String oldPathToComputationDataIfCSVSpecification, String newPathToComputationDataIfCSVSpecification){
		for(File fileOrDir: files){
			//dir
			if(fileOrDir.isDirectory()){
				File[] subFilesOrDirs = fileOrDir.listFiles();
				translateJsonFiles(subFilesOrDirs, trueGerman2EnglishFalseCSVSpecification,
						oldPathToComputationDataIfCSVSpecification, newPathToComputationDataIfCSVSpecification);
			}
			//file
			else{
				if(trueGerman2EnglishFalseCSVSpecification && ( fileOrDir.getName().endsWith(".json")
						|| fileOrDir.getName().endsWith(".Json") || fileOrDir.getName().endsWith(".JSON") ) ){
					try {
						readAndTranslate(fileOrDir, trueGerman2EnglishFalseCSVSpecification, oldPathToComputationDataIfCSVSpecification,
								newPathToComputationDataIfCSVSpecification);
						System.out.println(fileOrDir.getPath()+" translated.");
					} catch (IOException e) {
						System.out.println(fileOrDir.getPath()+" could not be translated!");
					}
				}
				else if(!trueGerman2EnglishFalseCSVSpecification && (fileOrDir.getName().endsWith("CSVSpezifizierung.json")
						|| fileOrDir.getName().endsWith(CSVSpecification.class.getSimpleName()+".json"))){
					try {
						readAndTranslate(fileOrDir, trueGerman2EnglishFalseCSVSpecification, oldPathToComputationDataIfCSVSpecification,
								newPathToComputationDataIfCSVSpecification);
						System.out.println(fileOrDir.getPath()+" paths adjusted.");
					} catch (IOException e) {
						System.out.println(fileOrDir.getPath()+" could not be adjusted!");
					}
				}
			}
		}
	}
	
	private static void readAndTranslate(File file, boolean trueGerman2EnglishFalseCSVSpecification,
			String oldPathToComputationDataIfCSVSpecification, String newPathToComputationDataIfCSVSpecification) throws IOException{
		FileReader fr = new FileReader(file);
		Scanner sc = new Scanner(fr);
		StringBuilder sb = new StringBuilder();
		
		while(sc.hasNext()){
			String currentLine = sc.nextLine();
			currentLine = checkAndTranslateLine(file.getName(), currentLine, trueGerman2EnglishFalseCSVSpecification,
					oldPathToComputationDataIfCSVSpecification, newPathToComputationDataIfCSVSpecification);
			sb.append(currentLine);
			sb.append(System.lineSeparator());
		}
		sc.close();
		
		FileWriter fw = new FileWriter(file);
		fw.write(sb.toString());
		fw.close();
	}
	
	private static String checkAndTranslateLine(String filename, String line, boolean trueGerman2EnglishFalseCSVSpecification,
			String oldPathToComputationDataIfCSVSpecification, String newPathToComputationDataIfCSVSpecification){
		
		//change paths within csv-specification
		if(!trueGerman2EnglishFalseCSVSpecification){
			line = line.replaceAll(oldPathToComputationDataIfCSVSpecification, newPathToComputationDataIfCSVSpecification);
		}
		//translate json-files from german to english
		else{
				
			if(filename.endsWith("berechnungsmetadaten.json")){
	
				line = line.replaceAll("klassenName", "className");
				line = line.replaceAll("layoutAlgorithmen", "layoutAlgorithms");
				line = line.replaceAll("algorithmen", "algorithms"); //must be before the next otherwise overwriting
				//Reihenfolge der Algorithmen so gewaehlt, dass es nicht zu Ueberschreibungen kommt
				line = line.replaceAll("FRLayoutMitQuadtree", "FRQuadtree");
				line = line.replaceAll("FRLayoutMitWSPDs_s", "FRWSPDb_b");
				line = line.replaceAll("FRLayoutMitWSPDp_s", "FRWSPDp_b");
				line = line.replaceAll("FRLayoutOhneMapsOhneRahmenImmerRep", "FRLayoutNoMapsNoFrameAlwaysRep");
				line = line.replaceAll("FRLayoutOhneMapsOhneRahmenGridVariante", "FRGrid");
				line = line.replaceAll("FRLayoutOhneMapsOhneRahmen", "FRLayoutNoMapsNoFrame");
				line = line.replaceAll("FRLayoutOhneMaps", "FRLayoutNoMaps");
				line = line.replaceAll("cPlusPlusSchnittstelle", "cPlusPlus");
//				line = line.replaceAll("CPlusPlusExternLayout", "CPlusPlusExternLayout"); //is the same
				line = line.replaceAll("algorithmustyp", "algorithmType");	
				line = line.replaceAll("MIT_WSPD", "WITH_WSPD");	
				line = line.replaceAll("MIT_QUADTREE", "WITH_QUADTREE");	
				line = line.replaceAll("SONSTIGER", "OTHER");	
//				line = line.replaceAll("EXTERN_C_PLUS_PLUS", "EXTERN_C_PLUS_PLUS"); //is the same
				line = line.replaceAll("namenZuSplitTreeUndWSPDNeukonstruktionsFunktionen",
						"namesOfTheRecomputationOfSplitTreeAndWSPDFunctions");
				line = line.replaceAll("linearesWachstumDesS", "linearGrowthOfS");
//				line = line.replaceAll("sMin", "sMin"); //same
				line = line.replaceAll("sSchrittweiteOderBasisBeiExpSWachstum",
						"sStepWidthOrBaseForExpGrowth");
				line = line.replaceAll("anzahlSWerte", "numberOfDifferentSValues");
				line = line.replaceAll("linearesWachstumDesThetas", "linearGrowthOfTheta");
//				line = line.replaceAll("thetaMin", "thetaMin"); //same
				line = line.replaceAll("thetaSchrittweiteOderBasisBeiExpThetaWachstum",
						"thetaStepWidthOrBaseForExpGrowth");
				line = line.replaceAll("anzahlThetaWerte", "numberOfDifferentThetaValues");
				line = line.replaceAll("anzahlSelbeTests", "numberOfSameTest");
				
			}
			else if(filename.endsWith("CSVSpezifizierung.json")){
	
				line = line.replaceAll("graphenGruppenDimension", "graphGroupDimension");
				line = line.replaceAll("graphenGruppen", "graphGroups");
				line = line.replaceAll("pfadFuerBerechnungswerteFuerDiesenGraph",
						"pathToTheCompuationDataForThisGraph");
				line = line.replaceAll("methodeDerZusammenfassung", "methodOfCombining");
				line = line.replaceAll("nameDerZeileOderSpalte", "nameOfTheRowOrColumn");
				line = line.replaceAll("KLEINSTER_WERT", "SMALLEST_VALUE");
				line = line.replaceAll("GROESSTER_WERT", "LARGEST_VALUE");
				line = line.replaceAll("SUMME", "SUM");
				line = line.replaceAll("MITTELWERT", "MEAN");
//				line = line.replaceAll("MEDIAN", "MEDIAN"); //same
				line = line.replaceAll("VARIANZ", "VARIANCE");
				line = line.replaceAll("STANDARDABWEICHUNG", "STANDARD_DEVIATION");
				line = line.replaceAll("ZEILENBESCHREIBUNG", "ROW_DESCRIPTION");
				line = line.replaceAll("SPALTENBESCHREIBUNG_TEIL1", "COLUMN_DESCRIPTION_PART1");
				line = line.replaceAll("SPALTENBESCHREIBUNG_TEIL2", "COLUMN_DESCRIPTION_PART2");
				line = line.replaceAll("algorithmusGruppenDimension", "algorithmGroupDimension");
				line = line.replaceAll("algorithmusGruppen", "algorithmGroups");
				line = line.replaceAll("zusammengefassteAlgorithmusspezifizierungen", "combinedAlgorithmSpecifications");
				line = line.replaceAll("indexAlgorithmus", "indexAlgorithm");
				line = line.replaceAll("indexNeukonstruktionsfunktion", "indexRecomputationFunction");
				line = line.replaceAll("indexSOderTheta", "indexSOrTheta");
				line = line.replaceAll("indexDurchlauf", "indexRun");
				line = line.replaceAll("laufzeitUndBewertungskriterienDimension",
						"runningTimeOrQualityCriterionsDimension"); //must be before the next otherwise overwriting
				line = line.replaceAll("laufzeitUndBewertungskriterien", "runningTimeOrQualityCriterions");
				line = line.replaceAll("methodenAufruf", "evaluationCriterionSelection");
				line = line.replaceAll("CPU_ZEIT_IN_NS", "CPU_TIME_IN_NS");
				line = line.replaceAll("ANZAHL_DER_KREUZUNGEN", "NUMBER_OF_CROSSINGS");
				line = line.replaceAll("QUALITAETSEIGENSCHAFTEN", "QUALITY_CRITERIONS");
				line = line.replaceAll("KLEINSTERWINKEL", "SMALLEST_ANGLE");
				line = line.replaceAll("ANZAHL_DER_ITERATIONEN", "NUMBER_OF_ITERATIONS");
				line = line.replaceAll("bewertungskriterium", "qualityCriterion");
				line = line.replaceAll("KANTENLAENGE", "EDGE_LENGTH");
				line = line.replaceAll("GEOMETRISCHER_KNOTENABSTAND_IM_VERHAELTNIS_ZU_KUERZESTEM_PFAD",
						"RATIO_GEOMETRIC_VERTEX_DISTANCE_TO_SHORTEST_PATH"); //must be before the next otherwise overwriting
				line = line.replaceAll("KNOTENABSTAND", "DISTANCE_VERTEX_VERTEX");
				line = line.replaceAll("ABSTAND_KNOTEN_NICHT_INZIDENTE_KANTEN", "DISTANCE_VERTEX_NOT_INCIDENT_EDGE");
				line = line.replaceAll("ABWEICHUNG_VOM_OPTIMALEN_WINKEL", "DEVIATION_FROM_THE_OPTIMAL_ANGLE");
				line = line.replaceAll("statistischeGroesse", "statistic");
				line = line.replaceAll("normierungImCSV", "normalizationInCSV");
				line = line.replaceAll("normierungNach", "normalizationBy");
				line = line.replaceAll("KEINE_NORMIERUNG", "NO_NORMALIZATION");
				line = line.replaceAll("SPALTENNORMIERUNG", "COLUMN_NORMALIZATION");
				line = line.replaceAll("ZEILENNORMIERUNG", "ROW_NORMALIZATION");
				line = line.replaceAll("nummerierungsSpalte", "numberingColumn");
				
			}
			else if(filename.startsWith("config")){
				
//				line = line.replaceAll("mode", "mode"); //same
				line = line.replaceAll("ausgewaehlteGraphen", "selectedGraphs");
				line = line.replaceAll("ausgewaehlteAlgorithmen", "selectedAlgorithms");
				line = line.replaceAll("layoutAlgorithmen", "layoutAlgorithms");
				//order here is chosen so, that there are no overwritings
				line = line.replaceAll("FRLayoutMitQuadtree", "FRQuadtree");
				line = line.replaceAll("FRLayoutMitWSPDs_s", "FRWSPDb_b");
				line = line.replaceAll("FRLayoutMitWSPDp_s", "FRWSPDp_b");
				line = line.replaceAll("FRLayoutOhneMapsOhneRahmenImmerRep", "FRLayoutNoMapsNoFrameAlwaysRep");
				line = line.replaceAll("FRLayoutOhneMapsOhneRahmenGridVariante", "FRGrid");
				line = line.replaceAll("FRLayoutOhneMapsOhneRahmen", "FRLayoutNoMapsNoFrame");
				line = line.replaceAll("FRLayoutOhneMaps", "FRLayoutNoMaps");
				line = line.replaceAll("externeCPlusPlusAlgPfade", "externCPlusPlusAlgPaths");
				line = line.replaceAll("ausgewaehltesSWachstum", "selectedSGrowth");
				line = line.replaceAll("kleinstesS", "smallestS");
				line = line.replaceAll("vergroesserungswertDesS", "increasingValueOfS");
				line = line.replaceAll("anzahlDerZuVerwendendenSWerte", "numberOfSValues");
				line = line.replaceAll("ausgewaehlteFunktionen", "selectedRecompFunctions");
				line = line.replaceAll("schwerpunkt", "updateBarycenters");
				line = line.replaceAll("ausgewaehltesThetaWachstum", "selectedThetaGrowth");
				line = line.replaceAll("kleinstesTheta", "smallestTheta");
				line = line.replaceAll("vergroesserungswertDesThetas", "increasingValueOfThetas");
				line = line.replaceAll("anzahlDerZuVerwendendenThetaWerte", "numberOfThetaValues");
				line = line.replaceAll("anzahlDerTests", "numberOfTests");
				line = line.replaceAll("bewertungskriterien", "qualityCriterions");
				line = line.replaceAll("KANTENLAENGE", "EDGE_LENGTH");
				line = line.replaceAll("GEOMETRISCHER_KNOTENABSTAND_IM_VERHAELTNIS_ZU_KUERZESTEM_PFAD",
						"RATIO_GEOMETRIC_VERTEX_DISTANCE_TO_SHORTEST_PATH"); //must be before the next otherwise overwriting
				line = line.replaceAll("KNOTENABSTAND", "DISTANCE_VERTEX_VERTEX");
				line = line.replaceAll("ABSTAND_KNOTEN_NICHT_INZIDENTE_KANTEN", "DISTANCE_VERTEX_NOT_INCIDENT_EDGE");
				line = line.replaceAll("ABWEICHUNG_VOM_OPTIMALEN_WINKEL", "DEVIATION_FROM_THE_OPTIMAL_ANGLE");
				line = line.replaceAll("bewertungskriteriumWinkel", "qualityCriterionAngle");
				line = line.replaceAll("bewertungskriteriumKreuzungen", "qualityCriterionCrossings");
//				line = line.replaceAll("maxNodesFRExact", "maxNodesFRExact"); //same
				
			}
			else{
				 
				//Berechnungswerte (ComputationData)
				line = line.replaceAll("anzahlDerKnoten", "numberOfVertices");
				line = line.replaceAll("anzahlDerKanten", "numberOfEdges");
				line = line.replaceAll("cpuZeitInNs", "cpuTimeInNs");
				line = line.replaceAll("anzahlDerKreuzungen", "numberOfCrossings");
				line = line.replaceAll("qualitaetseigenschaften", "qualityProperties");
				line = line.replaceAll("kleinsterWinkel", "smallestAngle");
				line = line.replaceAll("anzahlDerIterationen", "numberOfIterations");
				
				//Graphreferenz (GraphReference)
				line = line.replaceAll("graphenSindInVerzeichnisGespeichert",
						"graphsAreStoredInDirectory");
				line = line.replaceAll("graphenVerzeichnisNameOderMethodenkennwort",
						"directoryOrMethodCallParameter");
				line = line.replaceAll("anzahlDerGraphen", "numberOfGraphs");
				line = line.replaceAll("kurzeBeschreibung", "shortDescription");
				
			}
			
		}
		
		return line;
	}
}
