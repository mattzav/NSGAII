import java.util.ArrayList;
import java.util.HashMap;

public class Vehicle {
	private int capacity;
	private int currentCapacity;
	private double tourLength;
	private ArrayList<Integer> tour;

	// se il numero di droni è fissato, si puo usare array list
	private HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> droneTour;
	private HashMap<Integer, Double> longestDroneTourForNode;
	private HashMap<Integer, HashMap<Integer, Double>> currentDroneTourLength;
	private HashMap<Integer, HashMap<Integer, Double>> currentDroneEnergyUsed;
	private HashMap<Integer, HashMap<Integer, Double>> currentDroneTourProfit;

	public Vehicle(int capacity) {
		this.capacity = capacity;
		this.currentCapacity = capacity;
		this.tourLength = 0;

		this.tour = new ArrayList<Integer>();
		this.tour.add(0);

		this.droneTour = new HashMap<Integer, HashMap<Integer, ArrayList<Integer>>>();
		this.longestDroneTourForNode = new HashMap<Integer, Double>();
		this.currentDroneTourLength = new HashMap<Integer, HashMap<Integer, Double>>();
		this.currentDroneEnergyUsed = new HashMap<Integer, HashMap<Integer, Double>>();
		this.currentDroneTourProfit = new HashMap<Integer, HashMap<Integer, Double>>();
		// checkCapacity();

	}

