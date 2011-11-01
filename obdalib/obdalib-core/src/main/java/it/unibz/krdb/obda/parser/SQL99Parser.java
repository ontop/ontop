// $ANTLR 3.4 C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g 2011-10-28 11:22:02

package it.unibz.krdb.obda.parser;

import java.util.Stack;
import java.util.Queue;
import java.util.LinkedList;

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
import it.unibz.krdb.sql.api.AndOperator;
import it.unibz.krdb.sql.api.OrOperator;
import it.unibz.krdb.sql.api.ColumnReference;

import it.unibz.krdb.sql.api.Literal;
import it.unibz.krdb.sql.api.StringLiteral;
import it.unibz.krdb.sql.api.BooleanLiteral;
import it.unibz.krdb.sql.api.NumericLiteral;
import it.unibz.krdb.sql.api.IntegerLiteral;
import it.unibz.krdb.sql.api.DecimalLiteral;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class SQL99Parser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ALL", "ALPHA", "ALPHANUM", "AMPERSAND", "AND", "ANY", "APOSTROPHE", "AS", "ASTERISK", "AT", "AVG", "BACKSLASH", "BY", "CARET", "CHAR", "COLON", "COMMA", "CONCATENATION", "COUNT", "DECIMAL", "DECIMAL_NEGATIVE", "DECIMAL_POSITIVE", "DIGIT", "DISTINCT", "DOLLAR", "DOUBLE_SLASH", "ECHAR", "EQUALS", "EVERY", "EXCLAMATION", "FALSE", "FROM", "FULL", "GREATER", "GROUP", "HASH", "IN", "INNER", "INTEGER", "INTEGER_NEGATIVE", "INTEGER_POSITIVE", "IS", "JOIN", "LEFT", "LESS", "LPAREN", "LSQ_BRACKET", "MAX", "MIN", "MINUS", "NOT", "NULL", "ON", "OR", "ORDER", "OUTER", "PERCENT", "PERIOD", "PLUS", "QUESTION", "QUOTE_DOUBLE", "QUOTE_SINGLE", "RIGHT", "RPAREN", "RSQ_BRACKET", "SELECT", "SEMI", "SOLIDUS", "SOME", "STRING_WITH_QUOTE", "STRING_WITH_QUOTE_DOUBLE", "SUM", "TILDE", "TRUE", "UNDERSCORE", "UNION", "USING", "VARNAME", "WHERE", "WS"
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
    public static final int DECIMAL=23;
    public static final int DECIMAL_NEGATIVE=24;
    public static final int DECIMAL_POSITIVE=25;
    public static final int DIGIT=26;
    public static final int DISTINCT=27;
    public static final int DOLLAR=28;
    public static final int DOUBLE_SLASH=29;
    public static final int ECHAR=30;
    public static final int EQUALS=31;
    public static final int EVERY=32;
    public static final int EXCLAMATION=33;
    public static final int FALSE=34;
    public static final int FROM=35;
    public static final int FULL=36;
    public static final int GREATER=37;
    public static final int GROUP=38;
    public static final int HASH=39;
    public static final int IN=40;
    public static final int INNER=41;
    public static final int INTEGER=42;
    public static final int INTEGER_NEGATIVE=43;
    public static final int INTEGER_POSITIVE=44;
    public static final int IS=45;
    public static final int JOIN=46;
    public static final int LEFT=47;
    public static final int LESS=48;
    public static final int LPAREN=49;
    public static final int LSQ_BRACKET=50;
    public static final int MAX=51;
    public static final int MIN=52;
    public static final int MINUS=53;
    public static final int NOT=54;
    public static final int NULL=55;
    public static final int ON=56;
    public static final int OR=57;
    public static final int ORDER=58;
    public static final int OUTER=59;
    public static final int PERCENT=60;
    public static final int PERIOD=61;
    public static final int PLUS=62;
    public static final int QUESTION=63;
    public static final int QUOTE_DOUBLE=64;
    public static final int QUOTE_SINGLE=65;
    public static final int RIGHT=66;
    public static final int RPAREN=67;
    public static final int RSQ_BRACKET=68;
    public static final int SELECT=69;
    public static final int SEMI=70;
    public static final int SOLIDUS=71;
    public static final int SOME=72;
    public static final int STRING_WITH_QUOTE=73;
    public static final int STRING_WITH_QUOTE_DOUBLE=74;
    public static final int SUM=75;
    public static final int TILDE=76;
    public static final int TRUE=77;
    public static final int UNDERSCORE=78;
    public static final int UNION=79;
    public static final int USING=80;
    public static final int VARNAME=81;
    public static final int WHERE=82;
    public static final int WS=83;

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

    public String[] getTokenNames() { return SQL99Parser.tokenNames; }
    public String getGrammarFileName() { return "C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g"; }


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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:175:1: parse returns [QueryTree value] : query EOF ;
    public final QueryTree parse() throws RecognitionException {
        QueryTree value = null;


        QueryTree query1 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:176:3: ( query EOF )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:176:5: query EOF
            {
            pushFollow(FOLLOW_query_in_parse40);
            query1=query();

            state._fsp--;


            match(input,EOF,FOLLOW_EOF_in_parse42); 


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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:181:1: query returns [QueryTree value] : a= query_specification ( UNION ( set_quantifier )? b= query_specification )* ;
    public final QueryTree query() throws RecognitionException {
        QueryTree value = null;


        QueryTree a =null;

        QueryTree b =null;

        int set_quantifier2 =0;



        int quantifier = 0;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:185:3: (a= query_specification ( UNION ( set_quantifier )? b= query_specification )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:185:5: a= query_specification ( UNION ( set_quantifier )? b= query_specification )*
            {
            pushFollow(FOLLOW_query_specification_in_query70);
            a=query_specification();

            state._fsp--;


             
                  queryTree = a; 
                  value = queryTree;
                

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:189:5: ( UNION ( set_quantifier )? b= query_specification )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==UNION) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:189:6: UNION ( set_quantifier )? b= query_specification
            	    {
            	    match(input,UNION,FOLLOW_UNION_in_query79); 

            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:189:12: ( set_quantifier )?
            	    int alt1=2;
            	    int LA1_0 = input.LA(1);

            	    if ( (LA1_0==ALL||LA1_0==DISTINCT) ) {
            	        alt1=1;
            	    }
            	    switch (alt1) {
            	        case 1 :
            	            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:189:12: set_quantifier
            	            {
            	            pushFollow(FOLLOW_set_quantifier_in_query81);
            	            set_quantifier2=set_quantifier();

            	            state._fsp--;


            	            }
            	            break;

            	    }


            	    pushFollow(FOLLOW_query_specification_in_query86);
            	    b=query_specification();

            	    state._fsp--;



            	          quantifier = set_quantifier2;
            	          SetUnion union = new SetUnion(quantifier);
            	              
            	          QueryTree parent = new QueryTree(union);      
            	          parent.attachLeft(queryTree);
            	          parent.attachRight(b);
            	           
            	          queryTree = parent.root();
            	          value = queryTree;
            	        

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:202:1: query_specification returns [QueryTree value] : SELECT ( set_quantifier )? select_list table_expression ;
    public final QueryTree query_specification() throws RecognitionException {
        QueryTree value = null;


        TableExpression table_expression3 =null;

        ArrayList<DerivedColumn> select_list4 =null;

        int set_quantifier5 =0;



        int quantifier = 0;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:206:3: ( SELECT ( set_quantifier )? select_list table_expression )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:206:5: SELECT ( set_quantifier )? select_list table_expression
            {
            match(input,SELECT,FOLLOW_SELECT_in_query_specification114); 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:206:12: ( set_quantifier )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==ALL||LA3_0==DISTINCT) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:206:12: set_quantifier
                    {
                    pushFollow(FOLLOW_set_quantifier_in_query_specification116);
                    set_quantifier5=set_quantifier();

                    state._fsp--;


                    }
                    break;

            }


            pushFollow(FOLLOW_select_list_in_query_specification119);
            select_list4=select_list();

            state._fsp--;


            pushFollow(FOLLOW_table_expression_in_query_specification121);
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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:245:1: set_quantifier returns [int value] : ( ALL | DISTINCT );
    public final int set_quantifier() throws RecognitionException {
        int value = 0;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:246:3: ( ALL | DISTINCT )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==ALL) ) {
                alt4=1;
            }
            else if ( (LA4_0==DISTINCT) ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;

            }
            switch (alt4) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:246:5: ALL
                    {
                    match(input,ALL,FOLLOW_ALL_in_set_quantifier142); 

                     value = 1; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:247:5: DISTINCT
                    {
                    match(input,DISTINCT,FOLLOW_DISTINCT_in_set_quantifier150); 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:250:1: select_list returns [ArrayList<DerivedColumn> value] : a= select_sublist ( COMMA b= select_sublist )* ;
    public final ArrayList<DerivedColumn> select_list() throws RecognitionException {
        ArrayList<DerivedColumn> value = null;


        DerivedColumn a =null;

        DerivedColumn b =null;



          value = new ArrayList<DerivedColumn>();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:254:3: (a= select_sublist ( COMMA b= select_sublist )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:254:5: a= select_sublist ( COMMA b= select_sublist )*
            {
            pushFollow(FOLLOW_select_sublist_in_select_list178);
            a=select_sublist();

            state._fsp--;


             value.add(a); 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:254:48: ( COMMA b= select_sublist )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==COMMA) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:254:49: COMMA b= select_sublist
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_select_list183); 

            	    pushFollow(FOLLOW_select_sublist_in_select_list187);
            	    b=select_sublist();

            	    state._fsp--;


            	     value.add(b); 

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);


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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:257:1: select_sublist returns [DerivedColumn value] : ( qualified_asterisk | derived_column );
    public final DerivedColumn select_sublist() throws RecognitionException {
        DerivedColumn value = null;


        DerivedColumn derived_column6 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:258:3: ( qualified_asterisk | derived_column )
            int alt6=2;
            switch ( input.LA(1) ) {
            case VARNAME:
                {
                int LA6_1 = input.LA(2);

                if ( (LA6_1==PERIOD) ) {
                    int LA6_4 = input.LA(3);

                    if ( (LA6_4==ASTERISK) ) {
                        alt6=1;
                    }
                    else if ( (LA6_4==STRING_WITH_QUOTE_DOUBLE||LA6_4==VARNAME) ) {
                        alt6=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 6, 4, input);

                        throw nvae;

                    }
                }
                else if ( (LA6_1==AS||LA6_1==COMMA||LA6_1==FROM||LA6_1==STRING_WITH_QUOTE_DOUBLE||LA6_1==VARNAME) ) {
                    alt6=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 6, 1, input);

                    throw nvae;

                }
                }
                break;
            case STRING_WITH_QUOTE_DOUBLE:
                {
                int LA6_2 = input.LA(2);

                if ( (LA6_2==PERIOD) ) {
                    int LA6_4 = input.LA(3);

                    if ( (LA6_4==ASTERISK) ) {
                        alt6=1;
                    }
                    else if ( (LA6_4==STRING_WITH_QUOTE_DOUBLE||LA6_4==VARNAME) ) {
                        alt6=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 6, 4, input);

                        throw nvae;

                    }
                }
                else if ( (LA6_2==AS||LA6_2==COMMA||LA6_2==FROM||LA6_2==STRING_WITH_QUOTE_DOUBLE||LA6_2==VARNAME) ) {
                    alt6=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 6, 2, input);

                    throw nvae;

                }
                }
                break;
            case ANY:
            case AVG:
            case COUNT:
            case EVERY:
            case LPAREN:
            case MAX:
            case MIN:
            case SOME:
            case SUM:
                {
                alt6=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;

            }

            switch (alt6) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:258:5: qualified_asterisk
                    {
                    pushFollow(FOLLOW_qualified_asterisk_in_select_sublist210);
                    qualified_asterisk();

                    state._fsp--;


                     value = null; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:259:5: derived_column
                    {
                    pushFollow(FOLLOW_derived_column_in_select_sublist218);
                    derived_column6=derived_column();

                    state._fsp--;


                     value = derived_column6; 

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
    // $ANTLR end "select_sublist"



    // $ANTLR start "qualified_asterisk"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:262:1: qualified_asterisk : table_identifier PERIOD ASTERISK ;
    public final void qualified_asterisk() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:263:3: ( table_identifier PERIOD ASTERISK )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:263:5: table_identifier PERIOD ASTERISK
            {
            pushFollow(FOLLOW_table_identifier_in_qualified_asterisk235);
            table_identifier();

            state._fsp--;


            match(input,PERIOD,FOLLOW_PERIOD_in_qualified_asterisk237); 

            match(input,ASTERISK,FOLLOW_ASTERISK_in_qualified_asterisk239); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "qualified_asterisk"



    // $ANTLR start "derived_column"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:266:1: derived_column returns [DerivedColumn value] : value_expression ( ( AS )? alias_name )? ;
    public final DerivedColumn derived_column() throws RecognitionException {
        DerivedColumn value = null;


        AbstractValueExpression value_expression7 =null;

        String alias_name8 =null;



          value = new DerivedColumn();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:270:3: ( value_expression ( ( AS )? alias_name )? )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:270:5: value_expression ( ( AS )? alias_name )?
            {
            pushFollow(FOLLOW_value_expression_in_derived_column263);
            value_expression7=value_expression();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:270:22: ( ( AS )? alias_name )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==AS||LA8_0==STRING_WITH_QUOTE_DOUBLE||LA8_0==VARNAME) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:270:23: ( AS )? alias_name
                    {
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:270:23: ( AS )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0==AS) ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:270:23: AS
                            {
                            match(input,AS,FOLLOW_AS_in_derived_column266); 

                            }
                            break;

                    }


                    pushFollow(FOLLOW_alias_name_in_derived_column269);
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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:279:1: value_expression returns [AbstractValueExpression value] : ( numeric_value_expression | string_value_expression | reference_value_expression | collection_value_expression );
    public final AbstractValueExpression value_expression() throws RecognitionException {
        AbstractValueExpression value = null;


        NumericValueExpression numeric_value_expression9 =null;

        StringValueExpression string_value_expression10 =null;

        ReferenceValueExpression reference_value_expression11 =null;

        CollectionValueExpression collection_value_expression12 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:280:3: ( numeric_value_expression | string_value_expression | reference_value_expression | collection_value_expression )
            int alt9=4;
            switch ( input.LA(1) ) {
            case LPAREN:
                {
                switch ( input.LA(2) ) {
                case VARNAME:
                    {
                    switch ( input.LA(3) ) {
                    case PERIOD:
                        {
                        int LA9_8 = input.LA(4);

                        if ( (LA9_8==VARNAME) ) {
                            int LA9_9 = input.LA(5);

                            if ( (LA9_9==ASTERISK||LA9_9==MINUS||LA9_9==PLUS||LA9_9==RPAREN||LA9_9==SOLIDUS) ) {
                                alt9=1;
                            }
                            else if ( (LA9_9==CONCATENATION) ) {
                                alt9=2;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 9, 9, input);

                                throw nvae;

                            }
                        }
                        else if ( (LA9_8==STRING_WITH_QUOTE_DOUBLE) ) {
                            int LA9_10 = input.LA(5);

                            if ( (LA9_10==ASTERISK||LA9_10==MINUS||LA9_10==PLUS||LA9_10==RPAREN||LA9_10==SOLIDUS) ) {
                                alt9=1;
                            }
                            else if ( (LA9_10==CONCATENATION) ) {
                                alt9=2;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 9, 10, input);

                                throw nvae;

                            }
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 9, 8, input);

                            throw nvae;

                        }
                        }
                        break;
                    case ASTERISK:
                    case MINUS:
                    case PLUS:
                    case RPAREN:
                    case SOLIDUS:
                        {
                        alt9=1;
                        }
                        break;
                    case CONCATENATION:
                        {
                        alt9=2;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 9, 4, input);

                        throw nvae;

                    }

                    }
                    break;
                case STRING_WITH_QUOTE_DOUBLE:
                    {
                    switch ( input.LA(3) ) {
                    case PERIOD:
                        {
                        int LA9_8 = input.LA(4);

                        if ( (LA9_8==VARNAME) ) {
                            int LA9_9 = input.LA(5);

                            if ( (LA9_9==ASTERISK||LA9_9==MINUS||LA9_9==PLUS||LA9_9==RPAREN||LA9_9==SOLIDUS) ) {
                                alt9=1;
                            }
                            else if ( (LA9_9==CONCATENATION) ) {
                                alt9=2;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 9, 9, input);

                                throw nvae;

                            }
                        }
                        else if ( (LA9_8==STRING_WITH_QUOTE_DOUBLE) ) {
                            int LA9_10 = input.LA(5);

                            if ( (LA9_10==ASTERISK||LA9_10==MINUS||LA9_10==PLUS||LA9_10==RPAREN||LA9_10==SOLIDUS) ) {
                                alt9=1;
                            }
                            else if ( (LA9_10==CONCATENATION) ) {
                                alt9=2;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 9, 10, input);

                                throw nvae;

                            }
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 9, 8, input);

                            throw nvae;

                        }
                        }
                        break;
                    case ASTERISK:
                    case MINUS:
                    case PLUS:
                    case RPAREN:
                    case SOLIDUS:
                        {
                        alt9=1;
                        }
                        break;
                    case CONCATENATION:
                        {
                        alt9=2;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 9, 5, input);

                        throw nvae;

                    }

                    }
                    break;
                case DECIMAL:
                case DECIMAL_NEGATIVE:
                case DECIMAL_POSITIVE:
                case INTEGER:
                case INTEGER_NEGATIVE:
                case INTEGER_POSITIVE:
                    {
                    alt9=1;
                    }
                    break;
                case FALSE:
                case STRING_WITH_QUOTE:
                case TRUE:
                    {
                    alt9=2;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 9, 1, input);

                    throw nvae;

                }

                }
                break;
            case STRING_WITH_QUOTE_DOUBLE:
            case VARNAME:
                {
                alt9=3;
                }
                break;
            case ANY:
            case AVG:
            case COUNT:
            case EVERY:
            case MAX:
            case MIN:
            case SOME:
            case SUM:
                {
                alt9=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;

            }

            switch (alt9) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:280:5: numeric_value_expression
                    {
                    pushFollow(FOLLOW_numeric_value_expression_in_value_expression293);
                    numeric_value_expression9=numeric_value_expression();

                    state._fsp--;


                     value = numeric_value_expression9; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:281:5: string_value_expression
                    {
                    pushFollow(FOLLOW_string_value_expression_in_value_expression301);
                    string_value_expression10=string_value_expression();

                    state._fsp--;


                     value = string_value_expression10; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:282:5: reference_value_expression
                    {
                    pushFollow(FOLLOW_reference_value_expression_in_value_expression309);
                    reference_value_expression11=reference_value_expression();

                    state._fsp--;


                     value = reference_value_expression11; 

                    }
                    break;
                case 4 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:283:5: collection_value_expression
                    {
                    pushFollow(FOLLOW_collection_value_expression_in_value_expression317);
                    collection_value_expression12=collection_value_expression();

                    state._fsp--;


                     value = collection_value_expression12; 

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
    // $ANTLR end "value_expression"



    // $ANTLR start "numeric_value_expression"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:286:1: numeric_value_expression returns [NumericValueExpression value] : LPAREN numeric_operation RPAREN ;
    public final NumericValueExpression numeric_value_expression() throws RecognitionException {
        NumericValueExpression value = null;



          numericExp = new NumericValueExpression();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:290:3: ( LPAREN numeric_operation RPAREN )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:290:5: LPAREN numeric_operation RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_numeric_value_expression341); 

            pushFollow(FOLLOW_numeric_operation_in_numeric_value_expression343);
            numeric_operation();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_numeric_value_expression345); 


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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:295:1: numeric_operation : term ( (t= PLUS |t= MINUS ) term )* ;
    public final void numeric_operation() throws RecognitionException {
        Token t=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:296:3: ( term ( (t= PLUS |t= MINUS ) term )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:296:5: term ( (t= PLUS |t= MINUS ) term )*
            {
            pushFollow(FOLLOW_term_in_numeric_operation360);
            term();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:297:5: ( (t= PLUS |t= MINUS ) term )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0==MINUS||LA11_0==PLUS) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:298:7: (t= PLUS |t= MINUS ) term
            	    {
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:298:7: (t= PLUS |t= MINUS )
            	    int alt10=2;
            	    int LA10_0 = input.LA(1);

            	    if ( (LA10_0==PLUS) ) {
            	        alt10=1;
            	    }
            	    else if ( (LA10_0==MINUS) ) {
            	        alt10=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 10, 0, input);

            	        throw nvae;

            	    }
            	    switch (alt10) {
            	        case 1 :
            	            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:298:8: t= PLUS
            	            {
            	            t=(Token)match(input,PLUS,FOLLOW_PLUS_in_numeric_operation378); 

            	            }
            	            break;
            	        case 2 :
            	            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:298:15: t= MINUS
            	            {
            	            t=(Token)match(input,MINUS,FOLLOW_MINUS_in_numeric_operation382); 

            	            }
            	            break;

            	    }


            	     numericExp.putSpecification((t!=null?t.getText():null)); 

            	    pushFollow(FOLLOW_term_in_numeric_operation395);
            	    term();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "numeric_operation"



    // $ANTLR start "term"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:303:1: term : a= factor ( (t= ASTERISK |t= SOLIDUS ) b= factor )* ;
    public final void term() throws RecognitionException {
        Token t=null;
        Object a =null;

        Object b =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:304:3: (a= factor ( (t= ASTERISK |t= SOLIDUS ) b= factor )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:304:5: a= factor ( (t= ASTERISK |t= SOLIDUS ) b= factor )*
            {
            pushFollow(FOLLOW_factor_in_term417);
            a=factor();

            state._fsp--;


             numericExp.putSpecification(a); 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:305:5: ( (t= ASTERISK |t= SOLIDUS ) b= factor )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0==ASTERISK||LA13_0==SOLIDUS) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:306:7: (t= ASTERISK |t= SOLIDUS ) b= factor
            	    {
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:306:7: (t= ASTERISK |t= SOLIDUS )
            	    int alt12=2;
            	    int LA12_0 = input.LA(1);

            	    if ( (LA12_0==ASTERISK) ) {
            	        alt12=1;
            	    }
            	    else if ( (LA12_0==SOLIDUS) ) {
            	        alt12=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 12, 0, input);

            	        throw nvae;

            	    }
            	    switch (alt12) {
            	        case 1 :
            	            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:306:8: t= ASTERISK
            	            {
            	            t=(Token)match(input,ASTERISK,FOLLOW_ASTERISK_in_term437); 

            	            }
            	            break;
            	        case 2 :
            	            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:306:19: t= SOLIDUS
            	            {
            	            t=(Token)match(input,SOLIDUS,FOLLOW_SOLIDUS_in_term441); 

            	            }
            	            break;

            	    }


            	     numericExp.putSpecification((t!=null?t.getText():null)); 

            	    pushFollow(FOLLOW_factor_in_term455);
            	    b=factor();

            	    state._fsp--;


            	     numericExp.putSpecification(b); 

            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "term"



    // $ANTLR start "factor"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:311:1: factor returns [Object value] : ( column_reference | numeric_literal );
    public final Object factor() throws RecognitionException {
        Object value = null;


        ColumnReference column_reference13 =null;

        NumericLiteral numeric_literal14 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:312:3: ( column_reference | numeric_literal )
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==STRING_WITH_QUOTE_DOUBLE||LA14_0==VARNAME) ) {
                alt14=1;
            }
            else if ( ((LA14_0 >= DECIMAL && LA14_0 <= DECIMAL_POSITIVE)||(LA14_0 >= INTEGER && LA14_0 <= INTEGER_POSITIVE)) ) {
                alt14=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;

            }
            switch (alt14) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:312:5: column_reference
                    {
                    pushFollow(FOLLOW_column_reference_in_factor483);
                    column_reference13=column_reference();

                    state._fsp--;


                     value = column_reference13; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:313:5: numeric_literal
                    {
                    pushFollow(FOLLOW_numeric_literal_in_factor491);
                    numeric_literal14=numeric_literal();

                    state._fsp--;


                     value = numeric_literal14; 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:316:1: sign : ( PLUS | MINUS );
    public final void sign() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:317:3: ( PLUS | MINUS )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:
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
        return ;
    }
    // $ANTLR end "sign"



    // $ANTLR start "string_value_expression"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:321:1: string_value_expression returns [StringValueExpression value] : LPAREN concatenation RPAREN ;
    public final StringValueExpression string_value_expression() throws RecognitionException {
        StringValueExpression value = null;



          stringExp = new StringValueExpression();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:325:3: ( LPAREN concatenation RPAREN )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:325:5: LPAREN concatenation RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_string_value_expression534); 

            pushFollow(FOLLOW_concatenation_in_string_value_expression536);
            concatenation();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_string_value_expression538); 


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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:330:1: concatenation : a= character_factor ( CONCATENATION b= character_factor )+ ;
    public final void concatenation() throws RecognitionException {
        Object a =null;

        Object b =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:331:3: (a= character_factor ( CONCATENATION b= character_factor )+ )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:331:5: a= character_factor ( CONCATENATION b= character_factor )+
            {
            pushFollow(FOLLOW_character_factor_in_concatenation557);
            a=character_factor();

            state._fsp--;


             stringExp.putSpecification(a); 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:331:66: ( CONCATENATION b= character_factor )+
            int cnt15=0;
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==CONCATENATION) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:332:7: CONCATENATION b= character_factor
            	    {
            	    match(input,CONCATENATION,FOLLOW_CONCATENATION_in_concatenation569); 

            	     stringExp.putSpecification(StringValueExpression.CONCAT_OP); 

            	    pushFollow(FOLLOW_character_factor_in_concatenation582);
            	    b=character_factor();

            	    state._fsp--;


            	     stringExp.putSpecification(b); 

            	    }
            	    break;

            	default :
            	    if ( cnt15 >= 1 ) break loop15;
                        EarlyExitException eee =
                            new EarlyExitException(15, input);
                        throw eee;
                }
                cnt15++;
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "concatenation"



    // $ANTLR start "character_factor"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:336:1: character_factor returns [Object value] : ( column_reference | general_literal );
    public final Object character_factor() throws RecognitionException {
        Object value = null;


        ColumnReference column_reference15 =null;

        Literal general_literal16 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:337:3: ( column_reference | general_literal )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==STRING_WITH_QUOTE_DOUBLE||LA16_0==VARNAME) ) {
                alt16=1;
            }
            else if ( (LA16_0==FALSE||LA16_0==STRING_WITH_QUOTE||LA16_0==TRUE) ) {
                alt16=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;

            }
            switch (alt16) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:337:5: column_reference
                    {
                    pushFollow(FOLLOW_column_reference_in_character_factor603);
                    column_reference15=column_reference();

                    state._fsp--;


                     value = column_reference15; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:338:5: general_literal
                    {
                    pushFollow(FOLLOW_general_literal_in_character_factor611);
                    general_literal16=general_literal();

                    state._fsp--;


                     value = general_literal16; 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:341:1: reference_value_expression returns [ReferenceValueExpression value] : column_reference ;
    public final ReferenceValueExpression reference_value_expression() throws RecognitionException {
        ReferenceValueExpression value = null;


        ColumnReference column_reference17 =null;



          referenceExp = new ReferenceValueExpression();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:345:3: ( column_reference )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:345:5: column_reference
            {
            pushFollow(FOLLOW_column_reference_in_reference_value_expression635);
            column_reference17=column_reference();

            state._fsp--;


             
                  referenceExp.add(column_reference17);
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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:351:1: column_reference returns [ColumnReference value] : (t= table_identifier PERIOD )? column_name ;
    public final ColumnReference column_reference() throws RecognitionException {
        ColumnReference value = null;


        String t =null;

        String column_name18 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:352:3: ( (t= table_identifier PERIOD )? column_name )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:352:5: (t= table_identifier PERIOD )? column_name
            {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:352:5: (t= table_identifier PERIOD )?
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==VARNAME) ) {
                int LA17_1 = input.LA(2);

                if ( (LA17_1==PERIOD) ) {
                    alt17=1;
                }
            }
            else if ( (LA17_0==STRING_WITH_QUOTE_DOUBLE) ) {
                int LA17_2 = input.LA(2);

                if ( (LA17_2==PERIOD) ) {
                    alt17=1;
                }
            }
            switch (alt17) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:352:6: t= table_identifier PERIOD
                    {
                    pushFollow(FOLLOW_table_identifier_in_column_reference657);
                    t=table_identifier();

                    state._fsp--;


                    match(input,PERIOD,FOLLOW_PERIOD_in_column_reference659); 

                    }
                    break;

            }


            pushFollow(FOLLOW_column_name_in_column_reference663);
            column_name18=column_name();

            state._fsp--;



                  String table = "";
                  if (t != null) {
                    table = t;
                  }
                  value = new ColumnReference(table, column_name18);
                

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:361:1: collection_value_expression returns [CollectionValueExpression value] : set_function_specification ;
    public final CollectionValueExpression collection_value_expression() throws RecognitionException {
        CollectionValueExpression value = null;



          collectionExp = new CollectionValueExpression();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:365:3: ( set_function_specification )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:365:5: set_function_specification
            {
            pushFollow(FOLLOW_set_function_specification_in_collection_value_expression691);
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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:370:1: set_function_specification : ( COUNT LPAREN ASTERISK RPAREN | general_set_function );
    public final void set_function_specification() throws RecognitionException {
        Token COUNT19=null;
        Token ASTERISK20=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:371:3: ( COUNT LPAREN ASTERISK RPAREN | general_set_function )
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==COUNT) ) {
                int LA18_1 = input.LA(2);

                if ( (LA18_1==LPAREN) ) {
                    int LA18_3 = input.LA(3);

                    if ( (LA18_3==ASTERISK) ) {
                        alt18=1;
                    }
                    else if ( (LA18_3==STRING_WITH_QUOTE_DOUBLE||LA18_3==VARNAME) ) {
                        alt18=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 18, 3, input);

                        throw nvae;

                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 1, input);

                    throw nvae;

                }
            }
            else if ( (LA18_0==ANY||LA18_0==AVG||LA18_0==EVERY||(LA18_0 >= MAX && LA18_0 <= MIN)||LA18_0==SOME||LA18_0==SUM) ) {
                alt18=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;

            }
            switch (alt18) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:371:5: COUNT LPAREN ASTERISK RPAREN
                    {
                    COUNT19=(Token)match(input,COUNT,FOLLOW_COUNT_in_set_function_specification706); 

                    match(input,LPAREN,FOLLOW_LPAREN_in_set_function_specification708); 

                    ASTERISK20=(Token)match(input,ASTERISK,FOLLOW_ASTERISK_in_set_function_specification710); 

                    match(input,RPAREN,FOLLOW_RPAREN_in_set_function_specification712); 


                          collectionExp.putSpecification((COUNT19!=null?COUNT19.getText():null));
                          collectionExp.putSpecification((ASTERISK20!=null?ASTERISK20.getText():null));
                        

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:375:5: general_set_function
                    {
                    pushFollow(FOLLOW_general_set_function_in_set_function_specification720);
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
        return ;
    }
    // $ANTLR end "set_function_specification"



    // $ANTLR start "general_set_function"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:379:1: general_set_function : set_function_op LPAREN column_reference RPAREN ;
    public final void general_set_function() throws RecognitionException {
        String set_function_op21 =null;

        ColumnReference column_reference22 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:380:3: ( set_function_op LPAREN column_reference RPAREN )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:380:5: set_function_op LPAREN column_reference RPAREN
            {
            pushFollow(FOLLOW_set_function_op_in_general_set_function735);
            set_function_op21=set_function_op();

            state._fsp--;


            match(input,LPAREN,FOLLOW_LPAREN_in_general_set_function737); 

            pushFollow(FOLLOW_column_reference_in_general_set_function739);
            column_reference22=column_reference();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_general_set_function741); 


                  collectionExp.putSpecification(set_function_op21);
                  collectionExp.add(column_reference22);
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "general_set_function"



    // $ANTLR start "set_function_op"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:386:1: set_function_op returns [String value] : (t= AVG |t= MAX |t= MIN |t= SUM |t= EVERY |t= ANY |t= SOME |t= COUNT ) ;
    public final String set_function_op() throws RecognitionException {
        String value = null;


        Token t=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:387:3: ( (t= AVG |t= MAX |t= MIN |t= SUM |t= EVERY |t= ANY |t= SOME |t= COUNT ) )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:387:5: (t= AVG |t= MAX |t= MIN |t= SUM |t= EVERY |t= ANY |t= SOME |t= COUNT )
            {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:387:5: (t= AVG |t= MAX |t= MIN |t= SUM |t= EVERY |t= ANY |t= SOME |t= COUNT )
            int alt19=8;
            switch ( input.LA(1) ) {
            case AVG:
                {
                alt19=1;
                }
                break;
            case MAX:
                {
                alt19=2;
                }
                break;
            case MIN:
                {
                alt19=3;
                }
                break;
            case SUM:
                {
                alt19=4;
                }
                break;
            case EVERY:
                {
                alt19=5;
                }
                break;
            case ANY:
                {
                alt19=6;
                }
                break;
            case SOME:
                {
                alt19=7;
                }
                break;
            case COUNT:
                {
                alt19=8;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;

            }

            switch (alt19) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:387:6: t= AVG
                    {
                    t=(Token)match(input,AVG,FOLLOW_AVG_in_set_function_op765); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:387:14: t= MAX
                    {
                    t=(Token)match(input,MAX,FOLLOW_MAX_in_set_function_op771); 

                    }
                    break;
                case 3 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:387:22: t= MIN
                    {
                    t=(Token)match(input,MIN,FOLLOW_MIN_in_set_function_op777); 

                    }
                    break;
                case 4 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:387:30: t= SUM
                    {
                    t=(Token)match(input,SUM,FOLLOW_SUM_in_set_function_op783); 

                    }
                    break;
                case 5 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:387:38: t= EVERY
                    {
                    t=(Token)match(input,EVERY,FOLLOW_EVERY_in_set_function_op789); 

                    }
                    break;
                case 6 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:387:48: t= ANY
                    {
                    t=(Token)match(input,ANY,FOLLOW_ANY_in_set_function_op795); 

                    }
                    break;
                case 7 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:387:56: t= SOME
                    {
                    t=(Token)match(input,SOME,FOLLOW_SOME_in_set_function_op801); 

                    }
                    break;
                case 8 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:387:65: t= COUNT
                    {
                    t=(Token)match(input,COUNT,FOLLOW_COUNT_in_set_function_op807); 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:392:1: row_value_expression returns [IValueExpression value] : ( literal | value_expression );
    public final IValueExpression row_value_expression() throws RecognitionException {
        IValueExpression value = null;


        Literal literal23 =null;

        AbstractValueExpression value_expression24 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:393:3: ( literal | value_expression )
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( ((LA20_0 >= DECIMAL && LA20_0 <= DECIMAL_POSITIVE)||LA20_0==FALSE||(LA20_0 >= INTEGER && LA20_0 <= INTEGER_POSITIVE)||LA20_0==STRING_WITH_QUOTE||LA20_0==TRUE) ) {
                alt20=1;
            }
            else if ( (LA20_0==ANY||LA20_0==AVG||LA20_0==COUNT||LA20_0==EVERY||LA20_0==LPAREN||(LA20_0 >= MAX && LA20_0 <= MIN)||LA20_0==SOME||(LA20_0 >= STRING_WITH_QUOTE_DOUBLE && LA20_0 <= SUM)||LA20_0==VARNAME) ) {
                alt20=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;

            }
            switch (alt20) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:393:5: literal
                    {
                    pushFollow(FOLLOW_literal_in_row_value_expression829);
                    literal23=literal();

                    state._fsp--;


                     value = literal23; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:394:5: value_expression
                    {
                    pushFollow(FOLLOW_value_expression_in_row_value_expression837);
                    value_expression24=value_expression();

                    state._fsp--;


                     value = value_expression24; 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:397:1: literal returns [Literal value] : ( numeric_literal | general_literal );
    public final Literal literal() throws RecognitionException {
        Literal value = null;


        NumericLiteral numeric_literal25 =null;

        Literal general_literal26 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:398:3: ( numeric_literal | general_literal )
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( ((LA21_0 >= DECIMAL && LA21_0 <= DECIMAL_POSITIVE)||(LA21_0 >= INTEGER && LA21_0 <= INTEGER_POSITIVE)) ) {
                alt21=1;
            }
            else if ( (LA21_0==FALSE||LA21_0==STRING_WITH_QUOTE||LA21_0==TRUE) ) {
                alt21=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 21, 0, input);

                throw nvae;

            }
            switch (alt21) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:398:5: numeric_literal
                    {
                    pushFollow(FOLLOW_numeric_literal_in_literal856);
                    numeric_literal25=numeric_literal();

                    state._fsp--;


                     value = numeric_literal25; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:399:5: general_literal
                    {
                    pushFollow(FOLLOW_general_literal_in_literal864);
                    general_literal26=general_literal();

                    state._fsp--;


                     value = general_literal26; 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:402:1: table_expression returns [TableExpression value] : from_clause ( where_clause )? ( group_by_clause )? ;
    public final TableExpression table_expression() throws RecognitionException {
        TableExpression value = null;


        ArrayList<TablePrimary> from_clause27 =null;

        BooleanValueExpression where_clause28 =null;

        ArrayList<GroupingElement> group_by_clause29 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:403:3: ( from_clause ( where_clause )? ( group_by_clause )? )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:403:5: from_clause ( where_clause )? ( group_by_clause )?
            {
            pushFollow(FOLLOW_from_clause_in_table_expression883);
            from_clause27=from_clause();

            state._fsp--;



                  value = new TableExpression(from_clause27);
                

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:406:5: ( where_clause )?
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==WHERE) ) {
                alt22=1;
            }
            switch (alt22) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:406:6: where_clause
                    {
                    pushFollow(FOLLOW_where_clause_in_table_expression892);
                    where_clause28=where_clause();

                    state._fsp--;


                     value.setWhereClause(where_clause28); 

                    }
                    break;

            }


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:407:5: ( group_by_clause )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==GROUP) ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:407:6: group_by_clause
                    {
                    pushFollow(FOLLOW_group_by_clause_in_table_expression904);
                    group_by_clause29=group_by_clause();

                    state._fsp--;


                     value.setGroupByClause(group_by_clause29); 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:410:1: from_clause returns [ArrayList<TablePrimary> value] : FROM table_reference_list ;
    public final ArrayList<TablePrimary> from_clause() throws RecognitionException {
        ArrayList<TablePrimary> value = null;


        ArrayList<TablePrimary> table_reference_list30 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:411:3: ( FROM table_reference_list )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:411:5: FROM table_reference_list
            {
            match(input,FROM,FOLLOW_FROM_in_from_clause927); 

            pushFollow(FOLLOW_table_reference_list_in_from_clause929);
            table_reference_list30=table_reference_list();

            state._fsp--;



                  value = table_reference_list30;
                

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:416:1: table_reference_list returns [ArrayList<TablePrimary> value] : a= table_reference ( COMMA b= table_reference )* ;
    public final ArrayList<TablePrimary> table_reference_list() throws RecognitionException {
        ArrayList<TablePrimary> value = null;


        TablePrimary a =null;

        TablePrimary b =null;



          value = new ArrayList<TablePrimary>();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:420:3: (a= table_reference ( COMMA b= table_reference )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:420:5: a= table_reference ( COMMA b= table_reference )*
            {
            pushFollow(FOLLOW_table_reference_in_table_reference_list959);
            a=table_reference();

            state._fsp--;


             value.add(a); 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:421:5: ( COMMA b= table_reference )*
            loop24:
            do {
                int alt24=2;
                int LA24_0 = input.LA(1);

                if ( (LA24_0==COMMA) ) {
                    alt24=1;
                }


                switch (alt24) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:422:7: COMMA b= table_reference
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_table_reference_list976); 

            	    pushFollow(FOLLOW_table_reference_in_table_reference_list980);
            	    b=table_reference();

            	    state._fsp--;



            	            JoinOperator joinOp = new JoinOperator(JoinOperator.CROSS_JOIN);
            	            relationStack.push(joinOp);
            	            
            	            value.add(b);
            	          

            	    }
            	    break;

            	default :
            	    break loop24;
                }
            } while (true);


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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:430:1: table_reference returns [TablePrimary value] : table_primary ( joined_table )? ;
    public final TablePrimary table_reference() throws RecognitionException {
        TablePrimary value = null;


        TablePrimary table_primary31 =null;

        TablePrimary joined_table32 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:431:3: ( table_primary ( joined_table )? )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:431:5: table_primary ( joined_table )?
            {
            pushFollow(FOLLOW_table_primary_in_table_reference1007);
            table_primary31=table_primary();

            state._fsp--;


             value = table_primary31; 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:432:5: ( joined_table )?
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==FULL||LA25_0==INNER||(LA25_0 >= JOIN && LA25_0 <= LEFT)||LA25_0==RIGHT) ) {
                alt25=1;
            }
            switch (alt25) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:432:6: joined_table
                    {
                    pushFollow(FOLLOW_joined_table_in_table_reference1016);
                    joined_table32=joined_table();

                    state._fsp--;


                     value = joined_table32; 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:435:1: where_clause returns [BooleanValueExpression value] : WHERE search_condition ;
    public final BooleanValueExpression where_clause() throws RecognitionException {
        BooleanValueExpression value = null;


        BooleanValueExpression search_condition33 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:436:3: ( WHERE search_condition )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:436:5: WHERE search_condition
            {
            match(input,WHERE,FOLLOW_WHERE_in_where_clause1038); 

            pushFollow(FOLLOW_search_condition_in_where_clause1040);
            search_condition33=search_condition();

            state._fsp--;



                  value = search_condition33;
                

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:441:1: search_condition returns [BooleanValueExpression value] : boolean_value_expression ;
    public final BooleanValueExpression search_condition() throws RecognitionException {
        BooleanValueExpression value = null;


        BooleanValueExpression boolean_value_expression34 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:442:3: ( boolean_value_expression )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:442:5: boolean_value_expression
            {
            pushFollow(FOLLOW_boolean_value_expression_in_search_condition1059);
            boolean_value_expression34=boolean_value_expression();

            state._fsp--;



                  value = boolean_value_expression34;
                

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:447:1: boolean_value_expression returns [BooleanValueExpression value] : boolean_term ( OR boolean_term )* ;
    public final BooleanValueExpression boolean_value_expression() throws RecognitionException {
        BooleanValueExpression value = null;



          booleanExp = new BooleanValueExpression();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:451:3: ( boolean_term ( OR boolean_term )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:451:5: boolean_term ( OR boolean_term )*
            {
            pushFollow(FOLLOW_boolean_term_in_boolean_value_expression1085);
            boolean_term();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:451:18: ( OR boolean_term )*
            loop26:
            do {
                int alt26=2;
                int LA26_0 = input.LA(1);

                if ( (LA26_0==OR) ) {
                    alt26=1;
                }


                switch (alt26) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:451:19: OR boolean_term
            	    {
            	    match(input,OR,FOLLOW_OR_in_boolean_value_expression1088); 

            	     booleanExp.putSpecification(new OrOperator()); 

            	    pushFollow(FOLLOW_boolean_term_in_boolean_value_expression1092);
            	    boolean_term();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop26;
                }
            } while (true);



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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:456:1: boolean_term : boolean_factor ( AND boolean_factor )* ;
    public final void boolean_term() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:457:3: ( boolean_factor ( AND boolean_factor )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:457:5: boolean_factor ( AND boolean_factor )*
            {
            pushFollow(FOLLOW_boolean_factor_in_boolean_term1111);
            boolean_factor();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:457:20: ( AND boolean_factor )*
            loop27:
            do {
                int alt27=2;
                int LA27_0 = input.LA(1);

                if ( (LA27_0==AND) ) {
                    alt27=1;
                }


                switch (alt27) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:457:21: AND boolean_factor
            	    {
            	    match(input,AND,FOLLOW_AND_in_boolean_term1114); 

            	     booleanExp.putSpecification(new AndOperator()); 

            	    pushFollow(FOLLOW_boolean_factor_in_boolean_term1118);
            	    boolean_factor();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop27;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "boolean_term"



    // $ANTLR start "boolean_factor"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:461:1: boolean_factor : predicate ;
    public final void boolean_factor() throws RecognitionException {
        IPredicate predicate35 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:462:3: ( predicate )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:462:5: predicate
            {
            pushFollow(FOLLOW_predicate_in_boolean_factor1134);
            predicate35=predicate();

            state._fsp--;


             booleanExp.putSpecification(predicate35); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "boolean_factor"



    // $ANTLR start "predicate"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:465:1: predicate returns [IPredicate value] : comparison_predicate ;
    public final IPredicate predicate() throws RecognitionException {
        IPredicate value = null;


        ComparisonPredicate comparison_predicate36 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:466:3: ( comparison_predicate )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:466:5: comparison_predicate
            {
            pushFollow(FOLLOW_comparison_predicate_in_predicate1154);
            comparison_predicate36=comparison_predicate();

            state._fsp--;


             value = comparison_predicate36; 

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



    // $ANTLR start "comparison_predicate"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:471:1: comparison_predicate returns [ComparisonPredicate value] : a= row_value_expression comp_op b= row_value_expression ;
    public final ComparisonPredicate comparison_predicate() throws RecognitionException {
        ComparisonPredicate value = null;


        IValueExpression a =null;

        IValueExpression b =null;

        ComparisonPredicate.Operator comp_op37 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:472:3: (a= row_value_expression comp_op b= row_value_expression )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:472:5: a= row_value_expression comp_op b= row_value_expression
            {
            pushFollow(FOLLOW_row_value_expression_in_comparison_predicate1179);
            a=row_value_expression();

            state._fsp--;


            pushFollow(FOLLOW_comp_op_in_comparison_predicate1181);
            comp_op37=comp_op();

            state._fsp--;


            pushFollow(FOLLOW_row_value_expression_in_comparison_predicate1185);
            b=row_value_expression();

            state._fsp--;



                  value = new ComparisonPredicate(a, b, comp_op37);
                

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:477:1: comp_op returns [ComparisonPredicate.Operator value] : ( EQUALS | LESS GREATER | LESS | GREATER | LESS EQUALS | GREATER EQUALS );
    public final ComparisonPredicate.Operator comp_op() throws RecognitionException {
        ComparisonPredicate.Operator value = null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:478:3: ( EQUALS | LESS GREATER | LESS | GREATER | LESS EQUALS | GREATER EQUALS )
            int alt28=6;
            switch ( input.LA(1) ) {
            case EQUALS:
                {
                alt28=1;
                }
                break;
            case LESS:
                {
                switch ( input.LA(2) ) {
                case GREATER:
                    {
                    alt28=2;
                    }
                    break;
                case EQUALS:
                    {
                    alt28=5;
                    }
                    break;
                case ANY:
                case AVG:
                case COUNT:
                case DECIMAL:
                case DECIMAL_NEGATIVE:
                case DECIMAL_POSITIVE:
                case EVERY:
                case FALSE:
                case INTEGER:
                case INTEGER_NEGATIVE:
                case INTEGER_POSITIVE:
                case LPAREN:
                case MAX:
                case MIN:
                case SOME:
                case STRING_WITH_QUOTE:
                case STRING_WITH_QUOTE_DOUBLE:
                case SUM:
                case TRUE:
                case VARNAME:
                    {
                    alt28=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 28, 2, input);

                    throw nvae;

                }

                }
                break;
            case GREATER:
                {
                int LA28_3 = input.LA(2);

                if ( (LA28_3==EQUALS) ) {
                    alt28=6;
                }
                else if ( (LA28_3==ANY||LA28_3==AVG||(LA28_3 >= COUNT && LA28_3 <= DECIMAL_POSITIVE)||LA28_3==EVERY||LA28_3==FALSE||(LA28_3 >= INTEGER && LA28_3 <= INTEGER_POSITIVE)||LA28_3==LPAREN||(LA28_3 >= MAX && LA28_3 <= MIN)||(LA28_3 >= SOME && LA28_3 <= SUM)||LA28_3==TRUE||LA28_3==VARNAME) ) {
                    alt28=4;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 28, 3, input);

                    throw nvae;

                }
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 28, 0, input);

                throw nvae;

            }

            switch (alt28) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:478:5: EQUALS
                    {
                    match(input,EQUALS,FOLLOW_EQUALS_in_comp_op1204); 

                     value = ComparisonPredicate.Operator.EQ; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:479:5: LESS GREATER
                    {
                    match(input,LESS,FOLLOW_LESS_in_comp_op1212); 

                    match(input,GREATER,FOLLOW_GREATER_in_comp_op1214); 

                     value = ComparisonPredicate.Operator.NE; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:480:5: LESS
                    {
                    match(input,LESS,FOLLOW_LESS_in_comp_op1222); 

                     value = ComparisonPredicate.Operator.LT; 

                    }
                    break;
                case 4 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:481:5: GREATER
                    {
                    match(input,GREATER,FOLLOW_GREATER_in_comp_op1230); 

                     value = ComparisonPredicate.Operator.GT; 

                    }
                    break;
                case 5 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:482:5: LESS EQUALS
                    {
                    match(input,LESS,FOLLOW_LESS_in_comp_op1238); 

                    match(input,EQUALS,FOLLOW_EQUALS_in_comp_op1240); 

                     value = ComparisonPredicate.Operator.LE; 

                    }
                    break;
                case 6 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:483:5: GREATER EQUALS
                    {
                    match(input,GREATER,FOLLOW_GREATER_in_comp_op1248); 

                    match(input,EQUALS,FOLLOW_EQUALS_in_comp_op1250); 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:486:1: null_predicate : column_reference IS ( NOT )? NULL ;
    public final void null_predicate() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:487:3: ( column_reference IS ( NOT )? NULL )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:487:5: column_reference IS ( NOT )? NULL
            {
            pushFollow(FOLLOW_column_reference_in_null_predicate1265);
            column_reference();

            state._fsp--;


            match(input,IS,FOLLOW_IS_in_null_predicate1267); 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:487:25: ( NOT )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==NOT) ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:487:26: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_null_predicate1270); 

                    }
                    break;

            }


            match(input,NULL,FOLLOW_NULL_in_null_predicate1274); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "null_predicate"



    // $ANTLR start "in_predicate"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:490:1: in_predicate : column_reference ( NOT )? IN in_predicate_value ;
    public final void in_predicate() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:491:3: ( column_reference ( NOT )? IN in_predicate_value )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:491:5: column_reference ( NOT )? IN in_predicate_value
            {
            pushFollow(FOLLOW_column_reference_in_in_predicate1287);
            column_reference();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:491:22: ( NOT )?
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==NOT) ) {
                alt30=1;
            }
            switch (alt30) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:491:23: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_in_predicate1290); 

                    }
                    break;

            }


            match(input,IN,FOLLOW_IN_in_in_predicate1294); 

            pushFollow(FOLLOW_in_predicate_value_in_in_predicate1296);
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
        return ;
    }
    // $ANTLR end "in_predicate"



    // $ANTLR start "in_predicate_value"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:494:1: in_predicate_value : ( table_subquery | LPAREN in_value_list RPAREN );
    public final void in_predicate_value() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:495:3: ( table_subquery | LPAREN in_value_list RPAREN )
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==LPAREN) ) {
                int LA31_1 = input.LA(2);

                if ( (LA31_1==SELECT) ) {
                    alt31=1;
                }
                else if ( (LA31_1==ANY||LA31_1==AVG||(LA31_1 >= COUNT && LA31_1 <= DECIMAL_POSITIVE)||LA31_1==EVERY||LA31_1==FALSE||(LA31_1 >= INTEGER && LA31_1 <= INTEGER_POSITIVE)||LA31_1==LPAREN||(LA31_1 >= MAX && LA31_1 <= MIN)||(LA31_1 >= SOME && LA31_1 <= SUM)||LA31_1==TRUE||LA31_1==VARNAME) ) {
                    alt31=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 31, 1, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 31, 0, input);

                throw nvae;

            }
            switch (alt31) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:495:5: table_subquery
                    {
                    pushFollow(FOLLOW_table_subquery_in_in_predicate_value1311);
                    table_subquery();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:496:5: LPAREN in_value_list RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_in_predicate_value1317); 

                    pushFollow(FOLLOW_in_value_list_in_in_predicate_value1319);
                    in_value_list();

                    state._fsp--;


                    match(input,RPAREN,FOLLOW_RPAREN_in_in_predicate_value1321); 

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
        return ;
    }
    // $ANTLR end "in_predicate_value"



    // $ANTLR start "table_subquery"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:499:1: table_subquery : subquery ;
    public final void table_subquery() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:500:3: ( subquery )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:500:5: subquery
            {
            pushFollow(FOLLOW_subquery_in_table_subquery1334);
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
        return ;
    }
    // $ANTLR end "table_subquery"



    // $ANTLR start "subquery"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:503:1: subquery : LPAREN query RPAREN ;
    public final void subquery() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:504:3: ( LPAREN query RPAREN )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:504:5: LPAREN query RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_subquery1347); 

            pushFollow(FOLLOW_query_in_subquery1349);
            query();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_subquery1351); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "subquery"



    // $ANTLR start "in_value_list"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:507:1: in_value_list : row_value_expression ( COMMA row_value_expression )* ;
    public final void in_value_list() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:508:3: ( row_value_expression ( COMMA row_value_expression )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:508:5: row_value_expression ( COMMA row_value_expression )*
            {
            pushFollow(FOLLOW_row_value_expression_in_in_value_list1366);
            row_value_expression();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:508:26: ( COMMA row_value_expression )*
            loop32:
            do {
                int alt32=2;
                int LA32_0 = input.LA(1);

                if ( (LA32_0==COMMA) ) {
                    alt32=1;
                }


                switch (alt32) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:508:27: COMMA row_value_expression
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_in_value_list1369); 

            	    pushFollow(FOLLOW_row_value_expression_in_in_value_list1371);
            	    row_value_expression();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop32;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "in_value_list"



    // $ANTLR start "group_by_clause"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:511:1: group_by_clause returns [ArrayList<GroupingElement> value] : GROUP BY grouping_element_list ;
    public final ArrayList<GroupingElement> group_by_clause() throws RecognitionException {
        ArrayList<GroupingElement> value = null;


        ArrayList<GroupingElement> grouping_element_list38 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:512:3: ( GROUP BY grouping_element_list )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:512:5: GROUP BY grouping_element_list
            {
            match(input,GROUP,FOLLOW_GROUP_in_group_by_clause1390); 

            match(input,BY,FOLLOW_BY_in_group_by_clause1392); 

            pushFollow(FOLLOW_grouping_element_list_in_group_by_clause1394);
            grouping_element_list38=grouping_element_list();

            state._fsp--;



                  value = grouping_element_list38;
                

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:517:1: grouping_element_list returns [ArrayList<GroupingElement> value] : a= grouping_element ( COMMA b= grouping_element )* ;
    public final ArrayList<GroupingElement> grouping_element_list() throws RecognitionException {
        ArrayList<GroupingElement> value = null;


        GroupingElement a =null;

        GroupingElement b =null;



          value = new ArrayList<GroupingElement>();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:521:3: (a= grouping_element ( COMMA b= grouping_element )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:521:5: a= grouping_element ( COMMA b= grouping_element )*
            {
            pushFollow(FOLLOW_grouping_element_in_grouping_element_list1420);
            a=grouping_element();

            state._fsp--;


             value.add(a); 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:522:5: ( COMMA b= grouping_element )*
            loop33:
            do {
                int alt33=2;
                int LA33_0 = input.LA(1);

                if ( (LA33_0==COMMA) ) {
                    alt33=1;
                }


                switch (alt33) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:522:6: COMMA b= grouping_element
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_grouping_element_list1430); 

            	    pushFollow(FOLLOW_grouping_element_in_grouping_element_list1434);
            	    b=grouping_element();

            	    state._fsp--;


            	     value.add(b); 

            	    }
            	    break;

            	default :
            	    break loop33;
                }
            } while (true);


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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:525:1: grouping_element returns [GroupingElement value] : ( grouping_column_reference | LPAREN grouping_column_reference_list RPAREN );
    public final GroupingElement grouping_element() throws RecognitionException {
        GroupingElement value = null;


        ColumnReference grouping_column_reference39 =null;

        ArrayList<ColumnReference> grouping_column_reference_list40 =null;



          value = new GroupingElement();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:529:3: ( grouping_column_reference | LPAREN grouping_column_reference_list RPAREN )
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==STRING_WITH_QUOTE_DOUBLE||LA34_0==VARNAME) ) {
                alt34=1;
            }
            else if ( (LA34_0==LPAREN) ) {
                alt34=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 34, 0, input);

                throw nvae;

            }
            switch (alt34) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:529:5: grouping_column_reference
                    {
                    pushFollow(FOLLOW_grouping_column_reference_in_grouping_element1462);
                    grouping_column_reference39=grouping_column_reference();

                    state._fsp--;


                     value.add(grouping_column_reference39); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:530:5: LPAREN grouping_column_reference_list RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_grouping_element1470); 

                    pushFollow(FOLLOW_grouping_column_reference_list_in_grouping_element1472);
                    grouping_column_reference_list40=grouping_column_reference_list();

                    state._fsp--;


                    match(input,RPAREN,FOLLOW_RPAREN_in_grouping_element1474); 

                     value.update(grouping_column_reference_list40); 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:533:1: grouping_column_reference returns [ColumnReference value] : column_reference ;
    public final ColumnReference grouping_column_reference() throws RecognitionException {
        ColumnReference value = null;


        ColumnReference column_reference41 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:534:3: ( column_reference )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:534:5: column_reference
            {
            pushFollow(FOLLOW_column_reference_in_grouping_column_reference1495);
            column_reference41=column_reference();

            state._fsp--;


             value = column_reference41; 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:537:1: grouping_column_reference_list returns [ArrayList<ColumnReference> value] : a= column_reference ( COMMA b= column_reference )* ;
    public final ArrayList<ColumnReference> grouping_column_reference_list() throws RecognitionException {
        ArrayList<ColumnReference> value = null;


        ColumnReference a =null;

        ColumnReference b =null;



          value = new ArrayList<ColumnReference>();

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:541:3: (a= column_reference ( COMMA b= column_reference )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:541:5: a= column_reference ( COMMA b= column_reference )*
            {
            pushFollow(FOLLOW_column_reference_in_grouping_column_reference_list1523);
            a=column_reference();

            state._fsp--;


             value.add(a); 

            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:542:5: ( COMMA b= column_reference )*
            loop35:
            do {
                int alt35=2;
                int LA35_0 = input.LA(1);

                if ( (LA35_0==COMMA) ) {
                    alt35=1;
                }


                switch (alt35) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:542:6: COMMA b= column_reference
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_grouping_column_reference_list1532); 

            	    pushFollow(FOLLOW_column_reference_in_grouping_column_reference_list1536);
            	    b=column_reference();

            	    state._fsp--;


            	     value.add(b); 

            	    }
            	    break;

            	default :
            	    break loop35;
                }
            } while (true);


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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:545:1: joined_table returns [TablePrimary value] : ( ( join_type )? JOIN table_reference join_specification )+ ;
    public final TablePrimary joined_table() throws RecognitionException {
        TablePrimary value = null;


        int join_type42 =0;

        BooleanValueExpression join_specification43 =null;

        TablePrimary table_reference44 =null;



          int joinType = JoinOperator.JOIN; // by default

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:549:3: ( ( ( join_type )? JOIN table_reference join_specification )+ )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:549:5: ( ( join_type )? JOIN table_reference join_specification )+
            {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:549:5: ( ( join_type )? JOIN table_reference join_specification )+
            int cnt37=0;
            loop37:
            do {
                int alt37=2;
                int LA37_0 = input.LA(1);

                if ( (LA37_0==FULL||LA37_0==INNER||(LA37_0 >= JOIN && LA37_0 <= LEFT)||LA37_0==RIGHT) ) {
                    alt37=1;
                }


                switch (alt37) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:549:6: ( join_type )? JOIN table_reference join_specification
            	    {
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:549:6: ( join_type )?
            	    int alt36=2;
            	    int LA36_0 = input.LA(1);

            	    if ( (LA36_0==FULL||LA36_0==INNER||LA36_0==LEFT||LA36_0==RIGHT) ) {
            	        alt36=1;
            	    }
            	    switch (alt36) {
            	        case 1 :
            	            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:549:7: join_type
            	            {
            	            pushFollow(FOLLOW_join_type_in_joined_table1566);
            	            join_type42=join_type();

            	            state._fsp--;


            	             joinType = join_type42; 

            	            }
            	            break;

            	    }


            	    match(input,JOIN,FOLLOW_JOIN_in_joined_table1572); 

            	    pushFollow(FOLLOW_table_reference_in_joined_table1574);
            	    table_reference44=table_reference();

            	    state._fsp--;


            	    pushFollow(FOLLOW_join_specification_in_joined_table1576);
            	    join_specification43=join_specification();

            	    state._fsp--;



            	          JoinOperator joinOp = new JoinOperator(joinType);
            	          joinOp.copy(join_specification43.getSpecification());
            	          relationStack.push(joinOp);
            	          value = table_reference44;
            	        

            	    }
            	    break;

            	default :
            	    if ( cnt37 >= 1 ) break loop37;
                        EarlyExitException eee =
                            new EarlyExitException(37, input);
                        throw eee;
                }
                cnt37++;
            } while (true);


            }

        }
        catch (Exception e) {

                 // Does nothing.
              
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "joined_table"



    // $ANTLR start "join_type"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:560:1: join_type returns [int value] : ( INNER | outer_join_type ( OUTER )? );
    public final int join_type() throws RecognitionException {
        int value = 0;


        int outer_join_type45 =0;



          boolean bHasOuter = false;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:564:3: ( INNER | outer_join_type ( OUTER )? )
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==INNER) ) {
                alt39=1;
            }
            else if ( (LA39_0==FULL||LA39_0==LEFT||LA39_0==RIGHT) ) {
                alt39=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;

            }
            switch (alt39) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:564:5: INNER
                    {
                    match(input,INNER,FOLLOW_INNER_in_join_type1612); 

                     value = JoinOperator.INNER_JOIN; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:565:5: outer_join_type ( OUTER )?
                    {
                    pushFollow(FOLLOW_outer_join_type_in_join_type1620);
                    outer_join_type45=outer_join_type();

                    state._fsp--;


                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:565:21: ( OUTER )?
                    int alt38=2;
                    int LA38_0 = input.LA(1);

                    if ( (LA38_0==OUTER) ) {
                        alt38=1;
                    }
                    switch (alt38) {
                        case 1 :
                            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:565:22: OUTER
                            {
                            match(input,OUTER,FOLLOW_OUTER_in_join_type1623); 

                             bHasOuter = true; 

                            }
                            break;

                    }



                          if (bHasOuter) {
                            switch(outer_join_type45) {
                              case JoinOperator.LEFT_JOIN: value = JoinOperator.LEFT_OUTER_JOIN; break;
                              case JoinOperator.RIGHT_JOIN: value = JoinOperator.RIGHT_OUTER_JOIN; break;
                              case JoinOperator.FULL_JOIN: value = JoinOperator.FULL_OUTER_JOIN; break;
                            }
                          }
                          else {
                            value = outer_join_type45;
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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:579:1: outer_join_type returns [int value] : ( LEFT | RIGHT | FULL );
    public final int outer_join_type() throws RecognitionException {
        int value = 0;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:580:3: ( LEFT | RIGHT | FULL )
            int alt40=3;
            switch ( input.LA(1) ) {
            case LEFT:
                {
                alt40=1;
                }
                break;
            case RIGHT:
                {
                alt40=2;
                }
                break;
            case FULL:
                {
                alt40=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 40, 0, input);

                throw nvae;

            }

            switch (alt40) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:580:5: LEFT
                    {
                    match(input,LEFT,FOLLOW_LEFT_in_outer_join_type1648); 

                     value = JoinOperator.LEFT_JOIN; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:581:5: RIGHT
                    {
                    match(input,RIGHT,FOLLOW_RIGHT_in_outer_join_type1656); 

                     value = JoinOperator.RIGHT_JOIN; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:582:5: FULL
                    {
                    match(input,FULL,FOLLOW_FULL_in_outer_join_type1664); 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:585:1: join_specification returns [BooleanValueExpression value] : ( join_condition | named_columns_join );
    public final BooleanValueExpression join_specification() throws RecognitionException {
        BooleanValueExpression value = null;


        BooleanValueExpression join_condition46 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:586:3: ( join_condition | named_columns_join )
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==ON) ) {
                alt41=1;
            }
            else if ( (LA41_0==USING) ) {
                alt41=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 41, 0, input);

                throw nvae;

            }
            switch (alt41) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:586:5: join_condition
                    {
                    pushFollow(FOLLOW_join_condition_in_join_specification1683);
                    join_condition46=join_condition();

                    state._fsp--;


                     value = join_condition46; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:587:5: named_columns_join
                    {
                    pushFollow(FOLLOW_named_columns_join_in_join_specification1691);
                    named_columns_join();

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
        return value;
    }
    // $ANTLR end "join_specification"



    // $ANTLR start "join_condition"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:590:1: join_condition returns [BooleanValueExpression value] : ON search_condition ;
    public final BooleanValueExpression join_condition() throws RecognitionException {
        BooleanValueExpression value = null;


        BooleanValueExpression search_condition47 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:591:3: ( ON search_condition )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:591:5: ON search_condition
            {
            match(input,ON,FOLLOW_ON_in_join_condition1708); 

            pushFollow(FOLLOW_search_condition_in_join_condition1710);
            search_condition47=search_condition();

            state._fsp--;



                  value = search_condition47;
                

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:596:1: named_columns_join : USING LPAREN join_column_list RPAREN ;
    public final void named_columns_join() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:597:3: ( USING LPAREN join_column_list RPAREN )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:597:5: USING LPAREN join_column_list RPAREN
            {
            match(input,USING,FOLLOW_USING_in_named_columns_join1725); 

            match(input,LPAREN,FOLLOW_LPAREN_in_named_columns_join1727); 

            pushFollow(FOLLOW_join_column_list_in_named_columns_join1729);
            join_column_list();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_named_columns_join1731); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "named_columns_join"



    // $ANTLR start "join_column_list"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:600:1: join_column_list : column_name ( COMMA column_name )* ;
    public final void join_column_list() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:601:3: ( column_name ( COMMA column_name )* )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:601:5: column_name ( COMMA column_name )*
            {
            pushFollow(FOLLOW_column_name_in_join_column_list1744);
            column_name();

            state._fsp--;


            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:601:17: ( COMMA column_name )*
            loop42:
            do {
                int alt42=2;
                int LA42_0 = input.LA(1);

                if ( (LA42_0==COMMA) ) {
                    alt42=1;
                }


                switch (alt42) {
            	case 1 :
            	    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:601:18: COMMA column_name
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_join_column_list1747); 

            	    pushFollow(FOLLOW_column_name_in_join_column_list1749);
            	    column_name();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop42;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "join_column_list"



    // $ANTLR start "table_primary"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:605:1: table_primary returns [TablePrimary value] : ( table_name ( ( AS )? alias_name )? | derived_table ( AS )? alias_name );
    public final TablePrimary table_primary() throws RecognitionException {
        TablePrimary value = null;


        TablePrimary table_name48 =null;

        String alias_name49 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:606:3: ( table_name ( ( AS )? alias_name )? | derived_table ( AS )? alias_name )
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==STRING_WITH_QUOTE_DOUBLE||LA46_0==VARNAME) ) {
                alt46=1;
            }
            else if ( (LA46_0==LPAREN) ) {
                alt46=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 46, 0, input);

                throw nvae;

            }
            switch (alt46) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:606:5: table_name ( ( AS )? alias_name )?
                    {
                    pushFollow(FOLLOW_table_name_in_table_primary1769);
                    table_name48=table_name();

                    state._fsp--;


                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:607:5: ( ( AS )? alias_name )?
                    int alt44=2;
                    int LA44_0 = input.LA(1);

                    if ( (LA44_0==AS||LA44_0==STRING_WITH_QUOTE_DOUBLE||LA44_0==VARNAME) ) {
                        alt44=1;
                    }
                    switch (alt44) {
                        case 1 :
                            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:607:6: ( AS )? alias_name
                            {
                            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:607:6: ( AS )?
                            int alt43=2;
                            int LA43_0 = input.LA(1);

                            if ( (LA43_0==AS) ) {
                                alt43=1;
                            }
                            switch (alt43) {
                                case 1 :
                                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:607:6: AS
                                    {
                                    match(input,AS,FOLLOW_AS_in_table_primary1776); 

                                    }
                                    break;

                            }


                            pushFollow(FOLLOW_alias_name_in_table_primary1779);
                            alias_name49=alias_name();

                            state._fsp--;


                            }
                            break;

                    }



                          value = table_name48; 
                          value.setAlias(alias_name49);
                          Relation table = new Relation(value);      
                          relationStack.push(table);
                        

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:613:5: derived_table ( AS )? alias_name
                    {
                    pushFollow(FOLLOW_derived_table_in_table_primary1789);
                    derived_table();

                    state._fsp--;


                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:614:5: ( AS )?
                    int alt45=2;
                    int LA45_0 = input.LA(1);

                    if ( (LA45_0==AS) ) {
                        alt45=1;
                    }
                    switch (alt45) {
                        case 1 :
                            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:614:5: AS
                            {
                            match(input,AS,FOLLOW_AS_in_table_primary1795); 

                            }
                            break;

                    }


                    pushFollow(FOLLOW_alias_name_in_table_primary1798);
                    alias_name();

                    state._fsp--;



                          value = null;
                        

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
    // $ANTLR end "table_primary"



    // $ANTLR start "table_name"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:619:1: table_name returns [TablePrimary value] : ( schema_name PERIOD )? table_identifier ;
    public final TablePrimary table_name() throws RecognitionException {
        TablePrimary value = null;


        String schema_name50 =null;

        String table_identifier51 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:620:3: ( ( schema_name PERIOD )? table_identifier )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:620:5: ( schema_name PERIOD )? table_identifier
            {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:620:5: ( schema_name PERIOD )?
            int alt47=2;
            int LA47_0 = input.LA(1);

            if ( (LA47_0==VARNAME) ) {
                int LA47_1 = input.LA(2);

                if ( (LA47_1==PERIOD) ) {
                    alt47=1;
                }
            }
            else if ( (LA47_0==STRING_WITH_QUOTE_DOUBLE) ) {
                int LA47_2 = input.LA(2);

                if ( (LA47_2==PERIOD) ) {
                    alt47=1;
                }
            }
            switch (alt47) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:620:6: schema_name PERIOD
                    {
                    pushFollow(FOLLOW_schema_name_in_table_name1820);
                    schema_name50=schema_name();

                    state._fsp--;


                    match(input,PERIOD,FOLLOW_PERIOD_in_table_name1822); 

                    }
                    break;

            }


            pushFollow(FOLLOW_table_identifier_in_table_name1826);
            table_identifier51=table_identifier();

            state._fsp--;



                  String schema = schema_name50;      
                  if (schema != null && schema != "") {
                    value = new TablePrimary(schema, table_identifier51);
                  }
                  else {
                    value = new TablePrimary(table_identifier51);
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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:631:1: alias_name returns [String value] : identifier ;
    public final String alias_name() throws RecognitionException {
        String value = null;


        String identifier52 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:632:3: ( identifier )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:632:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_alias_name1847);
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
    // $ANTLR end "alias_name"



    // $ANTLR start "derived_table"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:635:1: derived_table : table_subquery ;
    public final void derived_table() throws RecognitionException {
        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:636:3: ( table_subquery )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:636:5: table_subquery
            {
            pushFollow(FOLLOW_table_subquery_in_derived_table1863);
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
        return ;
    }
    // $ANTLR end "derived_table"



    // $ANTLR start "table_identifier"
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:639:1: table_identifier returns [String value] : identifier ;
    public final String table_identifier() throws RecognitionException {
        String value = null;


        String identifier53 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:640:3: ( identifier )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:640:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_table_identifier1884);
            identifier53=identifier();

            state._fsp--;


             value = identifier53; 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:643:1: schema_name returns [String value] : identifier ;
    public final String schema_name() throws RecognitionException {
        String value = null;


        String identifier54 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:644:3: ( identifier )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:644:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_schema_name1905);
            identifier54=identifier();

            state._fsp--;


             value = identifier54; 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:647:1: column_name returns [String value] : identifier ;
    public final String column_name() throws RecognitionException {
        String value = null;


        String identifier55 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:648:3: ( identifier )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:648:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_column_name1928);
            identifier55=identifier();

            state._fsp--;


             value = identifier55; 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:651:1: identifier returns [String value] : (t= regular_identifier |t= delimited_identifier ) ;
    public final String identifier() throws RecognitionException {
        String value = null;


        String t =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:652:3: ( (t= regular_identifier |t= delimited_identifier ) )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:652:5: (t= regular_identifier |t= delimited_identifier )
            {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:652:5: (t= regular_identifier |t= delimited_identifier )
            int alt48=2;
            int LA48_0 = input.LA(1);

            if ( (LA48_0==VARNAME) ) {
                alt48=1;
            }
            else if ( (LA48_0==STRING_WITH_QUOTE_DOUBLE) ) {
                alt48=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 48, 0, input);

                throw nvae;

            }
            switch (alt48) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:652:6: t= regular_identifier
                    {
                    pushFollow(FOLLOW_regular_identifier_in_identifier1952);
                    t=regular_identifier();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:652:29: t= delimited_identifier
                    {
                    pushFollow(FOLLOW_delimited_identifier_in_identifier1958);
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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:655:1: regular_identifier returns [String value] : VARNAME ;
    public final String regular_identifier() throws RecognitionException {
        String value = null;


        Token VARNAME56=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:656:3: ( VARNAME )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:656:5: VARNAME
            {
            VARNAME56=(Token)match(input,VARNAME,FOLLOW_VARNAME_in_regular_identifier1978); 

             value = (VARNAME56!=null?VARNAME56.getText():null); 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:659:1: delimited_identifier returns [String value] : STRING_WITH_QUOTE_DOUBLE ;
    public final String delimited_identifier() throws RecognitionException {
        String value = null;


        Token STRING_WITH_QUOTE_DOUBLE57=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:660:3: ( STRING_WITH_QUOTE_DOUBLE )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:660:5: STRING_WITH_QUOTE_DOUBLE
            {
            STRING_WITH_QUOTE_DOUBLE57=(Token)match(input,STRING_WITH_QUOTE_DOUBLE,FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_delimited_identifier1997); 

             
                  value = (STRING_WITH_QUOTE_DOUBLE57!=null?STRING_WITH_QUOTE_DOUBLE57.getText():null);
                  value = value.substring(1, value.length()-1);
                

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:666:1: general_literal returns [Literal value] : ( string_literal | boolean_literal );
    public final Literal general_literal() throws RecognitionException {
        Literal value = null;


        StringLiteral string_literal58 =null;

        BooleanLiteral boolean_literal59 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:667:3: ( string_literal | boolean_literal )
            int alt49=2;
            int LA49_0 = input.LA(1);

            if ( (LA49_0==STRING_WITH_QUOTE) ) {
                alt49=1;
            }
            else if ( (LA49_0==FALSE||LA49_0==TRUE) ) {
                alt49=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 49, 0, input);

                throw nvae;

            }
            switch (alt49) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:667:5: string_literal
                    {
                    pushFollow(FOLLOW_string_literal_in_general_literal2016);
                    string_literal58=string_literal();

                    state._fsp--;


                     value = string_literal58; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:668:5: boolean_literal
                    {
                    pushFollow(FOLLOW_boolean_literal_in_general_literal2024);
                    boolean_literal59=boolean_literal();

                    state._fsp--;


                     value = boolean_literal59; 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:671:1: string_literal returns [StringLiteral value] : STRING_WITH_QUOTE ;
    public final StringLiteral string_literal() throws RecognitionException {
        StringLiteral value = null;


        Token STRING_WITH_QUOTE60=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:672:3: ( STRING_WITH_QUOTE )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:672:5: STRING_WITH_QUOTE
            {
            STRING_WITH_QUOTE60=(Token)match(input,STRING_WITH_QUOTE,FOLLOW_STRING_WITH_QUOTE_in_string_literal2043); 


                  String str = (STRING_WITH_QUOTE60!=null?STRING_WITH_QUOTE60.getText():null);
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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:679:1: boolean_literal returns [BooleanLiteral value] : (t= TRUE |t= FALSE ) ;
    public final BooleanLiteral boolean_literal() throws RecognitionException {
        BooleanLiteral value = null;


        Token t=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:680:3: ( (t= TRUE |t= FALSE ) )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:680:5: (t= TRUE |t= FALSE )
            {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:680:5: (t= TRUE |t= FALSE )
            int alt50=2;
            int LA50_0 = input.LA(1);

            if ( (LA50_0==TRUE) ) {
                alt50=1;
            }
            else if ( (LA50_0==FALSE) ) {
                alt50=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 50, 0, input);

                throw nvae;

            }
            switch (alt50) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:680:6: t= TRUE
                    {
                    t=(Token)match(input,TRUE,FOLLOW_TRUE_in_boolean_literal2065); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:680:15: t= FALSE
                    {
                    t=(Token)match(input,FALSE,FOLLOW_FALSE_in_boolean_literal2071); 

                    }
                    break;

            }


             value = new BooleanLiteral(Boolean.getBoolean((t!=null?t.getText():null))); 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:683:1: numeric_literal returns [NumericLiteral value] : ( numeric_literal_unsigned | numeric_literal_positive | numeric_literal_negative );
    public final NumericLiteral numeric_literal() throws RecognitionException {
        NumericLiteral value = null;


        NumericLiteral numeric_literal_unsigned61 =null;

        NumericLiteral numeric_literal_positive62 =null;

        NumericLiteral numeric_literal_negative63 =null;


        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:684:3: ( numeric_literal_unsigned | numeric_literal_positive | numeric_literal_negative )
            int alt51=3;
            switch ( input.LA(1) ) {
            case DECIMAL:
            case INTEGER:
                {
                alt51=1;
                }
                break;
            case DECIMAL_POSITIVE:
            case INTEGER_POSITIVE:
                {
                alt51=2;
                }
                break;
            case DECIMAL_NEGATIVE:
            case INTEGER_NEGATIVE:
                {
                alt51=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 51, 0, input);

                throw nvae;

            }

            switch (alt51) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:684:5: numeric_literal_unsigned
                    {
                    pushFollow(FOLLOW_numeric_literal_unsigned_in_numeric_literal2091);
                    numeric_literal_unsigned61=numeric_literal_unsigned();

                    state._fsp--;


                     value = numeric_literal_unsigned61; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:685:5: numeric_literal_positive
                    {
                    pushFollow(FOLLOW_numeric_literal_positive_in_numeric_literal2099);
                    numeric_literal_positive62=numeric_literal_positive();

                    state._fsp--;


                     value = numeric_literal_positive62; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:686:5: numeric_literal_negative
                    {
                    pushFollow(FOLLOW_numeric_literal_negative_in_numeric_literal2107);
                    numeric_literal_negative63=numeric_literal_negative();

                    state._fsp--;


                     value = numeric_literal_negative63; 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:689:1: numeric_literal_unsigned returns [NumericLiteral value] : ( INTEGER | DECIMAL );
    public final NumericLiteral numeric_literal_unsigned() throws RecognitionException {
        NumericLiteral value = null;


        Token INTEGER64=null;
        Token DECIMAL65=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:690:3: ( INTEGER | DECIMAL )
            int alt52=2;
            int LA52_0 = input.LA(1);

            if ( (LA52_0==INTEGER) ) {
                alt52=1;
            }
            else if ( (LA52_0==DECIMAL) ) {
                alt52=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 52, 0, input);

                throw nvae;

            }
            switch (alt52) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:690:5: INTEGER
                    {
                    INTEGER64=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_numeric_literal_unsigned2126); 

                     value = new IntegerLiteral((INTEGER64!=null?INTEGER64.getText():null)); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:691:5: DECIMAL
                    {
                    DECIMAL65=(Token)match(input,DECIMAL,FOLLOW_DECIMAL_in_numeric_literal_unsigned2134); 

                     value = new DecimalLiteral((DECIMAL65!=null?DECIMAL65.getText():null)); 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:694:1: numeric_literal_positive returns [NumericLiteral value] : ( INTEGER_POSITIVE | DECIMAL_POSITIVE );
    public final NumericLiteral numeric_literal_positive() throws RecognitionException {
        NumericLiteral value = null;


        Token INTEGER_POSITIVE66=null;
        Token DECIMAL_POSITIVE67=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:695:3: ( INTEGER_POSITIVE | DECIMAL_POSITIVE )
            int alt53=2;
            int LA53_0 = input.LA(1);

            if ( (LA53_0==INTEGER_POSITIVE) ) {
                alt53=1;
            }
            else if ( (LA53_0==DECIMAL_POSITIVE) ) {
                alt53=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 53, 0, input);

                throw nvae;

            }
            switch (alt53) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:695:5: INTEGER_POSITIVE
                    {
                    INTEGER_POSITIVE66=(Token)match(input,INTEGER_POSITIVE,FOLLOW_INTEGER_POSITIVE_in_numeric_literal_positive2153); 

                     value = new IntegerLiteral((INTEGER_POSITIVE66!=null?INTEGER_POSITIVE66.getText():null)); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:696:5: DECIMAL_POSITIVE
                    {
                    DECIMAL_POSITIVE67=(Token)match(input,DECIMAL_POSITIVE,FOLLOW_DECIMAL_POSITIVE_in_numeric_literal_positive2161); 

                     value = new DecimalLiteral((DECIMAL_POSITIVE67!=null?DECIMAL_POSITIVE67.getText():null)); 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:699:1: numeric_literal_negative returns [NumericLiteral value] : ( INTEGER_NEGATIVE | DECIMAL_NEGATIVE );
    public final NumericLiteral numeric_literal_negative() throws RecognitionException {
        NumericLiteral value = null;


        Token INTEGER_NEGATIVE68=null;
        Token DECIMAL_NEGATIVE69=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:700:3: ( INTEGER_NEGATIVE | DECIMAL_NEGATIVE )
            int alt54=2;
            int LA54_0 = input.LA(1);

            if ( (LA54_0==INTEGER_NEGATIVE) ) {
                alt54=1;
            }
            else if ( (LA54_0==DECIMAL_NEGATIVE) ) {
                alt54=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 54, 0, input);

                throw nvae;

            }
            switch (alt54) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:700:5: INTEGER_NEGATIVE
                    {
                    INTEGER_NEGATIVE68=(Token)match(input,INTEGER_NEGATIVE,FOLLOW_INTEGER_NEGATIVE_in_numeric_literal_negative2182); 

                     value = new IntegerLiteral((INTEGER_NEGATIVE68!=null?INTEGER_NEGATIVE68.getText():null)); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:701:5: DECIMAL_NEGATIVE
                    {
                    DECIMAL_NEGATIVE69=(Token)match(input,DECIMAL_NEGATIVE,FOLLOW_DECIMAL_NEGATIVE_in_numeric_literal_negative2190); 

                     value = new DecimalLiteral((DECIMAL_NEGATIVE69!=null?DECIMAL_NEGATIVE69.getText():null)); 

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
    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:704:1: truth_value returns [boolean value] : (t= TRUE |t= FALSE ) ;
    public final boolean truth_value() throws RecognitionException {
        boolean value = false;


        Token t=null;

        try {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:705:3: ( (t= TRUE |t= FALSE ) )
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:705:5: (t= TRUE |t= FALSE )
            {
            // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:705:5: (t= TRUE |t= FALSE )
            int alt55=2;
            int LA55_0 = input.LA(1);

            if ( (LA55_0==TRUE) ) {
                alt55=1;
            }
            else if ( (LA55_0==FALSE) ) {
                alt55=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 55, 0, input);

                throw nvae;

            }
            switch (alt55) {
                case 1 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:705:6: t= TRUE
                    {
                    t=(Token)match(input,TRUE,FOLLOW_TRUE_in_truth_value2214); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\SQL99.g:705:15: t= FALSE
                    {
                    t=(Token)match(input,FALSE,FOLLOW_FALSE_in_truth_value2220); 

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

    // Delegated rules


 

    public static final BitSet FOLLOW_query_in_parse40 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_parse42 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_query_specification_in_query70 = new BitSet(new long[]{0x0000000000000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_UNION_in_query79 = new BitSet(new long[]{0x0000000008000010L,0x0000000000000020L});
    public static final BitSet FOLLOW_set_quantifier_in_query81 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_query_specification_in_query86 = new BitSet(new long[]{0x0000000000000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_SELECT_in_query_specification114 = new BitSet(new long[]{0x001A000108404210L,0x0000000000020D00L});
    public static final BitSet FOLLOW_set_quantifier_in_query_specification116 = new BitSet(new long[]{0x001A000100404200L,0x0000000000020D00L});
    public static final BitSet FOLLOW_select_list_in_query_specification119 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_table_expression_in_query_specification121 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALL_in_set_quantifier142 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DISTINCT_in_set_quantifier150 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_select_sublist_in_select_list178 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_select_list183 = new BitSet(new long[]{0x001A000100404200L,0x0000000000020D00L});
    public static final BitSet FOLLOW_select_sublist_in_select_list187 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_qualified_asterisk_in_select_sublist210 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_derived_column_in_select_sublist218 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_identifier_in_qualified_asterisk235 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_PERIOD_in_qualified_asterisk237 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_ASTERISK_in_qualified_asterisk239 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_derived_column263 = new BitSet(new long[]{0x0000000000000802L,0x0000000000020400L});
    public static final BitSet FOLLOW_AS_in_derived_column266 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_alias_name_in_derived_column269 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numeric_value_expression_in_value_expression293 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_string_value_expression_in_value_expression301 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_reference_value_expression_in_value_expression309 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_collection_value_expression_in_value_expression317 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_numeric_value_expression341 = new BitSet(new long[]{0x00001C0003800000L,0x0000000000020400L});
    public static final BitSet FOLLOW_numeric_operation_in_numeric_value_expression343 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_numeric_value_expression345 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_numeric_operation360 = new BitSet(new long[]{0x4020000000000002L});
    public static final BitSet FOLLOW_PLUS_in_numeric_operation378 = new BitSet(new long[]{0x00001C0003800000L,0x0000000000020400L});
    public static final BitSet FOLLOW_MINUS_in_numeric_operation382 = new BitSet(new long[]{0x00001C0003800000L,0x0000000000020400L});
    public static final BitSet FOLLOW_term_in_numeric_operation395 = new BitSet(new long[]{0x4020000000000002L});
    public static final BitSet FOLLOW_factor_in_term417 = new BitSet(new long[]{0x0000000000001002L,0x0000000000000080L});
    public static final BitSet FOLLOW_ASTERISK_in_term437 = new BitSet(new long[]{0x00001C0003800000L,0x0000000000020400L});
    public static final BitSet FOLLOW_SOLIDUS_in_term441 = new BitSet(new long[]{0x00001C0003800000L,0x0000000000020400L});
    public static final BitSet FOLLOW_factor_in_term455 = new BitSet(new long[]{0x0000000000001002L,0x0000000000000080L});
    public static final BitSet FOLLOW_column_reference_in_factor483 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numeric_literal_in_factor491 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_string_value_expression534 = new BitSet(new long[]{0x0000000400000000L,0x0000000000022600L});
    public static final BitSet FOLLOW_concatenation_in_string_value_expression536 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_string_value_expression538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_character_factor_in_concatenation557 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_CONCATENATION_in_concatenation569 = new BitSet(new long[]{0x0000000400000000L,0x0000000000022600L});
    public static final BitSet FOLLOW_character_factor_in_concatenation582 = new BitSet(new long[]{0x0000000000200002L});
    public static final BitSet FOLLOW_column_reference_in_character_factor603 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_general_literal_in_character_factor611 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_reference_value_expression635 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_identifier_in_column_reference657 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_PERIOD_in_column_reference659 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_column_name_in_column_reference663 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_function_specification_in_collection_value_expression691 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNT_in_set_function_specification706 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_set_function_specification708 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_ASTERISK_in_set_function_specification710 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_set_function_specification712 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_general_set_function_in_set_function_specification720 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_function_op_in_general_set_function735 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_general_set_function737 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_column_reference_in_general_set_function739 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_general_set_function741 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AVG_in_set_function_op765 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MAX_in_set_function_op771 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MIN_in_set_function_op777 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUM_in_set_function_op783 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EVERY_in_set_function_op789 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ANY_in_set_function_op795 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOME_in_set_function_op801 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNT_in_set_function_op807 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_row_value_expression829 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_row_value_expression837 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numeric_literal_in_literal856 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_general_literal_in_literal864 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_from_clause_in_table_expression883 = new BitSet(new long[]{0x0000004000000002L,0x0000000000040000L});
    public static final BitSet FOLLOW_where_clause_in_table_expression892 = new BitSet(new long[]{0x0000004000000002L});
    public static final BitSet FOLLOW_group_by_clause_in_table_expression904 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_from_clause927 = new BitSet(new long[]{0x0002000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_table_reference_list_in_from_clause929 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_reference_in_table_reference_list959 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_table_reference_list976 = new BitSet(new long[]{0x0002000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_table_reference_in_table_reference_list980 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_table_primary_in_table_reference1007 = new BitSet(new long[]{0x0000C21000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_joined_table_in_table_reference1016 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHERE_in_where_clause1038 = new BitSet(new long[]{0x001A1C0503C04200L,0x0000000000022F00L});
    public static final BitSet FOLLOW_search_condition_in_where_clause1040 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_value_expression_in_search_condition1059 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression1085 = new BitSet(new long[]{0x0200000000000002L});
    public static final BitSet FOLLOW_OR_in_boolean_value_expression1088 = new BitSet(new long[]{0x001A1C0503C04200L,0x0000000000022F00L});
    public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression1092 = new BitSet(new long[]{0x0200000000000002L});
    public static final BitSet FOLLOW_boolean_factor_in_boolean_term1111 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_AND_in_boolean_term1114 = new BitSet(new long[]{0x001A1C0503C04200L,0x0000000000022F00L});
    public static final BitSet FOLLOW_boolean_factor_in_boolean_term1118 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_predicate_in_boolean_factor1134 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparison_predicate_in_predicate1154 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_row_value_expression_in_comparison_predicate1179 = new BitSet(new long[]{0x0001002080000000L});
    public static final BitSet FOLLOW_comp_op_in_comparison_predicate1181 = new BitSet(new long[]{0x001A1C0503C04200L,0x0000000000022F00L});
    public static final BitSet FOLLOW_row_value_expression_in_comparison_predicate1185 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_comp_op1204 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_comp_op1212 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_GREATER_in_comp_op1214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_comp_op1222 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_comp_op1230 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_comp_op1238 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_EQUALS_in_comp_op1240 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_comp_op1248 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_EQUALS_in_comp_op1250 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_null_predicate1265 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_IS_in_null_predicate1267 = new BitSet(new long[]{0x00C0000000000000L});
    public static final BitSet FOLLOW_NOT_in_null_predicate1270 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_NULL_in_null_predicate1274 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_in_predicate1287 = new BitSet(new long[]{0x0040010000000000L});
    public static final BitSet FOLLOW_NOT_in_in_predicate1290 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_IN_in_in_predicate1294 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_in_predicate_value_in_in_predicate1296 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_subquery_in_in_predicate_value1311 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_in_predicate_value1317 = new BitSet(new long[]{0x001A1C0503C04200L,0x0000000000022F00L});
    public static final BitSet FOLLOW_in_value_list_in_in_predicate_value1319 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_in_predicate_value1321 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_subquery_in_table_subquery1334 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_subquery1347 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_query_in_subquery1349 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_subquery1351 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_row_value_expression_in_in_value_list1366 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_in_value_list1369 = new BitSet(new long[]{0x001A1C0503C04200L,0x0000000000022F00L});
    public static final BitSet FOLLOW_row_value_expression_in_in_value_list1371 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_GROUP_in_group_by_clause1390 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_BY_in_group_by_clause1392 = new BitSet(new long[]{0x0002000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_grouping_element_list_in_group_by_clause1394 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_grouping_element_in_grouping_element_list1420 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_grouping_element_list1430 = new BitSet(new long[]{0x0002000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_grouping_element_in_grouping_element_list1434 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_grouping_column_reference_in_grouping_element1462 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_grouping_element1470 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_grouping_column_reference_list_in_grouping_element1472 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_grouping_element1474 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference1495 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference_list1523 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_grouping_column_reference_list1532 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference_list1536 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_join_type_in_joined_table1566 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_JOIN_in_joined_table1572 = new BitSet(new long[]{0x0002000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_table_reference_in_joined_table1574 = new BitSet(new long[]{0x0100000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_join_specification_in_joined_table1576 = new BitSet(new long[]{0x0000C21000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_INNER_in_join_type1612 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_outer_join_type_in_join_type1620 = new BitSet(new long[]{0x0800000000000002L});
    public static final BitSet FOLLOW_OUTER_in_join_type1623 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_in_outer_join_type1648 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RIGHT_in_outer_join_type1656 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FULL_in_outer_join_type1664 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_join_condition_in_join_specification1683 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_named_columns_join_in_join_specification1691 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ON_in_join_condition1708 = new BitSet(new long[]{0x001A1C0503C04200L,0x0000000000022F00L});
    public static final BitSet FOLLOW_search_condition_in_join_condition1710 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_USING_in_named_columns_join1725 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_named_columns_join1727 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_join_column_list_in_named_columns_join1729 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_named_columns_join1731 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_name_in_join_column_list1744 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_join_column_list1747 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_column_name_in_join_column_list1749 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_table_name_in_table_primary1769 = new BitSet(new long[]{0x0000000000000802L,0x0000000000020400L});
    public static final BitSet FOLLOW_AS_in_table_primary1776 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_alias_name_in_table_primary1779 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_derived_table_in_table_primary1789 = new BitSet(new long[]{0x0000000000000800L,0x0000000000020400L});
    public static final BitSet FOLLOW_AS_in_table_primary1795 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_alias_name_in_table_primary1798 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_schema_name_in_table_name1820 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_PERIOD_in_table_name1822 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_table_identifier_in_table_name1826 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_alias_name1847 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_subquery_in_derived_table1863 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_table_identifier1884 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_schema_name1905 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_column_name1928 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_regular_identifier_in_identifier1952 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_delimited_identifier_in_identifier1958 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VARNAME_in_regular_identifier1978 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_delimited_identifier1997 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_string_literal_in_general_literal2016 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_literal_in_general_literal2024 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_WITH_QUOTE_in_string_literal2043 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_boolean_literal2065 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_boolean_literal2071 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numeric_literal_unsigned_in_numeric_literal2091 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numeric_literal_positive_in_numeric_literal2099 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numeric_literal_negative_in_numeric_literal2107 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_in_numeric_literal_unsigned2126 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_in_numeric_literal_unsigned2134 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_POSITIVE_in_numeric_literal_positive2153 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_POSITIVE_in_numeric_literal_positive2161 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_NEGATIVE_in_numeric_literal_negative2182 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_NEGATIVE_in_numeric_literal_negative2190 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_truth_value2214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_truth_value2220 = new BitSet(new long[]{0x0000000000000002L});

}