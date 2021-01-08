import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

public class Main {
	static Random r = new Random();
	static int numObjective = 3;
	static boolean[] maxOrMinForThatObjective = new boolean[] { true, true, false };
	static int populationSize = 2 * 2; // numero pari
	static double minProfitNeeded = 20000;
	static int numIteration = 350;
	static double mutationProb = 0.2;
	static double crossoverProb = 0.8;
	static double maxBatteryConsumption = 30;
	static int numNodesInTheGraph = 4;
	static int numExtraNodesForDroneInTheGraph = 6;

	static Graph graph;

	static ArrayList<Vehicles> vehicles;
	static int numMaxVehicles = 3;

	static HashMap<Integer, ArrayList<Integer>> reachableUsingDrone;

	static ArrayList<Integer> copyNodes;
	static ArrayList<Integer> copyNodesDrones;

	public static void main(String[] args) {
		copyNodes = new ArrayList<Integer>();
		reachableUsingDrone = new HashMap<Integer, ArrayList<Integer>>();
		graph = new Graph(numNodesInTheGraph + numExtraNodesForDroneInTheGraph);
		graph.print();

		initReachableUsingDrone();// inizializziamo i punti potenzialmente raggiungibili dai droni per ogni nodo
		createRandomVehicles(); // creiamo i veicoli a disposizione con capacità random

		ArrayList<Individual> P = initPopulation();
//		System.out.println(P);

		ArrayList<ArrayList<Individual>> F = fast_non_dominated_sort(P);
		for (int front = 0; front < F.size(); front++) {
			System.out.println("Nel front " + front + " ci sono " + F.get(front).size() + " soluzioni");
			// System.out.println(F.get(front));
			crowding_distance_assignment(F.get(front));
		}

		while (numIteration-- > 0) {
			// System.out.println("Iter = " + numIteration);
			ArrayList<Individual> Q = generateChildren(P);

			ArrayList<Individual> union = new ArrayList<Individual>(Q);

			union.addAll(P);

			F = fast_non_dominated_sort(union);

			P = updatePopulation(F);

			// System.out.println(P.size());
		}

		F = fast_non_dominated_sort(P);

		for (int front = 0; front < F.size(); front++) {
			System.out.println(" Nel front " + front + " ci sono " + F.get(front).size() + " soluzioni");
			System.out.println(F.get(front));
			// //
		}

		checkSolution(P);

	}

