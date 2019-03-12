package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import eu.optique.r2rml.api.binding.rdf4j.RDF4JR2RMLMappingManager;
import eu.optique.r2rml.api.model.*;
import eu.optique.r2rml.api.model.impl.InvalidR2RMLMappingException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.*;

import java.util.*;
import java.util.stream.Stream;

public class R2RMLParser {

	private final RDF4JR2RMLMappingManager manager;
	private final TermFactory termFactory;
	private final TypeFactory typeFactory;
	private final RDF rdfFactory;
	private final DBFunctionSymbolFactory dbFunctionSymbolFactory;

	@Inject
	private R2RMLParser(TermFactory termFactory, TypeFactory typeFactory, RDF rdfFactory,
					    DBFunctionSymbolFactory dbFunctionSymbolFactory) {
		this.termFactory = termFactory;
		this.typeFactory = typeFactory;
		this.dbFunctionSymbolFactory = dbFunctionSymbolFactory;
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
	public String extractSQLQuery(TriplesMap tm) {
		return tm.getLogicalTable().getSQLQuery();
	}


	public Stream<IRI> extractClassIRIs(SubjectMap subjectMap) {
		return subjectMap.getClasses().stream();
	}

	public ImmutableTerm extractSubjectTerm(SubjectMap subjectMap) {
		return extractSubjectTerm(subjectMap, "");
	}

	private ImmutableTerm extractSubjectTerm(SubjectMap subjectMap, String joinCond) {
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
				.map(s -> isIRI
						? extractLexicalTerm(s, 1, joinCond)
						: extractLexicalTerm(s, 2, joinCond))
				.map(l -> termFactory.getRDFFunctionalTerm(l,
						termFactory.getRDFTermTypeConstant(isIRI
								? typeFactory.getIRITermType()
								: typeFactory.getBlankNodeType())))
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


	public ImmutableList<NonVariableTerm> extractRegularObjectTerms(PredicateObjectMap pom) {
		return extractRegularObjectTerms(pom, "");
	}

	private boolean isConcat(String st) {
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
	 * Extracts the object terms, they can be constants, columns or templates
	 *
	 */
	private ImmutableList<NonVariableTerm> extractRegularObjectTerms(PredicateObjectMap pom, String joinCond) {
		ImmutableList.Builder<NonVariableTerm> termListBuilder = ImmutableList.builder();
		for (ObjectMap om : pom.getObjectMaps()) {
			termListBuilder.add(extractRegularObjectTerm(om, joinCond));
		}
		return termListBuilder.build();
	}

	private NonVariableTerm extractRegularObjectTerm(ObjectMap om, String joinCond) {
		return om.getTermType().equals(R2RMLVocabulary.literal)
				? extractLiteral(om, joinCond)
				: extractIRIorBnodeTerm(om, joinCond);
	}

	private NonVariableTerm extractLiteral(ObjectMap om, String joinCond) {
		NonVariableTerm lexicalTerm = extractLiteralLexicalTerm(om, joinCond);
		RDFDatatype datatype =  Optional.ofNullable(om.getLanguageTag())
				.filter(tag -> !tag.isEmpty())
				.map(typeFactory::getLangTermType)
				.orElseGet(() -> Optional.ofNullable(om.getDatatype())
						.map(typeFactory::getDatatype)
						.orElseGet(() -> Optional.ofNullable(om.getConstant())
										.map(c -> (Literal) c)
										.map(Literal::getDatatype)
										.map(typeFactory::getDatatype)
										.orElseGet(typeFactory::getAbstractRDFSLiteral)));

		return (NonVariableTerm) termFactory.getRDFLiteralFunctionalTerm(lexicalTerm, datatype).simplify();
	}


	private NonVariableTerm extractLiteralLexicalTerm(ObjectMap om, String joinCond) {

		// CONSTANT
		RDFTerm constantObj = om.getConstant();
		if (constantObj != null) {
			if (constantObj instanceof Literal){
				return termFactory.getDBStringConstant(((Literal) constantObj).getLexicalForm());
			}
			else
				throw new R2RMLParsingBugException("Was expecting a Literal as constant, not a " + constantObj.getClass());
		}

		// COLUMN
		String col = om.getColumn();
		if (col != null) {
			col = trim(col);
			if (!joinCond.isEmpty()) {
				col = joinCond + col;
			}
			return termFactory.getPartiallyDefinedToStringCast(termFactory.getVariable(col));
		}

		// TEMPLATE
		Template t = om.getTemplate();
		if (t != null) {
			// then we check if the template includes a concat
			if (isConcat(t.toString())){
				return extractLexicalTerm(t.toString(), 4, joinCond);
			}
			else {
				String value = t.getColumnName(0);
				if (!joinCond.isEmpty()) {
					value = joinCond + value;
				}
				return termFactory.getPartiallyDefinedToStringCast(termFactory.getVariable(value));
			}
		}
		throw new R2RMLParsingBugException("Was expecting a Constant/Column/Template");
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
	 * gets the lexical sub-term of an RDF term
	 *
	 * @param parsedString
	 *            - the content of atom
	 * @param type
	 *            - 0=constant uri, 1=uri or iri, 2=bnode, 3=literal 4=concat
	 * @param joinCond
	 *            - CHILD_ or PARENT_ prefix for variables
	 * @return the constructed Function atom
	 */
	private NonVariableTerm extractLexicalTerm(String parsedString, int type, String joinCond) {

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
			// IRI
			case 1:
				FunctionSymbol templateFunctionSymbol = dbFunctionSymbolFactory.getIRIStringTemplateFunctionSymbol(string);
				return termFactory.getImmutableFunctionalTerm(templateFunctionSymbol, ImmutableList.copyOf(terms));
			// BNODE
			case 2:
				return termFactory.getImmutableFunctionalTerm(
						dbFunctionSymbolFactory.getBnodeStringTemplateFunctionSymbol(string),
						ImmutableList.copyOf(terms));
			case 4://concat
				return termFactory.getDBConcatFunctionalTerm(ImmutableList.copyOf(terms));
			default:
				throw new R2RMLParsingBugException("Unexpected type code: " + type);
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
	 * Bug most likely coming from the R2RML library, but we classify as an "internal" bug
	 */
	private static class R2RMLParsingBugException extends OntopInternalBugException {

		protected R2RMLParsingBugException(String message) {
			super(message);
		}
	}


}
