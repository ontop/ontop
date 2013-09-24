// $ANTLR 3.5 SQL99.g 2013-09-18 09:46:37

package it.unibz.krdb.obda.parser;

import java.util.Stack;
import java.util.Queue;
import java.util.LinkedList;
import java.util.EmptyStackException;

import java.lang.Number;

import it.unibz.krdb.sql.DBMetadata;

import it.unibz.krdb.sql.api.IValueExpression;
import it.unibz.krdb.sql.api.IPredicate;

import it.unibz.krdb.sql.api.QueryTree;
import it.unibz.krdb.sql.api.Projection;
import it.unibz.krdb.sql.api.Selection;
import it.unibz.krdb.sql.api.Aggregation;

import it.unibz.krdb.sql.api.Attribute;
import it.unibz.krdb.sql.api.JoinOperator;
import it.unibz.krdb.sql.api.SetUnion;
import it.unibz.krdb.sql.api.Relation;
import it.unibz.krdb.sql.api.RelationalAlgebra;

import it.unibz.krdb.sql.api.TableExpression;
import it.unibz.krdb.sql.api.AbstractValueExpression;
import it.unibz.krdb.sql.api.NumericValueExpression;
import it.unibz.krdb.sql.api.StringValueExpression;
import it.unibz.krdb.sql.api.ReferenceValueExpression;
import it.unibz.krdb.sql.api.CollectionValueExpression;
import it.unibz.krdb.sql.api.BooleanValueExpression;

import it.unibz.krdb.sql.api.TablePrimary;
import it.unibz.krdb.sql.api.DerivedColumn;
import it.unibz.krdb.sql.api.GroupingElement;
import it.unibz.krdb.sql.api.ComparisonPredicate;
import it.unibz.krdb.sql.api.NullPredicate;
import it.unibz.krdb.sql.api.AndOperator;
import it.unibz.krdb.sql.api.OrOperator;
import it.unibz.krdb.sql.api.LeftParenthesis;
import it.unibz.krdb.sql.api.RightParenthesis;
import it.unibz.krdb.sql.api.ColumnReference;

