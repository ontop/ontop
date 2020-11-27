package it.unibz.inf.ontop.spec.mapping.parser.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import eu.optique.r2rml.api.binding.rdf4j.RDF4JR2RMLMappingManager;
import eu.optique.r2rml.api.model.*;
import eu.optique.r2rml.api.model.impl.InvalidR2RMLMappingException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.template.impl.BnodeTemplateFactory;
import it.unibz.inf.ontop.model.template.impl.IRITemplateFactory;
import it.unibz.inf.ontop.model.template.impl.LiteralTemplateFactory;
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

	private final String baseIri = "http://example.com/base/";

	final IRITemplateFactory iriTemplateFactory;
	final BnodeTemplateFactory bnodeTemplateFactory;
	final LiteralTemplateFactory literalTemplateFactory;


	@Inject
	private R2RMLParser(TermFactory termFactory, TypeFactory typeFactory) {
		this.termFactory = termFactory;
		this.typeFactory = typeFactory;
		this.manager = RDF4JR2RMLMappingManager.getInstance();

		this.iriTemplateFactory = new IRITemplateFactory(termFactory);
		this.bnodeTemplateFactory =  new BnodeTemplateFactory(termFactory);
		this.literalTemplateFactory = new LiteralTemplateFactory(termFactory, typeFactory);
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
				.map(m -> extract(iriOrBnodeOrLiteralTerm, m))
				.collect(ImmutableCollectors.toList());
	}

	private final ImmutableMap<IRI, Extractor<TermMap>> iriTerm = ImmutableMap.of(
			R2RMLVocabulary.iri, new IriExtractor<>());

	private final ImmutableMap<IRI, Extractor<TermMap>> iriOrBnodeTerm = ImmutableMap.of(
			R2RMLVocabulary.iri, new IriExtractor<>(),
			R2RMLVocabulary.blankNode, new BnodeExtractor<>());

	private final ImmutableMap<IRI, Extractor<ObjectMap>> iriOrBnodeOrLiteralTerm = ImmutableMap.of(
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
			return iriTemplateFactory.getConstant(
							R2RMLVocabulary.resolveIri(constant.toString(), baseIri));
		}
		@Override
		public NonVariableTerm extract(Template template, T termMap) {
			return iriTemplateFactory.getTemplate(iriTemplateFactory.getComponents(
					R2RMLVocabulary.resolveIri(template.toString(), baseIri)));
		}
		@Override
		public 	NonVariableTerm extract(String column, T termMap) {
			return iriTemplateFactory.getColumn(column);
		}
	}

	private class BnodeExtractor<T extends TermMap> implements Extractor<T> {

		@Override
		public NonVariableTerm extract(RDFTerm constant, T termMap) {
			// https://www.w3.org/TR/r2rml/#constant says none can be an Bnode
			throw new R2RMLParsingBugException("Constant blank nodes are not accepted in R2RML (should have been detected earlier)");
		}
		@Override
		public NonVariableTerm extract(Template template, T termMap) {
			return bnodeTemplateFactory.getTemplate(bnodeTemplateFactory.getComponents(template.toString()));
		}
		@Override
		public 	NonVariableTerm extract(String column, T termMap) {
			return bnodeTemplateFactory.getColumn(column);
		}
	}

	private class LiteralExtractor<T extends ObjectMap> implements Extractor<T> {
		@Override
		public NonVariableTerm extract(RDFTerm constant, T om) {
			if (constant instanceof Literal) {
				return extract(literalTemplateFactory.getConstant(
						((Literal)constant).getLexicalForm()), om);
			}
			throw new R2RMLParsingBugException("Was expecting a Literal as constant, not a " + constant.getClass());
		}
		@Override
		public NonVariableTerm extract(Template template, T om) {
			return extract(literalTemplateFactory.getTemplate(
					literalTemplateFactory.getComponents(template.toString())), om);
		}
		@Override
		public 	NonVariableTerm extract(String column, T om) {
			return extract(literalTemplateFactory.getColumn(column), om);
		}

		private NonVariableTerm extract(ImmutableTerm t, T om) {
			RDFDatatype datatype = literalTemplateFactory.extractDatatype(
						Optional.ofNullable(om.getLanguageTag()),
						Optional.ofNullable(om.getDatatype()))
					// Third try: datatype of the constant
					.orElseGet(() -> Optional.ofNullable(om.getConstant())
							.map(c -> (Literal) c)
							.map(Literal::getDatatype)
							.map(typeFactory::getDatatype)
							// Default case: RDFS.LITERAL (abstract, to be inferred later)
							.orElseGet(typeFactory::getAbstractRDFSLiteral));

			return termFactory.getRDFLiteralFunctionalTerm(t,datatype);
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
