package it.unibz.inf.ontop.query.translation.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.LangSPARQLFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.AGG;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.RDF;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.algebra.*;

import java.util.Optional;
import java.util.Set;

public class RDF4JValueExprTranslator {

    private final Set<Variable> knownVariables;
    private final ImmutableMap<Variable, GroundTerm> externalBindings;
    private final boolean treatBNodeAsVariable;

    private final TermFactory termFactory;
    private final RDF rdfFactory;
    private final FunctionSymbolFactory functionSymbolFactory;
    private final RDF4JValueTranslator valueTranslator;
    private final VariableGenerator variableGenerator;

    public RDF4JValueExprTranslator(Set<Variable> knownVariables,
                                    ImmutableMap<Variable, GroundTerm> externalBindings,
                                    boolean treatBNodeAsVariable,
                                    TermFactory termFactory,
                                    RDF rdfFactory,
                                    TypeFactory typeFactory,
                                    FunctionSymbolFactory functionSymbolFactory,
                                    VariableGenerator variableGenerator){
        this.knownVariables = knownVariables;
        this.externalBindings = externalBindings;
        this.treatBNodeAsVariable = treatBNodeAsVariable;
        this.termFactory = termFactory;
        this.rdfFactory = rdfFactory;
        this.functionSymbolFactory = functionSymbolFactory;

        this.valueTranslator = new RDF4JValueTranslator(termFactory, rdfFactory, typeFactory);

        this.variableGenerator = variableGenerator;
    }


    /**
     * @param expr           expression
     * @return term
     */
    public ExtendedTerm getTerm(ValueExpr expr) {

        if (expr instanceof Var) {
            VariableOrGroundTerm var = translateRDF4JVar((Var) expr, false);
            return new ExtendedTerm(var);
        }
        if (expr instanceof ValueConstant) {
            Value v = ((ValueConstant) expr).getValue();
            return new ExtendedTerm(valueTranslator.getTermForLiteralOrIri(v));
        }
        if (expr instanceof Bound) {
            // BOUND (Sec 17.4.1.1)
            // xsd:boolean  BOUND (variable var)
            Var v = ((Bound) expr).getArg();
            Variable var = termFactory.getVariable(v.getName());
            ImmutableTerm boundTerm = knownVariables.contains(var)
                    ? getFunctionalTerm(SPARQL.BOUND, var)
                    : termFactory.getRDFLiteralConstant("false", XSD.BOOLEAN);
            return new ExtendedTerm(boundTerm);
        }
        if (expr instanceof UnaryValueOperator) {
            return getTerm((UnaryValueOperator) expr);
        }
        if (expr instanceof BinaryValueOperator) {
            return getTerm((BinaryValueOperator) expr);
        }
        if (expr instanceof FunctionCall) {
            FunctionCall f = (FunctionCall) expr;

            ImmutableList<ExtendedTerm> extendedTerms = f.getArgs().stream()
                    .map(this::getTerm)
                    .collect(ImmutableCollectors.toList());

            ImmutableList<ImmutableTerm> terms = extendedTerms.stream()
                    .map(ExtendedTerm::getTerm)
                    .collect(ImmutableCollectors.toList());

            ImmutableMap<Variable, Exists> existsMap = extendedTerms.stream()
                    .map(ExtendedTerm::getExistsMap)
                    .flatMap(s -> s.entrySet().stream())
                    .collect(ImmutableCollectors.toMap());

            String functionName = extractFunctionName(f.getURI());

            Optional<SPARQLFunctionSymbol> optionalFunctionSymbol = functionSymbolFactory.getSPARQLFunctionSymbol(
                    functionName, terms.size());

            if (optionalFunctionSymbol.isPresent()) {
                return new ExtendedTerm(termFactory.getImmutableFunctionalTerm(optionalFunctionSymbol.get(), terms), existsMap);
            }
        }
        if (expr instanceof NAryValueOperator) {
            return getTerm((NAryValueOperator) expr);
        }
        if (expr instanceof BNodeGenerator) {
            Optional<ExtendedTerm> term = Optional.ofNullable(((BNodeGenerator) expr).getNodeIdExpr())
                    .map(this::getTerm);

            return term
                    .map(t -> new ExtendedTerm(getFunctionalTerm(SPARQL.BNODE, t.getTerm()), t.getExistsMap()))
                    .orElseGet(() -> new ExtendedTerm(getFunctionalTerm(SPARQL.BNODE)));

        }
        if (expr instanceof If) {
            If ifExpr = (If) expr;

            ExtendedTerm condition = getTerm(ifExpr.getCondition());
            ExtendedTerm thenTerm = getTerm(ifExpr.getResult());
            ExtendedTerm elseTerm = getTerm(ifExpr.getAlternative());

            ImmutableMap<Variable, Exists> existsMap = ImmutableMap.<Variable, Exists>builder()
                    .putAll(condition.getExistsMap())
                    .putAll(thenTerm.getExistsMap())
                    .putAll(elseTerm.getExistsMap())
                    .build();

            return new ExtendedTerm(getFunctionalTerm(SPARQL.IF,
                        convertToXsdBooleanTerm(condition.getTerm()),
                        thenTerm.getTerm(),
                        elseTerm.getTerm()),
                    existsMap);
        }
        if (expr instanceof Exists) {
            Variable freshVariable = variableGenerator.generateNewVariable("prov");
            ImmutableFunctionalTerm boundTerm = getFunctionalTerm(SPARQL.BOUND,
                    termFactory.getRDFLiteralFunctionalTerm(freshVariable, XSD.STRING));

            return new ExtendedTerm(boundTerm,
                    ImmutableMap.of(freshVariable, (Exists)expr));
        }
        // other subclasses
        // SubQueryValueOperator
        // ValueExprTripleRef
        throw new RuntimeException(new OntopUnsupportedKGQueryException("The expression " + expr + " is not supported yet!"));
    }

