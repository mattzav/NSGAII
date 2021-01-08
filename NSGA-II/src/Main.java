import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import javax.management.RuntimeErrorException;

//NOTE: invece di arraylist usare array
// rendere la matrice simmetrica e "transitiva"= C(X,Z) = C(X,Y)+C(Y,Z)

// se non riesco a renderla ammissibile prendo un veicolo non ancora utilizzato e lo inserisco e lo riempio

// Si nota come all'aumentare delle iterazioni, il numero di front diminuisce e tendono ad essere tutte soluzioni non dominate (pareto ottime)
public class Main {

	public static Random r = new Random();

	private static int populationSize = 2 * 2; // numero pari
	private static double mutationProb = 0.2;

	private static double crossoverProb = 0.9;
	private static int numIteration = 10;
	public static int numObjective = 3;
	private static int geneLength = 1;

	public static Graph graph;

	public static int numNodesInTheGraph = 10;
	private static int numExtraNodesForDroneInTheGraph = 5;
	private static int numMaxVehicles = 10;
	private static ArrayList<Vehicles> vehicles;

	private static boolean[] maxOrMinForThatObjective = new boolean[] { true, true, false };

	private static double minProfitNeeded = 5;

	private static ArrayList<Integer> copyNodes;

	private static HashMap<Integer, ArrayList<Integer>> reachableUsingDrone;

	private static double maxBatteryConsumption = 20;

	public static void main(String[] args) {
		copyNodes = new ArrayList<Integer>();
		reachableUsingDrone = new HashMap<Integer, ArrayList<Integer>>();
		graph = new Graph(numNodesInTheGraph + numExtraNodesForDroneInTheGraph); // init random graph
		initReachableUsingDrone();

		// create numMaxVehicles with random capacity
		vehicles = new ArrayList<Vehicles>();
		for (int i = 0; i < numMaxVehicles; i++) {
			int capacity = r.nextInt(300) + 1;
			vehicles.add(new Vehicles(capacity));
		}
		//

		// System.out.println("Vehicles " + vehicles);
//		graph.print();

		ArrayList<Individual> P = initPopulation(); // generate random population
//		 System.out.println("First population" + P);

		ArrayList<ArrayList<Individual>> F = fast_non_dominated_sort(P);
		for (int front = 0; front < F.size(); front++) {
//			 System.out.println("In the front " + front + " there are " +
//			 F.get(front).size() + " solution");
//			 System.out.println(F.get(front));
			crowding_distance_assignment(F.get(front));
		}

		while (numIteration-- > 0) {

			ArrayList<Individual> Q = generateChildren(P);
			ArrayList<Individual> union = new ArrayList<Individual>(Q);
			union.addAll(P);
			F = fast_non_dominated_sort(union);

			P = updatePopulation(F);

		}

		F = fast_non_dominated_sort(P);

		for (int front = 0; front < F.size(); front++) {
//			System.out.println("In the front " + front + " there are " + F.get(front).size() + " solution");
//			System.out.println(F.get(front));
			crowding_distance_assignment(F.get(front));
		}

		System.out.println("last population " + P);
	}

	private static void initReachableUsingDrone() {

		// per ogni nodo del grafo iniziale (0,..,numNodesInTheGraph) calcolo quali
		// punti extra riesco a raggiungere
		for (int i = 0; i < numNodesInTheGraph; i++) {
			reachableUsingDrone.put(i, new ArrayList<Integer>());
			for (int j = numNodesInTheGraph; j < numNodesInTheGraph + numExtraNodesForDroneInTheGraph; j++) {
				if (graph.getNormalizedDuration(i, j) <= maxBatteryConsumption) {
					reachableUsingDrone.get(i).add(j);
				}
			}
		}
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

	// update population using front and, eventually, compute and use the crowd
	// distance for the i-th front
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

//		System.out.println("ordinati");
//		for(Individual ind:union.get(index))
//			System.out.println(ind.getCrowdDistance());

		int i = 0;
		while (toReturn.size() < populationSize) {
			toReturn.add(union.get(index).get(i));
			i++;
		}

//		System.out.println("MANTAINING");
//		for (Individual a : toReturn)
//			System.out.println(a);
//
//		System.out.println("END MANTAINING");

		return toReturn;
	}

