package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import eu.optique.r2rml.api.binding.rdf4j.RDF4JR2RMLMappingManager;
import eu.optique.r2rml.api.model.*;
import eu.optique.r2rml.api.model.impl.InvalidR2RMLMappingException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
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

	public ImmutableList<NonVariableTerm> extractGraphTerms(List<GraphMap> graphMaps) {

		for (GraphMap graphMap : graphMaps) {
			if (!graphMap.getTermType().equals(R2RMLVocabulary.iri))
				throw new R2RMLParsingBugException("The graph map must be an IRI, not " + graphMap.getTermType());
		}
		return graphMaps.stream()
				.map(this::extractIRIorBnodeTerm)
				.collect(ImmutableCollectors.toList());
	}

	public ImmutableTerm extractSubjectTerm(SubjectMap subjectMap) {
		return extractIRIorBnodeTerm(subjectMap);
	}

	public ImmutableList<NonVariableTerm> extractPredicateTerms(PredicateObjectMap pom) {
		return pom.getPredicateMaps().stream()
				.map(this::extractIRITerm)
				.collect(ImmutableCollectors.toList());
	}

	private NonVariableTerm extractIRITerm(TermMap termMap) {
		if (!termMap.getTermType().equals(R2RMLVocabulary.iri))
			throw new R2RMLParsingBugException("The term map must be an IRI, not " + termMap.getTermType());

		return extractIRIorBnodeTerm(termMap);
	}

	private NonVariableTerm extractIRIorBnodeTerm(TermMap termMap) {
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
						? extractTemplateLexicalTerm(s, RDFCategory.IRI)
						: extractTemplateLexicalTerm(s, RDFCategory.BNODE))
				.map(l -> termFactory.getRDFFunctionalTerm(l,
						termFactory.getRDFTermTypeConstant(isIRI
								? typeFactory.getIRITermType()
								: typeFactory.getBlankNodeType())))
				// COLUMN case
				.orElseGet(() -> Optional.ofNullable(termMap.getColumn())
						.map(this::getVariable)
						.map(lex -> termFactory.getRDFFunctionalTerm(
								lex,
								termFactory.getRDFTermTypeConstant(
										isIRI ? typeFactory.getIRITermType() : typeFactory.getBlankNodeType())))
						.orElseThrow(() -> new R2RMLParsingBugException("A term map is either constant-valued, " +
								"column-valued or template-valued.")));
	}


	/**
	 * Extracts the object terms, they can be constants, columns or templates
	 *
	 */

	public ImmutableList<NonVariableTerm> extractRegularObjectTerms(PredicateObjectMap pom) {
		ImmutableList.Builder<NonVariableTerm> termListBuilder = ImmutableList.builder();
		for (ObjectMap om : pom.getObjectMaps()) {
			termListBuilder.add(extractRegularObjectTerm(om));
		}
		return termListBuilder.build();
	}

	private NonVariableTerm extractRegularObjectTerm(ObjectMap om) {
		return om.getTermType().equals(R2RMLVocabulary.literal)
				? extractLiteral(om)
				: extractIRIorBnodeTerm(om);
	}

	private NonVariableTerm extractLiteral(ObjectMap om) {
		NonVariableTerm lexicalTerm = extractLiteralLexicalTerm(om);

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


	private NonVariableTerm extractLiteralLexicalTerm(ObjectMap om) {

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
			return getVariable(col);
		}

		// TEMPLATE
		Template t = om.getTemplate();
		if (t != null) {
			return extractTemplateLexicalTerm(t.toString(), RDFCategory.LITERAL);
		}
		throw new R2RMLParsingBugException("Was expecting a Constant/Column/Template");
	}

	/**
	 * gets the lexical term of a template-valued term map
	 *
	 * @param templateString
	 * @param type
	 *            IRI, BNODE, LITERAL§
	 * @return the constructed Function atom
	 */
	private NonVariableTerm extractTemplateLexicalTerm(String templateString, RDFCategory type) {

		// Non-final
		String string = templateString;
		if (!templateString.contains("{")) {
			return termFactory.getDBStringConstant(templateString);
		}

		if (type == RDFCategory.IRI) {
			string = R2RMLVocabulary.prefixUri(templateString);
		}

		String suffix = string; // literal case

		string = string.replace("\\{", "[");
		string = string.replace("\\}", "]");

		ImmutableList.Builder<NonVariableTerm> termListBuilder = ImmutableList.builder();

		while (string.contains("{")) {

			// Literal: if there is constant string in template, adds it to the term list
			if (type == RDFCategory.LITERAL) {
				int i = suffix.indexOf("{");
				int j = suffix.indexOf("\\{");

				while ((i - 1 == j) && (j != -1)) {
					i = suffix.indexOf("{",i + 1);
					j = suffix.indexOf("\\{",j + 1);
				}

				if (i > 0) {
					String cons = suffix.substring(0, i);
					termListBuilder.add(termFactory.getDBStringConstant(cons));
					suffix = suffix.substring(suffix.indexOf("}", i) + 1);
				}
				else {
					suffix = suffix.substring(suffix.indexOf("}") + 1);
				}
			}

			int end = string.indexOf("}");
			int begin = string.lastIndexOf("{", end);
			String var = string.substring(begin + 1, end);
			termListBuilder.add(getVariable(var));

			string = string.substring(0, begin) + "[]" + string.substring(end + 1);
		}
		if (type == RDFCategory.LITERAL && !suffix.isEmpty()) {
			termListBuilder.add(termFactory.getDBStringConstant(suffix));
		}

		string = string.replace("[", "{");
		string = string.replace("]", "}");

		ImmutableList<NonVariableTerm> terms = termListBuilder.build();

		switch (type) {
			case IRI:
				return termFactory.getImmutableFunctionalTerm(
						dbFunctionSymbolFactory.getIRIStringTemplateFunctionSymbol(string),
						terms);
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


	private ImmutableFunctionalTerm getVariable(String variableName) {
		return termFactory.getPartiallyDefinedToStringCast(termFactory.getVariable(variableName));
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
