package inf.unibz.it.quonto.obda.swing.action;

import inf.unibz.it.dl.assertion.Assertion;

import javax.swing.AbstractAction;

abstract public class ConstraintCheckAction<T extends Assertion> extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 6490836580243656564L;
	protected T constraint =  null; 
	
	public ConstraintCheckAction() {
		super();
	}
	
	abstract public boolean checkLogicalImplication() throws Exception;
	
	public void setConstraint(T constraint) {
		this.constraint = constraint;
	}
	
}