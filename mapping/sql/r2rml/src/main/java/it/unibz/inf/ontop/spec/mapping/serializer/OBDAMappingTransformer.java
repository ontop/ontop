package it.unibz.inf.ontop.spec.mapping.serializer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import eu.optique.r2rml.api.binding.rdf4j.RDF4JR2RMLMappingManager;
import eu.optique.r2rml.api.model.*;
import it.unibz.inf.ontop.exception.InvalidPrefixWritingException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
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
import it.unibz.inf.ontop.spec.mapping.impl.SQLQueryImpl;
import it.unibz.inf.ontop.spec.mapping.parser.impl.R2RMLVocabulary;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.Templates;
import org.apache.commons.rdf.api.*;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * Transform OBDA mappings in R2rml mappings
 * Initial @author s Sarah, Mindas, Timi, Guohui, Martin
 *
 * TODO: rename it R2RML serializer
 *
 */
public class OBDAMappingTransformer {

	private final RDF rdfFactory;
	private final TermFactory termFactory;
	private String baseIRIString;
	private final eu.optique.r2rml.api.MappingFactory mappingFactory;

	OBDAMappingTransformer(RDF rdfFactory, TermFactory termFactory) {
        this("urn:", rdfFactory, termFactory);
	}

    OBDAMappingTransformer(String baseIRIString, RDF rdfFactory, TermFactory termFactory) {
        this.baseIRIString = baseIRIString;
		this.rdfFactory = rdfFactory;
		this.termFactory = termFactory;
		this.mappingFactory = RDF4JR2RMLMappingManager.getInstance().getMappingFactory();
	}

    /**
	 * Get R2RML TriplesMaps from OBDA mapping axiom
	 */
	public Stream<TriplesMap> getTriplesMaps(SQLPPTriplesMap triplesMap, PrefixManager prefixManager) {

		SQLQueryImpl squery = (SQLQueryImpl) triplesMap.getSourceQuery();
		ImmutableList<TargetAtom> targetAtoms = triplesMap.getTargetAtoms();

		//triplesMap node
		String mapping_id = triplesMap.getId();

		// check if mapping id is an iri
		if (!mapping_id.contains(":")) {
            mapping_id = baseIRIString + mapping_id;
        }
		BlankNodeOrIRI mainNode = rdfFactory.createIRI(mapping_id);

		//Table
		LogicalTable logicalTable = mappingFactory.createR2RMLView(squery.getSQLQuery());

		ImmutableMultimap<ImmutableTerm, TargetAtom> targetAtomMultimap = targetAtoms.stream()
				.collect(ImmutableCollectors.toMultimap(
						a -> a.getSubstitutedTerm(0),
						a -> a));

		// Creates a triples map per subject map
		return targetAtomMultimap.asMap().entrySet().stream()
				.map(e -> extractTriplesMap(logicalTable, e.getKey(), e.getValue(), mainNode, prefixManager));
	}

	private TriplesMap extractTriplesMap(LogicalTable logicalTable, ImmutableTerm substitutedTerm,
										 Collection<TargetAtom> targetAtoms, BlankNodeOrIRI mainNode,
										 PrefixManager prefixManager) {
		SubjectMap sm = extractSubjectMap(substitutedTerm, prefixManager);
		TriplesMap tm = mappingFactory.createTriplesMap(logicalTable, sm, mainNode);

		ImmutableMap<Boolean, ImmutableList<TargetAtom>> targetAtomClassification = targetAtoms.stream()
				.collect(ImmutableCollectors.partitioningBy(OBDAMappingTransformer::isConstantClassTargetAtom));

		// Constant classes
		Optional.ofNullable(targetAtomClassification.get(true))
				.map(Collection::stream)
				.orElse(Stream.empty())
				.map(this::extractClassIRIFromConstantClassTargetAtom)
				.forEach(sm::addClass);


		// Other target atoms -> predicate object map
		Optional.ofNullable(targetAtomClassification.get(false))
				.map(Collection::stream)
				.orElse(Stream.empty())
				.map(a -> convertIntoPredicateObjectMap(a, prefixManager))
				.forEach(tm::addPredicateObjectMap);

		return tm;

	}

	private static boolean isConstantClassTargetAtom(TargetAtom targetAtom) {
		return Optional.of(targetAtom.getProjectionAtom())
				.filter(a -> a.getPredicate() instanceof RDFAtomPredicate)
				.flatMap(a -> ((RDFAtomPredicate) a.getPredicate()).getClassIRI(targetAtom.getSubstitutedTerms()))
				.isPresent();
	}