	// generate children using tournament selection, crossover and mutation
	private static ArrayList<Individual> generateChildren(ArrayList<Individual> population) {
		ArrayList<Individual> children = new ArrayList<Individual>();

		while (children.size() < populationSize) {
			ArrayList<Individual> copy = new ArrayList<Individual>(population);

			Individual first = copy.remove(r.nextInt(copy.size()));
			Individual second = copy.remove(r.nextInt(copy.size()));
			Individual third = copy.remove(r.nextInt(copy.size()));
			Individual fourth = copy.remove(r.nextInt(copy.size()));

			Individual winning_first = null, winning_second = null;

			if (first.getFront() < second.getFront()
					|| (first.getFront() == second.getFront() && first.getCrowdDistance() > second.getCrowdDistance()))
				winning_first = new Individual(first.getGenotypeVehicles());
			else
				winning_first = new Individual(second.getGenotypeVehicles());

			if (third.getFront() < fourth.getFront()
					|| (third.getFront() == fourth.getFront() && third.getCrowdDistance() > fourth.getCrowdDistance()))
				winning_second = new Individual(third.getGenotypeVehicles());
			else
				winning_second = new Individual(fourth.getGenotypeVehicles());

			System.out.println("WINNING");
			System.out.println("WF" + winning_first);
			System.out.println("WS" + winning_second);

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

//			System.out.println("CROSSOVER POINT " + crossOverPoint);

			for (int i = 0; i < childNumberOfVehicles; i++) {
				if (i < crossOverPoint) {
					currentIndividualVehiclesFC.add(winning_first.getGenotypeVehicles().get(i));
				} else {
					if (winning_second.getGenotypeVehicles().size() - 1 - (i - crossOverPoint) >= 0) {

						currentIndividualVehiclesFC.add(winning_second.getGenotypeVehicles()
								.get(winning_second.getGenotypeVehicles().size() - 1 - (i - crossOverPoint)));
					} else {

						currentIndividualVehiclesFC
								.add(winning_first.getGenotypeVehicles().get(winning_first.getGenotypeVehicles().size()
										+ (winning_second.getGenotypeVehicles().size() - 1 - (i - crossOverPoint))));
					}

				}

				if (crossOverPoint + i < winning_first.getGenotypeVehicles().size()) {
					currentIndividualVehiclesSC.add(winning_first.getGenotypeVehicles().get(crossOverPoint + i));

				} else {

					currentIndividualVehiclesSC.add(winning_second.getGenotypeVehicles()
							.get((crossOverPoint + i) % winning_first.getGenotypeVehicles().size()));
				}
			}

			Individual firstInd = new Individual(currentIndividualVehiclesFC);
			Individual secondInd = new Individual(currentIndividualVehiclesSC);

			System.out.println("STARTING");
			System.out.println(firstInd);
			System.out.println(secondInd);

			Individual firstInd_ = new Individual(recoverFeasibility(firstInd));
			Individual secondInd_ = new Individual(recoverFeasibility(secondInd));
			System.out.println("AFTER FEASIBILITY, \n First" + firstInd_ + " \n Second" + secondInd_);

			//inizia il problema perche cambia i valori dei veicoli ma non la funzione obiettivo dell'individuo
			
//			System.out.println("AA \n" + firstInd);
			Individual firstInd__ = new Individual(mutation(firstInd_));
			Individual secondInd__ = new Individual(mutation(secondInd_));
//			System.out.println("BB \n" + firstInd);
//
			System.out.println("AFTER MUTATION,\n FIRST CHILD " + firstInd__ + "\n" + "SECOND CHILD" + secondInd__);
//
//			firstInd = postOptimize(firstInd);
//			secondInd = postOptimize(secondInd);

//			System.out
//					.println("After post optimization,\n  FIRST CHILD " + firstInd + "\n" + "SECOND CHILD" + secondInd);
			children.add(firstInd__);
			children.add(secondInd__);

		}
		return children;

	}

	// if two or more vehicles visit the same node, leave it just in the one having
	// the easiest tour
	private static Individual postOptimize(Individual s) {
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

		// aggiungere lo stesso codice per evitare che un nodo per drone venga visitato
		// due volte

		return new Individual(s.getGenotypeVehicles());
	}

