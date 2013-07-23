package org.obda.owlapi;

public interface OBDAModelChangeVisitor {

	void visit(AddAxiom change);

	void visit(RemoveAxiom change);

	void visit(UpdateSource change);

	void visit(UpdateMappingAxiom change);

}
