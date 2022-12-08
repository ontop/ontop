package it.unibz.inf.ontop.si.repository.impl;

import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.term.functionsymbol.IRIDictionary;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * 	Semantic Index: reversible IRI to integer ID map
 */
public class IRIDictionaryImpl implements IRIDictionary {

	//private final static RDBMSSIRepositoryManager.TableDescription uriIdTable = new RDBMSSIRepositoryManager.TableDescription("URIID",
	//		ImmutableMap.of("ID", "INTEGER",
	//				"URI", "VARCHAR(400)"), "*");

	private final HashMap<String, Integer> uriIds = new HashMap<>();
	private final HashMap<Integer, String> uriMap2 = new HashMap<>();
	
	
	int getIdOrCreate(String irir, PreparedStatement stm) throws SQLException {
		Integer index =  uriIds.get(irir);
		if (index != null)
			return index;

		int id = uriIds.size();
		uriIds.put(irir, id);
		uriMap2.put(id, irir);
		return id;
	}

	/***
	 * We look for the ID in the list of IDs, if its not there, we return -2, which we know will never appear
	 * on the DB. This is correct because if a constant appears in a query, and that constant was never inserted
	 * in the DB, the query must be empty (that atom), by putting -2 as id, we will enforce that.
	 * @param iri
	 * @return
	 */
	@Override
	public int getId(String iri) {
		Integer id =  uriIds.get(iri);
		if (id != null)
			return id;
		return -2;
	}

	@Override
	public @Nonnull String getURI(Integer id) {
		String iri = uriMap2.get(id);
		if (iri == null)
			throw new MinorOntopInternalBugException("Unknown encoded ID used: " + id);
		return iri;
	}

	@Override
	public String toString() {
		return "si-dict";
	}
}