	private static ArrayList<Vehicles> mutation(Individual individual) {

//		System.out.println("BEFORE \n"+individual);

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

//		// add node with probability mutationProb
//		for (int i = 0; i < numNodesInTheGraph; i++) {
//			if (r.nextDouble() > mutationProb)
//				continue;
//			int extractedRandomNode = copyNodes.get(r.nextInt(copyNodes.size()));
//			Vehicles extractedRandomVehicle = individual.getGenotypeVehicles()
//					.get(r.nextInt(individual.getGenotypeVehicles().size()));
//			if (extractedRandomVehicle.getCurrentCapacity() - graph.getNeededResource(extractedRandomNode) >= 0
//					&& !extractedRandomVehicle.getTour().contains(extractedRandomNode)) {
//				extractedRandomVehicle.addNode(extractedRandomNode);
//			}
//		}

//		Individual current = new Individual(individual.getGenotypeVehicles());
//
//		// remove node with probability mutationProb
//		for (int i = 0; i < graph.getNumNodes(); i++) {
//			if (r.nextDouble() > mutationProb)
//				continue;
//			Vehicles extractedRandomVehicle = current.getGenotypeVehicles()
//					.get(r.nextInt(current.getGenotypeVehicles().size()));
//
//			if (extractedRandomVehicle.getTour().size() <= 1)
//				continue;
//
//			int extractedRandomNode = extractedRandomVehicle.getTour()
//					.get(r.nextInt(extractedRandomVehicle.getTour().size() - 1) + 1);
//
//			if (current.getObjectiveValue()[2] - graph.getProfit(extractedRandomNode)
//					- extractedRandomVehicle.getCurrentDroneTourProfit().get(extractedRandomNode) >= minProfitNeeded) {
//				extractedRandomVehicle.removeNode(extractedRandomNode);
//			}
//		}

//		System.out.println(individual.getGenotypeVehicles());

		return individual.getGenotypeVehicles();
	}

	public static ArrayList<Vehicles> recoverFeasibility(Individual current) {
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

		return current.getGenotypeVehicles();
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

			// decide number of vehicle for that solution
			ArrayList<Vehicles> vehiclesToUse = new ArrayList<Vehicles>();
			int numVehicles = r.nextInt(numMaxVehicles) + 1;

			ArrayList<Integer> vehiclesAvailable = new ArrayList<Integer>();
			for (int j = 0; j < numMaxVehicles; j++)
				vehiclesAvailable.add(j);

			for (int j = 0; j < numVehicles; j++) {
				Vehicles v = vehicles.get(vehiclesAvailable.remove(r.nextInt(vehiclesAvailable.size())));
				vehiclesToUse.add(new Vehicles(v.getCapacity()));
			}

			// ArrayList<ArrayList<Integer>> currentIndividualRoutes = new
			// ArrayList<ArrayList<Integer>>();

			ArrayList<Integer> nodesInTheGraph = new ArrayList<Integer>();
			ArrayList<Integer> nodesForTheDrones = new ArrayList<Integer>();

			for (int j = 1; j < numNodesInTheGraph; j++)
				nodesInTheGraph.add(j);

			// solo alla prima iterazione creo la lista e la ordino sul profitto
			if (i == 0) {
				copyNodes = new ArrayList<Integer>(nodesInTheGraph);
				// sort nodes depending on profit
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
			// with probability equal to 0.8, assign one node to a random vehicles (we avoid
			// to assign to times the same node to different vehicles that is useless)
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

			for (int j = numNodesInTheGraph; j < numNodesInTheGraph + numExtraNodesForDroneInTheGraph; j++)
				nodesForTheDrones.add(j);

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
						if (graph.getNormalizedDuration(k, selectedNode) + energyConsumed <= maxBatteryConsumption
								&& r.nextDouble() <= 0.5 && nodesForTheDrones.contains(selectedNode)) {
							nodesForTheDrones.remove(Integer.valueOf(selectedNode));
							vehiclesToUse.get(j).addExtraNode(k, selectedNode,
									graph.getNormalizedDuration(k, selectedNode), graph.getProfit(selectedNode));
							energyConsumed += graph.getNormalizedDuration(k, selectedNode);
						}
					}
				}
			}

			// feasibility
			toReturn.add(new Individual(recoverFeasibility(new Individual(vehiclesToUse)))); // create again the
																								// individual to update
																								// each value not
			// update in the last phase
		}

		return toReturn;
	}
}
