import java.util.ArrayList;
import java.util.HashMap;

public class Vehicles {

	private int capacity;
	private int currentCapacity;  
	private double tourLength; 
	private ArrayList<Integer> tour;  
	private boolean visited[]; 

	private HashMap<Integer, ArrayList<Integer>> droneTour; 
	private HashMap<Integer, Double> currentDroneTourLength; 
	private HashMap<Integer, Double> currentDroneTourProfit;

	public Vehicles(int capacity, ArrayList<Integer> tourNew) {

		this.droneTour = new HashMap<Integer, ArrayList<Integer>>(); // controllare se bisogna inizializzare con un dato
																		// proveniente come parametro
		this.currentDroneTourLength = new HashMap<Integer, Double>();
		this.currentDroneTourProfit = new HashMap<Integer, Double>();

		this.capacity = capacity;
		this.visited = new boolean[Main.graph.getNumNodes()];
		this.currentCapacity = capacity;

		tour = new ArrayList<Integer>();
		tourLength = 0;

		for (int k = 0; k < tourNew.size() - 1; k++) {
			if (tour.contains(tourNew.get(k+1)))
				throw new RuntimeException("errrror");
				
			tour.add(tourNew.get(k+1));
			droneTour.put(tourNew.get(k+1), new ArrayList<Integer>());

			currentDroneTourLength.put(tourNew.get(k+1), 0.);
			currentDroneTourProfit.put(tourNew.get(k+1), 0.);

			DurationParameters duration = Main.graph.getDuration(tourNew.get(k), tourNew.get(k + 1));
			tourLength += duration.getExpectedDuration() * 0.75;
			tourLength += duration.getVarianceDuration() * 0.25;

			currentCapacity -= Main.graph.getNeededResource(k + 1);
			visited[tourNew.get(k + 1)] = true;
		}

	}

	public Vehicles(int capacity) {

		this.capacity = capacity;
		this.visited = new boolean[Main.graph.getNumNodes()];
		this.currentCapacity = capacity;
		this.droneTour = new HashMap<Integer, ArrayList<Integer>>();
		this.currentDroneTourLength = new HashMap<Integer, Double>();
		this.currentDroneTourProfit = new HashMap<Integer, Double>();

		tour = new ArrayList<Integer>();
		tour.add(0);
		tourLength = 0;

	}

	public ArrayList<Integer> getTour() {
		return tour;
	}

	public int getCurrentCapacity() {
		return currentCapacity;
	}

	public void setCurrentCapacity(int currentCapacity) {
		this.currentCapacity = currentCapacity;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public HashMap<Integer, ArrayList<Integer>> getDroneTour() {
		return droneTour;
	}

	public HashMap<Integer, Double> getCurrentDroneTourProfit() {
		return currentDroneTourProfit;
	}

	@Override
	public String toString() {
		return "\n (" + capacity + "," + currentCapacity + ")" + tour + " LENGTH:" + tourLength + " \n Drone Tour:"
				+ droneTour;
	}

	public double getTourLength() {
		return tourLength;
	}

	public void addNode(int selectedNode) {

		if(selectedNode>Main.numNodesInTheGraph)
			throw new RuntimeException("TOO BIG");
		
		if(tour.contains(selectedNode))
			throw new RuntimeException("errorrrrrr");
		
		tour.add(selectedNode);
		droneTour.put(selectedNode, new ArrayList<Integer>());

		currentDroneTourLength.put(selectedNode, 0.);
		currentDroneTourProfit.put(selectedNode, 0.);

		DurationParameters duration = Main.graph.getDuration(tour.get(tour.size() - 2), selectedNode);
		tourLength += duration.getExpectedDuration() * 0.75;
		tourLength += duration.getVarianceDuration() * 0.25;

		currentCapacity -= Main.graph.getNeededResource(selectedNode);
		visited[selectedNode] = true;
	}

	public void swap(int i, int j) {
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
	}

	public void removeNode(int i) {
		visited[i] = false;

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

		tour.remove(tour.indexOf(i));

		tourLength -= currentDroneTourLength.get(i); // rimuovo la durata del tour del drone in quel nodo dato che non
														// ci sarà più

		currentDroneTourLength.remove(i);
		currentDroneTourProfit.remove(i);
		droneTour.remove(i);
	}

	public void addExtraNode(Integer k, int selectedNode, double normalizedDuration, double profit) {
		droneTour.get(k).add(selectedNode);
		currentDroneTourLength.replace(k, currentDroneTourLength.get(k) + normalizedDuration);
		tourLength += normalizedDuration;
		currentDroneTourProfit.replace(k, currentDroneTourProfit.get(k) + profit);
	}
}
