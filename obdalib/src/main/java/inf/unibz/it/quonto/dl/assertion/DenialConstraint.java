package inf.unibz.it.quonto.dl.assertion;

import inf.unibz.it.dl.assertion.Assertion;

public class DenialConstraint extends Assertion {
	
	private String constraint = null;
	
	private String annotation = null;
	
	public DenialConstraint(String constraint, String annotation) {
		this.constraint = constraint;
		this.annotation = annotation;
	}

	public void setConstraint(String constraint) {
		this.constraint = constraint;
	}

	public String getConstraint() {
		return constraint;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public String getAnnotation() {
		return annotation;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof DenialConstraint) {
			DenialConstraint dc = (DenialConstraint)object;
			return this.constraint.equals(dc.getConstraint());
		}
		return false;
	}
	
	public String toString() {
		return constraint;
	}
	
	@Override
	public int hashCode() {
		return constraint.hashCode();
	}
}
