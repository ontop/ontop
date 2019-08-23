package it.unibz.inf.ontop.answering.reformulation.generation.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

@Deprecated
@Singleton
public class Relation2Predicate {

	@Inject
	private Relation2Predicate() {
	}
	
	/**
	 * 
	 * @param predicate a predicate-name rendering of a possibly qualified table name
	 * @return
	 */
	@Deprecated
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
