// $ANTLR 3.4 C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g 2012-03-22 15:31:49

package it.unibz.krdb.obda.parser;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDALibConstants;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.TokenStream;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class TurtleParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ALPHA", "ALPHANUM", "AMPERSAND", "APOSTROPHE", "ASTERISK", "AT", "BACKSLASH", "BASE", "BLANK", "BLANK_PREFIX", "CARET", "CHAR", "COLON", "COMMA", "DECIMAL", "DECIMAL_NEGATIVE", "DECIMAL_POSITIVE", "DIGIT", "DOLLAR", "DOUBLE", "DOUBLE_NEGATIVE", "DOUBLE_POSITIVE", "DOUBLE_SLASH", "ECHAR", "EQUALS", "EXCLAMATION", "FALSE", "GREATER", "HASH", "ID", "ID_CORE", "ID_PLAIN", "ID_START", "INTEGER", "INTEGER_NEGATIVE", "INTEGER_POSITIVE", "LESS", "LPAREN", "LSQ_BRACKET", "MINUS", "PERCENT", "PERIOD", "PLUS", "PREFIX", "QUESTION", "QUOTE_DOUBLE", "QUOTE_SINGLE", "REFERENCE", "RPAREN", "RSQ_BRACKET", "SCHEMA", "SEMI", "SLASH", "STRING_PREFIX", "STRING_URI", "STRING_WITH_QUOTE", "STRING_WITH_QUOTE_DOUBLE", "TILDE", "TRUE", "UNDERSCORE", "URI_PATH", "VARNAME", "WS", "'a'"
    };

    public static final int EOF=-1;
    public static final int T__67=67;
    public static final int ALPHA=4;
    public static final int ALPHANUM=5;
    public static final int AMPERSAND=6;
    public static final int APOSTROPHE=7;
    public static final int ASTERISK=8;
    public static final int AT=9;
    public static final int BACKSLASH=10;
    public static final int BASE=11;
    public static final int BLANK=12;
    public static final int BLANK_PREFIX=13;
    public static final int CARET=14;
    public static final int CHAR=15;
    public static final int COLON=16;
    public static final int COMMA=17;
    public static final int DECIMAL=18;
    public static final int DECIMAL_NEGATIVE=19;
    public static final int DECIMAL_POSITIVE=20;
    public static final int DIGIT=21;
    public static final int DOLLAR=22;
    public static final int DOUBLE=23;
    public static final int DOUBLE_NEGATIVE=24;
    public static final int DOUBLE_POSITIVE=25;
    public static final int DOUBLE_SLASH=26;
    public static final int ECHAR=27;
    public static final int EQUALS=28;
    public static final int EXCLAMATION=29;
    public static final int FALSE=30;
    public static final int GREATER=31;
    public static final int HASH=32;
    public static final int ID=33;
    public static final int ID_CORE=34;
    public static final int ID_PLAIN=35;
    public static final int ID_START=36;
    public static final int INTEGER=37;
    public static final int INTEGER_NEGATIVE=38;
    public static final int INTEGER_POSITIVE=39;
    public static final int LESS=40;
    public static final int LPAREN=41;
    public static final int LSQ_BRACKET=42;
    public static final int MINUS=43;
    public static final int PERCENT=44;
    public static final int PERIOD=45;
    public static final int PLUS=46;
    public static final int PREFIX=47;
    public static final int QUESTION=48;
    public static final int QUOTE_DOUBLE=49;
    public static final int QUOTE_SINGLE=50;
    public static final int REFERENCE=51;
    public static final int RPAREN=52;
    public static final int RSQ_BRACKET=53;
    public static final int SCHEMA=54;
    public static final int SEMI=55;
    public static final int SLASH=56;
    public static final int STRING_PREFIX=57;
    public static final int STRING_URI=58;
    public static final int STRING_WITH_QUOTE=59;
    public static final int STRING_WITH_QUOTE_DOUBLE=60;
    public static final int TILDE=61;
    public static final int TRUE=62;
    public static final int UNDERSCORE=63;
    public static final int URI_PATH=64;
    public static final int VARNAME=65;
    public static final int WS=66;

    // delegates
    public Parser[] getDelegates() {
        return new Parser[] {};
    }

    // delegators


    public TurtleParser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public TurtleParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

    public String[] getTokenNames() { return TurtleParser.tokenNames; }
    public String getGrammarFileName() { return "C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g"; }


    /** Constants */
    private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    private static final URI RDF_TYPE_URI = URI.create(RDF_TYPE);

    /** Map of directives */
    private HashMap<String, String> directives = new HashMap<String, String>();

    /** The current subject term */
    private Term subject;

    private Set<Term> variableSet = new HashSet<Term>();

    /** A factory to construct the predicates and terms */
    private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();



    // $ANTLR start "parse"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:100:1: parse returns [CQIE value] : ( statement )* EOF ;
    public final CQIE parse() throws RecognitionException {
        CQIE value = null;


        List<Atom> statement1 =null;



          List<Atom> body = new LinkedList<Atom>();

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:104:3: ( ( statement )* EOF )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:104:5: ( statement )* EOF
            {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:104:5: ( statement )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==AT||LA1_0==COLON||LA1_0==DOLLAR||LA1_0==LESS||LA1_0==QUESTION||LA1_0==STRING_PREFIX) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:104:5: statement
            	    {
            	    pushFollow(FOLLOW_statement_in_parse57);
            	    statement1=statement();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            match(input,EOF,FOLLOW_EOF_in_parse60); 


                  int arity = variableSet.size();
                  List<Term> distinguishVariables = new ArrayList<Term>(variableSet);
                  Atom head = dfac.getAtom(dfac.getPredicate(OBDALibConstants.QUERY_HEAD_URI, arity, null), distinguishVariables);
                  if (statement1 != null) {
                    body.addAll(statement1); 
                  }
                  value = dfac.getCQIE(head, body); 
                

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



    // $ANTLR start "statement"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:115:1: statement returns [List<Atom> value] : ( directive PERIOD | triples PERIOD );
    public final List<Atom> statement() throws RecognitionException {
        List<Atom> value = null;


        List<Atom> triples2 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:116:3: ( directive PERIOD | triples PERIOD )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==AT) ) {
                alt2=1;
            }
            else if ( (LA2_0==COLON||LA2_0==DOLLAR||LA2_0==LESS||LA2_0==QUESTION||LA2_0==STRING_PREFIX) ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;

            }
            switch (alt2) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:116:5: directive PERIOD
                    {
                    pushFollow(FOLLOW_directive_in_statement81);
                    directive();

                    state._fsp--;


                    match(input,PERIOD,FOLLOW_PERIOD_in_statement83); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:117:5: triples PERIOD
                    {
                    pushFollow(FOLLOW_triples_in_statement89);
                    triples2=triples();

                    state._fsp--;


                    match(input,PERIOD,FOLLOW_PERIOD_in_statement91); 

                     value = triples2; 

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
    // $ANTLR end "statement"



    // $ANTLR start "directive"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:120:1: directive : ( base | prefixID );
    public final void directive() throws RecognitionException {
        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:121:3: ( base | prefixID )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==AT) ) {
                int LA3_1 = input.LA(2);

                if ( (LA3_1==BASE) ) {
                    alt3=1;
                }
                else if ( (LA3_1==PREFIX) ) {
                    alt3=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 1, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;

            }
            switch (alt3) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:121:5: base
                    {
                    pushFollow(FOLLOW_base_in_directive106);
                    base();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:122:5: prefixID
                    {
                    pushFollow(FOLLOW_prefixID_in_directive112);
                    prefixID();

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
    // $ANTLR end "directive"



    // $ANTLR start "base"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:125:1: base : AT BASE uriref ;
    public final void base() throws RecognitionException {
        String uriref3 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:126:3: ( AT BASE uriref )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:126:5: AT BASE uriref
            {
            match(input,AT,FOLLOW_AT_in_base125); 

            match(input,BASE,FOLLOW_BASE_in_base127); 

            pushFollow(FOLLOW_uriref_in_base129);
            uriref3=uriref();

            state._fsp--;



                  String uriref = uriref3;
                  directives.put("", uriref);
                

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
    // $ANTLR end "base"



    // $ANTLR start "prefixID"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:132:1: prefixID : AT PREFIX prefix uriref ;
    public final void prefixID() throws RecognitionException {
        TurtleParser.prefix_return prefix4 =null;

        String uriref5 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:133:3: ( AT PREFIX prefix uriref )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:133:5: AT PREFIX prefix uriref
            {
            match(input,AT,FOLLOW_AT_in_prefixID144); 

            match(input,PREFIX,FOLLOW_PREFIX_in_prefixID146); 

            pushFollow(FOLLOW_prefix_in_prefixID148);
            prefix4=prefix();

            state._fsp--;


            pushFollow(FOLLOW_uriref_in_prefixID150);
            uriref5=uriref();

            state._fsp--;



                  String prefix = (prefix4!=null?input.toString(prefix4.start,prefix4.stop):null);
                  String uriref = uriref5;
                  directives.put(prefix.substring(0, prefix.length()-1), uriref); // remove the end colon
                

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
    // $ANTLR end "prefixID"



    // $ANTLR start "triples"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:140:1: triples returns [List<Atom> value] : subject predicateObjectList ;
    public final List<Atom> triples() throws RecognitionException {
        List<Atom> value = null;


        Term subject6 =null;

        List<Atom> predicateObjectList7 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:141:3: ( subject predicateObjectList )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:141:5: subject predicateObjectList
            {
            pushFollow(FOLLOW_subject_in_triples171);
            subject6=subject();

            state._fsp--;



                  subject = subject6;
                

            pushFollow(FOLLOW_predicateObjectList_in_triples180);
            predicateObjectList7=predicateObjectList();

            state._fsp--;



                  value = predicateObjectList7;
                

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
    // $ANTLR end "triples"



    // $ANTLR start "predicateObjectList"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:149:1: predicateObjectList returns [List<Atom> value] : v1= verb l1= objectList ( SEMI v2= verb l2= objectList )* ;
    public final List<Atom> predicateObjectList() throws RecognitionException {
        List<Atom> value = null;


        URI v1 =null;

        List<Term> l1 =null;

        URI v2 =null;

        List<Term> l2 =null;



           value = new LinkedList<Atom>();

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:153:3: (v1= verb l1= objectList ( SEMI v2= verb l2= objectList )* )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:153:5: v1= verb l1= objectList ( SEMI v2= verb l2= objectList )*
            {
            pushFollow(FOLLOW_verb_in_predicateObjectList210);
            v1=verb();

            state._fsp--;


            pushFollow(FOLLOW_objectList_in_predicateObjectList214);
            l1=objectList();

            state._fsp--;



                  for (Term object : l1) {
                    Atom atom = null;
                    if (v1.equals(RDF_TYPE_URI)) {
                      URIConstant c = (URIConstant) object;  // it has to be a URI constant
                      Predicate predicate = dfac.getClassPredicate(c.getURI());
                      atom = dfac.getAtom(predicate, subject);
                    } else {
                      Predicate predicate = dfac.getPredicate(v1, 2, null); // the data type cannot be determined here!
                      atom = dfac.getAtom(predicate, subject, object);
                    }        
                    value.add(atom);
                  }
                

            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:167:5: ( SEMI v2= verb l2= objectList )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==SEMI) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:167:6: SEMI v2= verb l2= objectList
            	    {
            	    match(input,SEMI,FOLLOW_SEMI_in_predicateObjectList224); 

            	    pushFollow(FOLLOW_verb_in_predicateObjectList228);
            	    v2=verb();

            	    state._fsp--;


            	    pushFollow(FOLLOW_objectList_in_predicateObjectList232);
            	    l2=objectList();

            	    state._fsp--;



            	          for (Term object : l2) {
            	            Atom atom = null;
            	            if (v2.equals(RDF_TYPE_URI)) {
            	              URIConstant c = (URIConstant) object;  // it has to be a URI constant
            	              Predicate predicate = dfac.getClassPredicate(c.getURI());
            	              atom = dfac.getAtom(predicate, subject);
            	            } else {
            	              Predicate predicate = dfac.getPredicate(v2, 2, null); // the data type cannot be determined here!
            	              atom = dfac.getAtom(predicate, subject, object);
            	            }        
            	            value.add(atom);
            	          }
            	        

            	    }
            	    break;

            	default :
            	    break loop4;
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
    // $ANTLR end "predicateObjectList"



    // $ANTLR start "verb"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:183:1: verb returns [URI value] : ( predicate | 'a' );
    public final URI verb() throws RecognitionException {
        URI value = null;


        URI predicate8 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:184:3: ( predicate | 'a' )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==COLON||LA5_0==LESS||LA5_0==STRING_PREFIX) ) {
                alt5=1;
            }
            else if ( (LA5_0==67) ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;

            }
            switch (alt5) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:184:5: predicate
                    {
                    pushFollow(FOLLOW_predicate_in_verb255);
                    predicate8=predicate();

                    state._fsp--;


                     value = predicate8; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:185:5: 'a'
                    {
                    match(input,67,FOLLOW_67_in_verb263); 

                     value = RDF_TYPE_URI; 

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
    // $ANTLR end "verb"



    // $ANTLR start "objectList"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:188:1: objectList returns [List<Term> value] : o1= object ( COMMA o2= object )* ;
    public final List<Term> objectList() throws RecognitionException {
        List<Term> value = null;


        Term o1 =null;

        Term o2 =null;



          value = new ArrayList<Term>();

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:192:3: (o1= object ( COMMA o2= object )* )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:192:5: o1= object ( COMMA o2= object )*
            {
            pushFollow(FOLLOW_object_in_objectList291);
            o1=object();

            state._fsp--;


             value.add(o1); 

            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:192:42: ( COMMA o2= object )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==COMMA) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:192:43: COMMA o2= object
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_objectList296); 

            	    pushFollow(FOLLOW_object_in_objectList300);
            	    o2=object();

            	    state._fsp--;


            	     value.add(o2); 

            	    }
            	    break;

            	default :
            	    break loop6;
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
    // $ANTLR end "objectList"



    // $ANTLR start "subject"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:195:1: subject returns [Term value] : ( variable | function );
    public final Term subject() throws RecognitionException {
        Term value = null;


        Variable variable9 =null;

        Function function10 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:198:3: ( variable | function )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==DOLLAR||LA7_0==QUESTION) ) {
                alt7=1;
            }
            else if ( (LA7_0==COLON||LA7_0==LESS||LA7_0==STRING_PREFIX) ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;

            }
            switch (alt7) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:198:5: variable
                    {
                    pushFollow(FOLLOW_variable_in_subject324);
                    variable9=variable();

                    state._fsp--;


                     value = variable9; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:199:5: function
                    {
                    pushFollow(FOLLOW_function_in_subject332);
                    function10=function();

                    state._fsp--;


                     value = function10; 

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
    // $ANTLR end "subject"



    // $ANTLR start "predicate"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:202:1: predicate returns [URI value] : resource ;
    public final URI predicate() throws RecognitionException {
        URI value = null;


        URI resource11 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:203:3: ( resource )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:203:5: resource
            {
            pushFollow(FOLLOW_resource_in_predicate353);
            resource11=resource();

            state._fsp--;


             value = resource11; 

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



    // $ANTLR start "object"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:206:1: object returns [Term value] : ( resource | function | literal | variable );
    public final Term object() throws RecognitionException {
        Term value = null;


        URI resource12 =null;

        Function function13 =null;

        ValueConstant literal14 =null;

        Variable variable15 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:207:3: ( resource | function | literal | variable )
            int alt8=4;
            switch ( input.LA(1) ) {
            case LESS:
                {
                int LA8_1 = input.LA(2);

                if ( (LA8_1==STRING_URI) ) {
                    int LA8_6 = input.LA(3);

                    if ( (LA8_6==GREATER) ) {
                        int LA8_10 = input.LA(4);

                        if ( (LA8_10==COMMA||LA8_10==PERIOD||LA8_10==SEMI) ) {
                            alt8=1;
                        }
                        else if ( (LA8_10==LPAREN) ) {
                            alt8=2;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 8, 10, input);

                            throw nvae;

                        }
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 8, 6, input);

                        throw nvae;

                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 1, input);

                    throw nvae;

                }
                }
                break;
            case STRING_PREFIX:
                {
                switch ( input.LA(2) ) {
                case VARNAME:
                    {
                    int LA8_7 = input.LA(3);

                    if ( (LA8_7==COMMA||LA8_7==PERIOD||LA8_7==SEMI) ) {
                        alt8=1;
                    }
                    else if ( (LA8_7==LPAREN) ) {
                        alt8=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 8, 7, input);

                        throw nvae;

                    }
                    }
                    break;
                case COMMA:
                case PERIOD:
                case SEMI:
                    {
                    alt8=1;
                    }
                    break;
                case LPAREN:
                    {
                    alt8=2;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 2, input);

                    throw nvae;

                }

                }
                break;
            case COLON:
                {
                switch ( input.LA(2) ) {
                case VARNAME:
                    {
                    int LA8_7 = input.LA(3);

                    if ( (LA8_7==COMMA||LA8_7==PERIOD||LA8_7==SEMI) ) {
                        alt8=1;
                    }
                    else if ( (LA8_7==LPAREN) ) {
                        alt8=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 8, 7, input);

                        throw nvae;

                    }
                    }
                    break;
                case COMMA:
                case PERIOD:
                case SEMI:
                    {
                    alt8=1;
                    }
                    break;
                case LPAREN:
                    {
                    alt8=2;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 3, input);

                    throw nvae;

                }

                }
                break;
            case DECIMAL:
            case DECIMAL_NEGATIVE:
            case DECIMAL_POSITIVE:
            case DOUBLE:
            case DOUBLE_NEGATIVE:
            case DOUBLE_POSITIVE:
            case FALSE:
            case INTEGER:
            case INTEGER_NEGATIVE:
            case INTEGER_POSITIVE:
            case STRING_WITH_QUOTE_DOUBLE:
            case TRUE:
                {
                alt8=3;
                }
                break;
            case DOLLAR:
            case QUESTION:
                {
                alt8=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;

            }

            switch (alt8) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:207:5: resource
                    {
                    pushFollow(FOLLOW_resource_in_object374);
                    resource12=resource();

                    state._fsp--;


                     value = dfac.getURIConstant(resource12); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:208:5: function
                    {
                    pushFollow(FOLLOW_function_in_object383);
                    function13=function();

                    state._fsp--;


                     value = function13; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:209:5: literal
                    {
                    pushFollow(FOLLOW_literal_in_object391);
                    literal14=literal();

                    state._fsp--;


                     value = literal14; 

                    }
                    break;
                case 4 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:210:5: variable
                    {
                    pushFollow(FOLLOW_variable_in_object400);
                    variable15=variable();

                    state._fsp--;


                     value = variable15; 

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
    // $ANTLR end "object"



    // $ANTLR start "resource"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:214:1: resource returns [URI value] : ( uriref | qname );
    public final URI resource() throws RecognitionException {
        URI value = null;


        String uriref16 =null;

        String qname17 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:215:3: ( uriref | qname )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==LESS) ) {
                alt9=1;
            }
            else if ( (LA9_0==COLON||LA9_0==STRING_PREFIX) ) {
                alt9=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;

            }
            switch (alt9) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:215:5: uriref
                    {
                    pushFollow(FOLLOW_uriref_in_resource422);
                    uriref16=uriref();

                    state._fsp--;


                     value = URI.create(uriref16); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:216:5: qname
                    {
                    pushFollow(FOLLOW_qname_in_resource430);
                    qname17=qname();

                    state._fsp--;


                     value = URI.create(qname17); 

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
    // $ANTLR end "resource"



    // $ANTLR start "uriref"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:219:1: uriref returns [String value] : LESS relativeURI GREATER ;
    public final String uriref() throws RecognitionException {
        String value = null;


        TurtleParser.relativeURI_return relativeURI18 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:220:3: ( LESS relativeURI GREATER )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:220:5: LESS relativeURI GREATER
            {
            match(input,LESS,FOLLOW_LESS_in_uriref451); 

            pushFollow(FOLLOW_relativeURI_in_uriref453);
            relativeURI18=relativeURI();

            state._fsp--;


            match(input,GREATER,FOLLOW_GREATER_in_uriref455); 

             value = (relativeURI18!=null?input.toString(relativeURI18.start,relativeURI18.stop):null); 

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
    // $ANTLR end "uriref"



    // $ANTLR start "qname"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:223:1: qname returns [String value] : ( prefix | COLON ) ( name )? ;
    public final String qname() throws RecognitionException {
        String value = null;


        TurtleParser.prefix_return prefix19 =null;

        TurtleParser.name_return name20 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:224:3: ( ( prefix | COLON ) ( name )? )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:224:5: ( prefix | COLON ) ( name )?
            {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:224:5: ( prefix | COLON )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==STRING_PREFIX) ) {
                alt10=1;
            }
            else if ( (LA10_0==COLON) ) {
                alt10=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;

            }
            switch (alt10) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:224:6: prefix
                    {
                    pushFollow(FOLLOW_prefix_in_qname478);
                    prefix19=prefix();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:224:13: COLON
                    {
                    match(input,COLON,FOLLOW_COLON_in_qname480); 

                    }
                    break;

            }


            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:224:20: ( name )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==VARNAME) ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:224:20: name
                    {
                    pushFollow(FOLLOW_name_in_qname483);
                    name20=name();

                    state._fsp--;


                    }
                    break;

            }



                  String prefix = "";
                  if ((prefix19!=null?input.toString(prefix19.start,prefix19.stop):null) != null) {
                    prefix = (prefix19!=null?input.toString(prefix19.start,prefix19.stop):null).substring(0, (prefix19!=null?input.toString(prefix19.start,prefix19.stop):null).length()-1); // remove the colon!
                  }
                  String uri = directives.get(prefix);
                  value = uri + (name20!=null?input.toString(name20.start,name20.stop):null); 
                

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
    // $ANTLR end "qname"



    // $ANTLR start "blank"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:234:1: blank : ( nodeID | BLANK );
    public final void blank() throws RecognitionException {
        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:235:3: ( nodeID | BLANK )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==BLANK_PREFIX) ) {
                alt12=1;
            }
            else if ( (LA12_0==BLANK) ) {
                alt12=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;

            }
            switch (alt12) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:235:5: nodeID
                    {
                    pushFollow(FOLLOW_nodeID_in_blank501);
                    nodeID();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:236:5: BLANK
                    {
                    match(input,BLANK,FOLLOW_BLANK_in_blank509); 

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
    // $ANTLR end "blank"



    // $ANTLR start "variable"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:239:1: variable returns [Variable value] : ( QUESTION | DOLLAR ) name ;
    public final Variable variable() throws RecognitionException {
        Variable value = null;


        TurtleParser.name_return name21 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:240:3: ( ( QUESTION | DOLLAR ) name )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:240:5: ( QUESTION | DOLLAR ) name
            {
            if ( input.LA(1)==DOLLAR||input.LA(1)==QUESTION ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            pushFollow(FOLLOW_name_in_variable532);
            name21=name();

            state._fsp--;



                   value = dfac.getVariable((name21!=null?input.toString(name21.start,name21.stop):null));
                   variableSet.add(value);
                

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
    // $ANTLR end "variable"



    // $ANTLR start "function"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:246:1: function returns [Function value] : resource LPAREN terms RPAREN ;
    public final Function function() throws RecognitionException {
        Function value = null;


        URI resource22 =null;

        Vector<Term> terms23 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:247:3: ( resource LPAREN terms RPAREN )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:247:5: resource LPAREN terms RPAREN
            {
            pushFollow(FOLLOW_resource_in_function551);
            resource22=resource();

            state._fsp--;


            match(input,LPAREN,FOLLOW_LPAREN_in_function553); 

            pushFollow(FOLLOW_terms_in_function555);
            terms23=terms();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_function557); 


                  String functionName = resource22.toString();
                  int arity = terms23.size();    
                  Predicate functionSymbol = null;  	
                  if (functionName.equals(OBDAVocabulary.RDFS_LITERAL_URI)) {
                	functionSymbol = dfac.getDataTypePredicateLiteral();
                  } else if (functionName.equals(OBDAVocabulary.XSD_STRING_URI)) {
                	functionSymbol = dfac.getDataTypePredicateString();
                  } else if (functionName.equals(OBDAVocabulary.XSD_INTEGER_URI)) {
                 	functionSymbol = dfac.getDataTypePredicateInteger();
                  } else if (functionName.equals(OBDAVocabulary.XSD_DECIMAL_URI)) {
                	functionSymbol = dfac.getDataTypePredicateDecimal();
                  } else if (functionName.equals(OBDAVocabulary.XSD_DOUBLE_URI)) {
                	functionSymbol = dfac.getDataTypePredicateDouble();
                  } else if (functionName.equals(OBDAVocabulary.XSD_DATETIME_URI)) {
                	functionSymbol = dfac.getDataTypePredicateDateTime();
                  } else if (functionName.equals(OBDAVocabulary.XSD_BOOLEAN_URI)) {
                	functionSymbol = dfac.getDataTypePredicateBoolean();
                  } else {
                    functionSymbol = dfac.getPredicate(URI.create(functionName), arity);
                  }
                  value = dfac.getFunctionalTerm(functionSymbol, terms23);
                

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
    // $ANTLR end "function"



    // $ANTLR start "terms"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:272:1: terms returns [Vector<Term> value] : t1= term ( COMMA t2= term )* ;
    public final Vector<Term> terms() throws RecognitionException {
        Vector<Term> value = null;


        Term t1 =null;

        Term t2 =null;



          value = new Vector<Term>();

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:276:3: (t1= term ( COMMA t2= term )* )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:276:5: t1= term ( COMMA t2= term )*
            {
            pushFollow(FOLLOW_term_in_terms583);
            t1=term();

            state._fsp--;


             value.add(t1); 

            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:276:40: ( COMMA t2= term )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0==COMMA) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:276:41: COMMA t2= term
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_terms588); 

            	    pushFollow(FOLLOW_term_in_terms592);
            	    t2=term();

            	    state._fsp--;


            	     value.add(t2); 

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
        return value;
    }
    // $ANTLR end "terms"



    // $ANTLR start "term"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:279:1: term returns [Term value] : ( function | variable | literal );
    public final Term term() throws RecognitionException {
        Term value = null;


        Function function24 =null;

        Variable variable25 =null;

        ValueConstant literal26 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:280:3: ( function | variable | literal )
            int alt14=3;
            switch ( input.LA(1) ) {
            case COLON:
            case LESS:
            case STRING_PREFIX:
                {
                alt14=1;
                }
                break;
            case DOLLAR:
            case QUESTION:
                {
                alt14=2;
                }
                break;
            case DECIMAL:
            case DECIMAL_NEGATIVE:
            case DECIMAL_POSITIVE:
            case DOUBLE:
            case DOUBLE_NEGATIVE:
            case DOUBLE_POSITIVE:
            case FALSE:
            case INTEGER:
            case INTEGER_NEGATIVE:
            case INTEGER_POSITIVE:
            case STRING_WITH_QUOTE_DOUBLE:
            case TRUE:
                {
                alt14=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;

            }

            switch (alt14) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:280:5: function
                    {
                    pushFollow(FOLLOW_function_in_term613);
                    function24=function();

                    state._fsp--;


                     value = function24; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:281:5: variable
                    {
                    pushFollow(FOLLOW_variable_in_term621);
                    variable25=variable();

                    state._fsp--;


                     value = variable25; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:282:5: literal
                    {
                    pushFollow(FOLLOW_literal_in_term629);
                    literal26=literal();

                    state._fsp--;


                     value = literal26; 

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
    // $ANTLR end "term"



    // $ANTLR start "literal"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:285:1: literal returns [ValueConstant value] : ( stringLiteral | numericLiteral | booleanLiteral );
    public final ValueConstant literal() throws RecognitionException {
        ValueConstant value = null;


        ValueConstant stringLiteral27 =null;

        ValueConstant numericLiteral28 =null;

        ValueConstant booleanLiteral29 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:288:3: ( stringLiteral | numericLiteral | booleanLiteral )
            int alt15=3;
            switch ( input.LA(1) ) {
            case STRING_WITH_QUOTE_DOUBLE:
                {
                alt15=1;
                }
                break;
            case DECIMAL:
            case DECIMAL_NEGATIVE:
            case DECIMAL_POSITIVE:
            case DOUBLE:
            case DOUBLE_NEGATIVE:
            case DOUBLE_POSITIVE:
            case INTEGER:
            case INTEGER_NEGATIVE:
            case INTEGER_POSITIVE:
                {
                alt15=2;
                }
                break;
            case FALSE:
            case TRUE:
                {
                alt15=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;

            }

            switch (alt15) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:288:5: stringLiteral
                    {
                    pushFollow(FOLLOW_stringLiteral_in_literal652);
                    stringLiteral27=stringLiteral();

                    state._fsp--;


                     value = stringLiteral27; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:289:5: numericLiteral
                    {
                    pushFollow(FOLLOW_numericLiteral_in_literal661);
                    numericLiteral28=numericLiteral();

                    state._fsp--;


                     value = numericLiteral28; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:290:5: booleanLiteral
                    {
                    pushFollow(FOLLOW_booleanLiteral_in_literal669);
                    booleanLiteral29=booleanLiteral();

                    state._fsp--;


                     value = booleanLiteral29; 

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



    // $ANTLR start "stringLiteral"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:293:1: stringLiteral returns [ValueConstant value] : STRING_WITH_QUOTE_DOUBLE ;
    public final ValueConstant stringLiteral() throws RecognitionException {
        ValueConstant value = null;


        Token STRING_WITH_QUOTE_DOUBLE30=null;

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:294:3: ( STRING_WITH_QUOTE_DOUBLE )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:294:5: STRING_WITH_QUOTE_DOUBLE
            {
            STRING_WITH_QUOTE_DOUBLE30=(Token)match(input,STRING_WITH_QUOTE_DOUBLE,FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_stringLiteral688); 

             
                  String str = (STRING_WITH_QUOTE_DOUBLE30!=null?STRING_WITH_QUOTE_DOUBLE30.getText():null);
                  value = dfac.getValueConstant(str.substring(1, str.length()-1), COL_TYPE.LITERAL); // without the double quotes
                

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
    // $ANTLR end "stringLiteral"



    // $ANTLR start "dataTypeString"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:300:1: dataTypeString : STRING_WITH_QUOTE_DOUBLE REFERENCE resource ;
    public final void dataTypeString() throws RecognitionException {
        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:301:3: ( STRING_WITH_QUOTE_DOUBLE REFERENCE resource )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:301:5: STRING_WITH_QUOTE_DOUBLE REFERENCE resource
            {
            match(input,STRING_WITH_QUOTE_DOUBLE,FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_dataTypeString706); 

            match(input,REFERENCE,FOLLOW_REFERENCE_in_dataTypeString708); 

            pushFollow(FOLLOW_resource_in_dataTypeString710);
            resource();

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
    // $ANTLR end "dataTypeString"



    // $ANTLR start "numericLiteral"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:304:1: numericLiteral returns [ValueConstant value] : ( numericUnsigned | numericPositive | numericNegative );
    public final ValueConstant numericLiteral() throws RecognitionException {
        ValueConstant value = null;


        ValueConstant numericUnsigned31 =null;

        ValueConstant numericPositive32 =null;

        ValueConstant numericNegative33 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:305:3: ( numericUnsigned | numericPositive | numericNegative )
            int alt16=3;
            switch ( input.LA(1) ) {
            case DECIMAL:
            case DOUBLE:
            case INTEGER:
                {
                alt16=1;
                }
                break;
            case DECIMAL_POSITIVE:
            case DOUBLE_POSITIVE:
            case INTEGER_POSITIVE:
                {
                alt16=2;
                }
                break;
            case DECIMAL_NEGATIVE:
            case DOUBLE_NEGATIVE:
            case INTEGER_NEGATIVE:
                {
                alt16=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;

            }

            switch (alt16) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:305:5: numericUnsigned
                    {
                    pushFollow(FOLLOW_numericUnsigned_in_numericLiteral729);
                    numericUnsigned31=numericUnsigned();

                    state._fsp--;


                     value = numericUnsigned31; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:306:5: numericPositive
                    {
                    pushFollow(FOLLOW_numericPositive_in_numericLiteral737);
                    numericPositive32=numericPositive();

                    state._fsp--;


                     value = numericPositive32; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:307:5: numericNegative
                    {
                    pushFollow(FOLLOW_numericNegative_in_numericLiteral745);
                    numericNegative33=numericNegative();

                    state._fsp--;


                     value = numericNegative33; 

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
    // $ANTLR end "numericLiteral"



    // $ANTLR start "nodeID"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:310:1: nodeID : BLANK_PREFIX name ;
    public final void nodeID() throws RecognitionException {
        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:311:3: ( BLANK_PREFIX name )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:311:5: BLANK_PREFIX name
            {
            match(input,BLANK_PREFIX,FOLLOW_BLANK_PREFIX_in_nodeID760); 

            pushFollow(FOLLOW_name_in_nodeID762);
            name();

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
    // $ANTLR end "nodeID"


    public static class relativeURI_return extends ParserRuleReturnScope {
    };


    // $ANTLR start "relativeURI"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:314:1: relativeURI : STRING_URI ;
    public final TurtleParser.relativeURI_return relativeURI() throws RecognitionException {
        TurtleParser.relativeURI_return retval = new TurtleParser.relativeURI_return();
        retval.start = input.LT(1);


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:315:3: ( STRING_URI )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:315:5: STRING_URI
            {
            match(input,STRING_URI,FOLLOW_STRING_URI_in_relativeURI775); 

            }

            retval.stop = input.LT(-1);


        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "relativeURI"


    public static class prefix_return extends ParserRuleReturnScope {
    };


    // $ANTLR start "prefix"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:318:1: prefix : STRING_PREFIX ;
    public final TurtleParser.prefix_return prefix() throws RecognitionException {
        TurtleParser.prefix_return retval = new TurtleParser.prefix_return();
        retval.start = input.LT(1);


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:319:3: ( STRING_PREFIX )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:319:5: STRING_PREFIX
            {
            match(input,STRING_PREFIX,FOLLOW_STRING_PREFIX_in_prefix788); 

            }

            retval.stop = input.LT(-1);


        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "prefix"


    public static class name_return extends ParserRuleReturnScope {
    };


    // $ANTLR start "name"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:322:1: name : VARNAME ;
    public final TurtleParser.name_return name() throws RecognitionException {
        TurtleParser.name_return retval = new TurtleParser.name_return();
        retval.start = input.LT(1);


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:323:3: ( VARNAME )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:323:5: VARNAME
            {
            match(input,VARNAME,FOLLOW_VARNAME_in_name801); 

            }

            retval.stop = input.LT(-1);


        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "name"



    // $ANTLR start "language"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:326:1: language : CHAR ;
    public final void language() throws RecognitionException {
        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:327:3: ( CHAR )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:327:5: CHAR
            {
            match(input,CHAR,FOLLOW_CHAR_in_language815); 

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
    // $ANTLR end "language"



    // $ANTLR start "booleanLiteral"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:330:1: booleanLiteral returns [ValueConstant value] : ( TRUE | FALSE );
    public final ValueConstant booleanLiteral() throws RecognitionException {
        ValueConstant value = null;


        Token TRUE34=null;
        Token FALSE35=null;

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:331:3: ( TRUE | FALSE )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==TRUE) ) {
                alt17=1;
            }
            else if ( (LA17_0==FALSE) ) {
                alt17=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;

            }
            switch (alt17) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:331:5: TRUE
                    {
                    TRUE34=(Token)match(input,TRUE,FOLLOW_TRUE_in_booleanLiteral832); 

                     value = dfac.getValueConstant((TRUE34!=null?TRUE34.getText():null), COL_TYPE.BOOLEAN); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:332:5: FALSE
                    {
                    FALSE35=(Token)match(input,FALSE,FOLLOW_FALSE_in_booleanLiteral841); 

                     value = dfac.getValueConstant((FALSE35!=null?FALSE35.getText():null), COL_TYPE.BOOLEAN); 

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
    // $ANTLR end "booleanLiteral"



    // $ANTLR start "numericUnsigned"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:335:1: numericUnsigned returns [ValueConstant value] : ( INTEGER | DOUBLE | DECIMAL );
    public final ValueConstant numericUnsigned() throws RecognitionException {
        ValueConstant value = null;


        Token INTEGER36=null;
        Token DOUBLE37=null;
        Token DECIMAL38=null;

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:336:3: ( INTEGER | DOUBLE | DECIMAL )
            int alt18=3;
            switch ( input.LA(1) ) {
            case INTEGER:
                {
                alt18=1;
                }
                break;
            case DOUBLE:
                {
                alt18=2;
                }
                break;
            case DECIMAL:
                {
                alt18=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;

            }

            switch (alt18) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:336:5: INTEGER
                    {
                    INTEGER36=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_numericUnsigned862); 

                     value = dfac.getValueConstant((INTEGER36!=null?INTEGER36.getText():null), COL_TYPE.INTEGER); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:337:5: DOUBLE
                    {
                    DOUBLE37=(Token)match(input,DOUBLE,FOLLOW_DOUBLE_in_numericUnsigned870); 

                     value = dfac.getValueConstant((DOUBLE37!=null?DOUBLE37.getText():null), COL_TYPE.DOUBLE); 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:338:5: DECIMAL
                    {
                    DECIMAL38=(Token)match(input,DECIMAL,FOLLOW_DECIMAL_in_numericUnsigned879); 

                     value = dfac.getValueConstant((DECIMAL38!=null?DECIMAL38.getText():null), COL_TYPE.DECIMAL); 

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
    // $ANTLR end "numericUnsigned"



    // $ANTLR start "numericPositive"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:341:1: numericPositive returns [ValueConstant value] : ( INTEGER_POSITIVE | DOUBLE_POSITIVE | DECIMAL_POSITIVE );
    public final ValueConstant numericPositive() throws RecognitionException {
        ValueConstant value = null;


        Token INTEGER_POSITIVE39=null;
        Token DOUBLE_POSITIVE40=null;
        Token DECIMAL_POSITIVE41=null;

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:342:3: ( INTEGER_POSITIVE | DOUBLE_POSITIVE | DECIMAL_POSITIVE )
            int alt19=3;
            switch ( input.LA(1) ) {
            case INTEGER_POSITIVE:
                {
                alt19=1;
                }
                break;
            case DOUBLE_POSITIVE:
                {
                alt19=2;
                }
                break;
            case DECIMAL_POSITIVE:
                {
                alt19=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;

            }

            switch (alt19) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:342:5: INTEGER_POSITIVE
                    {
                    INTEGER_POSITIVE39=(Token)match(input,INTEGER_POSITIVE,FOLLOW_INTEGER_POSITIVE_in_numericPositive900); 

                     value = dfac.getValueConstant((INTEGER_POSITIVE39!=null?INTEGER_POSITIVE39.getText():null), COL_TYPE.INTEGER); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:343:5: DOUBLE_POSITIVE
                    {
                    DOUBLE_POSITIVE40=(Token)match(input,DOUBLE_POSITIVE,FOLLOW_DOUBLE_POSITIVE_in_numericPositive908); 

                     value = dfac.getValueConstant((DOUBLE_POSITIVE40!=null?DOUBLE_POSITIVE40.getText():null), COL_TYPE.DOUBLE); 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:344:5: DECIMAL_POSITIVE
                    {
                    DECIMAL_POSITIVE41=(Token)match(input,DECIMAL_POSITIVE,FOLLOW_DECIMAL_POSITIVE_in_numericPositive917); 

                     value = dfac.getValueConstant((DECIMAL_POSITIVE41!=null?DECIMAL_POSITIVE41.getText():null), COL_TYPE.DECIMAL); 

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
    // $ANTLR end "numericPositive"



    // $ANTLR start "numericNegative"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:347:1: numericNegative returns [ValueConstant value] : ( INTEGER_NEGATIVE | DOUBLE_NEGATIVE | DECIMAL_NEGATIVE );
    public final ValueConstant numericNegative() throws RecognitionException {
        ValueConstant value = null;


        Token INTEGER_NEGATIVE42=null;
        Token DOUBLE_NEGATIVE43=null;
        Token DECIMAL_NEGATIVE44=null;

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:348:3: ( INTEGER_NEGATIVE | DOUBLE_NEGATIVE | DECIMAL_NEGATIVE )
            int alt20=3;
            switch ( input.LA(1) ) {
            case INTEGER_NEGATIVE:
                {
                alt20=1;
                }
                break;
            case DOUBLE_NEGATIVE:
                {
                alt20=2;
                }
                break;
            case DECIMAL_NEGATIVE:
                {
                alt20=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;

            }

            switch (alt20) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:348:5: INTEGER_NEGATIVE
                    {
                    INTEGER_NEGATIVE42=(Token)match(input,INTEGER_NEGATIVE,FOLLOW_INTEGER_NEGATIVE_in_numericNegative938); 

                     value = dfac.getValueConstant((INTEGER_NEGATIVE42!=null?INTEGER_NEGATIVE42.getText():null), COL_TYPE.INTEGER); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:349:5: DOUBLE_NEGATIVE
                    {
                    DOUBLE_NEGATIVE43=(Token)match(input,DOUBLE_NEGATIVE,FOLLOW_DOUBLE_NEGATIVE_in_numericNegative946); 

                     value = dfac.getValueConstant((DOUBLE_NEGATIVE43!=null?DOUBLE_NEGATIVE43.getText():null), COL_TYPE.DOUBLE); 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:350:5: DECIMAL_NEGATIVE
                    {
                    DECIMAL_NEGATIVE44=(Token)match(input,DECIMAL_NEGATIVE,FOLLOW_DECIMAL_NEGATIVE_in_numericNegative955); 

                     value = dfac.getValueConstant((DECIMAL_NEGATIVE44!=null?DECIMAL_NEGATIVE44.getText():null), COL_TYPE.DECIMAL); 

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
    // $ANTLR end "numericNegative"

    // Delegated rules


 

    public static final BitSet FOLLOW_statement_in_parse57 = new BitSet(new long[]{0x0201010000410200L});
    public static final BitSet FOLLOW_EOF_in_parse60 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_directive_in_statement81 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_PERIOD_in_statement83 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_triples_in_statement89 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_PERIOD_in_statement91 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_base_in_directive106 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_prefixID_in_directive112 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_in_base125 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_BASE_in_base127 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_uriref_in_base129 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_in_prefixID144 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_PREFIX_in_prefixID146 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_prefix_in_prefixID148 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_uriref_in_prefixID150 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_subject_in_triples171 = new BitSet(new long[]{0x0200010000010000L,0x0000000000000008L});
    public static final BitSet FOLLOW_predicateObjectList_in_triples180 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_verb_in_predicateObjectList210 = new BitSet(new long[]{0x520101E043DD0000L});
    public static final BitSet FOLLOW_objectList_in_predicateObjectList214 = new BitSet(new long[]{0x0080000000000002L});
    public static final BitSet FOLLOW_SEMI_in_predicateObjectList224 = new BitSet(new long[]{0x0200010000010000L,0x0000000000000008L});
    public static final BitSet FOLLOW_verb_in_predicateObjectList228 = new BitSet(new long[]{0x520101E043DD0000L});
    public static final BitSet FOLLOW_objectList_in_predicateObjectList232 = new BitSet(new long[]{0x0080000000000002L});
    public static final BitSet FOLLOW_predicate_in_verb255 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_67_in_verb263 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_object_in_objectList291 = new BitSet(new long[]{0x0000000000020002L});
    public static final BitSet FOLLOW_COMMA_in_objectList296 = new BitSet(new long[]{0x520101E043DD0000L});
    public static final BitSet FOLLOW_object_in_objectList300 = new BitSet(new long[]{0x0000000000020002L});
    public static final BitSet FOLLOW_variable_in_subject324 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_in_subject332 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_resource_in_predicate353 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_resource_in_object374 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_in_object383 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_object391 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_object400 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_uriref_in_resource422 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qname_in_resource430 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_uriref451 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_relativeURI_in_uriref453 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_GREATER_in_uriref455 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_prefix_in_qname478 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_qname480 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_qname483 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nodeID_in_blank501 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BLANK_in_blank509 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_variable526 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_variable532 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_resource_in_function551 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_LPAREN_in_function553 = new BitSet(new long[]{0x520101E043DD0000L});
    public static final BitSet FOLLOW_terms_in_function555 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_function557 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_terms583 = new BitSet(new long[]{0x0000000000020002L});
    public static final BitSet FOLLOW_COMMA_in_terms588 = new BitSet(new long[]{0x520101E043DD0000L});
    public static final BitSet FOLLOW_term_in_terms592 = new BitSet(new long[]{0x0000000000020002L});
    public static final BitSet FOLLOW_function_in_term613 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_term621 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_term629 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stringLiteral_in_literal652 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericLiteral_in_literal661 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanLiteral_in_literal669 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_stringLiteral688 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_dataTypeString706 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_REFERENCE_in_dataTypeString708 = new BitSet(new long[]{0x0200010000010000L});
    public static final BitSet FOLLOW_resource_in_dataTypeString710 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericUnsigned_in_numericLiteral729 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericPositive_in_numericLiteral737 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericNegative_in_numericLiteral745 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BLANK_PREFIX_in_nodeID760 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_nodeID762 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_URI_in_relativeURI775 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_PREFIX_in_prefix788 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VARNAME_in_name801 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHAR_in_language815 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_booleanLiteral832 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_booleanLiteral841 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_in_numericUnsigned862 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLE_in_numericUnsigned870 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_in_numericUnsigned879 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_POSITIVE_in_numericPositive900 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLE_POSITIVE_in_numericPositive908 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_POSITIVE_in_numericPositive917 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_NEGATIVE_in_numericNegative938 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLE_NEGATIVE_in_numericNegative946 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_NEGATIVE_in_numericNegative955 = new BitSet(new long[]{0x0000000000000002L});

}