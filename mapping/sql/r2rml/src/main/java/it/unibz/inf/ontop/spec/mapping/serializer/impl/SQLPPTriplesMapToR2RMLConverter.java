package it.unibz.inf.ontop.spec.mapping.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import eu.optique.r2rml.api.MappingFactory;
import eu.optique.r2rml.api.model.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.template.TemplateFactory;
import it.unibz.inf.ontop.model.template.impl.BnodeTemplateFactory;
import it.unibz.inf.ontop.model.template.impl.IRITemplateFactory;
import it.unibz.inf.ontop.model.template.impl.LiteralTemplateFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.LanguageTag;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.spec.mapping.parser.impl.R2RMLVocabulary;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.*;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;


/**
 * Transform OBDA mappings in R2rml mappings
 * Initial @author s Sarah, Mindas, Timi, Guohui, Martin
 *
 */
public class SQLPPTriplesMapToR2RMLConverter {

	private final RDF rdfFactory;
	private final TermFactory termFactory;
	private final MappingFactory mappingFactory;

	private static final String baseIRIString = "urn:";

	public SQLPPTriplesMapToR2RMLConverter(RDF rdfFactory, TermFactory termFactory, MappingFactory mappingFactory) {
		this.rdfFactory = rdfFactory;
		this.termFactory = termFactory;
		this.mappingFactory = mappingFactory;
	}

    /**
	 * Get R2RML TriplesMaps from OBDA mapping axiom
	 */
	public Stream<TriplesMap> convert(SQLPPTriplesMap triplesMap) {

		// check if mapping id is an iri
		String mapping_id = triplesMap.getId();
		String mainNodeURLPrefix = !mapping_id.contains(":")
				? baseIRIString + mapping_id
				: mapping_id;

		LogicalTable logicalTable = mappingFactory.createR2RMLView(triplesMap.getSourceQuery().getSQL());

		ImmutableList<Map.Entry<RDFAtomPredicate, TargetAtom>> targetAtoms = triplesMap.getTargetAtoms().stream()
				.filter(t -> t.getProjectionAtom().getPredicate() instanceof RDFAtomPredicate)
				.map(t -> Maps.immutableEntry((RDFAtomPredicate)t.getProjectionAtom().getPredicate(), t))
				.collect(ImmutableCollectors.toList());

		ImmutableMap<ImmutableTerm, Collection<Map.Entry<RDFAtomPredicate, TargetAtom>>> subjectMap = targetAtoms.stream()
				.collect(ImmutableCollectors.toMultimap(
						e -> e.getKey().getSubject(e.getValue().getSubstitutedTerms()),
						e -> e))
				.asMap();

		return subjectMap.entrySet().stream()
				.flatMap(e -> processSameSubjectGroup(logicalTable, e.getKey(), e.getValue(), mainNodeURLPrefix));
	}

	private Stream<TriplesMap> processSameSubjectGroup(LogicalTable logicalTable,
													   ImmutableTerm subject,
													   Collection<Map.Entry<RDFAtomPredicate, TargetAtom>> targetAtoms,
													   String mainNodeIriPrefix) {

		ImmutableMap<Optional<ImmutableTerm>, Collection<Map.Entry<RDFAtomPredicate, TargetAtom>>> graphMap = targetAtoms.stream()
				.collect(ImmutableCollectors.toMultimap(
						e -> e.getKey().getGraph(e.getValue().getSubstitutedTerms()),
						e -> e))
				.asMap();

		return graphMap.entrySet().stream()
				.map(e -> processSameSubjectGraphGroup(logicalTable, subject, e.getKey(), e.getValue(), mainNodeIriPrefix));
	}

	private TriplesMap processSameSubjectGraphGroup(LogicalTable logicalTable,
													ImmutableTerm subject,
													Optional<ImmutableTerm> graph,
													Collection<Map.Entry<RDFAtomPredicate, TargetAtom>> targetAtoms,
													String mainNodeIriPrefix) {

		// Make sure we don't create triples map with the same name in case of multiple named graphs
		IRI iri = rdfFactory.createIRI(
				graph.map(t -> mainNodeIriPrefix + "-" + UUID.randomUUID().toString())
						.orElse(mainNodeIriPrefix));

		TriplesMap tm = mappingFactory.createTriplesMap(logicalTable, extractSubjectMap(subject), iri);

		graph.ifPresent(t -> tm.getSubjectMap().addGraphMap(extractGraphMap(t)));

		for (Map.Entry<RDFAtomPredicate, TargetAtom> e : targetAtoms)  {
			RDFAtomPredicate predicate = e.getKey();
			ImmutableList<ImmutableTerm> terms = e.getValue().getSubstitutedTerms();

			Optional<IRI> classIri = predicate.getClassIRI(terms);
			if (classIri.isPresent()) {
				tm.getSubjectMap().addClass(classIri.get());
			}
			else {
				tm.addPredicateObjectMap(mappingFactory.createPredicateObjectMap(
						extractPredicateMap(predicate.getProperty(terms)),
						extractObjectMap(predicate.getObject(terms))));
			}
		}

		return tm;
	}


