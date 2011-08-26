package it.unibz.krdb.obda.model;

import java.util.List;

public interface CQIE extends Query {

	public Atom getHead();

	public List<Atom> getBody();

	public void updateHead(Atom head);

	public void updateBody(List<Atom> body);

	public CQIE clone();
}
