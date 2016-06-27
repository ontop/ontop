package it.unibz.krdb.obda.owlrefplatform.core.translator;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.*;
import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.OBDAQueryModifiers.OrderCondition;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.UriTemplateMatcher;
import it.unibz.krdb.obda.parser.EncodeForURI;
import jdk.nashorn.internal.ir.annotations.Immutable;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/***
 * Translate a SPARQL algebra expression into a Datalog program that has the
 * same semantics. We use the built-in predicates Join and LeftJoin.
 * 
 * @author Roman Kontchakov
 */
public class SparqlAlgebraToDatalogTranslator {

	
	private final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
	
	private final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();

	private final UriTemplateMatcher uriTemplateMatcher;
	private final SemanticIndexURIMap uriRef;  
	
	private static final Logger log = LoggerFactory.getLogger(SparqlAlgebraToDatalogTranslator.class);
	
	/**
	 * 
	 * @param uriTemplateMatcher
	 * @param uriRef is used only in the Semantic Index mode
	 */
	
	public SparqlAlgebraToDatalogTranslator(UriTemplateMatcher uriTemplateMatcher, SemanticIndexURIMap uriRef) {
		this.uriTemplateMatcher = uriTemplateMatcher;
		this.uriRef = uriRef;
	}
	
	/**
	 * Translate a given SPARQL query object to datalog program.
	 * 
	 *
	 *            The Query object.
	 * @return Datalog program that represents the construction of the SPARQL
	 *         query.
	 */

	public SparqlQuery translate(ParsedQuery pq) {
		
		TupleExpr te = pq.getTupleExpr();
		log.debug("SPARQL algebra: \n{}", te);
        //System.out.println("SPARQL algebra: \n" + te);

        TranslationProgram program = new TranslationProgram();
        TranslationResult body = tran(te, program);

        List<Term> answerVariables;
		if (pq instanceof ParsedTupleQuery || pq instanceof ParsedGraphQuery) {
            // order elements of the set in some way by converting it into the list
            answerVariables = new ArrayList<Term>(body.variables);
        }
		else {
            // ASK queries have no answer variables
            answerVariables = Collections.emptyList();
        }

        Predicate pred = ofac.getPredicate(OBDAVocabulary.QUEST_QUERY, answerVariables.size());
        Function head = ofac.getFunction(pred, answerVariables);
        program.appendRule(head, body.atoms);

        List<String> signature = Lists.transform(answerVariables, t -> ((Variable)t).getName());

        //System.out.println("PROGRAM\n" + program.program);
		return new SparqlQuery(program.program, signature);
	}

    private static class TranslationResult {
        private final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

        final ImmutableList<Function> atoms;
        final ImmutableSet<Variable> variables;
        final boolean isBGP;

        TranslationResult(ImmutableList<Function> atoms, ImmutableSet<Variable> variables, boolean isBGP) {
            this.atoms = atoms;
            this.variables = variables;
            this.isBGP = isBGP;
        }

        private <T> TranslationResult extendWithBindings(Stream<T> bindings,
                                         java.util.function.Function<? super T, Variable> varNameMapper,
                                         java.util.function.BiFunction<? super T, ImmutableSet<Variable>, Term> exprMapper) {

            Set<Variable> vars = new HashSet<>(variables);
            Stream<Function> eqAtoms = bindings.map(b -> {
                Term expr = exprMapper.apply(b, ImmutableSet.copyOf(vars));

                Variable v = varNameMapper.apply(b);
                if (!vars.add(v))
                    throw new IllegalArgumentException("Duplicate binding for variable " + v);

                return ofac.getFunctionEQ(v, expr);
            });

            return new TranslationResult(getAtomsExtended(eqAtoms), ImmutableSet.copyOf(vars), false);
        }

        private ImmutableList<Function> getAtomsExtendedWithNulls(ImmutableSet<Variable> allVariables) {
            Sets.SetView<Variable>  nullVariables = Sets.difference(allVariables, variables);
            if (nullVariables.isEmpty())
                return atoms;

            return getAtomsExtended(nullVariables.stream().map(v -> ofac.getFunctionEQ(v, OBDAVocabulary.NULL)));
        }

