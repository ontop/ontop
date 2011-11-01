package it.unibz.krdb.obda.owlrefplatform.exception;

import it.unibz.krdb.obda.model.Predicate;

/***
 * Indicates that a URI has been PUNNED, i.e., it has been used as a Class and
 * Property at the same type (or Object and Data property).
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class PunningException extends Exception {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 5273586443299868448L;
	private Predicate			pred1				= null;
	private Predicate			pred2				= null;

	public PunningException(String message, Predicate pred1, Predicate pred2) {
		super(message);
		this.setPred1(pred1);
		this.setPred2(pred2);
	}
	
	public PunningException(String message) {
		super(message);
	}
	
	public PunningException(Predicate pred1, Predicate pred2) {
		this("", pred1, pred2);
	}

	private void setPred1(Predicate pred1) {
		this.pred1 = pred1;
	}

	public Predicate getPred1() {
		return pred1;
	}

	private void setPred2(Predicate pred2) {
		this.pred2 = pred2;
	}

	public Predicate getPred2() {
		return pred2;
	}
}
