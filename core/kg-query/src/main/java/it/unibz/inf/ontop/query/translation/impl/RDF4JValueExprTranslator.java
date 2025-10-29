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

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

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
    public ExistsMapAnnotatedObject<ImmutableTerm> getTerm(ValueExpr expr) {

        if (expr instanceof Var) {
            VariableOrGroundTerm var = translateRDF4JVar((Var) expr, false);
            return ExistsMapAnnotatedObject.of(var);
        }
        if (expr instanceof ValueConstant) {
            Value v = ((ValueConstant) expr).getValue();
            return ExistsMapAnnotatedObject.of(valueTranslator.getTermForLiteralOrIri(v));
        }
        if (expr instanceof Bound) {
            // BOUND (Sec 17.4.1.1)
            // xsd:boolean  BOUND (variable var)
            Var v = ((Bound) expr).getArg();
            Variable var = termFactory.getVariable(v.getName());
            ImmutableTerm boundTerm = knownVariables.contains(var)
                    ? getFunctionalTerm(SPARQL.BOUND, var)
                    : termFactory.getRDFLiteralConstant("false", XSD.BOOLEAN);
            return ExistsMapAnnotatedObject.of(boundTerm);
        }
        if (expr instanceof UnaryValueOperator) {
            return getTerm((UnaryValueOperator) expr);
        }
        if (expr instanceof BinaryValueOperator) {
            return getTerm((BinaryValueOperator) expr);
        }
        if (expr instanceof FunctionCall) {
            FunctionCall f = (FunctionCall) expr;

            ImmutableList<ExistsMapAnnotatedObject<ImmutableTerm>> extendedTerms = f.getArgs().stream()
                    .map(this::getTerm)
                    .collect(ImmutableCollectors.toList());

            String functionName = extractFunctionName(f.getURI());

            Optional<SPARQLFunctionSymbol> optionalFunctionSymbol = functionSymbolFactory.getSPARQLFunctionSymbol(
                    functionName, extendedTerms.size());

            if (optionalFunctionSymbol.isPresent()) {
                return ExistsMapAnnotatedObject.of(extendedTerms, terms -> termFactory.getImmutableFunctionalTerm(optionalFunctionSymbol.get(), terms));
            }
        }
        if (expr instanceof NAryValueOperator) {
            return getTerm((NAryValueOperator) expr);
        }
        if (expr instanceof BNodeGenerator) {
            Optional<ExistsMapAnnotatedObject<ImmutableTerm>> term = Optional.ofNullable(((BNodeGenerator) expr).getNodeIdExpr())
                    .map(this::getTerm);

            return term
                    .<ExistsMapAnnotatedObject<ImmutableTerm>>map(et -> ExistsMapAnnotatedObject.of(et, t -> getFunctionalTerm(SPARQL.BNODE, t)))
                    .orElseGet(() -> ExistsMapAnnotatedObject.of(getFunctionalTerm(SPARQL.BNODE)));

        }
        if (expr instanceof If) {
            If ifExpr = (If) expr;

            ExistsMapAnnotatedObject<ImmutableTerm> condition = getTerm(ifExpr.getCondition());
            ExistsMapAnnotatedObject<ImmutableTerm> thenTerm = getTerm(ifExpr.getResult());
            ExistsMapAnnotatedObject<ImmutableTerm> elseTerm = getTerm(ifExpr.getAlternative());

            return ExistsMapAnnotatedObject.of(condition, thenTerm, elseTerm, (c, t, e) -> getFunctionalTerm(SPARQL.IF, convertToXsdBooleanTerm(c), t, e));
        }
        if (expr instanceof Exists) {
            Variable freshVariable = variableGenerator.generateNewVariable("prov");
            ImmutableFunctionalTerm boundTerm = getFunctionalTerm(SPARQL.BOUND,
                    termFactory.getRDFLiteralFunctionalTerm(freshVariable, XSD.STRING));

            return new ExistsMapAnnotatedObject<>(boundTerm,
                    ImmutableMap.of(freshVariable, (Exists)expr));
        }
        // other subclasses
        // SubQueryValueOperator
        // ValueExprTripleRef
        throw new RuntimeException(new OntopUnsupportedKGQueryException("The expression " + expr + " is not supported yet!"));
    }

    private ExistsMapAnnotatedObject<ImmutableTerm> getTerm(UnaryValueOperator expr) {

        if (expr.getArg() == null) {
            if (expr instanceof Count)  // O-ary count
                return ExistsMapAnnotatedObject.of(getFunctionalTerm(SPARQL.COUNT));

            throw new RuntimeException(new OntopUnsupportedKGQueryException("The expression " + expr + " is not supported yet!"));
        }

        ExistsMapAnnotatedObject<ImmutableTerm> extendedTerm = getTerm(expr.getArg());

        if (expr instanceof AbstractAggregateOperator) {
            AbstractAggregateOperator aggExpr = (AbstractAggregateOperator) expr;
            if (aggExpr instanceof Count) { //Unary count
                return ExistsMapAnnotatedObject.of(extendedTerm, t -> getAggregateFunctionalTerm(SPARQL.COUNT, aggExpr.isDistinct(), t));
            }
            if (aggExpr instanceof Avg) {
                return ExistsMapAnnotatedObject.of(extendedTerm, t -> getAggregateFunctionalTerm(SPARQL.AVG, aggExpr.isDistinct(), t));
            }
            if (aggExpr instanceof Sum) {
                return ExistsMapAnnotatedObject.of(extendedTerm, t -> getAggregateFunctionalTerm(SPARQL.SUM, aggExpr.isDistinct(), t));
            }
            if (aggExpr instanceof Min) {
                return ExistsMapAnnotatedObject.of(extendedTerm, t -> getFunctionalTerm(SPARQL.MIN, t));
            }
            if (aggExpr instanceof Max) {
                return ExistsMapAnnotatedObject.of(extendedTerm, t -> getFunctionalTerm(SPARQL.MAX, t));
            }
            if (aggExpr instanceof Sample) {
                return ExistsMapAnnotatedObject.of(extendedTerm, t -> getFunctionalTerm(SPARQL.SAMPLE, t));
            }
            if (aggExpr instanceof GroupConcat) {
                String separator = Optional.ofNullable(((GroupConcat) aggExpr).getSeparator())
                        .map(e -> ((ValueConstant) e).getValue().stringValue())
                        .orElse(" "); // Default separator

                return ExistsMapAnnotatedObject.of(extendedTerm, t -> termFactory.getImmutableFunctionalTerm(
                        functionSymbolFactory.getSPARQLGroupConcatFunctionSymbol(separator, aggExpr.isDistinct()), t));
            }
            if (aggExpr instanceof AggregateFunctionCall) {
                AggregateFunctionCall call = (AggregateFunctionCall) aggExpr;
                if (call.getIRI().startsWith(AGG.PREFIX)) {
                    return ExistsMapAnnotatedObject.of(extendedTerm, t -> getAggregateFunctionalTerm(call.getIRI(), aggExpr.isDistinct(), t));
                }
            }
            throw new RuntimeException("Unreachable: all subclasses covered");
        }
        if (expr instanceof Not) {
            return ExistsMapAnnotatedObject.of(extendedTerm, t -> getFunctionalTerm(XPathFunction.NOT.getIRIString(), convertToXsdBooleanTerm(t)));
        }
        if (expr instanceof IsNumeric) {
            // isNumeric (Sec 17.4.2.4) for checking whether the argument is a numeric value
            return ExistsMapAnnotatedObject.of(extendedTerm, t -> getFunctionalTerm(SPARQL.IS_NUMERIC, t));
        }
        if (expr instanceof IsLiteral) {
            return ExistsMapAnnotatedObject.of(extendedTerm, t -> getFunctionalTerm(SPARQL.IS_LITERAL, t));
        }
        if (expr instanceof IsURI) {
            return ExistsMapAnnotatedObject.of(extendedTerm, t -> getFunctionalTerm(SPARQL.IS_IRI, t));
        }
        if (expr instanceof Str) {
            return ExistsMapAnnotatedObject.of(extendedTerm, t -> getFunctionalTerm(SPARQL.STR, t));
        }
        if (expr instanceof Datatype) {
            return ExistsMapAnnotatedObject.of(extendedTerm, t -> getFunctionalTerm(SPARQL.DATATYPE, t));
        }
        if (expr instanceof IsBNode) {
            return ExistsMapAnnotatedObject.of(extendedTerm, t -> getFunctionalTerm(SPARQL.IS_BLANK, t));
        }
        if (expr instanceof Lang) {
            if (expr.getArg() instanceof Var) {
                return ExistsMapAnnotatedObject.of(extendedTerm, t -> getFunctionalTerm(SPARQL.LANG, t));
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

            return ExistsMapAnnotatedObject.of(extendedTerm, t -> termFactory.getImmutableFunctionalTerm(functionSymbol, t));
        }
        // subclasses missing:
        //  - IsResource
        //   - LocalName
        //   - Namespace
        //   - Label
        //   - Like
        throw new RuntimeException(new OntopUnsupportedKGQueryException("The expression " + expr + " is not supported yet!"));
    }

    private ExistsMapAnnotatedObject<ImmutableTerm> getTerm(BinaryValueOperator expr) {

        ExistsMapAnnotatedObject<ImmutableTerm> extendedTerm1 = getTerm(expr.getLeftArg());
        ExistsMapAnnotatedObject<ImmutableTerm> extendedTerm2 = getTerm(expr.getRightArg());

        if (expr instanceof And) {
            return ExistsMapAnnotatedObject.of(extendedTerm1, extendedTerm2, (t1, t2) -> getFunctionalTerm(SPARQL.LOGICAL_AND, convertToXsdBooleanTerm(t1), convertToXsdBooleanTerm(t2)));
        }
        if (expr instanceof Or) {
            return ExistsMapAnnotatedObject.of(extendedTerm1, extendedTerm2, (t1, t2) -> getFunctionalTerm(SPARQL.LOGICAL_OR, convertToXsdBooleanTerm(t1), convertToXsdBooleanTerm(t2)));
        }
        if (expr instanceof SameTerm) {
            // sameTerm (Sec 17.4.1.8)
            // Corresponds to the STRICT equality (same lexical value, same type)
            return ExistsMapAnnotatedObject.of(extendedTerm1, extendedTerm2, (t1, t2) -> getFunctionalTerm(SPARQL.SAME_TERM, t1, t2));
        }
        if (expr instanceof Regex) {
            // REGEX (Sec 17.4.3.14)
            // xsd:boolean  REGEX (string literal text, simple literal pattern)
            // xsd:boolean  REGEX (string literal text, simple literal pattern, simple literal flags)
            Regex reg = (Regex) expr;
            if (reg.getFlagsArg() != null) {
                ExistsMapAnnotatedObject<ImmutableTerm> flagsTerm = getTerm(reg.getFlagsArg());
                return ExistsMapAnnotatedObject.of(extendedTerm1, extendedTerm2, flagsTerm, (t1, t2, f) -> getFunctionalTerm(SPARQL.REGEX, t1, t2, f));
            }
            return ExistsMapAnnotatedObject.of(extendedTerm1, extendedTerm2, (t1, t2) -> getFunctionalTerm(SPARQL.REGEX, t1, t2));
        }
        if (expr instanceof Compare) {
            return ExistsMapAnnotatedObject.of(extendedTerm1, extendedTerm2, Optional.ofNullable(CompareOperations.get(((Compare) expr).getOperator()))
                    .orElseThrow(() -> new RuntimeException(new OntopUnsupportedKGQueryException("Unsupported operator: " + expr))));
        }
        if (expr instanceof MathExpr) {
            return ExistsMapAnnotatedObject.of(extendedTerm1, extendedTerm2, Optional.ofNullable(ArithmeticOperations.get(((MathExpr) expr).getOperator()))
                    .orElseThrow(() -> new RuntimeException(new OntopUnsupportedKGQueryException("Unsupported operator: " + expr))));
        }
        /*
         * Restriction: the first argument must be LANG(...) and the second  a constant
         * (for guaranteeing that the langMatches logic is not delegated to the native query)
         */
        if (expr instanceof LangMatches) {
            if (!(extendedTerm1.get() instanceof ImmutableFunctionalTerm
                    && ((ImmutableFunctionalTerm) extendedTerm1.get()).getFunctionSymbol() instanceof LangSPARQLFunctionSymbol)
                    || !(extendedTerm2.get() instanceof RDFConstant)) {
                throw new RuntimeException(new OntopUnsupportedKGQueryException("The function langMatches is " +
                        "only supported with lang(..) function for the first argument and a constant for the second"));
            }
            return ExistsMapAnnotatedObject.of(extendedTerm1, extendedTerm2, (t1, t2) -> getFunctionalTerm(SPARQL.LANG_MATCHES, t1, t2));
        }
        throw new RuntimeException("Unreachable: all subclasses covered");
    }

    private ExistsMapAnnotatedObject<ImmutableTerm> getTerm(NAryValueOperator expr) {

        ImmutableList<ExistsMapAnnotatedObject<ImmutableTerm>> extendedTerms = expr.getArguments().stream()
                .map(this::getTerm)
                .collect(ImmutableCollectors.toList());

        if (expr instanceof Coalesce) {
            SPARQLFunctionSymbol functionSymbol = functionSymbolFactory.getRequiredSPARQLFunctionSymbol(
                    SPARQL.COALESCE, extendedTerms.size());
            return ExistsMapAnnotatedObject.of(extendedTerms, terms -> termFactory.getImmutableFunctionalTerm(functionSymbol, terms));
        }
        if (expr instanceof ListMemberOperator) {
            if (extendedTerms.size() < 2)
                throw new MinorOntopInternalBugException("Was not expecting a ListMemberOperator from RDF4J with less than 2 terms");

            ImmutableTerm firstArgument = extendedTerms.get(0).get();
            ImmutableTerm orTerm = extendedTerms.stream()
                    .map(ExistsMapAnnotatedObject::get)
                    .skip(1)
                    .map(t -> getFunctionalTerm(SPARQL.EQ, firstArgument, t))
                    .reduce((e1, e2) -> getFunctionalTerm(SPARQL.LOGICAL_OR, e1, e2))
                    .orElseThrow(() -> new MinorOntopInternalBugException("Cannot happen because there are at least 2 terms"));
            return ExistsMapAnnotatedObject.of(extendedTerms, terms -> orTerm);
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

    private final ImmutableMap<MathExpr.MathOp, BinaryOperator<ImmutableTerm>> ArithmeticOperations =
            ImmutableMap.of(
                    MathExpr.MathOp.PLUS, (t1, t2) -> getFunctionalTerm(SPARQL.ADD, t1, t2),
                    MathExpr.MathOp.MINUS, (t1, t2) -> getFunctionalTerm(SPARQL.SUBTRACT, t1, t2),
                    MathExpr.MathOp.MULTIPLY, (t1, t2) -> getFunctionalTerm(SPARQL.MULTIPLY, t1, t2),
                    MathExpr.MathOp.DIVIDE, (t1, t2) -> getFunctionalTerm(SPARQL.DIVIDE, t1, t2));

    private final ImmutableMap<Compare.CompareOp, BinaryOperator<ImmutableTerm>> CompareOperations =
            ImmutableMap.of(
                    Compare.CompareOp.EQ, (t1, t2) -> getFunctionalTerm(SPARQL.EQ, t1, t2),
                    Compare.CompareOp.LT, (t1, t2) -> getFunctionalTerm(SPARQL.LESS_THAN, t1, t2),
                    Compare.CompareOp.GT, (t1, t2) -> getFunctionalTerm(SPARQL.GREATER_THAN, t1, t2),
                    Compare.CompareOp.NE, (t1, t2) -> getFunctionalTerm(XPathFunction.NOT.getIRIString(), getFunctionalTerm(SPARQL.EQ, t1, t2)),
                    Compare.CompareOp.LE, (t1, t2) -> getFunctionalTerm(XPathFunction.NOT.getIRIString(), getFunctionalTerm(SPARQL.GREATER_THAN, t1, t2)),
                    Compare.CompareOp.GE, (t1, t2) -> getFunctionalTerm(XPathFunction.NOT.getIRIString(), getFunctionalTerm(SPARQL.LESS_THAN, t1, t2)));

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

    public static class ExistsMapAnnotatedObject<T> {
        private final T term;
        private final ImmutableMap<Variable, Exists> existsMap ;

        public ExistsMapAnnotatedObject(T term, ImmutableMap<Variable, Exists> existsMap) {
            this.existsMap = existsMap;
            this.term = term;
        }

        public ImmutableMap<Variable, Exists> getExistsMap() {
            return ImmutableMap.copyOf(existsMap);
        }

        public T get() {
            return term;
        }

        public static <T> ExistsMapAnnotatedObject<T> of(T term) {
            return new ExistsMapAnnotatedObject<>(term, ImmutableMap.of());
        }

        public static <T, R> ExistsMapAnnotatedObject<R> of(ExistsMapAnnotatedObject<T> term, Function<T, R> termCombiner) {
            return new ExistsMapAnnotatedObject<>(termCombiner.apply(term.get()), term.getExistsMap());
        }

        public static <T> ExistsMapAnnotatedObject<T> of(ExistsMapAnnotatedObject<T> term1, ExistsMapAnnotatedObject<T> term2, BinaryOperator<T> termCombiner) {
            ImmutableMap.Builder<Variable, Exists> builder = ImmutableMap.builder();
            builder.putAll(term1.getExistsMap());
            builder.putAll(term2.getExistsMap());
            return new ExistsMapAnnotatedObject<>(termCombiner.apply(term1.get(), term2.get()), builder.build());
        }

        public interface TernaryTermCombiner<T> {
            T  apply(T term1, T term2, T term3);
        }

        public static <T> ExistsMapAnnotatedObject<T> of(ExistsMapAnnotatedObject<T> term1, ExistsMapAnnotatedObject<T> term2, ExistsMapAnnotatedObject<T> term3, TernaryTermCombiner<T> termCombiner) {
            ImmutableMap.Builder<Variable, Exists> builder = ImmutableMap.builder();
            builder.putAll(term1.getExistsMap());
            builder.putAll(term2.getExistsMap());
            builder.putAll(term3.getExistsMap());
            return new ExistsMapAnnotatedObject<>(termCombiner.apply(term1.get(), term2.get(), term3.get()), builder.build());
        }

        public static <T> ExistsMapAnnotatedObject<T> of(ImmutableList<ExistsMapAnnotatedObject<T>> terms, Function<ImmutableList<T>, T> termCombiner) {
            return new ExistsMapAnnotatedObject<>(
                    termCombiner.apply(terms.stream().map(ExistsMapAnnotatedObject::get).collect(ImmutableCollectors.toList())),
                    terms.stream().map(ExistsMapAnnotatedObject::getExistsMap).map(ImmutableMap::entrySet).flatMap(Collection::stream).collect(ImmutableCollectors.toMap()));
        }
    }
}
