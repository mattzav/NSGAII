import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class Individual {
	private ArrayList<Vehicle> genotypeVehicle;
	private double objectiveValue[] = new double[Main.numObjective];

	private int front;
	private double crowdDistance;
	private ArrayList<Individual> dominated = new ArrayList<Individual>();
	private int dominatedBy = 0;

	private Set<Integer> visited;

	public Individual(ArrayList<Vehicle> v) {
		visited = new TreeSet<Integer>();
		updateSolution(v);
	}

	public void updateSolution(ArrayList<Vehicle> v) {
		if (v != null) {
			genotypeVehicle = new ArrayList<Vehicle>();
			for (Vehicle veic : v)
				genotypeVehicle.add(new Vehicle(veic));

		}
		objectiveValue[0] = genotypeVehicle.size(); // numVehicle

		visited = new TreeSet<Integer>();

		double maxLengthTour = 0;
		for (int j = 0; j < genotypeVehicle.size(); j++) {
			visited.addAll(genotypeVehicle.get(j).getTour());
			if (genotypeVehicle.get(j).getTourLength() > maxLengthTour) {
				maxLengthTour = genotypeVehicle.get(j).getTourLength();
			}
		}
		// START

		double totalSum = 0;
		for (int j = 0; j < genotypeVehicle.size(); j++) {
			double cumulative = 0;
			int pred = 0;
			for (Integer node : genotypeVehicle.get(j).getTour()) {
				if (node == 0)
					continue;
				cumulative += Main.graph.getDuration(pred, node);
				totalSum += cumulative;
				double droneMax = 0;
				for (int drone = 0; drone < Main.numDronesPerVehicle; drone++) {
					double currDrone = 0;
					if (genotypeVehicle.get(j).getDroneTour().get(node).get(drone) != null) {
						for (Integer droneNode : genotypeVehicle.get(j).getDroneTour().get(node).get(drone)) {
							totalSum += (cumulative + currDrone + Main.graph.getDuration(node, droneNode));
							currDrone += Main.graph.getDuration(node, droneNode) * 2;
						}
					}
					if (currDrone > droneMax)
						droneMax = currDrone;
				}
				cumulative += droneMax;
				pred = node;
			}
		}

		// END

//		System.out.println(v + " MAX LENGTH: "+maxLengthTour);
		objectiveValue[1] = totalSum;

		for (int j = 0; j < genotypeVehicle.size(); j++) {
			visited.addAll(genotypeVehicle.get(j).getTour());
			for (Integer node : genotypeVehicle.get(j).getTour()) {
				if (node != 0) {
					for (int drone = 0; drone < Main.numDronesPerVehicle; drone++)
						if (genotypeVehicle.get(j).getDroneTour().get(node).get(drone) != null)
							visited.addAll(genotypeVehicle.get(j).getDroneTour().get(node).get(drone));
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

	public ArrayList<Vehicle> getGenotypeVehicle() {
		return genotypeVehicle;
	}

	public void setGenotypeVehicle(ArrayList<Vehicle> genotypeVehicle) {
		this.genotypeVehicle = genotypeVehicle;
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
		return "\n" + "SOL: \n" + "Vehicle " + genotypeVehicle + "\n" + "\n Num vehicles =  " + objectiveValue[0] + "\n"
				+ "Max length Tour =" + objectiveValue[1] + "\n" + "Profit =" + objectiveValue[2] + "] \n \n";

	}
}
