package it.unibz.inf.ontop.spec.mapping.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import eu.optique.r2rml.api.MappingFactory;
import eu.optique.r2rml.api.model.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
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
	private final PrefixManager prefixManager;

	private static final String baseIRIString = "urn:";

	public SQLPPTriplesMapToR2RMLConverter(RDF rdfFactory, TermFactory termFactory, MappingFactory mappingFactory, PrefixManager prefixManager) {
		this.rdfFactory = rdfFactory;
		this.termFactory = termFactory;
		this.mappingFactory = mappingFactory;
		this.prefixManager = prefixManager;
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

	private <T extends TermMap> T extractTermMap(ImmutableTerm term,
												 Function<Template, T> templateFct,
												 Function<String, T> columnFct,
												 Function<IRI, T> iriFct,
												 Function<BlankNode, T> bNodeFct,
												 Function<Literal, T> literalFct) {

		if (term instanceof RDFConstant) {
			RDFConstant constant = (RDFConstant) term;
			ImmutableTerm lexicalTerm =  termFactory.getDBStringConstant(constant.getValue());
			RDFTermType termType = constant.getType();
			return extract(termType, lexicalTerm, templateFct, columnFct, iriFct, bNodeFct, literalFct);
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

		return extract(termType, lexicalTerm, templateFct, columnFct, iriFct, bNodeFct, literalFct);
	}

	private <T extends TermMap> T extract(RDFTermType termType, ImmutableTerm lexicalTerm,
												 Function<Template, T> templateFct,
												 Function<String, T> columnFct,
												 Function<IRI, T> iriFct,
												 Function<BlankNode, T> bNodeFct,
												 Function<Literal, T> literalFct) {
		if (termType instanceof ObjectRDFType) {
			if (((ObjectRDFType) termType).isBlankNode())
				return extractBnodeTermMap(lexicalTerm, templateFct, columnFct, bNodeFct);
			else
				return extractIriTermMap(lexicalTerm, templateFct, columnFct, iriFct);
		}
		if (termType instanceof RDFDatatype)
			return extractLiteralTermMap(lexicalTerm, (RDFDatatype) termType, templateFct, columnFct, literalFct);

		throw new MinorOntopInternalBugException("An RDF termType must be either an object type or a datatype");
	}

	private <T extends TermMap> T extractConstantIriTermMap(String lexicalString, Function<IRI, T> iriFct) {
		return iriFct.apply(rdfFactory.createIRI(lexicalString));
	}

	private <T extends TermMap> T extractColumnIriTermMap(Variable variable, Function<String, T> columnFct) {
		T termMap = columnFct.apply(variable.getName());
		termMap.setTermType(R2RMLVocabulary.iri);
		return termMap;
	}

	private <T extends TermMap> T extractTemplateIriTermMap(ImmutableFunctionalTerm functionalTerm, Function<Template, T> templateFct) {
		String templateString = iriTemplateFactory.serializeTemplateTerm(functionalTerm);
		T termMap = templateFct.apply(mappingFactory.createTemplate(templateString));
		termMap.setTermType(R2RMLVocabulary.iri);
		return termMap;
	}

	private <T extends TermMap> T extractIriTermMap(ImmutableTerm lexicalTerm,
                                                           Function<Template, T> templateFct,
                                                           Function<String, T> columnFct,
                                                           Function<IRI, T> iriFct) {
		if (lexicalTerm instanceof DBConstant)
			return extractConstantIriTermMap(((DBConstant) lexicalTerm).getValue(), iriFct);

		if (lexicalTerm instanceof Variable)
			return extractColumnIriTermMap((Variable) lexicalTerm, columnFct);

		if (lexicalTerm instanceof ImmutableFunctionalTerm)
			return extractTemplateIriTermMap((ImmutableFunctionalTerm) lexicalTerm, templateFct);

		throw new MinorOntopInternalBugException("Unexpected lexical term for an IRI: " + lexicalTerm);
	}

	private <T extends TermMap> T extractConstantBnodeTermMap(String lexicalString, Function<BlankNode, T> bNodeFct) {
		return bNodeFct.apply(rdfFactory.createBlankNode(lexicalString));
	}

	private <T extends TermMap> T extractColumnBnodeTermMap(Variable variable, Function<String, T> columnFct) {
		T termMap = columnFct.apply(variable.getName());
		termMap.setTermType(R2RMLVocabulary.blankNode);
		return termMap;
	}

	private <T extends TermMap> T extractTemplateBnodeTermMap(ImmutableFunctionalTerm functionalTerm, Function<Template, T> templateFct) {
		String templateString = bnodeTemplateFactory.serializeTemplateTerm(functionalTerm);
		T termMap = templateFct.apply(mappingFactory.createTemplate(templateString));
		termMap.setTermType(R2RMLVocabulary.blankNode);
		return termMap;
	}

	private <T extends TermMap> T extractBnodeTermMap(ImmutableTerm lexicalTerm,
														   Function<Template, T> templateFct,
														   Function<String, T> columnFct,
														   Function<BlankNode, T> bNodeFct) {
		if (lexicalTerm instanceof DBConstant)
			return extractConstantBnodeTermMap(((DBConstant) lexicalTerm).getValue(), bNodeFct);

		if (lexicalTerm instanceof Variable)
			return extractColumnBnodeTermMap((Variable) lexicalTerm, columnFct);

		if (lexicalTerm instanceof ImmutableFunctionalTerm)
			return extractTemplateBnodeTermMap((ImmutableFunctionalTerm)lexicalTerm, templateFct);

		throw new MinorOntopInternalBugException("Unexpected lexical term for an Bnode: " + lexicalTerm);
	}


	private final BnodeTemplateFactory bnodeTemplateFactory = new BnodeTemplateFactory(null);
	private final IRITemplateFactory iriTemplateFactory = new IRITemplateFactory(null);
	private final LiteralTemplateFactory literalTemplateFactory = new LiteralTemplateFactory(null, null);


	/**
	 * NB: T is assumed to be an ObjectMap
	 */
	private <T extends TermMap> T extractLiteralTermMap(ImmutableTerm lexicalTerm,
														RDFDatatype datatype,
														Function<Template, T> templateFct,
														Function<String, T> columnFct,
														Function<Literal, T> literalFct) {
		T termMap;
		if (lexicalTerm instanceof DBConstant) {
			String lexicalString = ((DBConstant) lexicalTerm).getValue();
			Literal literal = datatype.getLanguageTag()
					.map(lang -> rdfFactory.createLiteral(lexicalString, lang.getFullString()))
					.orElseGet(() -> rdfFactory.createLiteral(lexicalString, datatype.getIRI()));
			termMap = literalFct.apply(literal);
		}
		else if (lexicalTerm instanceof Variable) {
			termMap = columnFct.apply(((Variable) lexicalTerm).getName());
			termMap.setTermType(R2RMLVocabulary.literal);
		}
		else if (lexicalTerm instanceof ImmutableFunctionalTerm) {
			ImmutableFunctionalTerm functionalLexicalTerm = (ImmutableFunctionalTerm) lexicalTerm;
			termMap = templateFct.apply(mappingFactory.createTemplate(
						literalTemplateFactory.serializeTemplateTerm(functionalLexicalTerm)));
			termMap.setTermType(R2RMLVocabulary.literal);
		}
		else {
			throw new MinorOntopInternalBugException("Unexpected lexical term for a literal: " + lexicalTerm);
		}

		if (!(termMap instanceof ObjectMap))
			throw new MinorOntopInternalBugException("The termMap was expected to be an ObjectMap");
		ObjectMap objectMap = (ObjectMap) termMap;

		Optional<LanguageTag> optionalLangTag = datatype.getLanguageTag();
		if (optionalLangTag.isPresent())
			objectMap.setLanguageTag(optionalLangTag.get().getFullString());
		else {
			/*
			 * Ontop may use rdfs:literal internally for some terms whose datatype is not specified in the obda mapping.
			 *  If the term is built from a column or pattern, then its datatype must be inferred from the DB schema
			 *  (according to the R2RML spec),
			 *  so the R2RML mapping should not use rr:datatype for this term map
			 */
			if (!datatype.isAbstract()
					&& (!datatype.getIRI().equals(RDFS.LITERAL) ||
					!(lexicalTerm instanceof Variable)))
				objectMap.setDatatype(datatype.getIRI());
		}

		return termMap;
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