    private ExtendedTerm getTerm(UnaryValueOperator expr) {

        if (expr.getArg() == null) {
            if (expr instanceof Count)  // O-ary count
                return new ExtendedTerm(getFunctionalTerm(SPARQL.COUNT));

            throw new RuntimeException(new OntopUnsupportedKGQueryException("The expression " + expr + " is not supported yet!"));
        }

        ExtendedTerm extendedTerm = getTerm(expr.getArg());

        if (expr instanceof AbstractAggregateOperator) {
            AbstractAggregateOperator aggExpr = (AbstractAggregateOperator) expr;
            if (aggExpr instanceof Count) { //Unary count
                return getAggregateExtendedTerm(SPARQL.COUNT, aggExpr.isDistinct(), extendedTerm);
            }
            if (aggExpr instanceof Avg) {
                return getAggregateExtendedTerm(SPARQL.AVG, aggExpr.isDistinct(), extendedTerm);
            }
            if (aggExpr instanceof Sum) {
                return new ExtendedTerm(getAggregateFunctionalTerm(SPARQL.SUM, aggExpr.isDistinct(), extendedTerm.getTerm()), extendedTerm.getExistsMap());
            }
            if (aggExpr instanceof Min) {
                return getFunctionalExtendedTerm(SPARQL.MIN, extendedTerm);
            }
            if (aggExpr instanceof Max) {
                return getFunctionalExtendedTerm(SPARQL.MAX, extendedTerm);
            }
            if (aggExpr instanceof Sample) {
                return getFunctionalExtendedTerm(SPARQL.SAMPLE, extendedTerm);
            }
            if (aggExpr instanceof GroupConcat) {
                String separator = Optional.ofNullable(((GroupConcat) aggExpr).getSeparator())
                        .map(e -> ((ValueConstant) e).getValue().stringValue())
                        .orElse(" "); // Default separator

                ImmutableFunctionalTerm aggTerm = termFactory.getImmutableFunctionalTerm(
                        functionSymbolFactory.getSPARQLGroupConcatFunctionSymbol(separator, aggExpr.isDistinct()),
                        extendedTerm.getTerm());
                return new ExtendedTerm(aggTerm, extendedTerm.getExistsMap());
            }
            if (aggExpr instanceof AggregateFunctionCall) {
                AggregateFunctionCall call = (AggregateFunctionCall) aggExpr;
                if(call.getIRI().startsWith(AGG.PREFIX)) {
                    ImmutableFunctionalTerm aggTerm = getAggregateFunctionalTerm(call.getIRI(), aggExpr.isDistinct(), extendedTerm.getTerm());
                    return new ExtendedTerm(aggTerm, extendedTerm.getExistsMap());
                }
            }
            throw new RuntimeException("Unreachable: all subclasses covered");
        }
        if (expr instanceof Not) {
            ImmutableFunctionalTerm notTerm = getFunctionalTerm(XPathFunction.NOT.getIRIString(), convertToXsdBooleanTerm(extendedTerm.getTerm()));
            return new ExtendedTerm(notTerm, extendedTerm.getExistsMap());
        }
        if (expr instanceof IsNumeric) {
            // isNumeric (Sec 17.4.2.4) for checking whether the argument is a numeric value
            return getFunctionalExtendedTerm(SPARQL.IS_NUMERIC, extendedTerm);
        }
        if (expr instanceof IsLiteral) {
            return getFunctionalExtendedTerm(SPARQL.IS_LITERAL, extendedTerm);
        }
        if (expr instanceof IsURI) {
            return getFunctionalExtendedTerm(SPARQL.IS_IRI, extendedTerm);
        }
        if (expr instanceof Str) {
            return getFunctionalExtendedTerm(SPARQL.STR, extendedTerm);
        }
        if (expr instanceof Datatype) {
            return getFunctionalExtendedTerm(SPARQL.DATATYPE, extendedTerm);
        }
        if (expr instanceof IsBNode) {
            return getFunctionalExtendedTerm(SPARQL.IS_BLANK, extendedTerm);
        }
        if (expr instanceof Lang) {
            if (expr.getArg() instanceof Var) {
                return getFunctionalExtendedTerm(SPARQL.LANG, extendedTerm);
            }
            throw new RuntimeException(new OntopUnsupportedKGQueryException("A variable or a value is expected in " + expr));
        }
        if (expr instanceof IRIFunction) {
            // IRIFunction: IRI (Sec 17.4.2.8) for constructing IRIs
            Optional<org.apache.commons.rdf.api.IRI> optionalBaseIRI = Optional.ofNullable(((IRIFunction) expr).getBaseURI())
                    .map(rdfFactory::createIRI);

            SPARQLFunctionSymbol functionSymbol = optionalBaseIRI
                    .map(functionSymbolFactory::getIRIFunctionSymbol)
                    .orElseGet(functionSymbolFactory::getIRIFunctionSymbol);

            ImmutableFunctionalTerm iriTerm = termFactory.getImmutableFunctionalTerm(functionSymbol, extendedTerm.getTerm());
            return new ExtendedTerm(iriTerm, extendedTerm.getExistsMap());
        }
        // subclasses missing:
        //  - IsResource
        //   - LocalName
        //   - Namespace
        //   - Label
        //   - Like
        throw new RuntimeException(new OntopUnsupportedKGQueryException("The expression " + expr + " is not supported yet!"));
    }

