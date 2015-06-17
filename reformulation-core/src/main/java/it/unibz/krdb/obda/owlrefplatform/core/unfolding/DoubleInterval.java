package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

/**
 * Implementation of an immutable Double interval.
 */
public class DoubleInterval {
	private final double lowerBound;
	private final double upperBound;

	public DoubleInterval() {
		lowerBound = Double.NEGATIVE_INFINITY;
		upperBound = Double.POSITIVE_INFINITY;
	}
	public DoubleInterval(double lowerBound, double upperBound){
		if(lowerBound > upperBound) {
			throw new IllegalArgumentException("lowerBound > upperBound");
		}
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public Double getLowerBound() {
		return lowerBound;
	}

	public Double getUpperBound() {
		return upperBound;
	}
	
	/**
	 * Creates a new interval with the specified lower bound
	 * only if it is greater than the current one, otherwise
	 * returns the current object
	 */
	public DoubleInterval tryShrinkWithLowerBound(double lowerBound) {
		if (lowerBound <= this.lowerBound)
			return this;
		return new DoubleInterval(lowerBound, upperBound);
	}

	/**
	 * Creates a new interval with the specified upper bound
	 * only if it is less than the current one, otherwise
	 * returns the current object
	 */
	public DoubleInterval tryShrinkWithUpperBound(double upperBound) {
		if (upperBound >= this.upperBound)
			return this;
		return new DoubleInterval(lowerBound, upperBound);
	}
	
	@Override
	public DoubleInterval clone() {
		return new DoubleInterval(lowerBound, upperBound);
	}
}
