
public class Graph {

	private int numNodes;
	private DurationParameters[][] arcs;
	private double profit[];
	private int neededResource[];
	
	public Graph(int numNodes) {
		this.numNodes = numNodes;
		this.neededResource = new int[numNodes];
		this.profit = new double[numNodes];
		this.arcs = new DurationParameters[numNodes][numNodes];
		initRandom();
	}
	
	private void initRandom() {
		for (int i = 0; i < numNodes; i++) {
			if (i > 0) {
				profit[i] = 10 + 30* ((Main.r.nextInt() % 20) + 20) % 20; // profit from 1 to 20
				neededResource[i] = ((Main.r.nextInt() % 20) + 20) % 20; // needed resource from 1 to 20
			}else {
				profit[i] = 0;
				neededResource[i] = 0;
			}

			for (int j = 0; j < numNodes; j++)
				arcs[i][j] = new DurationParameters(1+((Main.r.nextInt() % 20) + 20) % 20,1+
						((Main.r.nextInt() % 30) + 30) % 30); // valore
			// atteso
			// tra 0 e
			// 20,
			// varianza
			// tra 0 e
			// 30
		}
	}

	public int getNeededResource(int i) {
		return neededResource[i];
	}

	public DurationParameters getDuration(int i, int j) {
		return arcs[i][j];
	}

	public double getProfit(int i) {
		return profit[i];
	}

	public int getNumNodes() {
		return numNodes;
	}

	public void print() {
		System.out.println("Num Nodes = " + numNodes);
		for (int i = 0; i < numNodes; i++)
			for (int j = 0; j < numNodes; j++)
				System.out.println("(" + i + "," + j + ") = " + arcs[i][j]);
		for (int i = 0; i < numNodes; i++)
			System.out.println("Profit " + i + " = " + profit[i]);
		for (int i = 0; i < numNodes; i++)
			System.out.println("Resource " + i + " = " + neededResource[i]);
	}

	public double getNormalizedDistance(int i, int j) {
		return arcs[i][j].getExpectedDuration() * 0.75 + arcs[i][j].getVarianceDuration() * 0.25;

	}
}