    private ExtendedTerm getAggregateExtendedTerm(String functionName, boolean isDistinct, ExtendedTerm extendedTerm) {
        ImmutableFunctionalTerm aggTerm = getAggregateFunctionalTerm(functionName, isDistinct, extendedTerm.getTerm());
        return new ExtendedTerm(aggTerm, extendedTerm.getExistsMap());
    }

    private ExtendedTerm getFunctionalExtendedTerm(String functionName, ExtendedTerm extendedTerm) {
        return new ExtendedTerm(getFunctionalTerm(functionName, extendedTerm.getTerm()), extendedTerm.getExistsMap());
    }

    private ExtendedTerm getTerm(BinaryValueOperator expr) {

        ExtendedTerm extendedTerm1 = getTerm(expr.getLeftArg());
        ExtendedTerm extendedTerm2 = getTerm(expr.getRightArg());

        ImmutableMap<Variable, Exists> existsMap = ImmutableMap.<Variable, Exists>builder()
                .putAll(extendedTerm1.getExistsMap())
                .putAll(extendedTerm2.getExistsMap())
                .build();
        ImmutableTerm term1 = extendedTerm1.getTerm();
        ImmutableTerm term2 = extendedTerm2.getTerm();

        if (expr instanceof And) {
            ImmutableFunctionalTerm andTerm = getFunctionalTerm(SPARQL.LOGICAL_AND, convertToXsdBooleanTerm(term1), convertToXsdBooleanTerm(term2));
            return new ExtendedTerm(andTerm, existsMap);
        }
        if (expr instanceof Or) {
            ImmutableFunctionalTerm orTerm = getFunctionalTerm(SPARQL.LOGICAL_OR, convertToXsdBooleanTerm(term1), convertToXsdBooleanTerm(term2));
            return new ExtendedTerm(orTerm, existsMap);
        }
        if (expr instanceof SameTerm) {
            // sameTerm (Sec 17.4.1.8)
            // Corresponds to the STRICT equality (same lexical value, same type)
            return new ExtendedTerm(getFunctionalTerm(SPARQL.SAME_TERM, term1, term2), existsMap);
        }
        if (expr instanceof Regex) {
            // REGEX (Sec 17.4.3.14)
            // xsd:boolean  REGEX (string literal text, simple literal pattern)
            // xsd:boolean  REGEX (string literal text, simple literal pattern, simple literal flags)
            Regex reg = (Regex) expr;
            if (reg.getFlagsArg() != null) {
                ExtendedTerm flagsTerm = getTerm(reg.getFlagsArg());
                ImmutableMap<Variable, Exists> updatedExistsMap = ImmutableMap.<Variable, Exists>builder()
                        .putAll(existsMap)
                        .putAll(flagsTerm.getExistsMap())
                        .build();
                return new ExtendedTerm(getFunctionalTerm(SPARQL.REGEX, term1, term2, flagsTerm.getTerm()), updatedExistsMap);
            }
            return new ExtendedTerm(getFunctionalTerm(SPARQL.REGEX, term1, term2), existsMap);
        }
        if (expr instanceof Compare) {
            switch (((Compare) expr).getOperator()) {
                case EQ:
                    return new ExtendedTerm(getFunctionalTerm(SPARQL.EQ, term1, term2), existsMap);
                case LT:
                    return new ExtendedTerm(getFunctionalTerm(SPARQL.LESS_THAN, term1, term2), existsMap);
                case GT:
                    return new ExtendedTerm(getFunctionalTerm(SPARQL.GREATER_THAN, term1, term2), existsMap);
                case NE:
                    return new ExtendedTerm(getFunctionalTerm(XPathFunction.NOT.getIRIString(),
                            getFunctionalTerm(SPARQL.EQ, term1, term2)), existsMap);
                case LE:
                    return new ExtendedTerm(getFunctionalTerm(XPathFunction.NOT.getIRIString(),
                            getFunctionalTerm(SPARQL.GREATER_THAN, term1, term2)), existsMap);
                case GE:
                    return new ExtendedTerm(getFunctionalTerm(XPathFunction.NOT.getIRIString(),
                            getFunctionalTerm(SPARQL.LESS_THAN, term1, term2)), existsMap);
                default:
                    throw new RuntimeException(new OntopUnsupportedKGQueryException("Unsupported operator: " + expr));
            }
        }
        if (expr instanceof MathExpr) {
            return new ExtendedTerm(getFunctionalTerm(NumericalOperations.get(((MathExpr) expr).getOperator()),
                    term1, term2), existsMap);
        }
        /*
         * Restriction: the first argument must be LANG(...) and the second  a constant
         * (for guaranteeing that the langMatches logic is not delegated to the native query)
         */
        if (expr instanceof LangMatches) {
            if (!(term1 instanceof ImmutableFunctionalTerm
                    && ((ImmutableFunctionalTerm) term1).getFunctionSymbol() instanceof LangSPARQLFunctionSymbol)
                    || !(term2 instanceof RDFConstant)) {
                throw new RuntimeException(new OntopUnsupportedKGQueryException("The function langMatches is " +
                        "only supported with lang(..) function for the first argument and a constant for the second"));
            }

            return new ExtendedTerm(getFunctionalTerm(SPARQL.LANG_MATCHES, term1, term2), existsMap);
        }
        throw new RuntimeException("Unreachable: all subclasses covered");
    }

