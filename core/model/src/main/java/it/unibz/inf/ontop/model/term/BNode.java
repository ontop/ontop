package it.unibz.inf.ontop.model.term;

public interface BNode extends ObjectConstant {

	/**
	 * Must not be used by external bindings
	 */
	String getInternalLabel();

	/**
	 * To be used by external bindings
	 */
	String getAnonymizedLabel(byte[] salt);
}