        private ImmutableList<Function> getAtomsExtended(Stream<Function> extension) {
            return ImmutableList.copyOf(Iterables.concat(atoms, extension.collect(Collectors.toList())));

//            ImmutableList.Builder builder = ImmutableList.<Function>builder().addAll(atoms)
//                    .addAll(nullVariables.stream().map(v -> ofac.getFunctionEQ(v, OBDAVocabulary.NULL)).iterator());

//            return builder.build();
        }

    }

    private static class TranslationProgram {
        private final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

        private final DatalogProgram program;
        private int predicateIdx = 0;

        TranslationProgram() {
            this.program = ofac.getDatalogProgram();
        }

        OBDAQueryModifiers getQueryModifiers() {
            return program.getQueryModifiers();
        }

        Function getFreshHead(List<Term> terms) {
            Predicate pred = ofac.getPredicate(OBDAVocabulary.QUEST_QUERY + predicateIdx, terms.size());
            predicateIdx++;
            return ofac.getFunction(pred, terms);
        }

        void appendRule(Function head, List<Function> body) {
            CQIE rule = ofac.getCQIE(head, body);
            program.appendRule(rule);
        }

        Function wrapNonTriplePattern(TranslationResult sub) {
            if (sub.atoms.size() > 1 || sub.atoms.get(0).isAlgebraFunction()) {
                Function head = getFreshHead(new ArrayList<>(sub.variables));
                appendRule(head, sub.atoms);
                return head;
            }
            return sub.atoms.get(0);
        }

        TranslationResult getFreshNode(ImmutableSet<Variable> vars) {
            Function head = getFreshHead(new ArrayList<>(vars));
            return new TranslationResult(ImmutableList.of(head), vars, false);
        }
    }

