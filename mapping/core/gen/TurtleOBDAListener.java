// Generated from /home/julien/workspace/ontop/ontop/mapping/core/src/main/antlr4/it/unibz/inf/ontop/spec/mapping/parser/impl/TurtleOBDA.g4 by ANTLR 4.7
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TurtleOBDAParser}.
 */
public interface TurtleOBDAListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#parse}.
	 * @param ctx the parse tree
	 */
	void enterParse(TurtleOBDAParser.ParseContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#parse}.
	 * @param ctx the parse tree
	 */
	void exitParse(TurtleOBDAParser.ParseContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#directiveStatement}.
	 * @param ctx the parse tree
	 */
	void enterDirectiveStatement(TurtleOBDAParser.DirectiveStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#directiveStatement}.
	 * @param ctx the parse tree
	 */
	void exitDirectiveStatement(TurtleOBDAParser.DirectiveStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#triplesStatement}.
	 * @param ctx the parse tree
	 */
	void enterTriplesStatement(TurtleOBDAParser.TriplesStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#triplesStatement}.
	 * @param ctx the parse tree
	 */
	void exitTriplesStatement(TurtleOBDAParser.TriplesStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#directive}.
	 * @param ctx the parse tree
	 */
	void enterDirective(TurtleOBDAParser.DirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#directive}.
	 * @param ctx the parse tree
	 */
	void exitDirective(TurtleOBDAParser.DirectiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#prefixID}.
	 * @param ctx the parse tree
	 */
	void enterPrefixID(TurtleOBDAParser.PrefixIDContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#prefixID}.
	 * @param ctx the parse tree
	 */
	void exitPrefixID(TurtleOBDAParser.PrefixIDContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#base}.
	 * @param ctx the parse tree
	 */
	void enterBase(TurtleOBDAParser.BaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#base}.
	 * @param ctx the parse tree
	 */
	void exitBase(TurtleOBDAParser.BaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#triples}.
	 * @param ctx the parse tree
	 */
	void enterTriples(TurtleOBDAParser.TriplesContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#triples}.
	 * @param ctx the parse tree
	 */
	void exitTriples(TurtleOBDAParser.TriplesContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#predicateObjectList}.
	 * @param ctx the parse tree
	 */
	void enterPredicateObjectList(TurtleOBDAParser.PredicateObjectListContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#predicateObjectList}.
	 * @param ctx the parse tree
	 */
	void exitPredicateObjectList(TurtleOBDAParser.PredicateObjectListContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#predicateObject}.
	 * @param ctx the parse tree
	 */
	void enterPredicateObject(TurtleOBDAParser.PredicateObjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#predicateObject}.
	 * @param ctx the parse tree
	 */
	void exitPredicateObject(TurtleOBDAParser.PredicateObjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#objectList}.
	 * @param ctx the parse tree
	 */
	void enterObjectList(TurtleOBDAParser.ObjectListContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#objectList}.
	 * @param ctx the parse tree
	 */
	void exitObjectList(TurtleOBDAParser.ObjectListContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#verb}.
	 * @param ctx the parse tree
	 */
	void enterVerb(TurtleOBDAParser.VerbContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#verb}.
	 * @param ctx the parse tree
	 */
	void exitVerb(TurtleOBDAParser.VerbContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#subject}.
	 * @param ctx the parse tree
	 */
	void enterSubject(TurtleOBDAParser.SubjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#subject}.
	 * @param ctx the parse tree
	 */
	void exitSubject(TurtleOBDAParser.SubjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#object}.
	 * @param ctx the parse tree
	 */
	void enterObject(TurtleOBDAParser.ObjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#object}.
	 * @param ctx the parse tree
	 */
	void exitObject(TurtleOBDAParser.ObjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#resource}.
	 * @param ctx the parse tree
	 */
	void enterResource(TurtleOBDAParser.ResourceContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#resource}.
	 * @param ctx the parse tree
	 */
	void exitResource(TurtleOBDAParser.ResourceContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#iriExt}.
	 * @param ctx the parse tree
	 */
	void enterIriExt(TurtleOBDAParser.IriExtContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#iriExt}.
	 * @param ctx the parse tree
	 */
	void exitIriExt(TurtleOBDAParser.IriExtContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#blank}.
	 * @param ctx the parse tree
	 */
	void enterBlank(TurtleOBDAParser.BlankContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#blank}.
	 * @param ctx the parse tree
	 */
	void exitBlank(TurtleOBDAParser.BlankContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#variable}.
	 * @param ctx the parse tree
	 */
	void enterVariable(TurtleOBDAParser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#variable}.
	 * @param ctx the parse tree
	 */
	void exitVariable(TurtleOBDAParser.VariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code variableLiteral_1}
	 * labeled alternative in {@link TurtleOBDAParser#variableLiteral}.
	 * @param ctx the parse tree
	 */
	void enterVariableLiteral_1(TurtleOBDAParser.VariableLiteral_1Context ctx);
	/**
	 * Exit a parse tree produced by the {@code variableLiteral_1}
	 * labeled alternative in {@link TurtleOBDAParser#variableLiteral}.
	 * @param ctx the parse tree
	 */
	void exitVariableLiteral_1(TurtleOBDAParser.VariableLiteral_1Context ctx);
	/**
	 * Enter a parse tree produced by the {@code variableLiteral_2}
	 * labeled alternative in {@link TurtleOBDAParser#variableLiteral}.
	 * @param ctx the parse tree
	 */
	void enterVariableLiteral_2(TurtleOBDAParser.VariableLiteral_2Context ctx);
	/**
	 * Exit a parse tree produced by the {@code variableLiteral_2}
	 * labeled alternative in {@link TurtleOBDAParser#variableLiteral}.
	 * @param ctx the parse tree
	 */
	void exitVariableLiteral_2(TurtleOBDAParser.VariableLiteral_2Context ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#languageTag}.
	 * @param ctx the parse tree
	 */
	void enterLanguageTag(TurtleOBDAParser.LanguageTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#languageTag}.
	 * @param ctx the parse tree
	 */
	void exitLanguageTag(TurtleOBDAParser.LanguageTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#iri}.
	 * @param ctx the parse tree
	 */
	void enterIri(TurtleOBDAParser.IriContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#iri}.
	 * @param ctx the parse tree
	 */
	void exitIri(TurtleOBDAParser.IriContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(TurtleOBDAParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(TurtleOBDAParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#untypedStringLiteral}.
	 * @param ctx the parse tree
	 */
	void enterUntypedStringLiteral(TurtleOBDAParser.UntypedStringLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#untypedStringLiteral}.
	 * @param ctx the parse tree
	 */
	void exitUntypedStringLiteral(TurtleOBDAParser.UntypedStringLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#typedLiteral}.
	 * @param ctx the parse tree
	 */
	void enterTypedLiteral(TurtleOBDAParser.TypedLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#typedLiteral}.
	 * @param ctx the parse tree
	 */
	void exitTypedLiteral(TurtleOBDAParser.TypedLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#litString}.
	 * @param ctx the parse tree
	 */
	void enterLitString(TurtleOBDAParser.LitStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#litString}.
	 * @param ctx the parse tree
	 */
	void exitLitString(TurtleOBDAParser.LitStringContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#untypedNumericLiteral}.
	 * @param ctx the parse tree
	 */
	void enterUntypedNumericLiteral(TurtleOBDAParser.UntypedNumericLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#untypedNumericLiteral}.
	 * @param ctx the parse tree
	 */
	void exitUntypedNumericLiteral(TurtleOBDAParser.UntypedNumericLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#untypedBooleanLiteral}.
	 * @param ctx the parse tree
	 */
	void enterUntypedBooleanLiteral(TurtleOBDAParser.UntypedBooleanLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#untypedBooleanLiteral}.
	 * @param ctx the parse tree
	 */
	void exitUntypedBooleanLiteral(TurtleOBDAParser.UntypedBooleanLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#numericUnsigned}.
	 * @param ctx the parse tree
	 */
	void enterNumericUnsigned(TurtleOBDAParser.NumericUnsignedContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#numericUnsigned}.
	 * @param ctx the parse tree
	 */
	void exitNumericUnsigned(TurtleOBDAParser.NumericUnsignedContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#numericPositive}.
	 * @param ctx the parse tree
	 */
	void enterNumericPositive(TurtleOBDAParser.NumericPositiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#numericPositive}.
	 * @param ctx the parse tree
	 */
	void exitNumericPositive(TurtleOBDAParser.NumericPositiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDAParser#numericNegative}.
	 * @param ctx the parse tree
	 */
	void enterNumericNegative(TurtleOBDAParser.NumericNegativeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDAParser#numericNegative}.
	 * @param ctx the parse tree
	 */
	void exitNumericNegative(TurtleOBDAParser.NumericNegativeContext ctx);
}