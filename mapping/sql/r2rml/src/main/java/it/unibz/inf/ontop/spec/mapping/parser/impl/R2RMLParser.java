package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import eu.optique.r2rml.api.binding.rdf4j.RDF4JR2RMLMappingManager;
import eu.optique.r2rml.api.model.*;
import eu.optique.r2rml.api.model.impl.InvalidR2RMLMappingException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.*;

import java.util.*;
import java.util.stream.Stream;

public class R2RMLParser {

	private final RDF4JR2RMLMappingManager manager;
	private final TermFactory termFactory;
	private final TypeFactory typeFactory;
	private final OntopMappingSQLSettings settings;
	private final RDF rdfFactory;

	@Inject
	private R2RMLParser(TermFactory termFactory, TypeFactory typeFactory, RDF rdfFactory,
					   OntopMappingSQLSettings settings) {
		this.termFactory = termFactory;
		this.typeFactory = typeFactory;
		this.settings = settings;
		this.manager = RDF4JR2RMLMappingManager.getInstance();
		this.rdfFactory = rdfFactory;
	}

	/**
	 * method to get the TriplesMaps from the given Graph
	 * @param myGraph - the Graph to process
	 * @return Collection<TriplesMap> - the collection of mappings
	 */
	public Collection<TriplesMap> extractTripleMaps(Graph myGraph) throws InvalidR2RMLMappingException {
		return manager.importMappings(myGraph);
	}

	/**
	 * Get SQL query of the TriplesMap
	 * @param tm
	 * @return
	 */
	public String getSQLQuery(TriplesMap tm) {
		return tm.getLogicalTable().getSQLQuery();
	}


	public Stream<IRI> extractClassIRIs(SubjectMap subjectMap) {
		return subjectMap.getClasses().stream();
	}

	/**
	 * Get predicates
	 * @param tm
	 * @return
	 */
	public Set<BlankNodeOrIRI> getPredicateObjects(TriplesMap tm) {
		Set<BlankNodeOrIRI> predobjs = new HashSet<>();
		for (PredicateObjectMap pobj : tm.getPredicateObjectMaps()) {
			for (PredicateMap pm : pobj.getPredicateMaps()) {
				BlankNodeOrIRI r = pm.getNode();
				predobjs.add(r);
			}
		}
		return predobjs;
	}

	public ImmutableTerm extractSubjectTerm(SubjectMap subjectMap) {
		return extractSubjectTerm(subjectMap, "");
	}

	public ImmutableTerm extractSubjectTerm(SubjectMap subjectMap, String joinCond) {
		return extractIRIorBnodeTerm(subjectMap, joinCond);
	}

	public ImmutableList<NonVariableTerm> extractPredicateTerms(PredicateObjectMap pom) {
		return pom.getPredicateMaps().stream()
				.map(this::extractIRITerm)
				.collect(ImmutableCollectors.toList());
	}


	private NonVariableTerm extractIRITerm(TermMap termMap) {
		if (!termMap.getTermType().equals(R2RMLVocabulary.iri))
			throw new R2RMLParsingBugException("The term map must be an IRI, not " + termMap.getTermType());
		return extractIRIorBnodeTerm(termMap, "");
	}

	private NonVariableTerm extractIRIorBnodeTerm(TermMap termMap, String joinCond) {
		IRI termTypeIRI = termMap.getTermType();

		boolean isIRI = termTypeIRI.equals(R2RMLVocabulary.iri);
		if ((!isIRI) && (!termTypeIRI.equals(R2RMLVocabulary.blankNode)))
			throw new R2RMLParsingBugException("Was expecting an IRI or a blank node, not a " + termTypeIRI);

		// CONSTANT CASE
		RDFTerm constant = termMap.getConstant();
		if (constant != null) {
			if (!isIRI)
				throw new R2RMLParsingBugException("Constant blank nodes are not accepted in R2RML " +
						"(should have been detected earlier)");
			return termFactory.getConstantIRI(rdfFactory.createIRI(constant.toString()));
		}

		return Optional.ofNullable(termMap.getTemplate())
				// TEMPLATE CASE
				// TODO: should we use the Template object instead?
				.map(Template::toString)
				.map(s -> extractRDFTerm(s, termTypeIRI, joinCond))
				// COLUMN case
				.orElseGet(() -> Optional.ofNullable(termMap.getColumn())
						.map(column -> termFactory.getPartiallyDefinedToStringCast(termFactory.getVariable(column)))
						.map(lex -> termFactory.getRDFFunctionalTerm(
								lex,
								termFactory.getRDFTermTypeConstant(
										isIRI ? typeFactory.getIRITermType() : typeFactory.getBlankNodeType())))
						.orElseThrow(() -> new R2RMLParsingBugException("A term map is either constant-valued, " +
								"column-valued or template-valued.")));
	}


