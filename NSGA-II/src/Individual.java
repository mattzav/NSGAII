import java.util.ArrayList;
import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

public class Individual {

	// private ArrayList<ArrayList<Integer>> genotypeRoutes;
	private ArrayList<Vehicles> genotypeVehicles;

	private double objectiveValue[] = new double[Main.numObjective];

	private int front;
	private double crowdDistance;
	private ArrayList<Individual> dominated = new ArrayList<Individual>();
	private int dominatedBy = 0;

	private Set<Integer> visited;

	public Individual(ArrayList<Vehicles> v) {
		visited = new TreeSet<Integer>();
		updateSolution(v);
	}

	public void setCrowdDistance(double maxValue) {
		this.crowdDistance = maxValue;
	}

	public double getCrowdDistance() {
		return crowdDistance;
	}

	public int getDominatedBy() {
		return dominatedBy;
	}

	public void setDominatedBy(int dominatedBy) {
		this.dominatedBy = dominatedBy;
	}

	public int getFront() {
		return front;
	}

	/*
	 * public ArrayList<ArrayList<Integer>> getGenotypeRoutes() { return
	 * genotypeRoutes; }
	 */

	public ArrayList<Individual> getDominated() {
		return dominated;
	}

	public void setDominated(ArrayList<Individual> dominated) {
		this.dominated = dominated;
	}

	public void setObjectiveValue(double[] objectiveValue) {
		this.objectiveValue = objectiveValue;
	}

	public double[] getObjectiveValue() {
		return objectiveValue;
	}

	public void setFront(int front) {
		this.front = front;
	}

	public void setGenotype(ArrayList<ArrayList<Integer>> g, ArrayList<Vehicles> v) {
		updateSolution(v);
	}

	private void updateSolution(ArrayList<Vehicles> v) {
		
		genotypeVehicles = v;
		objectiveValue[0] = v.size(); // numVehicles

		visited = new TreeSet<Integer>();
		
		
		double maxLengthTour = Double.MIN_VALUE;
		for (int j = 0; j < v.size(); j++) {
			visited.addAll(v.get(j).getTour());
			if (v.get(j).getTourLength() > maxLengthTour) {
				maxLengthTour = v.get(j).getTourLength();
			}
		}
//		System.out.println(v + " MAX LENGTH: "+maxLengthTour);
		objectiveValue[1] = maxLengthTour;

		for (int j = 0; j < v.size(); j++) {
			for (Integer node : v.get(j).getTour()) {
				if (node != 0) {
					visited.addAll(v.get(j).getDroneTour().get(node));
				}
			}
		}
		double profit = 0;
		for (Integer i : visited) {
			profit += Main.graph.getProfit(i);
		}

		objectiveValue[2] = profit;
	}

	public Set<Integer> getVisited() {
		return visited;
	}

	@Override
	public String toString() {
		return "\n" + "SOL: \n" + "Vehicles " + genotypeVehicles + "\n" + "Num veic " + objectiveValue[0] + "\n"
				+ "Max Tour " + objectiveValue[1] + "\n" + "Profit " + objectiveValue[2] + "\n";
	}

	public ArrayList<Vehicles> getGenotypeVehicles() {
		return genotypeVehicles;
	}
}
