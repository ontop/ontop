package org.obda.query.domain;

import java.util.List;

public interface CQIE extends Query{

	public Atom getHead();
	public List<Atom> getBody();
	public boolean isBoolean();
	public void updateHead(Atom head);
	public void updateBody(List<Atom> body);
	public CQIE clone();
}
