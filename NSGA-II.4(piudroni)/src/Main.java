import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import jxl.Workbook;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class Main {

	private static final String EXCEL_FILE_LOCATION = "C:\\Users\\Matte\\Desktop\\Dottorato\\Optimization\\OptimizationNSGAII\\Progetto\\Implementazione\\NSGA-II.4(piudroni)\\src\\Risultati\\ris.xls";
	static WritableWorkbook workBook = null;
	static WritableSheet excelSheet;

	static Random r = new Random();
	static int maxProfit;

	static int numObjective = 3;
	static boolean[] maxOrMinForThatObjective = new boolean[] { true, true, false };
	static int populationSize = 4;
	static double minProfitNeeded;
	static int numIter = 10;
	static double mutationProb = 0.2;
	static double crossoverProb = 0.9;
	static int numScenario = 1;

	// drones parameters
	static double maxBatteryConsumption = Double.MAX_VALUE;
	static int numExtraNodesForDroneInTheGraph;
	static int numDronesPerVehicle = 3;
	static double frameWeight = 1;
	static double batteryWeight = 1;
	static double k = 1;
	// end drones parameters

	static int numNodesInTheGraph;
	static int numMaxVehicle = 3;

	static ArrayList<Vehicle> vehicles;
	static Graph graph;

	static HashMap<Integer, ArrayList<Integer>> reachableUsingDrone;

	static ArrayList<Integer> copyNodes;
	static ArrayList<Integer> copyNodesDrones;

	public static void main(String[] args) throws FileNotFoundException {

		// create excel file and init count of rows

		File folder = new File("src\\TRPP datasets");

		for (final File fileEntry : folder.listFiles()) {
			
			System.out.println(fileEntry.getAbsolutePath());
			createExcelFile();

			double avgTime = 0, avgProfit = 0, avgVehicles = 0;
			for (int scenario = 0; scenario < numScenario; scenario++) {
				r.setSeed(scenario);
				System.out.println(scenario);
				int numIteration = numIter;
				copyNodes = new ArrayList<Integer>();
				reachableUsingDrone = new HashMap<Integer, ArrayList<Integer>>();
				readInstance(fileEntry);
//						graph.print();

				initReachableUsingDrone();// inizializziamo i punti potenzialmente raggiungibili dai droni
											// per
											// ogni
											// nodo

				createRandomVehicle(); // creiamo i veicoli a disposizione con capacità random

				long start = System.currentTimeMillis();

				ArrayList<Individual> P = initPopulation();
				// System.out.println(P);

				ArrayList<ArrayList<Individual>> F = fast_non_dominated_sort(P);
				for (int front = 0; front < F.size(); front++) {
//							System.out.println("Nel front " + front + " ci sono " + F.get(front).size() + " soluzioni"); //
//			System.out.println(F.get(front));
					crowding_distance_assignment(F.get(front));
				}

				while (numIteration-- > 0) {
					ArrayList<Individual> Q = generateChildren(P);

					ArrayList<Individual> union = new ArrayList<Individual>(Q);

					union.addAll(P);

					F = fast_non_dominated_sort(union);

					P = updatePopulation(F);

					// System.out.println(P.size());
				}

				F = fast_non_dominated_sort(P);

//						for (int front = 0; front < F.size(); front++) {
//							System.out
//									.println(" Nel front " + front + " ci sono " + F.get(front).size() + " soluzioni");
//							for (Individual a : F.get(front))
//								System.out.println(a.getObjectiveValue()[0] + " " + a.getObjectiveValue()[1] + " "
//										+ a.getObjectiveValue()[2]);
//						}

				checkSolution(P);
				addValue(P);
//						System.out.println("TIME = " + (System.currentTimeMillis() - start) / 1000);
				double currProfit = 0, currTime = 0, currVehicles = 0;
				for (int index = 0; index < populationSize; index++) {
					currVehicles += P.get(index).getObjectiveValue()[0];
					currTime += P.get(index).getObjectiveValue()[1];
					currProfit += P.get(index).getObjectiveValue()[2];
				}
				avgProfit += (currProfit / populationSize);
				avgTime += (currTime / populationSize);
				avgVehicles += (currVehicles / populationSize);
			}
			System.out.println("AVG PROFIT = " + avgProfit / numScenario);
			System.out.println("AVG TIME = " + avgTime / numScenario);
			System.out.println("AVG VEHICLES = " + avgVehicles / numScenario);
			closeExcelFile();
			break;

		}

	}

	private static void readInstance(File fileEntry) throws FileNotFoundException {
		Scanner myReader = new Scanner(fileEntry);
		int index = 0;
		while (myReader.hasNextLine()) {
			String data = myReader.nextLine();
			if (data.split("\t").length <= 1) {
				int nodes = Integer.valueOf(data.split("\t")[0]);
				numNodesInTheGraph = (int) Math.ceil(nodes * 0.75);
				numExtraNodesForDroneInTheGraph = (int) Math.floor(nodes * 0.25);
				graph = new Graph(1 + numNodesInTheGraph + numExtraNodesForDroneInTheGraph);
			} else {
				String values[] = data.split("\t");
				graph.coordinates[index] = new Pair(Integer.valueOf(values[0]), Integer.valueOf(values[1]));
				graph.profit[index] = Integer.valueOf(values[2]);
				maxProfit += graph.profit[index];
				graph.neededResource[index] = 0; // come impostare questo valore?
				index++;
			}
		}
		minProfitNeeded = maxProfit;
		graph.setDistance();
		graph.print();
		myReader.close();

	}

	private static void addValue(ArrayList<Individual> p) {
		try {
			for (int i = 2; i < 2 + populationSize; i++) {
				excelSheet.addCell(new Number(0, i, p.get(i - 2).getObjectiveValue()[0]));
				excelSheet.addCell(new Number(1, i, p.get(i - 2).getObjectiveValue()[1]));
				excelSheet.addCell(new Number(2, i, p.get(i - 2).getObjectiveValue()[2]));

			}
			Formula f = new Formula(3, 1, "AVERAGE(a3:a52)");
			excelSheet.addCell(f);

			f = new Formula(4, 1, "AVERAGE(b3:b52)");
			excelSheet.addCell(f);
			f = new Formula(5, 1, "AVERAGE(c3:c52)");
			excelSheet.addCell(f);

		} catch (Exception e) {
			throw new RuntimeException("Error adding excel value");
		}
	}

	private static void closeExcelFile() {

		if (workBook != null) {
			try {
				workBook.write();
				workBook.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (WriteException e) {
				e.printStackTrace();
			}
		}
	}

	private static void createExcelFile() {

		try {
			workBook = Workbook.createWorkbook(new File(EXCEL_FILE_LOCATION));

			// create an Excel sheet
			excelSheet = workBook.createSheet("Ris", 0);

			// add header into the Excel sheet
			Label label = new Label(0, 0, "NumVeic");
			excelSheet.addCell(label);

			label = new Label(1, 0, "Makespan");
			excelSheet.addCell(label);

			label = new Label(2, 0, "Profit");
			excelSheet.addCell(label);

		} catch (Exception e) {
			throw new RuntimeException("error creating excel file");
		}

	}

	private static void checkSolution(ArrayList<Individual> p) {
		for (Individual ind : p)
			for (Vehicle v : ind.getGenotypeVehicle())
				for (Integer node : v.getTour()) {
					for (int i = 0; i < numDronesPerVehicle; i++) {
						double duration = 0;
						double consumption = 0;
						if (node == 0)
							continue;
						if (v.getDroneTour().get(node).get(i) != null)
							for (Integer drone : v.getDroneTour().get(node).get(i)) {
								duration += 2 * graph.getDuration(node, drone);
								consumption += getConsumption(node, drone);
							}
						if (Math.abs(duration - v.getCurrentDroneTourLength().get(node).get(i)) >= Math.pow(10, -6)) {
							System.out.println(duration + " " + v.getCurrentDroneTourLength().get(node).get(i));
							throw new RuntimeException("error computing duration");
						}
						if (consumption > maxBatteryConsumption)
							throw new RuntimeException("error battery consumed");
					}
				}
	}

	private static ArrayList<Individual> updatePopulation(ArrayList<ArrayList<Individual>> union) {
		ArrayList<Individual> toReturn = new ArrayList<Individual>();
		int index = 0;
		while (toReturn.size() + union.get(index).size() <= populationSize) {
			crowding_distance_assignment(union.get(index));
			toReturn.addAll(union.get(index));
			index++;
			if (index == union.size())
				break;
		}

		if (toReturn.size() == populationSize) {
			return toReturn;
		}

		crowding_distance_assignment(union.get(index));
		Collections.sort(union.get(index), new Comparator<Individual>() {
			@Override
			public int compare(Individual o1, Individual o2) {
				if (o1.getCrowdDistance() > o2.getCrowdDistance())
					return -1;
				if (o1.getCrowdDistance() == o2.getCrowdDistance())
					return 0;
				return 1;

			}
		});

		int i = 0;
		while (toReturn.size() < populationSize) {
			toReturn.add(union.get(index).get(i));
			i++;
		}

		return toReturn;
	}

	private static ArrayList<Individual> generateChildren(ArrayList<Individual> population) {
		ArrayList<Individual> children = new ArrayList<Individual>();

//		for (Individual i : population)
//			for (Vehicle v : i.getGenotypeVehicle())
//				v.checkCapacity();

		// System.out.println(population);
		while (children.size() < populationSize) {

			// System.out.println(population.size());
//			for (Individual i : population) {
//				System.out.println(i);
//				for (Vehicle v : i.getGenotypeVehicle())
//					v.checkCapacity();
//			}
			ArrayList<Individual> copy = new ArrayList<Individual>(population);

			Individual first = new Individual(copy.remove(r.nextInt(copy.size())).getGenotypeVehicle());
			Individual second = new Individual(copy.remove(r.nextInt(copy.size())).getGenotypeVehicle());
			Individual third = new Individual(copy.remove(r.nextInt(copy.size())).getGenotypeVehicle());
			Individual fourth = new Individual(copy.remove(r.nextInt(copy.size())).getGenotypeVehicle());

//			for (Vehicle v : first.getGenotypeVehicle())
//				v.checkCapacity();
//			for (Vehicle v : second.getGenotypeVehicle())
//				v.checkCapacity();
//			for (Vehicle v : third.getGenotypeVehicle())
//				v.checkCapacity();
//			for (Vehicle v : fourth.getGenotypeVehicle())
//				v.checkCapacity();

			Individual winning_first = null, winning_second = null;

			if (first.getFront() < second.getFront()
					|| (first.getFront() == second.getFront() && first.getCrowdDistance() > second.getCrowdDistance()))
				winning_first = first;
			else
				winning_first = second;

			if (third.getFront() < fourth.getFront()
					|| (third.getFront() == fourth.getFront() && third.getCrowdDistance() > fourth.getCrowdDistance()))
				winning_second = third;
			else
				winning_second = fourth;

//			for (Vehicle v : winning_first.getGenotypeVehicle())
//				v.checkCapacity();
//
//			for (Vehicle v : winning_second.getGenotypeVehicle())
//				v.checkCapacity();

			// System.out.println("FIRST \n" + winning_first + "\n SECOND \n" +
			// winning_second);

			// just because the following crossover operator suppose that the vehicles in
			// winning_first are more or equal than the ones in winning_second,
			// we swap the two if this is not the case
			if (winning_first.getGenotypeVehicle().size() < winning_second.getGenotypeVehicle().size()) {
				Individual toSwap = new Individual(winning_first.getGenotypeVehicle());
				winning_first = new Individual(winning_second.getGenotypeVehicle());
				winning_second = new Individual(toSwap.getGenotypeVehicle());
			}

//			for (Vehicle v : winning_first.getGenotypeVehicle())
//				v.checkCapacity();
//
//			for (Vehicle v : winning_second.getGenotypeVehicle())
//				v.checkCapacity();

			Individual firstInd = null;
			Individual secondInd = null;

			if (r.nextDouble() <= crossoverProb) {
				int childNumberOfVehicle = (winning_first.getGenotypeVehicle().size()
						+ winning_second.getGenotypeVehicle().size()) / 2;

				ArrayList<Vehicle> currentIndividualVehicleFC = new ArrayList<Vehicle>();
				ArrayList<Vehicle> currentIndividualVehicleSC = new ArrayList<Vehicle>();
				int crossOverPoint = r.nextInt(childNumberOfVehicle) + 1;

				for (int i = 0; i < childNumberOfVehicle; i++) {
					if (i < crossOverPoint) {
						currentIndividualVehicleFC.add(new Vehicle(winning_first.getGenotypeVehicle().get(i)));
					} else {
						if (winning_second.getGenotypeVehicle().size() - 1 - (i - crossOverPoint) >= 0) {

							currentIndividualVehicleFC.add(new Vehicle(winning_second.getGenotypeVehicle()
									.get(winning_second.getGenotypeVehicle().size() - 1 - (i - crossOverPoint))));
						} else {

							currentIndividualVehicleFC.add(new Vehicle(winning_first.getGenotypeVehicle()
									.get(winning_first.getGenotypeVehicle().size()
											+ (winning_second.getGenotypeVehicle().size() - 1
													- (i - crossOverPoint)))));
						}

					}

					if (crossOverPoint + i < winning_first.getGenotypeVehicle().size()) {
						currentIndividualVehicleSC
								.add(new Vehicle(winning_first.getGenotypeVehicle().get(crossOverPoint + i)));

					} else {

						currentIndividualVehicleSC.add(new Vehicle(winning_second.getGenotypeVehicle()
								.get((crossOverPoint + i) % winning_first.getGenotypeVehicle().size())));
					}
				}

				firstInd = new Individual(currentIndividualVehicleFC);
				secondInd = new Individual(currentIndividualVehicleSC);

//			for (Vehicle v : firstInd.getGenotypeVehicle())
//				v.checkCapacity();
//			for (Vehicle v : secondInd.getGenotypeVehicle())
//				v.checkCapacity();

				// System.out.println("AFTER CROSSOVER \n FIRST \n" + firstInd + "\n SECOND \n"
				// + secondInd);

				recoverFeasibility(firstInd);
				recoverFeasibility(secondInd);
			} else {
				firstInd = new Individual(winning_first.getGenotypeVehicle());
				secondInd = new Individual(winning_second.getGenotypeVehicle());
			}

//			for (Vehicle v : firstInd.getGenotypeVehicle())
//				v.checkCapacity();
//			for (Vehicle v : secondInd.getGenotypeVehicle())
//				v.checkCapacity();

			// System.out.println("AFTER FEASIBILITY \n FIRST \n" + firstInd + "\n SECOND
			// \n" + secondInd);
			if (r.nextDouble() <= mutationProb) {
				mutation(firstInd);
				mutation(secondInd);
			}

//			for (Vehicle v : firstInd.getGenotypeVehicle())
//				v.checkCapacity();
//			for (Vehicle v : secondInd.getGenotypeVehicle())
//				v.checkCapacity();

			// System.out.println("AFTER MUTATION \n FIRST \n" + firstInd + "\n SECOND \n" +
			// secondInd);

			postOptimize(firstInd);
			postOptimize(secondInd);

//			for (Vehicle v : firstInd.getGenotypeVehicle())
//				v.checkCapacity();
//			for (Vehicle v : secondInd.getGenotypeVehicle())
//				v.checkCapacity();

			// System.out.println("AFTER OPTIMIZATION \n FIRST \n" + firstInd + "\n SECOND
			// \n" + secondInd);

			children.add(firstInd);
			children.add(secondInd);
//
//			for (Vehicle v : firstInd.getGenotypeVehicle())
//				v.checkCapacity();
//			for (Vehicle v : secondInd.getGenotypeVehicle())
//				v.checkCapacity();

		}
		return children;
	}

	private static void postOptimize(Individual s) {
		if (s.getGenotypeVehicle().size() == 1)
			return;

		// se un nodo è visitato più di una volta, lo lascio solo nel veicolo dove ha la
		// rotta per il drone più lunga
		for (int i = 1; i < numNodesInTheGraph; i++) {
			if (s.getVisited().contains(i)) {

				double mostProfitableTour = Double.MIN_VALUE;
				int maxTourVehicleIndex = -1;
				int howMany = 0;

				for (int j = 0; j < s.getGenotypeVehicle().size(); j++) {
					if (s.getGenotypeVehicle().get(j).getTour().contains(i)) {
						howMany++;
						double profit = 0;
						for (int drone = 0; drone < numDronesPerVehicle; drone++)
							profit += s.getGenotypeVehicle().get(j).getCurrentDroneTourProfit().get(i).get(drone);
						if (profit > mostProfitableTour) {
							mostProfitableTour = profit;
							maxTourVehicleIndex = j;
						}
					}
				}

				if (howMany >= 2) {
					for (int j = 0; j < s.getGenotypeVehicle().size(); j++) {
						if (j != maxTourVehicleIndex && s.getGenotypeVehicle().get(j).getTour().contains(i)) {
							s.getGenotypeVehicle().get(j).removeNode(i);
						}
					}
				}
			}
		}
//		System.out.println("BEFORE \n" + s.getGenotypeVehicle() + "\n END");
		Collections.sort(s.getGenotypeVehicle(), new Comparator<Vehicle>() {
			@Override
			public int compare(Vehicle o1, Vehicle o2) {
				if (o1.getTourLength() > o2.getTourLength())
					return 1;
				if (o1.getTourLength() == o2.getTourLength())
					return 0;

				return -1;
			}
		});

		boolean found = true;
		while (found) {
//			System.out.println("UPDATED \n " + s.getGenotypeVehicle());
			found = false;
			// controllare che il tour non sia vuoto
			Vehicle busiest = s.getGenotypeVehicle().get(s.getGenotypeVehicle().size() - 1);
			if (busiest.getTour().size() <= 1)
				break;
			int toAssign = busiest.getTour().get(1); // si potrebbe fare un for per ogni toAssing
			double droneTour = busiest.getLongestDroneTourForNode().get(toAssign);

//			System.out.println(busiest.getCurrentDroneTourLength());
			HashMap<Integer, ArrayList<Integer>> droneTourPath = busiest.getDroneTour().get(toAssign);
			double resourceNeeded = 0;
			for (int drone = 0; drone < numDronesPerVehicle; drone++)
				if (droneTourPath.get(drone) != null)
					for (Integer i : droneTourPath.get(drone))
						resourceNeeded += graph.getNeededResource(i);

			for (int index = 0; index < s.getGenotypeVehicle().size() - 1; index++) {
				Vehicle v = s.getGenotypeVehicle().get(index);
				if (v.getCurrentCapacity() >= graph.getNeededResource(toAssign) + resourceNeeded
						&& v.getTourLength() + graph.getDuration(v.getTour().get(v.getTour().size() - 1), toAssign)
								+ droneTour < busiest.getTourLength()) {

					Vehicle avoidLoop = new Vehicle(busiest);
					avoidLoop.removeNode(toAssign);
					if (avoidLoop.getTourLength() >= busiest.getTourLength())
						continue;

//					System.out.println("freest " + v);
//					System.out.println("busiest " + busiest);
//					System.out.println("I'M ADDING " + toAssign + " in " + v);

					v.addNode(toAssign);
					for (int drone = 0; drone < numDronesPerVehicle; drone++)
						v.addDronePath(toAssign, drone, droneTourPath.get(drone));
//					System.out.println("obtaining " + toAssign + " in " + v);
					busiest.removeNode(toAssign);
					found = true;

					int iter = index + 1;
					while (iter < s.getGenotypeVehicle().size()
							&& s.getGenotypeVehicle().get(iter).getTourLength() <= v.getTourLength())
						iter++;

					if (iter != index + 1) {
						for (int i = index; i < iter - 1; i++) {
							Vehicle swap = s.getGenotypeVehicle().get(i);
							s.getGenotypeVehicle().set(i, s.getGenotypeVehicle().get(i + 1));
							s.getGenotypeVehicle().set(i + 1, swap);
						}
					}

					// usato perchè se devo sostituirlo con l'ultimo, arriverò al secondo while con
					// l'elemento non più ultimo ma penultimo
					int stride = 0;
					if (iter == s.getGenotypeVehicle().size()) {
						stride = 1;
						busiest = s.getGenotypeVehicle().get(s.getGenotypeVehicle().size() - 2);
					}

					iter = s.getGenotypeVehicle().size() - 2;

					while (iter - stride >= 0
							&& s.getGenotypeVehicle().get(iter - stride).getTourLength() > busiest.getTourLength())
						iter--;
					if (iter != s.getGenotypeVehicle().size() - 2) {
						for (int i = s.getGenotypeVehicle().size() - 1 - stride; i > iter - stride + 1; i--) {
							Vehicle swap = s.getGenotypeVehicle().get(i);
							s.getGenotypeVehicle().set(i, s.getGenotypeVehicle().get(i - 1));
							s.getGenotypeVehicle().set(i - 1, swap);
						}
					}

					break;

				}
			}
		}

		// IMPLEMENTARE 2 OPT

//		System.out.println("AFTER \n" + s.getGenotypeVehicle() + "\n END");
		s.updateSolution(null);

	}

	private static void mutation(Individual individual) {

		// change position i with position j with probability equal to mutationProb
		for (Vehicle vehicle : individual.getGenotypeVehicle()) {
			for (int i = 1; i < vehicle.getTour().size() - 1; i++) {
				for (int j = i + 1; j < vehicle.getTour().size(); j++) {
					vehicle.swap(i, j);

				}
			}
		}

		// add node with probability mutationProb
		for (int i = 0; i < numNodesInTheGraph; i++) {
			int extractedRandomNode = copyNodes.get(r.nextInt(copyNodes.size()));
			if (individual.getVisited().contains(extractedRandomNode))
				continue;
			Vehicle extractedRandomVehicle = individual.getGenotypeVehicle()
					.get(r.nextInt(individual.getGenotypeVehicle().size()));
			if (extractedRandomVehicle.getCurrentCapacity() - graph.getNeededResource(extractedRandomNode) >= 0
					&& !extractedRandomVehicle.getTour().contains(extractedRandomNode)) {
				extractedRandomVehicle.addNode(extractedRandomNode);
			}
		}

		individual.updateSolution(null);

		double currentProfit = individual.getObjectiveValue()[2];
		// remove node with probability mutationProb
		for (int i = 0; i < numNodesInTheGraph; i++) {
			Vehicle extractedRandomVehicle = individual.getGenotypeVehicle()
					.get(r.nextInt(individual.getGenotypeVehicle().size()));

			if (extractedRandomVehicle.getTour().size() <= 1)
				continue;

			int extractedRandomNode = extractedRandomVehicle.getTour()
					.get(r.nextInt(extractedRandomVehicle.getTour().size() - 1) + 1);

			double droneProfit = 0;
			for (int drone = 0; drone < numDronesPerVehicle; drone++)
				droneProfit += extractedRandomVehicle.getCurrentDroneTourProfit().get(extractedRandomNode).get(drone);

			if (currentProfit - graph.getProfit(extractedRandomNode) - droneProfit >= minProfitNeeded) {
				currentProfit -= graph.getProfit(extractedRandomNode);
				currentProfit -= droneProfit;
				extractedRandomVehicle.removeNode(extractedRandomNode); // il profitto andrebbe sottrato in removeNode e
																		// non qui
			}
		}

		individual.updateSolution(null);

		boolean checked[] = new boolean[numExtraNodesForDroneInTheGraph];

		// add one drone node random
		for (int i = numNodesInTheGraph; i < numNodesInTheGraph + numExtraNodesForDroneInTheGraph; i++) {
			int extractedRandomNode = copyNodesDrones.get(r.nextInt(copyNodesDrones.size()));

			if (checked[extractedRandomNode - numNodesInTheGraph])
				continue;
			else
				checked[extractedRandomNode - numNodesInTheGraph] = true;

			if (individual.getVisited().contains(extractedRandomNode))
				continue;

			Vehicle extractedRandomVehicle = individual.getGenotypeVehicle()
					.get(r.nextInt(individual.getGenotypeVehicle().size()));
			if (extractedRandomVehicle.getTour().size() <= 1)
				continue;
			int extractedRandomNodeForAdding = extractedRandomVehicle.getTour()
					.get(r.nextInt(extractedRandomVehicle.getTour().size() - 1) + 1);
			int randomDrone = r.nextInt(numDronesPerVehicle);

			if (extractedRandomVehicle.getCurrentDroneEnergyUsed().get(extractedRandomNodeForAdding).get(randomDrone)
					+ getConsumption(extractedRandomNodeForAdding, extractedRandomNode) <= maxBatteryConsumption
					&& extractedRandomVehicle.getCurrentCapacity() >= Main.graph
							.getNeededResource(extractedRandomNode)) {
				extractedRandomVehicle.addExtraNode(extractedRandomNodeForAdding, extractedRandomNode, randomDrone,
						2 * graph.getDuration(extractedRandomNodeForAdding, extractedRandomNode),
						graph.getProfit(extractedRandomNode));
			}

		}

		individual.updateSolution(null);

		currentProfit = individual.getObjectiveValue()[2];
		// remove droneNode with probability mutationProb
		for (int i = numNodesInTheGraph; i < numNodesInTheGraph + numExtraNodesForDroneInTheGraph; i++) {
			Vehicle extractedRandomVehicle = individual.getGenotypeVehicle()
					.get(r.nextInt(individual.getGenotypeVehicle().size()));

			if (extractedRandomVehicle.getTour().size() <= 1)
				continue;

			int extractedRandomNode = extractedRandomVehicle.getTour()
					.get(r.nextInt(extractedRandomVehicle.getTour().size() - 1) + 1);
			int extractedRandomDrone = r.nextInt(numDronesPerVehicle);

			ArrayList<Integer> droneTourSelected = extractedRandomVehicle.getDroneTour().get(extractedRandomNode)
					.get(extractedRandomDrone);

			if (droneTourSelected == null || droneTourSelected.size() == 0)
				continue;

			int extractedDroneNode = droneTourSelected.get(r.nextInt(droneTourSelected.size()));

			if (currentProfit - graph.getProfit(extractedDroneNode) >= minProfitNeeded) {
				extractedRandomVehicle.removeExtraNode(extractedRandomNode, extractedRandomDrone, extractedDroneNode,
						2 * graph.getDuration(extractedRandomNode, extractedDroneNode),
						graph.getProfit(extractedDroneNode));
				currentProfit -= graph.getProfit(extractedDroneNode);
			}
		}
		individual.updateSolution(null);

	}

	// consumo in funzione della distanza d
	public static Double getConsumption(int startingNode, int endingNode) {
		double distance = 2 * graph.getDuration(startingNode, endingNode);
		return distance * k * (Math.pow(frameWeight + batteryWeight + graph.getNeededResource(endingNode), 1.5));
	}

	private static void crowding_distance_assignment(ArrayList<Individual> front_i_th) {
		for (Individual i : front_i_th) {
			i.setCrowdDistance(0);
		}

		for (int i = 0; i < numObjective; i++) {
			Collections.sort(front_i_th, new ObjectiveComparator(i));
			double normalization = front_i_th.get(front_i_th.size() - 1).getObjectiveValue()[i]
					- front_i_th.get(0).getObjectiveValue()[i];

			front_i_th.get(0).setCrowdDistance(Double.MAX_VALUE);
			front_i_th.get(front_i_th.size() - 1).setCrowdDistance(Double.MAX_VALUE);
			for (int index = 1; index < front_i_th.size() - 1; index++) {
				Individual current = front_i_th.get(index);
				current.setCrowdDistance(current.getCrowdDistance() + ((front_i_th.get(index + 1).getObjectiveValue()[i]
						- front_i_th.get(index - 1).getObjectiveValue()[i])) / normalization);
			}
		}
	}

	private static ArrayList<ArrayList<Individual>> fast_non_dominated_sort(ArrayList<Individual> population) {
		ArrayList<ArrayList<Individual>> toReturn = new ArrayList<ArrayList<Individual>>();
		ArrayList<Individual> F1 = new ArrayList<Individual>();

		for (Individual p : population) {
			ArrayList<Individual> dominated = new ArrayList<Individual>();
			int dominatedBy = 0;
			for (Individual q : population) {
				if (isDominatedBy(q, p))
					dominated.add(q);
				else if (isDominatedBy(p, q))
					dominatedBy++;
			}
			p.setDominatedBy(dominatedBy);
			if (dominatedBy == 0) {
				p.setFront(0);
				F1.add(p);
			}

			p.setDominated(dominated);
		}
		toReturn.add(F1);

		int i = 0;
		ArrayList<Individual> current = toReturn.get(i);
		while (!current.isEmpty()) {
			ArrayList<Individual> Q = new ArrayList<Individual>();
			for (Individual p : current)
				for (Individual q : p.getDominated()) {
					q.setDominatedBy(q.getDominatedBy() - 1);
					if (q.getDominatedBy() == 0) {
						q.setFront(i + 1);
						Q.add(q);
					}
				}
			i += 1;
			current = Q;
			if (Q.size() != 0)
				toReturn.add(Q);
		}

		return toReturn;
	}

	private static boolean isDominatedBy(Individual first, Individual second) {
		boolean atLeastOne = false;
		for (int i = 0; i < numObjective; i++)
			if (maxOrMinForThatObjective[i]) // we want to minimize that objective function
			{
				if (first.getObjectiveValue()[i] > second.getObjectiveValue()[i])
					atLeastOne = true;
				else if (first.getObjectiveValue()[i] < second.getObjectiveValue()[i])
					return false;
			} else {
				if (first.getObjectiveValue()[i] < second.getObjectiveValue()[i])
					atLeastOne = true;
				else if (first.getObjectiveValue()[i] > second.getObjectiveValue()[i])
					return false;
			}
		return atLeastOne;
	}

	private static ArrayList<Individual> initPopulation() {
		ArrayList<Individual> toReturn = new ArrayList<Individual>();
		for (int i = 0; i < populationSize; i++) {

			// creo una lista con valori da 0 alla size dei veicoli disponibili
			ArrayList<Integer> vehiclesIndexAvailable = new ArrayList<Integer>();
			for (int j = 0; j < numMaxVehicle; j++)
				vehiclesIndexAvailable.add(j);

			// decidiamo il numero di veicoli da usare nella soluzione in modo random
			int numVehicle = r.nextInt(numMaxVehicle) + 1;

			ArrayList<Vehicle> vehiclesToUse = new ArrayList<Vehicle>();
			for (int j = 0; j < numVehicle; j++) {
				Vehicle choosen = vehicles.get(vehiclesIndexAvailable.remove(r.nextInt(vehiclesIndexAvailable.size())));
				vehiclesToUse.add(new Vehicle(choosen.getCapacity()));
			}

			ArrayList<Integer> nodesInTheGraph = new ArrayList<Integer>();
			ArrayList<Integer> nodesForTheDrones = new ArrayList<Integer>();

			for (int j = 1; j < numNodesInTheGraph; j++)
				nodesInTheGraph.add(j);

			for (int j = numNodesInTheGraph; j < numNodesInTheGraph + numExtraNodesForDroneInTheGraph; j++)
				nodesForTheDrones.add(j);

			if (i == 0) {
				initCopyNodes(nodesInTheGraph);
				copyNodesDrones = new ArrayList<Integer>(nodesForTheDrones); // NON SONO ORDINATI IN BASE AL PROFITTO
			}

			// con probabilità pari a 0.8 assegniamo un nodo ad un veicolo random
			while (!nodesInTheGraph.isEmpty()) {
				int selectedNode = nodesInTheGraph.remove(r.nextInt(nodesInTheGraph.size()));

				if (r.nextDouble() <= 0.8) {
					int selectedVehicle = r.nextInt(numVehicle);
					if (vehiclesToUse.get(selectedVehicle).getCurrentCapacity() >= graph
							.getNeededResource(selectedNode)) {
						vehiclesToUse.get(selectedVehicle).addNode(selectedNode);
					}
				}
			}

			// add node for drone for random vehicles and random node as starting point
			for (int j = 0; j < numExtraNodesForDroneInTheGraph; j++) {
				int randomIndex = r.nextInt(numVehicle); // veicolo scelto random
				int sizeOfItsPath = vehiclesToUse.get(randomIndex).getTour().size();
				if (sizeOfItsPath <= 1)
					continue;
				int nodeRandom = vehiclesToUse.get(randomIndex).getTour().get(r.nextInt(sizeOfItsPath - 1) + 1);

				if (reachableUsingDrone.get(nodeRandom).size() == 0)
					continue;

				int destination = reachableUsingDrone.get(nodeRandom)
						.get(r.nextInt(reachableUsingDrone.get(nodeRandom).size()));
				int droneRandom = r.nextInt(numDronesPerVehicle);

				// scegliamo un indice di partenza random e usiamo il modulo per scorrere in
				// modo ciclico la lista (da i a n e da 0 a i-1)
				if (getConsumption(nodeRandom, destination)
						+ vehiclesToUse.get(randomIndex).getCurrentDroneEnergyUsed().get(nodeRandom)
								.get(droneRandom) <= maxBatteryConsumption
						&& nodesForTheDrones.contains(destination) && vehiclesToUse.get(randomIndex)
								.getCurrentCapacity() >= graph.getNeededResource(destination)) {
					nodesForTheDrones.remove(Integer.valueOf(destination));
					vehiclesToUse.get(randomIndex).addExtraNode(nodeRandom, destination, droneRandom,
							2 * graph.getDuration(nodeRandom, destination), graph.getProfit(destination));

				}

			}

			Individual toAdd = new Individual(vehiclesToUse);
			recoverFeasibility(toAdd);

			toReturn.add(toAdd);
		}

		return toReturn;

	}

	private static void recoverFeasibility(Individual toAdd) {

		if (toAdd.getObjectiveValue()[2] > minProfitNeeded)
			return;
		// sort vehicles depending on the length of the tour and assign,
		// until the feasibility is reached, the most profitable node to the most free
		// vehicle

		Collections.sort(toAdd.getGenotypeVehicle(), new Comparator<Vehicle>() {
			@Override
			public int compare(Vehicle o1, Vehicle o2) {
				if (o1.getTourLength() < o2.getTourLength())
					return -1;
				else if (o1.getTourLength() == o2.getTourLength())
					return 0;
				return 1;
			}
		});

		// provo ad aggiungere solo nodi del grafo e non nodi per i droni, aggiungere.
		double added = 0;
		for (int indexNode = 1; indexNode < numNodesInTheGraph
				&& toAdd.getObjectiveValue()[2] + added < minProfitNeeded; indexNode++) {
			if (toAdd.getVisited().contains(indexNode))
				continue;

			for (int j = 0; j < toAdd.getGenotypeVehicle().size(); j++) {
				if (toAdd.getGenotypeVehicle().get(j).getCurrentCapacity() >= graph.getNeededResource(indexNode)) {

					toAdd.getGenotypeVehicle().get(j).addNode(indexNode);
					added += graph.getProfit(indexNode);
					break;
				}

			}

		}
		toAdd.updateSolution(null);

		added = 0;
		for (int i = numNodesInTheGraph; i < numNodesInTheGraph + numExtraNodesForDroneInTheGraph
				&& toAdd.getObjectiveValue()[2] + added < minProfitNeeded; i++) {

			if (toAdd.getVisited().contains(i))
				continue;
			else {
				// debug
				for (int a = 0; a < toAdd.getGenotypeVehicle().size(); a++)
					for (Integer b : toAdd.getGenotypeVehicle().get(a).getTour())
						for (int d = 0; d < numDronesPerVehicle; d++) {
							HashMap<Integer, ArrayList<Integer>> check = toAdd.getGenotypeVehicle().get(a)
									.getDroneTour().get(b);
							if (check != null) {
								ArrayList<Integer> checkb = check.get(d);
								if (checkb != null && checkb.contains(i))
									throw new RuntimeException("errore");
							}
						}
				// end debug
			}
			boolean inserted = false;
			int startingrandom = r.nextInt(toAdd.getGenotypeVehicle().size());
			for (int j = startingrandom; j < startingrandom + toAdd.getGenotypeVehicle().size() && !inserted; j++) {
				Vehicle vehicle = toAdd.getGenotypeVehicle().get(j % toAdd.getGenotypeVehicle().size());
				if (vehicle.getTour().size() <= 1)
					continue;
				for (Integer nodeForAdding : vehicle.getTour()) {
					if (nodeForAdding == 0)
						continue;
					int droneRandomStart = r.nextInt(numDronesPerVehicle);
					for (int index = droneRandomStart; index < droneRandomStart + numDronesPerVehicle; index++)
						if (vehicle.getCurrentDroneEnergyUsed().get(nodeForAdding).get(index % numDronesPerVehicle)
								+ getConsumption(nodeForAdding, i) <= maxBatteryConsumption
								&& vehicle.getCurrentCapacity() >= Main.graph.getNeededResource(i)) {
							vehicle.addExtraNode(nodeForAdding, i, index % numDronesPerVehicle,
									2 * graph.getDuration(nodeForAdding, i), graph.getProfit(i));
							inserted = true;
							added += Main.graph.getProfit(i);
							break;
						}
					if (inserted)
						break;
				}
			}
		}

		toAdd.updateSolution(null);

	}

	private static void initCopyNodes(ArrayList<Integer> nodesInTheGraph) {
		copyNodes = new ArrayList<Integer>(nodesInTheGraph);

		// ordiniamo i nodi in base al profitto
		Collections.sort(copyNodes, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				if (graph.getProfit(o1) / graph.getNeededResource(o1) > graph.getProfit(o2)
						/ graph.getNeededResource(o2))
					return -1;
				if (graph.getProfit(o1) / graph.getNeededResource(o1) < graph.getProfit(o2)
						/ graph.getNeededResource(o2))
					return 1;

				return 0;
			}
		});
	}

	private static void createRandomVehicle() {
		vehicles = new ArrayList<Vehicle>();
		for (int i = 0; i < numMaxVehicle; i++) {
			boolean capacity = r.nextBoolean();
			if (capacity)
				vehicles.add(new Vehicle(100));
			else
				vehicles.add(new Vehicle(200));
		}
	}

	private static void initReachableUsingDrone() {
		// per ogni nodo del grafo iniziale (0,..,numNodesInTheGraph) calcolo quali
		// punti extra riesco a raggiungere
		for (int i = 0; i < numNodesInTheGraph; i++) {
			reachableUsingDrone.put(i, new ArrayList<Integer>());
			for (int j = numNodesInTheGraph; j < numNodesInTheGraph + numExtraNodesForDroneInTheGraph; j++) {
				if (getConsumption(i, j) <= maxBatteryConsumption) {
					reachableUsingDrone.get(i).add(j);
				}
			}
		}

	}
}
