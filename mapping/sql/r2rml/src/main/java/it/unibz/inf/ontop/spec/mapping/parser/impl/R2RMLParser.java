package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import eu.optique.r2rml.api.binding.rdf4j.RDF4JR2RMLMappingManager;
import eu.optique.r2rml.api.model.*;
import eu.optique.r2rml.api.model.impl.InvalidR2RMLMappingException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
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
						? extractTemplateLexicalTerm(s, RDFCategory.IRI, joinCond)
						: extractTemplateLexicalTerm(s, RDFCategory.BNODE, joinCond))
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

		// Datatype -> first try: language tag
		RDFDatatype datatype =  Optional.ofNullable(om.getLanguageTag())
				.filter(tag -> !tag.isEmpty())
				.map(typeFactory::getLangTermType)
				// Second try: explicit datatype
				.orElseGet(() -> Optional.ofNullable(om.getDatatype())
						.map(typeFactory::getDatatype)
						// Third try: datatype of the constant
						.orElseGet(() -> Optional.ofNullable(om.getConstant())
										.map(c -> (Literal) c)
										.map(Literal::getDatatype)
										.map(typeFactory::getDatatype)
								// Default case: RDFS.LITERAL (abstract, to be inferred later)
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
			return extractTemplateLexicalTerm(t.toString(), RDFCategory.LITERAL, joinCond);
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
	 * gets the lexical term of a template-valued term map
	 *
	 * @param templateString
	 * @param type
	 *            IRI, BNODE, LITERAL
	 * @param joinCond
	 *            - CHILD_ or PARENT_ prefix for variables
	 * @return the constructed Function atom
	 */
	private NonVariableTerm extractTemplateLexicalTerm(String templateString, RDFCategory type, String joinCond) {

		// Non-final
		String string = templateString;
		if (!string.contains("{")) {
			return termFactory.getDBStringConstant(templateString);
		}
		if (type == RDFCategory.IRI) {
			string = R2RMLVocabulary.prefixUri(string);
		}

		String str = string; // literal case

		string = string.replace("\\{", "[");
		string = string.replace("\\}", "]");

		ImmutableList.Builder<NonVariableTerm> termListBuilder = ImmutableList.builder();
		String cons;
		int i;
		while (string.contains("{")) {
			int end = string.indexOf("}");
			int begin = string.lastIndexOf("{", end);

			// Literal: if there is constant string in template, adds it to the term list
			if (type == RDFCategory.LITERAL){
				if ((i = getIndexOfCurlyB(str)) > 0){
					cons = str.substring(0, i);
					str = str.substring(str.indexOf("}", i)+1);
					termListBuilder.add(termFactory.getDBStringConstant(cons));
				}else{
					str = str.substring(str.indexOf("}")+1);
				}
			}

			String var = trim(string.substring(begin + 1, end));

			// trim for making variable
			termListBuilder.add(termFactory.getPartiallyDefinedToStringCast(termFactory.getVariable(joinCond + (var))));

			string = string.replaceFirst("\\{\"" + var + "\"\\}", "[]");
			string = string.replaceFirst("\\{" + var + "\\}", "[]");

		}
		if(type == RDFCategory.LITERAL){
			if (!str.equals("")){
				cons = str;
				termListBuilder.add(termFactory.getDBStringConstant(cons));
			}
		}

		string = string.replace("[", "{");
		string = string.replace("]", "}");

		ImmutableList<NonVariableTerm> terms = termListBuilder.build();

		switch (type) {
			case IRI:
				FunctionSymbol templateFunctionSymbol = dbFunctionSymbolFactory.getIRIStringTemplateFunctionSymbol(string);
				return termFactory.getImmutableFunctionalTerm(templateFunctionSymbol, terms);
			case BNODE:
				return termFactory.getImmutableFunctionalTerm(
						dbFunctionSymbolFactory.getBnodeStringTemplateFunctionSymbol(string),
						terms);
			case LITERAL:
				switch (terms.size()) {
					case 0:
						return termFactory.getDBStringConstant("");
					case 1:
						return terms.get(0);
					default:
					return termFactory.getNullRejectingDBConcatFunctionalTerm(terms);
				}
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

	private enum RDFCategory {
		IRI,
		BNODE,
		LITERAL
	}


}
