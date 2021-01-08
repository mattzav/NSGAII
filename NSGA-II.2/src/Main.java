import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

public class Main {

	public static Random r = new Random();

	private static int populationSize = 20 * 2; // numero pari
	private static double mutationProb = 0.2;
	private static double crossoverProb = 0.9;
	private static int numIteration = 100;
	public static int numObjective = 3;
	private static double minProfitNeeded = 200;
	private static boolean[] maxOrMinForThatObjective = new boolean[] { true, true, false };
	private static double maxBatteryConsumption = 100;

	public static Graph graph;
	public static int numNodesInTheGraph = 50;
	private static int numExtraNodesForDroneInTheGraph = 20;
	private static int numMaxVehicles = 10;
	private static ArrayList<Vehicles> vehicles;

	private static ArrayList<Integer> copyNodes;
	private static ArrayList<Integer> copyNodesDrones;

	private static HashMap<Integer, ArrayList<Integer>> reachableUsingDrone;

	public static void main(String[] args) {
		copyNodes = new ArrayList<Integer>();
		reachableUsingDrone = new HashMap<Integer, ArrayList<Integer>>();
		graph = new Graph(numNodesInTheGraph + numExtraNodesForDroneInTheGraph);

		graph.print();

		initReachableUsingDrone(); // inizializziamo i punti potenzialmente raggiungibili dai droni per ogni nodo
		createRandomVehicles(); // creiamo i veicoli a disposizione con capacità random

		ArrayList<Individual> P = initPopulation(); // generiamo la prima popolazione random
		// System.out.println(P);

//		System.out.println("INITIAL POPULATION \n" + P);
		ArrayList<ArrayList<Individual>> F = fast_non_dominated_sort(P);
		for (int front = 0; front < F.size(); front++) {
			crowding_distance_assignment(F.get(front));
			System.out.println(" in the fronth " + front + " there are " + F.get(front));
		}

		while (numIteration-- > 0) {

			ArrayList<Individual> Q = generateChildren(P);

			ArrayList<Individual> union = new ArrayList<Individual>(Q);
			union.addAll(P);
			F = fast_non_dominated_sort(union);

			P = updatePopulation(F);

		}
		// System.out.println("FINAL POPULATION \n"+P);

		F = fast_non_dominated_sort(P);

		for (int front = 0; front < F.size(); front++) {
			System.out.println(" in the fronth " + front + " there are " + F.get(front));
//			System.out.println(F.get(front));
			// crowding_distance_assignment(F.get(front));
		}
		int cont = 0;
		for (Individual p : P) {
			p.updateSolution(null);
			if (p.getObjectiveValue()[2] < minProfitNeeded)
				cont++;
		}
		// System.out.println("there are " + cont + " infeasible");

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
		while (children.size() < populationSize) {
			ArrayList<Individual> copy = new ArrayList<Individual>(population); // evitare la copia usando solo gli
																				// indici

			Individual first = copy.remove(r.nextInt(copy.size()));
			Individual second = copy.remove(r.nextInt(copy.size()));
			Individual third = copy.remove(r.nextInt(copy.size()));
			Individual fourth = copy.remove(r.nextInt(copy.size()));

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

			System.out.println("FIRST \n" + winning_first + "\n SECOND \n" + winning_second);

			// just because the following crossover operator suppose that the vehicles in
			// winning_first are more or equal than the ones in winning_second,
			// we swap the two if this is not the case
			if (winning_first.getGenotypeVehicles().size() < winning_second.getGenotypeVehicles().size()) {
				Individual toSwap = new Individual(winning_first.getGenotypeVehicles());
				winning_first = new Individual(winning_second.getGenotypeVehicles());
				winning_second = new Individual(toSwap.getGenotypeVehicles());
			}

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

			System.out.println("AFTER CROSSOVER \n FIRST \n" + firstInd + "\n SECOND \n" + secondInd);

			recoverFeasibility(firstInd);
			recoverFeasibility(secondInd);

			System.out.println("AFTER FEASIBILITY \n FIRST \n" + firstInd + "\n SECOND \n" + secondInd);

			mutation(firstInd);
			mutation(secondInd);

			System.out.println("AFTER MUTATION \n FIRST \n" + firstInd + "\n SECOND \n" + secondInd);

			postOptimize(firstInd);
			postOptimize(secondInd);

			System.out.println("AFTER OPTIMIZATION \n FIRST \n" + firstInd + "\n SECOND \n" + secondInd);

			children.add(firstInd);
			children.add(secondInd);

		}
		return children;
	}

