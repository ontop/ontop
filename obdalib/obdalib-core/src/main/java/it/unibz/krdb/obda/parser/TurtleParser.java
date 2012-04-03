// $ANTLR 3.4 C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g 2012-04-03 14:01:51

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

            pushFollow(FOLLOW_predicateObjectList_in_triples175);
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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:146:1: predicateObjectList returns [List<Atom> value] : v1= verb l1= objectList ( SEMI v2= verb l2= objectList )* ;
    public final List<Atom> predicateObjectList() throws RecognitionException {
        List<Atom> value = null;


        URI v1 =null;

        List<Term> l1 =null;

        URI v2 =null;

        List<Term> l2 =null;



           value = new LinkedList<Atom>();

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:150:3: (v1= verb l1= objectList ( SEMI v2= verb l2= objectList )* )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:150:5: v1= verb l1= objectList ( SEMI v2= verb l2= objectList )*
            {
            pushFollow(FOLLOW_verb_in_predicateObjectList205);
            v1=verb();

            state._fsp--;


            pushFollow(FOLLOW_objectList_in_predicateObjectList209);
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
                

            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:164:5: ( SEMI v2= verb l2= objectList )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==SEMI) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:164:6: SEMI v2= verb l2= objectList
            	    {
            	    match(input,SEMI,FOLLOW_SEMI_in_predicateObjectList219); 

            	    pushFollow(FOLLOW_verb_in_predicateObjectList223);
            	    v2=verb();

            	    state._fsp--;


            	    pushFollow(FOLLOW_objectList_in_predicateObjectList227);
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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:180:1: verb returns [URI value] : ( predicate | 'a' );
    public final URI verb() throws RecognitionException {
        URI value = null;


        URI predicate8 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:181:3: ( predicate | 'a' )
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
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:181:5: predicate
                    {
                    pushFollow(FOLLOW_predicate_in_verb250);
                    predicate8=predicate();

                    state._fsp--;


                     value = predicate8; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:182:5: 'a'
                    {
                    match(input,67,FOLLOW_67_in_verb258); 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:185:1: objectList returns [List<Term> value] : o1= object ( COMMA o2= object )* ;
    public final List<Term> objectList() throws RecognitionException {
        List<Term> value = null;


        Term o1 =null;

        Term o2 =null;



          value = new ArrayList<Term>();

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:189:3: (o1= object ( COMMA o2= object )* )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:189:5: o1= object ( COMMA o2= object )*
            {
            pushFollow(FOLLOW_object_in_objectList286);
            o1=object();

            state._fsp--;


             value.add(o1); 

            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:189:42: ( COMMA o2= object )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==COMMA) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:189:43: COMMA o2= object
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_objectList291); 

            	    pushFollow(FOLLOW_object_in_objectList295);
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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:192:1: subject returns [Term value] : ( variable | function );
    public final Term subject() throws RecognitionException {
        Term value = null;


        Variable variable9 =null;

        Function function10 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:195:3: ( variable | function )
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
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:195:5: variable
                    {
                    pushFollow(FOLLOW_variable_in_subject319);
                    variable9=variable();

                    state._fsp--;


                     value = variable9; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:196:5: function
                    {
                    pushFollow(FOLLOW_function_in_subject327);
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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:199:1: predicate returns [URI value] : resource ;
    public final URI predicate() throws RecognitionException {
        URI value = null;


        URI resource11 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:200:3: ( resource )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:200:5: resource
            {
            pushFollow(FOLLOW_resource_in_predicate348);
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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:203:1: object returns [Term value] : ( resource | function | literal | variable | dataTypeFunction );
    public final Term object() throws RecognitionException {
        Term value = null;


        URI resource12 =null;

        Function function13 =null;

        Term literal14 =null;

        Variable variable15 =null;

        Function dataTypeFunction16 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:204:3: ( resource | function | literal | variable | dataTypeFunction )
            int alt8=5;
            switch ( input.LA(1) ) {
            case LESS:
                {
                int LA8_1 = input.LA(2);

                if ( (LA8_1==STRING_URI) ) {
                    int LA8_6 = input.LA(3);

                    if ( (LA8_6==GREATER) ) {
                        int LA8_11 = input.LA(4);

                        if ( (LA8_11==COMMA||LA8_11==PERIOD||LA8_11==SEMI) ) {
                            alt8=1;
                        }
                        else if ( (LA8_11==LPAREN) ) {
                            alt8=2;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 8, 11, input);

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
                int LA8_5 = input.LA(2);

                if ( (LA8_5==VARNAME) ) {
                    int LA8_10 = input.LA(3);

                    if ( (LA8_10==COMMA||LA8_10==PERIOD||LA8_10==SEMI) ) {
                        alt8=4;
                    }
                    else if ( (LA8_10==AT||LA8_10==REFERENCE) ) {
                        alt8=5;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 8, 10, input);

                        throw nvae;

                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 5, input);

                    throw nvae;

                }
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;

            }

            switch (alt8) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:204:5: resource
                    {
                    pushFollow(FOLLOW_resource_in_object369);
                    resource12=resource();

                    state._fsp--;


                     value = dfac.getURIConstant(resource12); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:205:5: function
                    {
                    pushFollow(FOLLOW_function_in_object378);
                    function13=function();

                    state._fsp--;


                     value = function13; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:206:5: literal
                    {
                    pushFollow(FOLLOW_literal_in_object386);
                    literal14=literal();

                    state._fsp--;


                     value = literal14; 

                    }
                    break;
                case 4 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:207:5: variable
                    {
                    pushFollow(FOLLOW_variable_in_object395);
                    variable15=variable();

                    state._fsp--;


                     value = variable15; 

                    }
                    break;
                case 5 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:208:5: dataTypeFunction
                    {
                    pushFollow(FOLLOW_dataTypeFunction_in_object403);
                    dataTypeFunction16=dataTypeFunction();

                    state._fsp--;


                     value = dataTypeFunction16; 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:212:1: resource returns [URI value] : ( uriref | qname );
    public final URI resource() throws RecognitionException {
        URI value = null;


        String uriref17 =null;

        String qname18 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:213:3: ( uriref | qname )
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
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:213:5: uriref
                    {
                    pushFollow(FOLLOW_uriref_in_resource425);
                    uriref17=uriref();

                    state._fsp--;


                     value = URI.create(uriref17); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:214:5: qname
                    {
                    pushFollow(FOLLOW_qname_in_resource433);
                    qname18=qname();

                    state._fsp--;


                     value = URI.create(qname18); 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:217:1: uriref returns [String value] : LESS relativeURI GREATER ;
    public final String uriref() throws RecognitionException {
        String value = null;


        TurtleParser.relativeURI_return relativeURI19 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:218:3: ( LESS relativeURI GREATER )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:218:5: LESS relativeURI GREATER
            {
            match(input,LESS,FOLLOW_LESS_in_uriref454); 

            pushFollow(FOLLOW_relativeURI_in_uriref456);
            relativeURI19=relativeURI();

            state._fsp--;


            match(input,GREATER,FOLLOW_GREATER_in_uriref458); 

             value = (relativeURI19!=null?input.toString(relativeURI19.start,relativeURI19.stop):null); 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:221:1: qname returns [String value] : ( prefix | COLON ) ( name )? ;
    public final String qname() throws RecognitionException {
        String value = null;


        TurtleParser.prefix_return prefix20 =null;

        TurtleParser.name_return name21 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:222:3: ( ( prefix | COLON ) ( name )? )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:222:5: ( prefix | COLON ) ( name )?
            {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:222:5: ( prefix | COLON )
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
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:222:6: prefix
                    {
                    pushFollow(FOLLOW_prefix_in_qname481);
                    prefix20=prefix();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:222:13: COLON
                    {
                    match(input,COLON,FOLLOW_COLON_in_qname483); 

                    }
                    break;

            }


            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:222:20: ( name )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==VARNAME) ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:222:20: name
                    {
                    pushFollow(FOLLOW_name_in_qname486);
                    name21=name();

                    state._fsp--;


                    }
                    break;

            }



                  String prefix = "";
                  if ((prefix20!=null?input.toString(prefix20.start,prefix20.stop):null) != null) {
                    prefix = (prefix20!=null?input.toString(prefix20.start,prefix20.stop):null).substring(0, (prefix20!=null?input.toString(prefix20.start,prefix20.stop):null).length()-1); // remove the colon!
                  }
                  String uri = directives.get(prefix);
                  value = uri + (name21!=null?input.toString(name21.start,name21.stop):null); 
                

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:232:1: blank : ( nodeID | BLANK );
    public final void blank() throws RecognitionException {
        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:233:3: ( nodeID | BLANK )
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
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:233:5: nodeID
                    {
                    pushFollow(FOLLOW_nodeID_in_blank504);
                    nodeID();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:234:5: BLANK
                    {
                    match(input,BLANK,FOLLOW_BLANK_in_blank512); 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:237:1: variable returns [Variable value] : ( QUESTION | DOLLAR ) name ;
    public final Variable variable() throws RecognitionException {
        Variable value = null;


        TurtleParser.name_return name22 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:238:3: ( ( QUESTION | DOLLAR ) name )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:238:5: ( QUESTION | DOLLAR ) name
            {
            if ( input.LA(1)==DOLLAR||input.LA(1)==QUESTION ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            pushFollow(FOLLOW_name_in_variable535);
            name22=name();

            state._fsp--;



                   value = dfac.getVariable((name22!=null?input.toString(name22.start,name22.stop):null));
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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:244:1: function returns [Function value] : resource LPAREN terms RPAREN ;
    public final Function function() throws RecognitionException {
        Function value = null;


        URI resource23 =null;

        Vector<Term> terms24 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:245:3: ( resource LPAREN terms RPAREN )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:245:5: resource LPAREN terms RPAREN
            {
            pushFollow(FOLLOW_resource_in_function554);
            resource23=resource();

            state._fsp--;


            match(input,LPAREN,FOLLOW_LPAREN_in_function556); 

            pushFollow(FOLLOW_terms_in_function558);
            terms24=terms();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_function560); 


                  String functionName = resource23.toString();
                  int arity = terms24.size();    
                  Predicate functionSymbol = dfac.getPredicate(URI.create(functionName), arity);
                  value = dfac.getFunctionalTerm(functionSymbol, terms24);
                

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



    // $ANTLR start "dataTypeFunction"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:253:1: dataTypeFunction returns [Function value] : ( variable AT language | variable REFERENCE resource );
    public final Function dataTypeFunction() throws RecognitionException {
        Function value = null;


        Variable variable25 =null;

        TurtleParser.language_return language26 =null;

        Variable variable27 =null;

        URI resource28 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:254:3: ( variable AT language | variable REFERENCE resource )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==DOLLAR||LA13_0==QUESTION) ) {
                int LA13_1 = input.LA(2);

                if ( (LA13_1==VARNAME) ) {
                    int LA13_2 = input.LA(3);

                    if ( (LA13_2==AT) ) {
                        alt13=1;
                    }
                    else if ( (LA13_2==REFERENCE) ) {
                        alt13=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 13, 2, input);

                        throw nvae;

                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 1, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;

            }
            switch (alt13) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:254:5: variable AT language
                    {
                    pushFollow(FOLLOW_variable_in_dataTypeFunction579);
                    variable25=variable();

                    state._fsp--;


                    match(input,AT,FOLLOW_AT_in_dataTypeFunction581); 

                    pushFollow(FOLLOW_language_in_dataTypeFunction583);
                    language26=language();

                    state._fsp--;



                          Predicate functionSymbol = dfac.getDataTypePredicateLiteral();
                          Variable var = variable25;
                          ValueConstant lang = dfac.getValueConstant((language26!=null?input.toString(language26.start,language26.stop):null));
                          value = dfac.getFunctionalTerm(functionSymbol, var, lang);
                        

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:260:5: variable REFERENCE resource
                    {
                    pushFollow(FOLLOW_variable_in_dataTypeFunction591);
                    variable27=variable();

                    state._fsp--;


                    match(input,REFERENCE,FOLLOW_REFERENCE_in_dataTypeFunction593); 

                    pushFollow(FOLLOW_resource_in_dataTypeFunction595);
                    resource28=resource();

                    state._fsp--;



                          Variable var = variable27;
                          String functionName = resource28.toString();
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
                          }
                          value = dfac.getFunctionalTerm(functionSymbol, var);
                        

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
    // $ANTLR end "dataTypeFunction"



    // $ANTLR start "terms"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:283:1: terms returns [Vector<Term> value] : t1= term ( COMMA t2= term )* ;
    public final Vector<Term> terms() throws RecognitionException {
        Vector<Term> value = null;


        Term t1 =null;

        Term t2 =null;



          value = new Vector<Term>();

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:287:3: (t1= term ( COMMA t2= term )* )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:287:5: t1= term ( COMMA t2= term )*
            {
            pushFollow(FOLLOW_term_in_terms621);
            t1=term();

            state._fsp--;


             value.add(t1); 

            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:287:40: ( COMMA t2= term )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==COMMA) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:287:41: COMMA t2= term
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_terms626); 

            	    pushFollow(FOLLOW_term_in_terms630);
            	    t2=term();

            	    state._fsp--;


            	     value.add(t2); 

            	    }
            	    break;

            	default :
            	    break loop14;
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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:290:1: term returns [Term value] : ( function | variable | literal );
    public final Term term() throws RecognitionException {
        Term value = null;


        Function function29 =null;

        Variable variable30 =null;

        Term literal31 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:291:3: ( function | variable | literal )
            int alt15=3;
            switch ( input.LA(1) ) {
            case COLON:
            case LESS:
            case STRING_PREFIX:
                {
                alt15=1;
                }
                break;
            case DOLLAR:
            case QUESTION:
                {
                alt15=2;
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
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:291:5: function
                    {
                    pushFollow(FOLLOW_function_in_term651);
                    function29=function();

                    state._fsp--;


                     value = function29; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:292:5: variable
                    {
                    pushFollow(FOLLOW_variable_in_term659);
                    variable30=variable();

                    state._fsp--;


                     value = variable30; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:293:5: literal
                    {
                    pushFollow(FOLLOW_literal_in_term667);
                    literal31=literal();

                    state._fsp--;


                     value = literal31; 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:296:1: literal returns [Term value] : ( stringLiteral ( AT language )? | dataTypeString | numericLiteral | booleanLiteral );
    public final Term literal() throws RecognitionException {
        Term value = null;


        ValueConstant stringLiteral32 =null;

        TurtleParser.language_return language33 =null;

        Term dataTypeString34 =null;

        ValueConstant numericLiteral35 =null;

        ValueConstant booleanLiteral36 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:297:3: ( stringLiteral ( AT language )? | dataTypeString | numericLiteral | booleanLiteral )
            int alt17=4;
            switch ( input.LA(1) ) {
            case STRING_WITH_QUOTE_DOUBLE:
                {
                int LA17_1 = input.LA(2);

                if ( (LA17_1==AT||LA17_1==COMMA||LA17_1==PERIOD||LA17_1==RPAREN||LA17_1==SEMI) ) {
                    alt17=1;
                }
                else if ( (LA17_1==REFERENCE) ) {
                    alt17=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 1, input);

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
            case INTEGER:
            case INTEGER_NEGATIVE:
            case INTEGER_POSITIVE:
                {
                alt17=3;
                }
                break;
            case FALSE:
            case TRUE:
                {
                alt17=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;

            }

            switch (alt17) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:297:5: stringLiteral ( AT language )?
                    {
                    pushFollow(FOLLOW_stringLiteral_in_literal688);
                    stringLiteral32=stringLiteral();

                    state._fsp--;


                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:297:19: ( AT language )?
                    int alt16=2;
                    int LA16_0 = input.LA(1);

                    if ( (LA16_0==AT) ) {
                        alt16=1;
                    }
                    switch (alt16) {
                        case 1 :
                            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:297:20: AT language
                            {
                            match(input,AT,FOLLOW_AT_in_literal691); 

                            pushFollow(FOLLOW_language_in_literal693);
                            language33=language();

                            state._fsp--;


                            }
                            break;

                    }



                           Predicate functionSymbol = dfac.getDataTypePredicateLiteral();
                           ValueConstant constant = stringLiteral32;
                           if ((language33!=null?input.toString(language33.start,language33.stop):null) != null && (language33!=null?input.toString(language33.start,language33.stop):null).trim().length() > 0) {
                             constant = dfac.getValueConstant(constant.getValue(), (language33!=null?input.toString(language33.start,language33.stop):null));
                           }
                           value = dfac.getFunctionalTerm(functionSymbol, constant);
                        

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:305:5: dataTypeString
                    {
                    pushFollow(FOLLOW_dataTypeString_in_literal703);
                    dataTypeString34=dataTypeString();

                    state._fsp--;


                     value = dataTypeString34; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:306:5: numericLiteral
                    {
                    pushFollow(FOLLOW_numericLiteral_in_literal711);
                    numericLiteral35=numericLiteral();

                    state._fsp--;


                     value = numericLiteral35; 

                    }
                    break;
                case 4 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:307:5: booleanLiteral
                    {
                    pushFollow(FOLLOW_booleanLiteral_in_literal719);
                    booleanLiteral36=booleanLiteral();

                    state._fsp--;


                     value = booleanLiteral36; 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:310:1: stringLiteral returns [ValueConstant value] : STRING_WITH_QUOTE_DOUBLE ;
    public final ValueConstant stringLiteral() throws RecognitionException {
        ValueConstant value = null;


        Token STRING_WITH_QUOTE_DOUBLE37=null;

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:311:3: ( STRING_WITH_QUOTE_DOUBLE )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:311:5: STRING_WITH_QUOTE_DOUBLE
            {
            STRING_WITH_QUOTE_DOUBLE37=(Token)match(input,STRING_WITH_QUOTE_DOUBLE,FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_stringLiteral738); 

             
                  String str = (STRING_WITH_QUOTE_DOUBLE37!=null?STRING_WITH_QUOTE_DOUBLE37.getText():null);
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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:317:1: dataTypeString returns [Term value] : stringLiteral REFERENCE resource ;
    public final Term dataTypeString() throws RecognitionException {
        Term value = null;


        ValueConstant stringLiteral38 =null;

        URI resource39 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:318:3: ( stringLiteral REFERENCE resource )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:318:6: stringLiteral REFERENCE resource
            {
            pushFollow(FOLLOW_stringLiteral_in_dataTypeString761);
            stringLiteral38=stringLiteral();

            state._fsp--;


            match(input,REFERENCE,FOLLOW_REFERENCE_in_dataTypeString763); 

            pushFollow(FOLLOW_resource_in_dataTypeString765);
            resource39=resource();

            state._fsp--;



                  ValueConstant constant = stringLiteral38;
                  String functionName = resource39.toString();
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
                  }
                  value = dfac.getFunctionalTerm(functionSymbol, constant);
                

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
    // $ANTLR end "dataTypeString"



    // $ANTLR start "numericLiteral"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:341:1: numericLiteral returns [ValueConstant value] : ( numericUnsigned | numericPositive | numericNegative );
    public final ValueConstant numericLiteral() throws RecognitionException {
        ValueConstant value = null;


        ValueConstant numericUnsigned40 =null;

        ValueConstant numericPositive41 =null;

        ValueConstant numericNegative42 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:342:3: ( numericUnsigned | numericPositive | numericNegative )
            int alt18=3;
            switch ( input.LA(1) ) {
            case DECIMAL:
            case DOUBLE:
            case INTEGER:
                {
                alt18=1;
                }
                break;
            case DECIMAL_POSITIVE:
            case DOUBLE_POSITIVE:
            case INTEGER_POSITIVE:
                {
                alt18=2;
                }
                break;
            case DECIMAL_NEGATIVE:
            case DOUBLE_NEGATIVE:
            case INTEGER_NEGATIVE:
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
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:342:5: numericUnsigned
                    {
                    pushFollow(FOLLOW_numericUnsigned_in_numericLiteral786);
                    numericUnsigned40=numericUnsigned();

                    state._fsp--;


                     value = numericUnsigned40; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:343:5: numericPositive
                    {
                    pushFollow(FOLLOW_numericPositive_in_numericLiteral794);
                    numericPositive41=numericPositive();

                    state._fsp--;


                     value = numericPositive41; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:344:5: numericNegative
                    {
                    pushFollow(FOLLOW_numericNegative_in_numericLiteral802);
                    numericNegative42=numericNegative();

                    state._fsp--;


                     value = numericNegative42; 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:347:1: nodeID : BLANK_PREFIX name ;
    public final void nodeID() throws RecognitionException {
        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:348:3: ( BLANK_PREFIX name )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:348:5: BLANK_PREFIX name
            {
            match(input,BLANK_PREFIX,FOLLOW_BLANK_PREFIX_in_nodeID817); 

            pushFollow(FOLLOW_name_in_nodeID819);
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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:351:1: relativeURI : STRING_URI ;
    public final TurtleParser.relativeURI_return relativeURI() throws RecognitionException {
        TurtleParser.relativeURI_return retval = new TurtleParser.relativeURI_return();
        retval.start = input.LT(1);


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:352:3: ( STRING_URI )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:352:5: STRING_URI
            {
            match(input,STRING_URI,FOLLOW_STRING_URI_in_relativeURI832); 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:355:1: prefix : STRING_PREFIX ;
    public final TurtleParser.prefix_return prefix() throws RecognitionException {
        TurtleParser.prefix_return retval = new TurtleParser.prefix_return();
        retval.start = input.LT(1);


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:356:3: ( STRING_PREFIX )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:356:5: STRING_PREFIX
            {
            match(input,STRING_PREFIX,FOLLOW_STRING_PREFIX_in_prefix845); 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:359:1: name : VARNAME ;
    public final TurtleParser.name_return name() throws RecognitionException {
        TurtleParser.name_return retval = new TurtleParser.name_return();
        retval.start = input.LT(1);


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:360:3: ( VARNAME )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:360:5: VARNAME
            {
            match(input,VARNAME,FOLLOW_VARNAME_in_name858); 

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


    public static class language_return extends ParserRuleReturnScope {
    };


    // $ANTLR start "language"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:363:1: language : VARNAME ;
    public final TurtleParser.language_return language() throws RecognitionException {
        TurtleParser.language_return retval = new TurtleParser.language_return();
        retval.start = input.LT(1);


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:364:3: ( VARNAME )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:364:5: VARNAME
            {
            match(input,VARNAME,FOLLOW_VARNAME_in_language872); 

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
    // $ANTLR end "language"



    // $ANTLR start "booleanLiteral"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:367:1: booleanLiteral returns [ValueConstant value] : ( TRUE | FALSE );
    public final ValueConstant booleanLiteral() throws RecognitionException {
        ValueConstant value = null;


        Token TRUE43=null;
        Token FALSE44=null;

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:368:3: ( TRUE | FALSE )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==TRUE) ) {
                alt19=1;
            }
            else if ( (LA19_0==FALSE) ) {
                alt19=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;

            }
            switch (alt19) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:368:5: TRUE
                    {
                    TRUE43=(Token)match(input,TRUE,FOLLOW_TRUE_in_booleanLiteral889); 

                     value = dfac.getValueConstant((TRUE43!=null?TRUE43.getText():null), COL_TYPE.BOOLEAN); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:369:5: FALSE
                    {
                    FALSE44=(Token)match(input,FALSE,FOLLOW_FALSE_in_booleanLiteral898); 

                     value = dfac.getValueConstant((FALSE44!=null?FALSE44.getText():null), COL_TYPE.BOOLEAN); 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:372:1: numericUnsigned returns [ValueConstant value] : ( INTEGER | DOUBLE | DECIMAL );
    public final ValueConstant numericUnsigned() throws RecognitionException {
        ValueConstant value = null;


        Token INTEGER45=null;
        Token DOUBLE46=null;
        Token DECIMAL47=null;

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:373:3: ( INTEGER | DOUBLE | DECIMAL )
            int alt20=3;
            switch ( input.LA(1) ) {
            case INTEGER:
                {
                alt20=1;
                }
                break;
            case DOUBLE:
                {
                alt20=2;
                }
                break;
            case DECIMAL:
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
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:373:5: INTEGER
                    {
                    INTEGER45=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_numericUnsigned919); 

                     value = dfac.getValueConstant((INTEGER45!=null?INTEGER45.getText():null), COL_TYPE.INTEGER); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:374:5: DOUBLE
                    {
                    DOUBLE46=(Token)match(input,DOUBLE,FOLLOW_DOUBLE_in_numericUnsigned927); 

                     value = dfac.getValueConstant((DOUBLE46!=null?DOUBLE46.getText():null), COL_TYPE.DOUBLE); 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:375:5: DECIMAL
                    {
                    DECIMAL47=(Token)match(input,DECIMAL,FOLLOW_DECIMAL_in_numericUnsigned936); 

                     value = dfac.getValueConstant((DECIMAL47!=null?DECIMAL47.getText():null), COL_TYPE.DECIMAL); 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:378:1: numericPositive returns [ValueConstant value] : ( INTEGER_POSITIVE | DOUBLE_POSITIVE | DECIMAL_POSITIVE );
    public final ValueConstant numericPositive() throws RecognitionException {
        ValueConstant value = null;


        Token INTEGER_POSITIVE48=null;
        Token DOUBLE_POSITIVE49=null;
        Token DECIMAL_POSITIVE50=null;

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:379:3: ( INTEGER_POSITIVE | DOUBLE_POSITIVE | DECIMAL_POSITIVE )
            int alt21=3;
            switch ( input.LA(1) ) {
            case INTEGER_POSITIVE:
                {
                alt21=1;
                }
                break;
            case DOUBLE_POSITIVE:
                {
                alt21=2;
                }
                break;
            case DECIMAL_POSITIVE:
                {
                alt21=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 21, 0, input);

                throw nvae;

            }

            switch (alt21) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:379:5: INTEGER_POSITIVE
                    {
                    INTEGER_POSITIVE48=(Token)match(input,INTEGER_POSITIVE,FOLLOW_INTEGER_POSITIVE_in_numericPositive957); 

                     value = dfac.getValueConstant((INTEGER_POSITIVE48!=null?INTEGER_POSITIVE48.getText():null), COL_TYPE.INTEGER); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:380:5: DOUBLE_POSITIVE
                    {
                    DOUBLE_POSITIVE49=(Token)match(input,DOUBLE_POSITIVE,FOLLOW_DOUBLE_POSITIVE_in_numericPositive965); 

                     value = dfac.getValueConstant((DOUBLE_POSITIVE49!=null?DOUBLE_POSITIVE49.getText():null), COL_TYPE.DOUBLE); 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:381:5: DECIMAL_POSITIVE
                    {
                    DECIMAL_POSITIVE50=(Token)match(input,DECIMAL_POSITIVE,FOLLOW_DECIMAL_POSITIVE_in_numericPositive974); 

                     value = dfac.getValueConstant((DECIMAL_POSITIVE50!=null?DECIMAL_POSITIVE50.getText():null), COL_TYPE.DECIMAL); 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:384:1: numericNegative returns [ValueConstant value] : ( INTEGER_NEGATIVE | DOUBLE_NEGATIVE | DECIMAL_NEGATIVE );
    public final ValueConstant numericNegative() throws RecognitionException {
        ValueConstant value = null;


        Token INTEGER_NEGATIVE51=null;
        Token DOUBLE_NEGATIVE52=null;
        Token DECIMAL_NEGATIVE53=null;

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:385:3: ( INTEGER_NEGATIVE | DOUBLE_NEGATIVE | DECIMAL_NEGATIVE )
            int alt22=3;
            switch ( input.LA(1) ) {
            case INTEGER_NEGATIVE:
                {
                alt22=1;
                }
                break;
            case DOUBLE_NEGATIVE:
                {
                alt22=2;
                }
                break;
            case DECIMAL_NEGATIVE:
                {
                alt22=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;

            }

            switch (alt22) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:385:5: INTEGER_NEGATIVE
                    {
                    INTEGER_NEGATIVE51=(Token)match(input,INTEGER_NEGATIVE,FOLLOW_INTEGER_NEGATIVE_in_numericNegative995); 

                     value = dfac.getValueConstant((INTEGER_NEGATIVE51!=null?INTEGER_NEGATIVE51.getText():null), COL_TYPE.INTEGER); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:386:5: DOUBLE_NEGATIVE
                    {
                    DOUBLE_NEGATIVE52=(Token)match(input,DOUBLE_NEGATIVE,FOLLOW_DOUBLE_NEGATIVE_in_numericNegative1003); 

                     value = dfac.getValueConstant((DOUBLE_NEGATIVE52!=null?DOUBLE_NEGATIVE52.getText():null), COL_TYPE.DOUBLE); 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:387:5: DECIMAL_NEGATIVE
                    {
                    DECIMAL_NEGATIVE53=(Token)match(input,DECIMAL_NEGATIVE,FOLLOW_DECIMAL_NEGATIVE_in_numericNegative1012); 

                     value = dfac.getValueConstant((DECIMAL_NEGATIVE53!=null?DECIMAL_NEGATIVE53.getText():null), COL_TYPE.DECIMAL); 

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
    public static final BitSet FOLLOW_predicateObjectList_in_triples175 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_verb_in_predicateObjectList205 = new BitSet(new long[]{0x520101E043DD0000L});
    public static final BitSet FOLLOW_objectList_in_predicateObjectList209 = new BitSet(new long[]{0x0080000000000002L});
    public static final BitSet FOLLOW_SEMI_in_predicateObjectList219 = new BitSet(new long[]{0x0200010000010000L,0x0000000000000008L});
    public static final BitSet FOLLOW_verb_in_predicateObjectList223 = new BitSet(new long[]{0x520101E043DD0000L});
    public static final BitSet FOLLOW_objectList_in_predicateObjectList227 = new BitSet(new long[]{0x0080000000000002L});
    public static final BitSet FOLLOW_predicate_in_verb250 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_67_in_verb258 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_object_in_objectList286 = new BitSet(new long[]{0x0000000000020002L});
    public static final BitSet FOLLOW_COMMA_in_objectList291 = new BitSet(new long[]{0x520101E043DD0000L});
    public static final BitSet FOLLOW_object_in_objectList295 = new BitSet(new long[]{0x0000000000020002L});
    public static final BitSet FOLLOW_variable_in_subject319 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_in_subject327 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_resource_in_predicate348 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_resource_in_object369 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_in_object378 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_object386 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_object395 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dataTypeFunction_in_object403 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_uriref_in_resource425 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qname_in_resource433 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_uriref454 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_relativeURI_in_uriref456 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_GREATER_in_uriref458 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_prefix_in_qname481 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_qname483 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_qname486 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nodeID_in_blank504 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BLANK_in_blank512 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_variable529 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_variable535 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_resource_in_function554 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_LPAREN_in_function556 = new BitSet(new long[]{0x520101E043DD0000L});
    public static final BitSet FOLLOW_terms_in_function558 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_function560 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_dataTypeFunction579 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_AT_in_dataTypeFunction581 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_language_in_dataTypeFunction583 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_dataTypeFunction591 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_REFERENCE_in_dataTypeFunction593 = new BitSet(new long[]{0x0200010000010000L});
    public static final BitSet FOLLOW_resource_in_dataTypeFunction595 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_terms621 = new BitSet(new long[]{0x0000000000020002L});
    public static final BitSet FOLLOW_COMMA_in_terms626 = new BitSet(new long[]{0x520101E043DD0000L});
    public static final BitSet FOLLOW_term_in_terms630 = new BitSet(new long[]{0x0000000000020002L});
    public static final BitSet FOLLOW_function_in_term651 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_term659 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_term667 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stringLiteral_in_literal688 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_AT_in_literal691 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_language_in_literal693 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dataTypeString_in_literal703 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericLiteral_in_literal711 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanLiteral_in_literal719 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_stringLiteral738 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stringLiteral_in_dataTypeString761 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_REFERENCE_in_dataTypeString763 = new BitSet(new long[]{0x0200010000010000L});
    public static final BitSet FOLLOW_resource_in_dataTypeString765 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericUnsigned_in_numericLiteral786 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericPositive_in_numericLiteral794 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericNegative_in_numericLiteral802 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BLANK_PREFIX_in_nodeID817 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_nodeID819 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_URI_in_relativeURI832 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_PREFIX_in_prefix845 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VARNAME_in_name858 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VARNAME_in_language872 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_booleanLiteral889 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_booleanLiteral898 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_in_numericUnsigned919 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLE_in_numericUnsigned927 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_in_numericUnsigned936 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_POSITIVE_in_numericPositive957 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLE_POSITIVE_in_numericPositive965 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_POSITIVE_in_numericPositive974 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_NEGATIVE_in_numericNegative995 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLE_NEGATIVE_in_numericNegative1003 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_NEGATIVE_in_numericNegative1012 = new BitSet(new long[]{0x0000000000000002L});

}