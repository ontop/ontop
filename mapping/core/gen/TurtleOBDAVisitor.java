// Generated from /home/julien/workspace/ontop/ontop/mapping/core/src/main/antlr4/it/unibz/inf/ontop/spec/mapping/parser/impl/TurtleOBDA.g4 by ANTLR 4.7
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TurtleOBDAParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TurtleOBDAVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#parse}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParse(TurtleOBDAParser.ParseContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#directiveStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirectiveStatement(TurtleOBDAParser.DirectiveStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#triplesStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriplesStatement(TurtleOBDAParser.TriplesStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#directive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirective(TurtleOBDAParser.DirectiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#prefixID}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrefixID(TurtleOBDAParser.PrefixIDContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#base}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBase(TurtleOBDAParser.BaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#triples}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriples(TurtleOBDAParser.TriplesContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#predicateObjectList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicateObjectList(TurtleOBDAParser.PredicateObjectListContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#predicateObject}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicateObject(TurtleOBDAParser.PredicateObjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#objectList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectList(TurtleOBDAParser.ObjectListContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#verb}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVerb(TurtleOBDAParser.VerbContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#subject}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubject(TurtleOBDAParser.SubjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#object}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObject(TurtleOBDAParser.ObjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#resource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResource(TurtleOBDAParser.ResourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#iriExt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIriExt(TurtleOBDAParser.IriExtContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#blank}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlank(TurtleOBDAParser.BlankContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(TurtleOBDAParser.VariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code variableLiteral_1}
	 * labeled alternative in {@link TurtleOBDAParser#variableLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableLiteral_1(TurtleOBDAParser.VariableLiteral_1Context ctx);
	/**
	 * Visit a parse tree produced by the {@code variableLiteral_2}
	 * labeled alternative in {@link TurtleOBDAParser#variableLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableLiteral_2(TurtleOBDAParser.VariableLiteral_2Context ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#languageTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLanguageTag(TurtleOBDAParser.LanguageTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#iri}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIri(TurtleOBDAParser.IriContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(TurtleOBDAParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#untypedStringLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUntypedStringLiteral(TurtleOBDAParser.UntypedStringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#typedLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypedLiteral(TurtleOBDAParser.TypedLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#litString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLitString(TurtleOBDAParser.LitStringContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#untypedNumericLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUntypedNumericLiteral(TurtleOBDAParser.UntypedNumericLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#untypedBooleanLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUntypedBooleanLiteral(TurtleOBDAParser.UntypedBooleanLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#numericUnsigned}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericUnsigned(TurtleOBDAParser.NumericUnsignedContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#numericPositive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericPositive(TurtleOBDAParser.NumericPositiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#numericNegative}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericNegative(TurtleOBDAParser.NumericNegativeContext ctx);
}