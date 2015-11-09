package it.unibz.krdb.sql;

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.util.List;

public class Relation2DatalogPredicate {

	private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	
	public static Function getAtom(RelationDefinition r, List<Term> terms) {
		RelationID id = r.getID();
		
		String name = id.getSchemaName();
		if (name == null)
			name =  id.getTableName();
		else
			name = name + "." + id.getTableName();
		
		if (r.getAttributes().size() != terms.size())
			throw new IllegalArgumentException("The number of terms does not match the ariry of relation");
		
		Predicate pred = fac.getPredicate(name, terms.size());
		return fac.getFunction(pred, terms);
	}
	
	/**
	 * 
	 * @param s a predicate-name rendering of a possibly qualified table name
	 * @return
	 */
	
	
	public static RelationID createRelationFromPredicateName(Predicate predicate) {
		String s = predicate.getName();
		
		// ROMAN (7 Oct 2015): a better way of splitting is probably needed here
		String[] names = s.split("\\.");
		if (names.length == 1)
			return RelationID.createRelationIdFromDatabaseRecord(null, s);
		else
			return RelationID.createRelationIdFromDatabaseRecord(names[0], names[1]);			
	}	
}
