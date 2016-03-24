package it.unibz.krdb.sql;

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.util.List;

public class Relation2DatalogPredicate {

	private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	public static Predicate createPredicateFromRelation(RelationDefinition r) {
		RelationID id = r.getID();
		
		String name = id.getSchemaName();
		if (name == null)
			name =  id.getTableName();
		else
			name = name + "." + id.getTableName();
		
		Predicate pred = fac.getPredicate(name, r.getAttributes().size());
		return pred;
	}
	
	public static Function getAtom(RelationDefinition r, List<Term> terms) {
		if (r.getAttributes().size() != terms.size())
			throw new IllegalArgumentException("The number of terms does not match the arity of relation");
		
		Predicate pred = createPredicateFromRelation(r);
		return fac.getFunction(pred, terms);
	}
	
	/**
	 * 
	 * @param predicate a predicate-name rendering of a possibly qualified table name
	 * @return
	 */
	
	
	public static RelationID createRelationFromPredicateName(QuotedIDFactory idfac, Predicate predicate) {
		String s = predicate.getName();
		
		// ROMAN (7 Oct 2015): a better way of splitting is probably needed here
		String[] names = s.split("\\.");
		if (names.length == 1)
			return RelationID.createRelationIdFromDatabaseRecord(idfac, null, s);
		else {

			int position = s.indexOf('.');
			return RelationID.createRelationIdFromDatabaseRecord(idfac, s.substring(0,position), s.substring(position+1, s.length()));
		}
	}	
}
