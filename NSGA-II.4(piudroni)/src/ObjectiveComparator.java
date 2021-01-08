import java.util.Comparator;

public class ObjectiveComparator implements Comparator<Individual> {

	private int i_th_objective;

	public ObjectiveComparator(int i_th) {
		i_th_objective = i_th;
	}

	@Override
	public int compare(Individual o1, Individual o2) {
		if (o1.getObjectiveValue()[i_th_objective] < o2.getObjectiveValue()[i_th_objective])
			return -1;
		if (o1.getObjectiveValue()[i_th_objective] == o2.getObjectiveValue()[i_th_objective])
			return 0;
		return 1;
	}
}