import it.unibz.krdb.sql.api.Literal;
import it.unibz.krdb.sql.api.StringLiteral;
import it.unibz.krdb.sql.api.BooleanLiteral;
import it.unibz.krdb.sql.api.NumericLiteral;
import it.unibz.krdb.sql.api.IntegerLiteral;
import it.unibz.krdb.sql.api.DecimalLiteral;
import it.unibz.krdb.sql.api.DateTimeLiteral;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class SQL99Parser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "ALL", "ALPHA", "ALPHANUM", "AMPERSAND", 
		"AND", "ANY", "APOSTROPHE", "AS", "ASTERISK", "AT", "AVG", "BACKSLASH", 
		"BY", "CARET", "CHAR", "COLON", "COMMA", "CONCATENATION", "COUNT", "DATETIME", 
		"DECIMAL", "DECIMAL_NEGATIVE", "DECIMAL_POSITIVE", "DIGIT", "DISTINCT", 
		"DOLLAR", "DOUBLE_SLASH", "ECHAR", "EQUALS", "EVERY", "EXCLAMATION", "FALSE", 
		"FROM", "FULL", "GREATER", "GROUP", "HASH", "IN", "INNER", "INTEGER", 
		"INTEGER_NEGATIVE", "INTEGER_POSITIVE", "IS", "JOIN", "LEFT", "LESS", 
		"LPAREN", "LSQ_BRACKET", "MAX", "MIN", "MINUS", "NOT", "NULL", "ON", "OR", 
		"ORDER", "OUTER", "PERCENT", "PERIOD", "PLUS", "QUESTION", "QUOTE_DOUBLE", 
		"QUOTE_SINGLE", "RIGHT", "RPAREN", "RSQ_BRACKET", "SELECT", "SEMI", "SOLIDUS", 
		"SOME", "STRING_WITH_QUOTE", "STRING_WITH_QUOTE_DOUBLE", "SUM", "TILDE", 
		"TRUE", "UNDERSCORE", "UNION", "USING", "VARNAME", "WHERE", "WS"
	};
	public static final int EOF=-1;
	public static final int ALL=4;
	public static final int ALPHA=5;
	public static final int ALPHANUM=6;
	public static final int AMPERSAND=7;
	public static final int AND=8;
	public static final int ANY=9;
	public static final int APOSTROPHE=10;
	public static final int AS=11;
	public static final int ASTERISK=12;
	public static final int AT=13;
	public static final int AVG=14;
	public static final int BACKSLASH=15;
	public static final int BY=16;
	public static final int CARET=17;
	public static final int CHAR=18;
	public static final int COLON=19;
	public static final int COMMA=20;
	public static final int CONCATENATION=21;
	public static final int COUNT=22;
	public static final int DATETIME=23;
	public static final int DECIMAL=24;
	public static final int DECIMAL_NEGATIVE=25;
	public static final int DECIMAL_POSITIVE=26;
	public static final int DIGIT=27;
	public static final int DISTINCT=28;
	public static final int DOLLAR=29;
	public static final int DOUBLE_SLASH=30;
	public static final int ECHAR=31;
	public static final int EQUALS=32;
	public static final int EVERY=33;
	public static final int EXCLAMATION=34;
	public static final int FALSE=35;
	public static final int FROM=36;
	public static final int FULL=37;
	public static final int GREATER=38;
	public static final int GROUP=39;
	public static final int HASH=40;
	public static final int IN=41;
	public static final int INNER=42;
	public static final int INTEGER=43;
	public static final int INTEGER_NEGATIVE=44;
	public static final int INTEGER_POSITIVE=45;
	public static final int IS=46;
	public static final int JOIN=47;
	public static final int LEFT=48;
	public static final int LESS=49;
	public static final int LPAREN=50;
	public static final int LSQ_BRACKET=51;
	public static final int MAX=52;
	public static final int MIN=53;
	public static final int MINUS=54;
	public static final int NOT=55;
	public static final int NULL=56;
	public static final int ON=57;
	public static final int OR=58;
	public static final int ORDER=59;
	public static final int OUTER=60;
	public static final int PERCENT=61;
	public static final int PERIOD=62;
	public static final int PLUS=63;
	public static final int QUESTION=64;
	public static final int QUOTE_DOUBLE=65;
	public static final int QUOTE_SINGLE=66;
	public static final int RIGHT=67;
	public static final int RPAREN=68;
	public static final int RSQ_BRACKET=69;
	public static final int SELECT=70;
	public static final int SEMI=71;
	public static final int SOLIDUS=72;
	public static final int SOME=73;
	public static final int STRING_WITH_QUOTE=74;
	public static final int STRING_WITH_QUOTE_DOUBLE=75;
	public static final int SUM=76;
	public static final int TILDE=77;
	public static final int TRUE=78;
	public static final int UNDERSCORE=79;
	public static final int UNION=80;
	public static final int USING=81;
	public static final int VARNAME=82;
	public static final int WHERE=83;
	public static final int WS=84;

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public SQL99Parser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public SQL99Parser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	@Override public String[] getTokenNames() { return SQL99Parser.tokenNames; }
	@Override public String getGrammarFileName() { return "SQL99.g"; }



	    String error = "";
	    
	    public String getError() {
	    	return error;
	    }

	    //protected void mismatch(IntStream input, int ttype, BitSet follow)
	    //throws RecognitionException
	    //{
	    //throw new MismatchedTokenException(ttype, input);
	    //}

	    //public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow)
	    //throws RecognitionException
	   // {
	    //throw e;
	    //}

	  //  @Override
	  //  public void recover(IntStream input, RecognitionException re) {
	  //  	throw new RuntimeException(error);
	  //  }

	    
	    @Override
	    public void displayRecognitionError(String[] tokenNames,
	                                        RecognitionException e) {
	        String hdr = getErrorHeader(e);
	        String msg = getErrorMessage(e, tokenNames);
	        emitErrorMessage("Syntax error: " + msg + " Location: " + hdr);
	    }
	    @Override
	    public void emitErrorMessage(	String 	msg	 ) 	{
	    	error = msg;
	    	    }
	    
	  //  @Override
	 //   public Object recoverFromMismatchedToken	(	IntStream 	input,
	 //   		int 	ttype,
	 //   		BitSet 	follow	 
	 //   		)			 throws RecognitionException {
	 //   	throw new RecognitionException(input);
	 //   }
	    

	/** Global stack for keeping the projection column list */
	private Stack<Projection> projectionStack = new Stack<Projection>();

	/** Global stack for keeping the select all projection */
	private Stack<Boolean> AsteriskStack = new Stack<Boolean>();

	/** Global stack for keeping the relations */
	private Stack<RelationalAlgebra> relationStack = new Stack<RelationalAlgebra>();

	/** Temporary cache for keeping the numeric value expression */
	private NumericValueExpression numericExp;

	/** Temporary cache for keeping the string value expression */
	private StringValueExpression stringExp;

	/** Temporary cache for keeping the reference value expression */
	private ReferenceValueExpression referenceExp;

	/** Temporary cache for keeping the collection value expression */
	private CollectionValueExpression collectionExp;

	/** Temporary cache for keeping the boolean value expression */
	private BooleanValueExpression booleanExp;

	/** The root of the query tree */
	private QueryTree queryTree;

	/**
	 * Retrieves the query tree object. The tree represents
	 * the data structure of the SQL statement.
	 *
	 * @return Returns a query tree.
	 */
	public QueryTree getQueryTree() {
	  return queryTree;
	}

	/**
	 * A helper method to construct the projection. A projection
	 * object holds the information about the table columns in
	 * the SELECT keyword.
	 */
	private Projection createProjection(ArrayList<DerivedColumn> columnList) {
	  Projection prj = new Projection();
	  prj.addAll(columnList);
	  return prj;
	}

	/**
	 * A helper method to construct the selection. A selection object
	 * holds the information about the comparison predicate (e.g., A = B)
	 * in the WHERE statment.
	 */
	private Selection createSelection(BooleanValueExpression booleanExp) {
	  if (booleanExp == null) {
	    return null;
	  }
	  Selection slc = new Selection();
	  
	  try {
		  Queue<Object> specification = booleanExp.getSpecification();
		  slc.copy(specification);
		}
	  catch(Exception e) {
	    // Does nothing.
	  }
	  return slc;
	}

	/**
	 * A helper method to constuct the aggregation. An aggregation object
	 * holds the information about the table attributes that are used
	 * to group the data records. They appear in the GROUP BY statement.
	 */
	private Aggregation createAggregation(ArrayList<GroupingElement> groupingList) {
	  if (groupingList == null) {
	    return null;
	  }
	  Aggregation agg = new Aggregation();
	  agg.addAll(groupingList);
	  return agg;
	}

	/**
	 * Another helper method to construct the query tree. This method
	 * constructs the sub-tree taken the information from a query 
	 * specification.
	 *
	 * @param relation
	 *           The root of this sub-tree.
	 * @return Returns the query sub-tree.
	 */
	private QueryTree constructQueryTree(RelationalAlgebra relation) {

	  QueryTree parent = new QueryTree(relation);
	  
	  int flag = 1;
	  while (!relationStack.isEmpty()) {
	    relation = relationStack.pop();
	    QueryTree node = new QueryTree(relation);
	        
	    if ((flag % 2) == 1) {  // right child
	      parent.attachRight(node);
	    }
	    else {  // left child
	      parent.attachLeft(node);
	      parent = node;
	    }
	    flag++;
	  }
	  return parent.root();
	}



	// $ANTLR start "parse"
	// SQL99.g:277:1: parse returns [QueryTree value] : query EOF ;
	public final QueryTree parse() throws RecognitionException {
		QueryTree value = null;


		QueryTree query1 =null;

		try {
			// SQL99.g:278:3: ( query EOF )
			// SQL99.g:278:5: query EOF
			{
			pushFollow(FOLLOW_query_in_parse51);
			query1=query();
			state._fsp--;

			match(input,EOF,FOLLOW_EOF_in_parse53); 

			      value = query1;
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "parse"



	// $ANTLR start "query"
	// SQL99.g:283:1: query returns [QueryTree value] : query_specification ;
	public final QueryTree query() throws RecognitionException {
		QueryTree value = null;


		QueryTree query_specification2 =null;


		int quantifier = 0;

		try {
			// SQL99.g:287:3: ( query_specification )
			// SQL99.g:287:5: query_specification
			{
			pushFollow(FOLLOW_query_specification_in_query79);
			query_specification2=query_specification();
			state._fsp--;

			 
			      queryTree = query_specification2; 
			      value = queryTree;
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "query"



	// $ANTLR start "query_specification"
	// SQL99.g:308:1: query_specification returns [QueryTree value] : SELECT ( set_quantifier )? select_list table_expression ;
	public final QueryTree query_specification() throws RecognitionException {
		QueryTree value = null;


		TableExpression table_expression3 =null;
		ArrayList<DerivedColumn> select_list4 =null;
		int set_quantifier5 =0;


		int quantifier = 0;

		try {
			// SQL99.g:312:3: ( SELECT ( set_quantifier )? select_list table_expression )
			// SQL99.g:312:5: SELECT ( set_quantifier )? select_list table_expression
			{
			match(input,SELECT,FOLLOW_SELECT_in_query_specification120); 
			// SQL99.g:312:12: ( set_quantifier )?
			int alt1=2;
			int LA1_0 = input.LA(1);
			if ( (LA1_0==ALL||LA1_0==DISTINCT) ) {
				alt1=1;
			}
			switch (alt1) {
				case 1 :
					// SQL99.g:312:12: set_quantifier
					{
					pushFollow(FOLLOW_set_quantifier_in_query_specification122);
					set_quantifier5=set_quantifier();
					state._fsp--;

					}
					break;

			}

			pushFollow(FOLLOW_select_list_in_query_specification125);
			select_list4=select_list();
			state._fsp--;

			pushFollow(FOLLOW_table_expression_in_query_specification127);
			table_expression3=table_expression();
			state._fsp--;


			  
			      TableExpression te = table_expression3;
			      
			      // Construct the projection
			      ArrayList<TablePrimary> tableList = te.getFromClause();
			      ArrayList<DerivedColumn> columnList = select_list4;
			      Projection prj = createProjection(columnList);
			            
			      quantifier = set_quantifier5;
			      prj.setType(quantifier);
			      
			      // Construct the selection
			      BooleanValueExpression booleanExp = te.getWhereClause();
			      Selection slc = createSelection(booleanExp);
			      
			      // Construct the aggregation
			      ArrayList<GroupingElement> groupingList = te.getGroupByClause();
			      Aggregation agg = createAggregation(groupingList);
			      
			      // Construct the query tree
			      try {
				      RelationalAlgebra root = relationStack.pop();
				      root.setProjection(prj);
				      if (slc != null) {
				        root.setSelection(slc);
				      }
				      if (agg != null) {
				        root.setAggregation(agg);
				      }
				      value = constructQueryTree(root);
			      } 
			      catch(EmptyStackException e) {
			        // Does nothing
			      } 
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "query_specification"



	// $ANTLR start "set_quantifier"
	// SQL99.g:356:1: set_quantifier returns [int value] : ( ALL | DISTINCT );
	public final int set_quantifier() throws RecognitionException {
		int value = 0;


		try {
			// SQL99.g:357:3: ( ALL | DISTINCT )
			int alt2=2;
			int LA2_0 = input.LA(1);
			if ( (LA2_0==ALL) ) {
				alt2=1;
			}
			else if ( (LA2_0==DISTINCT) ) {
				alt2=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 2, 0, input);
				throw nvae;
			}

			switch (alt2) {
				case 1 :
					// SQL99.g:357:5: ALL
					{
					match(input,ALL,FOLLOW_ALL_in_set_quantifier148); 
					 value = 1; 
					}
					break;
				case 2 :
					// SQL99.g:358:5: DISTINCT
					{
					match(input,DISTINCT,FOLLOW_DISTINCT_in_set_quantifier156); 
					 value = 2; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "set_quantifier"



	// $ANTLR start "select_list"
	// SQL99.g:361:1: select_list returns [ArrayList<DerivedColumn> value] : a= select_sublist ( COMMA b= select_sublist )* ;
	public final ArrayList<DerivedColumn> select_list() throws RecognitionException {
		ArrayList<DerivedColumn> value = null;


		DerivedColumn a =null;
		DerivedColumn b =null;


		  value = new ArrayList<DerivedColumn>();

		try {
			// SQL99.g:365:3: (a= select_sublist ( COMMA b= select_sublist )* )
			// SQL99.g:365:5: a= select_sublist ( COMMA b= select_sublist )*
			{
			pushFollow(FOLLOW_select_sublist_in_select_list184);
			a=select_sublist();
			state._fsp--;

			 value.add(a); 
			// SQL99.g:365:48: ( COMMA b= select_sublist )*
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( (LA3_0==COMMA) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// SQL99.g:365:49: COMMA b= select_sublist
					{
					match(input,COMMA,FOLLOW_COMMA_in_select_list189); 
					pushFollow(FOLLOW_select_sublist_in_select_list193);
					b=select_sublist();
					state._fsp--;

					 value.add(b); 
					}
					break;

				default :
					break loop3;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "select_list"



	// $ANTLR start "select_sublist"
	// SQL99.g:368:1: select_sublist returns [DerivedColumn value] : derived_column ;
	public final DerivedColumn select_sublist() throws RecognitionException {
		DerivedColumn value = null;


		DerivedColumn derived_column6 =null;

		try {
			// SQL99.g:369:3: ( derived_column )
			// SQL99.g:369:5: derived_column
			{
			pushFollow(FOLLOW_derived_column_in_select_sublist216);
			derived_column6=derived_column();
			state._fsp--;

			 value = derived_column6; 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "select_sublist"



	// $ANTLR start "qualified_asterisk"
	// SQL99.g:373:1: qualified_asterisk : table_identifier PERIOD ASTERISK ;
	public final void qualified_asterisk() throws RecognitionException {
		try {
			// SQL99.g:374:3: ( table_identifier PERIOD ASTERISK )
			// SQL99.g:374:5: table_identifier PERIOD ASTERISK
			{
			pushFollow(FOLLOW_table_identifier_in_qualified_asterisk234);
			table_identifier();
			state._fsp--;

			match(input,PERIOD,FOLLOW_PERIOD_in_qualified_asterisk236); 
			match(input,ASTERISK,FOLLOW_ASTERISK_in_qualified_asterisk238); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "qualified_asterisk"



	// $ANTLR start "derived_column"
	// SQL99.g:377:1: derived_column returns [DerivedColumn value] : value_expression ( ( AS )? alias_name )? ;
	public final DerivedColumn derived_column() throws RecognitionException {
		DerivedColumn value = null;


		AbstractValueExpression value_expression7 =null;
		String alias_name8 =null;


		  value = new DerivedColumn();

		try {
			// SQL99.g:381:3: ( value_expression ( ( AS )? alias_name )? )
			// SQL99.g:381:5: value_expression ( ( AS )? alias_name )?
			{
			pushFollow(FOLLOW_value_expression_in_derived_column262);
			value_expression7=value_expression();
			state._fsp--;

			// SQL99.g:381:22: ( ( AS )? alias_name )?
			int alt5=2;
			int LA5_0 = input.LA(1);
			if ( (LA5_0==AS||LA5_0==STRING_WITH_QUOTE_DOUBLE||LA5_0==VARNAME) ) {
				alt5=1;
			}
			switch (alt5) {
				case 1 :
					// SQL99.g:381:23: ( AS )? alias_name
					{
					// SQL99.g:381:23: ( AS )?
					int alt4=2;
					int LA4_0 = input.LA(1);
					if ( (LA4_0==AS) ) {
						alt4=1;
					}
					switch (alt4) {
						case 1 :
							// SQL99.g:381:23: AS
							{
							match(input,AS,FOLLOW_AS_in_derived_column265); 
							}
							break;

					}

					pushFollow(FOLLOW_alias_name_in_derived_column268);
					alias_name8=alias_name();
					state._fsp--;

					}
					break;

			}


			      value.setValueExpression(value_expression7);
			      String alias = alias_name8;
			      if (alias != null) {
			        value.setAlias(alias_name8);
			      }
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "derived_column"



	// $ANTLR start "value_expression"
	// SQL99.g:390:1: value_expression returns [AbstractValueExpression value] : reference_value_expression ;
	public final AbstractValueExpression value_expression() throws RecognitionException {
		AbstractValueExpression value = null;


		ReferenceValueExpression reference_value_expression9 =null;

		try {
			// SQL99.g:391:3: ( reference_value_expression )
			// SQL99.g:391:5: reference_value_expression
			{
			pushFollow(FOLLOW_reference_value_expression_in_value_expression292);
			reference_value_expression9=reference_value_expression();
			state._fsp--;

			 value = reference_value_expression9; 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "value_expression"



	// $ANTLR start "numeric_value_expression"
	// SQL99.g:397:1: numeric_value_expression returns [NumericValueExpression value] : LPAREN numeric_operation RPAREN ;
	public final NumericValueExpression numeric_value_expression() throws RecognitionException {
		NumericValueExpression value = null;



		  numericExp = new NumericValueExpression();

		try {
			// SQL99.g:401:3: ( LPAREN numeric_operation RPAREN )
			// SQL99.g:401:5: LPAREN numeric_operation RPAREN
			{
			match(input,LPAREN,FOLLOW_LPAREN_in_numeric_value_expression319); 
			pushFollow(FOLLOW_numeric_operation_in_numeric_value_expression321);
			numeric_operation();
			state._fsp--;

			match(input,RPAREN,FOLLOW_RPAREN_in_numeric_value_expression323); 

			      value = numericExp;
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "numeric_value_expression"



	// $ANTLR start "numeric_operation"
	// SQL99.g:406:1: numeric_operation : term ( (t= PLUS |t= MINUS ) term )* ;
	public final void numeric_operation() throws RecognitionException {
		Token t=null;

		try {
			// SQL99.g:407:3: ( term ( (t= PLUS |t= MINUS ) term )* )
			// SQL99.g:407:5: term ( (t= PLUS |t= MINUS ) term )*
			{
			pushFollow(FOLLOW_term_in_numeric_operation338);
			term();
			state._fsp--;

			// SQL99.g:408:5: ( (t= PLUS |t= MINUS ) term )*
			loop7:
			while (true) {
				int alt7=2;
				int LA7_0 = input.LA(1);
				if ( (LA7_0==MINUS||LA7_0==PLUS) ) {
					alt7=1;
				}

				switch (alt7) {
				case 1 :
					// SQL99.g:409:7: (t= PLUS |t= MINUS ) term
					{
					// SQL99.g:409:7: (t= PLUS |t= MINUS )
					int alt6=2;
					int LA6_0 = input.LA(1);
					if ( (LA6_0==PLUS) ) {
						alt6=1;
					}
					else if ( (LA6_0==MINUS) ) {
						alt6=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 6, 0, input);
						throw nvae;
					}

					switch (alt6) {
						case 1 :
							// SQL99.g:409:8: t= PLUS
							{
							t=(Token)match(input,PLUS,FOLLOW_PLUS_in_numeric_operation356); 
							}
							break;
						case 2 :
							// SQL99.g:409:15: t= MINUS
							{
							t=(Token)match(input,MINUS,FOLLOW_MINUS_in_numeric_operation360); 
							}
							break;

					}

					 numericExp.putSpecification((t!=null?t.getText():null)); 
					pushFollow(FOLLOW_term_in_numeric_operation373);
					term();
					state._fsp--;

					}
					break;

				default :
					break loop7;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "numeric_operation"



	// $ANTLR start "term"
	// SQL99.g:414:1: term : a= factor ( (t= ASTERISK |t= SOLIDUS ) b= factor )* ;
	public final void term() throws RecognitionException {
		Token t=null;
		Object a =null;
		Object b =null;

		try {
			// SQL99.g:415:3: (a= factor ( (t= ASTERISK |t= SOLIDUS ) b= factor )* )
			// SQL99.g:415:5: a= factor ( (t= ASTERISK |t= SOLIDUS ) b= factor )*
			{
			pushFollow(FOLLOW_factor_in_term395);
			a=factor();
			state._fsp--;

			 numericExp.putSpecification(a); 
			// SQL99.g:416:5: ( (t= ASTERISK |t= SOLIDUS ) b= factor )*
			loop9:
			while (true) {
				int alt9=2;
				int LA9_0 = input.LA(1);
				if ( (LA9_0==ASTERISK||LA9_0==SOLIDUS) ) {
					alt9=1;
				}

				switch (alt9) {
				case 1 :
					// SQL99.g:417:7: (t= ASTERISK |t= SOLIDUS ) b= factor
					{
					// SQL99.g:417:7: (t= ASTERISK |t= SOLIDUS )
					int alt8=2;
					int LA8_0 = input.LA(1);
					if ( (LA8_0==ASTERISK) ) {
						alt8=1;
					}
					else if ( (LA8_0==SOLIDUS) ) {
						alt8=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 8, 0, input);
						throw nvae;
					}

					switch (alt8) {
						case 1 :
							// SQL99.g:417:8: t= ASTERISK
							{
							t=(Token)match(input,ASTERISK,FOLLOW_ASTERISK_in_term415); 
							}
							break;
						case 2 :
							// SQL99.g:417:19: t= SOLIDUS
							{
							t=(Token)match(input,SOLIDUS,FOLLOW_SOLIDUS_in_term419); 
							}
							break;

					}

					 numericExp.putSpecification((t!=null?t.getText():null)); 
					pushFollow(FOLLOW_factor_in_term433);
					b=factor();
					state._fsp--;

					 numericExp.putSpecification(b); 
					}
					break;

				default :
					break loop9;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "term"



	// $ANTLR start "factor"
	// SQL99.g:422:1: factor returns [Object value] : ( column_reference | numeric_literal );
	public final Object factor() throws RecognitionException {
		Object value = null;


		ColumnReference column_reference10 =null;
		NumericLiteral numeric_literal11 =null;

		try {
			// SQL99.g:423:3: ( column_reference | numeric_literal )
			int alt10=2;
			int LA10_0 = input.LA(1);
			if ( (LA10_0==PERIOD||LA10_0==STRING_WITH_QUOTE_DOUBLE||LA10_0==VARNAME) ) {
				alt10=1;
			}
			else if ( ((LA10_0 >= DECIMAL && LA10_0 <= DECIMAL_POSITIVE)||(LA10_0 >= INTEGER && LA10_0 <= INTEGER_POSITIVE)) ) {
				alt10=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 10, 0, input);
				throw nvae;
			}

			switch (alt10) {
				case 1 :
					// SQL99.g:423:5: column_reference
					{
					pushFollow(FOLLOW_column_reference_in_factor461);
					column_reference10=column_reference();
					state._fsp--;

					 value = column_reference10; 
					}
					break;
				case 2 :
					// SQL99.g:424:5: numeric_literal
					{
					pushFollow(FOLLOW_numeric_literal_in_factor469);
					numeric_literal11=numeric_literal();
					state._fsp--;

					 value = numeric_literal11; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "factor"



	// $ANTLR start "sign"
	// SQL99.g:427:1: sign : ( PLUS | MINUS );
	public final void sign() throws RecognitionException {
		try {
			// SQL99.g:428:3: ( PLUS | MINUS )
			// SQL99.g:
			{
			if ( input.LA(1)==MINUS||input.LA(1)==PLUS ) {
				input.consume();
				state.errorRecovery=false;
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "sign"



	// $ANTLR start "string_value_expression"
	// SQL99.g:432:1: string_value_expression returns [StringValueExpression value] : LPAREN concatenation RPAREN ;
	public final StringValueExpression string_value_expression() throws RecognitionException {
		StringValueExpression value = null;



		  stringExp = new StringValueExpression();

		try {
			// SQL99.g:436:3: ( LPAREN concatenation RPAREN )
			// SQL99.g:436:5: LPAREN concatenation RPAREN
			{
			match(input,LPAREN,FOLLOW_LPAREN_in_string_value_expression512); 
			pushFollow(FOLLOW_concatenation_in_string_value_expression514);
			concatenation();
			state._fsp--;

			match(input,RPAREN,FOLLOW_RPAREN_in_string_value_expression516); 

			      value = stringExp;
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "string_value_expression"



	// $ANTLR start "concatenation"
	// SQL99.g:441:1: concatenation : a= character_factor ( CONCATENATION b= character_factor )+ ;
	public final void concatenation() throws RecognitionException {
		Object a =null;
		Object b =null;

		try {
			// SQL99.g:442:3: (a= character_factor ( CONCATENATION b= character_factor )+ )
			// SQL99.g:442:5: a= character_factor ( CONCATENATION b= character_factor )+
			{
			pushFollow(FOLLOW_character_factor_in_concatenation535);
			a=character_factor();
			state._fsp--;

			 stringExp.putSpecification(a); 
			// SQL99.g:442:66: ( CONCATENATION b= character_factor )+
			int cnt11=0;
			loop11:
			while (true) {
				int alt11=2;
				int LA11_0 = input.LA(1);
				if ( (LA11_0==CONCATENATION) ) {
					alt11=1;
				}

				switch (alt11) {
				case 1 :
					// SQL99.g:443:7: CONCATENATION b= character_factor
					{
					match(input,CONCATENATION,FOLLOW_CONCATENATION_in_concatenation547); 
					 stringExp.putSpecification(StringValueExpression.CONCAT_OP); 
					pushFollow(FOLLOW_character_factor_in_concatenation560);
					b=character_factor();
					state._fsp--;

					 stringExp.putSpecification(b); 
					}
					break;

				default :
					if ( cnt11 >= 1 ) break loop11;
					EarlyExitException eee = new EarlyExitException(11, input);
					throw eee;
				}
				cnt11++;
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "concatenation"



	// $ANTLR start "character_factor"
	// SQL99.g:447:1: character_factor returns [Object value] : ( column_reference | general_literal );
	public final Object character_factor() throws RecognitionException {
		Object value = null;


		ColumnReference column_reference12 =null;
		Literal general_literal13 =null;

		try {
			// SQL99.g:448:3: ( column_reference | general_literal )
			int alt12=2;
			int LA12_0 = input.LA(1);
			if ( (LA12_0==PERIOD||LA12_0==STRING_WITH_QUOTE_DOUBLE||LA12_0==VARNAME) ) {
				alt12=1;
			}
			else if ( (LA12_0==FALSE||LA12_0==STRING_WITH_QUOTE||LA12_0==TRUE) ) {
				alt12=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 12, 0, input);
				throw nvae;
			}

			switch (alt12) {
				case 1 :
					// SQL99.g:448:5: column_reference
					{
					pushFollow(FOLLOW_column_reference_in_character_factor581);
					column_reference12=column_reference();
					state._fsp--;

					 value = column_reference12; 
					}
					break;
				case 2 :
					// SQL99.g:449:5: general_literal
					{
					pushFollow(FOLLOW_general_literal_in_character_factor589);
					general_literal13=general_literal();
					state._fsp--;

					 value = general_literal13; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "character_factor"



	// $ANTLR start "reference_value_expression"
	// SQL99.g:452:1: reference_value_expression returns [ReferenceValueExpression value] : column_reference ;
	public final ReferenceValueExpression reference_value_expression() throws RecognitionException {
		ReferenceValueExpression value = null;


		ColumnReference column_reference14 =null;


		  referenceExp = new ReferenceValueExpression();

		try {
			// SQL99.g:456:3: ( column_reference )
			// SQL99.g:456:5: column_reference
			{
			pushFollow(FOLLOW_column_reference_in_reference_value_expression613);
			column_reference14=column_reference();
			state._fsp--;

			 
			      referenceExp.add(column_reference14);
			      value = referenceExp;
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "reference_value_expression"



	// $ANTLR start "column_reference"
	// SQL99.g:462:1: column_reference returns [ColumnReference value] : ( (s= schema_name )? PERIOD t= table_identifier PERIOD )? column_name ;
	public final ColumnReference column_reference() throws RecognitionException {
		ColumnReference value = null;


		ArrayList<String> s =null;
		ArrayList<String> t =null;
		String column_name15 =null;

		try {
			// SQL99.g:463:3: ( ( (s= schema_name )? PERIOD t= table_identifier PERIOD )? column_name )
			// SQL99.g:463:5: ( (s= schema_name )? PERIOD t= table_identifier PERIOD )? column_name
			{
			// SQL99.g:463:5: ( (s= schema_name )? PERIOD t= table_identifier PERIOD )?
			int alt14=2;
			switch ( input.LA(1) ) {
				case VARNAME:
					{
					int LA14_1 = input.LA(2);
					if ( (LA14_1==PERIOD) ) {
						alt14=1;
					}
					}
					break;
				case STRING_WITH_QUOTE_DOUBLE:
					{
					int LA14_2 = input.LA(2);
					if ( (LA14_2==PERIOD) ) {
						alt14=1;
					}
					}
					break;
				case PERIOD:
					{
					alt14=1;
					}
					break;
			}
			switch (alt14) {
				case 1 :
					// SQL99.g:463:6: (s= schema_name )? PERIOD t= table_identifier PERIOD
					{
					// SQL99.g:463:6: (s= schema_name )?
					int alt13=2;
					int LA13_0 = input.LA(1);
					if ( (LA13_0==STRING_WITH_QUOTE_DOUBLE||LA13_0==VARNAME) ) {
						alt13=1;
					}
					switch (alt13) {
						case 1 :
							// SQL99.g:463:7: s= schema_name
							{
							pushFollow(FOLLOW_schema_name_in_column_reference636);
							s=schema_name();
							state._fsp--;

							}
							break;

					}

					match(input,PERIOD,FOLLOW_PERIOD_in_column_reference640); 
					pushFollow(FOLLOW_table_identifier_in_column_reference644);
					t=table_identifier();
					state._fsp--;

					match(input,PERIOD,FOLLOW_PERIOD_in_column_reference646); 
					}
					break;

			}

			pushFollow(FOLLOW_column_name_in_column_reference650);
			column_name15=column_name();
			state._fsp--;


			      String table = "";
			      if (t != null) {
			        table = t.get(0);
			        if (s != null) 
			        	table = s.get(0) + "." + table;
			      }
			      value = new ColumnReference(table, column_name15);
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "column_reference"



	// $ANTLR start "collection_value_expression"
	// SQL99.g:474:1: collection_value_expression returns [CollectionValueExpression value] : set_function_specification ;
	public final CollectionValueExpression collection_value_expression() throws RecognitionException {
		CollectionValueExpression value = null;



		  collectionExp = new CollectionValueExpression();

		try {
			// SQL99.g:478:3: ( set_function_specification )
			// SQL99.g:478:5: set_function_specification
			{
			pushFollow(FOLLOW_set_function_specification_in_collection_value_expression678);
			set_function_specification();
			state._fsp--;

			 
			      value = collectionExp;
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "collection_value_expression"



	// $ANTLR start "set_function_specification"
	// SQL99.g:483:1: set_function_specification : ( COUNT LPAREN ASTERISK RPAREN | general_set_function );
	public final void set_function_specification() throws RecognitionException {
		Token COUNT16=null;
		Token ASTERISK17=null;

		try {
			// SQL99.g:484:3: ( COUNT LPAREN ASTERISK RPAREN | general_set_function )
			int alt15=2;
			int LA15_0 = input.LA(1);
			if ( (LA15_0==COUNT) ) {
				int LA15_1 = input.LA(2);
				if ( (LA15_1==LPAREN) ) {
					int LA15_3 = input.LA(3);
					if ( (LA15_3==ASTERISK) ) {
						alt15=1;
					}
					else if ( (LA15_3==PERIOD||LA15_3==STRING_WITH_QUOTE_DOUBLE||LA15_3==VARNAME) ) {
						alt15=2;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 15, 3, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 15, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA15_0==ANY||LA15_0==AVG||LA15_0==EVERY||(LA15_0 >= MAX && LA15_0 <= MIN)||LA15_0==SOME||LA15_0==SUM) ) {
				alt15=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 15, 0, input);
				throw nvae;
			}

			switch (alt15) {
				case 1 :
					// SQL99.g:484:5: COUNT LPAREN ASTERISK RPAREN
					{
					COUNT16=(Token)match(input,COUNT,FOLLOW_COUNT_in_set_function_specification693); 
					match(input,LPAREN,FOLLOW_LPAREN_in_set_function_specification695); 
					ASTERISK17=(Token)match(input,ASTERISK,FOLLOW_ASTERISK_in_set_function_specification697); 
					match(input,RPAREN,FOLLOW_RPAREN_in_set_function_specification699); 

					      collectionExp.putSpecification((COUNT16!=null?COUNT16.getText():null));
					      collectionExp.putSpecification((ASTERISK17!=null?ASTERISK17.getText():null));
					    
					}
					break;
				case 2 :
					// SQL99.g:488:5: general_set_function
					{
					pushFollow(FOLLOW_general_set_function_in_set_function_specification707);
					general_set_function();
					state._fsp--;

					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "set_function_specification"



	// $ANTLR start "general_set_function"
	// SQL99.g:492:1: general_set_function : set_function_op LPAREN column_reference RPAREN ;
	public final void general_set_function() throws RecognitionException {
		String set_function_op18 =null;
		ColumnReference column_reference19 =null;

		try {
			// SQL99.g:493:3: ( set_function_op LPAREN column_reference RPAREN )
			// SQL99.g:493:5: set_function_op LPAREN column_reference RPAREN
			{
			pushFollow(FOLLOW_set_function_op_in_general_set_function722);
			set_function_op18=set_function_op();
			state._fsp--;

			match(input,LPAREN,FOLLOW_LPAREN_in_general_set_function724); 
			pushFollow(FOLLOW_column_reference_in_general_set_function726);
			column_reference19=column_reference();
			state._fsp--;

			match(input,RPAREN,FOLLOW_RPAREN_in_general_set_function728); 

			      collectionExp.putSpecification(set_function_op18);
			      collectionExp.add(column_reference19);
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "general_set_function"



	// $ANTLR start "set_function_op"
	// SQL99.g:499:1: set_function_op returns [String value] : (t= AVG |t= MAX |t= MIN |t= SUM |t= EVERY |t= ANY |t= SOME |t= COUNT ) ;
	public final String set_function_op() throws RecognitionException {
		String value = null;


		Token t=null;

		try {
			// SQL99.g:500:3: ( (t= AVG |t= MAX |t= MIN |t= SUM |t= EVERY |t= ANY |t= SOME |t= COUNT ) )
			// SQL99.g:500:5: (t= AVG |t= MAX |t= MIN |t= SUM |t= EVERY |t= ANY |t= SOME |t= COUNT )
			{
			// SQL99.g:500:5: (t= AVG |t= MAX |t= MIN |t= SUM |t= EVERY |t= ANY |t= SOME |t= COUNT )
			int alt16=8;
			switch ( input.LA(1) ) {
			case AVG:
				{
				alt16=1;
				}
				break;
			case MAX:
				{
				alt16=2;
				}
				break;
			case MIN:
				{
				alt16=3;
				}
				break;
			case SUM:
				{
				alt16=4;
				}
				break;
			case EVERY:
				{
				alt16=5;
				}
				break;
			case ANY:
				{
				alt16=6;
				}
				break;
			case SOME:
				{
				alt16=7;
				}
				break;
			case COUNT:
				{
				alt16=8;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 16, 0, input);
				throw nvae;
			}
			switch (alt16) {
				case 1 :
					// SQL99.g:500:6: t= AVG
					{
					t=(Token)match(input,AVG,FOLLOW_AVG_in_set_function_op752); 
					}
					break;
				case 2 :
					// SQL99.g:500:14: t= MAX
					{
					t=(Token)match(input,MAX,FOLLOW_MAX_in_set_function_op758); 
					}
					break;
				case 3 :
					// SQL99.g:500:22: t= MIN
					{
					t=(Token)match(input,MIN,FOLLOW_MIN_in_set_function_op764); 
					}
					break;
				case 4 :
					// SQL99.g:500:30: t= SUM
					{
					t=(Token)match(input,SUM,FOLLOW_SUM_in_set_function_op770); 
					}
					break;
				case 5 :
					// SQL99.g:500:38: t= EVERY
					{
					t=(Token)match(input,EVERY,FOLLOW_EVERY_in_set_function_op776); 
					}
					break;
				case 6 :
					// SQL99.g:500:48: t= ANY
					{
					t=(Token)match(input,ANY,FOLLOW_ANY_in_set_function_op782); 
					}
					break;
				case 7 :
					// SQL99.g:500:56: t= SOME
					{
					t=(Token)match(input,SOME,FOLLOW_SOME_in_set_function_op788); 
					}
					break;
				case 8 :
					// SQL99.g:500:65: t= COUNT
					{
					t=(Token)match(input,COUNT,FOLLOW_COUNT_in_set_function_op794); 
					}
					break;

			}


			      value = (t!=null?t.getText():null);
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "set_function_op"



	// $ANTLR start "row_value_expression"
	// SQL99.g:505:1: row_value_expression returns [IValueExpression value] : ( literal | value_expression );
	public final IValueExpression row_value_expression() throws RecognitionException {
		IValueExpression value = null;


		Literal literal20 =null;
		AbstractValueExpression value_expression21 =null;

		try {
			// SQL99.g:506:3: ( literal | value_expression )
			int alt17=2;
			int LA17_0 = input.LA(1);
			if ( ((LA17_0 >= DECIMAL && LA17_0 <= DECIMAL_POSITIVE)||LA17_0==FALSE||(LA17_0 >= INTEGER && LA17_0 <= INTEGER_POSITIVE)||LA17_0==STRING_WITH_QUOTE||LA17_0==TRUE) ) {
				alt17=1;
			}
			else if ( (LA17_0==PERIOD||LA17_0==STRING_WITH_QUOTE_DOUBLE||LA17_0==VARNAME) ) {
				alt17=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 17, 0, input);
				throw nvae;
			}

			switch (alt17) {
				case 1 :
					// SQL99.g:506:5: literal
					{
					pushFollow(FOLLOW_literal_in_row_value_expression816);
					literal20=literal();
					state._fsp--;

					 value = literal20; 
					}
					break;
				case 2 :
					// SQL99.g:507:5: value_expression
					{
					pushFollow(FOLLOW_value_expression_in_row_value_expression824);
					value_expression21=value_expression();
					state._fsp--;

					 value = value_expression21; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "row_value_expression"



	// $ANTLR start "literal"
	// SQL99.g:510:1: literal returns [Literal value] : ( numeric_literal | general_literal );
	public final Literal literal() throws RecognitionException {
		Literal value = null;


		NumericLiteral numeric_literal22 =null;
		Literal general_literal23 =null;

		try {
			// SQL99.g:511:3: ( numeric_literal | general_literal )
			int alt18=2;
			int LA18_0 = input.LA(1);
			if ( ((LA18_0 >= DECIMAL && LA18_0 <= DECIMAL_POSITIVE)||(LA18_0 >= INTEGER && LA18_0 <= INTEGER_POSITIVE)) ) {
				alt18=1;
			}
			else if ( (LA18_0==FALSE||LA18_0==STRING_WITH_QUOTE||LA18_0==TRUE) ) {
				alt18=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 18, 0, input);
				throw nvae;
			}

			switch (alt18) {
				case 1 :
					// SQL99.g:511:5: numeric_literal
					{
					pushFollow(FOLLOW_numeric_literal_in_literal843);
					numeric_literal22=numeric_literal();
					state._fsp--;

					 value = numeric_literal22; 
					}
					break;
				case 2 :
					// SQL99.g:512:5: general_literal
					{
					pushFollow(FOLLOW_general_literal_in_literal851);
					general_literal23=general_literal();
					state._fsp--;

					 value = general_literal23; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "literal"



	// $ANTLR start "table_expression"
	// SQL99.g:515:1: table_expression returns [TableExpression value] : from_clause ( where_clause )? ;
	public final TableExpression table_expression() throws RecognitionException {
		TableExpression value = null;


		ArrayList<TablePrimary> from_clause24 =null;
		BooleanValueExpression where_clause25 =null;

		try {
			// SQL99.g:516:3: ( from_clause ( where_clause )? )
			// SQL99.g:516:5: from_clause ( where_clause )?
			{
			pushFollow(FOLLOW_from_clause_in_table_expression870);
			from_clause24=from_clause();
			state._fsp--;


			      value = new TableExpression(from_clause24);
			    
			// SQL99.g:519:5: ( where_clause )?
			int alt19=2;
			int LA19_0 = input.LA(1);
			if ( (LA19_0==WHERE) ) {
				alt19=1;
			}
			switch (alt19) {
				case 1 :
					// SQL99.g:519:6: where_clause
					{
					pushFollow(FOLLOW_where_clause_in_table_expression879);
					where_clause25=where_clause();
					state._fsp--;

					 value.setWhereClause(where_clause25); 
					}
					break;

			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "table_expression"



	// $ANTLR start "from_clause"
	// SQL99.g:523:1: from_clause returns [ArrayList<TablePrimary> value] : FROM table_reference_list ;
	public final ArrayList<TablePrimary> from_clause() throws RecognitionException {
		ArrayList<TablePrimary> value = null;


		ArrayList<TablePrimary> table_reference_list26 =null;

		try {
			// SQL99.g:524:3: ( FROM table_reference_list )
			// SQL99.g:524:5: FROM table_reference_list
			{
			match(input,FROM,FOLLOW_FROM_in_from_clause904); 
			pushFollow(FOLLOW_table_reference_list_in_from_clause906);
			table_reference_list26=table_reference_list();
			state._fsp--;


			      value = table_reference_list26;
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "from_clause"



	// $ANTLR start "table_reference_list"
	// SQL99.g:529:1: table_reference_list returns [ArrayList<TablePrimary> value] : a= table_reference ( COMMA b= table_reference )* ;
	public final ArrayList<TablePrimary> table_reference_list() throws RecognitionException {
		ArrayList<TablePrimary> value = null;


		TablePrimary a =null;
		TablePrimary b =null;


		  value = new ArrayList<TablePrimary>();

		try {
			// SQL99.g:533:3: (a= table_reference ( COMMA b= table_reference )* )
			// SQL99.g:533:5: a= table_reference ( COMMA b= table_reference )*
			{
			pushFollow(FOLLOW_table_reference_in_table_reference_list936);
			a=table_reference();
			state._fsp--;

			 value.add(a); 
			// SQL99.g:534:5: ( COMMA b= table_reference )*
			loop20:
			while (true) {
				int alt20=2;
				int LA20_0 = input.LA(1);
				if ( (LA20_0==COMMA) ) {
					alt20=1;
				}

				switch (alt20) {
				case 1 :
					// SQL99.g:535:7: COMMA b= table_reference
					{
					match(input,COMMA,FOLLOW_COMMA_in_table_reference_list953); 
					pushFollow(FOLLOW_table_reference_in_table_reference_list957);
					b=table_reference();
					state._fsp--;


					        JoinOperator joinOp = new JoinOperator(JoinOperator.CROSS_JOIN);
					        relationStack.push(joinOp);
					        
					        value.add(b);
					      
					}
					break;

				default :
					break loop20;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "table_reference_list"



	// $ANTLR start "table_reference"
	// SQL99.g:543:1: table_reference returns [TablePrimary value] : table_primary ( joined_table )? ;
	public final TablePrimary table_reference() throws RecognitionException {
		TablePrimary value = null;


		TablePrimary table_primary27 =null;
		TablePrimary joined_table28 =null;

		try {
			// SQL99.g:544:3: ( table_primary ( joined_table )? )
			// SQL99.g:544:5: table_primary ( joined_table )?
			{
			pushFollow(FOLLOW_table_primary_in_table_reference984);
			table_primary27=table_primary();
			state._fsp--;

			 value = table_primary27; 
			// SQL99.g:545:5: ( joined_table )?
			int alt21=2;
			int LA21_0 = input.LA(1);
			if ( (LA21_0==FULL||LA21_0==INNER||(LA21_0 >= JOIN && LA21_0 <= LEFT)||LA21_0==RIGHT) ) {
				alt21=1;
			}
			switch (alt21) {
				case 1 :
					// SQL99.g:545:6: joined_table
					{
					pushFollow(FOLLOW_joined_table_in_table_reference993);
					joined_table28=joined_table();
					state._fsp--;

					 value = joined_table28; 
					}
					break;

			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "table_reference"



	// $ANTLR start "where_clause"
	// SQL99.g:548:1: where_clause returns [BooleanValueExpression value] : WHERE search_condition ;
	public final BooleanValueExpression where_clause() throws RecognitionException {
		BooleanValueExpression value = null;


		BooleanValueExpression search_condition29 =null;

		try {
			// SQL99.g:549:3: ( WHERE search_condition )
			// SQL99.g:549:5: WHERE search_condition
			{
			match(input,WHERE,FOLLOW_WHERE_in_where_clause1015); 
			pushFollow(FOLLOW_search_condition_in_where_clause1017);
			search_condition29=search_condition();
			state._fsp--;


			      value = search_condition29;
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "where_clause"



	// $ANTLR start "search_condition"
	// SQL99.g:554:1: search_condition returns [BooleanValueExpression value] : boolean_value_expression ;
	public final BooleanValueExpression search_condition() throws RecognitionException {
		BooleanValueExpression value = null;


		BooleanValueExpression boolean_value_expression30 =null;

		try {
			// SQL99.g:555:3: ( boolean_value_expression )
			// SQL99.g:555:5: boolean_value_expression
			{
			pushFollow(FOLLOW_boolean_value_expression_in_search_condition1036);
			boolean_value_expression30=boolean_value_expression();
			state._fsp--;


			      value = boolean_value_expression30;
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "search_condition"



	// $ANTLR start "boolean_value_expression"
	// SQL99.g:560:1: boolean_value_expression returns [BooleanValueExpression value] : boolean_term ( OR boolean_term )* ;
	public final BooleanValueExpression boolean_value_expression() throws RecognitionException {
		BooleanValueExpression value = null;



		  if (booleanExp == null) {
		     booleanExp = new BooleanValueExpression();
		  }

		try {
			// SQL99.g:566:3: ( boolean_term ( OR boolean_term )* )
			// SQL99.g:570:5: boolean_term ( OR boolean_term )*
			{
			pushFollow(FOLLOW_boolean_term_in_boolean_value_expression1072);
			boolean_term();
			state._fsp--;

			// SQL99.g:570:18: ( OR boolean_term )*
			loop22:
			while (true) {
				int alt22=2;
				int LA22_0 = input.LA(1);
				if ( (LA22_0==OR) ) {
					alt22=1;
				}

				switch (alt22) {
				case 1 :
					// SQL99.g:570:19: OR boolean_term
					{
					match(input,OR,FOLLOW_OR_in_boolean_value_expression1075); 
					booleanExp.putSpecification(new OrOperator()); 
					pushFollow(FOLLOW_boolean_term_in_boolean_value_expression1079);
					boolean_term();
					state._fsp--;

					}
					break;

				default :
					break loop22;
				}
			}

			 value = booleanExp; 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "boolean_value_expression"



	// $ANTLR start "boolean_term"
	// SQL99.g:574:1: boolean_term : boolean_factor ( AND boolean_factor )* ;
	public final void boolean_term() throws RecognitionException {
		try {
			// SQL99.g:575:3: ( boolean_factor ( AND boolean_factor )* )
			// SQL99.g:575:5: boolean_factor ( AND boolean_factor )*
			{
			pushFollow(FOLLOW_boolean_factor_in_boolean_term1102);
			boolean_factor();
			state._fsp--;

			// SQL99.g:575:20: ( AND boolean_factor )*
			loop23:
			while (true) {
				int alt23=2;
				int LA23_0 = input.LA(1);
				if ( (LA23_0==AND) ) {
					alt23=1;
				}

				switch (alt23) {
				case 1 :
					// SQL99.g:575:21: AND boolean_factor
					{
					match(input,AND,FOLLOW_AND_in_boolean_term1105); 
					 booleanExp.putSpecification(new AndOperator()); 
					pushFollow(FOLLOW_boolean_factor_in_boolean_term1109);
					boolean_factor();
					state._fsp--;

					}
					break;

				default :
					break loop23;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "boolean_term"



	// $ANTLR start "boolean_factor"
	// SQL99.g:578:1: boolean_factor : ( predicate | parenthesized_boolean_value_expression );
	public final void boolean_factor() throws RecognitionException {
		IPredicate predicate31 =null;

		try {
			// SQL99.g:579:3: ( predicate | parenthesized_boolean_value_expression )
			int alt24=2;
			int LA24_0 = input.LA(1);
			if ( ((LA24_0 >= DECIMAL && LA24_0 <= DECIMAL_POSITIVE)||LA24_0==FALSE||(LA24_0 >= INTEGER && LA24_0 <= INTEGER_POSITIVE)||LA24_0==PERIOD||(LA24_0 >= STRING_WITH_QUOTE && LA24_0 <= STRING_WITH_QUOTE_DOUBLE)||LA24_0==TRUE||LA24_0==VARNAME) ) {
				alt24=1;
			}
			else if ( (LA24_0==LPAREN) ) {
				alt24=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 24, 0, input);
				throw nvae;
			}

			switch (alt24) {
				case 1 :
					// SQL99.g:579:5: predicate
					{
					pushFollow(FOLLOW_predicate_in_boolean_factor1124);
					predicate31=predicate();
					state._fsp--;

					 booleanExp.putSpecification(predicate31); 
					}
					break;
				case 2 :
					// SQL99.g:580:5: parenthesized_boolean_value_expression
					{
					pushFollow(FOLLOW_parenthesized_boolean_value_expression_in_boolean_factor1132);
					parenthesized_boolean_value_expression();
					state._fsp--;

					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "boolean_factor"



	// $ANTLR start "predicate"
	// SQL99.g:583:1: predicate returns [IPredicate value] : ( comparison_predicate | null_predicate );
	public final IPredicate predicate() throws RecognitionException {
		IPredicate value = null;


		ComparisonPredicate comparison_predicate32 =null;
		NullPredicate null_predicate33 =null;

		try {
			// SQL99.g:584:3: ( comparison_predicate | null_predicate )
			int alt25=2;
			switch ( input.LA(1) ) {
			case DECIMAL:
			case DECIMAL_NEGATIVE:
			case DECIMAL_POSITIVE:
			case FALSE:
			case INTEGER:
			case INTEGER_NEGATIVE:
			case INTEGER_POSITIVE:
			case STRING_WITH_QUOTE:
			case TRUE:
				{
				alt25=1;
				}
				break;
			case VARNAME:
				{
				switch ( input.LA(2) ) {
				case PERIOD:
					{
					int LA25_4 = input.LA(3);
					if ( (LA25_4==VARNAME) ) {
						int LA25_6 = input.LA(4);
						if ( (LA25_6==PERIOD) ) {
							int LA25_8 = input.LA(5);
							if ( (LA25_8==VARNAME) ) {
								int LA25_9 = input.LA(6);
								if ( (LA25_9==EQUALS||LA25_9==GREATER||LA25_9==LESS) ) {
									alt25=1;
								}
								else if ( (LA25_9==IS) ) {
									alt25=2;
								}

								else {
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 25, 9, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}
							else if ( (LA25_8==STRING_WITH_QUOTE_DOUBLE) ) {
								int LA25_10 = input.LA(6);
								if ( (LA25_10==EQUALS||LA25_10==GREATER||LA25_10==LESS) ) {
									alt25=1;
								}
								else if ( (LA25_10==IS) ) {
									alt25=2;
								}

								else {
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 25, 10, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}

							else {
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 25, 8, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 25, 6, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}
					else if ( (LA25_4==STRING_WITH_QUOTE_DOUBLE) ) {
						int LA25_7 = input.LA(4);
						if ( (LA25_7==PERIOD) ) {
							int LA25_8 = input.LA(5);
							if ( (LA25_8==VARNAME) ) {
								int LA25_9 = input.LA(6);
								if ( (LA25_9==EQUALS||LA25_9==GREATER||LA25_9==LESS) ) {
									alt25=1;
								}
								else if ( (LA25_9==IS) ) {
									alt25=2;
								}

								else {
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 25, 9, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}
							else if ( (LA25_8==STRING_WITH_QUOTE_DOUBLE) ) {
								int LA25_10 = input.LA(6);
								if ( (LA25_10==EQUALS||LA25_10==GREATER||LA25_10==LESS) ) {
									alt25=1;
								}
								else if ( (LA25_10==IS) ) {
									alt25=2;
								}

								else {
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 25, 10, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}

							else {
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 25, 8, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 25, 7, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 25, 4, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

					}
					break;
				case EQUALS:
				case GREATER:
				case LESS:
					{
					alt25=1;
					}
					break;
				case IS:
					{
					alt25=2;
					}
					break;
				default:
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 25, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
				}
				break;
			case STRING_WITH_QUOTE_DOUBLE:
				{
				switch ( input.LA(2) ) {
				case PERIOD:
					{
					int LA25_4 = input.LA(3);
					if ( (LA25_4==VARNAME) ) {
						int LA25_6 = input.LA(4);
						if ( (LA25_6==PERIOD) ) {
							int LA25_8 = input.LA(5);
							if ( (LA25_8==VARNAME) ) {
								int LA25_9 = input.LA(6);
								if ( (LA25_9==EQUALS||LA25_9==GREATER||LA25_9==LESS) ) {
									alt25=1;
								}
								else if ( (LA25_9==IS) ) {
									alt25=2;
								}

								else {
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 25, 9, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}
							else if ( (LA25_8==STRING_WITH_QUOTE_DOUBLE) ) {
								int LA25_10 = input.LA(6);
								if ( (LA25_10==EQUALS||LA25_10==GREATER||LA25_10==LESS) ) {
									alt25=1;
								}
								else if ( (LA25_10==IS) ) {
									alt25=2;
								}

								else {
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 25, 10, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}

							else {
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 25, 8, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 25, 6, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}
					else if ( (LA25_4==STRING_WITH_QUOTE_DOUBLE) ) {
						int LA25_7 = input.LA(4);
						if ( (LA25_7==PERIOD) ) {
							int LA25_8 = input.LA(5);
							if ( (LA25_8==VARNAME) ) {
								int LA25_9 = input.LA(6);
								if ( (LA25_9==EQUALS||LA25_9==GREATER||LA25_9==LESS) ) {
									alt25=1;
								}
								else if ( (LA25_9==IS) ) {
									alt25=2;
								}

								else {
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 25, 9, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}
							else if ( (LA25_8==STRING_WITH_QUOTE_DOUBLE) ) {
								int LA25_10 = input.LA(6);
								if ( (LA25_10==EQUALS||LA25_10==GREATER||LA25_10==LESS) ) {
									alt25=1;
								}
								else if ( (LA25_10==IS) ) {
									alt25=2;
								}

								else {
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 25, 10, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}

							else {
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 25, 8, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 25, 7, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 25, 4, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

					}
					break;
				case EQUALS:
				case GREATER:
				case LESS:
					{
					alt25=1;
					}
					break;
				case IS:
					{
					alt25=2;
					}
					break;
				default:
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 25, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
				}
				break;
			case PERIOD:
				{
				int LA25_4 = input.LA(2);
				if ( (LA25_4==VARNAME) ) {
					int LA25_6 = input.LA(3);
					if ( (LA25_6==PERIOD) ) {
						int LA25_8 = input.LA(4);
						if ( (LA25_8==VARNAME) ) {
							int LA25_9 = input.LA(5);
							if ( (LA25_9==EQUALS||LA25_9==GREATER||LA25_9==LESS) ) {
								alt25=1;
							}
							else if ( (LA25_9==IS) ) {
								alt25=2;
							}

							else {
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 25, 9, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}
						else if ( (LA25_8==STRING_WITH_QUOTE_DOUBLE) ) {
							int LA25_10 = input.LA(5);
							if ( (LA25_10==EQUALS||LA25_10==GREATER||LA25_10==LESS) ) {
								alt25=1;
							}
							else if ( (LA25_10==IS) ) {
								alt25=2;
							}

							else {
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 25, 10, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 25, 8, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 25, 6, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA25_4==STRING_WITH_QUOTE_DOUBLE) ) {
					int LA25_7 = input.LA(3);
					if ( (LA25_7==PERIOD) ) {
						int LA25_8 = input.LA(4);
						if ( (LA25_8==VARNAME) ) {
							int LA25_9 = input.LA(5);
							if ( (LA25_9==EQUALS||LA25_9==GREATER||LA25_9==LESS) ) {
								alt25=1;
							}
							else if ( (LA25_9==IS) ) {
								alt25=2;
							}

							else {
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 25, 9, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}
						else if ( (LA25_8==STRING_WITH_QUOTE_DOUBLE) ) {
							int LA25_10 = input.LA(5);
							if ( (LA25_10==EQUALS||LA25_10==GREATER||LA25_10==LESS) ) {
								alt25=1;
							}
							else if ( (LA25_10==IS) ) {
								alt25=2;
							}

							else {
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 25, 10, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 25, 8, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 25, 7, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 25, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 25, 0, input);
				throw nvae;
			}
			switch (alt25) {
				case 1 :
					// SQL99.g:584:5: comparison_predicate
					{
					pushFollow(FOLLOW_comparison_predicate_in_predicate1150);
					comparison_predicate32=comparison_predicate();
					state._fsp--;

					 value = comparison_predicate32; 
					}
					break;
				case 2 :
					// SQL99.g:585:5: null_predicate
					{
					pushFollow(FOLLOW_null_predicate_in_predicate1158);
					null_predicate33=null_predicate();
					state._fsp--;

					 value = null_predicate33; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "predicate"



	// $ANTLR start "parenthesized_boolean_value_expression"
	// SQL99.g:589:1: parenthesized_boolean_value_expression : LPAREN boolean_value_expression RPAREN ;
	public final void parenthesized_boolean_value_expression() throws RecognitionException {
		try {
			// SQL99.g:590:3: ( LPAREN boolean_value_expression RPAREN )
			// SQL99.g:590:5: LPAREN boolean_value_expression RPAREN
			{
			match(input,LPAREN,FOLLOW_LPAREN_in_parenthesized_boolean_value_expression1174); 
			 booleanExp.putSpecification(new LeftParenthesis()); 
			pushFollow(FOLLOW_boolean_value_expression_in_parenthesized_boolean_value_expression1178);
			boolean_value_expression();
			state._fsp--;

			match(input,RPAREN,FOLLOW_RPAREN_in_parenthesized_boolean_value_expression1180); 
			 booleanExp.putSpecification(new RightParenthesis()); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "parenthesized_boolean_value_expression"



	// $ANTLR start "comparison_predicate"
	// SQL99.g:593:1: comparison_predicate returns [ComparisonPredicate value] : a= row_value_expression comp_op b= row_value_expression ;
	public final ComparisonPredicate comparison_predicate() throws RecognitionException {
		ComparisonPredicate value = null;


		IValueExpression a =null;
		IValueExpression b =null;
		ComparisonPredicate.Operator comp_op34 =null;

		try {
			// SQL99.g:594:3: (a= row_value_expression comp_op b= row_value_expression )
			// SQL99.g:594:5: a= row_value_expression comp_op b= row_value_expression
			{
			pushFollow(FOLLOW_row_value_expression_in_comparison_predicate1203);
			a=row_value_expression();
			state._fsp--;

			pushFollow(FOLLOW_comp_op_in_comparison_predicate1205);
			comp_op34=comp_op();
			state._fsp--;

			pushFollow(FOLLOW_row_value_expression_in_comparison_predicate1209);
			b=row_value_expression();
			state._fsp--;


			      value = new ComparisonPredicate(a, b, comp_op34);
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "comparison_predicate"



	// $ANTLR start "comp_op"
	// SQL99.g:599:1: comp_op returns [ComparisonPredicate.Operator value] : ( EQUALS | LESS GREATER | LESS | GREATER | LESS EQUALS | GREATER EQUALS );
	public final ComparisonPredicate.Operator comp_op() throws RecognitionException {
		ComparisonPredicate.Operator value = null;


		try {
			// SQL99.g:600:3: ( EQUALS | LESS GREATER | LESS | GREATER | LESS EQUALS | GREATER EQUALS )
			int alt26=6;
			switch ( input.LA(1) ) {
			case EQUALS:
				{
				alt26=1;
				}
				break;
			case LESS:
				{
				switch ( input.LA(2) ) {
				case GREATER:
					{
					alt26=2;
					}
					break;
				case EQUALS:
					{
					alt26=5;
					}
					break;
				case DECIMAL:
				case DECIMAL_NEGATIVE:
				case DECIMAL_POSITIVE:
				case FALSE:
				case INTEGER:
				case INTEGER_NEGATIVE:
				case INTEGER_POSITIVE:
				case PERIOD:
				case STRING_WITH_QUOTE:
				case STRING_WITH_QUOTE_DOUBLE:
				case TRUE:
				case VARNAME:
					{
					alt26=3;
					}
					break;
				default:
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 26, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
				}
				break;
			case GREATER:
				{
				int LA26_3 = input.LA(2);
				if ( (LA26_3==EQUALS) ) {
					alt26=6;
				}
				else if ( ((LA26_3 >= DECIMAL && LA26_3 <= DECIMAL_POSITIVE)||LA26_3==FALSE||(LA26_3 >= INTEGER && LA26_3 <= INTEGER_POSITIVE)||LA26_3==PERIOD||(LA26_3 >= STRING_WITH_QUOTE && LA26_3 <= STRING_WITH_QUOTE_DOUBLE)||LA26_3==TRUE||LA26_3==VARNAME) ) {
					alt26=4;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 26, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 26, 0, input);
				throw nvae;
			}
			switch (alt26) {
				case 1 :
					// SQL99.g:600:5: EQUALS
					{
					match(input,EQUALS,FOLLOW_EQUALS_in_comp_op1228); 
					 value = ComparisonPredicate.Operator.EQ; 
					}
					break;
				case 2 :
					// SQL99.g:601:5: LESS GREATER
					{
					match(input,LESS,FOLLOW_LESS_in_comp_op1236); 
					match(input,GREATER,FOLLOW_GREATER_in_comp_op1238); 
					 value = ComparisonPredicate.Operator.NE; 
					}
					break;
				case 3 :
					// SQL99.g:602:5: LESS
					{
					match(input,LESS,FOLLOW_LESS_in_comp_op1246); 
					 value = ComparisonPredicate.Operator.LT; 
					}
					break;
				case 4 :
					// SQL99.g:603:5: GREATER
					{
					match(input,GREATER,FOLLOW_GREATER_in_comp_op1254); 
					 value = ComparisonPredicate.Operator.GT; 
					}
					break;
				case 5 :
					// SQL99.g:604:5: LESS EQUALS
					{
					match(input,LESS,FOLLOW_LESS_in_comp_op1262); 
					match(input,EQUALS,FOLLOW_EQUALS_in_comp_op1264); 
					 value = ComparisonPredicate.Operator.LE; 
					}
					break;
				case 6 :
					// SQL99.g:605:5: GREATER EQUALS
					{
					match(input,GREATER,FOLLOW_GREATER_in_comp_op1272); 
					match(input,EQUALS,FOLLOW_EQUALS_in_comp_op1274); 
					 value = ComparisonPredicate.Operator.GE; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "comp_op"



	// $ANTLR start "null_predicate"
	// SQL99.g:608:1: null_predicate returns [NullPredicate value] : column_reference IS ( NOT )? NULL ;
	public final NullPredicate null_predicate() throws RecognitionException {
		NullPredicate value = null;


		ColumnReference column_reference35 =null;


		  boolean useIsNull = true;

		try {
			// SQL99.g:612:3: ( column_reference IS ( NOT )? NULL )
			// SQL99.g:612:5: column_reference IS ( NOT )? NULL
			{
			pushFollow(FOLLOW_column_reference_in_null_predicate1298);
			column_reference35=column_reference();
			state._fsp--;

			match(input,IS,FOLLOW_IS_in_null_predicate1300); 
			// SQL99.g:612:25: ( NOT )?
			int alt27=2;
			int LA27_0 = input.LA(1);
			if ( (LA27_0==NOT) ) {
				alt27=1;
			}
			switch (alt27) {
				case 1 :
					// SQL99.g:612:26: NOT
					{
					match(input,NOT,FOLLOW_NOT_in_null_predicate1303); 
					 useIsNull = false; 
					}
					break;

			}

			match(input,NULL,FOLLOW_NULL_in_null_predicate1309); 

			      value = new NullPredicate(column_reference35, useIsNull);
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "null_predicate"



	// $ANTLR start "in_predicate"
	// SQL99.g:617:1: in_predicate : column_reference ( NOT )? IN in_predicate_value ;
	public final void in_predicate() throws RecognitionException {
		try {
			// SQL99.g:618:3: ( column_reference ( NOT )? IN in_predicate_value )
			// SQL99.g:618:5: column_reference ( NOT )? IN in_predicate_value
			{
			pushFollow(FOLLOW_column_reference_in_in_predicate1324);
			column_reference();
			state._fsp--;

			// SQL99.g:618:22: ( NOT )?
			int alt28=2;
			int LA28_0 = input.LA(1);
			if ( (LA28_0==NOT) ) {
				alt28=1;
			}
			switch (alt28) {
				case 1 :
					// SQL99.g:618:23: NOT
					{
					match(input,NOT,FOLLOW_NOT_in_in_predicate1327); 
					}
					break;

			}

			match(input,IN,FOLLOW_IN_in_in_predicate1331); 
			pushFollow(FOLLOW_in_predicate_value_in_in_predicate1333);
			in_predicate_value();
			state._fsp--;

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "in_predicate"



	// $ANTLR start "in_predicate_value"
	// SQL99.g:621:1: in_predicate_value : ( table_subquery | LPAREN in_value_list RPAREN );
	public final void in_predicate_value() throws RecognitionException {
		try {
			// SQL99.g:622:3: ( table_subquery | LPAREN in_value_list RPAREN )
			int alt29=2;
			int LA29_0 = input.LA(1);
			if ( (LA29_0==LPAREN) ) {
				int LA29_1 = input.LA(2);
				if ( (LA29_1==SELECT) ) {
					alt29=1;
				}
				else if ( ((LA29_1 >= DECIMAL && LA29_1 <= DECIMAL_POSITIVE)||LA29_1==FALSE||(LA29_1 >= INTEGER && LA29_1 <= INTEGER_POSITIVE)||LA29_1==PERIOD||(LA29_1 >= STRING_WITH_QUOTE && LA29_1 <= STRING_WITH_QUOTE_DOUBLE)||LA29_1==TRUE||LA29_1==VARNAME) ) {
					alt29=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 29, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 29, 0, input);
				throw nvae;
			}

			switch (alt29) {
				case 1 :
					// SQL99.g:622:5: table_subquery
					{
					pushFollow(FOLLOW_table_subquery_in_in_predicate_value1348);
					table_subquery();
					state._fsp--;

					}
					break;
				case 2 :
					// SQL99.g:623:5: LPAREN in_value_list RPAREN
					{
					match(input,LPAREN,FOLLOW_LPAREN_in_in_predicate_value1354); 
					pushFollow(FOLLOW_in_value_list_in_in_predicate_value1356);
					in_value_list();
					state._fsp--;

					match(input,RPAREN,FOLLOW_RPAREN_in_in_predicate_value1358); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "in_predicate_value"



	// $ANTLR start "table_subquery"
	// SQL99.g:626:1: table_subquery : subquery ;
	public final void table_subquery() throws RecognitionException {
		try {
			// SQL99.g:627:3: ( subquery )
			// SQL99.g:627:5: subquery
			{
			pushFollow(FOLLOW_subquery_in_table_subquery1371);
			subquery();
			state._fsp--;

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "table_subquery"



	// $ANTLR start "subquery"
	// SQL99.g:630:1: subquery : LPAREN query RPAREN ;
	public final void subquery() throws RecognitionException {
		try {
			// SQL99.g:631:3: ( LPAREN query RPAREN )
			// SQL99.g:631:5: LPAREN query RPAREN
			{
			match(input,LPAREN,FOLLOW_LPAREN_in_subquery1384); 
			pushFollow(FOLLOW_query_in_subquery1386);
			query();
			state._fsp--;

			match(input,RPAREN,FOLLOW_RPAREN_in_subquery1388); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "subquery"



	// $ANTLR start "in_value_list"
	// SQL99.g:634:1: in_value_list : row_value_expression ( COMMA row_value_expression )* ;
	public final void in_value_list() throws RecognitionException {
		try {
			// SQL99.g:635:3: ( row_value_expression ( COMMA row_value_expression )* )
			// SQL99.g:635:5: row_value_expression ( COMMA row_value_expression )*
			{
			pushFollow(FOLLOW_row_value_expression_in_in_value_list1403);
			row_value_expression();
			state._fsp--;

			// SQL99.g:635:26: ( COMMA row_value_expression )*
			loop30:
			while (true) {
				int alt30=2;
				int LA30_0 = input.LA(1);
				if ( (LA30_0==COMMA) ) {
					alt30=1;
				}

				switch (alt30) {
				case 1 :
					// SQL99.g:635:27: COMMA row_value_expression
					{
					match(input,COMMA,FOLLOW_COMMA_in_in_value_list1406); 
					pushFollow(FOLLOW_row_value_expression_in_in_value_list1408);
					row_value_expression();
					state._fsp--;

					}
					break;

				default :
					break loop30;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "in_value_list"



	// $ANTLR start "group_by_clause"
	// SQL99.g:638:1: group_by_clause returns [ArrayList<GroupingElement> value] : GROUP BY grouping_element_list ;
	public final ArrayList<GroupingElement> group_by_clause() throws RecognitionException {
		ArrayList<GroupingElement> value = null;


		ArrayList<GroupingElement> grouping_element_list36 =null;

		try {
			// SQL99.g:639:3: ( GROUP BY grouping_element_list )
			// SQL99.g:639:5: GROUP BY grouping_element_list
			{
			match(input,GROUP,FOLLOW_GROUP_in_group_by_clause1427); 
			match(input,BY,FOLLOW_BY_in_group_by_clause1429); 
			pushFollow(FOLLOW_grouping_element_list_in_group_by_clause1431);
			grouping_element_list36=grouping_element_list();
			state._fsp--;


			      value = grouping_element_list36;
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "group_by_clause"



	// $ANTLR start "grouping_element_list"
	// SQL99.g:644:1: grouping_element_list returns [ArrayList<GroupingElement> value] : a= grouping_element ( COMMA b= grouping_element )* ;
	public final ArrayList<GroupingElement> grouping_element_list() throws RecognitionException {
		ArrayList<GroupingElement> value = null;


		GroupingElement a =null;
		GroupingElement b =null;


		  value = new ArrayList<GroupingElement>();

		try {
			// SQL99.g:648:3: (a= grouping_element ( COMMA b= grouping_element )* )
			// SQL99.g:648:5: a= grouping_element ( COMMA b= grouping_element )*
			{
			pushFollow(FOLLOW_grouping_element_in_grouping_element_list1457);
			a=grouping_element();
			state._fsp--;

			 value.add(a); 
			// SQL99.g:649:5: ( COMMA b= grouping_element )*
			loop31:
			while (true) {
				int alt31=2;
				int LA31_0 = input.LA(1);
				if ( (LA31_0==COMMA) ) {
					alt31=1;
				}

				switch (alt31) {
				case 1 :
					// SQL99.g:649:6: COMMA b= grouping_element
					{
					match(input,COMMA,FOLLOW_COMMA_in_grouping_element_list1467); 
					pushFollow(FOLLOW_grouping_element_in_grouping_element_list1471);
					b=grouping_element();
					state._fsp--;

					 value.add(b); 
					}
					break;

				default :
					break loop31;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "grouping_element_list"



	// $ANTLR start "grouping_element"
	// SQL99.g:652:1: grouping_element returns [GroupingElement value] : ( grouping_column_reference | LPAREN grouping_column_reference_list RPAREN );
	public final GroupingElement grouping_element() throws RecognitionException {
		GroupingElement value = null;


		ColumnReference grouping_column_reference37 =null;
		ArrayList<ColumnReference> grouping_column_reference_list38 =null;


		  value = new GroupingElement();

		try {
			// SQL99.g:656:3: ( grouping_column_reference | LPAREN grouping_column_reference_list RPAREN )
			int alt32=2;
			int LA32_0 = input.LA(1);
			if ( (LA32_0==PERIOD||LA32_0==STRING_WITH_QUOTE_DOUBLE||LA32_0==VARNAME) ) {
				alt32=1;
			}
			else if ( (LA32_0==LPAREN) ) {
				alt32=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 32, 0, input);
				throw nvae;
			}

			switch (alt32) {
				case 1 :
					// SQL99.g:656:5: grouping_column_reference
					{
					pushFollow(FOLLOW_grouping_column_reference_in_grouping_element1499);
					grouping_column_reference37=grouping_column_reference();
					state._fsp--;

					 value.add(grouping_column_reference37); 
					}
					break;
				case 2 :
					// SQL99.g:657:5: LPAREN grouping_column_reference_list RPAREN
					{
					match(input,LPAREN,FOLLOW_LPAREN_in_grouping_element1507); 
					pushFollow(FOLLOW_grouping_column_reference_list_in_grouping_element1509);
					grouping_column_reference_list38=grouping_column_reference_list();
					state._fsp--;

					match(input,RPAREN,FOLLOW_RPAREN_in_grouping_element1511); 
					 value.update(grouping_column_reference_list38); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "grouping_element"



	// $ANTLR start "grouping_column_reference"
	// SQL99.g:660:1: grouping_column_reference returns [ColumnReference value] : column_reference ;
	public final ColumnReference grouping_column_reference() throws RecognitionException {
		ColumnReference value = null;


		ColumnReference column_reference39 =null;

		try {
			// SQL99.g:661:3: ( column_reference )
			// SQL99.g:661:5: column_reference
			{
			pushFollow(FOLLOW_column_reference_in_grouping_column_reference1532);
			column_reference39=column_reference();
			state._fsp--;

			 value = column_reference39; 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "grouping_column_reference"



	// $ANTLR start "grouping_column_reference_list"
	// SQL99.g:664:1: grouping_column_reference_list returns [ArrayList<ColumnReference> value] : a= column_reference ( COMMA b= column_reference )* ;
	public final ArrayList<ColumnReference> grouping_column_reference_list() throws RecognitionException {
		ArrayList<ColumnReference> value = null;


		ColumnReference a =null;
		ColumnReference b =null;


		  value = new ArrayList<ColumnReference>();

		try {
			// SQL99.g:668:3: (a= column_reference ( COMMA b= column_reference )* )
			// SQL99.g:668:5: a= column_reference ( COMMA b= column_reference )*
			{
			pushFollow(FOLLOW_column_reference_in_grouping_column_reference_list1560);
			a=column_reference();
			state._fsp--;

			 value.add(a); 
			// SQL99.g:669:5: ( COMMA b= column_reference )*
			loop33:
			while (true) {
				int alt33=2;
				int LA33_0 = input.LA(1);
				if ( (LA33_0==COMMA) ) {
					alt33=1;
				}

				switch (alt33) {
				case 1 :
					// SQL99.g:669:6: COMMA b= column_reference
					{
					match(input,COMMA,FOLLOW_COMMA_in_grouping_column_reference_list1569); 
					pushFollow(FOLLOW_column_reference_in_grouping_column_reference_list1573);
					b=column_reference();
					state._fsp--;

					 value.add(b); 
					}
					break;

				default :
					break loop33;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "grouping_column_reference_list"



	// $ANTLR start "joined_table"
	// SQL99.g:672:1: joined_table returns [TablePrimary value] : ( ( join_type )? JOIN table_reference join_specification ) ;
	public final TablePrimary joined_table() throws RecognitionException {
		TablePrimary value = null;


		int join_type40 =0;
		BooleanValueExpression join_specification41 =null;
		TablePrimary table_reference42 =null;


		  int joinType = JoinOperator.JOIN; // by default

		try {
			// SQL99.g:676:3: ( ( ( join_type )? JOIN table_reference join_specification ) )
			// SQL99.g:676:5: ( ( join_type )? JOIN table_reference join_specification )
			{
			// SQL99.g:676:5: ( ( join_type )? JOIN table_reference join_specification )
			// SQL99.g:676:6: ( join_type )? JOIN table_reference join_specification
			{
			// SQL99.g:676:6: ( join_type )?
			int alt34=2;
			int LA34_0 = input.LA(1);
			if ( (LA34_0==FULL||LA34_0==INNER||LA34_0==LEFT||LA34_0==RIGHT) ) {
				alt34=1;
			}
			switch (alt34) {
				case 1 :
					// SQL99.g:676:7: join_type
					{
					pushFollow(FOLLOW_join_type_in_joined_table1603);
					join_type40=join_type();
					state._fsp--;

					 joinType = join_type40; 
					}
					break;

			}

			match(input,JOIN,FOLLOW_JOIN_in_joined_table1609); 
			pushFollow(FOLLOW_table_reference_in_joined_table1611);
			table_reference42=table_reference();
			state._fsp--;

			pushFollow(FOLLOW_join_specification_in_joined_table1613);
			join_specification41=join_specification();
			state._fsp--;


			      JoinOperator joinOp = new JoinOperator(joinType);
			      if (join_specification41 != null) {
			          joinOp.copy(join_specification41.getSpecification());
			          relationStack.push(joinOp);
			          value = table_reference42;
			      }
			    
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "joined_table"



	// $ANTLR start "join_type"
	// SQL99.g:686:1: join_type returns [int value] : ( INNER | outer_join_type ( OUTER )? );
	public final int join_type() throws RecognitionException {
		int value = 0;


		int outer_join_type43 =0;


		  boolean bHasOuter = false;

		try {
			// SQL99.g:690:3: ( INNER | outer_join_type ( OUTER )? )
			int alt36=2;
			int LA36_0 = input.LA(1);
			if ( (LA36_0==INNER) ) {
				alt36=1;
			}
			else if ( (LA36_0==FULL||LA36_0==LEFT||LA36_0==RIGHT) ) {
				alt36=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 36, 0, input);
				throw nvae;
			}

			switch (alt36) {
				case 1 :
					// SQL99.g:690:5: INNER
					{
					match(input,INNER,FOLLOW_INNER_in_join_type1638); 
					 value = JoinOperator.INNER_JOIN; 
					}
					break;
				case 2 :
					// SQL99.g:691:5: outer_join_type ( OUTER )?
					{
					pushFollow(FOLLOW_outer_join_type_in_join_type1646);
					outer_join_type43=outer_join_type();
					state._fsp--;

					// SQL99.g:691:21: ( OUTER )?
					int alt35=2;
					int LA35_0 = input.LA(1);
					if ( (LA35_0==OUTER) ) {
						alt35=1;
					}
					switch (alt35) {
						case 1 :
							// SQL99.g:691:22: OUTER
							{
							match(input,OUTER,FOLLOW_OUTER_in_join_type1649); 
							 bHasOuter = true; 
							}
							break;

					}


					      if (bHasOuter) {
					        switch(outer_join_type43) {
					          case JoinOperator.LEFT_JOIN: value = JoinOperator.LEFT_OUTER_JOIN; break;
					          case JoinOperator.RIGHT_JOIN: value = JoinOperator.RIGHT_OUTER_JOIN; break;
					          case JoinOperator.FULL_JOIN: value = JoinOperator.FULL_OUTER_JOIN; break;
					        }
					      }
					      else {
					        value = outer_join_type43;
					      }
					    
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "join_type"



	// $ANTLR start "outer_join_type"
	// SQL99.g:705:1: outer_join_type returns [int value] : ( LEFT | RIGHT | FULL );
	public final int outer_join_type() throws RecognitionException {
		int value = 0;


		try {
			// SQL99.g:706:3: ( LEFT | RIGHT | FULL )
			int alt37=3;
			switch ( input.LA(1) ) {
			case LEFT:
				{
				alt37=1;
				}
				break;
			case RIGHT:
				{
				alt37=2;
				}
				break;
			case FULL:
				{
				alt37=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 37, 0, input);
				throw nvae;
			}
			switch (alt37) {
				case 1 :
					// SQL99.g:706:5: LEFT
					{
					match(input,LEFT,FOLLOW_LEFT_in_outer_join_type1674); 
					 value = JoinOperator.LEFT_JOIN; 
					}
					break;
				case 2 :
					// SQL99.g:707:5: RIGHT
					{
					match(input,RIGHT,FOLLOW_RIGHT_in_outer_join_type1682); 
					 value = JoinOperator.RIGHT_JOIN; 
					}
					break;
				case 3 :
					// SQL99.g:708:5: FULL
					{
					match(input,FULL,FOLLOW_FULL_in_outer_join_type1690); 
					 value = JoinOperator.FULL_JOIN; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "outer_join_type"



	// $ANTLR start "join_specification"
	// SQL99.g:711:1: join_specification returns [BooleanValueExpression value] : join_condition ;
	public final BooleanValueExpression join_specification() throws RecognitionException {
		BooleanValueExpression value = null;


		BooleanValueExpression join_condition44 =null;

		try {
			// SQL99.g:712:3: ( join_condition )
			// SQL99.g:712:5: join_condition
			{
			pushFollow(FOLLOW_join_condition_in_join_specification1709);
			join_condition44=join_condition();
			state._fsp--;

			 value = join_condition44; 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "join_specification"



	// $ANTLR start "join_condition"
	// SQL99.g:716:1: join_condition returns [BooleanValueExpression value] : ON search_condition ;
	public final BooleanValueExpression join_condition() throws RecognitionException {
		BooleanValueExpression value = null;


		BooleanValueExpression search_condition45 =null;

		try {
			// SQL99.g:717:3: ( ON search_condition )
			// SQL99.g:717:5: ON search_condition
			{
			match(input,ON,FOLLOW_ON_in_join_condition1729); 
			pushFollow(FOLLOW_search_condition_in_join_condition1731);
			search_condition45=search_condition();
			state._fsp--;


			      value = search_condition45;
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "join_condition"



	// $ANTLR start "named_columns_join"
	// SQL99.g:722:1: named_columns_join : USING LPAREN join_column_list RPAREN ;
	public final void named_columns_join() throws RecognitionException {
		try {
			// SQL99.g:723:3: ( USING LPAREN join_column_list RPAREN )
			// SQL99.g:723:5: USING LPAREN join_column_list RPAREN
			{
			match(input,USING,FOLLOW_USING_in_named_columns_join1746); 
			match(input,LPAREN,FOLLOW_LPAREN_in_named_columns_join1748); 
			pushFollow(FOLLOW_join_column_list_in_named_columns_join1750);
			join_column_list();
			state._fsp--;

			match(input,RPAREN,FOLLOW_RPAREN_in_named_columns_join1752); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "named_columns_join"



	// $ANTLR start "join_column_list"
	// SQL99.g:726:1: join_column_list : column_name ( COMMA column_name )* ;
	public final void join_column_list() throws RecognitionException {
		try {
			// SQL99.g:727:3: ( column_name ( COMMA column_name )* )
			// SQL99.g:727:5: column_name ( COMMA column_name )*
			{
			pushFollow(FOLLOW_column_name_in_join_column_list1765);
			column_name();
			state._fsp--;

			// SQL99.g:727:17: ( COMMA column_name )*
			loop38:
			while (true) {
				int alt38=2;
				int LA38_0 = input.LA(1);
				if ( (LA38_0==COMMA) ) {
					alt38=1;
				}

				switch (alt38) {
				case 1 :
					// SQL99.g:727:18: COMMA column_name
					{
					match(input,COMMA,FOLLOW_COMMA_in_join_column_list1768); 
					pushFollow(FOLLOW_column_name_in_join_column_list1770);
					column_name();
					state._fsp--;

					}
					break;

				default :
					break loop38;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "join_column_list"



	// $ANTLR start "table_primary"
	// SQL99.g:731:1: table_primary returns [TablePrimary value] : table_name ( ( AS )? alias_name )? ;
	public final TablePrimary table_primary() throws RecognitionException {
		TablePrimary value = null;


		TablePrimary table_name46 =null;
		String alias_name47 =null;

		try {
			// SQL99.g:732:3: ( table_name ( ( AS )? alias_name )? )
			// SQL99.g:732:5: table_name ( ( AS )? alias_name )?
			{
			pushFollow(FOLLOW_table_name_in_table_primary1790);
			table_name46=table_name();
			state._fsp--;

			// SQL99.g:733:5: ( ( AS )? alias_name )?
			int alt40=2;
			int LA40_0 = input.LA(1);
			if ( (LA40_0==AS||LA40_0==STRING_WITH_QUOTE_DOUBLE||LA40_0==VARNAME) ) {
				alt40=1;
			}
			switch (alt40) {
				case 1 :
					// SQL99.g:733:6: ( AS )? alias_name
					{
					// SQL99.g:733:6: ( AS )?
					int alt39=2;
					int LA39_0 = input.LA(1);
					if ( (LA39_0==AS) ) {
						alt39=1;
					}
					switch (alt39) {
						case 1 :
							// SQL99.g:733:6: AS
							{
							match(input,AS,FOLLOW_AS_in_table_primary1797); 
							}
							break;

					}

					pushFollow(FOLLOW_alias_name_in_table_primary1800);
					alias_name47=alias_name();
					state._fsp--;

					}
					break;

			}


			      value = table_name46; 
			      value.setAlias(alias_name47);
			      Relation table = new Relation(value);      
			      relationStack.push(table);
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "table_primary"



	// $ANTLR start "table_name"
	// SQL99.g:745:1: table_name returns [TablePrimary value] : ( schema_name PERIOD )? table_identifier ;
	public final TablePrimary table_name() throws RecognitionException {
		TablePrimary value = null;


		ArrayList<String> schema_name48 =null;
		ArrayList<String> table_identifier49 =null;

		try {
			// SQL99.g:746:3: ( ( schema_name PERIOD )? table_identifier )
			// SQL99.g:746:5: ( schema_name PERIOD )? table_identifier
			{
			// SQL99.g:746:5: ( schema_name PERIOD )?
			int alt41=2;
			int LA41_0 = input.LA(1);
			if ( (LA41_0==VARNAME) ) {
				int LA41_1 = input.LA(2);
				if ( (LA41_1==PERIOD) ) {
					alt41=1;
				}
			}
			else if ( (LA41_0==STRING_WITH_QUOTE_DOUBLE) ) {
				int LA41_2 = input.LA(2);
				if ( (LA41_2==PERIOD) ) {
					alt41=1;
				}
			}
			switch (alt41) {
				case 1 :
					// SQL99.g:746:6: schema_name PERIOD
					{
					pushFollow(FOLLOW_schema_name_in_table_name1828);
					schema_name48=schema_name();
					state._fsp--;

					match(input,PERIOD,FOLLOW_PERIOD_in_table_name1830); 
					}
					break;

			}

			pushFollow(FOLLOW_table_identifier_in_table_name1834);
			table_identifier49=table_identifier();
			state._fsp--;


			      String schema = schema_name48.get(1);      
			      if (schema != null && schema != "") {
			        value = new TablePrimary(schema_name48.get(1), table_identifier49.get(1), schema_name48.get(0) + "." + table_identifier49.get(0));
			      }
			      else {
			        value = new TablePrimary(table_identifier49.get(1), table_identifier49.get(0));
			      }      
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "table_name"



	// $ANTLR start "alias_name"
	// SQL99.g:757:1: alias_name returns [String value] : identifier ;
	public final String alias_name() throws RecognitionException {
		String value = null;


		ArrayList<String> identifier50 =null;

		try {
			// SQL99.g:758:3: ( identifier )
			// SQL99.g:758:5: identifier
			{
			pushFollow(FOLLOW_identifier_in_alias_name1855);
			identifier50=identifier();
			state._fsp--;

			 value = identifier50.get(1); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "alias_name"



	// $ANTLR start "derived_table"
	// SQL99.g:761:1: derived_table : table_subquery ;
	public final void derived_table() throws RecognitionException {
		try {
			// SQL99.g:762:3: ( table_subquery )
			// SQL99.g:762:5: table_subquery
			{
			pushFollow(FOLLOW_table_subquery_in_derived_table1871);
			table_subquery();
			state._fsp--;

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "derived_table"



	// $ANTLR start "table_identifier"
	// SQL99.g:765:1: table_identifier returns [ArrayList<String> value] : identifier ;
	public final ArrayList<String> table_identifier() throws RecognitionException {
		ArrayList<String> value = null;


		ArrayList<String> identifier51 =null;

		try {
			// SQL99.g:766:3: ( identifier )
			// SQL99.g:766:5: identifier
			{
			pushFollow(FOLLOW_identifier_in_table_identifier1892);
			identifier51=identifier();
			state._fsp--;

			 value = identifier51; 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "table_identifier"



	// $ANTLR start "schema_name"
	// SQL99.g:769:1: schema_name returns [ArrayList<String> value] : identifier ;
	public final ArrayList<String> schema_name() throws RecognitionException {
		ArrayList<String> value = null;


		ArrayList<String> identifier52 =null;

		try {
			// SQL99.g:770:3: ( identifier )
			// SQL99.g:770:5: identifier
			{
			pushFollow(FOLLOW_identifier_in_schema_name1913);
			identifier52=identifier();
			state._fsp--;

			 value = identifier52; 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "schema_name"



	// $ANTLR start "column_name"
	// SQL99.g:773:1: column_name returns [String value] : identifier ;
	public final String column_name() throws RecognitionException {
		String value = null;


		ArrayList<String> identifier53 =null;

		try {
			// SQL99.g:774:3: ( identifier )
			// SQL99.g:774:5: identifier
			{
			pushFollow(FOLLOW_identifier_in_column_name1936);
			identifier53=identifier();
			state._fsp--;

			 value = identifier53.get(1); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "column_name"



	// $ANTLR start "identifier"
	// SQL99.g:777:1: identifier returns [ArrayList<String> value] : (t= regular_identifier |t= delimited_identifier ) ;
	public final ArrayList<String> identifier() throws RecognitionException {
		ArrayList<String> value = null;


		ArrayList<String> t =null;

		try {
			// SQL99.g:778:3: ( (t= regular_identifier |t= delimited_identifier ) )
			// SQL99.g:778:5: (t= regular_identifier |t= delimited_identifier )
			{
			// SQL99.g:778:5: (t= regular_identifier |t= delimited_identifier )
			int alt42=2;
			int LA42_0 = input.LA(1);
			if ( (LA42_0==VARNAME) ) {
				alt42=1;
			}
			else if ( (LA42_0==STRING_WITH_QUOTE_DOUBLE) ) {
				alt42=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 42, 0, input);
				throw nvae;
			}

			switch (alt42) {
				case 1 :
					// SQL99.g:778:6: t= regular_identifier
					{
					pushFollow(FOLLOW_regular_identifier_in_identifier1960);
					t=regular_identifier();
					state._fsp--;

					}
					break;
				case 2 :
					// SQL99.g:778:29: t= delimited_identifier
					{
					pushFollow(FOLLOW_delimited_identifier_in_identifier1966);
					t=delimited_identifier();
					state._fsp--;

					}
					break;

			}

			 value = t; 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "identifier"



	// $ANTLR start "regular_identifier"
	// SQL99.g:781:1: regular_identifier returns [ArrayList<String> value] : VARNAME ;
	public final ArrayList<String> regular_identifier() throws RecognitionException {
		ArrayList<String> value = null;


		Token VARNAME54=null;

		try {
			// SQL99.g:782:3: ( VARNAME )
			// SQL99.g:782:5: VARNAME
			{
			VARNAME54=(Token)match(input,VARNAME,FOLLOW_VARNAME_in_regular_identifier1986); 
			 value = new ArrayList<String>();
			  	value.add((VARNAME54!=null?VARNAME54.getText():null));
			  	value.add((VARNAME54!=null?VARNAME54.getText():null));
			  	 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "regular_identifier"



	// $ANTLR start "delimited_identifier"
	// SQL99.g:788:1: delimited_identifier returns [ArrayList<String> value] : STRING_WITH_QUOTE_DOUBLE ;
	public final ArrayList<String> delimited_identifier() throws RecognitionException {
		ArrayList<String> value = null;


		Token STRING_WITH_QUOTE_DOUBLE55=null;

		try {
			// SQL99.g:789:3: ( STRING_WITH_QUOTE_DOUBLE )
			// SQL99.g:789:5: STRING_WITH_QUOTE_DOUBLE
			{
			STRING_WITH_QUOTE_DOUBLE55=(Token)match(input,STRING_WITH_QUOTE_DOUBLE,FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_delimited_identifier2005); 
			 
				value = new ArrayList<String>();  
			    value.add((STRING_WITH_QUOTE_DOUBLE55!=null?STRING_WITH_QUOTE_DOUBLE55.getText():null));
			 	value.add((STRING_WITH_QUOTE_DOUBLE55!=null?STRING_WITH_QUOTE_DOUBLE55.getText():null).substring(1, (STRING_WITH_QUOTE_DOUBLE55!=null?STRING_WITH_QUOTE_DOUBLE55.getText():null).length()-1));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "delimited_identifier"



	// $ANTLR start "general_literal"
	// SQL99.g:796:1: general_literal returns [Literal value] : ( string_literal | boolean_literal );
	public final Literal general_literal() throws RecognitionException {
		Literal value = null;


		StringLiteral string_literal56 =null;
		BooleanLiteral boolean_literal57 =null;

		try {
			// SQL99.g:797:3: ( string_literal | boolean_literal )
			int alt43=2;
			int LA43_0 = input.LA(1);
			if ( (LA43_0==STRING_WITH_QUOTE) ) {
				alt43=1;
			}
			else if ( (LA43_0==FALSE||LA43_0==TRUE) ) {
				alt43=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 43, 0, input);
				throw nvae;
			}

			switch (alt43) {
				case 1 :
					// SQL99.g:797:5: string_literal
					{
					pushFollow(FOLLOW_string_literal_in_general_literal2024);
					string_literal56=string_literal();
					state._fsp--;

					 value = string_literal56; 
					}
					break;
				case 2 :
					// SQL99.g:798:5: boolean_literal
					{
					pushFollow(FOLLOW_boolean_literal_in_general_literal2032);
					boolean_literal57=boolean_literal();
					state._fsp--;

					 value = boolean_literal57; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "general_literal"



	// $ANTLR start "string_literal"
	// SQL99.g:801:1: string_literal returns [StringLiteral value] : STRING_WITH_QUOTE ;
	public final StringLiteral string_literal() throws RecognitionException {
		StringLiteral value = null;


		Token STRING_WITH_QUOTE58=null;

		try {
			// SQL99.g:802:3: ( STRING_WITH_QUOTE )
			// SQL99.g:802:5: STRING_WITH_QUOTE
			{
			STRING_WITH_QUOTE58=(Token)match(input,STRING_WITH_QUOTE,FOLLOW_STRING_WITH_QUOTE_in_string_literal2051); 

			      String str = (STRING_WITH_QUOTE58!=null?STRING_WITH_QUOTE58.getText():null);
			      str = str.substring(1, str.length()-1);
			      value = new StringLiteral(str);
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "string_literal"



	// $ANTLR start "boolean_literal"
	// SQL99.g:809:1: boolean_literal returns [BooleanLiteral value] : (t= TRUE |t= FALSE ) ;
	public final BooleanLiteral boolean_literal() throws RecognitionException {
		BooleanLiteral value = null;


		Token t=null;

		try {
			// SQL99.g:810:3: ( (t= TRUE |t= FALSE ) )
			// SQL99.g:810:5: (t= TRUE |t= FALSE )
			{
			// SQL99.g:810:5: (t= TRUE |t= FALSE )
			int alt44=2;
			int LA44_0 = input.LA(1);
			if ( (LA44_0==TRUE) ) {
				alt44=1;
			}
			else if ( (LA44_0==FALSE) ) {
				alt44=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 44, 0, input);
				throw nvae;
			}

			switch (alt44) {
				case 1 :
					// SQL99.g:810:6: t= TRUE
					{
					t=(Token)match(input,TRUE,FOLLOW_TRUE_in_boolean_literal2073); 
					}
					break;
				case 2 :
					// SQL99.g:810:15: t= FALSE
					{
					t=(Token)match(input,FALSE,FOLLOW_FALSE_in_boolean_literal2079); 
					}
					break;

			}

			 value = new BooleanLiteral(Boolean.parseBoolean((t!=null?t.getText():null))); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "boolean_literal"



	// $ANTLR start "numeric_literal"
	// SQL99.g:813:1: numeric_literal returns [NumericLiteral value] : ( numeric_literal_unsigned | numeric_literal_positive | numeric_literal_negative );
	public final NumericLiteral numeric_literal() throws RecognitionException {
		NumericLiteral value = null;


		NumericLiteral numeric_literal_unsigned59 =null;
		NumericLiteral numeric_literal_positive60 =null;
		NumericLiteral numeric_literal_negative61 =null;

		try {
			// SQL99.g:814:3: ( numeric_literal_unsigned | numeric_literal_positive | numeric_literal_negative )
			int alt45=3;
			switch ( input.LA(1) ) {
			case DECIMAL:
			case INTEGER:
				{
				alt45=1;
				}
				break;
			case DECIMAL_POSITIVE:
			case INTEGER_POSITIVE:
				{
				alt45=2;
				}
				break;
			case DECIMAL_NEGATIVE:
			case INTEGER_NEGATIVE:
				{
				alt45=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 45, 0, input);
				throw nvae;
			}
			switch (alt45) {
				case 1 :
					// SQL99.g:814:5: numeric_literal_unsigned
					{
					pushFollow(FOLLOW_numeric_literal_unsigned_in_numeric_literal2099);
					numeric_literal_unsigned59=numeric_literal_unsigned();
					state._fsp--;

					 value = numeric_literal_unsigned59; 
					}
					break;
				case 2 :
					// SQL99.g:815:5: numeric_literal_positive
					{
					pushFollow(FOLLOW_numeric_literal_positive_in_numeric_literal2107);
					numeric_literal_positive60=numeric_literal_positive();
					state._fsp--;

					 value = numeric_literal_positive60; 
					}
					break;
				case 3 :
					// SQL99.g:816:5: numeric_literal_negative
					{
					pushFollow(FOLLOW_numeric_literal_negative_in_numeric_literal2115);
					numeric_literal_negative61=numeric_literal_negative();
					state._fsp--;

					 value = numeric_literal_negative61; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "numeric_literal"



	// $ANTLR start "numeric_literal_unsigned"
	// SQL99.g:819:1: numeric_literal_unsigned returns [NumericLiteral value] : ( INTEGER | DECIMAL );
	public final NumericLiteral numeric_literal_unsigned() throws RecognitionException {
		NumericLiteral value = null;


		Token INTEGER62=null;
		Token DECIMAL63=null;

		try {
			// SQL99.g:820:3: ( INTEGER | DECIMAL )
			int alt46=2;
			int LA46_0 = input.LA(1);
			if ( (LA46_0==INTEGER) ) {
				alt46=1;
			}
			else if ( (LA46_0==DECIMAL) ) {
				alt46=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 46, 0, input);
				throw nvae;
			}

			switch (alt46) {
				case 1 :
					// SQL99.g:820:5: INTEGER
					{
					INTEGER62=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_numeric_literal_unsigned2134); 
					 value = new IntegerLiteral((INTEGER62!=null?INTEGER62.getText():null)); 
					}
					break;
				case 2 :
					// SQL99.g:821:5: DECIMAL
					{
					DECIMAL63=(Token)match(input,DECIMAL,FOLLOW_DECIMAL_in_numeric_literal_unsigned2142); 
					 value = new DecimalLiteral((DECIMAL63!=null?DECIMAL63.getText():null)); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "numeric_literal_unsigned"



	// $ANTLR start "numeric_literal_positive"
	// SQL99.g:824:1: numeric_literal_positive returns [NumericLiteral value] : ( INTEGER_POSITIVE | DECIMAL_POSITIVE );
	public final NumericLiteral numeric_literal_positive() throws RecognitionException {
		NumericLiteral value = null;


		Token INTEGER_POSITIVE64=null;
		Token DECIMAL_POSITIVE65=null;

		try {
			// SQL99.g:825:3: ( INTEGER_POSITIVE | DECIMAL_POSITIVE )
			int alt47=2;
			int LA47_0 = input.LA(1);
			if ( (LA47_0==INTEGER_POSITIVE) ) {
				alt47=1;
			}
			else if ( (LA47_0==DECIMAL_POSITIVE) ) {
				alt47=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 47, 0, input);
				throw nvae;
			}

			switch (alt47) {
				case 1 :
					// SQL99.g:825:5: INTEGER_POSITIVE
					{
					INTEGER_POSITIVE64=(Token)match(input,INTEGER_POSITIVE,FOLLOW_INTEGER_POSITIVE_in_numeric_literal_positive2161); 
					 value = new IntegerLiteral((INTEGER_POSITIVE64!=null?INTEGER_POSITIVE64.getText():null)); 
					}
					break;
				case 2 :
					// SQL99.g:826:5: DECIMAL_POSITIVE
					{
					DECIMAL_POSITIVE65=(Token)match(input,DECIMAL_POSITIVE,FOLLOW_DECIMAL_POSITIVE_in_numeric_literal_positive2169); 
					 value = new DecimalLiteral((DECIMAL_POSITIVE65!=null?DECIMAL_POSITIVE65.getText():null)); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "numeric_literal_positive"



	// $ANTLR start "numeric_literal_negative"
	// SQL99.g:829:1: numeric_literal_negative returns [NumericLiteral value] : ( INTEGER_NEGATIVE | DECIMAL_NEGATIVE );
	public final NumericLiteral numeric_literal_negative() throws RecognitionException {
		NumericLiteral value = null;


		Token INTEGER_NEGATIVE66=null;
		Token DECIMAL_NEGATIVE67=null;

		try {
			// SQL99.g:830:3: ( INTEGER_NEGATIVE | DECIMAL_NEGATIVE )
			int alt48=2;
			int LA48_0 = input.LA(1);
			if ( (LA48_0==INTEGER_NEGATIVE) ) {
				alt48=1;
			}
			else if ( (LA48_0==DECIMAL_NEGATIVE) ) {
				alt48=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 48, 0, input);
				throw nvae;
			}

			switch (alt48) {
				case 1 :
					// SQL99.g:830:5: INTEGER_NEGATIVE
					{
					INTEGER_NEGATIVE66=(Token)match(input,INTEGER_NEGATIVE,FOLLOW_INTEGER_NEGATIVE_in_numeric_literal_negative2190); 
					 value = new IntegerLiteral((INTEGER_NEGATIVE66!=null?INTEGER_NEGATIVE66.getText():null)); 
					}
					break;
				case 2 :
					// SQL99.g:831:5: DECIMAL_NEGATIVE
					{
					DECIMAL_NEGATIVE67=(Token)match(input,DECIMAL_NEGATIVE,FOLLOW_DECIMAL_NEGATIVE_in_numeric_literal_negative2198); 
					 value = new DecimalLiteral((DECIMAL_NEGATIVE67!=null?DECIMAL_NEGATIVE67.getText():null)); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "numeric_literal_negative"



	// $ANTLR start "truth_value"
	// SQL99.g:834:1: truth_value returns [boolean value] : (t= TRUE |t= FALSE ) ;
	public final boolean truth_value() throws RecognitionException {
		boolean value = false;


		Token t=null;

		try {
			// SQL99.g:835:3: ( (t= TRUE |t= FALSE ) )
			// SQL99.g:835:5: (t= TRUE |t= FALSE )
			{
			// SQL99.g:835:5: (t= TRUE |t= FALSE )
			int alt49=2;
			int LA49_0 = input.LA(1);
			if ( (LA49_0==TRUE) ) {
				alt49=1;
			}
			else if ( (LA49_0==FALSE) ) {
				alt49=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 49, 0, input);
				throw nvae;
			}

			switch (alt49) {
				case 1 :
					// SQL99.g:835:6: t= TRUE
					{
					t=(Token)match(input,TRUE,FOLLOW_TRUE_in_truth_value2222); 
					}
					break;
				case 2 :
					// SQL99.g:835:15: t= FALSE
					{
					t=(Token)match(input,FALSE,FOLLOW_FALSE_in_truth_value2228); 
					}
					break;

			}

			 value = Boolean.getBoolean((t!=null?t.getText():null)); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "truth_value"



	// $ANTLR start "datetime_literal"
	// SQL99.g:838:1: datetime_literal returns [DateTimeLiteral value] : DATETIME ;
	public final DateTimeLiteral datetime_literal() throws RecognitionException {
		DateTimeLiteral value = null;


		Token DATETIME68=null;

		try {
			// SQL99.g:839:3: ( DATETIME )
			// SQL99.g:839:5: DATETIME
			{
			DATETIME68=(Token)match(input,DATETIME,FOLLOW_DATETIME_in_datetime_literal2248); 
			 value = new DateTimeLiteral((DATETIME68!=null?DATETIME68.getText():null)); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "datetime_literal"

	// Delegated rules



	public static final BitSet FOLLOW_query_in_parse51 = new BitSet(new long[]{0x0000000000000000L});
	public static final BitSet FOLLOW_EOF_in_parse53 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_query_specification_in_query79 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SELECT_in_query_specification120 = new BitSet(new long[]{0x4000000010000010L,0x0000000000040800L});
	public static final BitSet FOLLOW_set_quantifier_in_query_specification122 = new BitSet(new long[]{0x4000000000000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_select_list_in_query_specification125 = new BitSet(new long[]{0x0000001000000000L});
	public static final BitSet FOLLOW_table_expression_in_query_specification127 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ALL_in_set_quantifier148 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DISTINCT_in_set_quantifier156 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_select_sublist_in_select_list184 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_COMMA_in_select_list189 = new BitSet(new long[]{0x4000000000000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_select_sublist_in_select_list193 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_derived_column_in_select_sublist216 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_table_identifier_in_qualified_asterisk234 = new BitSet(new long[]{0x4000000000000000L});
	public static final BitSet FOLLOW_PERIOD_in_qualified_asterisk236 = new BitSet(new long[]{0x0000000000001000L});
	public static final BitSet FOLLOW_ASTERISK_in_qualified_asterisk238 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_value_expression_in_derived_column262 = new BitSet(new long[]{0x0000000000000802L,0x0000000000040800L});
	public static final BitSet FOLLOW_AS_in_derived_column265 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_alias_name_in_derived_column268 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_reference_value_expression_in_value_expression292 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_numeric_value_expression319 = new BitSet(new long[]{0x4000380007000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_numeric_operation_in_numeric_value_expression321 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_RPAREN_in_numeric_value_expression323 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_term_in_numeric_operation338 = new BitSet(new long[]{0x8040000000000002L});
	public static final BitSet FOLLOW_PLUS_in_numeric_operation356 = new BitSet(new long[]{0x4000380007000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_MINUS_in_numeric_operation360 = new BitSet(new long[]{0x4000380007000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_term_in_numeric_operation373 = new BitSet(new long[]{0x8040000000000002L});
	public static final BitSet FOLLOW_factor_in_term395 = new BitSet(new long[]{0x0000000000001002L,0x0000000000000100L});
	public static final BitSet FOLLOW_ASTERISK_in_term415 = new BitSet(new long[]{0x4000380007000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_SOLIDUS_in_term419 = new BitSet(new long[]{0x4000380007000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_factor_in_term433 = new BitSet(new long[]{0x0000000000001002L,0x0000000000000100L});
	public static final BitSet FOLLOW_column_reference_in_factor461 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_numeric_literal_in_factor469 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_string_value_expression512 = new BitSet(new long[]{0x4000000800000000L,0x0000000000044C00L});
	public static final BitSet FOLLOW_concatenation_in_string_value_expression514 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_RPAREN_in_string_value_expression516 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_character_factor_in_concatenation535 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_CONCATENATION_in_concatenation547 = new BitSet(new long[]{0x4000000800000000L,0x0000000000044C00L});
	public static final BitSet FOLLOW_character_factor_in_concatenation560 = new BitSet(new long[]{0x0000000000200002L});
	public static final BitSet FOLLOW_column_reference_in_character_factor581 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_general_literal_in_character_factor589 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_column_reference_in_reference_value_expression613 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_schema_name_in_column_reference636 = new BitSet(new long[]{0x4000000000000000L});
	public static final BitSet FOLLOW_PERIOD_in_column_reference640 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_table_identifier_in_column_reference644 = new BitSet(new long[]{0x4000000000000000L});
	public static final BitSet FOLLOW_PERIOD_in_column_reference646 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_column_name_in_column_reference650 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_function_specification_in_collection_value_expression678 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_COUNT_in_set_function_specification693 = new BitSet(new long[]{0x0004000000000000L});
	public static final BitSet FOLLOW_LPAREN_in_set_function_specification695 = new BitSet(new long[]{0x0000000000001000L});
	public static final BitSet FOLLOW_ASTERISK_in_set_function_specification697 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_RPAREN_in_set_function_specification699 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_general_set_function_in_set_function_specification707 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_function_op_in_general_set_function722 = new BitSet(new long[]{0x0004000000000000L});
	public static final BitSet FOLLOW_LPAREN_in_general_set_function724 = new BitSet(new long[]{0x4000000000000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_column_reference_in_general_set_function726 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_RPAREN_in_general_set_function728 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_AVG_in_set_function_op752 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_MAX_in_set_function_op758 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_MIN_in_set_function_op764 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SUM_in_set_function_op770 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_EVERY_in_set_function_op776 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ANY_in_set_function_op782 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SOME_in_set_function_op788 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_COUNT_in_set_function_op794 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_literal_in_row_value_expression816 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_value_expression_in_row_value_expression824 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_numeric_literal_in_literal843 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_general_literal_in_literal851 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_from_clause_in_table_expression870 = new BitSet(new long[]{0x0000000000000002L,0x0000000000080000L});
	public static final BitSet FOLLOW_where_clause_in_table_expression879 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FROM_in_from_clause904 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_table_reference_list_in_from_clause906 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_table_reference_in_table_reference_list936 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_COMMA_in_table_reference_list953 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_table_reference_in_table_reference_list957 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_table_primary_in_table_reference984 = new BitSet(new long[]{0x0001842000000002L,0x0000000000000008L});
	public static final BitSet FOLLOW_joined_table_in_table_reference993 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WHERE_in_where_clause1015 = new BitSet(new long[]{0x4004380807000000L,0x0000000000044C00L});
	public static final BitSet FOLLOW_search_condition_in_where_clause1017 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_boolean_value_expression_in_search_condition1036 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression1072 = new BitSet(new long[]{0x0400000000000002L});
	public static final BitSet FOLLOW_OR_in_boolean_value_expression1075 = new BitSet(new long[]{0x4004380807000000L,0x0000000000044C00L});
	public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression1079 = new BitSet(new long[]{0x0400000000000002L});
	public static final BitSet FOLLOW_boolean_factor_in_boolean_term1102 = new BitSet(new long[]{0x0000000000000102L});
	public static final BitSet FOLLOW_AND_in_boolean_term1105 = new BitSet(new long[]{0x4004380807000000L,0x0000000000044C00L});
	public static final BitSet FOLLOW_boolean_factor_in_boolean_term1109 = new BitSet(new long[]{0x0000000000000102L});
	public static final BitSet FOLLOW_predicate_in_boolean_factor1124 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_parenthesized_boolean_value_expression_in_boolean_factor1132 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_comparison_predicate_in_predicate1150 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_null_predicate_in_predicate1158 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_parenthesized_boolean_value_expression1174 = new BitSet(new long[]{0x4004380807000000L,0x0000000000044C00L});
	public static final BitSet FOLLOW_boolean_value_expression_in_parenthesized_boolean_value_expression1178 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_RPAREN_in_parenthesized_boolean_value_expression1180 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_row_value_expression_in_comparison_predicate1203 = new BitSet(new long[]{0x0002004100000000L});
	public static final BitSet FOLLOW_comp_op_in_comparison_predicate1205 = new BitSet(new long[]{0x4000380807000000L,0x0000000000044C00L});
	public static final BitSet FOLLOW_row_value_expression_in_comparison_predicate1209 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_EQUALS_in_comp_op1228 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LESS_in_comp_op1236 = new BitSet(new long[]{0x0000004000000000L});
	public static final BitSet FOLLOW_GREATER_in_comp_op1238 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LESS_in_comp_op1246 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_GREATER_in_comp_op1254 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LESS_in_comp_op1262 = new BitSet(new long[]{0x0000000100000000L});
	public static final BitSet FOLLOW_EQUALS_in_comp_op1264 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_GREATER_in_comp_op1272 = new BitSet(new long[]{0x0000000100000000L});
	public static final BitSet FOLLOW_EQUALS_in_comp_op1274 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_column_reference_in_null_predicate1298 = new BitSet(new long[]{0x0000400000000000L});
	public static final BitSet FOLLOW_IS_in_null_predicate1300 = new BitSet(new long[]{0x0180000000000000L});
	public static final BitSet FOLLOW_NOT_in_null_predicate1303 = new BitSet(new long[]{0x0100000000000000L});
	public static final BitSet FOLLOW_NULL_in_null_predicate1309 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_column_reference_in_in_predicate1324 = new BitSet(new long[]{0x0080020000000000L});
	public static final BitSet FOLLOW_NOT_in_in_predicate1327 = new BitSet(new long[]{0x0000020000000000L});
	public static final BitSet FOLLOW_IN_in_in_predicate1331 = new BitSet(new long[]{0x0004000000000000L});
	public static final BitSet FOLLOW_in_predicate_value_in_in_predicate1333 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_table_subquery_in_in_predicate_value1348 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_in_predicate_value1354 = new BitSet(new long[]{0x4000380807000000L,0x0000000000044C00L});
	public static final BitSet FOLLOW_in_value_list_in_in_predicate_value1356 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_RPAREN_in_in_predicate_value1358 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_subquery_in_table_subquery1371 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_subquery1384 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_query_in_subquery1386 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_RPAREN_in_subquery1388 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_row_value_expression_in_in_value_list1403 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_COMMA_in_in_value_list1406 = new BitSet(new long[]{0x4000380807000000L,0x0000000000044C00L});
	public static final BitSet FOLLOW_row_value_expression_in_in_value_list1408 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_GROUP_in_group_by_clause1427 = new BitSet(new long[]{0x0000000000010000L});
	public static final BitSet FOLLOW_BY_in_group_by_clause1429 = new BitSet(new long[]{0x4004000000000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_grouping_element_list_in_group_by_clause1431 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_grouping_element_in_grouping_element_list1457 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_COMMA_in_grouping_element_list1467 = new BitSet(new long[]{0x4004000000000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_grouping_element_in_grouping_element_list1471 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_grouping_column_reference_in_grouping_element1499 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_grouping_element1507 = new BitSet(new long[]{0x4000000000000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_grouping_column_reference_list_in_grouping_element1509 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_RPAREN_in_grouping_element1511 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_column_reference_in_grouping_column_reference1532 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_column_reference_in_grouping_column_reference_list1560 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_COMMA_in_grouping_column_reference_list1569 = new BitSet(new long[]{0x4000000000000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_column_reference_in_grouping_column_reference_list1573 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_join_type_in_joined_table1603 = new BitSet(new long[]{0x0000800000000000L});
	public static final BitSet FOLLOW_JOIN_in_joined_table1609 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_table_reference_in_joined_table1611 = new BitSet(new long[]{0x0200000000000000L});
	public static final BitSet FOLLOW_join_specification_in_joined_table1613 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INNER_in_join_type1638 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_outer_join_type_in_join_type1646 = new BitSet(new long[]{0x1000000000000002L});
	public static final BitSet FOLLOW_OUTER_in_join_type1649 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LEFT_in_outer_join_type1674 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_RIGHT_in_outer_join_type1682 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FULL_in_outer_join_type1690 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_join_condition_in_join_specification1709 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ON_in_join_condition1729 = new BitSet(new long[]{0x4004380807000000L,0x0000000000044C00L});
	public static final BitSet FOLLOW_search_condition_in_join_condition1731 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_USING_in_named_columns_join1746 = new BitSet(new long[]{0x0004000000000000L});
	public static final BitSet FOLLOW_LPAREN_in_named_columns_join1748 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_join_column_list_in_named_columns_join1750 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_RPAREN_in_named_columns_join1752 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_column_name_in_join_column_list1765 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_COMMA_in_join_column_list1768 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_column_name_in_join_column_list1770 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_table_name_in_table_primary1790 = new BitSet(new long[]{0x0000000000000802L,0x0000000000040800L});
	public static final BitSet FOLLOW_AS_in_table_primary1797 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_alias_name_in_table_primary1800 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_schema_name_in_table_name1828 = new BitSet(new long[]{0x4000000000000000L});
	public static final BitSet FOLLOW_PERIOD_in_table_name1830 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_table_identifier_in_table_name1834 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identifier_in_alias_name1855 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_table_subquery_in_derived_table1871 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identifier_in_table_identifier1892 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identifier_in_schema_name1913 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identifier_in_column_name1936 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_regular_identifier_in_identifier1960 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_delimited_identifier_in_identifier1966 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VARNAME_in_regular_identifier1986 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_delimited_identifier2005 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_string_literal_in_general_literal2024 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_boolean_literal_in_general_literal2032 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_WITH_QUOTE_in_string_literal2051 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TRUE_in_boolean_literal2073 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FALSE_in_boolean_literal2079 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_numeric_literal_unsigned_in_numeric_literal2099 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_numeric_literal_positive_in_numeric_literal2107 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_numeric_literal_negative_in_numeric_literal2115 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGER_in_numeric_literal_unsigned2134 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DECIMAL_in_numeric_literal_unsigned2142 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGER_POSITIVE_in_numeric_literal_positive2161 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DECIMAL_POSITIVE_in_numeric_literal_positive2169 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGER_NEGATIVE_in_numeric_literal_negative2190 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DECIMAL_NEGATIVE_in_numeric_literal_negative2198 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TRUE_in_truth_value2222 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FALSE_in_truth_value2228 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DATETIME_in_datetime_literal2248 = new BitSet(new long[]{0x0000000000000002L});
}