	private static void checkSolution(ArrayList<Individual> p) {
		for (Individual ind : p)
			for (Vehicles v : ind.getGenotypeVehicles())
				for (Integer node : v.getTour()) {
					double duration = 0;
					if (node == 0)
						continue;
					for (Integer drone : v.getDroneTour().get(node))
						duration += getConsumption(2 * graph.getNormalizedDistance(node, drone));
					if (duration != v.getCurrentDroneTourLength().get(node))
						throw new RuntimeException("error computing duration");
					if (duration > maxBatteryConsumption)
						throw new RuntimeException("error battery consumed");

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
//			for (Vehicles v : i.getGenotypeVehicles())
//				v.checkCapacity();

		// System.out.println(population);
		while (children.size() < populationSize) {

			// System.out.println(population.size());
//			for (Individual i : population) {
//				System.out.println(i);
//				for (Vehicles v : i.getGenotypeVehicles())
//					v.checkCapacity();
//			}
			ArrayList<Individual> copy = new ArrayList<Individual>(population);

			Individual first = new Individual(copy.remove(r.nextInt(copy.size())).getGenotypeVehicles());
			Individual second = new Individual(copy.remove(r.nextInt(copy.size())).getGenotypeVehicles());
			Individual third = new Individual(copy.remove(r.nextInt(copy.size())).getGenotypeVehicles());
			Individual fourth = new Individual(copy.remove(r.nextInt(copy.size())).getGenotypeVehicles());

//			for (Vehicles v : first.getGenotypeVehicles())
//				v.checkCapacity();
//			for (Vehicles v : second.getGenotypeVehicles())
//				v.checkCapacity();
//			for (Vehicles v : third.getGenotypeVehicles())
//				v.checkCapacity();
//			for (Vehicles v : fourth.getGenotypeVehicles())
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

//			for (Vehicles v : winning_first.getGenotypeVehicles())
//				v.checkCapacity();
//
//			for (Vehicles v : winning_second.getGenotypeVehicles())
//				v.checkCapacity();

			// System.out.println("FIRST \n" + winning_first + "\n SECOND \n" +
			// winning_second);

			// just because the following crossover operator suppose that the vehicles in
			// winning_first are more or equal than the ones in winning_second,
			// we swap the two if this is not the case
			if (winning_first.getGenotypeVehicles().size() < winning_second.getGenotypeVehicles().size()) {
				Individual toSwap = new Individual(winning_first.getGenotypeVehicles());
				winning_first = new Individual(winning_second.getGenotypeVehicles());
				winning_second = new Individual(toSwap.getGenotypeVehicles());
			}

//			for (Vehicles v : winning_first.getGenotypeVehicles())
//				v.checkCapacity();
//
//			for (Vehicles v : winning_second.getGenotypeVehicles())
//				v.checkCapacity();

			int childNumberOfVehicles = (winning_first.getGenotypeVehicles().size()
					+ winning_second.getGenotypeVehicles().size()) / 2;

			ArrayList<Vehicles> currentIndividualVehiclesFC = new ArrayList<Vehicles>();
			ArrayList<Vehicles> currentIndividualVehiclesSC = new ArrayList<Vehicles>();
			int crossOverPoint = r.nextInt(childNumberOfVehicles) + 1;

			for (int i = 0; i < childNumberOfVehicles; i++) {
				if (i < crossOverPoint) {
					currentIndividualVehiclesFC.add(new Vehicles(winning_first.getGenotypeVehicles().get(i)));
				} else {
					if (winning_second.getGenotypeVehicles().size() - 1 - (i - crossOverPoint) >= 0) {

						currentIndividualVehiclesFC.add(new Vehicles(winning_second.getGenotypeVehicles()
								.get(winning_second.getGenotypeVehicles().size() - 1 - (i - crossOverPoint))));
					} else {

						currentIndividualVehiclesFC.add(new Vehicles(
								winning_first.getGenotypeVehicles().get(winning_first.getGenotypeVehicles().size()
										+ (winning_second.getGenotypeVehicles().size() - 1 - (i - crossOverPoint)))));
					}

				}

				if (crossOverPoint + i < winning_first.getGenotypeVehicles().size()) {
					currentIndividualVehiclesSC
							.add(new Vehicles(winning_first.getGenotypeVehicles().get(crossOverPoint + i)));

				} else {

					currentIndividualVehiclesSC.add(new Vehicles(winning_second.getGenotypeVehicles()
							.get((crossOverPoint + i) % winning_first.getGenotypeVehicles().size())));
				}
			}

			Individual firstInd = new Individual(currentIndividualVehiclesFC);
			Individual secondInd = new Individual(currentIndividualVehiclesSC);

//			for (Vehicles v : firstInd.getGenotypeVehicles())
//				v.checkCapacity();
//			for (Vehicles v : secondInd.getGenotypeVehicles())
//				v.checkCapacity();

			// System.out.println("AFTER CROSSOVER \n FIRST \n" + firstInd + "\n SECOND \n"
			// + secondInd);

			recoverFeasibility(firstInd);
			recoverFeasibility(secondInd);

//			for (Vehicles v : firstInd.getGenotypeVehicles())
//				v.checkCapacity();
//			for (Vehicles v : secondInd.getGenotypeVehicles())
//				v.checkCapacity();

			// System.out.println("AFTER FEASIBILITY \n FIRST \n" + firstInd + "\n SECOND
			// \n" + secondInd);

			mutation(firstInd);
			mutation(secondInd);

//			for (Vehicles v : firstInd.getGenotypeVehicles())
//				v.checkCapacity();
//			for (Vehicles v : secondInd.getGenotypeVehicles())
//				v.checkCapacity();

			// System.out.println("AFTER MUTATION \n FIRST \n" + firstInd + "\n SECOND \n" +
			// secondInd);

			postOptimize(firstInd);
			postOptimize(secondInd);

//			for (Vehicles v : firstInd.getGenotypeVehicles())
//				v.checkCapacity();
//			for (Vehicles v : secondInd.getGenotypeVehicles())
//				v.checkCapacity();

			// System.out.println("AFTER OPTIMIZATION \n FIRST \n" + firstInd + "\n SECOND
			// \n" + secondInd);

			children.add(firstInd);
			children.add(secondInd);
//
//			for (Vehicles v : firstInd.getGenotypeVehicles())
//				v.checkCapacity();
//			for (Vehicles v : secondInd.getGenotypeVehicles())
//				v.checkCapacity();

		}
		return children;
	}

	private static void postOptimize(Individual s) {
		if (s.getGenotypeVehicles().size() == 1)
			return;

		// se un nodo è visitato più di una volta, lo lascio solo nel veicolo dove ha la
		// rotta per il drone più lunga
		for (int i = 1; i < numNodesInTheGraph; i++) {
			if (s.getVisited().contains(i)) {

				double mostProfitableTour = Double.MIN_VALUE;
				int maxTourVehicleIndex = -1;
				int howMany = 0;

				for (int j = 0; j < s.getGenotypeVehicles().size(); j++) {
					if (s.getGenotypeVehicles().get(j).getTour().contains(i)) {
						howMany++;
						if (s.getGenotypeVehicles().get(j).getCurrentDroneTourProfit().get(i) > mostProfitableTour) {
							mostProfitableTour = s.getGenotypeVehicles().get(j).getCurrentDroneTourProfit().get(i);
							maxTourVehicleIndex = j;
						}
					}
				}

				if (howMany >= 2) {
					for (int j = 0; j < s.getGenotypeVehicles().size(); j++) {
						if (j != maxTourVehicleIndex && s.getGenotypeVehicles().get(j).getTour().contains(i)) {
							s.getGenotypeVehicles().get(j).removeNode(i);
						}
					}
				}
			}
		}
//		System.out.println("BEFORE \n" + s.getGenotypeVehicles() + "\n END");
		Collections.sort(s.getGenotypeVehicles(), new Comparator<Vehicles>() {
			@Override
			public int compare(Vehicles o1, Vehicles o2) {
				if (o1.getTourLength() > o2.getTourLength())
					return 1;
				if (o1.getTourLength() == o2.getTourLength())
					return 0;

				return -1;
			}
		});

		boolean found = true;
		while (found) {
			System.out.println("UPDATED \n " + s.getGenotypeVehicles());
			found = false;
			// controllare che il tour non sia vuoto
			Vehicles busiest = s.getGenotypeVehicles().get(s.getGenotypeVehicles().size() - 1);
			if (busiest.getTour().size() <= 1)
				break;
			int toAssign = busiest.getTour().get(1);
			double droneTour = busiest.getCurrentDroneTourLength().get(toAssign);

//			System.out.println(busiest.getCurrentDroneTourLength());
			ArrayList<Integer> droneTourPath = busiest.getDroneTour().get(toAssign);
			double resourceNeeded = 0;
			for (Integer i : droneTourPath)
				resourceNeeded += graph.getNeededResource(i);

			for (int index = 0; index < s.getGenotypeVehicles().size() - 1; index++) {
				Vehicles v = s.getGenotypeVehicles().get(index);
				if (v.getCurrentCapacity() >= graph.getNeededResource(toAssign) + resourceNeeded && v.getTourLength()
						+ graph.getNormalizedDistance(v.getTour().get(v.getTour().size() - 1), toAssign)
						+ droneTour < busiest.getTourLength()) {

					Vehicles avoidLoop = new Vehicles(busiest);
					avoidLoop.removeNode(toAssign);
					if (avoidLoop.getTourLength() >= busiest.getTourLength())
						continue;

					System.out.println("freest " + v);
					System.out.println("busiest " + busiest);
					System.out.println("I'M ADDING " + toAssign + " in " + v);

					v.addNode(toAssign);
					v.addDronePath(toAssign, droneTourPath);
//					System.out.println("obtaining " + toAssign + " in " + v);
					busiest.removeNode(toAssign);
					found = true;

					int iter = index + 1;
					while (iter < s.getGenotypeVehicles().size()
							&& s.getGenotypeVehicles().get(iter).getTourLength() <= v.getTourLength())
						iter++;

					if (iter != index + 1) {
						for (int i = index; i < iter - 1; i++) {
							Vehicles swap = s.getGenotypeVehicles().get(i);
							s.getGenotypeVehicles().set(i, s.getGenotypeVehicles().get(i + 1));
							s.getGenotypeVehicles().set(i + 1, swap);
						}
					}

					// usato perchè se devo sostituirlo con l'ultimo, arriverò al secondo while con
					// l'elemento non più ultimo ma penultimo
					int stride = 0;
					if (iter == s.getGenotypeVehicles().size()) {
						stride = 1;
						busiest = s.getGenotypeVehicles().get(s.getGenotypeVehicles().size() - 2);
					}

					iter = s.getGenotypeVehicles().size() - 2;

					while (iter - stride >= 0
							&& s.getGenotypeVehicles().get(iter - stride).getTourLength() > busiest.getTourLength())
						iter--;
					if (iter != s.getGenotypeVehicles().size() - 2) {
						for (int i = s.getGenotypeVehicles().size() - 1 - stride; i > iter - stride + 1; i--) {
							Vehicles swap = s.getGenotypeVehicles().get(i);
							s.getGenotypeVehicles().set(i, s.getGenotypeVehicles().get(i - 1));
							s.getGenotypeVehicles().set(i - 1, swap);
						}
					}

					break;

				}
			}
		}

		System.out.println("AFTER \n" + s.getGenotypeVehicles() + "\n END");
		s.updateSolution(null);

	}

	private static void mutation(Individual individual) {

		// change position i with position j with probability equal to mutationProb
		for (Vehicles vehicle : individual.getGenotypeVehicles()) {
			for (int i = 1; i < vehicle.getTour().size() - 1; i++) {
				for (int j = i + 1; j < vehicle.getTour().size(); j++) {
					if (r.nextDouble() <= mutationProb) {
						vehicle.swap(i, j);

					}

				}
			}
		}

		// add node with probability mutationProb
		for (int i = 0; i < numNodesInTheGraph; i++) {
			if (r.nextDouble() > mutationProb)
				continue;
			int extractedRandomNode = copyNodes.get(r.nextInt(copyNodes.size()));
			if (individual.getVisited().contains(extractedRandomNode))
				continue;
			Vehicles extractedRandomVehicle = individual.getGenotypeVehicles()
					.get(r.nextInt(individual.getGenotypeVehicles().size()));
			if (extractedRandomVehicle.getCurrentCapacity() - graph.getNeededResource(extractedRandomNode) >= 0
					&& !extractedRandomVehicle.getTour().contains(extractedRandomNode)) {
				extractedRandomVehicle.addNode(extractedRandomNode);
			}
		}

		individual.updateSolution(null);

		double currentProfit = individual.getObjectiveValue()[2];
		// remove node with probability mutationProb
		for (int i = 0; i < numNodesInTheGraph; i++) {
			if (r.nextDouble() > mutationProb)
				continue;
			Vehicles extractedRandomVehicle = individual.getGenotypeVehicles()
					.get(r.nextInt(individual.getGenotypeVehicles().size()));

			if (extractedRandomVehicle.getTour().size() <= 1)
				continue;

			int extractedRandomNode = extractedRandomVehicle.getTour()
					.get(r.nextInt(extractedRandomVehicle.getTour().size() - 1) + 1);

			if (currentProfit - graph.getProfit(extractedRandomNode)
					- extractedRandomVehicle.getCurrentDroneTourProfit().get(extractedRandomNode) >= minProfitNeeded) {
				currentProfit -= graph.getProfit(extractedRandomNode);
				currentProfit -= extractedRandomVehicle.getCurrentDroneTourProfit().get(extractedRandomNode);
				extractedRandomVehicle.removeNode(extractedRandomNode);
			}
		}

		individual.updateSolution(null);

		boolean checked[] = new boolean[numExtraNodesForDroneInTheGraph];

		// add one drone node random
		for (int i = numNodesInTheGraph; i < numNodesInTheGraph + numExtraNodesForDroneInTheGraph; i++) {
			if (r.nextDouble() > mutationProb)
				continue;
			int extractedRandomNode = copyNodesDrones.get(r.nextInt(copyNodesDrones.size()));

			if (checked[extractedRandomNode - numNodesInTheGraph])
				continue;
			else
				checked[extractedRandomNode - numNodesInTheGraph] = true;

			if (individual.getVisited().contains(extractedRandomNode))
				continue;

			Vehicles extractedRandomVehicle = individual.getGenotypeVehicles()
					.get(r.nextInt(individual.getGenotypeVehicles().size()));
			if (extractedRandomVehicle.getTour().size() <= 1)
				continue;
			int extractedRandomNodeForAdding = extractedRandomVehicle.getTour()
					.get(r.nextInt(extractedRandomVehicle.getTour().size() - 1) + 1);
			if (extractedRandomVehicle.getCurrentDroneTourLength().get(extractedRandomNodeForAdding)
					+ getConsumption(2 * graph.getNormalizedDistance(extractedRandomNodeForAdding,
							extractedRandomNode)) <= maxBatteryConsumption
					&& extractedRandomVehicle.getCurrentCapacity() >= Main.graph
							.getNeededResource(extractedRandomNode)) {
				extractedRandomVehicle.addExtraNode(extractedRandomNodeForAdding, extractedRandomNode,
						getConsumption(
								2 * graph.getNormalizedDistance(extractedRandomNodeForAdding, extractedRandomNode)),
						graph.getProfit(extractedRandomNode));
			}

		}

		individual.updateSolution(null);

		currentProfit = individual.getObjectiveValue()[2];
		// remove droneNode with probability mutationProb
		for (int i = numNodesInTheGraph; i < numNodesInTheGraph + numExtraNodesForDroneInTheGraph; i++) {
			if (r.nextDouble() > mutationProb)
				continue;
			Vehicles extractedRandomVehicle = individual.getGenotypeVehicles()
					.get(r.nextInt(individual.getGenotypeVehicles().size()));

			if (extractedRandomVehicle.getTour().size() <= 1)
				continue;

			int extractedRandomNode = extractedRandomVehicle.getTour()
					.get(r.nextInt(extractedRandomVehicle.getTour().size() - 1) + 1);

			ArrayList<Integer> droneTourSelected = extractedRandomVehicle.getDroneTour().get(extractedRandomNode);

			if (droneTourSelected.size() == 0)
				continue;

			int extractedDroneNode = droneTourSelected.get(r.nextInt(droneTourSelected.size()));

			if (currentProfit - graph.getProfit(extractedDroneNode) >= minProfitNeeded) {
				extractedRandomVehicle.removeExtraNode(extractedRandomNode, extractedDroneNode,
						getConsumption(2 * graph.getNormalizedDistance(extractedRandomNode, extractedDroneNode)),
						graph.getProfit(extractedDroneNode));
				currentProfit -= graph.getProfit(extractedDroneNode);
			}
		}
		individual.updateSolution(null);

	}

	// consumo in funzione della distanza d
	public static Double getConsumption(double d) {
		return d;
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
			for (int j = 0; j < numMaxVehicles; j++)
				vehiclesIndexAvailable.add(j);

			// decidiamo il numero di veicoli da usare nella soluzione in modo random
			int numVehicles = r.nextInt(numMaxVehicles) + 1;

			ArrayList<Vehicles> vehiclesToUse = new ArrayList<Vehicles>();
			for (int j = 0; j < numVehicles; j++) {
				Vehicles choosen = vehicles
						.get(vehiclesIndexAvailable.remove(r.nextInt(vehiclesIndexAvailable.size())));
				vehiclesToUse.add(new Vehicles(choosen.getCapacity()));
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

				if (r.nextDouble() <= crossoverProb) {
					int selectedVehicles = r.nextInt(numVehicles);
					if (vehiclesToUse.get(selectedVehicles).getCurrentCapacity() >= graph
							.getNeededResource(selectedNode)) {
						vehiclesToUse.get(selectedVehicles).addNode(selectedNode);
					}
				}
			}

			HashMap<Pair, Double> energyConsumed = new HashMap<Pair, Double>();

			// add node for drone for random vehicles and random node as starting point
			for (int j = 0; j < numExtraNodesForDroneInTheGraph; j++) {
				int randomIndex = r.nextInt(numVehicles); // veicolo scelto random
				int sizeOfItsPath = vehiclesToUse.get(randomIndex).getTour().size();
				if (sizeOfItsPath <= 1)
					continue;
				int nodeRandom = vehiclesToUse.get(randomIndex).getTour().get(r.nextInt(sizeOfItsPath - 1) + 1);

				if (reachableUsingDrone.get(nodeRandom).size() == 0)
					continue;

				if (energyConsumed.get(new Pair(randomIndex, nodeRandom)) == null)
					energyConsumed.put(new Pair(randomIndex, nodeRandom), 0.);

				int destination = reachableUsingDrone.get(nodeRandom)
						.get(r.nextInt(reachableUsingDrone.get(nodeRandom).size()));
				// scegliamo un indice di partenza random e usiamo il modulo per scorrere in
				// modo ciclico la lista (da i a n e da 0 a i-1)

				if (getConsumption(2 * graph.getNormalizedDistance(nodeRandom, destination))
						+ energyConsumed.get(new Pair(randomIndex, nodeRandom)) <= maxBatteryConsumption
						&& nodesForTheDrones.contains(destination) && vehiclesToUse.get(randomIndex)
								.getCurrentCapacity() >= graph.getNeededResource(destination)) {
					nodesForTheDrones.remove(Integer.valueOf(destination));
					vehiclesToUse.get(randomIndex).addExtraNode(nodeRandom, destination,
							getConsumption(2 * graph.getNormalizedDistance(nodeRandom, destination)),
							graph.getProfit(destination));
					energyConsumed.replace(new Pair(randomIndex, nodeRandom),
							energyConsumed.get(new Pair(randomIndex, nodeRandom))
									+ getConsumption(2 * graph.getNormalizedDistance(nodeRandom, destination)));
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

		Collections.sort(toAdd.getGenotypeVehicles(), new Comparator<Vehicles>() {
			@Override
			public int compare(Vehicles o1, Vehicles o2) {
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

			for (int j = 0; j < toAdd.getGenotypeVehicles().size(); j++) {
				if (toAdd.getGenotypeVehicles().get(j).getCurrentCapacity() >= graph.getNeededResource(indexNode)) {

					toAdd.getGenotypeVehicles().get(j).addNode(indexNode);
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
			boolean inserted = false;
			int startingrandom = r.nextInt(toAdd.getGenotypeVehicles().size());
			for (int j = startingrandom; j < startingrandom + toAdd.getGenotypeVehicles().size() && !inserted; j++) {
				Vehicles vehicle = toAdd.getGenotypeVehicles().get(j % toAdd.getGenotypeVehicles().size());
				if (vehicle.getTour().size() <= 1)
					continue;
				for (Integer nodeForAdding : vehicle.getTour()) {
					if (nodeForAdding == 0)
						continue;
					if (vehicle.getCurrentDroneTourLength().get(nodeForAdding)
							+ getConsumption(2 * graph.getNormalizedDistance(nodeForAdding, i)) <= maxBatteryConsumption
							&& vehicle.getCurrentCapacity() >= Main.graph.getNeededResource(i)) {
						vehicle.addExtraNode(nodeForAdding, i,
								getConsumption(2 * graph.getNormalizedDistance(nodeForAdding, i)), graph.getProfit(i));
						inserted = true;
						added += Main.graph.getProfit(i);
						break;
					}
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

	private static void createRandomVehicles() {
		vehicles = new ArrayList<Vehicles>();
		for (int i = 0; i < numMaxVehicles; i++) {
			boolean capacity = r.nextBoolean();
			if (capacity)
				vehicles.add(new Vehicles(1000));
			else
				vehicles.add(new Vehicles(2000));
		}
	}

	private static void initReachableUsingDrone() {
		// per ogni nodo del grafo iniziale (0,..,numNodesInTheGraph) calcolo quali
		// punti extra riesco a raggiungere
		for (int i = 0; i < numNodesInTheGraph; i++) {
			reachableUsingDrone.put(i, new ArrayList<Integer>());
			for (int j = numNodesInTheGraph; j < numNodesInTheGraph + numExtraNodesForDroneInTheGraph; j++) {
				if (getConsumption(2 * graph.getNormalizedDistance(i, j)) <= maxBatteryConsumption) {
					reachableUsingDrone.get(i).add(j);
				}
			}
		}

	}
}