	private static <T> T unsupportedBlankNode(BlankNode t) {
		throw new MinorOntopInternalBugException("Unexpected transformation: " + t.toString());
	}

	private SubjectMap extractSubjectMap(ImmutableTerm term) {
		return extractTermMap(term,
				getExtractor(mappingFactory::createSubjectMap,
				mappingFactory::createSubjectMap,
				mappingFactory::createSubjectMap,
				// TODO: allow blank nodes to appear in a subject map
				SQLPPTriplesMapToR2RMLConverter::unsupportedBlankNode));
	}

	private GraphMap extractGraphMap(ImmutableTerm term) {
		return extractTermMap(term,
				getExtractor(mappingFactory::createGraphMap,
				mappingFactory::createGraphMap,
				mappingFactory::createGraphMap,
				SQLPPTriplesMapToR2RMLConverter::unsupportedBlankNode));
	}

	private PredicateMap extractPredicateMap(ImmutableTerm term) {
		return extractTermMap(term,
				getExtractor(mappingFactory::createPredicateMap,
				mappingFactory::createPredicateMap,
				mappingFactory::createPredicateMap,
				SQLPPTriplesMapToR2RMLConverter::unsupportedBlankNode));
	}

	private ObjectMap extractObjectMap(ImmutableTerm term) {
		return extractTermMap(term,
				getExtractor(mappingFactory::createObjectMap,
				mappingFactory::createObjectMap,
				mappingFactory::createObjectMap,
				mappingFactory::createObjectMap,
				mappingFactory::createObjectMap));
	}

	private final BnodeTemplateFactory bnodeTemplateFactory = new BnodeTemplateFactory(null);
	private final IRITemplateFactory iriTemplateFactory = new IRITemplateFactory(null);
	private final LiteralTemplateFactory literalTemplateFactory = new LiteralTemplateFactory(null, null);

	private <T extends TermMap> Function<RDFTermType, Extractor<T>> getExtractor(
														  Function<Template, T> templateFct,
														  Function<String, T> columnFct,
														  Function<IRI, T> iriFct,
														  Function<BlankNode, T> bNodeFct) {
		return type -> {
			if (type instanceof ObjectRDFType) {
				return (((ObjectRDFType) type).isBlankNode())
						? new Extractor<>(R2RMLVocabulary.blankNode, bnodeTemplateFactory, columnFct, templateFct, v -> bNodeFct.apply(rdfFactory.createBlankNode(v)))
						: new Extractor<>(R2RMLVocabulary.iri, iriTemplateFactory, columnFct, templateFct, v -> iriFct.apply(rdfFactory.createIRI(v)));
			}
			throw new MinorOntopInternalBugException("Unexpected term type termMap: " + type);
		};
	}

	private <T extends ObjectMap> Function<RDFTermType, Extractor<T>> getExtractor(
														Function<Template, T> templateFct,
														Function<String, T> columnFct,
														Function<IRI, T> iriFct,
														Function<BlankNode, T> bNodeFct,
														Function<Literal, T> literalFct) {
		return type -> {
			if (type instanceof ObjectRDFType) {
				return (((ObjectRDFType) type).isBlankNode())
						? new Extractor<>(R2RMLVocabulary.blankNode, bnodeTemplateFactory, columnFct, templateFct, v -> bNodeFct.apply(rdfFactory.createBlankNode(v)))
						: new Extractor<>(R2RMLVocabulary.iri, iriTemplateFactory, columnFct, templateFct, v -> iriFct.apply(rdfFactory.createIRI(v)));
			} else if (type instanceof RDFDatatype) {
				RDFDatatype datatype = (RDFDatatype) type;
				return new LiteralExtractor<>(R2RMLVocabulary.literal, datatype, literalTemplateFactory, columnFct, templateFct, literalFct);
			}
			throw new MinorOntopInternalBugException("Unexpected term type termMap: " + type);
		};
	}

