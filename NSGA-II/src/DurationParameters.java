
public class DurationParameters {

	private int expectedDuration;
	private int varianceDuration;

	public DurationParameters(int expectedDuration, int varianceDuration) {
		this.expectedDuration = expectedDuration;
		this.varianceDuration = varianceDuration;
	}
	
	public int getExpectedDuration() {
		return expectedDuration;
	}
	public void setExpectedDuration(int expectedDuration) {
		this.expectedDuration = expectedDuration;
	}
	public int getVarianceDuration() {
		return varianceDuration;
	}
	public void setVarianceDuration(int varianceDuration) {
		this.varianceDuration = varianceDuration;
	}
	
	
	@Override
	public String toString() {
		return "("+expectedDuration+","+varianceDuration+")";
	}
	
	
}
