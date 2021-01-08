import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class Individual {
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

	public void updateSolution(ArrayList<Vehicles> v) {
		if (v != null) {
			genotypeVehicles = new ArrayList<Vehicles>();
			for (Vehicles veic : v)
				genotypeVehicles.add(new Vehicles(veic));

		}
		objectiveValue[0] = genotypeVehicles.size(); // numVehicles

		visited = new TreeSet<Integer>();

		double maxLengthTour = 0;
		for (int j = 0; j < genotypeVehicles.size(); j++) {
			visited.addAll(genotypeVehicles.get(j).getTour());
			if (genotypeVehicles.get(j).getTourLength() > maxLengthTour) {
				maxLengthTour = genotypeVehicles.get(j).getTourLength();
			}
		}
//		System.out.println(v + " MAX LENGTH: "+maxLengthTour);
		objectiveValue[1] = maxLengthTour;

		for (int j = 0; j < genotypeVehicles.size(); j++) {
			visited.addAll(genotypeVehicles.get(j).getTour());
			for (Integer node : genotypeVehicles.get(j).getTour()) {
				if (node != 0) {
					visited.addAll(genotypeVehicles.get(j).getDroneTour().get(node));
				}
			}
		}

		double profit = 0;
		for (Integer i : visited)
			profit += Main.graph.getProfit(i);
		objectiveValue[2] = profit;
	}

	public Set<Integer> getVisited() {
		return visited;
	}

	public ArrayList<Vehicles> getGenotypeVehicles() {
		return genotypeVehicles;
	}

	public void setGenotypeVehicles(ArrayList<Vehicles> genotypeVehicles) {
		this.genotypeVehicles = genotypeVehicles;
	}

	public double[] getObjectiveValue() {
		return objectiveValue;
	}

	public void setObjectiveValue(double[] objectiveValue) {
		this.objectiveValue = objectiveValue;
	}

	public int getFront() {
		return front;
	}

	public void setFront(int front) {
		this.front = front;
	}

	public double getCrowdDistance() {
		return crowdDistance;
	}

	public void setCrowdDistance(double crowdDistance) {
		this.crowdDistance = crowdDistance;
	}

	public ArrayList<Individual> getDominated() {
		return dominated;
	}

	public void setDominated(ArrayList<Individual> dominated) {
		this.dominated = dominated;
	}

	public int getDominatedBy() {
		return dominatedBy;
	}

	public void setDominatedBy(int dominatedBy) {
		this.dominatedBy = dominatedBy;
	}

	@Override
	public String toString() {
		return "\n" + "SOL: \n" + "Vehicles " + genotypeVehicles + "\n" + "\n Num vehicles =  " + objectiveValue[0]
				+ "\n" + "Max length Tour =" + objectiveValue[1] + "\n" + "Profit =" + objectiveValue[2] + "] \n \n";

	}
}
