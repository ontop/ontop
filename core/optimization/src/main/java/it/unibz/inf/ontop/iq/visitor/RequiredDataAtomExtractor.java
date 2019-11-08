package it.unibz.inf.ontop.iq.visitor;

import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;

import java.util.stream.Stream;

/**
 * Looks for data atoms that are required to provide tuples.
 *
 * For instance, excludes data atoms only appearing on the right of a LJ.
 *
 * MAY BE INCOMPLETE
 *
 */
public interface RequiredDataAtomExtractor extends IQVisitor<Stream<DataAtom<RelationPredicate>>> {
}