	private IRI extractClassIRIFromConstantClassTargetAtom(TargetAtom targetAtom) {
		return Optional.of(targetAtom.getProjectionAtom())
				.filter(a -> a.getPredicate() instanceof RDFAtomPredicate)
				.flatMap(a -> ((RDFAtomPredicate) a.getPredicate()).getClassIRI(targetAtom.getSubstitutedTerms()))
				.orElseThrow(() -> new IllegalArgumentException("The target atom is expected to have a constant class"));
	}

	private PredicateObjectMap convertIntoPredicateObjectMap(TargetAtom targetAtom, PrefixManager prefixManager) {
		return mappingFactory.createPredicateObjectMap(
				extractPredicateMap(targetAtom, prefixManager),
				extractObjectMap(targetAtom, prefixManager));
	}

	private SubjectMap extractSubjectMap(ImmutableTerm substitutedTerm, PrefixManager prefixManager) {
		return extractTermMap(substitutedTerm, true, false,
				mappingFactory::createSubjectMap,
				mappingFactory::createSubjectMap,
				mappingFactory::createSubjectMap,
				// TODO: allow blank nodes to appear in a subject map
				l -> {
					throw new UnsupportedOperationException();
				},
				l -> {
					throw new UnsupportedOperationException();
				},
				prefixManager);
	}

	private PredicateMap extractPredicateMap(TargetAtom targetAtom, PrefixManager prefixManager) {
		return extractTermMap(targetAtom.getSubstitutedTerm(1), false, false,
				mappingFactory::createPredicateMap,
				mappingFactory::createPredicateMap,
				mappingFactory::createPredicateMap,
				l -> {
					throw new UnsupportedOperationException();
				},
				l -> {
					throw new UnsupportedOperationException();
				},
		        prefixManager);
	}

	private ObjectMap extractObjectMap(TargetAtom targetAtom, PrefixManager prefixManager) {
		return extractTermMap(targetAtom.getSubstitutedTerm(2), true, true,
				mappingFactory::createObjectMap,
				mappingFactory::createObjectMap,
				mappingFactory::createObjectMap,
				mappingFactory::createObjectMap,
				mappingFactory::createObjectMap,
				prefixManager);
	}

	private <T extends TermMap> T extractTermMap(ImmutableTerm substitutedTerm, boolean acceptBNode, boolean acceptLiterals,
												 java.util.function.Function<Template, T> templateFct,
												 java.util.function.Function<String, T> columnFct,
												 java.util.function.Function<IRI, T> iriFct,
												 java.util.function.Function<BlankNode, T> bNodeFct,
												 java.util.function.Function<Literal, T> literalFct,
												 PrefixManager prefixManager) {

		ImmutableFunctionalTerm rdfFunctionalTerm = Optional.of(substitutedTerm)
				.filter(t -> (t instanceof ImmutableFunctionalTerm) || (t instanceof RDFConstant))
				.map(t -> convertIntoRDFFunctionalTerm((NonVariableTerm) t))
				.filter(t -> t.getFunctionSymbol() instanceof RDFTermFunctionSymbol)
				.orElseThrow(() -> new R2RMLSerializationException(
						"Was expecting a RDFTerm functional or constant term, not " + substitutedTerm));

		ImmutableTerm lexicalTerm = uncast(rdfFunctionalTerm.getTerm(0));

		// Might be abstract (e.g. partially defined literal map)
		RDFTermType termType = Optional.of(rdfFunctionalTerm.getTerm(1))
				.filter(t -> t instanceof RDFTermTypeConstant)
				.map(t -> (RDFTermTypeConstant) t)
				.map(RDFTermTypeConstant::getRDFTermType)
				.orElseThrow(() -> new R2RMLSerializationException(
						"Was expecting a RDFTermTypeConstant in the mapping assertion, not "
								+ rdfFunctionalTerm.getTerm(1)));

		if (termType instanceof ObjectRDFType)
			return extractIriOrBnodeTermMap(lexicalTerm, (ObjectRDFType) termType, acceptBNode,
					templateFct, columnFct, iriFct, bNodeFct, prefixManager);
		else if (termType instanceof RDFDatatype)
			if (acceptLiterals)
				return extractLiteralTermMap(lexicalTerm, (RDFDatatype) termType, templateFct, columnFct, literalFct);
			else
				throw new MinorOntopInternalBugException("A literal term map has been found in an unexpected area: "
						+ substitutedTerm);
		else
			throw new MinorOntopInternalBugException("An RDF termType must be either an object type or a datatype");
	}