	private <T extends TermMap> T extractTermMap(ImmutableTerm term,
												 Function<RDFTermType, Extractor<T>> extractorFactory) {

		if (term instanceof RDFConstant) {
			RDFConstant constant = (RDFConstant) term;
			ImmutableTerm lexicalTerm =  termFactory.getDBStringConstant(constant.getValue());
			RDFTermType termType = constant.getType();
			return extractorFactory.apply(termType).extract(lexicalTerm);
		}

		ImmutableFunctionalTerm rdfFunctionalTerm = (ImmutableFunctionalTerm) term;

		ImmutableTerm lexicalTerm = DBTypeConversionFunctionSymbol.uncast(rdfFunctionalTerm.getTerm(0));

		// Might be abstract (e.g. partially defined literal map)
		RDFTermType termType = Optional.of(rdfFunctionalTerm.getTerm(1))
				.filter(t -> t instanceof RDFTermTypeConstant)
				.map(t -> (RDFTermTypeConstant) t)
				.map(RDFTermTypeConstant::getRDFTermType)
				.orElseThrow(() -> new R2RMLSerializationException(
						"Was expecting a RDFTermTypeConstant in the mapping assertion, not "
								+ rdfFunctionalTerm.getTerm(1)));

		return extractorFactory.apply(termType).extract(lexicalTerm);
	}


	private class Extractor<T extends TermMap> {
		final IRI termType;
		final TemplateFactory templateFactory;
		final Function<String, T> columnFct;
		final Function<Template, T> templateFct;
		final Function<String, T> constantFct;

		Extractor(IRI termType, TemplateFactory templateFactory, Function<String, T> columnFct, Function<Template, T> templateFct, Function<String, T> constantFct) {
			this.termType = termType;
			this.templateFactory = templateFactory;
			this.columnFct = columnFct;
			this.templateFct = templateFct;
			this.constantFct = constantFct;
		}

		public T extractConstant(DBConstant term) {
			return constantFct.apply(term.getValue());
		}
		public T extractColumn(Variable term) {
			T termMap = columnFct.apply(term.getName());
			termMap.setTermType(termType);
			return termMap;
		}
		public T extractTemplate(ImmutableFunctionalTerm term) {
			String templateString = templateFactory.serializeTemplateTerm(term);
			T termMap = templateFct.apply(mappingFactory.createTemplate(templateString));
			termMap.setTermType(termType);
			return termMap;
		}
		public T extract(ImmutableTerm term) {
			if (term instanceof DBConstant)
				return extractConstant((DBConstant)term);
			else if (term instanceof Variable)
				return extractColumn((Variable)term);
			else if (term instanceof ImmutableFunctionalTerm)
				return extractTemplate((ImmutableFunctionalTerm)term);

			throw new MinorOntopInternalBugException("Unexpected lexical term for an termMap: " + term);
		}
	}

	private class LiteralExtractor<T extends ObjectMap> extends Extractor<T> {
		final RDFDatatype datatype;
		final Function<Literal, T> constantFct;

		LiteralExtractor(IRI termType, RDFDatatype datatype, TemplateFactory templateFactory, Function<String, T> columnFct, Function<Template, T> templateFct, Function<Literal, T> constantFct) {
			super(termType, templateFactory, columnFct, templateFct, null);
			this.datatype = datatype;
			this.constantFct = constantFct;
		}

		@Override
		public T extractConstant(DBConstant term) {
			Literal literal = datatype.getLanguageTag()
					.map(lang -> rdfFactory.createLiteral(term.getValue(), lang.getFullString()))
					.orElseGet(() -> rdfFactory.createLiteral(term.getValue(), datatype.getIRI()));

			return constantFct.apply(literal);
		}
		public T extractColumn(Variable term) {
			T termMap = super.extractColumn(term);
			setDatatype(termMap, datatype);
			return termMap;
		}
		public T extractTemplate(ImmutableFunctionalTerm term) {
			T termMap = super.extractTemplate(term);
			setDatatype(termMap, datatype);
			return termMap;
		}

		private void setDatatype(ObjectMap objectMap, RDFDatatype datatype) {
			Optional<LanguageTag> optionalLangTag = datatype.getLanguageTag();
			if (optionalLangTag.isPresent())
				objectMap.setLanguageTag(optionalLangTag.get().getFullString());
			else if (!datatype.getIRI().equals(RDFS.LITERAL))
				objectMap.setDatatype(datatype.getIRI());
		}
	}



	/**
	 * TODO: shall we consider as an internal bug or differently?
	 */
	static class R2RMLSerializationException extends OntopInternalBugException {
		private R2RMLSerializationException(String message) {
			super(message);
		}
	}
}
