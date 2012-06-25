package it.unibz.krdb.obda.owlapi3.model;

public interface OWLOBDAModelChangeVisitor {

	void visit(AddAxiom change);

	void visit(RemoveAxiom change);

	void visit(UpdateSource change);

	void visit(UpdateMappingAxiom change);

}
