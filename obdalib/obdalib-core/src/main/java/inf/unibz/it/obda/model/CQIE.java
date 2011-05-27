package inf.unibz.it.obda.model;

import java.util.List;

public interface CQIE extends Query {

	public Atom getHead();
	public List<Atom> getBody();
//	public boolean isBoolean();
	public void updateHead(Atom head);
	public void updateBody(List<Atom> body);
	
	//TODO make sure that clone is being use instead of copy, also for each atom and term
	public CQIE clone();
}