    private ExtendedTerm getTerm(NAryValueOperator expr) {

        ImmutableList<ExtendedTerm> extendedTerms = expr.getArguments().stream()
                .map(this::getTerm)
                .collect(ImmutableCollectors.toList());

        ImmutableList<ImmutableTerm> terms = extendedTerms.stream()
                .map(ExtendedTerm::getTerm)
                .collect(ImmutableCollectors.toList());

        ImmutableMap<Variable, Exists> existsMap = extendedTerms.stream()
                .map(ExtendedTerm::getExistsMap)
                .flatMap(s -> s.entrySet().stream())
                .collect(ImmutableCollectors.toMap());

        if (expr instanceof Coalesce) {
            SPARQLFunctionSymbol functionSymbol = functionSymbolFactory.getRequiredSPARQLFunctionSymbol(
                    SPARQL.COALESCE, terms.size());
            return new ExtendedTerm(termFactory.getImmutableFunctionalTerm(functionSymbol, terms), existsMap);
        }
        if (expr instanceof ListMemberOperator) {
            if (terms.size() < 2)
                throw new MinorOntopInternalBugException("Was not expecting a ListMemberOperator from RDF4J with less than 2 terms");

            ImmutableTerm firstArgument = terms.get(0);
            ImmutableTerm orTerm = terms.stream()
                    .skip(1)
                    .map(t -> getFunctionalTerm(SPARQL.EQ, firstArgument, t))
                    .reduce((e1, e2) -> getFunctionalTerm(SPARQL.LOGICAL_OR, e1, e2))
                    .orElseThrow(() -> new MinorOntopInternalBugException("Cannot happen because there are at least 2 terms"));
            return new ExtendedTerm(orTerm, existsMap);
        }
        throw new RuntimeException("Unreachable: all subclasses covered");
    }

