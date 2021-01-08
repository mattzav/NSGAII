
public class Graph {

	public int numNodes;
	public Double[][] arcs;
	public double profit[];
	public int neededResource[];
	public Pair[] coordinates;

	public Graph(int numNodes) {
		this.numNodes = numNodes;
		this.neededResource = new int[numNodes];
		this.profit = new double[numNodes];
		this.arcs = new Double[numNodes][numNodes];
		this.coordinates = new Pair[numNodes];
	}

	public void setDistance() {
		for (int i = 0; i < numNodes - 1; i++)
			for (int j = i + 1; j < numNodes; j++) {
				arcs[i][j] = Math.sqrt(Math.pow(coordinates[i].first - coordinates[j].first, 2)
						+ Math.pow(coordinates[i].second - coordinates[j].second, 2));
				arcs[j][i] = arcs[i][j];
				arcs[i][i] = 0.;

			}
	}

	public int getNeededResource(int i) {
		return neededResource[i];
	}

	public Double getDuration(int i, int j) {
		return arcs[i][j];
	}

	public double getProfit(int i) {
		return profit[i];
	}

	public int getNumNodes() {
		return numNodes;
	}

	public void print() {

		for (int i = 0; i < numNodes; i++)
			for (int j = 0; j < numNodes; j++)
				System.out.println("(" + i + "," + j + ") = " + arcs[i][j]);

	}

}