    private TranslationResult tran(TupleExpr node, TranslationProgram program) {

        //System.out.println("node: \n" + node);

        if (node instanceof Slice) {   // SLICE algebra operation
            Slice slice = (Slice) node;
            OBDAQueryModifiers modifiers = program.getQueryModifiers();
            modifiers.setOffset(slice.getOffset());
            modifiers.setLimit(slice.getLimit());
            return tran(slice.getArg(), program);
        }
        else if (node instanceof Distinct) { // DISTINCT algebra operation
            Distinct distinct = (Distinct) node;
            program.getQueryModifiers().setDistinct();
            return tran(distinct.getArg(), program);
        }
        else if (node instanceof Reduced) {  // REDUCED algebra operation
            Reduced reduced = (Reduced) node;
            return tran(reduced.getArg(), program);
        }
        else if (node instanceof Order) {   // ORDER algebra operation
            Order order = (Order) node;
            OBDAQueryModifiers modifiers = program.getQueryModifiers();
            for (OrderElem c : order.getElements()) {
                ValueExpr expression = c.getExpr();
                if (!(expression instanceof Var))
                    throw new IllegalArgumentException("Error translating ORDER BY. "
                            + "The current implementation can only sort by variables. "
                            + "This query has a more complex expression '" + expression + "'");

                Var v = (Var) expression;
                Variable var = ofac.getVariable(v.getName());
                int direction =  c.isAscending() ? OrderCondition.ORDER_ASCENDING
                        : OrderCondition.ORDER_DESCENDING;
                modifiers.addOrderCondition(var, direction);
            }
            return tran(order.getArg(), program);
        }
        else if (node instanceof StatementPattern) { // triple pattern
            return translate((StatementPattern) node);
        }
        else if (node instanceof SingletonSet) {
            // the empty BGP has no variables and gives a single solution mapping on every non-empty graph
            return new TranslationResult(ImmutableList.of(), ImmutableSet.of(), true);
        }
        else if (node instanceof Join) {     // JOIN algebra operation
            Join join = (Join) node;
            TranslationResult a1 = tran(join.getLeftArg(), program);
            TranslationResult a2 = tran(join.getRightArg(), program);
            ImmutableSet<Variable> vars = Sets.union(a1.variables, a2.variables).immutableCopy();

            if (a1.isBGP && a2.isBGP) {             // collect triple patterns into BGPs
                ImmutableList<Function> atoms =
                        ImmutableList.<Function>builder().addAll(a1.atoms).addAll(a2.atoms).build();
                return new TranslationResult(atoms, vars, true);
            }
            else {
                Function body = ofac.getSPARQLJoin(program.wrapNonTriplePattern(a1),
                        program.wrapNonTriplePattern(a2));

                return new TranslationResult(ImmutableList.of(body), vars, false);
            }
        }
        else if (node instanceof LeftJoin) {  // OPTIONAL algebra operation
            LeftJoin lj = (LeftJoin) node;
            TranslationResult a1 = tran(lj.getLeftArg(), program);
            TranslationResult a2 = tran(lj.getRightArg(), program);
            ImmutableSet<Variable> vars = Sets.union(a1.variables, a2.variables).immutableCopy();

            Function body = ofac.getSPARQLLeftJoin(program.wrapNonTriplePattern(a1),
                    program.wrapNonTriplePattern(a2));

            ValueExpr expr = lj.getCondition();
            if (expr != null) {
                Function f = getFilterExpression(expr, vars);
                body.getTerms().add(f);
            }

            return new TranslationResult(ImmutableList.of(body), vars, false);
        }
        else if (node instanceof Union) {   // UNION algebra operation
            Union union = (Union) node;
            TranslationResult a1 = tran(union.getLeftArg(), program);
            TranslationResult a2 = tran(union.getRightArg(), program);
            ImmutableSet<Variable> vars = Sets.union(a1.variables, a2.variables).immutableCopy();

            TranslationResult res = program.getFreshNode(vars);
            program.appendRule(res.atoms.get(0), a1.getAtomsExtendedWithNulls(vars));
            program.appendRule(res.atoms.get(0), a2.getAtomsExtendedWithNulls(vars));
            return res;
        }
        else if (node instanceof Filter) {   // FILTER algebra operation
            Filter filter = (Filter) node;
            TranslationResult a = tran(filter.getArg(), program);

            Function f = getFilterExpression(filter.getCondition(), a.variables);
            ImmutableList<Function> atoms = ImmutableList.<Function>builder().addAll(a.atoms).add(f).build();
            // TODO: split ANDs in the FILTER?

            return new TranslationResult(atoms, a.variables, false);
        }
        else if (node instanceof Projection) {  // PROJECT algebra operation
            Projection projection = (Projection) node;
            TranslationResult sub = tran(projection.getArg(), program);

            List<ProjectionElem> pes = projection.getProjectionElemList().getElements();
            // the two lists are required to synchronise the order of variables
            List<Term> sVars = new ArrayList<>(pes.size());
            List<Term> tVars = new ArrayList<>(pes.size());
            boolean noRenaming = true;
            for (ProjectionElem pe : pes)  {
                Variable sVar = ofac.getVariable(pe.getSourceName());
                if (!sub.variables.contains(sVar))
                    throw new IllegalArgumentException("Projection source of " + pe
                            + " not found in " + projection.getArg());
                sVars.add(sVar);

                Variable tVar = ofac.getVariable(pe.getTargetName());
                tVars.add(tVar);

                if (!sVar.equals(tVar))
                    noRenaming = false;
            }
            if (noRenaming && sVars.containsAll(sub.variables)) // neither projection nor renaming
                return sub;

            ImmutableSet<Variable> vars = ImmutableSet.copyOf(
                    tVars.stream().map(t -> (Variable)t).collect(Collectors.toSet()));

            if (noRenaming)
                return new TranslationResult(sub.atoms, vars, false);

            Function head = program.getFreshHead(sVars);
            program.appendRule(head, sub.atoms);

            Function atom = ofac.getFunction(head.getFunctionSymbol(), tVars);
            return new TranslationResult(ImmutableList.of(atom), vars, false);
        }
        else if (node instanceof Extension) {     // EXTEND algebra operation
            Extension extension = (Extension) node;
            TranslationResult sub = tran(extension.getArg(), program);
            return sub.extendWithBindings(
                    StreamSupport.stream(extension.getElements().spliterator(), false)
                            // ignore EXTEND(P, v, v), which is sometimes introduced by Sesame SPARQL parser
                            .filter(ee -> !(ee.getExpr() instanceof Var &&
                                    ee.getName().equals(((Var)ee.getExpr()).getName()))),
                            ee -> ofac.getVariable(ee.getName()),
                            (ee, vars) -> getExpression(ee.getExpr(), vars));
        }
        else if (node instanceof BindingSetAssignment) { // VALUES in SPARQL
            BindingSetAssignment values = (BindingSetAssignment) node;

            TranslationResult empty = new TranslationResult(ImmutableList.of(), ImmutableSet.of(), false);
            List<TranslationResult> bindings =
                StreamSupport.stream(values.getBindingSets().spliterator(), false)
                    .map(bs -> empty.extendWithBindings(
                            StreamSupport.stream(bs.spliterator(), false),
                            be -> ofac.getVariable(be.getName()),
                            (be, vars) -> getConstantExpression(be.getValue()))).collect(Collectors.toList());

            ImmutableSet.Builder allVarsBuilder = ImmutableSet.<Variable>builder();
            bindings.forEach(s -> allVarsBuilder.addAll(s.variables));
            ImmutableSet<Variable> allVars = allVarsBuilder.build();

            TranslationResult res = program.getFreshNode(allVars);
            bindings.forEach(p ->
                program.appendRule(res.atoms.get(0), p.getAtomsExtendedWithNulls(allVars)));
            return res;
        }
        else if (node instanceof Group) {
            throw new IllegalArgumentException("GROUP BY is not supported yet");
        }
        throw new IllegalArgumentException("Not supported: " + node);
    }

