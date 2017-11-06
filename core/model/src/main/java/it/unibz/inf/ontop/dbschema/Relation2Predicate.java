package it.unibz.inf.ontop.dbschema;

import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Term;

@Singleton
public class Relation2Predicate {

	private final AtomFactory atomFactory;
	private final TermFactory termFactory;

	@Inject
	private Relation2Predicate(AtomFactory atomFactory, TermFactory termFactory) {
		this.atomFactory = atomFactory;
		this.termFactory = termFactory;
	}

	public Predicate createPredicateFromRelation(RelationDefinition r) {
		
		Predicate pred = termFactory.getPredicate(extractPredicateName(r), r.getAttributes().size());
		return pred;
	}

	private  String extractPredicateName(RelationDefinition r) {
		RelationID id = r.getID();
		String name = id.getSchemaName();
		if (name == null)
			name =  id.getTableName();
		else
			name = name + "." + id.getTableName();
		return name;
	}

	public AtomPredicate createAtomPredicateFromRelation(RelationDefinition r) {
		return atomFactory.getAtomPredicate(extractPredicateName(r), r.getAttributes().size());
	}
	
	public Function getAtom(RelationDefinition r, List<Term> terms) {
		if (r.getAttributes().size() != terms.size())
			throw new IllegalArgumentException("The number of terms does not match the arity of relation");
		
		Predicate pred = createPredicateFromRelation(r);
		return termFactory.getFunction(pred, terms);
	}
	
	/**
	 * 
	 * @param predicate a predicate-name rendering of a possibly qualified table name
	 * @return
	 */
	
	
	public RelationID createRelationFromPredicateName(QuotedIDFactory idfac, Predicate predicate) {
		String s = predicate.getName();
		
		// ROMAN (7 Oct 2015): a better way of splitting is probably needed here
		// String[] names = s.split("\\.");
		int position = s.indexOf('.');
		if (position == -1)
			return RelationID.createRelationIdFromDatabaseRecord(idfac, null, s);
		else {
			return RelationID.createRelationIdFromDatabaseRecord(idfac,
					s.substring(0, position), s.substring(position + 1, s.length()));
		}
	}
}
