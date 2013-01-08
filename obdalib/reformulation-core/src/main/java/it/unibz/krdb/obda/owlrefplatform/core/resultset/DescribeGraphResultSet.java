package it.unibz.krdb.obda.owlrefplatform.core.resultset;

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.GraphResultSet;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.shared.PrefixMapping;

public class DescribeGraphResultSet implements GraphResultSet {

	private OBDAResultSet tupleResultSet;

	private PrefixMapping prefixMapping;

	private OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
	private OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	
	private List<Constant> tupleResults = new ArrayList<Constant>();
	
	private static final String VAR1 = "x";
	private static final String VAR2 = "y";
	private static final String VAR3 = "z";

	public DescribeGraphResultSet(OBDAResultSet results, PrefixMapping pm) {
		tupleResultSet = results;
		prefixMapping = pm;
		storeResultsToLocalList();
	}

	private void storeResultsToLocalList() {
		try {
			while(tupleResultSet.nextRow()) {
				tupleResults.add(tupleResultSet.getConstant(1));
			}
		} catch (OBDAException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasNext() throws OBDAException {
		return tupleResults.size() != 0;
	}

	@Override
	public List<Assertion> next() throws OBDAException {
		Constant originalConstant = tupleResults.remove(0);
		String selectQuery = getSelectQuery(originalConstant);
		OBDAStatement stmt = tupleResultSet.getStatement();
		OBDAResultSet rs = stmt.execute(selectQuery);

		List<Assertion> tripleAssertions = new ArrayList<Assertion>();
		while (rs.nextRow()) {
			Constant subjectConstant = (rs.getConstant(1) == null) ? originalConstant : rs.getConstant(1);
			Constant predicateConstant = (rs.getConstant(2) == null) ? originalConstant : rs.getConstant(2);
			Constant objectConstant = (rs.getConstant(3) == null) ? originalConstant : rs.getConstant(3);
			
			// Determines the type of assertion
			String predicateName = predicateConstant.getValue();
			if (predicateName.equals(OBDAVocabulary.RDF_TYPE)) {
				Predicate concept = dfac.getClassPredicate(subjectConstant.getValue());
				ClassAssertion ca = ofac.createClassAssertion(
						concept,
						(ObjectConstant) objectConstant);
				tripleAssertions.add(ca);
			} else {
				if (objectConstant instanceof URIConstant) {
					Predicate role = dfac.getObjectPropertyPredicate(predicateName);
					ObjectPropertyAssertion op = ofac.createObjectPropertyAssertion(
							role, 
							(ObjectConstant) subjectConstant, 
							(ObjectConstant) objectConstant);
					tripleAssertions.add(op);
				} else {
					Predicate attribute = dfac.getDataPropertyPredicate(predicateName);
					DataPropertyAssertion dp = ofac.createDataPropertyAssertion(
							attribute, 
							(ObjectConstant) subjectConstant, 
							(ValueConstant) objectConstant);
					tripleAssertions.add(dp);
				}
			}
		}
		rs.close();
		return tripleAssertions;
	}

	@Override
	public void close() throws OBDAException {
		tupleResultSet.close();
	}

	private String getSelectQuery(Constant constant) {
		StringBuffer sb = new StringBuffer();
		Map<String, String> prefixMap = prefixMapping.getNsPrefixMap();
		for (String prefix : prefixMap.keySet()) {
			sb.append(String.format("PREFIX %s: <%s>\n", prefix, prefixMap
					.get(prefix)));
		}
		sb.append(String.format("SELECT ?%s ?%s ?%s\n", VAR1, VAR2, VAR3));
		sb.append("WHERE {\n");
		sb.append(String.format("   { ?%s ?%s <%s> } UNION\n", VAR1, VAR2, constant
				.toString()));
		sb.append(String.format("   { ?%s <%s> ?%s } UNION\n", VAR1,
				constant.toString(), VAR3));
		sb.append(String.format("   { <%s> ?%s ?%s }\n", constant.toString(),
				VAR2, VAR3));
		sb.append("}\n");
		return sb.toString();
	}
}