	private Function getFilterExpression(ValueExpr condition, ImmutableSet<Variable> variables) {
        Term term = getExpression(condition, variables);
        return (term instanceof Function) ? (Function) term : ofac.getFunctionIsTrue(term);
	}

	/***
	 * This translates a single triple. 
	 * 
	 * @param triple
	 * @return
	 */
	private TranslationResult translate(StatementPattern triple) {

        // A triple pattern is member of the set (RDF-T + V) x (I + V) x (RDF-T + V)
        // VarOrTerm ::=  Var | GraphTerm
        // GraphTerm ::=  iri | RDFLiteral | NumericLiteral | BooleanLiteral | BlankNode | NIL

        ImmutableSet.Builder<Variable> variables = ImmutableSet.builder();
        Function atom;

        Value s = triple.getSubjectVar().getValue();
        Value p = triple.getPredicateVar().getValue();
        Value o = triple.getObjectVar().getValue();

        Term sTerm = (s == null) ? createVariable(triple.getSubjectVar(), variables) : getLiteralOrIri(s);

		if (p == null) {
			//  term variable term .
            Term pTerm = createVariable(triple.getPredicateVar(), variables);
            Term oTerm = (o == null) ? createVariable(triple.getObjectVar(), variables) : getLiteralOrIri(o);
			atom = ofac.getTripleAtom(sTerm, pTerm, oTerm);
		}
		else if (p instanceof URI) {
			if (p.equals(RDF.TYPE)) {
				if (o == null) {
					// term rdf:type variable .
					Term pTerm = ofac.getUriTemplate(ofac.getConstantLiteral(OBDAVocabulary.RDF_TYPE));
                    Term oTerm = createVariable(triple.getObjectVar(), variables);
					atom = ofac.getTripleAtom(sTerm, pTerm, oTerm);
				}
				else if (o instanceof URI) {
					// term rdf:type uri .
					Predicate.COL_TYPE type = dtfac.getDatatype((URI)o);
					if (type != null) // datatype
						atom = ofac.getFunction(dtfac.getTypePredicate(type), sTerm);
					else // class
						atom = ofac.getFunction(ofac.getClassPredicate(o.stringValue()), sTerm);
				}
				else
					throw new IllegalArgumentException("Unsupported query syntax");
			}
			else {
				// term uri term . (where uri is either an object or a datatype property)
				Term oTerm = (o == null) ? createVariable(triple.getObjectVar(), variables) : getLiteralOrIri(o);
				Predicate predicate = ofac.getPredicate(p.stringValue(), new COL_TYPE[] { null, null });
				atom = ofac.getFunction(predicate, sTerm, oTerm);
			}
		}
		else
			// if predicate is a variable or literal
			throw new RuntimeException("Unsupported query syntax");

        return new TranslationResult(ImmutableList.of(atom), variables.build(), true);
	}

    private Term createVariable(Var v, ImmutableSet.Builder<Variable> variables) {
        Variable var = ofac.getVariable(v.getName());
        variables.add(var);
        return var;
    }

