// $ANTLR 3.5.2 TurtleOBDA.g 2016-04-20 15:22:59

package it.unibz.krdb.obda.parser;

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.utils.QueryUtils;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("all")
public class TurtleOBDAParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "ALPHA", "ALPHANUM", "AMPERSAND", 
		"APOSTROPHE", "ASTERISK", "AT", "BACKSLASH", "BASE", "BLANK", "BLANK_PREFIX", 
		"CARET", "CHAR", "COLON", "COMMA", "DECIMAL", "DECIMAL_NEGATIVE", "DECIMAL_POSITIVE", 
		"DIGIT", "DOLLAR", "DOUBLE", "DOUBLE_NEGATIVE", "DOUBLE_POSITIVE", "DOUBLE_SLASH", 
		"ECHAR", "EQUALS", "EXCLAMATION", "FALSE", "GREATER", "HASH", "ID", "ID_CORE", 
		"ID_START", "INTEGER", "INTEGER_NEGATIVE", "INTEGER_POSITIVE", "LCR_BRACKET", 
		"LESS", "LPAREN", "LSQ_BRACKET", "LTSIGN", "MINUS", "NAMESPACE", "NAME_CHAR", 
		"NAME_START_CHAR", "NCNAME", "NCNAME_EXT", "PERCENT", "PERIOD", "PLUS", 
		"PREFIX", "PREFIXED_NAME", "QUESTION", "QUOTE_DOUBLE", "QUOTE_SINGLE", 
		"RCR_BRACKET", "REFERENCE", "RPAREN", "RSQ_BRACKET", "RTSIGN", "SCHEMA", 
		"SEMI", "SLASH", "STRING_URI", "STRING_WITH_BRACKET", "STRING_WITH_CURLY_BRACKET", 
		"STRING_WITH_QUOTE", "STRING_WITH_QUOTE_DOUBLE", "TILDE", "TRUE", "UNDERSCORE", 
		"URI_PATH", "VARNAME", "WS", "'a'"
	};
	public static final int EOF=-1;
	public static final int T__77=77;
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
	public static final int ID_START=35;
	public static final int INTEGER=36;
	public static final int INTEGER_NEGATIVE=37;
	public static final int INTEGER_POSITIVE=38;
	public static final int LCR_BRACKET=39;
	public static final int LESS=40;
	public static final int LPAREN=41;
	public static final int LSQ_BRACKET=42;
	public static final int LTSIGN=43;
	public static final int MINUS=44;
	public static final int NAMESPACE=45;
	public static final int NAME_CHAR=46;
	public static final int NAME_START_CHAR=47;
	public static final int NCNAME=48;
	public static final int NCNAME_EXT=49;
	public static final int PERCENT=50;
	public static final int PERIOD=51;
	public static final int PLUS=52;
	public static final int PREFIX=53;
	public static final int PREFIXED_NAME=54;
	public static final int QUESTION=55;
	public static final int QUOTE_DOUBLE=56;
	public static final int QUOTE_SINGLE=57;
	public static final int RCR_BRACKET=58;
	public static final int REFERENCE=59;
	public static final int RPAREN=60;
	public static final int RSQ_BRACKET=61;
	public static final int RTSIGN=62;
	public static final int SCHEMA=63;
	public static final int SEMI=64;
	public static final int SLASH=65;
	public static final int STRING_URI=66;
	public static final int STRING_WITH_BRACKET=67;
	public static final int STRING_WITH_CURLY_BRACKET=68;
	public static final int STRING_WITH_QUOTE=69;
	public static final int STRING_WITH_QUOTE_DOUBLE=70;
	public static final int TILDE=71;
	public static final int TRUE=72;
	public static final int UNDERSCORE=73;
	public static final int URI_PATH=74;
	public static final int VARNAME=75;
	public static final int WS=76;

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public TurtleOBDAParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public TurtleOBDAParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	@Override public String[] getTokenNames() { return TurtleOBDAParser.tokenNames; }
	@Override public String getGrammarFileName() { return "TurtleOBDA.g"; }


	/** Map of directives */
	private HashMap<String, String> directives = new HashMap<String, String>();

	/** The current subject term */
	private Term currentSubject;

	/** All variables */
	private Set<Term> variableSet = new HashSet<Term>();

	/** A factory to construct the predicates and terms */
	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
	private static final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();

	private String error = "";

	public String getError() {
	   return error;
	}

	protected void mismatch(IntStream input, int ttype, BitSet follow) throws RecognitionException {
	   throw new MismatchedTokenException(ttype, input);
	}

	public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException {
	   throw e;
	}

	@Override
	public void recover(IntStream input, RecognitionException re) {
	   throw new RuntimeException(error);
	}

	@Override
	public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	   String hdr = getErrorHeader(e);
	   String msg = getErrorMessage(e, tokenNames);
	   emitErrorMessage("Syntax error: " + msg + " Location: " + hdr);
	}

	@Override
	public void emitErrorMessage(String msg) {
	   error = msg;
	}
	    
	public Object recoverFromMismatchedTokenrecoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException {
	   throw new RecognitionException(input);
	}

	private String removeBrackets(String text) {
	   return text.substring(1, text.length()-1);
	}

		private Term construct(String text) {
		   Term toReturn = null;
		   final String PLACEHOLDER = "{}";
		   List<Term> terms = new LinkedList<Term>();
		   List<FormatString> tokens = parse(text);
		   int size = tokens.size();
		   if (size == 1) {
		      FormatString token = tokens.get(0);
		      if (token instanceof FixedString) {
		          ValueConstant uriTemplate = dfac.getConstantLiteral(token.toString()); // a single URI template
		          toReturn = dfac.getUriTemplate(uriTemplate);
		      }
		      else if (token instanceof ColumnString) {
		         // a single URI template
		         Variable column = dfac.getVariable(token.toString());
		         toReturn = dfac.getUriTemplate(column);
		      }
		   }
		   else {
		      StringBuilder sb = new StringBuilder();
		      for(FormatString token : tokens) {
		         if (token instanceof FixedString) { // if part of URI template
		            sb.append(token.toString());
		         }
		         else if (token instanceof ColumnString) {
		            sb.append(PLACEHOLDER);
		            Variable column = dfac.getVariable(token.toString());
		            terms.add(column);
		         }
		      }
		      ValueConstant uriTemplate = dfac.getConstantLiteral(sb.toString()); // complete URI template
		      terms.add(0, uriTemplate);
		      toReturn = dfac.getUriTemplate(terms);
		   }
		   return toReturn;
		}
		
	// Column placeholder pattern
	private static final String formatSpecifier = "\\{([^\\}]+)?\\}";
	private static Pattern chPattern = Pattern.compile(formatSpecifier);

	private List<FormatString> parse(String text) {
	   List<FormatString> toReturn = new ArrayList<FormatString>();
	   Matcher m = chPattern.matcher(text);
	   int i = 0;
	   while (i < text.length()) {
	      if (m.find(i)) {
	         if (m.start() != i) {
	            toReturn.add(new FixedString(text.substring(i, m.start())));
	         }
	         String value = m.group(1);
	         toReturn.add(new ColumnString(value));
	         i = m.end();
	      }
	      else {
	         toReturn.add(new FixedString(text.substring(i)));
	         break;
	      }
	   }
	   return toReturn;
	}

	private interface FormatString {
	   int index();
	   String toString();
	}

	private class FixedString implements FormatString {
	   private String s;
	   FixedString(String s) { this.s = s; }
	   @Override public int index() { return -1; }  // flag code for fixed string
	   @Override public String toString() { return s; }
	}

	private class ColumnString implements FormatString {
	   private String s;
	   ColumnString(String s) { this.s = s; }
	   @Override public int index() { return 0; }  // flag code for column string
	   @Override public String toString() { return s; }
	}

		//this function distinguishes curly bracket with 
		//back slash "\{" from curly bracket "{" 
		private int getIndexOfCurlyB(String str){
		   int i;
		   int j;
		   i = str.indexOf("{");
		   j = str.indexOf("\\{");
		      while((i-1 == j) &&(j != -1)){		
			i = str.indexOf("{",i+1);
			j = str.indexOf("\\{",j+1);		
		      }	
		  return i;
		}
		
		//in case of concat this function parses the literal 
		//and adds parsed constant literals and template literal to terms list
		private ArrayList<Term> addToTermsList(String str){
		   ArrayList<Term> terms = new ArrayList<Term>();
		   int i,j;
		   String st;
		   str = str.substring(1, str.length()-1);
		   while(str.contains("{")){
		      i = getIndexOfCurlyB(str);
		      if (i > 0){
		    	 st = str.substring(0,i);
		    	 st = st.replace("\\\\", "");
		         terms.add(dfac.getConstantLiteral(st));
		         str = str.substring(str.indexOf("{", i), str.length());
		      }else if (i == 0){
		         j = str.indexOf("}");
		         terms.add(dfac.getVariable(str.substring(1,j)));
		         str = str.substring(j+1,str.length());
		      } else {
		    	  break;
		      }
		   }
		   if(!str.equals("")){
		      str = str.replace("\\\\", "");
		      terms.add(dfac.getConstantLiteral(str));
		   }
		   return terms;
		}
		
		//this function returns nested concats 
		//in case of more than two terms need to be concatted
		private Term getNestedConcat(String str){
		   ArrayList<Term> terms = new ArrayList<Term>();
		   terms = addToTermsList(str);
		   if(terms.size() == 1){
		      Variable v = (Variable) terms.get(0);
	          variableSet.add(v);
	          return v;
		   }

		   Function f = dfac.getFunctionConcat(terms.get(0),terms.get(1));
	           for(int j=2;j<terms.size();j++){
	              f = dfac.getFunctionConcat(f,terms.get(j));
	           }

		   return f;
		}

	/**
	 * This methods construct an atom from a triple 
	 * 
	 * For the input (subject, pred, object), the result is 
	 * <ul>
	 *  <li> object(subject), if pred == rdf:type and subject is grounded ; </li>
	 *  <li> predicate(subject, object), if pred != rdf:type and predicate is grounded ; </li>
	 *  <li> triple(subject, pred, object), otherwise (it is a higher order atom). </li>
	 * </ul>
	 */
		private Function makeAtom(Term subject, Term pred, Term object) {
		     Function atom = null;

		        if (isRDFType(pred)) {
			             if (object instanceof  Function) {
			                  if(QueryUtils.isGrounded(object)) {
			                      ValueConstant c = ((ValueConstant) ((Function) object).getTerm(0));  // it has to be a URI constant
			                      Predicate predicate = dfac.getClassPredicate(c.getValue());
			                      atom = dfac.getFunction(predicate, subject);
			                  } else {
			                       atom = dfac.getTripleAtom(subject, pred, object);
			                  }
			             }
			             else if (object instanceof  Variable){
			                  Term uriOfPred = dfac.getUriTemplate(pred);
			                  Term uriOfObject = dfac.getUriTemplate(object);
			                  atom = dfac.getTripleAtom(subject, uriOfPred,  uriOfObject);
			              }
			             else {
			                  throw new IllegalArgumentException("parser cannot handle object " + object);
			              }
			        } else if( ! QueryUtils.isGrounded(pred) ){
			             atom = dfac.getTripleAtom(subject, pred,  object);
			        } else {
	                			             //Predicate predicate = dfac.getPredicate(pred.toString(), 2); // the data type cannot be determined here!
	                			             Predicate predicate;
	                			             if(pred instanceof Function) {
	                							 ValueConstant pr = (ValueConstant) ((Function) pred).getTerm(0);
	                							 if (object instanceof Variable) {
	                								 predicate = dfac.getPredicate(pr.getValue(), 2);
	                							 } else {
	                								 if (object instanceof Function) {
	                									 if (((Function) object).getFunctionSymbol() instanceof URITemplatePredicate) {

	                										 predicate = dfac.getObjectPropertyPredicate(pr.getValue());
	                									 } else {
	                										 predicate = dfac.getDataPropertyPredicate(pr.getValue());
	                									 }
	                								 }
	                									 else {
	                										 throw new IllegalArgumentException("parser cannot handle object " + object);
	                									 }
	                							 }
	                						 }else {
	                			                  throw new IllegalArgumentException("predicate should be a URI Function");
	                			             }
	                			             atom = dfac.getFunction(predicate, subject, object);
	                			       }
	                			       return atom;
		  }


	private static boolean isRDFType(Term pred) {
	//		if (pred instanceof Constant && ((Constant) pred).getValue().equals(OBDAVocabulary.RDF_TYPE)) {
	//			return true;
	//		}
			if (pred instanceof Function && ((Function) pred).getTerm(0) instanceof Constant ) {
				String c= ((Constant) ((Function) pred).getTerm(0)).getValue();
				return c.equals(OBDAVocabulary.RDF_TYPE);
			}	
			return false;
		}




	// $ANTLR start "parse"
	// TurtleOBDA.g:386:1: parse returns [List<Function> value] : ( directiveStatement )* t1= triplesStatement (t2= triplesStatement )* EOF ;
	public final List<Function> parse() throws RecognitionException {
		List<Function> value = null;


		List<Function> t1 =null;
		List<Function> t2 =null;

		try {
			// TurtleOBDA.g:387:3: ( ( directiveStatement )* t1= triplesStatement (t2= triplesStatement )* EOF )
			// TurtleOBDA.g:387:5: ( directiveStatement )* t1= triplesStatement (t2= triplesStatement )* EOF
			{
			// TurtleOBDA.g:387:5: ( directiveStatement )*
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==AT) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// TurtleOBDA.g:387:5: directiveStatement
					{
					pushFollow(FOLLOW_directiveStatement_in_parse54);
					directiveStatement();
					state._fsp--;

					}
					break;

				default :
					break loop1;
				}
			}

			pushFollow(FOLLOW_triplesStatement_in_parse63);
			t1=triplesStatement();
			state._fsp--;


			      value =  t1;
			    
			// TurtleOBDA.g:391:7: (t2= triplesStatement )*
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( (LA2_0==PREFIXED_NAME||(LA2_0 >= STRING_WITH_BRACKET && LA2_0 <= STRING_WITH_CURLY_BRACKET)) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// TurtleOBDA.g:391:8: t2= triplesStatement
					{
					pushFollow(FOLLOW_triplesStatement_in_parse76);
					t2=triplesStatement();
					state._fsp--;


					      List<Function> additionalTriples = t2;
					      if (additionalTriples != null) {
					        // If there are additional triple statements then just add to the existing body
					        List<Function> existingBody = value;
					        existingBody.addAll(additionalTriples);
					      }
					    
					}
					break;

				default :
					break loop2;
				}
			}

			match(input,EOF,FOLLOW_EOF_in_parse83); 
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



	// $ANTLR start "directiveStatement"
	// TurtleOBDA.g:401:1: directiveStatement : directive PERIOD ;
	public final void directiveStatement() throws RecognitionException {
		try {
			// TurtleOBDA.g:402:3: ( directive PERIOD )
			// TurtleOBDA.g:402:5: directive PERIOD
			{
			pushFollow(FOLLOW_directive_in_directiveStatement96);
			directive();
			state._fsp--;

			match(input,PERIOD,FOLLOW_PERIOD_in_directiveStatement98); 
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
	// $ANTLR end "directiveStatement"



	// $ANTLR start "triplesStatement"
	// TurtleOBDA.g:405:1: triplesStatement returns [List<Function> value] : triples ( WS )* PERIOD ;
	public final List<Function> triplesStatement() throws RecognitionException {
		List<Function> value = null;


		List<Function> triples1 =null;

		try {
			// TurtleOBDA.g:406:3: ( triples ( WS )* PERIOD )
			// TurtleOBDA.g:406:5: triples ( WS )* PERIOD
			{
			pushFollow(FOLLOW_triples_in_triplesStatement115);
			triples1=triples();
			state._fsp--;

			// TurtleOBDA.g:406:13: ( WS )*
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( (LA3_0==WS) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// TurtleOBDA.g:406:13: WS
					{
					match(input,WS,FOLLOW_WS_in_triplesStatement117); 
					}
					break;

				default :
					break loop3;
				}
			}

			match(input,PERIOD,FOLLOW_PERIOD_in_triplesStatement120); 
			 value = triples1; 
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
	// $ANTLR end "triplesStatement"



	// $ANTLR start "directive"
	// TurtleOBDA.g:409:1: directive : ( base | prefixID );
	public final void directive() throws RecognitionException {
		try {
			// TurtleOBDA.g:410:3: ( base | prefixID )
			int alt4=2;
			int LA4_0 = input.LA(1);
			if ( (LA4_0==AT) ) {
				int LA4_1 = input.LA(2);
				if ( (LA4_1==BASE) ) {
					alt4=1;
				}
				else if ( (LA4_1==PREFIX) ) {
					alt4=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 4, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 4, 0, input);
				throw nvae;
			}

			switch (alt4) {
				case 1 :
					// TurtleOBDA.g:410:5: base
					{
					pushFollow(FOLLOW_base_in_directive135);
					base();
					state._fsp--;

					}
					break;
				case 2 :
					// TurtleOBDA.g:411:5: prefixID
					{
					pushFollow(FOLLOW_prefixID_in_directive141);
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
	}
	// $ANTLR end "directive"



	// $ANTLR start "base"
	// TurtleOBDA.g:414:1: base : AT BASE uriref ;
	public final void base() throws RecognitionException {
		try {
			// TurtleOBDA.g:415:3: ( AT BASE uriref )
			// TurtleOBDA.g:415:5: AT BASE uriref
			{
			match(input,AT,FOLLOW_AT_in_base154); 
			match(input,BASE,FOLLOW_BASE_in_base156); 
			pushFollow(FOLLOW_uriref_in_base158);
			uriref();
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
	// $ANTLR end "base"



	// $ANTLR start "prefixID"
	// TurtleOBDA.g:418:1: prefixID : AT PREFIX ( namespace | defaultNamespace ) uriref ;
	public final void prefixID() throws RecognitionException {
		ParserRuleReturnScope namespace2 =null;
		ParserRuleReturnScope defaultNamespace3 =null;
		String uriref4 =null;


		  String prefix = "";

		try {
			// TurtleOBDA.g:422:3: ( AT PREFIX ( namespace | defaultNamespace ) uriref )
			// TurtleOBDA.g:422:5: AT PREFIX ( namespace | defaultNamespace ) uriref
			{
			match(input,AT,FOLLOW_AT_in_prefixID176); 
			match(input,PREFIX,FOLLOW_PREFIX_in_prefixID178); 
			// TurtleOBDA.g:422:15: ( namespace | defaultNamespace )
			int alt5=2;
			int LA5_0 = input.LA(1);
			if ( (LA5_0==NAMESPACE) ) {
				alt5=1;
			}
			else if ( (LA5_0==COLON) ) {
				alt5=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 5, 0, input);
				throw nvae;
			}

			switch (alt5) {
				case 1 :
					// TurtleOBDA.g:422:16: namespace
					{
					pushFollow(FOLLOW_namespace_in_prefixID181);
					namespace2=namespace();
					state._fsp--;

					 prefix = (namespace2!=null?input.toString(namespace2.start,namespace2.stop):null); 
					}
					break;
				case 2 :
					// TurtleOBDA.g:422:58: defaultNamespace
					{
					pushFollow(FOLLOW_defaultNamespace_in_prefixID187);
					defaultNamespace3=defaultNamespace();
					state._fsp--;

					 prefix = (defaultNamespace3!=null?input.toString(defaultNamespace3.start,defaultNamespace3.stop):null); 
					}
					break;

			}

			pushFollow(FOLLOW_uriref_in_prefixID192);
			uriref4=uriref();
			state._fsp--;


			      String uriref = uriref4;
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
	}
	// $ANTLR end "prefixID"



	// $ANTLR start "triples"
	// TurtleOBDA.g:428:1: triples returns [List<Function> value] : subject predicateObjectList ;
	public final List<Function> triples() throws RecognitionException {
		List<Function> value = null;


		Term subject5 =null;
		List<Function> predicateObjectList6 =null;

		try {
			// TurtleOBDA.g:429:3: ( subject predicateObjectList )
			// TurtleOBDA.g:429:5: subject predicateObjectList
			{
			pushFollow(FOLLOW_subject_in_triples211);
			subject5=subject();
			state._fsp--;

			 currentSubject = subject5; 
			pushFollow(FOLLOW_predicateObjectList_in_triples215);
			predicateObjectList6=predicateObjectList();
			state._fsp--;


			      value = predicateObjectList6;
			    
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
	// TurtleOBDA.g:434:1: predicateObjectList returns [List<Function> value] : v1= verb l1= objectList ( SEMI v2= verb l2= objectList )* ;
	public final List<Function> predicateObjectList() throws RecognitionException {
		List<Function> value = null;


		Term v1 =null;
		List<Term> l1 =null;
		Term v2 =null;
		List<Term> l2 =null;


		   value = new LinkedList<Function>();

		try {
			// TurtleOBDA.g:438:3: (v1= verb l1= objectList ( SEMI v2= verb l2= objectList )* )
			// TurtleOBDA.g:438:5: v1= verb l1= objectList ( SEMI v2= verb l2= objectList )*
			{
			pushFollow(FOLLOW_verb_in_predicateObjectList241);
			v1=verb();
			state._fsp--;

			pushFollow(FOLLOW_objectList_in_predicateObjectList247);
			l1=objectList();
			state._fsp--;


			      for (Term object : l1) {
			        Function atom = makeAtom(currentSubject, v1, object);
			        value.add(atom);
			      }
			    
			// TurtleOBDA.g:444:5: ( SEMI v2= verb l2= objectList )*
			loop6:
			while (true) {
				int alt6=2;
				int LA6_0 = input.LA(1);
				if ( (LA6_0==SEMI) ) {
					alt6=1;
				}

				switch (alt6) {
				case 1 :
					// TurtleOBDA.g:444:6: SEMI v2= verb l2= objectList
					{
					match(input,SEMI,FOLLOW_SEMI_in_predicateObjectList256); 
					pushFollow(FOLLOW_verb_in_predicateObjectList260);
					v2=verb();
					state._fsp--;

					pushFollow(FOLLOW_objectList_in_predicateObjectList264);
					l2=objectList();
					state._fsp--;


					      for (Term object : l2) {
					        Function atom = makeAtom(currentSubject, v2, object);
					        value.add(atom);
					      }
					    
					}
					break;

				default :
					break loop6;
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
	// $ANTLR end "predicateObjectList"



	// $ANTLR start "verb"
	// TurtleOBDA.g:453:1: verb returns [Term value] : ( predicate | 'a' );
	public final Term verb() throws RecognitionException {
		Term value = null;


		Term predicate7 =null;

		try {
			// TurtleOBDA.g:454:3: ( predicate | 'a' )
			int alt7=2;
			int LA7_0 = input.LA(1);
			if ( (LA7_0==PREFIXED_NAME||LA7_0==STRING_WITH_BRACKET) ) {
				alt7=1;
			}
			else if ( (LA7_0==77) ) {
				alt7=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 7, 0, input);
				throw nvae;
			}

			switch (alt7) {
				case 1 :
					// TurtleOBDA.g:454:5: predicate
					{
					pushFollow(FOLLOW_predicate_in_verb288);
					predicate7=predicate();
					state._fsp--;

					 value = predicate7; 
					}
					break;
				case 2 :
					// TurtleOBDA.g:455:5: 'a'
					{
					match(input,77,FOLLOW_77_in_verb296); 

					  Term constant = dfac.getConstantLiteral(OBDAVocabulary.RDF_TYPE);
					  value = dfac.getUriTemplate(constant);
					  
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
	// TurtleOBDA.g:461:1: objectList returns [List<Term> value] : o1= object ( COMMA o2= object )* ;
	public final List<Term> objectList() throws RecognitionException {
		List<Term> value = null;


		Term o1 =null;
		Term o2 =null;


		  value = new ArrayList<Term>();

		try {
			// TurtleOBDA.g:465:3: (o1= object ( COMMA o2= object )* )
			// TurtleOBDA.g:465:5: o1= object ( COMMA o2= object )*
			{
			pushFollow(FOLLOW_object_in_objectList322);
			o1=object();
			state._fsp--;

			 value.add(o1); 
			// TurtleOBDA.g:465:42: ( COMMA o2= object )*
			loop8:
			while (true) {
				int alt8=2;
				int LA8_0 = input.LA(1);
				if ( (LA8_0==COMMA) ) {
					alt8=1;
				}

				switch (alt8) {
				case 1 :
					// TurtleOBDA.g:465:43: COMMA o2= object
					{
					match(input,COMMA,FOLLOW_COMMA_in_objectList327); 
					pushFollow(FOLLOW_object_in_objectList331);
					o2=object();
					state._fsp--;

					 value.add(o2); 
					}
					break;

				default :
					break loop8;
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
	// $ANTLR end "objectList"



	// $ANTLR start "subject"
	// TurtleOBDA.g:468:1: subject returns [Term value] : ( resource | variable );
	public final Term subject() throws RecognitionException {
		Term value = null;


		Term resource8 =null;
		Variable variable9 =null;

		try {
			// TurtleOBDA.g:469:3: ( resource | variable )
			int alt9=2;
			int LA9_0 = input.LA(1);
			if ( (LA9_0==PREFIXED_NAME||LA9_0==STRING_WITH_BRACKET) ) {
				alt9=1;
			}
			else if ( (LA9_0==STRING_WITH_CURLY_BRACKET) ) {
				alt9=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 9, 0, input);
				throw nvae;
			}

			switch (alt9) {
				case 1 :
					// TurtleOBDA.g:469:5: resource
					{
					pushFollow(FOLLOW_resource_in_subject353);
					resource8=resource();
					state._fsp--;

					 value = resource8; 
					}
					break;
				case 2 :
					// TurtleOBDA.g:470:5: variable
					{
					pushFollow(FOLLOW_variable_in_subject361);
					variable9=variable();
					state._fsp--;

					 value = variable9; 
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
	// TurtleOBDA.g:475:1: predicate returns [Term value] : resource ;
	public final Term predicate() throws RecognitionException {
		Term value = null;


		Term resource10 =null;

		try {
			// TurtleOBDA.g:476:3: ( resource )
			// TurtleOBDA.g:476:5: resource
			{
			pushFollow(FOLLOW_resource_in_predicate382);
			resource10=resource();
			state._fsp--;


			  	value = resource10; 
			//      Term nl = resource10;
			//      if (nl instanceof URIConstant) {
			//        URIConstant c = (URIConstant) nl;
			//        value = c.getValue();
			//      } else {
			//        throw new RuntimeException("Unsupported predicate syntax: " + nl.toString());
			//      }
			    
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
	// TurtleOBDA.g:488:1: object returns [Term value] : ( resource | literal | typedLiteral | variable );
	public final Term object() throws RecognitionException {
		Term value = null;


		Term resource11 =null;
		Term literal12 =null;
		Function typedLiteral13 =null;
		Variable variable14 =null;

		try {
			// TurtleOBDA.g:489:3: ( resource | literal | typedLiteral | variable )
			int alt10=4;
			switch ( input.LA(1) ) {
			case PREFIXED_NAME:
			case STRING_WITH_BRACKET:
				{
				alt10=1;
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
				alt10=2;
				}
				break;
			case STRING_WITH_CURLY_BRACKET:
				{
				int LA10_3 = input.LA(2);
				if ( (LA10_3==AT||LA10_3==REFERENCE) ) {
					alt10=3;
				}
				else if ( (LA10_3==COMMA||LA10_3==PERIOD||LA10_3==SEMI||LA10_3==WS) ) {
					alt10=4;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 10, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 10, 0, input);
				throw nvae;
			}
			switch (alt10) {
				case 1 :
					// TurtleOBDA.g:489:5: resource
					{
					pushFollow(FOLLOW_resource_in_object401);
					resource11=resource();
					state._fsp--;

					 value = resource11; 
					}
					break;
				case 2 :
					// TurtleOBDA.g:490:5: literal
					{
					pushFollow(FOLLOW_literal_in_object409);
					literal12=literal();
					state._fsp--;

					 value = literal12; 
					}
					break;
				case 3 :
					// TurtleOBDA.g:491:5: typedLiteral
					{
					pushFollow(FOLLOW_typedLiteral_in_object418);
					typedLiteral13=typedLiteral();
					state._fsp--;

					 value = typedLiteral13; 
					}
					break;
				case 4 :
					// TurtleOBDA.g:492:5: variable
					{
					pushFollow(FOLLOW_variable_in_object426);
					variable14=variable();
					state._fsp--;

					 value = variable14; 
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
	// TurtleOBDA.g:496:1: resource returns [Term value] : ( uriref | qname );
	public final Term resource() throws RecognitionException {
		Term value = null;


		String uriref15 =null;
		String qname16 =null;

		try {
			// TurtleOBDA.g:497:4: ( uriref | qname )
			int alt11=2;
			int LA11_0 = input.LA(1);
			if ( (LA11_0==STRING_WITH_BRACKET) ) {
				alt11=1;
			}
			else if ( (LA11_0==PREFIXED_NAME) ) {
				alt11=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 11, 0, input);
				throw nvae;
			}

			switch (alt11) {
				case 1 :
					// TurtleOBDA.g:497:6: uriref
					{
					pushFollow(FOLLOW_uriref_in_resource447);
					uriref15=uriref();
					state._fsp--;

					 value = construct(uriref15); 
					}
					break;
				case 2 :
					// TurtleOBDA.g:498:6: qname
					{
					pushFollow(FOLLOW_qname_in_resource456);
					qname16=qname();
					state._fsp--;

					 value = construct(qname16); 
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
	// TurtleOBDA.g:503:1: uriref returns [String value] : STRING_WITH_BRACKET ;
	public final String uriref() throws RecognitionException {
		String value = null;


		Token STRING_WITH_BRACKET17=null;

		try {
			// TurtleOBDA.g:504:3: ( STRING_WITH_BRACKET )
			// TurtleOBDA.g:504:5: STRING_WITH_BRACKET
			{
			STRING_WITH_BRACKET17=(Token)match(input,STRING_WITH_BRACKET,FOLLOW_STRING_WITH_BRACKET_in_uriref481); 
			 value = removeBrackets((STRING_WITH_BRACKET17!=null?STRING_WITH_BRACKET17.getText():null)); 
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
	// TurtleOBDA.g:507:1: qname returns [String value] : PREFIXED_NAME ;
	public final String qname() throws RecognitionException {
		String value = null;


		Token PREFIXED_NAME18=null;

		try {
			// TurtleOBDA.g:508:3: ( PREFIXED_NAME )
			// TurtleOBDA.g:508:5: PREFIXED_NAME
			{
			PREFIXED_NAME18=(Token)match(input,PREFIXED_NAME,FOLLOW_PREFIXED_NAME_in_qname500); 

			      String[] tokens = (PREFIXED_NAME18!=null?PREFIXED_NAME18.getText():null).split(":", 2);
			      String uri = directives.get(tokens[0]);  // the first token is the prefix
			      value = uri + tokens[1];  // the second token is the local name
			    
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
	// TurtleOBDA.g:515:1: blank : ( nodeID | BLANK );
	public final void blank() throws RecognitionException {
		try {
			// TurtleOBDA.g:516:3: ( nodeID | BLANK )
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
					// TurtleOBDA.g:516:5: nodeID
					{
					pushFollow(FOLLOW_nodeID_in_blank515);
					nodeID();
					state._fsp--;

					}
					break;
				case 2 :
					// TurtleOBDA.g:517:5: BLANK
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
	}
	// $ANTLR end "blank"



	// $ANTLR start "variable"
	// TurtleOBDA.g:520:1: variable returns [Variable value] : STRING_WITH_CURLY_BRACKET ;
	public final Variable variable() throws RecognitionException {
		Variable value = null;


		Token STRING_WITH_CURLY_BRACKET19=null;

		try {
			// TurtleOBDA.g:521:3: ( STRING_WITH_CURLY_BRACKET )
			// TurtleOBDA.g:521:5: STRING_WITH_CURLY_BRACKET
			{
			STRING_WITH_CURLY_BRACKET19=(Token)match(input,STRING_WITH_CURLY_BRACKET,FOLLOW_STRING_WITH_CURLY_BRACKET_in_variable538); 

			      value = dfac.getVariable(removeBrackets((STRING_WITH_CURLY_BRACKET19!=null?STRING_WITH_CURLY_BRACKET19.getText():null)));
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
	// TurtleOBDA.g:527:1: function returns [Function value] : resource LPAREN terms RPAREN ;
	public final Function function() throws RecognitionException {
		Function value = null;


		Term resource20 =null;
		Vector<Term> terms21 =null;

		try {
			// TurtleOBDA.g:528:3: ( resource LPAREN terms RPAREN )
			// TurtleOBDA.g:528:5: resource LPAREN terms RPAREN
			{
			pushFollow(FOLLOW_resource_in_function559);
			resource20=resource();
			state._fsp--;

			match(input,LPAREN,FOLLOW_LPAREN_in_function561); 
			pushFollow(FOLLOW_terms_in_function563);
			terms21=terms();
			state._fsp--;

			match(input,RPAREN,FOLLOW_RPAREN_in_function565); 

			      String functionName = resource20.toString();
			      int arity = terms21.size();
			      Predicate functionSymbol = dfac.getPredicate(functionName, arity);
			      value = dfac.getFunction(functionSymbol, terms21);
			    
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



	// $ANTLR start "typedLiteral"
	// TurtleOBDA.g:536:1: typedLiteral returns [Function value] : ( variable AT language | variable REFERENCE resource );
	public final Function typedLiteral() throws RecognitionException {
		Function value = null;


		Variable variable22 =null;
		Term language23 =null;
		Variable variable24 =null;
		Term resource25 =null;

		try {
			// TurtleOBDA.g:537:3: ( variable AT language | variable REFERENCE resource )
			int alt13=2;
			int LA13_0 = input.LA(1);
			if ( (LA13_0==STRING_WITH_CURLY_BRACKET) ) {
				int LA13_1 = input.LA(2);
				if ( (LA13_1==AT) ) {
					alt13=1;
				}
				else if ( (LA13_1==REFERENCE) ) {
					alt13=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 13, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 13, 0, input);
				throw nvae;
			}

			switch (alt13) {
				case 1 :
					// TurtleOBDA.g:537:5: variable AT language
					{
					pushFollow(FOLLOW_variable_in_typedLiteral584);
					variable22=variable();
					state._fsp--;

					match(input,AT,FOLLOW_AT_in_typedLiteral586); 
					pushFollow(FOLLOW_language_in_typedLiteral588);
					language23=language();
					state._fsp--;


					      Variable var = variable22;
					      Term lang = language23;   
					      value = dfac.getTypedTerm(var, lang);

					    
					}
					break;
				case 2 :
					// TurtleOBDA.g:543:5: variable REFERENCE resource
					{
					pushFollow(FOLLOW_variable_in_typedLiteral596);
					variable24=variable();
					state._fsp--;

					match(input,REFERENCE,FOLLOW_REFERENCE_in_typedLiteral598); 
					pushFollow(FOLLOW_resource_in_typedLiteral600);
					resource25=resource();
					state._fsp--;


					      Variable var = variable24;
					      //String functionName = resource25.toString();
					      // resource25 must be a URIConstant
					    String functionName = null;
					    if (resource25 instanceof Function){
					       functionName = ((ValueConstant) ((Function)resource25).getTerm(0)).getValue();
					    } else {
					        throw new IllegalArgumentException("resource25 should be an URI");
					    }
					    Predicate.COL_TYPE type = dtfac.getDatatype(functionName);
					    if (type == null)  
					 	  throw new RuntimeException("ERROR. A mapping involves an unsupported datatype. \nOffending datatype:" + functionName);
					    
					      value = dfac.getTypedTerm(var, type);

						
					     
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
	// $ANTLR end "typedLiteral"



	// $ANTLR start "language"
	// TurtleOBDA.g:563:1: language returns [Term value] : ( languageTag | variable );
	public final Term language() throws RecognitionException {
		Term value = null;


		ParserRuleReturnScope languageTag26 =null;
		Variable variable27 =null;

		try {
			// TurtleOBDA.g:564:3: ( languageTag | variable )
			int alt14=2;
			int LA14_0 = input.LA(1);
			if ( (LA14_0==VARNAME) ) {
				alt14=1;
			}
			else if ( (LA14_0==STRING_WITH_CURLY_BRACKET) ) {
				alt14=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 14, 0, input);
				throw nvae;
			}

			switch (alt14) {
				case 1 :
					// TurtleOBDA.g:564:5: languageTag
					{
					pushFollow(FOLLOW_languageTag_in_language619);
					languageTag26=languageTag();
					state._fsp--;


					    	value = dfac.getConstantLiteral((languageTag26!=null?input.toString(languageTag26.start,languageTag26.stop):null).toLowerCase(), COL_TYPE.STRING);
					    
					}
					break;
				case 2 :
					// TurtleOBDA.g:567:5: variable
					{
					pushFollow(FOLLOW_variable_in_language627);
					variable27=variable();
					state._fsp--;


					    	value = variable27;
					    
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
	// $ANTLR end "language"



	// $ANTLR start "terms"
	// TurtleOBDA.g:572:1: terms returns [Vector<Term> value] : t1= term ( COMMA t2= term )* ;
	public final Vector<Term> terms() throws RecognitionException {
		Vector<Term> value = null;


		Term t1 =null;
		Term t2 =null;


		  value = new Vector<Term>();

		try {
			// TurtleOBDA.g:576:3: (t1= term ( COMMA t2= term )* )
			// TurtleOBDA.g:576:5: t1= term ( COMMA t2= term )*
			{
			pushFollow(FOLLOW_term_in_terms653);
			t1=term();
			state._fsp--;

			 value.add(t1); 
			// TurtleOBDA.g:576:40: ( COMMA t2= term )*
			loop15:
			while (true) {
				int alt15=2;
				int LA15_0 = input.LA(1);
				if ( (LA15_0==COMMA) ) {
					alt15=1;
				}

				switch (alt15) {
				case 1 :
					// TurtleOBDA.g:576:41: COMMA t2= term
					{
					match(input,COMMA,FOLLOW_COMMA_in_terms658); 
					pushFollow(FOLLOW_term_in_terms662);
					t2=term();
					state._fsp--;

					 value.add(t2); 
					}
					break;

				default :
					break loop15;
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
	// $ANTLR end "terms"



	// $ANTLR start "term"
	// TurtleOBDA.g:579:1: term returns [Term value] : ( function | variable | literal );
	public final Term term() throws RecognitionException {
		Term value = null;


		Function function28 =null;
		Variable variable29 =null;
		Term literal30 =null;

		try {
			// TurtleOBDA.g:580:3: ( function | variable | literal )
			int alt16=3;
			switch ( input.LA(1) ) {
			case PREFIXED_NAME:
			case STRING_WITH_BRACKET:
				{
				alt16=1;
				}
				break;
			case STRING_WITH_CURLY_BRACKET:
				{
				alt16=2;
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
					// TurtleOBDA.g:580:5: function
					{
					pushFollow(FOLLOW_function_in_term683);
					function28=function();
					state._fsp--;

					 value = function28; 
					}
					break;
				case 2 :
					// TurtleOBDA.g:581:5: variable
					{
					pushFollow(FOLLOW_variable_in_term691);
					variable29=variable();
					state._fsp--;

					 value = variable29; 
					}
					break;
				case 3 :
					// TurtleOBDA.g:582:5: literal
					{
					pushFollow(FOLLOW_literal_in_term699);
					literal30=literal();
					state._fsp--;

					 value = literal30; 
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
	// TurtleOBDA.g:591:1: literal returns [Term value] : ( stringLiteral ( AT language )? | dataTypeString | numericLiteral | booleanLiteral );
	public final Term literal() throws RecognitionException {
		Term value = null;


		Term language31 =null;
		Term stringLiteral32 =null;
		Term dataTypeString33 =null;
		Term numericLiteral34 =null;
		Term booleanLiteral35 =null;

		try {
			// TurtleOBDA.g:592:3: ( stringLiteral ( AT language )? | dataTypeString | numericLiteral | booleanLiteral )
			int alt18=4;
			switch ( input.LA(1) ) {
			case STRING_WITH_QUOTE_DOUBLE:
				{
				int LA18_1 = input.LA(2);
				if ( (LA18_1==AT||LA18_1==COMMA||LA18_1==PERIOD||LA18_1==RPAREN||LA18_1==SEMI||LA18_1==WS) ) {
					alt18=1;
				}
				else if ( (LA18_1==REFERENCE) ) {
					alt18=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 18, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
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
				alt18=3;
				}
				break;
			case FALSE:
			case TRUE:
				{
				alt18=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 18, 0, input);
				throw nvae;
			}
			switch (alt18) {
				case 1 :
					// TurtleOBDA.g:592:5: stringLiteral ( AT language )?
					{
					pushFollow(FOLLOW_stringLiteral_in_literal719);
					stringLiteral32=stringLiteral();
					state._fsp--;

					// TurtleOBDA.g:592:19: ( AT language )?
					int alt17=2;
					int LA17_0 = input.LA(1);
					if ( (LA17_0==AT) ) {
						alt17=1;
					}
					switch (alt17) {
						case 1 :
							// TurtleOBDA.g:592:20: AT language
							{
							match(input,AT,FOLLOW_AT_in_literal722); 
							pushFollow(FOLLOW_language_in_literal724);
							language31=language();
							state._fsp--;

							}
							break;

					}


					       Term lang = language31;
					       Term literal = stringLiteral32;
					       if (literal instanceof Function){
					          Function f = (Function)stringLiteral32;
					          if (lang != null){
					             value = dfac.getTypedTerm(f,lang);
					          }else{
					             value = dfac.getTypedTerm(f, COL_TYPE.LITERAL);
					          }       
					       }else{

					       //if variable we cannot assign a datatype yet
					       if (literal instanceof Variable)
					       {
					            value = dfac.getTypedTerm(literal, COL_TYPE.LITERAL);
					       }
					       else{
					          ValueConstant constant = (ValueConstant)stringLiteral32;
					          if (lang != null) {
						     value = dfac.getTypedTerm(constant, lang);
					          } else {
					      	     value = dfac.getTypedTerm(constant, COL_TYPE.LITERAL);
					          }
					       }
					       }
					    
					}
					break;
				case 2 :
					// TurtleOBDA.g:619:5: dataTypeString
					{
					pushFollow(FOLLOW_dataTypeString_in_literal734);
					dataTypeString33=dataTypeString();
					state._fsp--;

					 value = dataTypeString33; 
					}
					break;
				case 3 :
					// TurtleOBDA.g:620:5: numericLiteral
					{
					pushFollow(FOLLOW_numericLiteral_in_literal742);
					numericLiteral34=numericLiteral();
					state._fsp--;

					 value = numericLiteral34; 
					}
					break;
				case 4 :
					// TurtleOBDA.g:621:5: booleanLiteral
					{
					pushFollow(FOLLOW_booleanLiteral_in_literal750);
					booleanLiteral35=booleanLiteral();
					state._fsp--;

					 value = booleanLiteral35; 
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
	// TurtleOBDA.g:624:1: stringLiteral returns [Term value] : STRING_WITH_QUOTE_DOUBLE ;
	public final Term stringLiteral() throws RecognitionException {
		Term value = null;


		Token STRING_WITH_QUOTE_DOUBLE36=null;

		try {
			// TurtleOBDA.g:625:3: ( STRING_WITH_QUOTE_DOUBLE )
			// TurtleOBDA.g:625:5: STRING_WITH_QUOTE_DOUBLE
			{
			STRING_WITH_QUOTE_DOUBLE36=(Token)match(input,STRING_WITH_QUOTE_DOUBLE,FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_stringLiteral769); 

			      String str = (STRING_WITH_QUOTE_DOUBLE36!=null?STRING_WITH_QUOTE_DOUBLE36.getText():null);
			      if (str.contains("{")){
			      	value = getNestedConcat(str);
			      }else{
			      	value = dfac.getConstantLiteral(str.substring(1, str.length()-1), COL_TYPE.LITERAL); // without the double quotes
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
	// $ANTLR end "stringLiteral"



	// $ANTLR start "dataTypeString"
	// TurtleOBDA.g:635:1: dataTypeString returns [Term value] : stringLiteral REFERENCE resource ;
	public final Term dataTypeString() throws RecognitionException {
		Term value = null;


		Term stringLiteral37 =null;
		Term resource38 =null;

		try {
			// TurtleOBDA.g:636:3: ( stringLiteral REFERENCE resource )
			// TurtleOBDA.g:636:6: stringLiteral REFERENCE resource
			{
			pushFollow(FOLLOW_stringLiteral_in_dataTypeString789);
			stringLiteral37=stringLiteral();
			state._fsp--;

			match(input,REFERENCE,FOLLOW_REFERENCE_in_dataTypeString791); 
			pushFollow(FOLLOW_resource_in_dataTypeString793);
			resource38=resource();
			state._fsp--;


			      Term stringValue = stringLiteral37;

			          if (resource38 instanceof Function){
			          	    String functionName = ( (ValueConstant) ((Function)resource38).getTerm(0) ).getValue();

			                    Predicate.COL_TYPE type = dtfac.getDatatype(functionName);
			                    if (type == null) {
			                      throw new RuntimeException("Unsupported datatype: " + functionName);
			                    }
			                    value = dfac.getTypedTerm(stringValue, type);
			                    }
			           else {
			          value = dfac.getTypedTerm(stringValue, COL_TYPE.LITERAL);
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
	// $ANTLR end "dataTypeString"



	// $ANTLR start "numericLiteral"
	// TurtleOBDA.g:654:1: numericLiteral returns [Term value] : ( numericUnsigned | numericPositive | numericNegative );
	public final Term numericLiteral() throws RecognitionException {
		Term value = null;


		Term numericUnsigned39 =null;
		Term numericPositive40 =null;
		Term numericNegative41 =null;

		try {
			// TurtleOBDA.g:655:3: ( numericUnsigned | numericPositive | numericNegative )
			int alt19=3;
			switch ( input.LA(1) ) {
			case DECIMAL:
			case DOUBLE:
			case INTEGER:
				{
				alt19=1;
				}
				break;
			case DECIMAL_POSITIVE:
			case DOUBLE_POSITIVE:
			case INTEGER_POSITIVE:
				{
				alt19=2;
				}
				break;
			case DECIMAL_NEGATIVE:
			case DOUBLE_NEGATIVE:
			case INTEGER_NEGATIVE:
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
					// TurtleOBDA.g:655:5: numericUnsigned
					{
					pushFollow(FOLLOW_numericUnsigned_in_numericLiteral809);
					numericUnsigned39=numericUnsigned();
					state._fsp--;

					 value = numericUnsigned39; 
					}
					break;
				case 2 :
					// TurtleOBDA.g:656:5: numericPositive
					{
					pushFollow(FOLLOW_numericPositive_in_numericLiteral817);
					numericPositive40=numericPositive();
					state._fsp--;

					 value = numericPositive40; 
					}
					break;
				case 3 :
					// TurtleOBDA.g:657:5: numericNegative
					{
					pushFollow(FOLLOW_numericNegative_in_numericLiteral825);
					numericNegative41=numericNegative();
					state._fsp--;

					 value = numericNegative41; 
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
	// TurtleOBDA.g:660:1: nodeID : BLANK_PREFIX name ;
	public final void nodeID() throws RecognitionException {
		try {
			// TurtleOBDA.g:661:3: ( BLANK_PREFIX name )
			// TurtleOBDA.g:661:5: BLANK_PREFIX name
			{
			match(input,BLANK_PREFIX,FOLLOW_BLANK_PREFIX_in_nodeID840); 
			pushFollow(FOLLOW_name_in_nodeID842);
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
	}
	// $ANTLR end "nodeID"



	// $ANTLR start "relativeURI"
	// TurtleOBDA.g:664:1: relativeURI : STRING_URI ;
	public final void relativeURI() throws RecognitionException {
		try {
			// TurtleOBDA.g:665:3: ( STRING_URI )
			// TurtleOBDA.g:665:5: STRING_URI
			{
			match(input,STRING_URI,FOLLOW_STRING_URI_in_relativeURI856); 
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
	// $ANTLR end "relativeURI"


	public static class namespace_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "namespace"
	// TurtleOBDA.g:668:1: namespace : NAMESPACE ;
	public final TurtleOBDAParser.namespace_return namespace() throws RecognitionException {
		TurtleOBDAParser.namespace_return retval = new TurtleOBDAParser.namespace_return();
		retval.start = input.LT(1);

		try {
			// TurtleOBDA.g:669:3: ( NAMESPACE )
			// TurtleOBDA.g:669:5: NAMESPACE
			{
			match(input,NAMESPACE,FOLLOW_NAMESPACE_in_namespace869); 
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
	// $ANTLR end "namespace"


	public static class defaultNamespace_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "defaultNamespace"
	// TurtleOBDA.g:672:1: defaultNamespace : COLON ;
	public final TurtleOBDAParser.defaultNamespace_return defaultNamespace() throws RecognitionException {
		TurtleOBDAParser.defaultNamespace_return retval = new TurtleOBDAParser.defaultNamespace_return();
		retval.start = input.LT(1);

		try {
			// TurtleOBDA.g:673:3: ( COLON )
			// TurtleOBDA.g:673:5: COLON
			{
			match(input,COLON,FOLLOW_COLON_in_defaultNamespace884); 
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
	// $ANTLR end "defaultNamespace"



	// $ANTLR start "name"
	// TurtleOBDA.g:676:1: name : VARNAME ;
	public final void name() throws RecognitionException {
		try {
			// TurtleOBDA.g:677:3: ( VARNAME )
			// TurtleOBDA.g:677:5: VARNAME
			{
			match(input,VARNAME,FOLLOW_VARNAME_in_name897); 
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
	// $ANTLR end "name"


	public static class languageTag_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "languageTag"
	// TurtleOBDA.g:680:1: languageTag : VARNAME ;
	public final TurtleOBDAParser.languageTag_return languageTag() throws RecognitionException {
		TurtleOBDAParser.languageTag_return retval = new TurtleOBDAParser.languageTag_return();
		retval.start = input.LT(1);

		try {
			// TurtleOBDA.g:681:3: ( VARNAME )
			// TurtleOBDA.g:681:5: VARNAME
			{
			match(input,VARNAME,FOLLOW_VARNAME_in_languageTag910); 
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
	// $ANTLR end "languageTag"



	// $ANTLR start "booleanLiteral"
	// TurtleOBDA.g:684:1: booleanLiteral returns [Term value] : ( TRUE | FALSE );
	public final Term booleanLiteral() throws RecognitionException {
		Term value = null;


		Token TRUE42=null;
		Token FALSE43=null;

		try {
			// TurtleOBDA.g:685:3: ( TRUE | FALSE )
			int alt20=2;
			int LA20_0 = input.LA(1);
			if ( (LA20_0==TRUE) ) {
				alt20=1;
			}
			else if ( (LA20_0==FALSE) ) {
				alt20=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 20, 0, input);
				throw nvae;
			}

			switch (alt20) {
				case 1 :
					// TurtleOBDA.g:685:5: TRUE
					{
					TRUE42=(Token)match(input,TRUE,FOLLOW_TRUE_in_booleanLiteral927); 

					  ValueConstant trueConstant = dfac.getConstantLiteral((TRUE42!=null?TRUE42.getText():null), COL_TYPE.LITERAL);
					  value = dfac.getTypedTerm(trueConstant, COL_TYPE.BOOLEAN); 
					}
					break;
				case 2 :
					// TurtleOBDA.g:688:5: FALSE
					{
					FALSE43=(Token)match(input,FALSE,FOLLOW_FALSE_in_booleanLiteral936); 

					  ValueConstant falseConstant = dfac.getConstantLiteral((FALSE43!=null?FALSE43.getText():null), COL_TYPE.LITERAL);
					  value = dfac.getTypedTerm(falseConstant, COL_TYPE.BOOLEAN);
					  
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
	// TurtleOBDA.g:694:1: numericUnsigned returns [Term value] : ( INTEGER | DOUBLE | DECIMAL );
	public final Term numericUnsigned() throws RecognitionException {
		Term value = null;


		Token INTEGER44=null;
		Token DOUBLE45=null;
		Token DECIMAL46=null;

		try {
			// TurtleOBDA.g:695:3: ( INTEGER | DOUBLE | DECIMAL )
			int alt21=3;
			switch ( input.LA(1) ) {
			case INTEGER:
				{
				alt21=1;
				}
				break;
			case DOUBLE:
				{
				alt21=2;
				}
				break;
			case DECIMAL:
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
					// TurtleOBDA.g:695:5: INTEGER
					{
					INTEGER44=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_numericUnsigned955); 

					  ValueConstant integerConstant = dfac.getConstantLiteral((INTEGER44!=null?INTEGER44.getText():null), COL_TYPE.LITERAL);
					  value = dfac.getTypedTerm(integerConstant, COL_TYPE.INTEGER);
					  
					}
					break;
				case 2 :
					// TurtleOBDA.g:699:5: DOUBLE
					{
					DOUBLE45=(Token)match(input,DOUBLE,FOLLOW_DOUBLE_in_numericUnsigned963); 

					  ValueConstant doubleConstant = dfac.getConstantLiteral((DOUBLE45!=null?DOUBLE45.getText():null), COL_TYPE.LITERAL);
					  value = dfac.getTypedTerm(doubleConstant, COL_TYPE.DOUBLE);
					  
					}
					break;
				case 3 :
					// TurtleOBDA.g:703:5: DECIMAL
					{
					DECIMAL46=(Token)match(input,DECIMAL,FOLLOW_DECIMAL_in_numericUnsigned972); 

					  ValueConstant decimalConstant = dfac.getConstantLiteral((DECIMAL46!=null?DECIMAL46.getText():null), COL_TYPE.LITERAL);
					  value = dfac.getTypedTerm(decimalConstant, COL_TYPE.DECIMAL);
					   
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
	// TurtleOBDA.g:709:1: numericPositive returns [Term value] : ( INTEGER_POSITIVE | DOUBLE_POSITIVE | DECIMAL_POSITIVE );
	public final Term numericPositive() throws RecognitionException {
		Term value = null;


		Token INTEGER_POSITIVE47=null;
		Token DOUBLE_POSITIVE48=null;
		Token DECIMAL_POSITIVE49=null;

		try {
			// TurtleOBDA.g:710:3: ( INTEGER_POSITIVE | DOUBLE_POSITIVE | DECIMAL_POSITIVE )
			int alt22=3;
			switch ( input.LA(1) ) {
			case INTEGER_POSITIVE:
				{
				alt22=1;
				}
				break;
			case DOUBLE_POSITIVE:
				{
				alt22=2;
				}
				break;
			case DECIMAL_POSITIVE:
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
					// TurtleOBDA.g:710:5: INTEGER_POSITIVE
					{
					INTEGER_POSITIVE47=(Token)match(input,INTEGER_POSITIVE,FOLLOW_INTEGER_POSITIVE_in_numericPositive991); 

					   ValueConstant integerConstant = dfac.getConstantLiteral((INTEGER_POSITIVE47!=null?INTEGER_POSITIVE47.getText():null), COL_TYPE.LITERAL);
					   value = dfac.getTypedTerm(integerConstant, COL_TYPE.INTEGER);
					  
					}
					break;
				case 2 :
					// TurtleOBDA.g:714:5: DOUBLE_POSITIVE
					{
					DOUBLE_POSITIVE48=(Token)match(input,DOUBLE_POSITIVE,FOLLOW_DOUBLE_POSITIVE_in_numericPositive999); 

					  ValueConstant doubleConstant = dfac.getConstantLiteral((DOUBLE_POSITIVE48!=null?DOUBLE_POSITIVE48.getText():null), COL_TYPE.LITERAL);
					  value = dfac.getTypedTerm(doubleConstant, COL_TYPE.DOUBLE);
					  
					}
					break;
				case 3 :
					// TurtleOBDA.g:718:5: DECIMAL_POSITIVE
					{
					DECIMAL_POSITIVE49=(Token)match(input,DECIMAL_POSITIVE,FOLLOW_DECIMAL_POSITIVE_in_numericPositive1008); 

					  ValueConstant decimalConstant = dfac.getConstantLiteral((DECIMAL_POSITIVE49!=null?DECIMAL_POSITIVE49.getText():null), COL_TYPE.LITERAL);
					  value = dfac.getTypedTerm(decimalConstant, COL_TYPE.DECIMAL);
					   
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
	// TurtleOBDA.g:724:1: numericNegative returns [Term value] : ( INTEGER_NEGATIVE | DOUBLE_NEGATIVE | DECIMAL_NEGATIVE );
	public final Term numericNegative() throws RecognitionException {
		Term value = null;


		Token INTEGER_NEGATIVE50=null;
		Token DOUBLE_NEGATIVE51=null;
		Token DECIMAL_NEGATIVE52=null;

		try {
			// TurtleOBDA.g:725:3: ( INTEGER_NEGATIVE | DOUBLE_NEGATIVE | DECIMAL_NEGATIVE )
			int alt23=3;
			switch ( input.LA(1) ) {
			case INTEGER_NEGATIVE:
				{
				alt23=1;
				}
				break;
			case DOUBLE_NEGATIVE:
				{
				alt23=2;
				}
				break;
			case DECIMAL_NEGATIVE:
				{
				alt23=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 23, 0, input);
				throw nvae;
			}
			switch (alt23) {
				case 1 :
					// TurtleOBDA.g:725:5: INTEGER_NEGATIVE
					{
					INTEGER_NEGATIVE50=(Token)match(input,INTEGER_NEGATIVE,FOLLOW_INTEGER_NEGATIVE_in_numericNegative1027); 

					  ValueConstant integerConstant = dfac.getConstantLiteral((INTEGER_NEGATIVE50!=null?INTEGER_NEGATIVE50.getText():null), COL_TYPE.LITERAL);
					  value = dfac.getTypedTerm(integerConstant, COL_TYPE.INTEGER);
					  
					}
					break;
				case 2 :
					// TurtleOBDA.g:729:5: DOUBLE_NEGATIVE
					{
					DOUBLE_NEGATIVE51=(Token)match(input,DOUBLE_NEGATIVE,FOLLOW_DOUBLE_NEGATIVE_in_numericNegative1035); 

					   ValueConstant doubleConstant = dfac.getConstantLiteral((DOUBLE_NEGATIVE51!=null?DOUBLE_NEGATIVE51.getText():null), COL_TYPE.LITERAL);
					   value = dfac.getTypedTerm(doubleConstant, COL_TYPE.DOUBLE);
					  
					}
					break;
				case 3 :
					// TurtleOBDA.g:733:5: DECIMAL_NEGATIVE
					{
					DECIMAL_NEGATIVE52=(Token)match(input,DECIMAL_NEGATIVE,FOLLOW_DECIMAL_NEGATIVE_in_numericNegative1044); 

					  ValueConstant decimalConstant = dfac.getConstantLiteral((DECIMAL_NEGATIVE52!=null?DECIMAL_NEGATIVE52.getText():null), COL_TYPE.LITERAL);
					  value = dfac.getTypedTerm(decimalConstant, COL_TYPE.DECIMAL);
					  
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



	public static final BitSet FOLLOW_directiveStatement_in_parse54 = new BitSet(new long[]{0x0040000000000200L,0x0000000000000018L});
	public static final BitSet FOLLOW_triplesStatement_in_parse63 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000018L});
	public static final BitSet FOLLOW_triplesStatement_in_parse76 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000018L});
	public static final BitSet FOLLOW_EOF_in_parse83 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_directive_in_directiveStatement96 = new BitSet(new long[]{0x0008000000000000L});
	public static final BitSet FOLLOW_PERIOD_in_directiveStatement98 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_triples_in_triplesStatement115 = new BitSet(new long[]{0x0008000000000000L,0x0000000000001000L});
	public static final BitSet FOLLOW_WS_in_triplesStatement117 = new BitSet(new long[]{0x0008000000000000L,0x0000000000001000L});
	public static final BitSet FOLLOW_PERIOD_in_triplesStatement120 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_base_in_directive135 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_prefixID_in_directive141 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_AT_in_base154 = new BitSet(new long[]{0x0000000000000800L});
	public static final BitSet FOLLOW_BASE_in_base156 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_uriref_in_base158 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_AT_in_prefixID176 = new BitSet(new long[]{0x0020000000000000L});
	public static final BitSet FOLLOW_PREFIX_in_prefixID178 = new BitSet(new long[]{0x0000200000010000L});
	public static final BitSet FOLLOW_namespace_in_prefixID181 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_defaultNamespace_in_prefixID187 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_uriref_in_prefixID192 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_subject_in_triples211 = new BitSet(new long[]{0x0040000000000000L,0x0000000000002008L});
	public static final BitSet FOLLOW_predicateObjectList_in_triples215 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_verb_in_predicateObjectList241 = new BitSet(new long[]{0x00400070439C0000L,0x0000000000000158L});
	public static final BitSet FOLLOW_objectList_in_predicateObjectList247 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000001L});
	public static final BitSet FOLLOW_SEMI_in_predicateObjectList256 = new BitSet(new long[]{0x0040000000000000L,0x0000000000002008L});
	public static final BitSet FOLLOW_verb_in_predicateObjectList260 = new BitSet(new long[]{0x00400070439C0000L,0x0000000000000158L});
	public static final BitSet FOLLOW_objectList_in_predicateObjectList264 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000001L});
	public static final BitSet FOLLOW_predicate_in_verb288 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_77_in_verb296 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_object_in_objectList322 = new BitSet(new long[]{0x0000000000020002L});
	public static final BitSet FOLLOW_COMMA_in_objectList327 = new BitSet(new long[]{0x00400070439C0000L,0x0000000000000158L});
	public static final BitSet FOLLOW_object_in_objectList331 = new BitSet(new long[]{0x0000000000020002L});
	public static final BitSet FOLLOW_resource_in_subject353 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variable_in_subject361 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_resource_in_predicate382 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_resource_in_object401 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_literal_in_object409 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_typedLiteral_in_object418 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variable_in_object426 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_uriref_in_resource447 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_qname_in_resource456 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_WITH_BRACKET_in_uriref481 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PREFIXED_NAME_in_qname500 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_nodeID_in_blank515 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BLANK_in_blank521 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_WITH_CURLY_BRACKET_in_variable538 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_resource_in_function559 = new BitSet(new long[]{0x0000020000000000L});
	public static final BitSet FOLLOW_LPAREN_in_function561 = new BitSet(new long[]{0x00400070439C0000L,0x0000000000000158L});
	public static final BitSet FOLLOW_terms_in_function563 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_RPAREN_in_function565 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variable_in_typedLiteral584 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_AT_in_typedLiteral586 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000810L});
	public static final BitSet FOLLOW_language_in_typedLiteral588 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variable_in_typedLiteral596 = new BitSet(new long[]{0x0800000000000000L});
	public static final BitSet FOLLOW_REFERENCE_in_typedLiteral598 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_resource_in_typedLiteral600 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_languageTag_in_language619 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variable_in_language627 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_term_in_terms653 = new BitSet(new long[]{0x0000000000020002L});
	public static final BitSet FOLLOW_COMMA_in_terms658 = new BitSet(new long[]{0x00400070439C0000L,0x0000000000000158L});
	public static final BitSet FOLLOW_term_in_terms662 = new BitSet(new long[]{0x0000000000020002L});
	public static final BitSet FOLLOW_function_in_term683 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variable_in_term691 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_literal_in_term699 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_stringLiteral_in_literal719 = new BitSet(new long[]{0x0000000000000202L});
	public static final BitSet FOLLOW_AT_in_literal722 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000810L});
	public static final BitSet FOLLOW_language_in_literal724 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dataTypeString_in_literal734 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_numericLiteral_in_literal742 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_booleanLiteral_in_literal750 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_stringLiteral769 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_stringLiteral_in_dataTypeString789 = new BitSet(new long[]{0x0800000000000000L});
	public static final BitSet FOLLOW_REFERENCE_in_dataTypeString791 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_resource_in_dataTypeString793 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_numericUnsigned_in_numericLiteral809 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_numericPositive_in_numericLiteral817 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_numericNegative_in_numericLiteral825 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BLANK_PREFIX_in_nodeID840 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
	public static final BitSet FOLLOW_name_in_nodeID842 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_URI_in_relativeURI856 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NAMESPACE_in_namespace869 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_COLON_in_defaultNamespace884 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VARNAME_in_name897 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VARNAME_in_languageTag910 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TRUE_in_booleanLiteral927 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FALSE_in_booleanLiteral936 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGER_in_numericUnsigned955 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOUBLE_in_numericUnsigned963 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DECIMAL_in_numericUnsigned972 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGER_POSITIVE_in_numericPositive991 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOUBLE_POSITIVE_in_numericPositive999 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DECIMAL_POSITIVE_in_numericPositive1008 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGER_NEGATIVE_in_numericNegative1027 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOUBLE_NEGATIVE_in_numericNegative1035 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DECIMAL_NEGATIVE_in_numericNegative1044 = new BitSet(new long[]{0x0000000000000002L});
}