	public Vehicle(Vehicle v) {
		this.capacity = v.capacity;
		this.currentCapacity = v.getCurrentCapacity();
		this.tourLength = v.getTourLength();
		this.tour = new ArrayList<Integer>();
		for (Integer node : v.getTour())
			tour.add(node);

		this.droneTour = new HashMap<Integer, HashMap<Integer, ArrayList<Integer>>>();
		for (Integer keynode : v.getDroneTour().keySet()) {
			droneTour.put(keynode, new HashMap<Integer, ArrayList<Integer>>());
			for (Integer keydrone : v.getDroneTour().get(keynode).keySet()) {
				droneTour.get(keynode).put(keydrone, new ArrayList<Integer>());
				for (Integer node : v.getDroneTour().get(keynode).get(keydrone))
					droneTour.get(keynode).get(keydrone).add(node);
			}
		}

		this.longestDroneTourForNode = new HashMap<Integer, Double>();
		for (Integer keynode : v.getLongestDroneTourForNode().keySet()) {
			longestDroneTourForNode.put(keynode, v.getLongestDroneTourForNode().get(keynode));
		}

		this.currentDroneTourLength = new HashMap<Integer, HashMap<Integer, Double>>();
		for (Integer keynode : v.getCurrentDroneTourLength().keySet()) {
			currentDroneTourLength.put(keynode, new HashMap<Integer, Double>());
			for (Integer keydrone : v.getCurrentDroneTourLength().get(keynode).keySet()) {
				currentDroneTourLength.get(keynode).put(keydrone,
						v.getCurrentDroneTourLength().get(keynode).get(keydrone));
			}
		}

		this.currentDroneEnergyUsed = new HashMap<Integer, HashMap<Integer, Double>>();
		for (Integer keynode : v.getCurrentDroneEnergyUsed().keySet()) {
			currentDroneEnergyUsed.put(keynode, new HashMap<Integer, Double>());
			for (Integer keydrone : v.getCurrentDroneEnergyUsed().get(keynode).keySet()) {
				currentDroneEnergyUsed.get(keynode).put(keydrone,
						v.getCurrentDroneEnergyUsed().get(keynode).get(keydrone));
			}
		}

		this.currentDroneTourProfit = new HashMap<Integer, HashMap<Integer, Double>>();
		for (Integer keynode : v.getCurrentDroneTourProfit().keySet()) {
			currentDroneTourProfit.put(keynode, new HashMap<Integer, Double>());
			for (Integer keydrone : v.getCurrentDroneTourProfit().get(keynode).keySet()) {
				currentDroneTourProfit.get(keynode).put(keydrone,
						v.getCurrentDroneTourProfit().get(keynode).get(keydrone));
			}
		}

		// checkCapacity();
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getCurrentCapacity() {
		return currentCapacity;
	}

	public void setCurrentCapacity(int currentCapacity) {
		this.currentCapacity = currentCapacity;
	}

	public double getTourLength() {
		return tourLength;
	}

	public void setTourLength(double tourLength) {
		this.tourLength = tourLength;
	}

	public ArrayList<Integer> getTour() {
		return tour;
	}

	public void setTour(ArrayList<Integer> tour) {
		this.tour = tour;
	}

	public HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> getDroneTour() {
		return droneTour;
	}

	public void setDroneTour(HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> droneTour) {
		this.droneTour = droneTour;
	}

	public HashMap<Integer, Double> getLongestDroneTourForNode() {
		return longestDroneTourForNode;
	}

	public HashMap<Integer, HashMap<Integer, Double>> getCurrentDroneTourLength() {
		return currentDroneTourLength;
	}

	public void setCurrentDroneTourLength(HashMap<Integer, HashMap<Integer, Double>> currentDroneTourLength) {
		this.currentDroneTourLength = currentDroneTourLength;
	}

	public HashMap<Integer, HashMap<Integer, Double>> getCurrentDroneTourProfit() {
		return currentDroneTourProfit;
	}

	public HashMap<Integer, HashMap<Integer, Double>> getCurrentDroneEnergyUsed() {
		return currentDroneEnergyUsed;
	}

	public void setCurrentDroneTourProfit(HashMap<Integer, HashMap<Integer, Double>> currentDroneTourProfit) {
		this.currentDroneTourProfit = currentDroneTourProfit;
	}

	public void addNode(int selectedNode) {

		// checkCapacity();
		currentCapacity -= Main.graph.getNeededResource(selectedNode);
		tourLength += Main.graph.getDuration(tour.get(tour.size() - 1), selectedNode);
		tour.add(selectedNode);

		droneTour.put(selectedNode, new HashMap<Integer, ArrayList<Integer>>());
		longestDroneTourForNode.put(selectedNode, 0.);
		for (int i = 0; i < Main.numDronesPerVehicle; i++) {
			if (currentDroneTourLength.get(selectedNode) == null)
				currentDroneTourLength.put(selectedNode, new HashMap<Integer, Double>());

			if (currentDroneTourProfit.get(selectedNode) == null)
				currentDroneTourProfit.put(selectedNode, new HashMap<Integer, Double>());

			if (currentDroneEnergyUsed.get(selectedNode) == null)
				currentDroneEnergyUsed.put(selectedNode, new HashMap<Integer, Double>());

			currentDroneTourLength.get(selectedNode).put(i, 0.);
			currentDroneTourProfit.get(selectedNode).put(i, 0.);
			currentDroneEnergyUsed.get(selectedNode).put(i, 0.);
		}
		// checkCapacity();
	}

	// da aggiornare considerando piu droni
//	public void checkCapacity() {
//
//		double usedCapacity = 0;
//		for (Integer i : tour) {
//			usedCapacity += Main.graph.getNeededResource(i);
//			if (droneTour.get(i) == null)
//				continue;
//			for (Integer y : droneTour.get(i))
//				usedCapacity += Main.graph.getNeededResource(y);
//		}
//		if (usedCapacity + currentCapacity != capacity) {
//			System.out.println(usedCapacity + "  " + currentCapacity + " " + capacity);
//			System.out.println(toString());
//			throw new RuntimeException("problema");
//		}
//
//	}

	public void addExtraNode(Integer startingNode, int selectedNode, int selectedDrone, double normalizedDistance,
			double profit) {
		// checkCapacity();

		currentCapacity -= Main.graph.getNeededResource(selectedNode);
//
//		if (droneTour.get(startingNode).contains(selectedNode))
//			throw new RuntimeException("errore");

		if (droneTour.get(startingNode).get(selectedDrone) == null)
			droneTour.get(startingNode).put(selectedDrone, new ArrayList<Integer>());
		droneTour.get(startingNode).get(selectedDrone).add(selectedNode);

		double newLength = currentDroneTourLength.get(startingNode).get(selectedDrone) + normalizedDistance;
		currentDroneTourLength.get(startingNode).replace(selectedDrone, newLength);
		if (newLength > longestDroneTourForNode.get(startingNode)) {
			tourLength += (newLength - longestDroneTourForNode.get(startingNode));
			longestDroneTourForNode.replace(startingNode, newLength);
		}

		currentDroneTourProfit.get(startingNode).replace(selectedDrone,
				currentDroneTourProfit.get(startingNode).get(selectedDrone) + profit);

		currentDroneEnergyUsed.get(startingNode).replace(selectedDrone,
				currentDroneEnergyUsed.get(startingNode).get(selectedDrone)
						+ Main.getConsumption(startingNode, selectedNode));
		// checkCapacity();

	}

	public void addDronePath(int node, int drone, ArrayList<Integer> droneTourPath) {
		// checkCapacity();
		if (droneTourPath != null)
			for (Integer i : droneTourPath)
				addExtraNode(node, i, drone, 2 * Main.graph.getDuration(node, i), Main.graph.getProfit(i));
		// checkCapacity();

	}

	@Override
	public String toString() {
		String toAdd = "";
		for (Integer node : droneTour.keySet()) {
			toAdd += "  Node: " + node + ", Max Drone Tour: " + longestDroneTourForNode.get(node) + "\n";
			for (Integer droneNode : droneTour.get(node).keySet())
				toAdd += "   Drone: " + droneNode + ", Tour: " + droneTour.get(node).get(droneNode) + ", Length: "
						+ currentDroneTourLength.get(node).get(droneNode) + ", Consumption: "
						+ currentDroneEnergyUsed.get(node).get(droneNode) + "\n";
		}
		return "\n (" + capacity + "," + currentCapacity + ")" + tour + " LENGTH:" + tourLength + " \n Drone Tour: \n"
				+ toAdd;
	}

	public void swap(int i, int j) {
		// checkCapacity();
		Double duration = Main.graph.getDuration(tour.get(i - 1), tour.get(i));
		tourLength -= duration;

		duration = Main.graph.getDuration(tour.get(i), tour.get(i + 1));
		tourLength -= duration;

		if (j != i + 1) {
			duration = Main.graph.getDuration(tour.get(j - 1), tour.get(j));
			tourLength -= duration;
		}

		if (j < tour.size() - 1) {
			duration = Main.graph.getDuration(tour.get(j), tour.get(j + 1));
			tourLength -= duration;
		}
		duration = Main.graph.getDuration(tour.get(i - 1), tour.get(j));
		tourLength += duration;

		if (j != i + 1) {
			duration = Main.graph.getDuration(tour.get(j), tour.get(i + 1));
			tourLength += duration;

			duration = Main.graph.getDuration(tour.get(j - 1), tour.get(i));
			tourLength += duration;
		} else {
			duration = Main.graph.getDuration(tour.get(j), tour.get(i));
			tourLength += duration;
		}

		if (j < tour.size() - 1) {
			duration = Main.graph.getDuration(tour.get(i), tour.get(j + 1));
			tourLength += duration;
		}

		int swap = tour.get(i);
		tour.set(i, tour.get(j));
		tour.set(j, swap);
		// checkCapacity();
	}

	public void removeNode(int i) {
		// checkCapacity();
		Double duration = Main.graph.getDuration(tour.get(tour.indexOf(i) - 1), tour.get(tour.indexOf(i)));
		tourLength -= duration;

		if (tour.indexOf(i) < tour.size() - 1) {
			duration = Main.graph.getDuration(tour.get(tour.indexOf(i)), tour.get(tour.indexOf(i) + 1));
			tourLength -= duration;

			duration = Main.graph.getDuration(tour.get(tour.indexOf(i) - 1), tour.get(tour.indexOf(i) + 1));
			tourLength += duration;
		}

		currentCapacity += Main.graph.getNeededResource(i);
		for (int k = 0; k < Main.numDronesPerVehicle; k++)
			if (droneTour.get(i).get(k) != null)
				for (Integer visited : droneTour.get(i).get(k))
					currentCapacity += Main.graph.getNeededResource(visited);

		tour.remove(tour.indexOf(i));

		tourLength -= longestDroneTourForNode.get(i);// rimuovo la durata del tour del drone piu impegnato

		longestDroneTourForNode.remove(i);
		currentDroneTourLength.remove(i);
		currentDroneTourProfit.remove(i);
		currentDroneEnergyUsed.remove(i);
		droneTour.remove(i);
		// checkCapacity();

	}

	public void removeExtraNode(Integer k, int drone, int selectedNode, double normalizedDuration, double profit) {
		// checkCapacity();
		currentCapacity += Main.graph.getNeededResource(selectedNode);

		droneTour.get(k).get(drone).remove(Integer.valueOf(selectedNode));

		if (longestDroneTourForNode.get(k) == currentDroneTourLength.get(k).get(drone) && normalizedDuration != 0) {

			currentDroneTourLength.get(k).replace(drone, currentDroneTourLength.get(k).get(drone) - normalizedDuration);

			double maxTourLength = 0.;
			for (int drone_id = 0; drone_id < Main.numDronesPerVehicle; drone_id++)
				if (currentDroneTourLength.get(k).get(drone_id) > maxTourLength)
					maxTourLength = currentDroneTourLength.get(k).get(drone_id);
			tourLength -= (longestDroneTourForNode.get(k) - maxTourLength);
			longestDroneTourForNode.replace(k, maxTourLength);
		} else {
			currentDroneTourLength.get(k).replace(drone, currentDroneTourLength.get(k).get(drone) - normalizedDuration);
		}
		currentDroneTourProfit.get(k).replace(drone, currentDroneTourProfit.get(k).get(drone) - profit);
		currentDroneEnergyUsed.get(k).replace(drone,
				currentDroneEnergyUsed.get(k).get(drone) - Main.getConsumption(k, selectedNode));
		// checkCapacity();

	}
}