    private ImmutableFunctionalTerm getFunctionalTerm(String functionName) {
        return termFactory.getImmutableFunctionalTerm(functionSymbolFactory.getRequiredSPARQLFunctionSymbol(functionName, 0));
    }

    private ImmutableFunctionalTerm getFunctionalTerm(String functionName, ImmutableTerm t) {
        return termFactory.getImmutableFunctionalTerm(functionSymbolFactory.getRequiredSPARQLFunctionSymbol(functionName, 1), t);
    }

    private ImmutableFunctionalTerm getFunctionalTerm(String functionName, ImmutableTerm t1, ImmutableTerm t2) {
        return termFactory.getImmutableFunctionalTerm(functionSymbolFactory.getRequiredSPARQLFunctionSymbol(functionName, 2), t1, t2);
    }

    private ImmutableFunctionalTerm getFunctionalTerm(String functionName, ImmutableTerm t1, ImmutableTerm t2, ImmutableTerm t3) {
        return termFactory.getImmutableFunctionalTerm(functionSymbolFactory.getRequiredSPARQLFunctionSymbol(functionName, 3), t1, t2, t3);
    }

    private ImmutableFunctionalTerm getAggregateFunctionalTerm(String officialName, boolean isDistinct, ImmutableTerm t) {
        return termFactory.getImmutableFunctionalTerm(
                isDistinct
                        ? functionSymbolFactory.getRequiredSPARQLDistinctAggregateFunctionSymbol(officialName, 1)
                        : functionSymbolFactory.getRequiredSPARQLFunctionSymbol(officialName, 1),
                t);
    }


