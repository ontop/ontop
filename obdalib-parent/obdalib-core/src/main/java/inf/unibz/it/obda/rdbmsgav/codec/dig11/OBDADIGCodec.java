package inf.unibz.it.obda.rdbmsgav.codec.dig11;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.domain.Query;
import inf.unibz.it.obda.domain.SourceQuery;
import inf.unibz.it.obda.domain.TargetQuery;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.ucq.dig11.DIG12Coupler;
import inf.unibz.it.ucq.domain.BinaryQueryAtom;
import inf.unibz.it.ucq.domain.ConceptQueryAtom;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.ConstantTerm;
import inf.unibz.it.ucq.domain.FunctionTerm;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.domain.VariableTerm;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

//TODO: Refactor to make it a real instance of codec, possibly
//split into different codecs for the elements of the query

public class OBDADIGCodec {
	
	private static final String	TELLS_MAPPING_SQLQUERY_QUERYATR	= "query";
	private static final String	TELLS_MAPPING_SQLQUERY			= "SQLQuery";
	private static final String	TELLS_MAPPING_DATA_SOURCE_URI	= "dataSourceURI";
	private static final String	TELLS_MAPPING_MAPPING_AXIOM		= "MappingAxiom";
	private static final String	TELLS_MAPPING_ID				= "id";
	public static final String	TELLS_MAPPING_ENABLED			= "enabled";
	
	private static APIController apic = null;

	public static Element getDIG(OBDAMappingAxiom mapping, String datasource_uri, Document parentdoc, APIController apic) {
		Element dig_mapping = null;
	
		SourceQuery source_query = mapping.getSourceQuery();
		Element dig_mappingbody = null;
		TargetQuery target_query = mapping.getTargetQuery();
		Element dig_mappinghead = null;
	
		dig_mappinghead = getDIG(target_query, parentdoc, apic);
		dig_mappingbody = getDIG(source_query, parentdoc, apic);
	
		Element new_dig_mapping = parentdoc.createElement(TELLS_MAPPING_MAPPING_AXIOM);
		new_dig_mapping.setAttribute(TELLS_MAPPING_DATA_SOURCE_URI, datasource_uri);
		new_dig_mapping.setAttribute(TELLS_MAPPING_ID, mapping.getId());
		// new_dig_mapping.setAttribute(TELLS_MAPPING_ENABLED, "true");
		new_dig_mapping.appendChild(dig_mappinghead);
		new_dig_mapping.appendChild(dig_mappingbody);
		dig_mapping = new_dig_mapping;
	
		return dig_mapping;
	}

	public static Element getDIG(Query query, Document parentdoc, APIController apic) {
		// TODO Move the SPARQL query translation to this method instead of its
		// own builder
		Element dig_query = null;
	
		if (query instanceof RDBMSSQLQuery) {
			// <SQLQuery query="SELECT name, lastname FROM R WHERE gpa >=
			// 7.5" arity="2" />
			Element new_query = parentdoc.createElement(TELLS_MAPPING_SQLQUERY);
			new_query.setAttribute(TELLS_MAPPING_SQLQUERY_QUERYATR, query.toString());
			dig_query = new_query;
	
		} else if (query instanceof ConjunctiveQuery) {
			Element new_dig_query = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_RETRIEVE);
	
			Element head = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_HEAD);
			Element body = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_BODY);
			Element intersection = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_QAND);
	
			ConjunctiveQuery cq = (ConjunctiveQuery) query;
			ArrayList<QueryAtom> atoms = cq.getAtoms();
			ArrayList<QueryTerm> head_variables = cq.getHeadTerms();
	
			for (int i = 0; i < atoms.size(); i++) {
				QueryAtom atom = atoms.get(i);
				Element dig_atom = getDIG(atom, parentdoc,apic);
				if (dig_atom != null) {
					intersection.appendChild(dig_atom);
				} else {
					System.out.println("Atom null while trying to render it");
				}
			}
			// building the head with all variables selected
	
			for (QueryTerm variableTerm : head_variables) {
				Element head_term = getDIG(variableTerm, parentdoc);
				head.appendChild(head_term);
			}
	
			body.appendChild(intersection);
			new_dig_query.appendChild(head);
			new_dig_query.appendChild(body);
			dig_query = new_dig_query;
		}
		// else {
		// throw new Exception("Unsupported query type: " +
		// query.getClass().toString());
		// }
	
		return dig_query;
	}

	public static Element getDIG(QueryAtom atom, Document parentdoc, APIController apic) {
		Element dig_atom = null;
	
		if (atom instanceof ConceptQueryAtom) {
			Element concept_query_atom = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_CQATOM);
			ConceptQueryAtom concept_atom = (ConceptQueryAtom) atom;
			String name = apic.getEntityNameRenderer().getPredicateName(concept_atom);
			ArrayList<QueryTerm> terms = concept_atom.getTerms();
			for (int i = 0; i < terms.size(); i++) {
				QueryTerm current_term = terms.get(i);
				Element dig_term = getDIG(current_term, parentdoc);
				concept_query_atom.appendChild(dig_term);
			}
			Element concept_element = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_CATOM);
			concept_element.setAttribute(DIG12Coupler.ASKS_QUERY_NAME, name);
			concept_query_atom.appendChild(concept_element);
			dig_atom = concept_query_atom;
		} else if (atom instanceof BinaryQueryAtom) {
			Element role_query_atom = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_RQATOM);
			BinaryQueryAtom role_atom = (BinaryQueryAtom) atom;
			String name = apic.getEntityNameRenderer().getPredicateName(role_atom);
			ArrayList<QueryTerm> terms = role_atom.getTerms();
			for (int i = 0; i < terms.size(); i++) {
				QueryTerm current_term = terms.get(i);
				Element dig_term = getDIG(current_term, parentdoc);
				if (atom != null) {
					role_query_atom.appendChild(dig_term);
				} else {
					//TODO replace this for proper logging
					System.out.println("Atom null while trying to render it");
				}
			}
			Element role_element = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_RATOM);
			role_element.setAttribute(DIG12Coupler.ASKS_QUERY_NAME, name);
			role_query_atom.appendChild(role_element);
			dig_atom = role_query_atom;
		}
	
		return dig_atom;
	}

	public static Element getDIG(QueryTerm term, Document parentdoc) {
		Element dig_term = null;
		if (term instanceof VariableTerm) {
	
			Element new_term = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_INDVAR);
			new_term.setAttribute(DIG12Coupler.ASKS_QUERY_NAME, term.getVariableName());
			dig_term = new_term;
		} else if (term instanceof ConstantTerm) {
			// throw new Exception("Contants not supported");
			// Element new_term =
			// parentdoc.createElement(DIG12Coupler.ASKS_QUERY_INDIVIDUAL);
			// new_term.setAttribute(DIG12Coupler.ASKS_QUERY_NAME, term.getName());
			// dig_term = new_term;
	
		} else if (term instanceof FunctionTerm) {
	
			Element new_term = parentdoc.createElement(DIG12Coupler.ASKS_QUERY_FUNCTION);
			new_term.setAttribute(DIG12Coupler.ASKS_QUERY_NAME, apic.getEntityNameRenderer().getFunctionName((FunctionTerm) term));
			FunctionTerm function_term = (FunctionTerm) term;
			ArrayList<QueryTerm> parameters = function_term.getParameters();
			for (int i = 0; i < parameters.size(); i++) {
				Element parameter = getDIG(parameters.get(i), parentdoc);
				new_term.appendChild(parameter);
			}
			dig_term = new_term;
	
		}
	
		return dig_term;
	
	}

}