    private Term getLiteral(Literal literal) {
        URI typeURI = literal.getDatatype();
        String value = literal.getLabel();

        COL_TYPE type;
        if (typeURI == null)
            type = COL_TYPE.LITERAL;
        else {
            type = dtfac.getDatatype(typeURI);
            if (type == null)
                // ROMAN (27 June 2016): type1 in open-eq-05 test would not be supported in OWL
                // the actual value is LOST here
                return ofac.getUriTemplateForDatatype(typeURI.stringValue());
                // old strict version:
                // throw new RuntimeException("Unsupported datatype: " + typeURI);

            // check if the value is (lexically) correct for the specified datatype
            if (!XMLDatatypeUtil.isValidValue(value, typeURI))
                throw new RuntimeException("Invalid lexical form for datatype. Found: " + value);
        }

        Term constant = ofac.getConstantLiteral(value, type);

        // wrap the constant in its datatype function
        if (type == COL_TYPE.LITERAL) {
            // if the object has type LITERAL, check the language tag
            String lang = literal.getLanguage();
            if (lang != null && !lang.equals(""))
                return ofac.getTypedTerm(constant, lang);
        }
        return ofac.getTypedTerm(constant, type);
    }

	private Term getLiteralOrIri(Value v) {

		if (v instanceof Literal) {
			return getLiteral((Literal) v);
		}
		else if (v instanceof URI) {
            String uri = EncodeForURI.decodeURIEscapeCodes(v.stringValue());
			if (uriRef != null) {
				// if in the Semantic Index mode 
				int id = uriRef.getId(uri);
				return ofac.getUriTemplate(ofac.getConstantLiteral(String.valueOf(id), COL_TYPE.INTEGER));
			} 
			else {
				return uriTemplateMatcher.generateURIFunction(uri);
			}
		}

        throw new RuntimeException("The value " + v + " is not supported yet!");
	}

    private Term getConstantExpression(Value v) {

        if (v instanceof Literal) {
            return getLiteral((Literal) v);
        }
        else if (v instanceof URI) {
            String uri = EncodeForURI.decodeURIEscapeCodes(v.stringValue());
            Function constantFunction = uriTemplateMatcher.generateURIFunction(uri);
            if (constantFunction.getArity() == 1) {
                // ROMAN (27 June 2016: this means ZERO arguments, e.g., xsd:double or :z
                // despite the name, this is NOT necessarily a datatype
                constantFunction = ofac.getUriTemplateForDatatype(uri);
            }
            return constantFunction;
        }

        throw new RuntimeException("The value " + v + " is not supported yet!");
    }