	private static void postOptimize(Individual s) {

		if (s.getGenotypeVehicles().size() == 1)
			return;

		// se un nodo è visitato più di una volta, lo lascio solo nel veicolo con rotta
		// più breve
		for (int i = 1; i < numNodesInTheGraph; i++) {
			if (s.getVisited().contains(i)) {

				double minTour = Double.MAX_VALUE;
				int minTourVehicleIndex = -1;
				int howMany = 0;

				for (int j = 0; j < s.getGenotypeVehicles().size(); j++) {
					if (s.getGenotypeVehicles().get(j).getTour().contains(i)) {
						howMany++;
						if (s.getGenotypeVehicles().get(j).getTourLength() < minTour) {
							minTour = s.getGenotypeVehicles().get(j).getTourLength();
							minTourVehicleIndex = j;
						}
					}
				}

				if (howMany >= 2) {
					for (int j = 0; j < s.getGenotypeVehicles().size(); j++) {
						if (j != minTourVehicleIndex && s.getGenotypeVehicles().get(j).getTour().contains(i)) {
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
//			System.out.println("UPDATED \n " + s.getGenotypeVehicles());
			found = false;
			// controllare che il tour non sia vuoto
			Vehicles busiest = s.getGenotypeVehicles().get(s.getGenotypeVehicles().size() - 1);
			if (busiest.getTour().size() <= 1)
				break;
			int toAssign = busiest.getTour().get(1); // prendo l'ultimo veicolo (rotta piu lunga)
			double droneTour = busiest.getCurrentDroneTourLength().get(toAssign);
//			System.out.println(busiest.getCurrentDroneTourLength());
			ArrayList<Integer> droneTourPath = busiest.getDroneTour().get(toAssign);

			for (int index = 0; index < s.getGenotypeVehicles().size()-1; index++) {
				Vehicles v = s.getGenotypeVehicles().get(index);
				if (v.getCurrentCapacity() >= graph.getNeededResource(toAssign) && v.getTourLength()
						+ graph.getNormalizedDistance(v.getTour().get(v.getTour().size() - 1), toAssign)
						+ droneTour < busiest.getTourLength()) {

					Vehicles avoidLoop = new Vehicles(busiest);
					avoidLoop.removeNode(toAssign);
					if (avoidLoop.getTourLength() >= busiest.getTourLength())
						continue;

					System.out.println("freest " + v);
					System.out.println("busiest " + busiest);
					System.out.println("I'M ADDING "+toAssign+ " in "+v);
					
					v.addNode(toAssign);
					v.addDronePath(toAssign, droneTourPath);
					System.out.println("obtaining "+toAssign+ " in "+v);
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

//		System.out.println("AFTER \n" + s.getGenotypeVehicles() + "\n END");
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
				extractedRandomVehicle.removeNode(extractedRandomNode);
				currentProfit -= graph.getProfit(extractedRandomNode);
			}
		}

		individual.updateSolution(null);

		// add one drone node random
		for (int i = numNodesInTheGraph; i < numNodesInTheGraph + numExtraNodesForDroneInTheGraph; i++) {
			if (r.nextDouble() > mutationProb)
				continue;
			int extractedRandomNode = copyNodesDrones.get(r.nextInt(copyNodesDrones.size()));
			if (individual.getVisited().contains(extractedRandomNode))
				continue;
			Vehicles extractedRandomVehicle = individual.getGenotypeVehicles()
					.get(r.nextInt(individual.getGenotypeVehicles().size()));
			if (extractedRandomVehicle.getTour().size() <= 1)
				continue;
			int extractedRandomNodeForAdding = extractedRandomVehicle.getTour()
					.get(r.nextInt(extractedRandomVehicle.getTour().size() - 1) + 1);
			if (extractedRandomVehicle.getCurrentDroneTourLength().get(extractedRandomNodeForAdding)
					+ graph.getNormalizedDistance(extractedRandomNodeForAdding,
							extractedRandomNode) <= maxBatteryConsumption) {
				extractedRandomVehicle.addExtraNode(extractedRandomNodeForAdding, extractedRandomNode,
						graph.getNormalizedDistance(extractedRandomNodeForAdding, extractedRandomNode),
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
						graph.getNormalizedDistance(extractedRandomNode, extractedDroneNode),
						graph.getProfit(extractedDroneNode));
				currentProfit -= graph.getProfit(extractedDroneNode);
			}
		}
		individual.updateSolution(null);

	}

	private static void recoverFeasibility(Individual current) {
		// sort vehicles depending on the length of the tour and assign,
		// until the feasibility is reached, the most profitable node to the most free
		// vehicle

		Collections.sort(current.getGenotypeVehicles(), new Comparator<Vehicles>() {
			@Override
			public int compare(Vehicles o1, Vehicles o2) {
				if (o1.getTourLength() < o2.getTourLength())
					return -1;
				else if (o1.getTourLength() == o2.getTourLength())
					return 0;
				return 1;
			}
		});

		// invece di usare current si potrebbe usare semplicemente vehicle to use e si
		// potrebbe eliminare il metodo getVisited in vehicles che è usato solo qui
		double added = 0;
		for (int indexNode = 1; indexNode < numNodesInTheGraph
				&& current.getObjectiveValue()[2] + added < minProfitNeeded; indexNode++) {
			if (current.getVisited().contains(indexNode))
				continue;

			for (int j = 0; j < current.getGenotypeVehicles().size(); j++) {
				if (current.getGenotypeVehicles().get(j).getCurrentCapacity() >= graph.getNeededResource(indexNode)) {

					current.getGenotypeVehicles().get(j).addNode(indexNode);
					added += graph.getProfit(indexNode);
					current.getVisited().add(indexNode);
					break;
				}

			}

		}
		current.updateSolution(null);
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
				vehiclesToUse.add(new Vehicles(choosen.getCapacity())); // PROVARE CON add(choosen)
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
					int selectedVehicles = r.nextInt(numVehicles);
					if (vehiclesToUse.get(selectedVehicles).getCurrentCapacity() >= graph
							.getNeededResource(selectedNode)) {
						vehiclesToUse.get(selectedVehicles).addNode(selectedNode);
					}
				}

			}

			// create tour for each vehicle and for each visited node
			for (int j = 0; j < numVehicles; j++) {
				for (Integer k : vehiclesToUse.get(j).getTour()) {
					if (k == 0 || reachableUsingDrone.get(k).size() == 0)
						continue;
					double energyConsumed = 0;
					int startingIndexRandom = r.nextInt(reachableUsingDrone.get(k).size());

					// scegliamo un indice di partenza random e usiamo il modulo per scorrere in
					// modo ciclio la lista (da i a n e da 0 a i-1)
					for (int index = startingIndexRandom; index < startingIndexRandom
							+ reachableUsingDrone.get(k).size(); index++) {
						int selectedNode = reachableUsingDrone.get(k).get(index % reachableUsingDrone.get(k).size());
						if (graph.getNormalizedDistance(k, selectedNode) + energyConsumed <= maxBatteryConsumption
								&& r.nextDouble() <= 0.5 && nodesForTheDrones.contains(selectedNode)) {
							nodesForTheDrones.remove(Integer.valueOf(selectedNode));
							vehiclesToUse.get(j).addExtraNode(k, selectedNode,
									graph.getNormalizedDistance(k, selectedNode), graph.getProfit(selectedNode));
							energyConsumed += graph.getNormalizedDistance(k, selectedNode);
						}
					}
				}
			}

			// manca la parte drone
			Individual toAdd = new Individual(vehiclesToUse);
			recoverFeasibility(toAdd);
			toReturn.add(toAdd);

		}
		return toReturn;
	}

	// creiamo una lista di copia e la ordiniamo sul profitto rispettoalla risorsa
	// richiesta
	private static void initCopyNodes(ArrayList<Integer> nodesInTheGraph) {
		copyNodes = new ArrayList<Integer>(nodesInTheGraph);

		// ordiniamo i nodi in base al profitto
		Collections.sort(copyNodes, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				if ((0.0001 + graph.getProfit(o1))
						/ (0.0001 + graph.getNeededResource(o1)) > (0.0001 + graph.getProfit(o2))
								/ (0.0001 + graph.getNeededResource(o2)))
					return -1;
				if ((0.0001 + graph.getProfit(o1))
						/ (0.0001 + graph.getNeededResource(o1)) < (0.0001 + graph.getProfit(o2))
								/ (0.0001 + graph.getNeededResource(o2)))
					return 1;

				return 0;
			}
		});
	}

	// creiamo numMaxVehicles con capacità random
	private static void createRandomVehicles() {
		vehicles = new ArrayList<Vehicles>();
		for (int i = 0; i < numMaxVehicles; i++) {
			boolean capacity = r.nextBoolean();
			if (capacity)
				vehicles.add(new Vehicles(500));
			else
				vehicles.add(new Vehicles(300));
		}

	}

	// per ogni nodo del grafo di base (0,..,numNodesInTheGraph) creiamo una lista
	// di nodi raggiungibili dal drone partendo da quel nodo.
	private static void initReachableUsingDrone() {

		// per ogni nodo del grafo iniziale (0,..,numNodesInTheGraph) calcolo quali
		// punti extra riesco a raggiungere
		for (int i = 0; i < numNodesInTheGraph; i++) {
			reachableUsingDrone.put(i, new ArrayList<Integer>());
			for (int j = numNodesInTheGraph; j < numNodesInTheGraph + numExtraNodesForDroneInTheGraph; j++) {
				if (graph.getNormalizedDistance(i, j) <= maxBatteryConsumption) {
					reachableUsingDrone.get(i).add(j);
				}
			}
		}
	}

}
