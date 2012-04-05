// $ANTLR 3.4 C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g 2012-04-05 14:51:50

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
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
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
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

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
    public final CQIE parse() throws Exception {
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
    public final List<Atom> statement() throws Exception {
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
    public final List<Atom> triples() throws Exception {
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
    public final List<Atom> predicateObjectList() throws Exception {
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

            if ( (LA5_0==COLON||LA5_0==STRING_PREFIX) ) {
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
    public final List<Term> objectList() throws Exception {
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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:192:1: subject returns [Term value] : ( variable | function | uriTemplateFunction );
    public final Term subject() throws Exception {
        Term value = null;


        Variable variable9 =null;

        Function function10 =null;

        Function uriTemplateFunction11 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:195:3: ( variable | function | uriTemplateFunction )
            int alt7=3;
            switch ( input.LA(1) ) {
            case DOLLAR:
            case QUESTION:
                {
                alt7=1;
                }
                break;
            case COLON:
            case STRING_PREFIX:
                {
                alt7=2;
                }
                break;
            case LESS:
                {
                alt7=3;
                }
                break;
            default:
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
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:197:5: uriTemplateFunction
                    {
                    pushFollow(FOLLOW_uriTemplateFunction_in_subject335);
                    uriTemplateFunction11=uriTemplateFunction();

                    state._fsp--;


                     value = uriTemplateFunction11; 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:200:1: predicate returns [URI value] : resource ;
    public final URI predicate() throws RecognitionException {
        URI value = null;


        URI resource12 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:201:3: ( resource )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:201:5: resource
            {
            pushFollow(FOLLOW_resource_in_predicate356);
            resource12=resource();

            state._fsp--;


             value = resource12; 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:204:1: object returns [Term value] : ( resource | function | literal | variable | dataTypeFunction | uriTemplateFunction );
    public final Term object() throws Exception {
        Term value = null;


        URI resource13 =null;

        Function function14 =null;

        Term literal15 =null;

        Variable variable16 =null;

        Function dataTypeFunction17 =null;

        Function uriTemplateFunction18 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:205:3: ( resource | function | literal | variable | dataTypeFunction | uriTemplateFunction )
            int alt8=6;
            switch ( input.LA(1) ) {
            case STRING_PREFIX:
                {
                switch ( input.LA(2) ) {
                case VARNAME:
                    {
                    int LA8_6 = input.LA(3);

                    if ( (LA8_6==COMMA||LA8_6==PERIOD||LA8_6==SEMI) ) {
                        alt8=1;
                    }
                    else if ( (LA8_6==LPAREN) ) {
                        alt8=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 8, 6, input);

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
                        new NoViableAltException("", 8, 1, input);

                    throw nvae;

                }

                }
                break;
            case COLON:
                {
                switch ( input.LA(2) ) {
                case VARNAME:
                    {
                    int LA8_6 = input.LA(3);

                    if ( (LA8_6==COMMA||LA8_6==PERIOD||LA8_6==SEMI) ) {
                        alt8=1;
                    }
                    else if ( (LA8_6==LPAREN) ) {
                        alt8=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 8, 6, input);

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
                int LA8_4 = input.LA(2);

                if ( (LA8_4==VARNAME) ) {
                    int LA8_9 = input.LA(3);

                    if ( (LA8_9==COMMA||LA8_9==PERIOD||LA8_9==SEMI) ) {
                        alt8=4;
                    }
                    else if ( (LA8_9==AT||LA8_9==REFERENCE) ) {
                        alt8=5;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 8, 9, input);

                        throw nvae;

                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 4, input);

                    throw nvae;

                }
                }
                break;
            case LESS:
                {
                alt8=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;

            }

            switch (alt8) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:205:5: resource
                    {
                    pushFollow(FOLLOW_resource_in_object377);
                    resource13=resource();

                    state._fsp--;


                     value = dfac.getURIConstant(resource13); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:206:5: function
                    {
                    pushFollow(FOLLOW_function_in_object386);
                    function14=function();

                    state._fsp--;


                     value = function14; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:207:5: literal
                    {
                    pushFollow(FOLLOW_literal_in_object394);
                    literal15=literal();

                    state._fsp--;


                     value = literal15; 

                    }
                    break;
                case 4 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:208:5: variable
                    {
                    pushFollow(FOLLOW_variable_in_object403);
                    variable16=variable();

                    state._fsp--;


                     value = variable16; 

                    }
                    break;
                case 5 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:209:5: dataTypeFunction
                    {
                    pushFollow(FOLLOW_dataTypeFunction_in_object411);
                    dataTypeFunction17=dataTypeFunction();

                    state._fsp--;


                     value = dataTypeFunction17; 

                    }
                    break;
                case 6 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:210:5: uriTemplateFunction
                    {
                    pushFollow(FOLLOW_uriTemplateFunction_in_object419);
                    uriTemplateFunction18=uriTemplateFunction();

                    state._fsp--;


                     value = uriTemplateFunction18; 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:214:1: resource returns [URI value] : qname ;
    public final URI resource() throws RecognitionException {
        URI value = null;


        String qname19 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:216:3: ( qname )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:216:5: qname
            {
            pushFollow(FOLLOW_qname_in_resource442);
            qname19=qname();

            state._fsp--;


             value = URI.create(qname19); 

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


        TurtleParser.relativeURI_return relativeURI20 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:220:3: ( LESS relativeURI GREATER )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:220:5: LESS relativeURI GREATER
            {
            match(input,LESS,FOLLOW_LESS_in_uriref463); 

            pushFollow(FOLLOW_relativeURI_in_uriref465);
            relativeURI20=relativeURI();

            state._fsp--;


            match(input,GREATER,FOLLOW_GREATER_in_uriref467); 

             value = (relativeURI20!=null?input.toString(relativeURI20.start,relativeURI20.stop):null); 

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


        TurtleParser.prefix_return prefix21 =null;

        TurtleParser.name_return name22 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:224:3: ( ( prefix | COLON ) ( name )? )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:224:5: ( prefix | COLON ) ( name )?
            {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:224:5: ( prefix | COLON )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==STRING_PREFIX) ) {
                alt9=1;
            }
            else if ( (LA9_0==COLON) ) {
                alt9=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;

            }
            switch (alt9) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:224:6: prefix
                    {
                    pushFollow(FOLLOW_prefix_in_qname490);
                    prefix21=prefix();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:224:13: COLON
                    {
                    match(input,COLON,FOLLOW_COLON_in_qname492); 

                    }
                    break;

            }


            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:224:20: ( name )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==VARNAME) ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:224:20: name
                    {
                    pushFollow(FOLLOW_name_in_qname495);
                    name22=name();

                    state._fsp--;


                    }
                    break;

            }



                  String prefix = "";
                  if ((prefix21!=null?input.toString(prefix21.start,prefix21.stop):null) != null) {
                    prefix = (prefix21!=null?input.toString(prefix21.start,prefix21.stop):null).substring(0, (prefix21!=null?input.toString(prefix21.start,prefix21.stop):null).length()-1); // remove the colon!
                  }
                  String uri = directives.get(prefix);
                  value = uri + (name22!=null?input.toString(name22.start,name22.stop):null); 
                

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
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==BLANK_PREFIX) ) {
                alt11=1;
            }
            else if ( (LA11_0==BLANK) ) {
                alt11=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;

            }
            switch (alt11) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:235:5: nodeID
                    {
                    pushFollow(FOLLOW_nodeID_in_blank513);
                    nodeID();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:236:5: BLANK
                    {
                    match(input,BLANK,FOLLOW_BLANK_in_blank521); 

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


        TurtleParser.name_return name23 =null;


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


            pushFollow(FOLLOW_name_in_variable544);
            name23=name();

            state._fsp--;



                   value = dfac.getVariable((name23!=null?input.toString(name23.start,name23.stop):null));
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
    public final Function function() throws Exception {
        Function value = null;


        URI resource24 =null;

        Vector<Term> terms25 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:247:3: ( resource LPAREN terms RPAREN )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:247:5: resource LPAREN terms RPAREN
            {
            pushFollow(FOLLOW_resource_in_function563);
            resource24=resource();

            state._fsp--;


            match(input,LPAREN,FOLLOW_LPAREN_in_function565); 

            pushFollow(FOLLOW_terms_in_function567);
            terms25=terms();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_function569); 


                  String functionName = resource24.toString();
                  int arity = terms25.size();  
                  Predicate functionSymbol = dfac.getPredicate(URI.create(functionName), arity);
                  value = dfac.getFunctionalTerm(functionSymbol, terms25);
                

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:255:1: dataTypeFunction returns [Function value] : ( variable AT language | variable REFERENCE resource );
    public final Function dataTypeFunction() throws Exception {
        Function value = null;


        Variable variable26 =null;

        TurtleParser.language_return language27 =null;

        Variable variable28 =null;

        URI resource29 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:256:3: ( variable AT language | variable REFERENCE resource )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==DOLLAR||LA12_0==QUESTION) ) {
                int LA12_1 = input.LA(2);

                if ( (LA12_1==VARNAME) ) {
                    int LA12_2 = input.LA(3);

                    if ( (LA12_2==AT) ) {
                        alt12=1;
                    }
                    else if ( (LA12_2==REFERENCE) ) {
                        alt12=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 12, 2, input);

                        throw nvae;

                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 12, 1, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;

            }
            switch (alt12) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:256:5: variable AT language
                    {
                    pushFollow(FOLLOW_variable_in_dataTypeFunction588);
                    variable26=variable();

                    state._fsp--;


                    match(input,AT,FOLLOW_AT_in_dataTypeFunction590); 

                    pushFollow(FOLLOW_language_in_dataTypeFunction592);
                    language27=language();

                    state._fsp--;



                          Predicate functionSymbol = dfac.getDataTypePredicateLiteral();
                          Variable var = variable26;
                          ValueConstant lang = dfac.getValueConstant((language27!=null?input.toString(language27.start,language27.stop):null));
                          value = dfac.getFunctionalTerm(functionSymbol, var, lang);
                        

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:262:5: variable REFERENCE resource
                    {
                    pushFollow(FOLLOW_variable_in_dataTypeFunction600);
                    variable28=variable();

                    state._fsp--;


                    match(input,REFERENCE,FOLLOW_REFERENCE_in_dataTypeFunction602); 

                    pushFollow(FOLLOW_resource_in_dataTypeFunction604);
                    resource29=resource();

                    state._fsp--;



                          Variable var = variable28;
                          String functionName = resource29.toString();
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
                            throw new Exception("Unknown datatype: " + functionName);
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



    // $ANTLR start "uriTemplateFunction"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:287:1: uriTemplateFunction returns [Function value] : LESS stringLiteral GREATER ;
    public final Function uriTemplateFunction() throws Exception {
        Function value = null;


        ValueConstant stringLiteral30 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:288:3: ( LESS stringLiteral GREATER )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:288:5: LESS stringLiteral GREATER
            {
            match(input,LESS,FOLLOW_LESS_in_uriTemplateFunction623); 

            pushFollow(FOLLOW_stringLiteral_in_uriTemplateFunction625);
            stringLiteral30=stringLiteral();

            state._fsp--;


            match(input,GREATER,FOLLOW_GREATER_in_uriTemplateFunction627); 


                  String template = stringLiteral30.toString();
                  List<Term> terms = new ArrayList<Term>();
                  
                  if (template.contains("&") && template.contains(";")) {
                    // scan the input string if it contains "{" ... "}"
                    int start = template.indexOf("&");
                    int end = template.indexOf(";");
                    
                    // extract the whole prefix placeholder, e.g., "&hello;"
                    String prefixPlaceHolder = template.substring(start, end+1);
                    
                    // extract the prefix name, e.g., "&hello;" --> "hello"
                    String prefix = prefixPlaceHolder.substring(1, prefixPlaceHolder.length()-1);
                    
                    // make an exception for base prefix: replace it to a blank string
                    prefix = prefix.equals(":") ? "" : prefix;
                    
                    String uri = directives.get(prefix);        
                    if (uri == null) {
                      throw new Exception("The prefix name is unknown: " + prefix); // the prefix is unknown.
                    }
                    template = template.replaceFirst(prefixPlaceHolder, uri);
                  }
                  
                  while (template.contains("{") && template.contains("}")) {
                    // scan the input string if it contains "{" ... "}"
                    int start = template.indexOf("{");
                    int end = template.indexOf("}");
                    
                    // extract the whole placeholder, e.g., "{?var}"
                    String placeHolder = template.substring(start, end+1);
                    template = template.replace(placeHolder, "[]"); // change the placeholder string temporarly
                    
                    // extract the variable name only, e.g., "{?var}" --> "var"
                    try {
                    	String variableName = placeHolder.substring(2, placeHolder.length()-1);                    	
                    	if (variableName.equals("")) {
                    		throw new Exception("Variable name has not been properly defined!");
                    	}
                        terms.add(dfac.getVariable(variableName));
                    } catch (IndexOutOfBoundsException e) {
                    	throw new Exception("Variable name has not been properly defined!");
                    }
                  }
                  // replace the placeholder string to the original. The current string becomes the template
                  template = template.replaceAll("\\[\\]", "{}");
                  ValueConstant uriTemplate = dfac.getValueConstant(template);
                  
                  // the URI template is always on the first position in the term list
                  terms.add(0, uriTemplate);
                  value = dfac.getFunctionalTerm(dfac.getPredicate(OBDAVocabulary.QUEST_URI, terms.size(), null), terms);
                

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
    // $ANTLR end "uriTemplateFunction"



    // $ANTLR start "terms"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:335:1: terms returns [Vector<Term> value] : t1= term ( COMMA t2= term )* ;
    public final Vector<Term> terms() throws Exception {
        Vector<Term> value = null;


        Term t1 =null;

        Term t2 =null;



          value = new Vector<Term>();

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:339:3: (t1= term ( COMMA t2= term )* )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:339:5: t1= term ( COMMA t2= term )*
            {
            pushFollow(FOLLOW_term_in_terms653);
            t1=term();

            state._fsp--;


             value.add(t1); 

            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:339:40: ( COMMA t2= term )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0==COMMA) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:339:41: COMMA t2= term
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_terms658); 

            	    pushFollow(FOLLOW_term_in_terms662);
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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:342:1: term returns [Term value] : ( function | variable | literal );
    public final Term term() throws Exception {
        Term value = null;


        Function function31 =null;

        Variable variable32 =null;

        Term literal33 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:343:3: ( function | variable | literal )
            int alt14=3;
            switch ( input.LA(1) ) {
            case COLON:
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
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:343:5: function
                    {
                    pushFollow(FOLLOW_function_in_term683);
                    function31=function();

                    state._fsp--;


                     value = function31; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:344:5: variable
                    {
                    pushFollow(FOLLOW_variable_in_term691);
                    variable32=variable();

                    state._fsp--;


                     value = variable32; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:345:5: literal
                    {
                    pushFollow(FOLLOW_literal_in_term699);
                    literal33=literal();

                    state._fsp--;


                     value = literal33; 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:348:1: literal returns [Term value] : ( stringLiteral ( AT language )? | dataTypeString | numericLiteral | booleanLiteral );
    public final Term literal() throws Exception {
        Term value = null;


        ValueConstant stringLiteral34 =null;

        TurtleParser.language_return language35 =null;

        Term dataTypeString36 =null;

        ValueConstant numericLiteral37 =null;

        ValueConstant booleanLiteral38 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:349:3: ( stringLiteral ( AT language )? | dataTypeString | numericLiteral | booleanLiteral )
            int alt16=4;
            switch ( input.LA(1) ) {
            case STRING_WITH_QUOTE_DOUBLE:
                {
                int LA16_1 = input.LA(2);

                if ( (LA16_1==AT||LA16_1==COMMA||LA16_1==PERIOD||LA16_1==RPAREN||LA16_1==SEMI) ) {
                    alt16=1;
                }
                else if ( (LA16_1==REFERENCE) ) {
                    alt16=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 16, 1, input);

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
                alt16=3;
                }
                break;
            case FALSE:
            case TRUE:
                {
                alt16=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;

            }

            switch (alt16) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:349:5: stringLiteral ( AT language )?
                    {
                    pushFollow(FOLLOW_stringLiteral_in_literal720);
                    stringLiteral34=stringLiteral();

                    state._fsp--;


                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:349:19: ( AT language )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0==AT) ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:349:20: AT language
                            {
                            match(input,AT,FOLLOW_AT_in_literal723); 

                            pushFollow(FOLLOW_language_in_literal725);
                            language35=language();

                            state._fsp--;


                            }
                            break;

                    }



                           Predicate functionSymbol = dfac.getDataTypePredicateLiteral();
                           ValueConstant constant = stringLiteral34;
                           if ((language35!=null?input.toString(language35.start,language35.stop):null) != null && (language35!=null?input.toString(language35.start,language35.stop):null).trim().length() > 0) {
                             constant = dfac.getValueConstant(constant.getValue(), (language35!=null?input.toString(language35.start,language35.stop):null));
                           }
                           value = dfac.getFunctionalTerm(functionSymbol, constant);
                        

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:357:5: dataTypeString
                    {
                    pushFollow(FOLLOW_dataTypeString_in_literal735);
                    dataTypeString36=dataTypeString();

                    state._fsp--;


                     value = dataTypeString36; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:358:5: numericLiteral
                    {
                    pushFollow(FOLLOW_numericLiteral_in_literal743);
                    numericLiteral37=numericLiteral();

                    state._fsp--;


                     value = numericLiteral37; 

                    }
                    break;
                case 4 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:359:5: booleanLiteral
                    {
                    pushFollow(FOLLOW_booleanLiteral_in_literal751);
                    booleanLiteral38=booleanLiteral();

                    state._fsp--;


                     value = booleanLiteral38; 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:362:1: stringLiteral returns [ValueConstant value] : STRING_WITH_QUOTE_DOUBLE ;
    public final ValueConstant stringLiteral() throws RecognitionException {
        ValueConstant value = null;


        Token STRING_WITH_QUOTE_DOUBLE39=null;

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:363:3: ( STRING_WITH_QUOTE_DOUBLE )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:363:5: STRING_WITH_QUOTE_DOUBLE
            {
            STRING_WITH_QUOTE_DOUBLE39=(Token)match(input,STRING_WITH_QUOTE_DOUBLE,FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_stringLiteral770); 

             
                  String str = (STRING_WITH_QUOTE_DOUBLE39!=null?STRING_WITH_QUOTE_DOUBLE39.getText():null);
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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:369:1: dataTypeString returns [Term value] : stringLiteral REFERENCE resource ;
    public final Term dataTypeString() throws Exception {
        Term value = null;


        ValueConstant stringLiteral40 =null;

        URI resource41 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:370:3: ( stringLiteral REFERENCE resource )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:370:6: stringLiteral REFERENCE resource
            {
            pushFollow(FOLLOW_stringLiteral_in_dataTypeString793);
            stringLiteral40=stringLiteral();

            state._fsp--;


            match(input,REFERENCE,FOLLOW_REFERENCE_in_dataTypeString795); 

            pushFollow(FOLLOW_resource_in_dataTypeString797);
            resource41=resource();

            state._fsp--;



                  ValueConstant constant = stringLiteral40;
                  String functionName = resource41.toString();
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
                    throw new Exception("Unknown datatype: " + functionName);
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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:395:1: numericLiteral returns [ValueConstant value] : ( numericUnsigned | numericPositive | numericNegative );
    public final ValueConstant numericLiteral() throws RecognitionException {
        ValueConstant value = null;


        ValueConstant numericUnsigned42 =null;

        ValueConstant numericPositive43 =null;

        ValueConstant numericNegative44 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:396:3: ( numericUnsigned | numericPositive | numericNegative )
            int alt17=3;
            switch ( input.LA(1) ) {
            case DECIMAL:
            case DOUBLE:
            case INTEGER:
                {
                alt17=1;
                }
                break;
            case DECIMAL_POSITIVE:
            case DOUBLE_POSITIVE:
            case INTEGER_POSITIVE:
                {
                alt17=2;
                }
                break;
            case DECIMAL_NEGATIVE:
            case DOUBLE_NEGATIVE:
            case INTEGER_NEGATIVE:
                {
                alt17=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;

            }

            switch (alt17) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:396:5: numericUnsigned
                    {
                    pushFollow(FOLLOW_numericUnsigned_in_numericLiteral818);
                    numericUnsigned42=numericUnsigned();

                    state._fsp--;


                     value = numericUnsigned42; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:397:5: numericPositive
                    {
                    pushFollow(FOLLOW_numericPositive_in_numericLiteral826);
                    numericPositive43=numericPositive();

                    state._fsp--;


                     value = numericPositive43; 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:398:5: numericNegative
                    {
                    pushFollow(FOLLOW_numericNegative_in_numericLiteral834);
                    numericNegative44=numericNegative();

                    state._fsp--;


                     value = numericNegative44; 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:401:1: nodeID : BLANK_PREFIX name ;
    public final void nodeID() throws RecognitionException {
        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:402:3: ( BLANK_PREFIX name )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:402:5: BLANK_PREFIX name
            {
            match(input,BLANK_PREFIX,FOLLOW_BLANK_PREFIX_in_nodeID849); 

            pushFollow(FOLLOW_name_in_nodeID851);
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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:405:1: relativeURI : STRING_URI ;
    public final TurtleParser.relativeURI_return relativeURI() throws RecognitionException {
        TurtleParser.relativeURI_return retval = new TurtleParser.relativeURI_return();
        retval.start = input.LT(1);


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:406:3: ( STRING_URI )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:406:5: STRING_URI
            {
            match(input,STRING_URI,FOLLOW_STRING_URI_in_relativeURI864); 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:409:1: prefix : STRING_PREFIX ;
    public final TurtleParser.prefix_return prefix() throws RecognitionException {
        TurtleParser.prefix_return retval = new TurtleParser.prefix_return();
        retval.start = input.LT(1);


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:410:3: ( STRING_PREFIX )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:410:5: STRING_PREFIX
            {
            match(input,STRING_PREFIX,FOLLOW_STRING_PREFIX_in_prefix877); 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:413:1: name : VARNAME ;
    public final TurtleParser.name_return name() throws RecognitionException {
        TurtleParser.name_return retval = new TurtleParser.name_return();
        retval.start = input.LT(1);


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:414:3: ( VARNAME )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:414:5: VARNAME
            {
            match(input,VARNAME,FOLLOW_VARNAME_in_name890); 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:417:1: language : VARNAME ;
    public final TurtleParser.language_return language() throws RecognitionException {
        TurtleParser.language_return retval = new TurtleParser.language_return();
        retval.start = input.LT(1);


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:418:3: ( VARNAME )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:418:5: VARNAME
            {
            match(input,VARNAME,FOLLOW_VARNAME_in_language904); 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:421:1: booleanLiteral returns [ValueConstant value] : ( TRUE | FALSE );
    public final ValueConstant booleanLiteral() throws RecognitionException {
        ValueConstant value = null;


        Token TRUE45=null;
        Token FALSE46=null;

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:422:3: ( TRUE | FALSE )
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==TRUE) ) {
                alt18=1;
            }
            else if ( (LA18_0==FALSE) ) {
                alt18=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;

            }
            switch (alt18) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:422:5: TRUE
                    {
                    TRUE45=(Token)match(input,TRUE,FOLLOW_TRUE_in_booleanLiteral921); 

                     value = dfac.getValueConstant((TRUE45!=null?TRUE45.getText():null), COL_TYPE.BOOLEAN); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:423:5: FALSE
                    {
                    FALSE46=(Token)match(input,FALSE,FOLLOW_FALSE_in_booleanLiteral930); 

                     value = dfac.getValueConstant((FALSE46!=null?FALSE46.getText():null), COL_TYPE.BOOLEAN); 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:426:1: numericUnsigned returns [ValueConstant value] : ( INTEGER | DOUBLE | DECIMAL );
    public final ValueConstant numericUnsigned() throws RecognitionException {
        ValueConstant value = null;


        Token INTEGER47=null;
        Token DOUBLE48=null;
        Token DECIMAL49=null;

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:427:3: ( INTEGER | DOUBLE | DECIMAL )
            int alt19=3;
            switch ( input.LA(1) ) {
            case INTEGER:
                {
                alt19=1;
                }
                break;
            case DOUBLE:
                {
                alt19=2;
                }
                break;
            case DECIMAL:
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
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:427:5: INTEGER
                    {
                    INTEGER47=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_numericUnsigned951); 

                     value = dfac.getValueConstant((INTEGER47!=null?INTEGER47.getText():null), COL_TYPE.INTEGER); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:428:5: DOUBLE
                    {
                    DOUBLE48=(Token)match(input,DOUBLE,FOLLOW_DOUBLE_in_numericUnsigned959); 

                     value = dfac.getValueConstant((DOUBLE48!=null?DOUBLE48.getText():null), COL_TYPE.DOUBLE); 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:429:5: DECIMAL
                    {
                    DECIMAL49=(Token)match(input,DECIMAL,FOLLOW_DECIMAL_in_numericUnsigned968); 

                     value = dfac.getValueConstant((DECIMAL49!=null?DECIMAL49.getText():null), COL_TYPE.DECIMAL); 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:432:1: numericPositive returns [ValueConstant value] : ( INTEGER_POSITIVE | DOUBLE_POSITIVE | DECIMAL_POSITIVE );
    public final ValueConstant numericPositive() throws RecognitionException {
        ValueConstant value = null;


        Token INTEGER_POSITIVE50=null;
        Token DOUBLE_POSITIVE51=null;
        Token DECIMAL_POSITIVE52=null;

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:433:3: ( INTEGER_POSITIVE | DOUBLE_POSITIVE | DECIMAL_POSITIVE )
            int alt20=3;
            switch ( input.LA(1) ) {
            case INTEGER_POSITIVE:
                {
                alt20=1;
                }
                break;
            case DOUBLE_POSITIVE:
                {
                alt20=2;
                }
                break;
            case DECIMAL_POSITIVE:
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
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:433:5: INTEGER_POSITIVE
                    {
                    INTEGER_POSITIVE50=(Token)match(input,INTEGER_POSITIVE,FOLLOW_INTEGER_POSITIVE_in_numericPositive989); 

                     value = dfac.getValueConstant((INTEGER_POSITIVE50!=null?INTEGER_POSITIVE50.getText():null), COL_TYPE.INTEGER); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:434:5: DOUBLE_POSITIVE
                    {
                    DOUBLE_POSITIVE51=(Token)match(input,DOUBLE_POSITIVE,FOLLOW_DOUBLE_POSITIVE_in_numericPositive997); 

                     value = dfac.getValueConstant((DOUBLE_POSITIVE51!=null?DOUBLE_POSITIVE51.getText():null), COL_TYPE.DOUBLE); 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:435:5: DECIMAL_POSITIVE
                    {
                    DECIMAL_POSITIVE52=(Token)match(input,DECIMAL_POSITIVE,FOLLOW_DECIMAL_POSITIVE_in_numericPositive1006); 

                     value = dfac.getValueConstant((DECIMAL_POSITIVE52!=null?DECIMAL_POSITIVE52.getText():null), COL_TYPE.DECIMAL); 

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
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:438:1: numericNegative returns [ValueConstant value] : ( INTEGER_NEGATIVE | DOUBLE_NEGATIVE | DECIMAL_NEGATIVE );
    public final ValueConstant numericNegative() throws RecognitionException {
        ValueConstant value = null;


        Token INTEGER_NEGATIVE53=null;
        Token DOUBLE_NEGATIVE54=null;
        Token DECIMAL_NEGATIVE55=null;

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:439:3: ( INTEGER_NEGATIVE | DOUBLE_NEGATIVE | DECIMAL_NEGATIVE )
            int alt21=3;
            switch ( input.LA(1) ) {
            case INTEGER_NEGATIVE:
                {
                alt21=1;
                }
                break;
            case DOUBLE_NEGATIVE:
                {
                alt21=2;
                }
                break;
            case DECIMAL_NEGATIVE:
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
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:439:5: INTEGER_NEGATIVE
                    {
                    INTEGER_NEGATIVE53=(Token)match(input,INTEGER_NEGATIVE,FOLLOW_INTEGER_NEGATIVE_in_numericNegative1027); 

                     value = dfac.getValueConstant((INTEGER_NEGATIVE53!=null?INTEGER_NEGATIVE53.getText():null), COL_TYPE.INTEGER); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:440:5: DOUBLE_NEGATIVE
                    {
                    DOUBLE_NEGATIVE54=(Token)match(input,DOUBLE_NEGATIVE,FOLLOW_DOUBLE_NEGATIVE_in_numericNegative1035); 

                     value = dfac.getValueConstant((DOUBLE_NEGATIVE54!=null?DOUBLE_NEGATIVE54.getText():null), COL_TYPE.DOUBLE); 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\parser\\Turtle.g:441:5: DECIMAL_NEGATIVE
                    {
                    DECIMAL_NEGATIVE55=(Token)match(input,DECIMAL_NEGATIVE,FOLLOW_DECIMAL_NEGATIVE_in_numericNegative1044); 

                     value = dfac.getValueConstant((DECIMAL_NEGATIVE55!=null?DECIMAL_NEGATIVE55.getText():null), COL_TYPE.DECIMAL); 

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
    public static final BitSet FOLLOW_subject_in_triples171 = new BitSet(new long[]{0x0200000000010000L,0x0000000000000008L});
    public static final BitSet FOLLOW_predicateObjectList_in_triples175 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_verb_in_predicateObjectList205 = new BitSet(new long[]{0x520101E043DD0000L});
    public static final BitSet FOLLOW_objectList_in_predicateObjectList209 = new BitSet(new long[]{0x0080000000000002L});
    public static final BitSet FOLLOW_SEMI_in_predicateObjectList219 = new BitSet(new long[]{0x0200000000010000L,0x0000000000000008L});
    public static final BitSet FOLLOW_verb_in_predicateObjectList223 = new BitSet(new long[]{0x520101E043DD0000L});
    public static final BitSet FOLLOW_objectList_in_predicateObjectList227 = new BitSet(new long[]{0x0080000000000002L});
    public static final BitSet FOLLOW_predicate_in_verb250 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_67_in_verb258 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_object_in_objectList286 = new BitSet(new long[]{0x0000000000020002L});
    public static final BitSet FOLLOW_COMMA_in_objectList291 = new BitSet(new long[]{0x520101E043DD0000L});
    public static final BitSet FOLLOW_object_in_objectList295 = new BitSet(new long[]{0x0000000000020002L});
    public static final BitSet FOLLOW_variable_in_subject319 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_in_subject327 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_uriTemplateFunction_in_subject335 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_resource_in_predicate356 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_resource_in_object377 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_in_object386 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_object394 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_object403 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dataTypeFunction_in_object411 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_uriTemplateFunction_in_object419 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qname_in_resource442 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_uriref463 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_relativeURI_in_uriref465 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_GREATER_in_uriref467 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_prefix_in_qname490 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_qname492 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_qname495 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nodeID_in_blank513 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BLANK_in_blank521 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_variable538 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_variable544 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_resource_in_function563 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_LPAREN_in_function565 = new BitSet(new long[]{0x520100E043DD0000L});
    public static final BitSet FOLLOW_terms_in_function567 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_function569 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_dataTypeFunction588 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_AT_in_dataTypeFunction590 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_language_in_dataTypeFunction592 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_dataTypeFunction600 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_REFERENCE_in_dataTypeFunction602 = new BitSet(new long[]{0x0200000000010000L});
    public static final BitSet FOLLOW_resource_in_dataTypeFunction604 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_uriTemplateFunction623 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_stringLiteral_in_uriTemplateFunction625 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_GREATER_in_uriTemplateFunction627 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_terms653 = new BitSet(new long[]{0x0000000000020002L});
    public static final BitSet FOLLOW_COMMA_in_terms658 = new BitSet(new long[]{0x520100E043DD0000L});
    public static final BitSet FOLLOW_term_in_terms662 = new BitSet(new long[]{0x0000000000020002L});
    public static final BitSet FOLLOW_function_in_term683 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_term691 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_term699 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stringLiteral_in_literal720 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_AT_in_literal723 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_language_in_literal725 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dataTypeString_in_literal735 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericLiteral_in_literal743 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanLiteral_in_literal751 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_stringLiteral770 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stringLiteral_in_dataTypeString793 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_REFERENCE_in_dataTypeString795 = new BitSet(new long[]{0x0200000000010000L});
    public static final BitSet FOLLOW_resource_in_dataTypeString797 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericUnsigned_in_numericLiteral818 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericPositive_in_numericLiteral826 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericNegative_in_numericLiteral834 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BLANK_PREFIX_in_nodeID849 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_nodeID851 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_URI_in_relativeURI864 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_PREFIX_in_prefix877 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VARNAME_in_name890 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VARNAME_in_language904 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_booleanLiteral921 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_booleanLiteral930 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_in_numericUnsigned951 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLE_in_numericUnsigned959 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_in_numericUnsigned968 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_POSITIVE_in_numericPositive989 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLE_POSITIVE_in_numericPositive997 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_POSITIVE_in_numericPositive1006 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_NEGATIVE_in_numericNegative1027 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLE_NEGATIVE_in_numericNegative1035 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DECIMAL_NEGATIVE_in_numericNegative1044 = new BitSet(new long[]{0x0000000000000002L});

}