	private Term getExpression(ValueExpr expr, ImmutableSet<Variable> variables) {

        // PrimaryExpression ::= BrackettedExpression | BuiltInCall | iriOrFunction |
        //                          RDFLiteral | NumericLiteral | BooleanLiteral | Var
        // iriOrFunction ::= iri ArgList?

		if (expr instanceof Var) {
            Var v = (Var) expr;
            Variable var = ofac.getVariable(v.getName());
            return variables.contains(var) ? var : OBDAVocabulary.NULL;
		} 
		else if (expr instanceof ValueConstant) {
			return getConstantExpression(((ValueConstant) expr).getValue());
		}
        else if (expr instanceof Bound) {
            // BOUND (Sec 17.4.1.1)
            // xsd:boolean  BOUND (variable var)
            Var v = ((Bound) expr).getArg();
            Variable var = ofac.getVariable(v.getName());
            return variables.contains(var) ? ofac.getFunctionIsNotNull(var) : ofac.getBooleanConstant(false);
        }
        else if (expr instanceof UnaryValueOperator) {
            Term term = getExpression(((UnaryValueOperator) expr).getArg(), variables);

            if (expr instanceof Not) {
                return ofac.getFunctionNOT(term);
            }
            else if (expr instanceof IsLiteral) {
                return ofac.getFunction(ExpressionOperation.IS_LITERAL, term);
            }
            else if (expr instanceof IsURI) {
                return ofac.getFunction(ExpressionOperation.IS_IRI, term);
            }
            else if (expr instanceof Str) {
                return ofac.getFunction(ExpressionOperation.SPARQL_STR, term);
            }
            else if (expr instanceof Datatype) {
                return ofac.getFunction(ExpressionOperation.SPARQL_DATATYPE, term);
            }
            else if (expr instanceof IsBNode) {
                return ofac.getFunction(ExpressionOperation.IS_BLANK, term);
            }
            else if (expr instanceof Lang) {
                ValueExpr arg = ((UnaryValueOperator) expr).getArg();
                if (arg instanceof Var)
                    return ofac.getFunction(ExpressionOperation.SPARQL_LANG, term);
                else
                    throw new RuntimeException("A variable or a value is expected in " + expr);
            }
            // other subclasses
            // IRIFunction: IRI (Sec 17.4.2.8) for constructing IRIs
            // IsNumeric:  isNumeric (Sec 17.4.2.4) for checking whether the argument is a numeric value
            // AggregateOperatorBase: Avg, Min, Max, etc.
            // Like:  ??
            // IsResource: ??
            // LocalName: ??
            // Namespace: ??
            // Label: ??
		}
		else if (expr instanceof BinaryValueOperator) {
            BinaryValueOperator bexpr = (BinaryValueOperator) expr;
            Term term1 = getExpression(bexpr.getLeftArg(), variables);
            Term term2 = getExpression(bexpr.getRightArg(), variables);

            if (expr instanceof And) {
                return ofac.getFunctionAND(term1, term2);
            }
            else if (expr instanceof Or) {
                return ofac.getFunctionOR(term1, term2);
            }
            else if (expr instanceof SameTerm) {
                return ofac.getFunctionEQ(term1, term2);
            }
            else if (expr instanceof Regex) {
                // REGEX (Sec 17.4.3.14)
                // xsd:boolean  REGEX (string literal text, simple literal pattern)
                // xsd:boolean  REGEX (string literal text, simple literal pattern, simple literal flags)
                Regex reg = (Regex) expr;
                Term term3 = (reg.getFlagsArg() != null) ?
                        getExpression(reg.getFlagsArg(), variables) : OBDAVocabulary.NULL;
                return ofac.getFunction(ExpressionOperation.REGEX, term1, term2, term3);
            }
            else if (expr instanceof Compare) {
                ExpressionOperation p = RelationalOperations.get(((Compare) expr).getOperator());
                return ofac.getFunction(p, term1, term2);
            }
            else if (expr instanceof MathExpr) {
                ExpressionOperation p = NumericalOperations.get(((MathExpr)expr).getOperator());
                return ofac.getFunction(p, term1, term2);
            }
            else if (expr instanceof LangMatches) {
                if (term2 instanceof Function) {
                    Function f = (Function) term2;
                    if (f.isDataTypeFunction()) {
                        Term functionTerm = f.getTerm(0);
                        if (functionTerm instanceof Constant) {
                            Constant c = (Constant) functionTerm;
                            term2 = ofac.getFunction(f.getFunctionSymbol(),
                                    ofac.getConstantLiteral(c.getValue().toLowerCase(),
                                            c.getType()));
                        }
                    }
                }

                return ofac.getLANGMATCHESFunction(term1, term2);
            }
            // other subclasses
            // SameTerm
        }
		else if (expr instanceof FunctionCall) {
            FunctionCall f = (FunctionCall) expr;

            int arity = f.getArgs().size();
            List<Term> terms = new ArrayList<>(arity);
            for (ValueExpr a : f.getArgs())
                terms.add(getExpression(a, variables));

            OperationPredicate p = XPathFunctions.get(f.getURI());
            if (p != null) {
                if (arity != p.getArity())
                    throw new UnsupportedOperationException(
                            "Wrong number of arguments (found " + terms.size() + ", only " +
                                    p.getArity() + "supported) for SPARQL " + f.getURI() + "function");

                return ofac.getFunction(p, terms);
            }

            // these are all special cases with **variable** number of arguments

            switch (f.getURI()) {
                // CONCAT (Sec 17.4.3.12)
                // string literal  CONCAT(string literal ltrl1 ... string literal ltrln)
                case "http://www.w3.org/2005/xpath-functions#concat":
                    if (arity < 1)
                        throw new UnsupportedOperationException("Wrong number of arguments (found " + terms.size() +
                                ", at least 1) for SPARQL function CONCAT");

                    Term concat = terms.get(0);
                    for (int i = 1; i < arity; i++) // .get(i) is OK because it's based on an array
                        concat = ofac.getFunctionConcat(concat, terms.get(i));
                    return concat;

                // REPLACE (Sec 17.4.3.15)
                //string literal  REPLACE (string literal arg, simple literal pattern, simple literal replacement )
                //string literal  REPLACE (string literal arg, simple literal pattern, simple literal replacement,  simple literal flags)
                case "http://www.w3.org/2005/xpath-functions#replace":
                    if (arity == 3)
                        return ofac.getFunctionReplace(terms.get(0), terms.get(1), terms.get(2));
                    else if (arity == 4)
                        // TODO: the fourth argument is flags (see http://www.w3.org/TR/xpath-functions/#flags)
                        // (it is ignored at the moment)
                        return ofac.getFunctionReplace(terms.get(0), terms.get(1), terms.get(2));

                    throw new UnsupportedOperationException("Wrong number of arguments (found "
                            + terms.size() + ", only 3 or 4 supported) for SPARQL function REPLACE");

                    // SUBSTR (Sec 17.4.3.3)
                    // string literal  SUBSTR(string literal source, xsd:integer startingLoc)
                    // string literal  SUBSTR(string literal source, xsd:integer startingLoc, xsd:integer length)
                case "http://www.w3.org/2005/xpath-functions#substring":
                    if (arity == 2)
                        return ofac.getFunctionSubstring(terms.get(0), terms.get(1));
                    else if (arity == 3)
                        return ofac.getFunctionSubstring(terms.get(0), terms.get(1), terms.get(2));

                    throw new UnsupportedOperationException("Wrong number of arguments (found "
                            + terms.size() + ", only 2 or 3 supported) for SPARQL function SUBSTRING");

                default:
                    throw new RuntimeException("Function " + f.getURI() + " is not supported yet!");
            }
		}
        // other subclasses
        // SubQueryValueOperator
        // If
        // BNodeGenerator
        // NAryValueOperator (ListMemberOperator and Coalesce)
		throw new RuntimeException("The expression " + expr + " is not supported yet!");
	}

	

	