    private ImmutableTerm convertToXsdBooleanTerm(ImmutableTerm term) {

        return term.inferType()
                .flatMap(TermTypeInference::getTermType)
                .filter(t -> t instanceof RDFDatatype)
                .map(t -> (RDFDatatype) t)
                .filter(t -> t.isA(XSD.BOOLEAN))
                .isPresent()
                    ? term
                    : termFactory.getSPARQLEffectiveBooleanValue(term);
    }

    private static final ImmutableMap<MathExpr.MathOp, String> NumericalOperations =
            ImmutableMap.<MathExpr.MathOp, String>builder()
                    .put(MathExpr.MathOp.PLUS, SPARQL.ADD)
                    .put(MathExpr.MathOp.MINUS, SPARQL.SUBTRACT)
                    .put(MathExpr.MathOp.MULTIPLY, SPARQL.MULTIPLY)
                    .put(MathExpr.MathOp.DIVIDE, SPARQL.DIVIDE)
                    .build();


    /**
     * Changes some function names when RDF4J abuses the SPARQL standard (i.e. is too tightly-coupled)
     *
     * The typical example is the YEAR() function which is replaced by RDF4J by fn:year-from-dateTime because
     * the SPARQL 1.1 specification has only consider the case of xsd:dateTime, not xsd:date.
     * Obviously, all the major implementations also support the case of xsd:date and use the fun:year-from-date when
     * appropriated.
     *
     * This method reverses fn:year-from-dateTime into YEAR, as it now maps to a function symbol that accepts
     * both xsd:date and xsd:dateTime.
     *
     */
    private String extractFunctionName(String uri) {

        if (uri.equals(XPathFunction.YEAR_FROM_DATETIME.getIRIString()))
            return SPARQL.YEAR;
        else if (uri.equals(XPathFunction.MONTH_FROM_DATETIME.getIRIString()))
            return SPARQL.MONTH;
        else if (uri.equals(XPathFunction.DAY_FROM_DATETIME.getIRIString()))
            return SPARQL.DAY;
        else
            return uri;
    }


    /**
     * Translates a RDF4J "Var" (which can be a variable or a constant) into a Ontop term.
     */
    public VariableOrGroundTerm translateRDF4JVar(Var v, boolean leafNode) {
        // If this "Var" is a constant
        if ((v.hasValue()))
            return valueTranslator.getTermForLiteralOrIri(v.getValue());

        if (v.isAnonymous() && !treatBNodeAsVariable)
            return termFactory.getConstantBNode(v.getName());

        // Otherwise, this "Var" is a variable
        Variable var = termFactory.getVariable(v.getName());
        // If the subtree is empty, create a variable
        if (leafNode)
            return var;
        // Otherwise, check whether the variable is projected
        return knownVariables.contains(var)
                ? var
                : Optional.ofNullable(externalBindings.get(var))
                        .orElseGet(termFactory::getNullConstant);
    }

    public static class ExtendedTerm {
        private final ImmutableTerm term;
        private final ImmutableMap<Variable, Exists> existsMap ;

        public ExtendedTerm(ImmutableTerm term) {
            this.existsMap = ImmutableMap.of();
            this.term = term;
        }

        public ExtendedTerm(ImmutableTerm term, ImmutableMap<Variable, Exists> existsMap) {
            this.existsMap = existsMap;
            this.term = term;
        }

        public ImmutableMap<Variable, Exists> getExistsMap() {
            return ImmutableMap.copyOf(existsMap);
        }

        public ImmutableTerm getTerm() {
            return term;
        }

    }
}
