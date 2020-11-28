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
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.parser.impl.R2RMLVocabulary;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.*;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
	private static <T> T unsupportedLiteral(Literal t) {
		throw new MinorOntopInternalBugException("Unexpected transformation: " + t.toString());
	}

	private SubjectMap extractSubjectMap(ImmutableTerm term) {
		return extractTermMap(term,
				mappingFactory::createSubjectMap,
				mappingFactory::createSubjectMap,
				mappingFactory::createSubjectMap,
				// TODO: allow blank nodes to appear in a subject map
				SQLPPTriplesMapToR2RMLConverter::unsupportedBlankNode,
				SQLPPTriplesMapToR2RMLConverter::unsupportedLiteral);
	}

	private GraphMap extractGraphMap(ImmutableTerm term) {
		return extractTermMap(term,
				mappingFactory::createGraphMap,
				mappingFactory::createGraphMap,
				mappingFactory::createGraphMap,
				SQLPPTriplesMapToR2RMLConverter::unsupportedBlankNode,
				SQLPPTriplesMapToR2RMLConverter::unsupportedLiteral);
	}

	private PredicateMap extractPredicateMap(ImmutableTerm term) {
		return extractTermMap(term,
				mappingFactory::createPredicateMap,
				mappingFactory::createPredicateMap,
				mappingFactory::createPredicateMap,
				SQLPPTriplesMapToR2RMLConverter::unsupportedBlankNode,
				SQLPPTriplesMapToR2RMLConverter::unsupportedLiteral);
	}

	private ObjectMap extractObjectMap(ImmutableTerm term) {
		return extractTermMap(term,
				mappingFactory::createObjectMap,
				mappingFactory::createObjectMap,
				mappingFactory::createObjectMap,
				mappingFactory::createObjectMap,
				mappingFactory::createObjectMap);
	}

	private final BnodeTemplateFactory bnodeTemplateFactory = new BnodeTemplateFactory(null);
	private final IRITemplateFactory iriTemplateFactory = new IRITemplateFactory(null);
	private final LiteralTemplateFactory literalTemplateFactory = new LiteralTemplateFactory(null, null);


	private <T extends TermMap> T extractTermMap(ImmutableTerm term,
												 Function<Template, T> templateFct,
												 Function<String, T> columnFct,
												 Function<IRI, T> iriFct,
												 Function<BlankNode, T> bNodeFct,
												 Function<Literal, T> literalFct) {

		Extractor<T, IRI> iriExtractor = new Extractor<>(R2RMLVocabulary.iri, iriTemplateFactory, columnFct, templateFct, iriFct);
		Extractor<T, BlankNode> bnodeExtractor = new Extractor<>(R2RMLVocabulary.blankNode, bnodeTemplateFactory, columnFct, templateFct, bNodeFct);
		Extractor<T, Literal> literalExtractor = new Extractor<>(R2RMLVocabulary.literal, literalTemplateFactory, columnFct, templateFct, literalFct);

		if (term instanceof RDFConstant) {
			System.out.println("RDFConstant BINGO: " + term);
			RDFConstant constant = (RDFConstant) term;
			ImmutableTerm lexicalTerm =  termFactory.getDBStringConstant(constant.getValue());
			RDFTermType termType = constant.getType();
			return extract(termType, lexicalTerm, iriExtractor, bnodeExtractor, literalExtractor);
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

		return extract(termType, lexicalTerm, iriExtractor, bnodeExtractor, literalExtractor);
	}

	private <T extends TermMap> T extract(RDFTermType termType,
										  ImmutableTerm lexicalTerm,
										  Extractor<T, IRI> iriExtractor,
										  Extractor<T, BlankNode> bnodeExtractor,
										  Extractor<T, Literal> literalExtractor) {

		if (termType instanceof ObjectRDFType) {
			return (((ObjectRDFType) termType).isBlankNode())
				? bnodeExtractor.extract(lexicalTerm, rdfFactory::createBlankNode)
			    : iriExtractor.extract(lexicalTerm, rdfFactory::createIRI);
		}
		else if (termType instanceof RDFDatatype) {
			RDFDatatype datatype = (RDFDatatype) termType;
			T termMap = literalExtractor.extract(lexicalTerm, v -> createLiteral(v, datatype));

			ObjectMap objectMap = (ObjectMap) termMap;
			Optional<LanguageTag> optionalLangTag = datatype.getLanguageTag();
			if (optionalLangTag.isPresent())
				objectMap.setLanguageTag(optionalLangTag.get().getFullString());
			else if (!datatype.getIRI().equals(RDFS.LITERAL))
					objectMap.setDatatype(datatype.getIRI());

			return termMap;
		}

		throw new MinorOntopInternalBugException("An RDF termType must be either an object type or a datatype");
	}

	private class Extractor<T extends TermMap, C> {
		final IRI termType;
		final TemplateFactory templateFactory;
		final Function<String, T> columnFct;
		final Function<Template, T> templateFct;
		final Function<C, T> constantFct;

		Extractor(IRI termType, TemplateFactory templateFactory, Function<String, T> columnFct, Function<Template, T> templateFct, Function<C, T> constantFct) {
			this.termType = termType;
			this.templateFactory = templateFactory;
			this.columnFct = columnFct;
			this.templateFct = templateFct;
			this.constantFct = constantFct;
		}

		public T extract(ImmutableTerm term, Function<String, C> cFct) {
			if (term instanceof DBConstant) {
				String v = ((DBConstant) term).getValue();
				return constantFct.apply(cFct.apply(v));
			}
			else if (term instanceof Variable) {
				T termMap = columnFct.apply(((Variable) term).getName());
				termMap.setTermType(termType);
				return termMap;
			}
			else if (term instanceof ImmutableFunctionalTerm) {
				String templateString = templateFactory.serializeTemplateTerm((ImmutableFunctionalTerm) term);
				T termMap = templateFct.apply(mappingFactory.createTemplate(templateString));
				termMap.setTermType(termType);
				return termMap;
			}

			throw new MinorOntopInternalBugException("Unexpected lexical term for an IRI: " + term);
		}
	}


	private Literal createLiteral(String lexicalString, RDFDatatype datatype) {
		return datatype.getLanguageTag()
				.map(lang -> rdfFactory.createLiteral(lexicalString, lang.getFullString()))
				.orElseGet(() -> rdfFactory.createLiteral(lexicalString, datatype.getIRI()));
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
