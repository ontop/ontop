package it.unibz.inf.ontop.spec.mapping.parser.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import eu.optique.r2rml.api.binding.rdf4j.RDF4JR2RMLMappingManager;
import eu.optique.r2rml.api.model.*;
import eu.optique.r2rml.api.model.impl.InvalidR2RMLMappingException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
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

	private final MappingParserHelper factory;

	@Inject
	private R2RMLParser(TermFactory termFactory, TypeFactory typeFactory, RDF rdfFactory) {
		this.termFactory = termFactory;
		this.typeFactory = typeFactory;
		this.manager = RDF4JR2RMLMappingManager.getInstance();
		this.rdfFactory = rdfFactory;
		this.factory = new MappingParserHelper(termFactory, typeFactory);
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
	 */
	public String extractSQLQuery(TriplesMap tm) {
		return tm.getLogicalTable().getSQLQuery();
	}

	public Stream<IRI> extractClassIRIs(SubjectMap subjectMap) {
		return subjectMap.getClasses().stream();
	}

	public ImmutableList<NonVariableTerm> extractGraphTerms(List<GraphMap> graphMaps) {
		return graphMaps.stream()
				.map(m -> extract(iriTerm, m))
				.collect(ImmutableCollectors.toList());
	}

	public ImmutableTerm extractSubjectTerm(SubjectMap m) {
		return extract(iriOrBnodeTerm, m);
	}

	public ImmutableList<NonVariableTerm> extractPredicateTerms(PredicateObjectMap pom) {
		return pom.getPredicateMaps().stream()
				.map(m -> extract(iriTerm, m))
				.collect(ImmutableCollectors.toList());
	}

	public ImmutableList<NonVariableTerm> extractRegularObjectTerms(PredicateObjectMap pom) {
		return pom.getObjectMaps().stream()
				.map(m -> extract(iriOrBnodeOrLiteral, m))
				.collect(ImmutableCollectors.toList());
	}

	private final ImmutableMap<IRI, Extractor<TermMap>> iriTerm = ImmutableMap.of(
			R2RMLVocabulary.iri, new IriExtractor<>());

	private final ImmutableMap<IRI, Extractor<TermMap>> iriOrBnodeTerm = ImmutableMap.of(
			R2RMLVocabulary.iri, new IriExtractor<>(),
			R2RMLVocabulary.blankNode, new BnodeExtractor<>());

	private final ImmutableMap<IRI, Extractor<ObjectMap>> iriOrBnodeOrLiteral = ImmutableMap.of(
			R2RMLVocabulary.iri, new IriExtractor<>(),
			R2RMLVocabulary.blankNode, new BnodeExtractor<>(),
			R2RMLVocabulary.literal, new LiteralExtractor<>());

	private <T extends TermMap> NonVariableTerm extract(ImmutableMap<IRI, Extractor<T>> map, T termMap) {
		return map.computeIfAbsent(termMap.getTermType(), k -> {
			throw new R2RMLParsingBugException("Was expecting one of " + map.keySet() +
						" when encountered " + termMap);
		}).extract(termMap);
	}

	private interface Extractor<T extends TermMap> {
		NonVariableTerm extract(RDFTerm constant, T termMap);
		NonVariableTerm extract(Template template, T termMap);
		NonVariableTerm extract(String column, T termMap);

		default NonVariableTerm extract(T termMap) {
			if (termMap.getConstant() != null)
				return extract(termMap.getConstant(), termMap);

			if (termMap.getTemplate() != null)
				return extract(termMap.getTemplate(), termMap);

			if (termMap.getColumn() != null)
				return extract(termMap.getColumn(), termMap);

			throw new R2RMLParsingBugException("A term map is either constant-valued, column-valued or template-valued.");
		}
	}

	private class IriExtractor<T extends TermMap> implements Extractor<T> {
		@Override
		public NonVariableTerm extract(RDFTerm constant, T termMap) {
			return termFactory.getConstantIRI(rdfFactory.createIRI(constant.toString()));
		}
		@Override
		public NonVariableTerm extract(Template template, T termMap) {
			ImmutableList<TemplateComponent> components = TemplateComponent.getComponents(
					// Q: WHY DOES IT NOT APPLY TO OTHER CASES?
					R2RMLVocabulary.resolveIri(template.toString(), "http://example.com/base/"));

			ImmutableList<ImmutableTerm> terms = factory.getTemplateTerms(components);
			return terms.isEmpty()
					? termFactory.getIRIFunctionalTerm(// Q: DOES IT MAKE SENSE?
					termFactory.getDBStringConstant(components.get(0).getComponent()))
					: termFactory.getIRIFunctionalTerm(factory.getTemplateString(components), terms);
		}
		@Override
		public 	NonVariableTerm extract(String column, T termMap) {
			return termFactory.getIRIFunctionalTerm(factory.getVariable(column));
		}
	}

	private class BnodeExtractor<T extends TermMap> implements Extractor<T> {
		@Override
		public NonVariableTerm extract(RDFTerm constant, T termMap) {
			throw new R2RMLParsingBugException("Constant blank nodes are not accepted in R2RML (should have been detected earlier)");
		}
		@Override
		public NonVariableTerm extract(Template template, T termMap) {
			ImmutableList<TemplateComponent> components = TemplateComponent.getComponents(template.toString());
			ImmutableList<ImmutableTerm> terms = factory.getTemplateTerms(components);
			return terms.isEmpty()
					? termFactory.getBnodeFunctionalTerm(// Q: DOES IT MAKE SENSE?
							termFactory.getDBStringConstant(components.get(0).getComponent()))
					: termFactory.getBnodeFunctionalTerm(factory.getTemplateString(components), terms);
		}
		@Override
		public 	NonVariableTerm extract(String column, T termMap) {
			return termFactory.getBnodeFunctionalTerm(factory.getVariable(column));
		}
	}

	private class LiteralExtractor<T extends ObjectMap> implements Extractor<T> {
		@Override
		public NonVariableTerm extract(RDFTerm constant, T om) {
			if (constant instanceof Literal) {
				return termFactory.getRDFLiteralFunctionalTerm(
						termFactory.getDBStringConstant(((Literal) constant).getLexicalForm()),
						extractDatatype(om));
			}
			throw new R2RMLParsingBugException("Was expecting a Literal as constant, not a " + constant.getClass());
		}
		@Override
		public NonVariableTerm extract(Template template, T om) {
			return termFactory.getRDFLiteralFunctionalTerm(
					factory.getLiteralTemplateTerm(template.toString()),
					extractDatatype(om));
		}
		@Override
		public 	NonVariableTerm extract(String column, T om) {
			return 	termFactory.getRDFLiteralFunctionalTerm(
					factory.getVariable(column),
					extractDatatype(om));
		}

		private RDFDatatype extractDatatype(ObjectMap om) {
			return  factory.extractDatatype(
					Optional.ofNullable(om.getLanguageTag()),
					Optional.ofNullable(om.getDatatype()))
					// Third try: datatype of the constant
					.orElseGet(() -> Optional.ofNullable(om.getConstant())
							.map(c -> (Literal) c)
							.map(Literal::getDatatype)
							.map(typeFactory::getDatatype)
							// Default case: RDFS.LITERAL (abstract, to be inferred later)
							.orElseGet(typeFactory::getAbstractRDFSLiteral));
		}
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
