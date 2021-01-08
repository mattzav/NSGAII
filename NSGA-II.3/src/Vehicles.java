import java.util.ArrayList;
import java.util.HashMap;

public class Vehicles {
	private int capacity;
	private int currentCapacity;
	private double tourLength;
	private ArrayList<Integer> tour;

	private HashMap<Integer, ArrayList<Integer>> droneTour;
	private HashMap<Integer, Double> currentDroneTourLength;
	private HashMap<Integer, Double> currentDroneTourProfit;

	public Vehicles(int capacity) {
		this.capacity = capacity;
		this.currentCapacity = capacity;
		this.tourLength = 0;

		this.tour = new ArrayList<Integer>();
		this.tour.add(0);

		this.droneTour = new HashMap<Integer, ArrayList<Integer>>();
		this.currentDroneTourLength = new HashMap<Integer, Double>();
		this.currentDroneTourProfit = new HashMap<Integer, Double>();
		// checkCapacity();

	}

	public Vehicles(Vehicles v) {
		this.capacity = v.capacity;
		this.currentCapacity = v.getCurrentCapacity();
		this.tourLength = v.getTourLength();
		this.tour = new ArrayList<Integer>();
		for (Integer node : v.getTour())
			tour.add(node);

		this.droneTour = new HashMap<Integer, ArrayList<Integer>>();
		for (Integer key : v.getDroneTour().keySet()) {
			droneTour.put(key, new ArrayList<Integer>());
			for (Integer node : v.getDroneTour().get(key))
				droneTour.get(key).add(node);
		}

		this.currentDroneTourLength = new HashMap<Integer, Double>();
		for (Integer key : v.getCurrentDroneTourLength().keySet()) {
			currentDroneTourLength.put(key, v.getCurrentDroneTourLength().get(key));
		}

		this.currentDroneTourProfit = new HashMap<Integer, Double>();
		for (Integer key : v.getCurrentDroneTourProfit().keySet()) {
			currentDroneTourProfit.put(key, v.getCurrentDroneTourProfit().get(key));
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

	public HashMap<Integer, ArrayList<Integer>> getDroneTour() {
		return droneTour;
	}

	public void setDroneTour(HashMap<Integer, ArrayList<Integer>> droneTour) {
		this.droneTour = droneTour;
	}

	public HashMap<Integer, Double> getCurrentDroneTourLength() {
		return currentDroneTourLength;
	}

	public void setCurrentDroneTourLength(HashMap<Integer, Double> currentDroneTourLength) {
		this.currentDroneTourLength = currentDroneTourLength;
	}

	public HashMap<Integer, Double> getCurrentDroneTourProfit() {
		return currentDroneTourProfit;
	}

	public void setCurrentDroneTourProfit(HashMap<Integer, Double> currentDroneTourProfit) {
		this.currentDroneTourProfit = currentDroneTourProfit;
	}

	public void addNode(int selectedNode) {

		// checkCapacity();
		currentCapacity -= Main.graph.getNeededResource(selectedNode);
		tourLength += Main.graph.getNormalizedDistance(tour.get(tour.size() - 1), selectedNode);
		tour.add(selectedNode);

		droneTour.put(selectedNode, new ArrayList<Integer>());
		currentDroneTourLength.put(selectedNode, 0.);
		currentDroneTourProfit.put(selectedNode, 0.);
		// checkCapacity();
	}

	public void checkCapacity() {

		double usedCapacity = 0;
		for (Integer i : tour) {
			usedCapacity += Main.graph.getNeededResource(i);
			if (droneTour.get(i) == null)
				continue;
			for (Integer y : droneTour.get(i))
				usedCapacity += Main.graph.getNeededResource(y);
		}
		if (usedCapacity + currentCapacity != capacity) {
			System.out.println(usedCapacity + "  " + currentCapacity + " " + capacity);
			System.out.println(toString());
			throw new RuntimeException("problema");
		}

	}

	public void addExtraNode(Integer k, int selectedNode, double normalizedDistance, double profit) {
		// checkCapacity();
		tourLength += normalizedDistance;

		currentCapacity -= Main.graph.getNeededResource(selectedNode);

		if (droneTour.get(k).contains(selectedNode))
			throw new RuntimeException("errore");

		droneTour.get(k).add(selectedNode);
		currentDroneTourLength.replace(k, currentDroneTourLength.get(k) + normalizedDistance);
		currentDroneTourProfit.replace(k, currentDroneTourProfit.get(k) + profit);
		// checkCapacity();

	}

	public void addDronePath(int newNode, ArrayList<Integer> droneTourPath) {
		// checkCapacity();
		for (Integer i : droneTourPath)
			addExtraNode(newNode, i, Main.getConsumption(2 * Main.graph.getNormalizedDistance(newNode, i)),
					Main.graph.getProfit(i));
		// checkCapacity();

	}

	@Override
	public String toString() {
		return "\n (" + capacity + "," + currentCapacity + ")" + tour + " LENGTH:" + tourLength + " \n Drone Tour:"
				+ droneTour;
	}

	public void swap(int i, int j) {
		// checkCapacity();
		DurationParameters duration = Main.graph.getDuration(tour.get(i - 1), tour.get(i));
		tourLength -= duration.getExpectedDuration() * 0.75;
		tourLength -= duration.getVarianceDuration() * 0.25;

		duration = Main.graph.getDuration(tour.get(i), tour.get(i + 1));
		tourLength -= duration.getExpectedDuration() * 0.75;
		tourLength -= duration.getVarianceDuration() * 0.25;

		if (j != i + 1) {
			duration = Main.graph.getDuration(tour.get(j - 1), tour.get(j));
			tourLength -= duration.getExpectedDuration() * 0.75;
			tourLength -= duration.getVarianceDuration() * 0.25;
		}

		if (j < tour.size() - 1) {
			duration = Main.graph.getDuration(tour.get(j), tour.get(j + 1));
			tourLength -= duration.getExpectedDuration() * 0.75;
			tourLength -= duration.getVarianceDuration() * 0.25;
		}
		duration = Main.graph.getDuration(tour.get(i - 1), tour.get(j));
		tourLength += duration.getExpectedDuration() * 0.75;
		tourLength += duration.getVarianceDuration() * 0.25;

		if (j != i + 1) {
			duration = Main.graph.getDuration(tour.get(j), tour.get(i + 1));
			tourLength += duration.getExpectedDuration() * 0.75;
			tourLength += duration.getVarianceDuration() * 0.25;

			duration = Main.graph.getDuration(tour.get(j - 1), tour.get(i));
			tourLength += duration.getExpectedDuration() * 0.75;
			tourLength += duration.getVarianceDuration() * 0.25;
		} else {
			duration = Main.graph.getDuration(tour.get(j), tour.get(i));
			tourLength += duration.getExpectedDuration() * 0.75;
			tourLength += duration.getVarianceDuration() * 0.25;
		}

		if (j < tour.size() - 1) {
			duration = Main.graph.getDuration(tour.get(i), tour.get(j + 1));
			tourLength += duration.getExpectedDuration() * 0.75;
			tourLength += duration.getVarianceDuration() * 0.25;
		}

		int swap = tour.get(i);
		tour.set(i, tour.get(j));
		tour.set(j, swap);
		// checkCapacity();
	}

	public void removeNode(int i) {
		// checkCapacity();
		DurationParameters duration = Main.graph.getDuration(tour.get(tour.indexOf(i) - 1), tour.get(tour.indexOf(i)));
		tourLength -= duration.getExpectedDuration() * 0.75;
		tourLength -= duration.getVarianceDuration() * 0.25;

		if (tour.indexOf(i) < tour.size() - 1) {
			duration = Main.graph.getDuration(tour.get(tour.indexOf(i)), tour.get(tour.indexOf(i) + 1));
			tourLength -= duration.getExpectedDuration() * 0.75;
			tourLength -= duration.getVarianceDuration() * 0.25;

			duration = Main.graph.getDuration(tour.get(tour.indexOf(i) - 1), tour.get(tour.indexOf(i) + 1));
			tourLength += duration.getExpectedDuration() * 0.75;
			tourLength += duration.getVarianceDuration() * 0.25;
		}
		currentCapacity += Main.graph.getNeededResource(i);
		for (Integer visited : droneTour.get(i))
			currentCapacity += Main.graph.getNeededResource(visited);

		tour.remove(tour.indexOf(i));

		tourLength -= currentDroneTourLength.get(i); // rimuovo la durata del tour del drone in quel nodo dato che non
														// ci sarà più

		currentDroneTourLength.remove(i);
		currentDroneTourProfit.remove(i);
		droneTour.remove(i);
		// checkCapacity();

	}

	public void removeExtraNode(Integer k, int selectedNode, double normalizedDuration, double profit) {
		// checkCapacity();
		currentCapacity += Main.graph.getNeededResource(selectedNode);
		tourLength -= normalizedDuration;

		droneTour.get(k).remove(Integer.valueOf(selectedNode));
		currentDroneTourLength.replace(k, currentDroneTourLength.get(k) - normalizedDuration);
		currentDroneTourProfit.replace(k, currentDroneTourProfit.get(k) - profit);

		// checkCapacity();

	}
}