    // XPath 1.0 functions (XPath 1.1 has variants with more arguments)
    private static final ImmutableMap<String, OperationPredicate> XPathFunctions =
            new ImmutableMap.Builder<String, OperationPredicate>()
                    .put("http://www.w3.org/2005/xpath-functions#upper-case", ExpressionOperation.UCASE)
                    .put("http://www.w3.org/2005/xpath-functions#lower-case", ExpressionOperation.LCASE)
                    .put("http://www.w3.org/2005/xpath-functions#string-length", ExpressionOperation.STRLEN)
                    .put("http://www.w3.org/2005/xpath-functions#substring-before", ExpressionOperation.STRBEFORE)
                    .put("http://www.w3.org/2005/xpath-functions#substring-after", ExpressionOperation.STRAFTER)
                    .put("http://www.w3.org/2005/xpath-functions#starts-with", ExpressionOperation.STR_STARTS)
                    .put("http://www.w3.org/2005/xpath-functions#ends-with", ExpressionOperation.STR_ENDS)
                    .put("http://www.w3.org/2005/xpath-functions#encode-for-uri", ExpressionOperation.ENCODE_FOR_URI)
                    .put("http://www.w3.org/2005/xpath-functions#contains", ExpressionOperation.CONTAINS)
                    .put("UUID", ExpressionOperation.UUID)
                    .put("STRUUID", ExpressionOperation.STRUUID)

                    .put("http://www.w3.org/2005/xpath-functions#numeric-abs", ExpressionOperation.ABS)
                    .put("http://www.w3.org/2005/xpath-functions#numeric-ceil", ExpressionOperation.CEIL)
                    .put("http://www.w3.org/2005/xpath-functions#numeric-floor", ExpressionOperation.FLOOR)
                    .put("http://www.w3.org/2005/xpath-functions#numeric-round", ExpressionOperation.ROUND)
                    .put("RAND", ExpressionOperation.RAND)