	private ImmutableFunctionalTerm convertIntoRDFFunctionalTerm(NonVariableTerm term) {
		if (term instanceof  RDFConstant) {
			RDFConstant constant = (RDFConstant) term;
			return termFactory.getRDFFunctionalTerm(
					termFactory.getDBStringConstant(constant.getValue()),
					termFactory.getRDFTermTypeConstant(constant.getType()));
		}
		else
			return (ImmutableFunctionalTerm) term;
	}

	private <T extends TermMap> T extractIriOrBnodeTermMap(ImmutableTerm lexicalTerm, ObjectRDFType termType, boolean acceptBNode,
                                                           java.util.function.Function<Template, T> templateFct,
                                                           java.util.function.Function<String, T> columnFct,
                                                           java.util.function.Function<IRI, T> iriFct,
                                                           java.util.function.Function<BlankNode, T> bNodeFct,
                                                           PrefixManager prefixManager) {
		if ((!acceptBNode) && termType.isBlankNode())
			throw new MinorOntopInternalBugException("Bnode term map found in an unexpected area: " + lexicalTerm);

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
			String templateString = getTemplate((ImmutableFunctionalTerm) lexicalTerm, prefixManager);
			termMap = templateFct.apply(mappingFactory.createTemplate(templateString));
		}
		else {
			throw new MinorOntopInternalBugException("Unexpected lexical term for an IRI/Bnode: " + lexicalTerm);
		}

		termMap.setTermType(termType.isBlankNode() ? R2RMLVocabulary.blankNode : R2RMLVocabulary.iri);
		return termMap;
	}

	private String getTemplate(ImmutableFunctionalTerm lexicalTerm, PrefixManager prefixManager) {
		FunctionSymbol functionSymbol = lexicalTerm.getFunctionSymbol();
		if (functionSymbol instanceof BnodeStringTemplateFunctionSymbol) {
			return "_:" + Templates.getTemplateString(lexicalTerm);
		}
		if (functionSymbol instanceof IRIStringTemplateFunctionSymbol) {
			return expandPrefix(Templates.getTemplateString(lexicalTerm), prefixManager);
		}
		if (functionSymbol instanceof DBConcatFunctionSymbol){
			return Templates.getDBConcatTemplateString(lexicalTerm);
		}
		throw new R2RMLSerializationException ("Unexpected function symbol "+functionSymbol + " in term "+lexicalTerm);
	}

	private String expandPrefix(String prefixedTemplate, PrefixManager prefixManager) {
		String expandedTemplate = prefixedTemplate;
		try {
			expandedTemplate = prefixManager.getExpandForm(prefixedTemplate);
		} catch (InvalidPrefixWritingException e){

		}
		return expandedTemplate;
	}

	/**
	 * NB: T is assumed to be an ObjectMap
	 */
	private <T extends TermMap> T extractLiteralTermMap(ImmutableTerm lexicalTerm, RDFDatatype datatype,
														java.util.function.Function<Template, T> templateFct,
														java.util.function.Function<String, T> columnFct,
														java.util.function.Function<Literal, T> literalFct) {
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
			Predicate functionSymbol = functionalLexicalTerm.getFunctionSymbol();

			if (functionSymbol instanceof DBConcatFunctionSymbol) { //concat
				termMap = templateFct.apply(mappingFactory.createTemplate(
						TargetQueryRenderer.displayConcat(functionalLexicalTerm)));
			} else
				throw new R2RMLSerializationException("Unexpected function symbol: " + functionSymbol);
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
		if(!datatype.equals(RDFS.LITERAL))
			return false;
		if(lexicalTerm instanceof Variable)
			return true;
		if(lexicalTerm instanceof ImmutableFunctionalTerm){
			FunctionSymbol fs = ((ImmutableFunctionalTerm) lexicalTerm).getFunctionSymbol();
			if(fs instanceof BnodeStringTemplateFunctionSymbol || fs instanceof IRIStringTemplateFunctionSymbol)
				return true;
		}
		return false;
	}

	private ImmutableTerm uncast(ImmutableTerm lexicalTerm) {
		return Optional.of(lexicalTerm)
				.filter(t -> t instanceof ImmutableFunctionalTerm)
				.map(t -> (ImmutableFunctionalTerm) t)
				.filter(t -> (t.getFunctionSymbol() instanceof DBTypeConversionFunctionSymbol)
						&& t.getFunctionSymbol().getArity() == 1)
				.map(t -> t.getTerm(0))
				.orElse(lexicalTerm);
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
