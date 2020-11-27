package it.unibz.inf.ontop.spec.mapping.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import eu.optique.r2rml.api.MappingFactory;
import eu.optique.r2rml.api.model.*;
import it.unibz.inf.ontop.exception.InvalidPrefixWritingException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.template.impl.BnodeTemplateFactory;
import it.unibz.inf.ontop.model.template.impl.IRITemplateFactory;
import it.unibz.inf.ontop.model.template.impl.LiteralTemplateFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
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

		ImmutableFunctionalTerm rdfFunctionalTerm = Optional.of(term)
				.filter(t -> t instanceof NonVariableTerm)
				.map(t -> convertIntoRDFFunctionalTerm((NonVariableTerm) t))
				.filter(t -> t.getFunctionSymbol() instanceof RDFTermFunctionSymbol)
				.orElseThrow(() -> new R2RMLSerializationException(
						"Was expecting a RDFTerm functional or constant term, not " + term));

		ImmutableTerm lexicalTerm = DBTypeConversionFunctionSymbol.uncast(rdfFunctionalTerm.getTerm(0));

		// Might be abstract (e.g. partially defined literal map)
		RDFTermType termType = Optional.of(rdfFunctionalTerm.getTerm(1))
				.filter(t -> t instanceof RDFTermTypeConstant)
				.map(t -> (RDFTermTypeConstant) t)
				.map(RDFTermTypeConstant::getRDFTermType)
				.orElseThrow(() -> new R2RMLSerializationException(
						"Was expecting a RDFTermTypeConstant in the mapping assertion, not "
								+ rdfFunctionalTerm.getTerm(1)));

		if (termType instanceof ObjectRDFType)
			return extractIriOrBnodeTermMap(lexicalTerm, (ObjectRDFType) termType, templateFct, columnFct, iriFct, bNodeFct);
		if (termType instanceof RDFDatatype)
			return extractLiteralTermMap(lexicalTerm, (RDFDatatype) termType, templateFct, columnFct, literalFct);

		throw new MinorOntopInternalBugException("An RDF termType must be either an object type or a datatype");
	}

	private ImmutableFunctionalTerm convertIntoRDFFunctionalTerm(NonVariableTerm term) {
		if (term instanceof  RDFConstant) {
			RDFConstant constant = (RDFConstant) term;
			return termFactory.getRDFFunctionalTerm(
					termFactory.getDBStringConstant(constant.getValue()),
					termFactory.getRDFTermTypeConstant(constant.getType()));
		}
		return (ImmutableFunctionalTerm) term;
	}

	private <T extends TermMap> T extractIriOrBnodeTermMap(ImmutableTerm lexicalTerm,
														   ObjectRDFType termType,
                                                           Function<Template, T> templateFct,
                                                           Function<String, T> columnFct,
                                                           Function<IRI, T> iriFct,
                                                           Function<BlankNode, T> bNodeFct) {
		T termMap;
		if (lexicalTerm instanceof DBConstant) { //fixed string
			String lexicalString = ((DBConstant) lexicalTerm).getValue();
			termMap = termType.isBlankNode()
					? bNodeFct.apply(rdfFactory.createBlankNode(lexicalString))
					: iriFct.apply(rdfFactory.createIRI(lexicalString));
		}
		else if (lexicalTerm instanceof Variable) {
			termMap = columnFct.apply(((Variable) lexicalTerm).getName());
		}
		else if (lexicalTerm instanceof ImmutableFunctionalTerm) {
			String templateString = getTemplate((ImmutableFunctionalTerm) lexicalTerm);
			termMap = templateFct.apply(mappingFactory.createTemplate(templateString));
		}
		else {
			throw new MinorOntopInternalBugException("Unexpected lexical term for an IRI/Bnode: " + lexicalTerm);
		}

		termMap.setTermType(termType.isBlankNode() ? R2RMLVocabulary.blankNode : R2RMLVocabulary.iri);
		return termMap;
	}

	private final BnodeTemplateFactory bnodeTemplateFactory = new BnodeTemplateFactory(null);
	private final IRITemplateFactory iriTemplateFactory = new IRITemplateFactory(null);
	private final LiteralTemplateFactory literalTemplateFactory = new LiteralTemplateFactory(null, null);

	private String getTemplate(ImmutableFunctionalTerm lexicalTerm) {
		FunctionSymbol functionSymbol = lexicalTerm.getFunctionSymbol();
		if (functionSymbol instanceof BnodeStringTemplateFunctionSymbol) {
			return bnodeTemplateFactory.serializeTemplateTerm(lexicalTerm);
		}
		if (functionSymbol instanceof IRIStringTemplateFunctionSymbol) {
			String prefixedTemplate = iriTemplateFactory.serializeTemplateTerm(lexicalTerm);
			try {
				String expanded = prefixManager.getExpandForm(prefixedTemplate);
				if (!expanded.equals(prefixedTemplate))
					System.out.println("TEMPLATE BINGO");
				return expanded;
			}
			catch (InvalidPrefixWritingException e) {
				return prefixedTemplate;
			}
		}
		throw new R2RMLSerializationException("Unexpected function symbol " + functionSymbol + " in term " + lexicalTerm);
	}


	/**
	 * NB: T is assumed to be an ObjectMap
	 */
	private <T extends TermMap> T extractLiteralTermMap(ImmutableTerm lexicalTerm,
														RDFDatatype datatype,
														Function<Template, T> templateFct,
														Function<String, T> columnFct,
														Function<Literal, T> literalFct) {
		T termMap;
		if (lexicalTerm instanceof Variable) {
			termMap = columnFct.apply(((Variable) lexicalTerm).getName());
		}
		else if (lexicalTerm instanceof DBConstant) {
			String lexicalString = ((DBConstant) lexicalTerm).getValue();
			Literal literal = datatype.getLanguageTag()
					.map(lang -> rdfFactory.createLiteral(lexicalString, lang.getFullString()))
					.orElseGet(() -> rdfFactory.createLiteral(lexicalString, datatype.getIRI()));
			termMap = literalFct.apply(literal);
		}
		else if (lexicalTerm instanceof ImmutableFunctionalTerm) {
			ImmutableFunctionalTerm functionalLexicalTerm = (ImmutableFunctionalTerm) lexicalTerm;
			if (functionalLexicalTerm.getFunctionSymbol() instanceof DBConcatFunctionSymbol) { //concat
				termMap = templateFct.apply(mappingFactory.createTemplate(
						literalTemplateFactory.serializeTemplateTerm(functionalLexicalTerm)));
			}
			else
				throw new R2RMLSerializationException("Unexpected function symbol in: " + lexicalTerm);
		}
		else {
			throw new MinorOntopInternalBugException("Unexpected lexical term for a literal: " + lexicalTerm);
		}

		termMap.setTermType(R2RMLVocabulary.literal);

		if (!(termMap instanceof ObjectMap))
			throw new MinorOntopInternalBugException("The termMap was expected to be an ObjectMap");
		ObjectMap objectMap = (ObjectMap) termMap;

		Optional<LanguageTag> optionalLangTag = datatype.getLanguageTag();
		if (optionalLangTag.isPresent())
			objectMap.setLanguageTag(optionalLangTag.get().getFullString());
		else if (!datatype.isAbstract()
				&& !isOntopInternalRDFLiteral(datatype, lexicalTerm))
			objectMap.setDatatype(datatype.getIRI());

		return termMap;
	}

	/**
	 * Ontop may use rdfs:literal internally for some terms whose datatype is not specified in the obda mapping.
	 *  If the term is built from a column or pattern, then its datatype must be inferred from the DB schema (according to the R2RML spec),
	 *  so the R2RML mapping should not use rr:datatype for this term map
	 */
	private boolean isOntopInternalRDFLiteral(RDFDatatype datatype, ImmutableTerm lexicalTerm) {
		if (!datatype.getIRI().equals(RDFS.LITERAL))
			return false;
		if (lexicalTerm instanceof Variable)
			return true;
		if (lexicalTerm instanceof ImmutableFunctionalTerm) {
			FunctionSymbol fs = ((ImmutableFunctionalTerm) lexicalTerm).getFunctionSymbol();
			return fs instanceof BnodeStringTemplateFunctionSymbol || fs instanceof IRIStringTemplateFunctionSymbol;
		}
		return false;
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