                    .put("http://www.w3.org/2005/xpath-functions#year-from-dateTime", ExpressionOperation.YEAR)
                    .put("http://www.w3.org/2005/xpath-functions#day-from-dateTime", ExpressionOperation.DAY)
                    .put("http://www.w3.org/2005/xpath-functions#month-from-dateTime", ExpressionOperation.MONTH)
                    .put("http://www.w3.org/2005/xpath-functions#hours-from-dateTime", ExpressionOperation.HOURS)
                    .put("http://www.w3.org/2005/xpath-functions#minutes-from-dateTime", ExpressionOperation.MINUTES)
                    .put("http://www.w3.org/2005/xpath-functions#seconds-from-dateTime", ExpressionOperation.SECONDS)
                    .put("NOW", ExpressionOperation.NOW)
                    .put("TZ", ExpressionOperation.TZ)

                    .put("MD5", ExpressionOperation.MD5)
                    .put("SHA1", ExpressionOperation.SHA1)
                    .put("SHA256", ExpressionOperation.SHA256)
                    .put("SHA512", ExpressionOperation.SHA512)
                    .build();

    private static final ImmutableMap<COL_TYPE, java.util.function.Function<Literal, String>> LiteralConversion =
            new ImmutableMap.Builder<COL_TYPE, java.util.function.Function<Literal, String>>()
                    .put (COL_TYPE.INTEGER, (Literal l) -> l.integerValue().toString())
                    .put (COL_TYPE.NEGATIVE_INTEGER, (Literal l) -> l.integerValue().toString())
                    .put (COL_TYPE.NON_POSITIVE_INTEGER, (Literal l) -> l.integerValue().toString())
                    .put (COL_TYPE.POSITIVE_INTEGER, (Literal l) -> l.integerValue().toString())
                    .put (COL_TYPE.NON_NEGATIVE_INTEGER, (Literal l) -> l.integerValue().toString())
                    .put (COL_TYPE.DECIMAL, (Literal l) -> l.decimalValue().toString())
                    .put (COL_TYPE.INT, (Literal l) -> Integer.toString(l.intValue()))
                    .put (COL_TYPE.UNSIGNED_INT, (Literal l) -> Integer.toString(l.intValue()))
                    .put (COL_TYPE.LONG, (Literal l) -> Long.toString(l.longValue()))
                    .put (COL_TYPE.FLOAT, (Literal l) -> Float.toString(l.floatValue()))
                    .put (COL_TYPE.DOUBLE, (Literal l) -> Double.toString(l.doubleValue()))
                    .put (COL_TYPE.BOOLEAN, (Literal l) -> Boolean.toString(l.booleanValue()))
                    .put (COL_TYPE.DATETIME_STAMP, (Literal l) -> l.calendarValue().toString())
                    .put (COL_TYPE.DATETIME, (Literal l) -> l.calendarValue().toString())
                    .put (COL_TYPE.YEAR, (Literal l) -> l.calendarValue().toString())
                    .put (COL_TYPE.DATE, (Literal l) -> l.calendarValue().toString())
                    .put (COL_TYPE.TIME, (Literal l) -> l.calendarValue().toString())
                    .put (COL_TYPE.STRING, (Literal l) -> l.stringValue())
                    .put (COL_TYPE.LITERAL, (Literal l) -> l.stringValue())
                    .build();

	private static final ImmutableMap<Compare.CompareOp, ExpressionOperation> RelationalOperations =
			new ImmutableMap.Builder<Compare.CompareOp, ExpressionOperation>()
				.put(Compare.CompareOp.EQ, ExpressionOperation.EQ)
				.put(Compare.CompareOp.GE, ExpressionOperation.GTE)
				.put(Compare.CompareOp.GT, ExpressionOperation.GT)
				.put(Compare.CompareOp.LE, ExpressionOperation.LTE)
				.put(Compare.CompareOp.LT, ExpressionOperation.LT)
				.put(Compare.CompareOp.NE, ExpressionOperation.NEQ)
				.build();

	private static final ImmutableMap<MathExpr.MathOp, ExpressionOperation> NumericalOperations =
			new ImmutableMap.Builder<MathExpr.MathOp, ExpressionOperation>()
			.put(MathExpr.MathOp.PLUS, ExpressionOperation.ADD)
			.put(MathExpr.MathOp.MINUS, ExpressionOperation.SUBTRACT)
			.put(MathExpr.MathOp.MULTIPLY, ExpressionOperation.MULTIPLY)
			.put(MathExpr.MathOp.DIVIDE, ExpressionOperation.DIVIDE)
			.build();

}
