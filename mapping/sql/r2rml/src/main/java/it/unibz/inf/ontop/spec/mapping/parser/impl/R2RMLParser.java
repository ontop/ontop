package it.unibz.inf.ontop.spec.mapping.parser.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import eu.optique.r2rml.api.binding.rdf4j.RDF4JR2RMLMappingManager;
import eu.optique.r2rml.api.model.*;
import eu.optique.r2rml.api.model.impl.InvalidR2RMLMappingException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.template.TemplateFactory;
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

	private final String baseIri = "http://example.com/base/";

	private final ImmutableMap<IRI, TermMapFactory<TermMap, ? extends TemplateFactory>> iriTerm;
	private final ImmutableMap<IRI, TermMapFactory<TermMap, ? extends TemplateFactory>> iriOrBnodeTerm;
	private final ImmutableMap<IRI, TermMapFactory<ObjectMap, ? extends TemplateFactory>> iriOrBnodeOrLiteralTerm;

	@Inject
	private R2RMLParser(TermFactory termFactory, TypeFactory typeFactory) {
		this.termFactory = termFactory;
		this.manager = RDF4JR2RMLMappingManager.getInstance();

		IRITemplateFactory iriTemplateFactory = new IRITemplateFactory(termFactory);
		BnodeTemplateFactory bnodeTemplateFactory = new BnodeTemplateFactory(termFactory);
		LiteralTemplateFactory literalTemplateFactory = new LiteralTemplateFactory(termFactory, typeFactory);

		this.iriTerm = ImmutableMap.of(
				R2RMLVocabulary.iri, new IriTermMapFactory<>(iriTemplateFactory));
		this.iriOrBnodeTerm = ImmutableMap.of(
				R2RMLVocabulary.iri, new IriTermMapFactory<>(iriTemplateFactory),
				R2RMLVocabulary.blankNode, new BnodeTermMapFactory<>(bnodeTemplateFactory));
		this.iriOrBnodeOrLiteralTerm = ImmutableMap.of(
				R2RMLVocabulary.iri, new IriTermMapFactory<>(iriTemplateFactory),
				R2RMLVocabulary.blankNode, new BnodeTermMapFactory<>(bnodeTemplateFactory),
				R2RMLVocabulary.literal, new LiteralTermMapFactory<>(literalTemplateFactory));
	}

	/**
	 * method to get the TriplesMaps from the given Graph
	 * @param graph - the Graph to process
	 * @return the collection of triples maps
	 */
	public Collection<TriplesMap> extractTripleMaps(Graph graph) throws InvalidR2RMLMappingException {
		return manager.importMappings(graph);
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


	private <T extends TermMap> NonVariableTerm extract(ImmutableMap<IRI, TermMapFactory<T, ? extends TemplateFactory>> map, T termMap) {
		return map.computeIfAbsent(termMap.getTermType(), k -> {
			throw new R2RMLParsingBugException("Was expecting one of " + map.keySet() +
						" when encountered " + termMap);
		}).extract(termMap);
	}

	/*
	   @see it.unibz.inf.ontop.spec.mapping.serializer.impl.SQLPPTriplesMapToR2RMLConverter
	 */

	private abstract static class TermMapFactory<T extends TermMap, F extends TemplateFactory> {
		protected final F templateFactory;
		public TermMapFactory(F templateFactory) {
			this.templateFactory = templateFactory;
		}

		protected abstract NonVariableTerm onConstant(RDFTerm constant);

		protected NonVariableTerm onColumn(String column) {
			return templateFactory.getColumn(column);
		}

		protected NonVariableTerm onTemplate(String template) {
			return templateFactory.getTemplate(templateFactory.getComponents(template));
		}

		public NonVariableTerm extract(T termMap) {
			if (termMap.getConstant() != null)
				return onConstant(termMap.getConstant());

			if (termMap.getTemplate() != null)
				return onTemplate(termMap.getTemplate().toString());

			if (termMap.getColumn() != null)
				return onColumn(termMap.getColumn());

			throw new R2RMLParsingBugException("A term map is either constant-valued, column-valued or template-valued.");
		}
	}

	private class IriTermMapFactory<T extends TermMap> extends TermMapFactory<T, IRITemplateFactory> {
		public IriTermMapFactory(IRITemplateFactory templateFactory) {
			super(templateFactory);
		}

		@Override
		protected NonVariableTerm onConstant(RDFTerm constant) {
			return templateFactory.getConstant(
							R2RMLVocabulary.resolveIri(constant.toString(), baseIri));
		}
		@Override
		protected NonVariableTerm onTemplate(String template) {
			return super.onTemplate(R2RMLVocabulary.resolveIri(template, baseIri));
		}
	}

	private static class BnodeTermMapFactory<T extends TermMap> extends TermMapFactory<T, BnodeTemplateFactory> {
		public BnodeTermMapFactory(BnodeTemplateFactory templateFactory) {
			super(templateFactory);
		}

		@Override
		protected NonVariableTerm onConstant(RDFTerm constant) {
			// https://www.w3.org/TR/r2rml/#constant says none can be an Bnode
			throw new R2RMLParsingBugException("Constant blank nodes are not accepted in R2RML (should have been detected earlier)");
		}
	}

	private class LiteralTermMapFactory<T extends ObjectMap> extends TermMapFactory<T, LiteralTemplateFactory> {
		public LiteralTermMapFactory(LiteralTemplateFactory templateFactory) {
			super(templateFactory);
		}
		@Override
		protected NonVariableTerm onConstant(RDFTerm constant) {
			if (constant instanceof Literal) {
				return templateFactory.getConstant(((Literal)constant).getLexicalForm());
			}
			throw new R2RMLParsingBugException("Was expecting a Literal as constant, not a " + constant.getClass());
		}

		@Override
		public NonVariableTerm extract(T om) {
			RDFDatatype datatype = templateFactory.extractDatatype(
						Optional.ofNullable(om.getLanguageTag()),
						Optional.ofNullable(om.getDatatype()))
					// Third try: datatype of the constant
					.orElseGet(() -> Optional.ofNullable(om.getConstant())
							.map(c -> (Literal) c)
							.map(Literal::getDatatype)
							.map(templateFactory::getDatatype)
							// Default case: RDFS.LITERAL (abstract, to be inferred later)
							.orElseGet(templateFactory::getAbstractRDFSLiteral));

			return termFactory.getRDFLiteralFunctionalTerm(super.extract(om), datatype);
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