	public ImmutableTerm extractRegularObjectTerms(PredicateObjectMap pom) throws InvalidR2RMLMappingException {
		return extractRegularObjectTerms(pom, "");
	}

	public boolean isConcat(String st) {
		int i, j;
		if ((i = st.indexOf("{")) > -1) {
			if ((j = st.lastIndexOf("{")) > i) {
				return true;
			} else if ((i > 0) || ((j > 0) && (j < (st.length() - 1)))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Get the object atom, it can be a constant, a column or a template
	 *
	 * @param pom
	 * @param joinCond
	 * @return
	 * @throws Exception
	 */
	public ImmutableTerm extractRegularObjectTerms(PredicateObjectMap pom, String joinCond) throws InvalidR2RMLMappingException {
		ImmutableTerm lexicalTerm = null;
		if (pom.getObjectMaps().isEmpty()) {
			return null;
		}
		ObjectMap om = pom.getObjectMap(0);

		String lan = om.getLanguageTag();
		IRI datatype = om.getDatatype();

		// we check if the object map is a constant (can be a iri or a literal)
        // TODO(xiao): toString() is suspicious
        RDFTerm constantObj = om.getConstant();
		if (constantObj != null) {
			// boolean isURI = false;
			// try {
			// java.net.URI.create(obj);
			// isURI = true;
			// } catch (IllegalArgumentException e){
			//
			// }

			// if the literal has a language property or a datatype property we
			// create the function object later

			if (lan != null || datatype != null) {
				lexicalTerm = termFactory.getDBStringConstant(((Literal) constantObj).getLexicalForm());

			} else {

				if (constantObj instanceof Literal){

					String lexicalString = ((Literal) constantObj).getLexicalForm();
					lexicalTerm = termFactory.getDBStringConstant(lexicalString);
					Literal constantLit1 = (Literal) constantObj;

					String lanConstant = om.getLanguageTag();
					IRI datatypeConstant = constantLit1.getDatatype();

					// we check if it is a literal with language tag

					if (lanConstant != null) {
						return termFactory.getRDFLiteralFunctionalTerm(lexicalTerm, lanConstant);
					}

					// we check if it is a typed literal
					else if (datatypeConstant != null) {
						if ((!settings.areAbstractDatatypesToleratedInMapping())
							&& typeFactory.getDatatype(datatypeConstant).isAbstract())
							throw new InvalidR2RMLMappingException("Abstract datatype "
									+datatypeConstant  + " detected in the mapping assertion.\nSet the property "
									+ OntopMappingSettings.TOLERATE_ABSTRACT_DATATYPE + " to true to tolerate them.");
						return termFactory.getRDFLiteralFunctionalTerm(lexicalTerm, datatypeConstant);
					}
					else {
						// Use RDFS.LITERAL when the datatype is not specified (-> to be inferred)
						return termFactory.getRDFLiteralConstant(lexicalString, RDFS.LITERAL);
					}
                } else if (constantObj instanceof IRI){
                    return termFactory.getConstantIRI((IRI) constantObj);
                }
			}
		}

		// we check if the object map is a column
		// if it has a datatype or language property or its a iri we check it later
		String col = om.getColumn();
		if (col != null) {
			col = trim(col);

			if (!joinCond.isEmpty()) {
				col = joinCond + col;
			}

			lexicalTerm = termFactory.getPartiallyDefinedToStringCast(termFactory.getVariable(col));

		}

		// we check if the object map is a template (can be a iri, a literal or
		// a blank node)
		Template t = om.getTemplate();
		IRI typ = om.getTermType();
		boolean concat = false;
		if (t != null) {
			//we check if the template is a literal
			//then we check if the template includes concat 
			concat = isConcat(t.toString());
			if (typ.equals(R2RMLVocabulary.literal) && (concat)){
				lexicalTerm = extractRDFTerm(t.toString(), 4, joinCond);
			}else {

				// a template can be a rr:IRI, a
				// rr:Literal or rr:BlankNode

				// if the literal has a language property or a datatype property
				// we
				// create the function object later
				if (lan != null || datatype != null) {
					String value = t.getColumnName(0);
					if (!joinCond.isEmpty()) {
						value = joinCond + value;

					}
					lexicalTerm = termFactory.getPartiallyDefinedToStringCast(termFactory.getVariable(value));
				} else {
					IRI type = om.getTermType();

					// we check if the template is a IRI a simple literal or a
					// blank
					// node and create the function object
					lexicalTerm = extractRDFTerm(t.toString(), type, joinCond);
				}
			}
		}
		else{
			//assign iri template
			TermMap.TermMapType termMapType = om.getTermMapType();
			if(termMapType.equals(TermMap.TermMapType.CONSTANT_VALUED)){

			} else if(termMapType.equals(TermMap.TermMapType.COLUMN_VALUED)){
				if(typ.equals(R2RMLVocabulary.iri)) {
					// Cast to Variable added. TODO: check
					return termFactory.getIRIFunctionalTerm((Variable) lexicalTerm, true);
				}
			}

		}

		// we check if it is a literal with language tag

		if (lan != null) {
			return termFactory.getRDFLiteralFunctionalTerm(lexicalTerm, lan);
		}

		// we check if it is a typed literal
		if (datatype != null) {
			return termFactory.getRDFLiteralFunctionalTerm(lexicalTerm, datatype);
		}

		if (typ.equals(R2RMLVocabulary.literal)) {
			return termFactory.getRDFLiteralFunctionalTerm(lexicalTerm, RDFS.LITERAL);
		}

		throw new MinorOntopInternalBugException("TODO: fix this broken logic for " + lexicalTerm);
	}

	private NonVariableTerm extractRDFTerm(String string, IRI typeIRI, String joinCond) {

		if (typeIRI.equals(R2RMLVocabulary.iri)) {

			return extractFunctionalIRITerm(string, joinCond);

		} else if (typeIRI.equals(R2RMLVocabulary.blankNode)) {

			return extractRDFTerm(string, 2, joinCond);

		} else if (typeIRI.equals(R2RMLVocabulary.literal)) {

			return extractRDFTerm(trim(string), 3, joinCond);
		}
		throw new R2RMLParsingBugException("Unexpected term type: " + typeIRI);
	}

	private NonVariableTerm extractFunctionalIRITerm(String string, String joinCond) {
		return extractRDFTerm(string, 1, joinCond);
	}

	private NonVariableTerm extractFunctionalIRITerm(String string) {
		return extractRDFTerm(string, 1);
	}

	public NonVariableTerm extractRDFTerm(String parsedString, int type) {
		return extractRDFTerm(parsedString, type, "");
	}
	
	
	//this function distinguishes curly bracket with back slash "\{" from curly bracket "{" 
	private int getIndexOfCurlyB(String str){
		int i;
		int j;
		i = str.indexOf("{");
		j = str.indexOf("\\{");
		
		while((i-1 == j) && (j != -1)){		
			i = str.indexOf("{",i+1);
			j = str.indexOf("\\{",j+1);		
		}	
		return i;
	}

	/**
	 * get a typed atom
	 * 
	 * @param parsedString
	 *            - the content of atom
	 * @param type
	 *            - 0=constant uri, 1=uri or iri, 2=bnode, 3=literal 4=concat
	 * @param joinCond
	 *            - CHILD_ or PARENT_ prefix for variables
	 * @return the constructed Function atom
	 */
	private NonVariableTerm extractRDFTerm(String parsedString, int type,
										   String joinCond) {

		List<ImmutableTerm> terms = new ArrayList<>();
		String string = (parsedString);
		if (!string.contains("{")) {
			if (type < 3) {
    				if (!R2RMLVocabulary.isResourceString(string)) {
						string = R2RMLVocabulary.prefixUri("{" + string + "}");
					if (type == 2) {
						string = "\"" + string + "\"";
					}
				} else {
					type = 0;
				}
			}
		}
		if (type == 1) {
			string = R2RMLVocabulary.prefixUri(string);
		}

		String str = string; //str for concat of constant literal
		
		string = string.replace("\\{", "[");
		string = string.replace("\\}", "]");
		
		String cons;
		int i;
		while (string.contains("{")) {
			int end = string.indexOf("}");
			int begin = string.lastIndexOf("{", end);
			
			// (Concat) if there is constant literal in template, adds it to terms list 
			if (type == 4){
				if ((i = getIndexOfCurlyB(str)) > 0){
					cons = str.substring(0, i);
					str = str.substring(str.indexOf("}", i)+1, str.length());
					terms.add(termFactory.getDBStringConstant(cons));
				}else{
					str = str.substring(str.indexOf("}")+1);
				}
			}

			String var = trim(string.substring(begin + 1, end));

			// trim for making variable
			terms.add(termFactory.getPartiallyDefinedToStringCast(termFactory.getVariable(joinCond + (var))));

			string = string.replaceFirst("\\{\"" + var + "\"\\}", "[]");
			string = string.replaceFirst("\\{" + var + "\\}", "[]");
			
		}
		if(type == 4){
			if (!str.equals("")){
				cons = str;
				terms.add(termFactory.getDBStringConstant(cons));
			}
		}
	
		string = string.replace("[", "{");
		string = string.replace("]", "}");

		switch (type) {
		// constant uri
		case 0:
			IRI iri = rdfFactory.createIRI(string);
			return termFactory.getConstantIRI(iri);
			// URI or IRI
		case 1:
			return termFactory.getIRIFunctionalTerm(string, ImmutableList.copyOf(terms));
			// BNODE
		case 2:
			return termFactory.getBnodeFunctionalTerm(string, ImmutableList.copyOf(terms));
			// simple LITERAL
		case 3:
			ImmutableTerm lexicalValue = terms.remove(0);
			// pred = typeFactory.getRequiredTypePredicate(); //
			// the URI template is always on the first position in the term list
			// terms.add(0, uriTemplate);
			return termFactory.getRDFLiteralFunctionalTerm(lexicalValue, XSD.STRING);
		case 4://concat
			return termFactory.getDBConcatFunctionalTerm(ImmutableList.copyOf(terms));
		default:
			throw new MinorOntopInternalBugException("Unexpected type code: " + type);
		}
	}

	/**
	 * method that trims a string of all its double apostrophes from beginning
	 * and end
	 * 
	 * @param string
	 *            - to be trimmed
	 * @return the string without any quotes
	 */
	private String trim(String string) {

		while (string.startsWith("\"") && string.endsWith("\"")) {

			string = string.substring(1, string.length() - 1);
		}
		return string;
	}

	/**
	 * method to trim a string of its leading or trailing quotes but one
	 * 
	 * @param string
	 *            - to be trimmed
	 * @return the string left with one leading and trailing quote
	 */
	private String trimTo1(String string) {

		while (string.startsWith("\"\"") && string.endsWith("\"\"")) {

			string = string.substring(1, string.length() - 1);
		}
		return string;
	}

	/**
	 * Bug most likely coming from the R2RML library, but we classify as an "internal" bug
	 */
	private static class R2RMLParsingBugException extends OntopInternalBugException {

		protected R2RMLParsingBugException(String message) {
			super(message);
		}
	}


}
