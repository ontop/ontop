// $ANTLR 3.4 /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g 2012-01-02 22:59:12

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
    public String getGrammarFileName() { return "/Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g"; }


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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:176:1: parse returns [QueryTree value] : query EOF ;
    public final QueryTree parse() throws RecognitionException {
        QueryTree value = null;


        QueryTree query1 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:177:3: ( query EOF )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:177:5: query EOF
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:182:1: query returns [QueryTree value] : query_specification ;
    public final QueryTree query() throws RecognitionException {
        QueryTree value = null;


        QueryTree query_specification2 =null;



        int quantifier = 0;

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:186:3: ( query_specification )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:186:5: query_specification
            {
            pushFollow(FOLLOW_query_specification_in_query68);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:207:1: query_specification returns [QueryTree value] : SELECT ( set_quantifier )? select_list table_expression ;
    public final QueryTree query_specification() throws RecognitionException {
        QueryTree value = null;


        TableExpression table_expression3 =null;

        ArrayList<DerivedColumn> select_list4 =null;

        int set_quantifier5 =0;



        int quantifier = 0;

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:211:3: ( SELECT ( set_quantifier )? select_list table_expression )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:211:5: SELECT ( set_quantifier )? select_list table_expression
            {
            match(input,SELECT,FOLLOW_SELECT_in_query_specification109); 

            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:211:12: ( set_quantifier )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==ALL||LA1_0==DISTINCT) ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:211:12: set_quantifier
                    {
                    pushFollow(FOLLOW_set_quantifier_in_query_specification111);
                    set_quantifier5=set_quantifier();

                    state._fsp--;


                    }
                    break;

            }


            pushFollow(FOLLOW_select_list_in_query_specification114);
            select_list4=select_list();

            state._fsp--;


            pushFollow(FOLLOW_table_expression_in_query_specification116);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:255:1: set_quantifier returns [int value] : ( ALL | DISTINCT );
    public final int set_quantifier() throws RecognitionException {
        int value = 0;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:256:3: ( ALL | DISTINCT )
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
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:256:5: ALL
                    {
                    match(input,ALL,FOLLOW_ALL_in_set_quantifier137); 

                     value = 1; 

                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:257:5: DISTINCT
                    {
                    match(input,DISTINCT,FOLLOW_DISTINCT_in_set_quantifier145); 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:260:1: select_list returns [ArrayList<DerivedColumn> value] : a= select_sublist ( COMMA b= select_sublist )* ;
    public final ArrayList<DerivedColumn> select_list() throws RecognitionException {
        ArrayList<DerivedColumn> value = null;


        DerivedColumn a =null;

        DerivedColumn b =null;



          value = new ArrayList<DerivedColumn>();

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:264:3: (a= select_sublist ( COMMA b= select_sublist )* )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:264:5: a= select_sublist ( COMMA b= select_sublist )*
            {
            pushFollow(FOLLOW_select_sublist_in_select_list173);
            a=select_sublist();

            state._fsp--;


             value.add(a); 

            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:264:48: ( COMMA b= select_sublist )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==COMMA) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:264:49: COMMA b= select_sublist
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_select_list178); 

            	    pushFollow(FOLLOW_select_sublist_in_select_list182);
            	    b=select_sublist();

            	    state._fsp--;


            	     value.add(b); 

            	    }
            	    break;

            	default :
            	    break loop3;
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:267:1: select_sublist returns [DerivedColumn value] : derived_column ;
    public final DerivedColumn select_sublist() throws RecognitionException {
        DerivedColumn value = null;


        DerivedColumn derived_column6 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:268:3: ( derived_column )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:268:5: derived_column
            {
            pushFollow(FOLLOW_derived_column_in_select_sublist205);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:272:1: qualified_asterisk : table_identifier PERIOD ASTERISK ;
    public final void qualified_asterisk() throws RecognitionException {
        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:273:3: ( table_identifier PERIOD ASTERISK )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:273:5: table_identifier PERIOD ASTERISK
            {
            pushFollow(FOLLOW_table_identifier_in_qualified_asterisk223);
            table_identifier();

            state._fsp--;


            match(input,PERIOD,FOLLOW_PERIOD_in_qualified_asterisk225); 

            match(input,ASTERISK,FOLLOW_ASTERISK_in_qualified_asterisk227); 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:276:1: derived_column returns [DerivedColumn value] : value_expression ( ( AS )? alias_name )? ;
    public final DerivedColumn derived_column() throws RecognitionException {
        DerivedColumn value = null;


        AbstractValueExpression value_expression7 =null;

        String alias_name8 =null;



          value = new DerivedColumn();

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:280:3: ( value_expression ( ( AS )? alias_name )? )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:280:5: value_expression ( ( AS )? alias_name )?
            {
            pushFollow(FOLLOW_value_expression_in_derived_column251);
            value_expression7=value_expression();

            state._fsp--;


            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:280:22: ( ( AS )? alias_name )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==AS||LA5_0==STRING_WITH_QUOTE_DOUBLE||LA5_0==VARNAME) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:280:23: ( AS )? alias_name
                    {
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:280:23: ( AS )?
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( (LA4_0==AS) ) {
                        alt4=1;
                    }
                    switch (alt4) {
                        case 1 :
                            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:280:23: AS
                            {
                            match(input,AS,FOLLOW_AS_in_derived_column254); 

                            }
                            break;

                    }


                    pushFollow(FOLLOW_alias_name_in_derived_column257);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:289:1: value_expression returns [AbstractValueExpression value] : reference_value_expression ;
    public final AbstractValueExpression value_expression() throws RecognitionException {
        AbstractValueExpression value = null;


        ReferenceValueExpression reference_value_expression9 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:290:3: ( reference_value_expression )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:290:5: reference_value_expression
            {
            pushFollow(FOLLOW_reference_value_expression_in_value_expression281);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:296:1: numeric_value_expression returns [NumericValueExpression value] : LPAREN numeric_operation RPAREN ;
    public final NumericValueExpression numeric_value_expression() throws RecognitionException {
        NumericValueExpression value = null;



          numericExp = new NumericValueExpression();

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:300:3: ( LPAREN numeric_operation RPAREN )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:300:5: LPAREN numeric_operation RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_numeric_value_expression308); 

            pushFollow(FOLLOW_numeric_operation_in_numeric_value_expression310);
            numeric_operation();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_numeric_value_expression312); 


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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:305:1: numeric_operation : term ( (t= PLUS |t= MINUS ) term )* ;
    public final void numeric_operation() throws RecognitionException {
        Token t=null;

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:306:3: ( term ( (t= PLUS |t= MINUS ) term )* )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:306:5: term ( (t= PLUS |t= MINUS ) term )*
            {
            pushFollow(FOLLOW_term_in_numeric_operation327);
            term();

            state._fsp--;


            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:307:5: ( (t= PLUS |t= MINUS ) term )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==MINUS||LA7_0==PLUS) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:308:7: (t= PLUS |t= MINUS ) term
            	    {
            	    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:308:7: (t= PLUS |t= MINUS )
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
            	            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:308:8: t= PLUS
            	            {
            	            t=(Token)match(input,PLUS,FOLLOW_PLUS_in_numeric_operation345); 

            	            }
            	            break;
            	        case 2 :
            	            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:308:15: t= MINUS
            	            {
            	            t=(Token)match(input,MINUS,FOLLOW_MINUS_in_numeric_operation349); 

            	            }
            	            break;

            	    }


            	     numericExp.putSpecification((t!=null?t.getText():null)); 

            	    pushFollow(FOLLOW_term_in_numeric_operation362);
            	    term();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop7;
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:313:1: term : a= factor ( (t= ASTERISK |t= SOLIDUS ) b= factor )* ;
    public final void term() throws RecognitionException {
        Token t=null;
        Object a =null;

        Object b =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:314:3: (a= factor ( (t= ASTERISK |t= SOLIDUS ) b= factor )* )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:314:5: a= factor ( (t= ASTERISK |t= SOLIDUS ) b= factor )*
            {
            pushFollow(FOLLOW_factor_in_term384);
            a=factor();

            state._fsp--;


             numericExp.putSpecification(a); 

            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:315:5: ( (t= ASTERISK |t= SOLIDUS ) b= factor )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==ASTERISK||LA9_0==SOLIDUS) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:316:7: (t= ASTERISK |t= SOLIDUS ) b= factor
            	    {
            	    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:316:7: (t= ASTERISK |t= SOLIDUS )
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
            	            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:316:8: t= ASTERISK
            	            {
            	            t=(Token)match(input,ASTERISK,FOLLOW_ASTERISK_in_term404); 

            	            }
            	            break;
            	        case 2 :
            	            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:316:19: t= SOLIDUS
            	            {
            	            t=(Token)match(input,SOLIDUS,FOLLOW_SOLIDUS_in_term408); 

            	            }
            	            break;

            	    }


            	     numericExp.putSpecification((t!=null?t.getText():null)); 

            	    pushFollow(FOLLOW_factor_in_term422);
            	    b=factor();

            	    state._fsp--;


            	     numericExp.putSpecification(b); 

            	    }
            	    break;

            	default :
            	    break loop9;
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:321:1: factor returns [Object value] : ( column_reference | numeric_literal );
    public final Object factor() throws RecognitionException {
        Object value = null;


        ColumnReference column_reference10 =null;

        NumericLiteral numeric_literal11 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:322:3: ( column_reference | numeric_literal )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==STRING_WITH_QUOTE_DOUBLE||LA10_0==VARNAME) ) {
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
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:322:5: column_reference
                    {
                    pushFollow(FOLLOW_column_reference_in_factor450);
                    column_reference10=column_reference();

                    state._fsp--;


                     value = column_reference10; 

                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:323:5: numeric_literal
                    {
                    pushFollow(FOLLOW_numeric_literal_in_factor458);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:326:1: sign : ( PLUS | MINUS );
    public final void sign() throws RecognitionException {
        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:327:3: ( PLUS | MINUS )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:331:1: string_value_expression returns [StringValueExpression value] : LPAREN concatenation RPAREN ;
    public final StringValueExpression string_value_expression() throws RecognitionException {
        StringValueExpression value = null;



          stringExp = new StringValueExpression();

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:335:3: ( LPAREN concatenation RPAREN )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:335:5: LPAREN concatenation RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_string_value_expression501); 

            pushFollow(FOLLOW_concatenation_in_string_value_expression503);
            concatenation();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_string_value_expression505); 


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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:340:1: concatenation : a= character_factor ( CONCATENATION b= character_factor )+ ;
    public final void concatenation() throws RecognitionException {
        Object a =null;

        Object b =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:341:3: (a= character_factor ( CONCATENATION b= character_factor )+ )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:341:5: a= character_factor ( CONCATENATION b= character_factor )+
            {
            pushFollow(FOLLOW_character_factor_in_concatenation524);
            a=character_factor();

            state._fsp--;


             stringExp.putSpecification(a); 

            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:341:66: ( CONCATENATION b= character_factor )+
            int cnt11=0;
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0==CONCATENATION) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:342:7: CONCATENATION b= character_factor
            	    {
            	    match(input,CONCATENATION,FOLLOW_CONCATENATION_in_concatenation536); 

            	     stringExp.putSpecification(StringValueExpression.CONCAT_OP); 

            	    pushFollow(FOLLOW_character_factor_in_concatenation549);
            	    b=character_factor();

            	    state._fsp--;


            	     stringExp.putSpecification(b); 

            	    }
            	    break;

            	default :
            	    if ( cnt11 >= 1 ) break loop11;
                        EarlyExitException eee =
                            new EarlyExitException(11, input);
                        throw eee;
                }
                cnt11++;
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:346:1: character_factor returns [Object value] : ( column_reference | general_literal );
    public final Object character_factor() throws RecognitionException {
        Object value = null;


        ColumnReference column_reference12 =null;

        Literal general_literal13 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:347:3: ( column_reference | general_literal )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==STRING_WITH_QUOTE_DOUBLE||LA12_0==VARNAME) ) {
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
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:347:5: column_reference
                    {
                    pushFollow(FOLLOW_column_reference_in_character_factor570);
                    column_reference12=column_reference();

                    state._fsp--;


                     value = column_reference12; 

                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:348:5: general_literal
                    {
                    pushFollow(FOLLOW_general_literal_in_character_factor578);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:351:1: reference_value_expression returns [ReferenceValueExpression value] : column_reference ;
    public final ReferenceValueExpression reference_value_expression() throws RecognitionException {
        ReferenceValueExpression value = null;


        ColumnReference column_reference14 =null;



          referenceExp = new ReferenceValueExpression();

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:355:3: ( column_reference )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:355:5: column_reference
            {
            pushFollow(FOLLOW_column_reference_in_reference_value_expression602);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:361:1: column_reference returns [ColumnReference value] : (t= table_identifier PERIOD )? column_name ;
    public final ColumnReference column_reference() throws RecognitionException {
        ColumnReference value = null;


        String t =null;

        String column_name15 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:362:3: ( (t= table_identifier PERIOD )? column_name )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:362:5: (t= table_identifier PERIOD )? column_name
            {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:362:5: (t= table_identifier PERIOD )?
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==VARNAME) ) {
                int LA13_1 = input.LA(2);

                if ( (LA13_1==PERIOD) ) {
                    alt13=1;
                }
            }
            else if ( (LA13_0==STRING_WITH_QUOTE_DOUBLE) ) {
                int LA13_2 = input.LA(2);

                if ( (LA13_2==PERIOD) ) {
                    alt13=1;
                }
            }
            switch (alt13) {
                case 1 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:362:6: t= table_identifier PERIOD
                    {
                    pushFollow(FOLLOW_table_identifier_in_column_reference624);
                    t=table_identifier();

                    state._fsp--;


                    match(input,PERIOD,FOLLOW_PERIOD_in_column_reference626); 

                    }
                    break;

            }


            pushFollow(FOLLOW_column_name_in_column_reference630);
            column_name15=column_name();

            state._fsp--;



                  String table = "";
                  if (t != null) {
                    table = t;
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:371:1: collection_value_expression returns [CollectionValueExpression value] : set_function_specification ;
    public final CollectionValueExpression collection_value_expression() throws RecognitionException {
        CollectionValueExpression value = null;



          collectionExp = new CollectionValueExpression();

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:375:3: ( set_function_specification )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:375:5: set_function_specification
            {
            pushFollow(FOLLOW_set_function_specification_in_collection_value_expression658);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:380:1: set_function_specification : ( COUNT LPAREN ASTERISK RPAREN | general_set_function );
    public final void set_function_specification() throws RecognitionException {
        Token COUNT16=null;
        Token ASTERISK17=null;

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:381:3: ( COUNT LPAREN ASTERISK RPAREN | general_set_function )
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==COUNT) ) {
                int LA14_1 = input.LA(2);

                if ( (LA14_1==LPAREN) ) {
                    int LA14_3 = input.LA(3);

                    if ( (LA14_3==ASTERISK) ) {
                        alt14=1;
                    }
                    else if ( (LA14_3==STRING_WITH_QUOTE_DOUBLE||LA14_3==VARNAME) ) {
                        alt14=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 14, 3, input);

                        throw nvae;

                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 14, 1, input);

                    throw nvae;

                }
            }
            else if ( (LA14_0==ANY||LA14_0==AVG||LA14_0==EVERY||(LA14_0 >= MAX && LA14_0 <= MIN)||LA14_0==SOME||LA14_0==SUM) ) {
                alt14=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;

            }
            switch (alt14) {
                case 1 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:381:5: COUNT LPAREN ASTERISK RPAREN
                    {
                    COUNT16=(Token)match(input,COUNT,FOLLOW_COUNT_in_set_function_specification673); 

                    match(input,LPAREN,FOLLOW_LPAREN_in_set_function_specification675); 

                    ASTERISK17=(Token)match(input,ASTERISK,FOLLOW_ASTERISK_in_set_function_specification677); 

                    match(input,RPAREN,FOLLOW_RPAREN_in_set_function_specification679); 


                          collectionExp.putSpecification((COUNT16!=null?COUNT16.getText():null));
                          collectionExp.putSpecification((ASTERISK17!=null?ASTERISK17.getText():null));
                        

                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:385:5: general_set_function
                    {
                    pushFollow(FOLLOW_general_set_function_in_set_function_specification687);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:389:1: general_set_function : set_function_op LPAREN column_reference RPAREN ;
    public final void general_set_function() throws RecognitionException {
        String set_function_op18 =null;

        ColumnReference column_reference19 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:390:3: ( set_function_op LPAREN column_reference RPAREN )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:390:5: set_function_op LPAREN column_reference RPAREN
            {
            pushFollow(FOLLOW_set_function_op_in_general_set_function702);
            set_function_op18=set_function_op();

            state._fsp--;


            match(input,LPAREN,FOLLOW_LPAREN_in_general_set_function704); 

            pushFollow(FOLLOW_column_reference_in_general_set_function706);
            column_reference19=column_reference();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_general_set_function708); 


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
        return ;
    }
    // $ANTLR end "general_set_function"



    // $ANTLR start "set_function_op"
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:396:1: set_function_op returns [String value] : (t= AVG |t= MAX |t= MIN |t= SUM |t= EVERY |t= ANY |t= SOME |t= COUNT ) ;
    public final String set_function_op() throws RecognitionException {
        String value = null;


        Token t=null;

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:397:3: ( (t= AVG |t= MAX |t= MIN |t= SUM |t= EVERY |t= ANY |t= SOME |t= COUNT ) )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:397:5: (t= AVG |t= MAX |t= MIN |t= SUM |t= EVERY |t= ANY |t= SOME |t= COUNT )
            {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:397:5: (t= AVG |t= MAX |t= MIN |t= SUM |t= EVERY |t= ANY |t= SOME |t= COUNT )
            int alt15=8;
            switch ( input.LA(1) ) {
            case AVG:
                {
                alt15=1;
                }
                break;
            case MAX:
                {
                alt15=2;
                }
                break;
            case MIN:
                {
                alt15=3;
                }
                break;
            case SUM:
                {
                alt15=4;
                }
                break;
            case EVERY:
                {
                alt15=5;
                }
                break;
            case ANY:
                {
                alt15=6;
                }
                break;
            case SOME:
                {
                alt15=7;
                }
                break;
            case COUNT:
                {
                alt15=8;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;

            }

            switch (alt15) {
                case 1 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:397:6: t= AVG
                    {
                    t=(Token)match(input,AVG,FOLLOW_AVG_in_set_function_op732); 

                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:397:14: t= MAX
                    {
                    t=(Token)match(input,MAX,FOLLOW_MAX_in_set_function_op738); 

                    }
                    break;
                case 3 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:397:22: t= MIN
                    {
                    t=(Token)match(input,MIN,FOLLOW_MIN_in_set_function_op744); 

                    }
                    break;
                case 4 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:397:30: t= SUM
                    {
                    t=(Token)match(input,SUM,FOLLOW_SUM_in_set_function_op750); 

                    }
                    break;
                case 5 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:397:38: t= EVERY
                    {
                    t=(Token)match(input,EVERY,FOLLOW_EVERY_in_set_function_op756); 

                    }
                    break;
                case 6 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:397:48: t= ANY
                    {
                    t=(Token)match(input,ANY,FOLLOW_ANY_in_set_function_op762); 

                    }
                    break;
                case 7 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:397:56: t= SOME
                    {
                    t=(Token)match(input,SOME,FOLLOW_SOME_in_set_function_op768); 

                    }
                    break;
                case 8 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:397:65: t= COUNT
                    {
                    t=(Token)match(input,COUNT,FOLLOW_COUNT_in_set_function_op774); 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:402:1: row_value_expression returns [IValueExpression value] : ( literal | value_expression );
    public final IValueExpression row_value_expression() throws RecognitionException {
        IValueExpression value = null;


        Literal literal20 =null;

        AbstractValueExpression value_expression21 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:403:3: ( literal | value_expression )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( ((LA16_0 >= DECIMAL && LA16_0 <= DECIMAL_POSITIVE)||LA16_0==FALSE||(LA16_0 >= INTEGER && LA16_0 <= INTEGER_POSITIVE)||LA16_0==STRING_WITH_QUOTE||LA16_0==TRUE) ) {
                alt16=1;
            }
            else if ( (LA16_0==STRING_WITH_QUOTE_DOUBLE||LA16_0==VARNAME) ) {
                alt16=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;

            }
            switch (alt16) {
                case 1 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:403:5: literal
                    {
                    pushFollow(FOLLOW_literal_in_row_value_expression796);
                    literal20=literal();

                    state._fsp--;


                     value = literal20; 

                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:404:5: value_expression
                    {
                    pushFollow(FOLLOW_value_expression_in_row_value_expression804);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:407:1: literal returns [Literal value] : ( numeric_literal | general_literal );
    public final Literal literal() throws RecognitionException {
        Literal value = null;


        NumericLiteral numeric_literal22 =null;

        Literal general_literal23 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:408:3: ( numeric_literal | general_literal )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( ((LA17_0 >= DECIMAL && LA17_0 <= DECIMAL_POSITIVE)||(LA17_0 >= INTEGER && LA17_0 <= INTEGER_POSITIVE)) ) {
                alt17=1;
            }
            else if ( (LA17_0==FALSE||LA17_0==STRING_WITH_QUOTE||LA17_0==TRUE) ) {
                alt17=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;

            }
            switch (alt17) {
                case 1 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:408:5: numeric_literal
                    {
                    pushFollow(FOLLOW_numeric_literal_in_literal823);
                    numeric_literal22=numeric_literal();

                    state._fsp--;


                     value = numeric_literal22; 

                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:409:5: general_literal
                    {
                    pushFollow(FOLLOW_general_literal_in_literal831);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:412:1: table_expression returns [TableExpression value] : from_clause ( where_clause )? ;
    public final TableExpression table_expression() throws RecognitionException {
        TableExpression value = null;


        ArrayList<TablePrimary> from_clause24 =null;

        BooleanValueExpression where_clause25 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:413:3: ( from_clause ( where_clause )? )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:413:5: from_clause ( where_clause )?
            {
            pushFollow(FOLLOW_from_clause_in_table_expression850);
            from_clause24=from_clause();

            state._fsp--;



                  value = new TableExpression(from_clause24);
                

            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:416:5: ( where_clause )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==WHERE) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:416:6: where_clause
                    {
                    pushFollow(FOLLOW_where_clause_in_table_expression859);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:420:1: from_clause returns [ArrayList<TablePrimary> value] : FROM table_reference_list ;
    public final ArrayList<TablePrimary> from_clause() throws RecognitionException {
        ArrayList<TablePrimary> value = null;


        ArrayList<TablePrimary> table_reference_list26 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:421:3: ( FROM table_reference_list )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:421:5: FROM table_reference_list
            {
            match(input,FROM,FOLLOW_FROM_in_from_clause884); 

            pushFollow(FOLLOW_table_reference_list_in_from_clause886);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:426:1: table_reference_list returns [ArrayList<TablePrimary> value] : a= table_reference ( COMMA b= table_reference )* ;
    public final ArrayList<TablePrimary> table_reference_list() throws RecognitionException {
        ArrayList<TablePrimary> value = null;


        TablePrimary a =null;

        TablePrimary b =null;



          value = new ArrayList<TablePrimary>();

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:430:3: (a= table_reference ( COMMA b= table_reference )* )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:430:5: a= table_reference ( COMMA b= table_reference )*
            {
            pushFollow(FOLLOW_table_reference_in_table_reference_list916);
            a=table_reference();

            state._fsp--;


             value.add(a); 

            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:431:5: ( COMMA b= table_reference )*
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( (LA19_0==COMMA) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:432:7: COMMA b= table_reference
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_table_reference_list933); 

            	    pushFollow(FOLLOW_table_reference_in_table_reference_list937);
            	    b=table_reference();

            	    state._fsp--;



            	            JoinOperator joinOp = new JoinOperator(JoinOperator.CROSS_JOIN);
            	            relationStack.push(joinOp);
            	            
            	            value.add(b);
            	          

            	    }
            	    break;

            	default :
            	    break loop19;
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:440:1: table_reference returns [TablePrimary value] : table_primary ( joined_table )? ;
    public final TablePrimary table_reference() throws RecognitionException {
        TablePrimary value = null;


        TablePrimary table_primary27 =null;

        TablePrimary joined_table28 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:441:3: ( table_primary ( joined_table )? )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:441:5: table_primary ( joined_table )?
            {
            pushFollow(FOLLOW_table_primary_in_table_reference964);
            table_primary27=table_primary();

            state._fsp--;


             value = table_primary27; 

            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:442:5: ( joined_table )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==FULL||LA20_0==INNER||(LA20_0 >= JOIN && LA20_0 <= LEFT)||LA20_0==RIGHT) ) {
                alt20=1;
            }
            switch (alt20) {
                case 1 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:442:6: joined_table
                    {
                    pushFollow(FOLLOW_joined_table_in_table_reference973);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:445:1: where_clause returns [BooleanValueExpression value] : WHERE search_condition ;
    public final BooleanValueExpression where_clause() throws RecognitionException {
        BooleanValueExpression value = null;


        BooleanValueExpression search_condition29 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:446:3: ( WHERE search_condition )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:446:5: WHERE search_condition
            {
            match(input,WHERE,FOLLOW_WHERE_in_where_clause995); 

            pushFollow(FOLLOW_search_condition_in_where_clause997);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:451:1: search_condition returns [BooleanValueExpression value] : boolean_value_expression ;
    public final BooleanValueExpression search_condition() throws RecognitionException {
        BooleanValueExpression value = null;


        BooleanValueExpression boolean_value_expression30 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:452:3: ( boolean_value_expression )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:452:5: boolean_value_expression
            {
            pushFollow(FOLLOW_boolean_value_expression_in_search_condition1016);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:457:1: boolean_value_expression returns [BooleanValueExpression value] : boolean_term ( OR boolean_term )* ;
    public final BooleanValueExpression boolean_value_expression() throws RecognitionException {
        BooleanValueExpression value = null;



          booleanExp = new BooleanValueExpression();

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:461:3: ( boolean_term ( OR boolean_term )* )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:465:5: boolean_term ( OR boolean_term )*
            {
            pushFollow(FOLLOW_boolean_term_in_boolean_value_expression1052);
            boolean_term();

            state._fsp--;


            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:465:18: ( OR boolean_term )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( (LA21_0==OR) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:465:19: OR boolean_term
            	    {
            	    match(input,OR,FOLLOW_OR_in_boolean_value_expression1055); 

            	    booleanExp.putSpecification(new OrOperator()); 

            	    pushFollow(FOLLOW_boolean_term_in_boolean_value_expression1059);
            	    boolean_term();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop21;
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:469:1: boolean_term : boolean_factor ( AND boolean_factor )* ;
    public final void boolean_term() throws RecognitionException {
        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:470:3: ( boolean_factor ( AND boolean_factor )* )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:470:5: boolean_factor ( AND boolean_factor )*
            {
            pushFollow(FOLLOW_boolean_factor_in_boolean_term1082);
            boolean_factor();

            state._fsp--;


            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:470:20: ( AND boolean_factor )*
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( (LA22_0==AND) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:470:21: AND boolean_factor
            	    {
            	    match(input,AND,FOLLOW_AND_in_boolean_term1085); 

            	     booleanExp.putSpecification(new AndOperator()); 

            	    pushFollow(FOLLOW_boolean_factor_in_boolean_term1089);
            	    boolean_factor();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop22;
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:474:1: boolean_factor : predicate ;
    public final void boolean_factor() throws RecognitionException {
        IPredicate predicate31 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:475:3: ( predicate )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:475:5: predicate
            {
            pushFollow(FOLLOW_predicate_in_boolean_factor1105);
            predicate31=predicate();

            state._fsp--;


             booleanExp.putSpecification(predicate31); 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:478:1: predicate returns [IPredicate value] : comparison_predicate ;
    public final IPredicate predicate() throws RecognitionException {
        IPredicate value = null;


        ComparisonPredicate comparison_predicate32 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:479:3: ( comparison_predicate )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:479:5: comparison_predicate
            {
            pushFollow(FOLLOW_comparison_predicate_in_predicate1125);
            comparison_predicate32=comparison_predicate();

            state._fsp--;


             value = comparison_predicate32; 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:484:1: comparison_predicate returns [ComparisonPredicate value] : a= row_value_expression comp_op b= row_value_expression ;
    public final ComparisonPredicate comparison_predicate() throws RecognitionException {
        ComparisonPredicate value = null;


        IValueExpression a =null;

        IValueExpression b =null;

        ComparisonPredicate.Operator comp_op33 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:485:3: (a= row_value_expression comp_op b= row_value_expression )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:485:5: a= row_value_expression comp_op b= row_value_expression
            {
            pushFollow(FOLLOW_row_value_expression_in_comparison_predicate1150);
            a=row_value_expression();

            state._fsp--;


            pushFollow(FOLLOW_comp_op_in_comparison_predicate1152);
            comp_op33=comp_op();

            state._fsp--;


            pushFollow(FOLLOW_row_value_expression_in_comparison_predicate1156);
            b=row_value_expression();

            state._fsp--;



                  value = new ComparisonPredicate(a, b, comp_op33);
                

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:490:1: comp_op returns [ComparisonPredicate.Operator value] : ( EQUALS | LESS GREATER | LESS | GREATER | LESS EQUALS | GREATER EQUALS );
    public final ComparisonPredicate.Operator comp_op() throws RecognitionException {
        ComparisonPredicate.Operator value = null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:491:3: ( EQUALS | LESS GREATER | LESS | GREATER | LESS EQUALS | GREATER EQUALS )
            int alt23=6;
            switch ( input.LA(1) ) {
            case EQUALS:
                {
                alt23=1;
                }
                break;
            case LESS:
                {
                switch ( input.LA(2) ) {
                case GREATER:
                    {
                    alt23=2;
                    }
                    break;
                case EQUALS:
                    {
                    alt23=5;
                    }
                    break;
                case DECIMAL:
                case DECIMAL_NEGATIVE:
                case DECIMAL_POSITIVE:
                case FALSE:
                case INTEGER:
                case INTEGER_NEGATIVE:
                case INTEGER_POSITIVE:
                case STRING_WITH_QUOTE:
                case STRING_WITH_QUOTE_DOUBLE:
                case TRUE:
                case VARNAME:
                    {
                    alt23=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 23, 2, input);

                    throw nvae;

                }

                }
                break;
            case GREATER:
                {
                int LA23_3 = input.LA(2);

                if ( (LA23_3==EQUALS) ) {
                    alt23=6;
                }
                else if ( ((LA23_3 >= DECIMAL && LA23_3 <= DECIMAL_POSITIVE)||LA23_3==FALSE||(LA23_3 >= INTEGER && LA23_3 <= INTEGER_POSITIVE)||(LA23_3 >= STRING_WITH_QUOTE && LA23_3 <= STRING_WITH_QUOTE_DOUBLE)||LA23_3==TRUE||LA23_3==VARNAME) ) {
                    alt23=4;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 23, 3, input);

                    throw nvae;

                }
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 23, 0, input);

                throw nvae;

            }

            switch (alt23) {
                case 1 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:491:5: EQUALS
                    {
                    match(input,EQUALS,FOLLOW_EQUALS_in_comp_op1175); 

                     value = ComparisonPredicate.Operator.EQ; 

                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:492:5: LESS GREATER
                    {
                    match(input,LESS,FOLLOW_LESS_in_comp_op1183); 

                    match(input,GREATER,FOLLOW_GREATER_in_comp_op1185); 

                     value = ComparisonPredicate.Operator.NE; 

                    }
                    break;
                case 3 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:493:5: LESS
                    {
                    match(input,LESS,FOLLOW_LESS_in_comp_op1193); 

                     value = ComparisonPredicate.Operator.LT; 

                    }
                    break;
                case 4 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:494:5: GREATER
                    {
                    match(input,GREATER,FOLLOW_GREATER_in_comp_op1201); 

                     value = ComparisonPredicate.Operator.GT; 

                    }
                    break;
                case 5 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:495:5: LESS EQUALS
                    {
                    match(input,LESS,FOLLOW_LESS_in_comp_op1209); 

                    match(input,EQUALS,FOLLOW_EQUALS_in_comp_op1211); 

                     value = ComparisonPredicate.Operator.LE; 

                    }
                    break;
                case 6 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:496:5: GREATER EQUALS
                    {
                    match(input,GREATER,FOLLOW_GREATER_in_comp_op1219); 

                    match(input,EQUALS,FOLLOW_EQUALS_in_comp_op1221); 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:499:1: null_predicate : column_reference IS ( NOT )? NULL ;
    public final void null_predicate() throws RecognitionException {
        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:500:3: ( column_reference IS ( NOT )? NULL )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:500:5: column_reference IS ( NOT )? NULL
            {
            pushFollow(FOLLOW_column_reference_in_null_predicate1236);
            column_reference();

            state._fsp--;


            match(input,IS,FOLLOW_IS_in_null_predicate1238); 

            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:500:25: ( NOT )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==NOT) ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:500:26: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_null_predicate1241); 

                    }
                    break;

            }


            match(input,NULL,FOLLOW_NULL_in_null_predicate1245); 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:503:1: in_predicate : column_reference ( NOT )? IN in_predicate_value ;
    public final void in_predicate() throws RecognitionException {
        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:504:3: ( column_reference ( NOT )? IN in_predicate_value )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:504:5: column_reference ( NOT )? IN in_predicate_value
            {
            pushFollow(FOLLOW_column_reference_in_in_predicate1258);
            column_reference();

            state._fsp--;


            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:504:22: ( NOT )?
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==NOT) ) {
                alt25=1;
            }
            switch (alt25) {
                case 1 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:504:23: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_in_predicate1261); 

                    }
                    break;

            }


            match(input,IN,FOLLOW_IN_in_in_predicate1265); 

            pushFollow(FOLLOW_in_predicate_value_in_in_predicate1267);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:507:1: in_predicate_value : ( table_subquery | LPAREN in_value_list RPAREN );
    public final void in_predicate_value() throws RecognitionException {
        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:508:3: ( table_subquery | LPAREN in_value_list RPAREN )
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==LPAREN) ) {
                int LA26_1 = input.LA(2);

                if ( (LA26_1==SELECT) ) {
                    alt26=1;
                }
                else if ( ((LA26_1 >= DECIMAL && LA26_1 <= DECIMAL_POSITIVE)||LA26_1==FALSE||(LA26_1 >= INTEGER && LA26_1 <= INTEGER_POSITIVE)||(LA26_1 >= STRING_WITH_QUOTE && LA26_1 <= STRING_WITH_QUOTE_DOUBLE)||LA26_1==TRUE||LA26_1==VARNAME) ) {
                    alt26=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 26, 1, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                throw nvae;

            }
            switch (alt26) {
                case 1 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:508:5: table_subquery
                    {
                    pushFollow(FOLLOW_table_subquery_in_in_predicate_value1282);
                    table_subquery();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:509:5: LPAREN in_value_list RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_in_predicate_value1288); 

                    pushFollow(FOLLOW_in_value_list_in_in_predicate_value1290);
                    in_value_list();

                    state._fsp--;


                    match(input,RPAREN,FOLLOW_RPAREN_in_in_predicate_value1292); 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:512:1: table_subquery : subquery ;
    public final void table_subquery() throws RecognitionException {
        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:513:3: ( subquery )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:513:5: subquery
            {
            pushFollow(FOLLOW_subquery_in_table_subquery1305);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:516:1: subquery : LPAREN query RPAREN ;
    public final void subquery() throws RecognitionException {
        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:517:3: ( LPAREN query RPAREN )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:517:5: LPAREN query RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_subquery1318); 

            pushFollow(FOLLOW_query_in_subquery1320);
            query();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_subquery1322); 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:520:1: in_value_list : row_value_expression ( COMMA row_value_expression )* ;
    public final void in_value_list() throws RecognitionException {
        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:521:3: ( row_value_expression ( COMMA row_value_expression )* )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:521:5: row_value_expression ( COMMA row_value_expression )*
            {
            pushFollow(FOLLOW_row_value_expression_in_in_value_list1337);
            row_value_expression();

            state._fsp--;


            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:521:26: ( COMMA row_value_expression )*
            loop27:
            do {
                int alt27=2;
                int LA27_0 = input.LA(1);

                if ( (LA27_0==COMMA) ) {
                    alt27=1;
                }


                switch (alt27) {
            	case 1 :
            	    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:521:27: COMMA row_value_expression
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_in_value_list1340); 

            	    pushFollow(FOLLOW_row_value_expression_in_in_value_list1342);
            	    row_value_expression();

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
    // $ANTLR end "in_value_list"



    // $ANTLR start "group_by_clause"
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:524:1: group_by_clause returns [ArrayList<GroupingElement> value] : GROUP BY grouping_element_list ;
    public final ArrayList<GroupingElement> group_by_clause() throws RecognitionException {
        ArrayList<GroupingElement> value = null;


        ArrayList<GroupingElement> grouping_element_list34 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:525:3: ( GROUP BY grouping_element_list )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:525:5: GROUP BY grouping_element_list
            {
            match(input,GROUP,FOLLOW_GROUP_in_group_by_clause1361); 

            match(input,BY,FOLLOW_BY_in_group_by_clause1363); 

            pushFollow(FOLLOW_grouping_element_list_in_group_by_clause1365);
            grouping_element_list34=grouping_element_list();

            state._fsp--;



                  value = grouping_element_list34;
                

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:530:1: grouping_element_list returns [ArrayList<GroupingElement> value] : a= grouping_element ( COMMA b= grouping_element )* ;
    public final ArrayList<GroupingElement> grouping_element_list() throws RecognitionException {
        ArrayList<GroupingElement> value = null;


        GroupingElement a =null;

        GroupingElement b =null;



          value = new ArrayList<GroupingElement>();

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:534:3: (a= grouping_element ( COMMA b= grouping_element )* )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:534:5: a= grouping_element ( COMMA b= grouping_element )*
            {
            pushFollow(FOLLOW_grouping_element_in_grouping_element_list1391);
            a=grouping_element();

            state._fsp--;


             value.add(a); 

            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:535:5: ( COMMA b= grouping_element )*
            loop28:
            do {
                int alt28=2;
                int LA28_0 = input.LA(1);

                if ( (LA28_0==COMMA) ) {
                    alt28=1;
                }


                switch (alt28) {
            	case 1 :
            	    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:535:6: COMMA b= grouping_element
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_grouping_element_list1401); 

            	    pushFollow(FOLLOW_grouping_element_in_grouping_element_list1405);
            	    b=grouping_element();

            	    state._fsp--;


            	     value.add(b); 

            	    }
            	    break;

            	default :
            	    break loop28;
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:538:1: grouping_element returns [GroupingElement value] : ( grouping_column_reference | LPAREN grouping_column_reference_list RPAREN );
    public final GroupingElement grouping_element() throws RecognitionException {
        GroupingElement value = null;


        ColumnReference grouping_column_reference35 =null;

        ArrayList<ColumnReference> grouping_column_reference_list36 =null;



          value = new GroupingElement();

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:542:3: ( grouping_column_reference | LPAREN grouping_column_reference_list RPAREN )
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==STRING_WITH_QUOTE_DOUBLE||LA29_0==VARNAME) ) {
                alt29=1;
            }
            else if ( (LA29_0==LPAREN) ) {
                alt29=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 29, 0, input);

                throw nvae;

            }
            switch (alt29) {
                case 1 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:542:5: grouping_column_reference
                    {
                    pushFollow(FOLLOW_grouping_column_reference_in_grouping_element1433);
                    grouping_column_reference35=grouping_column_reference();

                    state._fsp--;


                     value.add(grouping_column_reference35); 

                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:543:5: LPAREN grouping_column_reference_list RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_grouping_element1441); 

                    pushFollow(FOLLOW_grouping_column_reference_list_in_grouping_element1443);
                    grouping_column_reference_list36=grouping_column_reference_list();

                    state._fsp--;


                    match(input,RPAREN,FOLLOW_RPAREN_in_grouping_element1445); 

                     value.update(grouping_column_reference_list36); 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:546:1: grouping_column_reference returns [ColumnReference value] : column_reference ;
    public final ColumnReference grouping_column_reference() throws RecognitionException {
        ColumnReference value = null;


        ColumnReference column_reference37 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:547:3: ( column_reference )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:547:5: column_reference
            {
            pushFollow(FOLLOW_column_reference_in_grouping_column_reference1466);
            column_reference37=column_reference();

            state._fsp--;


             value = column_reference37; 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:550:1: grouping_column_reference_list returns [ArrayList<ColumnReference> value] : a= column_reference ( COMMA b= column_reference )* ;
    public final ArrayList<ColumnReference> grouping_column_reference_list() throws RecognitionException {
        ArrayList<ColumnReference> value = null;


        ColumnReference a =null;

        ColumnReference b =null;



          value = new ArrayList<ColumnReference>();

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:554:3: (a= column_reference ( COMMA b= column_reference )* )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:554:5: a= column_reference ( COMMA b= column_reference )*
            {
            pushFollow(FOLLOW_column_reference_in_grouping_column_reference_list1494);
            a=column_reference();

            state._fsp--;


             value.add(a); 

            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:555:5: ( COMMA b= column_reference )*
            loop30:
            do {
                int alt30=2;
                int LA30_0 = input.LA(1);

                if ( (LA30_0==COMMA) ) {
                    alt30=1;
                }


                switch (alt30) {
            	case 1 :
            	    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:555:6: COMMA b= column_reference
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_grouping_column_reference_list1503); 

            	    pushFollow(FOLLOW_column_reference_in_grouping_column_reference_list1507);
            	    b=column_reference();

            	    state._fsp--;


            	     value.add(b); 

            	    }
            	    break;

            	default :
            	    break loop30;
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:558:1: joined_table returns [TablePrimary value] : ( ( join_type )? JOIN table_reference join_specification )+ ;
    public final TablePrimary joined_table() throws RecognitionException {
        TablePrimary value = null;


        int join_type38 =0;

        BooleanValueExpression join_specification39 =null;

        TablePrimary table_reference40 =null;



          int joinType = JoinOperator.JOIN; // by default

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:562:3: ( ( ( join_type )? JOIN table_reference join_specification )+ )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:562:5: ( ( join_type )? JOIN table_reference join_specification )+
            {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:562:5: ( ( join_type )? JOIN table_reference join_specification )+
            int cnt32=0;
            loop32:
            do {
                int alt32=2;
                int LA32_0 = input.LA(1);

                if ( (LA32_0==FULL||LA32_0==INNER||(LA32_0 >= JOIN && LA32_0 <= LEFT)||LA32_0==RIGHT) ) {
                    alt32=1;
                }


                switch (alt32) {
            	case 1 :
            	    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:562:6: ( join_type )? JOIN table_reference join_specification
            	    {
            	    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:562:6: ( join_type )?
            	    int alt31=2;
            	    int LA31_0 = input.LA(1);

            	    if ( (LA31_0==FULL||LA31_0==INNER||LA31_0==LEFT||LA31_0==RIGHT) ) {
            	        alt31=1;
            	    }
            	    switch (alt31) {
            	        case 1 :
            	            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:562:7: join_type
            	            {
            	            pushFollow(FOLLOW_join_type_in_joined_table1537);
            	            join_type38=join_type();

            	            state._fsp--;


            	             joinType = join_type38; 

            	            }
            	            break;

            	    }


            	    match(input,JOIN,FOLLOW_JOIN_in_joined_table1543); 

            	    pushFollow(FOLLOW_table_reference_in_joined_table1545);
            	    table_reference40=table_reference();

            	    state._fsp--;


            	    pushFollow(FOLLOW_join_specification_in_joined_table1547);
            	    join_specification39=join_specification();

            	    state._fsp--;



            	          JoinOperator joinOp = new JoinOperator(joinType);
            	          joinOp.copy(join_specification39.getSpecification());
            	          relationStack.push(joinOp);
            	          value = table_reference40;
            	        

            	    }
            	    break;

            	default :
            	    if ( cnt32 >= 1 ) break loop32;
                        EarlyExitException eee =
                            new EarlyExitException(32, input);
                        throw eee;
                }
                cnt32++;
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:573:1: join_type returns [int value] : ( INNER | outer_join_type ( OUTER )? );
    public final int join_type() throws RecognitionException {
        int value = 0;


        int outer_join_type41 =0;



          boolean bHasOuter = false;

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:577:3: ( INNER | outer_join_type ( OUTER )? )
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==INNER) ) {
                alt34=1;
            }
            else if ( (LA34_0==FULL||LA34_0==LEFT||LA34_0==RIGHT) ) {
                alt34=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 34, 0, input);

                throw nvae;

            }
            switch (alt34) {
                case 1 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:577:5: INNER
                    {
                    match(input,INNER,FOLLOW_INNER_in_join_type1583); 

                     value = JoinOperator.INNER_JOIN; 

                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:578:5: outer_join_type ( OUTER )?
                    {
                    pushFollow(FOLLOW_outer_join_type_in_join_type1591);
                    outer_join_type41=outer_join_type();

                    state._fsp--;


                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:578:21: ( OUTER )?
                    int alt33=2;
                    int LA33_0 = input.LA(1);

                    if ( (LA33_0==OUTER) ) {
                        alt33=1;
                    }
                    switch (alt33) {
                        case 1 :
                            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:578:22: OUTER
                            {
                            match(input,OUTER,FOLLOW_OUTER_in_join_type1594); 

                             bHasOuter = true; 

                            }
                            break;

                    }



                          if (bHasOuter) {
                            switch(outer_join_type41) {
                              case JoinOperator.LEFT_JOIN: value = JoinOperator.LEFT_OUTER_JOIN; break;
                              case JoinOperator.RIGHT_JOIN: value = JoinOperator.RIGHT_OUTER_JOIN; break;
                              case JoinOperator.FULL_JOIN: value = JoinOperator.FULL_OUTER_JOIN; break;
                            }
                          }
                          else {
                            value = outer_join_type41;
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:592:1: outer_join_type returns [int value] : ( LEFT | RIGHT | FULL );
    public final int outer_join_type() throws RecognitionException {
        int value = 0;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:593:3: ( LEFT | RIGHT | FULL )
            int alt35=3;
            switch ( input.LA(1) ) {
            case LEFT:
                {
                alt35=1;
                }
                break;
            case RIGHT:
                {
                alt35=2;
                }
                break;
            case FULL:
                {
                alt35=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 35, 0, input);

                throw nvae;

            }

            switch (alt35) {
                case 1 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:593:5: LEFT
                    {
                    match(input,LEFT,FOLLOW_LEFT_in_outer_join_type1619); 

                     value = JoinOperator.LEFT_JOIN; 

                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:594:5: RIGHT
                    {
                    match(input,RIGHT,FOLLOW_RIGHT_in_outer_join_type1627); 

                     value = JoinOperator.RIGHT_JOIN; 

                    }
                    break;
                case 3 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:595:5: FULL
                    {
                    match(input,FULL,FOLLOW_FULL_in_outer_join_type1635); 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:598:1: join_specification returns [BooleanValueExpression value] : join_condition ;
    public final BooleanValueExpression join_specification() throws RecognitionException {
        BooleanValueExpression value = null;


        BooleanValueExpression join_condition42 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:599:3: ( join_condition )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:599:5: join_condition
            {
            pushFollow(FOLLOW_join_condition_in_join_specification1654);
            join_condition42=join_condition();

            state._fsp--;


             value = join_condition42; 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:603:1: join_condition returns [BooleanValueExpression value] : ON search_condition ;
    public final BooleanValueExpression join_condition() throws RecognitionException {
        BooleanValueExpression value = null;


        BooleanValueExpression search_condition43 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:604:3: ( ON search_condition )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:604:5: ON search_condition
            {
            match(input,ON,FOLLOW_ON_in_join_condition1674); 

            pushFollow(FOLLOW_search_condition_in_join_condition1676);
            search_condition43=search_condition();

            state._fsp--;



                  value = search_condition43;
                

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:609:1: named_columns_join : USING LPAREN join_column_list RPAREN ;
    public final void named_columns_join() throws RecognitionException {
        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:610:3: ( USING LPAREN join_column_list RPAREN )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:610:5: USING LPAREN join_column_list RPAREN
            {
            match(input,USING,FOLLOW_USING_in_named_columns_join1691); 

            match(input,LPAREN,FOLLOW_LPAREN_in_named_columns_join1693); 

            pushFollow(FOLLOW_join_column_list_in_named_columns_join1695);
            join_column_list();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_named_columns_join1697); 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:613:1: join_column_list : column_name ( COMMA column_name )* ;
    public final void join_column_list() throws RecognitionException {
        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:614:3: ( column_name ( COMMA column_name )* )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:614:5: column_name ( COMMA column_name )*
            {
            pushFollow(FOLLOW_column_name_in_join_column_list1710);
            column_name();

            state._fsp--;


            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:614:17: ( COMMA column_name )*
            loop36:
            do {
                int alt36=2;
                int LA36_0 = input.LA(1);

                if ( (LA36_0==COMMA) ) {
                    alt36=1;
                }


                switch (alt36) {
            	case 1 :
            	    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:614:18: COMMA column_name
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_join_column_list1713); 

            	    pushFollow(FOLLOW_column_name_in_join_column_list1715);
            	    column_name();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop36;
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:618:1: table_primary returns [TablePrimary value] : ( table_name ( ( AS )? alias_name )? | derived_table ( AS )? alias_name );
    public final TablePrimary table_primary() throws RecognitionException {
        TablePrimary value = null;


        TablePrimary table_name44 =null;

        String alias_name45 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:619:3: ( table_name ( ( AS )? alias_name )? | derived_table ( AS )? alias_name )
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( (LA40_0==STRING_WITH_QUOTE_DOUBLE||LA40_0==VARNAME) ) {
                alt40=1;
            }
            else if ( (LA40_0==LPAREN) ) {
                alt40=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 40, 0, input);

                throw nvae;

            }
            switch (alt40) {
                case 1 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:619:5: table_name ( ( AS )? alias_name )?
                    {
                    pushFollow(FOLLOW_table_name_in_table_primary1735);
                    table_name44=table_name();

                    state._fsp--;


                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:620:5: ( ( AS )? alias_name )?
                    int alt38=2;
                    int LA38_0 = input.LA(1);

                    if ( (LA38_0==AS||LA38_0==STRING_WITH_QUOTE_DOUBLE||LA38_0==VARNAME) ) {
                        alt38=1;
                    }
                    switch (alt38) {
                        case 1 :
                            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:620:6: ( AS )? alias_name
                            {
                            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:620:6: ( AS )?
                            int alt37=2;
                            int LA37_0 = input.LA(1);

                            if ( (LA37_0==AS) ) {
                                alt37=1;
                            }
                            switch (alt37) {
                                case 1 :
                                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:620:6: AS
                                    {
                                    match(input,AS,FOLLOW_AS_in_table_primary1742); 

                                    }
                                    break;

                            }


                            pushFollow(FOLLOW_alias_name_in_table_primary1745);
                            alias_name45=alias_name();

                            state._fsp--;


                            }
                            break;

                    }



                          value = table_name44; 
                          value.setAlias(alias_name45);
                          Relation table = new Relation(value);      
                          relationStack.push(table);
                        

                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:626:5: derived_table ( AS )? alias_name
                    {
                    pushFollow(FOLLOW_derived_table_in_table_primary1755);
                    derived_table();

                    state._fsp--;


                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:627:5: ( AS )?
                    int alt39=2;
                    int LA39_0 = input.LA(1);

                    if ( (LA39_0==AS) ) {
                        alt39=1;
                    }
                    switch (alt39) {
                        case 1 :
                            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:627:5: AS
                            {
                            match(input,AS,FOLLOW_AS_in_table_primary1761); 

                            }
                            break;

                    }


                    pushFollow(FOLLOW_alias_name_in_table_primary1764);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:632:1: table_name returns [TablePrimary value] : ( schema_name PERIOD )? table_identifier ;
    public final TablePrimary table_name() throws RecognitionException {
        TablePrimary value = null;


        String schema_name46 =null;

        String table_identifier47 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:633:3: ( ( schema_name PERIOD )? table_identifier )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:633:5: ( schema_name PERIOD )? table_identifier
            {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:633:5: ( schema_name PERIOD )?
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
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:633:6: schema_name PERIOD
                    {
                    pushFollow(FOLLOW_schema_name_in_table_name1786);
                    schema_name46=schema_name();

                    state._fsp--;


                    match(input,PERIOD,FOLLOW_PERIOD_in_table_name1788); 

                    }
                    break;

            }


            pushFollow(FOLLOW_table_identifier_in_table_name1792);
            table_identifier47=table_identifier();

            state._fsp--;



                  String schema = schema_name46;      
                  if (schema != null && schema != "") {
                    value = new TablePrimary(schema, table_identifier47);
                  }
                  else {
                    value = new TablePrimary(table_identifier47);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:644:1: alias_name returns [String value] : identifier ;
    public final String alias_name() throws RecognitionException {
        String value = null;


        String identifier48 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:645:3: ( identifier )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:645:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_alias_name1813);
            identifier48=identifier();

            state._fsp--;


             value = identifier48; 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:648:1: derived_table : table_subquery ;
    public final void derived_table() throws RecognitionException {
        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:649:3: ( table_subquery )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:649:5: table_subquery
            {
            pushFollow(FOLLOW_table_subquery_in_derived_table1829);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:652:1: table_identifier returns [String value] : identifier ;
    public final String table_identifier() throws RecognitionException {
        String value = null;


        String identifier49 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:653:3: ( identifier )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:653:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_table_identifier1850);
            identifier49=identifier();

            state._fsp--;


             value = identifier49; 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:656:1: schema_name returns [String value] : identifier ;
    public final String schema_name() throws RecognitionException {
        String value = null;


        String identifier50 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:657:3: ( identifier )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:657:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_schema_name1871);
            identifier50=identifier();

            state._fsp--;


             value = identifier50; 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:660:1: column_name returns [String value] : identifier ;
    public final String column_name() throws RecognitionException {
        String value = null;


        String identifier51 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:661:3: ( identifier )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:661:5: identifier
            {
            pushFollow(FOLLOW_identifier_in_column_name1894);
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
    // $ANTLR end "column_name"



    // $ANTLR start "identifier"
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:664:1: identifier returns [String value] : (t= regular_identifier |t= delimited_identifier ) ;
    public final String identifier() throws RecognitionException {
        String value = null;


        String t =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:665:3: ( (t= regular_identifier |t= delimited_identifier ) )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:665:5: (t= regular_identifier |t= delimited_identifier )
            {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:665:5: (t= regular_identifier |t= delimited_identifier )
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
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:665:6: t= regular_identifier
                    {
                    pushFollow(FOLLOW_regular_identifier_in_identifier1918);
                    t=regular_identifier();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:665:29: t= delimited_identifier
                    {
                    pushFollow(FOLLOW_delimited_identifier_in_identifier1924);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:668:1: regular_identifier returns [String value] : VARNAME ;
    public final String regular_identifier() throws RecognitionException {
        String value = null;


        Token VARNAME52=null;

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:669:3: ( VARNAME )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:669:5: VARNAME
            {
            VARNAME52=(Token)match(input,VARNAME,FOLLOW_VARNAME_in_regular_identifier1944); 

             value = (VARNAME52!=null?VARNAME52.getText():null); 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:672:1: delimited_identifier returns [String value] : STRING_WITH_QUOTE_DOUBLE ;
    public final String delimited_identifier() throws RecognitionException {
        String value = null;


        Token STRING_WITH_QUOTE_DOUBLE53=null;

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:673:3: ( STRING_WITH_QUOTE_DOUBLE )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:673:5: STRING_WITH_QUOTE_DOUBLE
            {
            STRING_WITH_QUOTE_DOUBLE53=(Token)match(input,STRING_WITH_QUOTE_DOUBLE,FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_delimited_identifier1963); 

             
                  value = (STRING_WITH_QUOTE_DOUBLE53!=null?STRING_WITH_QUOTE_DOUBLE53.getText():null);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:679:1: general_literal returns [Literal value] : ( string_literal | boolean_literal );
    public final Literal general_literal() throws RecognitionException {
        Literal value = null;


        StringLiteral string_literal54 =null;

        BooleanLiteral boolean_literal55 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:680:3: ( string_literal | boolean_literal )
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
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:680:5: string_literal
                    {
                    pushFollow(FOLLOW_string_literal_in_general_literal1982);
                    string_literal54=string_literal();

                    state._fsp--;


                     value = string_literal54; 

                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:681:5: boolean_literal
                    {
                    pushFollow(FOLLOW_boolean_literal_in_general_literal1990);
                    boolean_literal55=boolean_literal();

                    state._fsp--;


                     value = boolean_literal55; 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:684:1: string_literal returns [StringLiteral value] : STRING_WITH_QUOTE ;
    public final StringLiteral string_literal() throws RecognitionException {
        StringLiteral value = null;


        Token STRING_WITH_QUOTE56=null;

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:685:3: ( STRING_WITH_QUOTE )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:685:5: STRING_WITH_QUOTE
            {
            STRING_WITH_QUOTE56=(Token)match(input,STRING_WITH_QUOTE,FOLLOW_STRING_WITH_QUOTE_in_string_literal2009); 


                  String str = (STRING_WITH_QUOTE56!=null?STRING_WITH_QUOTE56.getText():null);
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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:692:1: boolean_literal returns [BooleanLiteral value] : (t= TRUE |t= FALSE ) ;
    public final BooleanLiteral boolean_literal() throws RecognitionException {
        BooleanLiteral value = null;


        Token t=null;

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:693:3: ( (t= TRUE |t= FALSE ) )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:693:5: (t= TRUE |t= FALSE )
            {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:693:5: (t= TRUE |t= FALSE )
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
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:693:6: t= TRUE
                    {
                    t=(Token)match(input,TRUE,FOLLOW_TRUE_in_boolean_literal2031); 

                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:693:15: t= FALSE
                    {
                    t=(Token)match(input,FALSE,FOLLOW_FALSE_in_boolean_literal2037); 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:696:1: numeric_literal returns [NumericLiteral value] : ( numeric_literal_unsigned | numeric_literal_positive | numeric_literal_negative );
    public final NumericLiteral numeric_literal() throws RecognitionException {
        NumericLiteral value = null;


        NumericLiteral numeric_literal_unsigned57 =null;

        NumericLiteral numeric_literal_positive58 =null;

        NumericLiteral numeric_literal_negative59 =null;


        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:697:3: ( numeric_literal_unsigned | numeric_literal_positive | numeric_literal_negative )
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
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:697:5: numeric_literal_unsigned
                    {
                    pushFollow(FOLLOW_numeric_literal_unsigned_in_numeric_literal2057);
                    numeric_literal_unsigned57=numeric_literal_unsigned();

                    state._fsp--;


                     value = numeric_literal_unsigned57; 

                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:698:5: numeric_literal_positive
                    {
                    pushFollow(FOLLOW_numeric_literal_positive_in_numeric_literal2065);
                    numeric_literal_positive58=numeric_literal_positive();

                    state._fsp--;


                     value = numeric_literal_positive58; 

                    }
                    break;
                case 3 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:699:5: numeric_literal_negative
                    {
                    pushFollow(FOLLOW_numeric_literal_negative_in_numeric_literal2073);
                    numeric_literal_negative59=numeric_literal_negative();

                    state._fsp--;


                     value = numeric_literal_negative59; 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:702:1: numeric_literal_unsigned returns [NumericLiteral value] : ( INTEGER | DECIMAL );
    public final NumericLiteral numeric_literal_unsigned() throws RecognitionException {
        NumericLiteral value = null;


        Token INTEGER60=null;
        Token DECIMAL61=null;

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:703:3: ( INTEGER | DECIMAL )
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
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:703:5: INTEGER
                    {
                    INTEGER60=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_numeric_literal_unsigned2092); 

                     value = new IntegerLiteral((INTEGER60!=null?INTEGER60.getText():null)); 

                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:704:5: DECIMAL
                    {
                    DECIMAL61=(Token)match(input,DECIMAL,FOLLOW_DECIMAL_in_numeric_literal_unsigned2100); 

                     value = new DecimalLiteral((DECIMAL61!=null?DECIMAL61.getText():null)); 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:707:1: numeric_literal_positive returns [NumericLiteral value] : ( INTEGER_POSITIVE | DECIMAL_POSITIVE );
    public final NumericLiteral numeric_literal_positive() throws RecognitionException {
        NumericLiteral value = null;


        Token INTEGER_POSITIVE62=null;
        Token DECIMAL_POSITIVE63=null;

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:708:3: ( INTEGER_POSITIVE | DECIMAL_POSITIVE )
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
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:708:5: INTEGER_POSITIVE
                    {
                    INTEGER_POSITIVE62=(Token)match(input,INTEGER_POSITIVE,FOLLOW_INTEGER_POSITIVE_in_numeric_literal_positive2119); 

                     value = new IntegerLiteral((INTEGER_POSITIVE62!=null?INTEGER_POSITIVE62.getText():null)); 

                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:709:5: DECIMAL_POSITIVE
                    {
                    DECIMAL_POSITIVE63=(Token)match(input,DECIMAL_POSITIVE,FOLLOW_DECIMAL_POSITIVE_in_numeric_literal_positive2127); 

                     value = new DecimalLiteral((DECIMAL_POSITIVE63!=null?DECIMAL_POSITIVE63.getText():null)); 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:712:1: numeric_literal_negative returns [NumericLiteral value] : ( INTEGER_NEGATIVE | DECIMAL_NEGATIVE );
    public final NumericLiteral numeric_literal_negative() throws RecognitionException {
        NumericLiteral value = null;


        Token INTEGER_NEGATIVE64=null;
        Token DECIMAL_NEGATIVE65=null;

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:713:3: ( INTEGER_NEGATIVE | DECIMAL_NEGATIVE )
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
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:713:5: INTEGER_NEGATIVE
                    {
                    INTEGER_NEGATIVE64=(Token)match(input,INTEGER_NEGATIVE,FOLLOW_INTEGER_NEGATIVE_in_numeric_literal_negative2148); 

                     value = new IntegerLiteral((INTEGER_NEGATIVE64!=null?INTEGER_NEGATIVE64.getText():null)); 

                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:714:5: DECIMAL_NEGATIVE
                    {
                    DECIMAL_NEGATIVE65=(Token)match(input,DECIMAL_NEGATIVE,FOLLOW_DECIMAL_NEGATIVE_in_numeric_literal_negative2156); 

                     value = new DecimalLiteral((DECIMAL_NEGATIVE65!=null?DECIMAL_NEGATIVE65.getText():null)); 

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
    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:717:1: truth_value returns [boolean value] : (t= TRUE |t= FALSE ) ;
    public final boolean truth_value() throws RecognitionException {
        boolean value = false;


        Token t=null;

        try {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:718:3: ( (t= TRUE |t= FALSE ) )
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:718:5: (t= TRUE |t= FALSE )
            {
            // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:718:5: (t= TRUE |t= FALSE )
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
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:718:6: t= TRUE
                    {
                    t=(Token)match(input,TRUE,FOLLOW_TRUE_in_truth_value2180); 

                    }
                    break;
                case 2 :
                    // /Users/mariano/Code/obda_eclipse_workspace_release/obdalib-parent/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/SQL99.g:718:15: t= FALSE
                    {
                    t=(Token)match(input,FALSE,FOLLOW_FALSE_in_truth_value2186); 

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
    public static final BitSet FOLLOW_query_specification_in_query68 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SELECT_in_query_specification109 = new BitSet(new long[]{0x0000000008000010L,0x0000000000020400L});
    public static final BitSet FOLLOW_set_quantifier_in_query_specification111 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_select_list_in_query_specification114 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_table_expression_in_query_specification116 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALL_in_set_quantifier137 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DISTINCT_in_set_quantifier145 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_select_sublist_in_select_list173 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_select_list178 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_select_sublist_in_select_list182 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_derived_column_in_select_sublist205 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_identifier_in_qualified_asterisk223 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_PERIOD_in_qualified_asterisk225 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_ASTERISK_in_qualified_asterisk227 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_derived_column251 = new BitSet(new long[]{0x0000000000000802L,0x0000000000020400L});
    public static final BitSet FOLLOW_AS_in_derived_column254 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_alias_name_in_derived_column257 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_reference_value_expression_in_value_expression281 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_numeric_value_expression308 = new BitSet(new long[]{0x00001C0003800000L,0x0000000000020400L});
    public static final BitSet FOLLOW_numeric_operation_in_numeric_value_expression310 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_numeric_value_expression312 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_numeric_operation327 = new BitSet(new long[]{0x4020000000000002L});
    public static final BitSet FOLLOW_PLUS_in_numeric_operation345 = new BitSet(new long[]{0x00001C0003800000L,0x0000000000020400L});
    public static final BitSet FOLLOW_MINUS_in_numeric_operation349 = new BitSet(new long[]{0x00001C0003800000L,0x0000000000020400L});
    public static final BitSet FOLLOW_term_in_numeric_operation362 = new BitSet(new long[]{0x4020000000000002L});
    public static final BitSet FOLLOW_factor_in_term384 = new BitSet(new long[]{0x0000000000001002L,0x0000000000000080L});
    public static final BitSet FOLLOW_ASTERISK_in_term404 = new BitSet(new long[]{0x00001C0003800000L,0x0000000000020400L});
    public static final BitSet FOLLOW_SOLIDUS_in_term408 = new BitSet(new long[]{0x00001C0003800000L,0x0000000000020400L});
    public static final BitSet FOLLOW_factor_in_term422 = new BitSet(new long[]{0x0000000000001002L,0x0000000000000080L});
    public static final BitSet FOLLOW_column_reference_in_factor450 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numeric_literal_in_factor458 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_string_value_expression501 = new BitSet(new long[]{0x0000000400000000L,0x0000000000022600L});
    public static final BitSet FOLLOW_concatenation_in_string_value_expression503 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_string_value_expression505 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_character_factor_in_concatenation524 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_CONCATENATION_in_concatenation536 = new BitSet(new long[]{0x0000000400000000L,0x0000000000022600L});
    public static final BitSet FOLLOW_character_factor_in_concatenation549 = new BitSet(new long[]{0x0000000000200002L});
    public static final BitSet FOLLOW_column_reference_in_character_factor570 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_general_literal_in_character_factor578 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_reference_value_expression602 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_identifier_in_column_reference624 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_PERIOD_in_column_reference626 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_column_name_in_column_reference630 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_function_specification_in_collection_value_expression658 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNT_in_set_function_specification673 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_set_function_specification675 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_ASTERISK_in_set_function_specification677 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_set_function_specification679 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_general_set_function_in_set_function_specification687 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_function_op_in_general_set_function702 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_general_set_function704 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_column_reference_in_general_set_function706 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_general_set_function708 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AVG_in_set_function_op732 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MAX_in_set_function_op738 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MIN_in_set_function_op744 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUM_in_set_function_op750 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EVERY_in_set_function_op756 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ANY_in_set_function_op762 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOME_in_set_function_op768 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNT_in_set_function_op774 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_row_value_expression796 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expression_in_row_value_expression804 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numeric_literal_in_literal823 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_general_literal_in_literal831 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_from_clause_in_table_expression850 = new BitSet(new long[]{0x0000000000000002L,0x0000000000040000L});
    public static final BitSet FOLLOW_where_clause_in_table_expression859 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_from_clause884 = new BitSet(new long[]{0x0002000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_table_reference_list_in_from_clause886 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_reference_in_table_reference_list916 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_table_reference_list933 = new BitSet(new long[]{0x0002000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_table_reference_in_table_reference_list937 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_table_primary_in_table_reference964 = new BitSet(new long[]{0x0000C21000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_joined_table_in_table_reference973 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHERE_in_where_clause995 = new BitSet(new long[]{0x00001C0403800000L,0x0000000000022600L});
    public static final BitSet FOLLOW_search_condition_in_where_clause997 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_value_expression_in_search_condition1016 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression1052 = new BitSet(new long[]{0x0200000000000002L});
    public static final BitSet FOLLOW_OR_in_boolean_value_expression1055 = new BitSet(new long[]{0x00001C0403800000L,0x0000000000022600L});
    public static final BitSet FOLLOW_boolean_term_in_boolean_value_expression1059 = new BitSet(new long[]{0x0200000000000002L});
    public static final BitSet FOLLOW_boolean_factor_in_boolean_term1082 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_AND_in_boolean_term1085 = new BitSet(new long[]{0x00001C0403800000L,0x0000000000022600L});
    public static final BitSet FOLLOW_boolean_factor_in_boolean_term1089 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_predicate_in_boolean_factor1105 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparison_predicate_in_predicate1125 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_row_value_expression_in_comparison_predicate1150 = new BitSet(new long[]{0x0001002080000000L});
    public static final BitSet FOLLOW_comp_op_in_comparison_predicate1152 = new BitSet(new long[]{0x00001C0403800000L,0x0000000000022600L});
    public static final BitSet FOLLOW_row_value_expression_in_comparison_predicate1156 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_comp_op1175 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_comp_op1183 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_GREATER_in_comp_op1185 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_comp_op1193 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_comp_op1201 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_comp_op1209 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_EQUALS_in_comp_op1211 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_comp_op1219 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_EQUALS_in_comp_op1221 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_null_predicate1236 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_IS_in_null_predicate1238 = new BitSet(new long[]{0x00C0000000000000L});
    public static final BitSet FOLLOW_NOT_in_null_predicate1241 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_NULL_in_null_predicate1245 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_in_predicate1258 = new BitSet(new long[]{0x0040010000000000L});
    public static final BitSet FOLLOW_NOT_in_in_predicate1261 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_IN_in_in_predicate1265 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_in_predicate_value_in_in_predicate1267 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_subquery_in_in_predicate_value1282 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_in_predicate_value1288 = new BitSet(new long[]{0x00001C0403800000L,0x0000000000022600L});
    public static final BitSet FOLLOW_in_value_list_in_in_predicate_value1290 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_in_predicate_value1292 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_subquery_in_table_subquery1305 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_subquery1318 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_query_in_subquery1320 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_subquery1322 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_row_value_expression_in_in_value_list1337 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_in_value_list1340 = new BitSet(new long[]{0x00001C0403800000L,0x0000000000022600L});
    public static final BitSet FOLLOW_row_value_expression_in_in_value_list1342 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_GROUP_in_group_by_clause1361 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_BY_in_group_by_clause1363 = new BitSet(new long[]{0x0002000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_grouping_element_list_in_group_by_clause1365 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_grouping_element_in_grouping_element_list1391 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_grouping_element_list1401 = new BitSet(new long[]{0x0002000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_grouping_element_in_grouping_element_list1405 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_grouping_column_reference_in_grouping_element1433 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_grouping_element1441 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_grouping_column_reference_list_in_grouping_element1443 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_grouping_element1445 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference1466 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference_list1494 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_grouping_column_reference_list1503 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_column_reference_in_grouping_column_reference_list1507 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_join_type_in_joined_table1537 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_JOIN_in_joined_table1543 = new BitSet(new long[]{0x0002000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_table_reference_in_joined_table1545 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_join_specification_in_joined_table1547 = new BitSet(new long[]{0x0000C21000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_INNER_in_join_type1583 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_outer_join_type_in_join_type1591 = new BitSet(new long[]{0x0800000000000002L});
    public static final BitSet FOLLOW_OUTER_in_join_type1594 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFT_in_outer_join_type1619 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RIGHT_in_outer_join_type1627 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FULL_in_outer_join_type1635 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_join_condition_in_join_specification1654 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ON_in_join_condition1674 = new BitSet(new long[]{0x00001C0403800000L,0x0000000000022600L});
    public static final BitSet FOLLOW_search_condition_in_join_condition1676 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_USING_in_named_columns_join1691 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_named_columns_join1693 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_join_column_list_in_named_columns_join1695 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_named_columns_join1697 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_name_in_join_column_list1710 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_join_column_list1713 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_column_name_in_join_column_list1715 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_table_name_in_table_primary1735 = new BitSet(new long[]{0x0000000000000802L,0x0000000000020400L});
    public static final BitSet FOLLOW_AS_in_table_primary1742 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_alias_name_in_table_primary1745 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_derived_table_in_table_primary1755 = new BitSet(new long[]{0x0000000000000800L,0x0000000000020400L});
    public static final BitSet FOLLOW_AS_in_table_primary1761 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_alias_name_in_table_primary1764 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_schema_name_in_table_name1786 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_PERIOD_in_table_name1788 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020400L});
    public static final BitSet FOLLOW_table_identifier_in_table_name1792 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_alias_name1813 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_subquery_in_derived_table1829 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_table_identifier1850 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_schema_name1871 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_column_name1894 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_regular_identifier_in_identifier1918 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_delimited_identifier_in_identifier1924 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VARNAME_in_regular_identifier1944 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_delimited_identifier1963 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_string_literal_in_general_literal1982 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_boolean_literal_in_general_literal1990 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_WITH_QUOTE_in_string_literal2009 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_boolean_literal2031 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_boolean_literal2037 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numeric_literal_unsigned_in_numeric_literal2057 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numeric_literal_positive_in_numeric_literal2065 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numeric_literal_negative_in_numeric_literal2073 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_in_numeric_literal_unsigned2092 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_in_numeric_literal_unsigned2100 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_POSITIVE_in_numeric_literal_positive2119 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_POSITIVE_in_numeric_literal_positive2127 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_NEGATIVE_in_numeric_literal_negative2148 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_NEGATIVE_in_numeric_literal_negative2156 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_truth_value2180 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_truth_value2186 = new BitSet(new long[]{0x0000000000000002L});

}