package inf.unibz.it.quonto.obda.swing.action;

import inf.unibz.it.quonto.dl.assertion.DenialConstraint;

abstract public class CheckDenialConstraintLogicalImplicationAction  extends ConstraintCheckAction<DenialConstraint>{
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 6490836580243656564L;
	protected DenialConstraint constraint =  null; 
	
	public CheckDenialConstraintLogicalImplicationAction() {
		super();
		
	}
	
	
}
