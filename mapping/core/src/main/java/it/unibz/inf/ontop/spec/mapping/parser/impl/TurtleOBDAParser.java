// $ANTLR 3.5.2 TurtleOBDA.g 2017-01-16 18:44:28

package it.unibz.inf.ontop.spec.mapping.parser.impl;

import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate.COL_TYPE;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static it.unibz.inf.ontop.model.IriConstants.RDF_TYPE;
import static it.unibz.inf.ontop.model.OntopModelSingletons.*;

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

	@Override
	public Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException {
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
		          ValueConstant uriTemplate = TERM_FACTORY.getConstantLiteral(token.toString()); // a single URI template
		          toReturn = TERM_FACTORY.getUriTemplate(uriTemplate);
		      }
		      else if (token instanceof ColumnString) {
		         // a single URI template
		         Variable column = TERM_FACTORY.getVariable(token.toString());
		         toReturn = TERM_FACTORY.getUriTemplate(column);
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
		            Variable column = TERM_FACTORY.getVariable(token.toString());
		            terms.add(column);
		         }
		      }
		      ValueConstant uriTemplate = TERM_FACTORY.getConstantLiteral(sb.toString()); // complete URI template
		      terms.add(0, uriTemplate);
		      toReturn = TERM_FACTORY.getUriTemplate(terms);
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
		         terms.add(TERM_FACTORY.getConstantLiteral(st));
		         str = str.substring(str.indexOf("{", i), str.length());
		      }else if (i == 0){
		         j = str.indexOf("}");
		         terms.add(TERM_FACTORY.getVariable(str.substring(1,j)));
		         str = str.substring(j+1,str.length());
		      } else {
		    	  break;
		      }
		   }
		   if(!str.equals("")){
		      str = str.replace("\\\\", "");
		      terms.add(TERM_FACTORY.getConstantLiteral(str));
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

	       Function f = TERM_FACTORY.getFunction(ExpressionOperation.CONCAT, terms.get(0), terms.get(1));
	       for(int j=2;j<terms.size();j++) {
	          f = TERM_FACTORY.getFunction(ExpressionOperation.CONCAT, f, terms.get(j));
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
			                      Predicate predicate = TERM_FACTORY.getClassPredicate(c.getValue());
			                      atom = TERM_FACTORY.getFunction(predicate, subject);
			                  } else {
			                       atom = ATOM_FACTORY.getTripleAtom(subject, pred, object);
			                  }
			             }
			             else if (object instanceof  Variable){
			                  Term uriOfPred = TERM_FACTORY.getUriTemplate(pred);
			                  Term uriOfObject = TERM_FACTORY.getUriTemplate(object);
			                  atom = ATOM_FACTORY.getTripleAtom(subject, uriOfPred,  uriOfObject);
			              }
			             else {
			                  throw new IllegalArgumentException("parser cannot handle object " + object);
			              }
			        } else if( ! QueryUtils.isGrounded(pred) ){
			             atom = ATOM_FACTORY.getTripleAtom(subject, pred,  object);
			        } else {
	                			             //Predicate predicate = TERM_FACTORY.getPredicate(pred.toString(), 2); // the data type cannot be determined here!
	                			             Predicate predicate;
	                			             if(pred instanceof Function) {
	                							 ValueConstant pr = (ValueConstant) ((Function) pred).getTerm(0);
	                							 if (object instanceof Variable) {
	                								 predicate = TERM_FACTORY.getPredicate(pr.getValue(), 2);
	                							 } else {
	                								 if (object instanceof Function) {
	                									 if (((Function) object).getFunctionSymbol() instanceof URITemplatePredicate) {

	                										 predicate = TERM_FACTORY.getObjectPropertyPredicate(pr.getValue());
	                									 } else {
	                										 predicate = TERM_FACTORY.getDataPropertyPredicate(pr.getValue());
	                									 }
	                								 }
	                									 else {
	                										 throw new IllegalArgumentException("parser cannot handle object " + object);
	                									 }
	                							 }
	                						 }else {
	                			                  throw new IllegalArgumentException("predicate should be a URI Function");
	                			             }
	                			             atom = TERM_FACTORY.getFunction(predicate, subject, object);
	                			       }
	                			       return atom;
		  }


	private static boolean isRDFType(Term pred) {
	//		if (pred instanceof Constant && ((Constant) pred).getValue().equals(IriConstants.RDF_TYPE)) {
	//			return true;
	//		}
			if (pred instanceof Function && ((Function) pred).getTerm(0) instanceof Constant) {
				String c= ((Constant) ((Function) pred).getTerm(0)).getValue();
				return c.equals(RDF_TYPE);
			}	
			return false;
		}




	// $ANTLR start "parse"
	// TurtleOBDA.g:366:1: parse returns [List<Function> value] : ( directiveStatement )* t1= triplesStatement (t2= triplesStatement )* EOF ;
	public final List<Function> parse() throws RecognitionException {
		List<Function> value = null;


		List<Function> t1 =null;
		List<Function> t2 =null;

		try {
			// TurtleOBDA.g:367:3: ( ( directiveStatement )* t1= triplesStatement (t2= triplesStatement )* EOF )
			// TurtleOBDA.g:367:5: ( directiveStatement )* t1= triplesStatement (t2= triplesStatement )* EOF
			{
			// TurtleOBDA.g:367:5: ( directiveStatement )*
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==AT) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// TurtleOBDA.g:367:5: directiveStatement
					{
					pushFollow(FOLLOW_directiveStatement_in_parse58);
					directiveStatement();
					state._fsp--;

					}
					break;

				default :
					break loop1;
				}
			}

			pushFollow(FOLLOW_triplesStatement_in_parse67);
			t1=triplesStatement();
			state._fsp--;


			      value =  t1;
			    
			// TurtleOBDA.g:371:7: (t2= triplesStatement )*
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( (LA2_0==PREFIXED_NAME||(LA2_0 >= STRING_WITH_BRACKET && LA2_0 <= STRING_WITH_CURLY_BRACKET)) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// TurtleOBDA.g:371:8: t2= triplesStatement
					{
					pushFollow(FOLLOW_triplesStatement_in_parse80);
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

			match(input,EOF,FOLLOW_EOF_in_parse87); 
			}

		}
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
	// TurtleOBDA.g:381:1: directiveStatement : directive PERIOD ;
	public final void directiveStatement() throws RecognitionException {
		try {
			// TurtleOBDA.g:382:3: ( directive PERIOD )
			// TurtleOBDA.g:382:5: directive PERIOD
			{
			pushFollow(FOLLOW_directive_in_directiveStatement100);
			directive();
			state._fsp--;

			match(input,PERIOD,FOLLOW_PERIOD_in_directiveStatement102); 
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
	// TurtleOBDA.g:385:1: triplesStatement returns [List<Function> value] : triples ( WS )* PERIOD ;
	public final List<Function> triplesStatement() throws RecognitionException {
		List<Function> value = null;


		List<Function> triples1 =null;

		try {
			// TurtleOBDA.g:386:3: ( triples ( WS )* PERIOD )
			// TurtleOBDA.g:386:5: triples ( WS )* PERIOD
			{
			pushFollow(FOLLOW_triples_in_triplesStatement119);
			triples1=triples();
			state._fsp--;

			// TurtleOBDA.g:386:13: ( WS )*
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( (LA3_0==WS) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// TurtleOBDA.g:386:13: WS
					{
					match(input,WS,FOLLOW_WS_in_triplesStatement121); 
					}
					break;

				default :
					break loop3;
				}
			}

			match(input,PERIOD,FOLLOW_PERIOD_in_triplesStatement124); 
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
	// TurtleOBDA.g:389:1: directive : ( base | prefixID );
	public final void directive() throws RecognitionException {
		try {
			// TurtleOBDA.g:390:3: ( base | prefixID )
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
					// TurtleOBDA.g:390:5: base
					{
					pushFollow(FOLLOW_base_in_directive139);
					base();
					state._fsp--;

					}
					break;
				case 2 :
					// TurtleOBDA.g:391:5: prefixID
					{
					pushFollow(FOLLOW_prefixID_in_directive145);
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
	// TurtleOBDA.g:394:1: base : AT BASE uriref ;
	public final void base() throws RecognitionException {
		try {
			// TurtleOBDA.g:395:3: ( AT BASE uriref )
			// TurtleOBDA.g:395:5: AT BASE uriref
			{
			match(input,AT,FOLLOW_AT_in_base158); 
			match(input,BASE,FOLLOW_BASE_in_base160); 
			pushFollow(FOLLOW_uriref_in_base162);
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
	// TurtleOBDA.g:398:1: prefixID : AT PREFIX ( namespace | defaultNamespace ) uriref ;
	public final void prefixID() throws RecognitionException {
		ParserRuleReturnScope namespace2 =null;
		ParserRuleReturnScope defaultNamespace3 =null;
		String uriref4 =null;


		  String prefix = "";

		try {
			// TurtleOBDA.g:402:3: ( AT PREFIX ( namespace | defaultNamespace ) uriref )
			// TurtleOBDA.g:402:5: AT PREFIX ( namespace | defaultNamespace ) uriref
			{
			match(input,AT,FOLLOW_AT_in_prefixID180); 
			match(input,PREFIX,FOLLOW_PREFIX_in_prefixID182); 
			// TurtleOBDA.g:402:15: ( namespace | defaultNamespace )
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
					// TurtleOBDA.g:402:16: namespace
					{
					pushFollow(FOLLOW_namespace_in_prefixID185);
					namespace2=namespace();
					state._fsp--;

					 prefix = (namespace2!=null?input.toString(namespace2.start,namespace2.stop):null); 
					}
					break;
				case 2 :
					// TurtleOBDA.g:402:58: defaultNamespace
					{
					pushFollow(FOLLOW_defaultNamespace_in_prefixID191);
					defaultNamespace3=defaultNamespace();
					state._fsp--;

					 prefix = (defaultNamespace3!=null?input.toString(defaultNamespace3.start,defaultNamespace3.stop):null); 
					}
					break;

			}

			pushFollow(FOLLOW_uriref_in_prefixID196);
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
	// TurtleOBDA.g:408:1: triples returns [List<Function> value] : subject predicateObjectList ;
	public final List<Function> triples() throws RecognitionException {
		List<Function> value = null;


		Term subject5 =null;
		List<Function> predicateObjectList6 =null;

		try {
			// TurtleOBDA.g:409:3: ( subject predicateObjectList )
			// TurtleOBDA.g:409:5: subject predicateObjectList
			{
			pushFollow(FOLLOW_subject_in_triples215);
			subject5=subject();
			state._fsp--;

			 currentSubject = subject5; 
			pushFollow(FOLLOW_predicateObjectList_in_triples219);
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
	// TurtleOBDA.g:414:1: predicateObjectList returns [List<Function> value] : v1= verb l1= objectList ( SEMI v2= verb l2= objectList )* ;
	public final List<Function> predicateObjectList() throws RecognitionException {
		List<Function> value = null;


		Term v1 =null;
		List<Term> l1 =null;
		Term v2 =null;
		List<Term> l2 =null;


		   value = new LinkedList<Function>();

		try {
			// TurtleOBDA.g:418:3: (v1= verb l1= objectList ( SEMI v2= verb l2= objectList )* )
			// TurtleOBDA.g:418:5: v1= verb l1= objectList ( SEMI v2= verb l2= objectList )*
			{
			pushFollow(FOLLOW_verb_in_predicateObjectList245);
			v1=verb();
			state._fsp--;

			pushFollow(FOLLOW_objectList_in_predicateObjectList251);
			l1=objectList();
			state._fsp--;


			      for (Term object : l1) {
			        Function atom = makeAtom(currentSubject, v1, object);
			        value.add(atom);
			      }
			    
			// TurtleOBDA.g:424:5: ( SEMI v2= verb l2= objectList )*
			loop6:
			while (true) {
				int alt6=2;
				int LA6_0 = input.LA(1);
				if ( (LA6_0==SEMI) ) {
					alt6=1;
				}

				switch (alt6) {
				case 1 :
					// TurtleOBDA.g:424:6: SEMI v2= verb l2= objectList
					{
					match(input,SEMI,FOLLOW_SEMI_in_predicateObjectList260); 
					pushFollow(FOLLOW_verb_in_predicateObjectList264);
					v2=verb();
					state._fsp--;

					pushFollow(FOLLOW_objectList_in_predicateObjectList268);
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
	// TurtleOBDA.g:433:1: verb returns [Term value] : ( predicate | 'a' );
	public final Term verb() throws RecognitionException {
		Term value = null;


		Term predicate7 =null;

		try {
			// TurtleOBDA.g:434:3: ( predicate | 'a' )
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
					// TurtleOBDA.g:434:5: predicate
					{
					pushFollow(FOLLOW_predicate_in_verb292);
					predicate7=predicate();
					state._fsp--;

					 value = predicate7; 
					}
					break;
				case 2 :
					// TurtleOBDA.g:435:5: 'a'
					{
					match(input,77,FOLLOW_77_in_verb300); 

					  Term constant = TERM_FACTORY.getConstantLiteral(RDF_TYPE);
					  value = TERM_FACTORY.getUriTemplate(constant);
					  
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
	// TurtleOBDA.g:441:1: objectList returns [List<Term> value] : o1= object ( COMMA o2= object )* ;
	public final List<Term> objectList() throws RecognitionException {
		List<Term> value = null;


		Term o1 =null;
		Term o2 =null;


		  value = new ArrayList<Term>();

		try {
			// TurtleOBDA.g:445:3: (o1= object ( COMMA o2= object )* )
			// TurtleOBDA.g:445:5: o1= object ( COMMA o2= object )*
			{
			pushFollow(FOLLOW_object_in_objectList326);
			o1=object();
			state._fsp--;

			 value.add(o1); 
			// TurtleOBDA.g:445:42: ( COMMA o2= object )*
			loop8:
			while (true) {
				int alt8=2;
				int LA8_0 = input.LA(1);
				if ( (LA8_0==COMMA) ) {
					alt8=1;
				}

				switch (alt8) {
				case 1 :
					// TurtleOBDA.g:445:43: COMMA o2= object
					{
					match(input,COMMA,FOLLOW_COMMA_in_objectList331); 
					pushFollow(FOLLOW_object_in_objectList335);
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
	// TurtleOBDA.g:448:1: subject returns [Term value] : ( resource | variable );
	public final Term subject() throws RecognitionException {
		Term value = null;


		Term resource8 =null;
		Variable variable9 =null;

		try {
			// TurtleOBDA.g:449:3: ( resource | variable )
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
					// TurtleOBDA.g:449:5: resource
					{
					pushFollow(FOLLOW_resource_in_subject357);
					resource8=resource();
					state._fsp--;

					 value = resource8; 
					}
					break;
				case 2 :
					// TurtleOBDA.g:450:5: variable
					{
					pushFollow(FOLLOW_variable_in_subject365);
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
	// TurtleOBDA.g:455:1: predicate returns [Term value] : resource ;
	public final Term predicate() throws RecognitionException {
		Term value = null;


		Term resource10 =null;

		try {
			// TurtleOBDA.g:456:3: ( resource )
			// TurtleOBDA.g:456:5: resource
			{
			pushFollow(FOLLOW_resource_in_predicate386);
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
	// TurtleOBDA.g:468:1: object returns [Term value] : ( resource | literal | typedLiteral | variable );
	public final Term object() throws RecognitionException {
		Term value = null;


		Term resource11 =null;
		Term literal12 =null;
		Function typedLiteral13 =null;
		Variable variable14 =null;

		try {
			// TurtleOBDA.g:469:3: ( resource | literal | typedLiteral | variable )
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
					// TurtleOBDA.g:469:5: resource
					{
					pushFollow(FOLLOW_resource_in_object405);
					resource11=resource();
					state._fsp--;

					 value = resource11; 
					}
					break;
				case 2 :
					// TurtleOBDA.g:470:5: literal
					{
					pushFollow(FOLLOW_literal_in_object413);
					literal12=literal();
					state._fsp--;

					 value = literal12; 
					}
					break;
				case 3 :
					// TurtleOBDA.g:471:5: typedLiteral
					{
					pushFollow(FOLLOW_typedLiteral_in_object422);
					typedLiteral13=typedLiteral();
					state._fsp--;

					 value = typedLiteral13; 
					}
					break;
				case 4 :
					// TurtleOBDA.g:472:5: variable
					{
					pushFollow(FOLLOW_variable_in_object430);
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
	// TurtleOBDA.g:476:1: resource returns [Term value] : ( uriref | qname );
	public final Term resource() throws RecognitionException {
		Term value = null;


		String uriref15 =null;
		String qname16 =null;

		try {
			// TurtleOBDA.g:477:4: ( uriref | qname )
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
					// TurtleOBDA.g:477:6: uriref
					{
					pushFollow(FOLLOW_uriref_in_resource451);
					uriref15=uriref();
					state._fsp--;

					 value = construct(uriref15); 
					}
					break;
				case 2 :
					// TurtleOBDA.g:478:6: qname
					{
					pushFollow(FOLLOW_qname_in_resource460);
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
	// TurtleOBDA.g:483:1: uriref returns [String value] : STRING_WITH_BRACKET ;
	public final String uriref() throws RecognitionException {
		String value = null;


		Token STRING_WITH_BRACKET17=null;

		try {
			// TurtleOBDA.g:484:3: ( STRING_WITH_BRACKET )
			// TurtleOBDA.g:484:5: STRING_WITH_BRACKET
			{
			STRING_WITH_BRACKET17=(Token)match(input,STRING_WITH_BRACKET,FOLLOW_STRING_WITH_BRACKET_in_uriref485); 
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
	// TurtleOBDA.g:487:1: qname returns [String value] : PREFIXED_NAME ;
	public final String qname() throws RecognitionException {
		String value = null;


		Token PREFIXED_NAME18=null;

		try {
			// TurtleOBDA.g:488:3: ( PREFIXED_NAME )
			// TurtleOBDA.g:488:5: PREFIXED_NAME
			{
			PREFIXED_NAME18=(Token)match(input,PREFIXED_NAME,FOLLOW_PREFIXED_NAME_in_qname504); 

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
	// TurtleOBDA.g:495:1: blank : ( nodeID | BLANK );
	public final void blank() throws RecognitionException {
		try {
			// TurtleOBDA.g:496:3: ( nodeID | BLANK )
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
					// TurtleOBDA.g:496:5: nodeID
					{
					pushFollow(FOLLOW_nodeID_in_blank519);
					nodeID();
					state._fsp--;

					}
					break;
				case 2 :
					// TurtleOBDA.g:497:5: BLANK
					{
					match(input,BLANK,FOLLOW_BLANK_in_blank525); 
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
	// TurtleOBDA.g:500:1: variable returns [Variable value] : STRING_WITH_CURLY_BRACKET ;
	public final Variable variable() throws RecognitionException {
		Variable value = null;


		Token STRING_WITH_CURLY_BRACKET19=null;

		try {
			// TurtleOBDA.g:501:3: ( STRING_WITH_CURLY_BRACKET )
			// TurtleOBDA.g:501:5: STRING_WITH_CURLY_BRACKET
			{
			STRING_WITH_CURLY_BRACKET19=(Token)match(input,STRING_WITH_CURLY_BRACKET,FOLLOW_STRING_WITH_CURLY_BRACKET_in_variable542); 

			      value = TERM_FACTORY.getVariable(removeBrackets((STRING_WITH_CURLY_BRACKET19!=null?STRING_WITH_CURLY_BRACKET19.getText():null)));
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
	// TurtleOBDA.g:507:1: function returns [Function value] : resource LPAREN terms RPAREN ;
	public final Function function() throws RecognitionException {
		Function value = null;


		Term resource20 =null;
		Vector<Term> terms21 =null;

		try {
			// TurtleOBDA.g:508:3: ( resource LPAREN terms RPAREN )
			// TurtleOBDA.g:508:5: resource LPAREN terms RPAREN
			{
			pushFollow(FOLLOW_resource_in_function563);
			resource20=resource();
			state._fsp--;

			match(input,LPAREN,FOLLOW_LPAREN_in_function565); 
			pushFollow(FOLLOW_terms_in_function567);
			terms21=terms();
			state._fsp--;

			match(input,RPAREN,FOLLOW_RPAREN_in_function569); 

			      String functionName = resource20.toString();
			      int arity = terms21.size();
			      Predicate functionSymbol = TERM_FACTORY.getPredicate(functionName, arity);
			      value = TERM_FACTORY.getFunction(functionSymbol, terms21);
			    
			}

		}
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
	// TurtleOBDA.g:516:1: typedLiteral returns [Function value] : ( variable AT language | variable REFERENCE resource );
	public final Function typedLiteral() throws RecognitionException {
		Function value = null;


		Variable variable22 =null;
		Term language23 =null;
		Variable variable24 =null;
		Term resource25 =null;

		try {
			// TurtleOBDA.g:517:3: ( variable AT language | variable REFERENCE resource )
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
					// TurtleOBDA.g:517:5: variable AT language
					{
					pushFollow(FOLLOW_variable_in_typedLiteral588);
					variable22=variable();
					state._fsp--;

					match(input,AT,FOLLOW_AT_in_typedLiteral590); 
					pushFollow(FOLLOW_language_in_typedLiteral592);
					language23=language();
					state._fsp--;


					      Variable var = variable22;
					      Term lang = language23;   
					      value = TERM_FACTORY.getTypedTerm(var, lang);

					    
					}
					break;
				case 2 :
					// TurtleOBDA.g:523:5: variable REFERENCE resource
					{
					pushFollow(FOLLOW_variable_in_typedLiteral600);
					variable24=variable();
					state._fsp--;

					match(input,REFERENCE,FOLLOW_REFERENCE_in_typedLiteral602); 
					pushFollow(FOLLOW_resource_in_typedLiteral604);
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
					    Optional<COL_TYPE> type = TYPE_FACTORY.getDatatype(functionName);
					    if (!type.isPresent())
					 	  throw new RuntimeException("ERROR. A mapping involves an unsupported datatype. \nOffending datatype:" + functionName);
					    
					      value = TERM_FACTORY.getTypedTerm(var, type.get());

						
					     
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
	// TurtleOBDA.g:543:1: language returns [Term value] : ( languageTag | variable );
	public final Term language() throws RecognitionException {
		Term value = null;


		ParserRuleReturnScope languageTag26 =null;
		Variable variable27 =null;

		try {
			// TurtleOBDA.g:544:3: ( languageTag | variable )
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
					// TurtleOBDA.g:544:5: languageTag
					{
					pushFollow(FOLLOW_languageTag_in_language623);
					languageTag26=languageTag();
					state._fsp--;


					    	value = TERM_FACTORY.getConstantLiteral((languageTag26!=null?input.toString(languageTag26.start,languageTag26.stop):null).toLowerCase(), COL_TYPE.STRING);
					    
					}
					break;
				case 2 :
					// TurtleOBDA.g:547:5: variable
					{
					pushFollow(FOLLOW_variable_in_language631);
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
	// TurtleOBDA.g:552:1: terms returns [Vector<Term> value] : t1= term ( COMMA t2= term )* ;
	public final Vector<Term> terms() throws RecognitionException {
		Vector<Term> value = null;


		Term t1 =null;
		Term t2 =null;


		  value = new Vector<Term>();

		try {
			// TurtleOBDA.g:556:3: (t1= term ( COMMA t2= term )* )
			// TurtleOBDA.g:556:5: t1= term ( COMMA t2= term )*
			{
			pushFollow(FOLLOW_term_in_terms657);
			t1=term();
			state._fsp--;

			 value.add(t1); 
			// TurtleOBDA.g:556:40: ( COMMA t2= term )*
			loop15:
			while (true) {
				int alt15=2;
				int LA15_0 = input.LA(1);
				if ( (LA15_0==COMMA) ) {
					alt15=1;
				}

				switch (alt15) {
				case 1 :
					// TurtleOBDA.g:556:41: COMMA t2= term
					{
					match(input,COMMA,FOLLOW_COMMA_in_terms662); 
					pushFollow(FOLLOW_term_in_terms666);
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
	// TurtleOBDA.g:559:1: term returns [Term value] : ( function | variable | literal );
	public final Term term() throws RecognitionException {
		Term value = null;


		Function function28 =null;
		Variable variable29 =null;
		Term literal30 =null;

		try {
			// TurtleOBDA.g:560:3: ( function | variable | literal )
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
					// TurtleOBDA.g:560:5: function
					{
					pushFollow(FOLLOW_function_in_term687);
					function28=function();
					state._fsp--;

					 value = function28; 
					}
					break;
				case 2 :
					// TurtleOBDA.g:561:5: variable
					{
					pushFollow(FOLLOW_variable_in_term695);
					variable29=variable();
					state._fsp--;

					 value = variable29; 
					}
					break;
				case 3 :
					// TurtleOBDA.g:562:5: literal
					{
					pushFollow(FOLLOW_literal_in_term703);
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
	// TurtleOBDA.g:565:1: literal returns [Term value] : ( stringLiteral ( AT language )? | dataTypeString | numericLiteral | booleanLiteral );
	public final Term literal() throws RecognitionException {
		Term value = null;


		Term language31 =null;
		Term stringLiteral32 =null;
		Term dataTypeString33 =null;
		Term numericLiteral34 =null;
		Term booleanLiteral35 =null;

		try {
			// TurtleOBDA.g:566:3: ( stringLiteral ( AT language )? | dataTypeString | numericLiteral | booleanLiteral )
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
					// TurtleOBDA.g:566:5: stringLiteral ( AT language )?
					{
					pushFollow(FOLLOW_stringLiteral_in_literal722);
					stringLiteral32=stringLiteral();
					state._fsp--;

					// TurtleOBDA.g:566:19: ( AT language )?
					int alt17=2;
					int LA17_0 = input.LA(1);
					if ( (LA17_0==AT) ) {
						alt17=1;
					}
					switch (alt17) {
						case 1 :
							// TurtleOBDA.g:566:20: AT language
							{
							match(input,AT,FOLLOW_AT_in_literal725); 
							pushFollow(FOLLOW_language_in_literal727);
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
					             value = TERM_FACTORY.getTypedTerm(f,lang);
					          }else{
					             value = TERM_FACTORY.getTypedTerm(f, COL_TYPE.STRING);
					          }       
					       }else{

					       //if variable we cannot assign a datatype yet
					       if (literal instanceof Variable)
					       {
					            value = TERM_FACTORY.getTypedTerm(literal, COL_TYPE.STRING);
					       }
					       else{
					          ValueConstant constant = (ValueConstant)stringLiteral32;
					          if (lang != null) {
						     value = TERM_FACTORY.getTypedTerm(constant, lang);
					          } else {
					      	     value = TERM_FACTORY.getTypedTerm(constant, COL_TYPE.STRING);
					          }
					       }
					       }
					    
					}
					break;
				case 2 :
					// TurtleOBDA.g:593:5: dataTypeString
					{
					pushFollow(FOLLOW_dataTypeString_in_literal737);
					dataTypeString33=dataTypeString();
					state._fsp--;

					 value = dataTypeString33; 
					}
					break;
				case 3 :
					// TurtleOBDA.g:594:5: numericLiteral
					{
					pushFollow(FOLLOW_numericLiteral_in_literal745);
					numericLiteral34=numericLiteral();
					state._fsp--;

					 value = numericLiteral34; 
					}
					break;
				case 4 :
					// TurtleOBDA.g:595:5: booleanLiteral
					{
					pushFollow(FOLLOW_booleanLiteral_in_literal753);
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
	// TurtleOBDA.g:598:1: stringLiteral returns [Term value] : STRING_WITH_QUOTE_DOUBLE ;
	public final Term stringLiteral() throws RecognitionException {
		Term value = null;


		Token STRING_WITH_QUOTE_DOUBLE36=null;

		try {
			// TurtleOBDA.g:599:3: ( STRING_WITH_QUOTE_DOUBLE )
			// TurtleOBDA.g:599:5: STRING_WITH_QUOTE_DOUBLE
			{
			STRING_WITH_QUOTE_DOUBLE36=(Token)match(input,STRING_WITH_QUOTE_DOUBLE,FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_stringLiteral772); 

			      String str = (STRING_WITH_QUOTE_DOUBLE36!=null?STRING_WITH_QUOTE_DOUBLE36.getText():null);
			      if (str.contains("{")){
			      	value = getNestedConcat(str);
			      }else{
			      	value = TERM_FACTORY.getConstantLiteral(str.substring(1, str.length()-1), COL_TYPE.STRING); // without the double quotes
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
	// TurtleOBDA.g:609:1: dataTypeString returns [Term value] : stringLiteral REFERENCE resource ;
	public final Term dataTypeString() throws RecognitionException {
		Term value = null;


		Term stringLiteral37 =null;
		Term resource38 =null;

		try {
			// TurtleOBDA.g:610:3: ( stringLiteral REFERENCE resource )
			// TurtleOBDA.g:610:6: stringLiteral REFERENCE resource
			{
			pushFollow(FOLLOW_stringLiteral_in_dataTypeString792);
			stringLiteral37=stringLiteral();
			state._fsp--;

			match(input,REFERENCE,FOLLOW_REFERENCE_in_dataTypeString794); 
			pushFollow(FOLLOW_resource_in_dataTypeString796);
			resource38=resource();
			state._fsp--;


			      Term stringValue = stringLiteral37;

			          if (resource38 instanceof Function){
			          	    String functionName = ( (ValueConstant) ((Function)resource38).getTerm(0) ).getValue();

			                    Optional<COL_TYPE> type = TYPE_FACTORY.getDatatype(functionName);
			                    if (!type.isPresent()) {
			                      throw new RuntimeException("Unsupported datatype: " + functionName);
			                    }
			                    value = TERM_FACTORY.getTypedTerm(stringValue, type.get());
			                    }
			           else {
			          value = TERM_FACTORY.getTypedTerm(stringValue, COL_TYPE.STRING);
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
	// TurtleOBDA.g:628:1: numericLiteral returns [Term value] : ( numericUnsigned | numericPositive | numericNegative );
	public final Term numericLiteral() throws RecognitionException {
		Term value = null;


		Term numericUnsigned39 =null;
		Term numericPositive40 =null;
		Term numericNegative41 =null;

		try {
			// TurtleOBDA.g:629:3: ( numericUnsigned | numericPositive | numericNegative )
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
					// TurtleOBDA.g:629:5: numericUnsigned
					{
					pushFollow(FOLLOW_numericUnsigned_in_numericLiteral812);
					numericUnsigned39=numericUnsigned();
					state._fsp--;

					 value = numericUnsigned39; 
					}
					break;
				case 2 :
					// TurtleOBDA.g:630:5: numericPositive
					{
					pushFollow(FOLLOW_numericPositive_in_numericLiteral820);
					numericPositive40=numericPositive();
					state._fsp--;

					 value = numericPositive40; 
					}
					break;
				case 3 :
					// TurtleOBDA.g:631:5: numericNegative
					{
					pushFollow(FOLLOW_numericNegative_in_numericLiteral828);
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
	// TurtleOBDA.g:634:1: nodeID : BLANK_PREFIX name ;
	public final void nodeID() throws RecognitionException {
		try {
			// TurtleOBDA.g:635:3: ( BLANK_PREFIX name )
			// TurtleOBDA.g:635:5: BLANK_PREFIX name
			{
			match(input,BLANK_PREFIX,FOLLOW_BLANK_PREFIX_in_nodeID843); 
			pushFollow(FOLLOW_name_in_nodeID845);
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
	// TurtleOBDA.g:638:1: relativeURI : STRING_URI ;
	public final void relativeURI() throws RecognitionException {
		try {
			// TurtleOBDA.g:639:3: ( STRING_URI )
			// TurtleOBDA.g:639:5: STRING_URI
			{
			match(input,STRING_URI,FOLLOW_STRING_URI_in_relativeURI859); 
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
	// TurtleOBDA.g:642:1: namespace : NAMESPACE ;
	public final TurtleOBDAParser.namespace_return namespace() throws RecognitionException {
		TurtleOBDAParser.namespace_return retval = new TurtleOBDAParser.namespace_return();
		retval.start = input.LT(1);

		try {
			// TurtleOBDA.g:643:3: ( NAMESPACE )
			// TurtleOBDA.g:643:5: NAMESPACE
			{
			match(input,NAMESPACE,FOLLOW_NAMESPACE_in_namespace872); 
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
	// TurtleOBDA.g:646:1: defaultNamespace : COLON ;
	public final TurtleOBDAParser.defaultNamespace_return defaultNamespace() throws RecognitionException {
		TurtleOBDAParser.defaultNamespace_return retval = new TurtleOBDAParser.defaultNamespace_return();
		retval.start = input.LT(1);

		try {
			// TurtleOBDA.g:647:3: ( COLON )
			// TurtleOBDA.g:647:5: COLON
			{
			match(input,COLON,FOLLOW_COLON_in_defaultNamespace887); 
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
	// TurtleOBDA.g:650:1: name : VARNAME ;
	public final void name() throws RecognitionException {
		try {
			// TurtleOBDA.g:651:3: ( VARNAME )
			// TurtleOBDA.g:651:5: VARNAME
			{
			match(input,VARNAME,FOLLOW_VARNAME_in_name900); 
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
	// TurtleOBDA.g:654:1: languageTag : VARNAME ;
	public final TurtleOBDAParser.languageTag_return languageTag() throws RecognitionException {
		TurtleOBDAParser.languageTag_return retval = new TurtleOBDAParser.languageTag_return();
		retval.start = input.LT(1);

		try {
			// TurtleOBDA.g:655:3: ( VARNAME )
			// TurtleOBDA.g:655:5: VARNAME
			{
			match(input,VARNAME,FOLLOW_VARNAME_in_languageTag913); 
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
	// TurtleOBDA.g:658:1: booleanLiteral returns [Term value] : ( TRUE | FALSE );
	public final Term booleanLiteral() throws RecognitionException {
		Term value = null;


		Token TRUE42=null;
		Token FALSE43=null;

		try {
			// TurtleOBDA.g:659:3: ( TRUE | FALSE )
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
					// TurtleOBDA.g:659:5: TRUE
					{
					TRUE42=(Token)match(input,TRUE,FOLLOW_TRUE_in_booleanLiteral930); 

					  ValueConstant trueConstant = TERM_FACTORY.getConstantLiteral((TRUE42!=null?TRUE42.getText():null), COL_TYPE.LITERAL);
					  value = TERM_FACTORY.getTypedTerm(trueConstant, COL_TYPE.BOOLEAN);
					}
					break;
				case 2 :
					// TurtleOBDA.g:662:5: FALSE
					{
					FALSE43=(Token)match(input,FALSE,FOLLOW_FALSE_in_booleanLiteral939); 

					  ValueConstant falseConstant = TERM_FACTORY.getConstantLiteral((FALSE43!=null?FALSE43.getText():null), COL_TYPE.LITERAL);
					  value = TERM_FACTORY.getTypedTerm(falseConstant, COL_TYPE.BOOLEAN);
					  
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
	// TurtleOBDA.g:668:1: numericUnsigned returns [Term value] : ( INTEGER | DOUBLE | DECIMAL );
	public final Term numericUnsigned() throws RecognitionException {
		Term value = null;


		Token INTEGER44=null;
		Token DOUBLE45=null;
		Token DECIMAL46=null;

		try {
			// TurtleOBDA.g:669:3: ( INTEGER | DOUBLE | DECIMAL )
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
					// TurtleOBDA.g:669:5: INTEGER
					{
					INTEGER44=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_numericUnsigned958); 

					  ValueConstant integerConstant = TERM_FACTORY.getConstantLiteral((INTEGER44!=null?INTEGER44.getText():null), COL_TYPE.LITERAL);
					  value = TERM_FACTORY.getTypedTerm(integerConstant, COL_TYPE.INTEGER);
					  
					}
					break;
				case 2 :
					// TurtleOBDA.g:673:5: DOUBLE
					{
					DOUBLE45=(Token)match(input,DOUBLE,FOLLOW_DOUBLE_in_numericUnsigned966); 

					  ValueConstant doubleConstant = TERM_FACTORY.getConstantLiteral((DOUBLE45!=null?DOUBLE45.getText():null), COL_TYPE.LITERAL);
					  value = TERM_FACTORY.getTypedTerm(doubleConstant, COL_TYPE.DOUBLE);
					  
					}
					break;
				case 3 :
					// TurtleOBDA.g:677:5: DECIMAL
					{
					DECIMAL46=(Token)match(input,DECIMAL,FOLLOW_DECIMAL_in_numericUnsigned975); 

					  ValueConstant decimalConstant = TERM_FACTORY.getConstantLiteral((DECIMAL46!=null?DECIMAL46.getText():null), COL_TYPE.LITERAL);
					  value = TERM_FACTORY.getTypedTerm(decimalConstant, COL_TYPE.DECIMAL);
					   
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
	// TurtleOBDA.g:683:1: numericPositive returns [Term value] : ( INTEGER_POSITIVE | DOUBLE_POSITIVE | DECIMAL_POSITIVE );
	public final Term numericPositive() throws RecognitionException {
		Term value = null;


		Token INTEGER_POSITIVE47=null;
		Token DOUBLE_POSITIVE48=null;
		Token DECIMAL_POSITIVE49=null;

		try {
			// TurtleOBDA.g:684:3: ( INTEGER_POSITIVE | DOUBLE_POSITIVE | DECIMAL_POSITIVE )
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
					// TurtleOBDA.g:684:5: INTEGER_POSITIVE
					{
					INTEGER_POSITIVE47=(Token)match(input,INTEGER_POSITIVE,FOLLOW_INTEGER_POSITIVE_in_numericPositive994); 

					   ValueConstant integerConstant = TERM_FACTORY.getConstantLiteral((INTEGER_POSITIVE47!=null?INTEGER_POSITIVE47.getText():null), COL_TYPE.LITERAL);
					   value = TERM_FACTORY.getTypedTerm(integerConstant, COL_TYPE.INTEGER);
					  
					}
					break;
				case 2 :
					// TurtleOBDA.g:688:5: DOUBLE_POSITIVE
					{
					DOUBLE_POSITIVE48=(Token)match(input,DOUBLE_POSITIVE,FOLLOW_DOUBLE_POSITIVE_in_numericPositive1002); 

					  ValueConstant doubleConstant = TERM_FACTORY.getConstantLiteral((DOUBLE_POSITIVE48!=null?DOUBLE_POSITIVE48.getText():null), COL_TYPE.LITERAL);
					  value = TERM_FACTORY.getTypedTerm(doubleConstant, COL_TYPE.DOUBLE);
					  
					}
					break;
				case 3 :
					// TurtleOBDA.g:692:5: DECIMAL_POSITIVE
					{
					DECIMAL_POSITIVE49=(Token)match(input,DECIMAL_POSITIVE,FOLLOW_DECIMAL_POSITIVE_in_numericPositive1011); 

					  ValueConstant decimalConstant = TERM_FACTORY.getConstantLiteral((DECIMAL_POSITIVE49!=null?DECIMAL_POSITIVE49.getText():null), COL_TYPE.LITERAL);
					  value = TERM_FACTORY.getTypedTerm(decimalConstant, COL_TYPE.DECIMAL);
					   
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
	// TurtleOBDA.g:698:1: numericNegative returns [Term value] : ( INTEGER_NEGATIVE | DOUBLE_NEGATIVE | DECIMAL_NEGATIVE );
	public final Term numericNegative() throws RecognitionException {
		Term value = null;


		Token INTEGER_NEGATIVE50=null;
		Token DOUBLE_NEGATIVE51=null;
		Token DECIMAL_NEGATIVE52=null;

		try {
			// TurtleOBDA.g:699:3: ( INTEGER_NEGATIVE | DOUBLE_NEGATIVE | DECIMAL_NEGATIVE )
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
					// TurtleOBDA.g:699:5: INTEGER_NEGATIVE
					{
					INTEGER_NEGATIVE50=(Token)match(input,INTEGER_NEGATIVE,FOLLOW_INTEGER_NEGATIVE_in_numericNegative1030); 

					  ValueConstant integerConstant = TERM_FACTORY.getConstantLiteral((INTEGER_NEGATIVE50!=null?INTEGER_NEGATIVE50.getText():null), COL_TYPE.LITERAL);
					  value = TERM_FACTORY.getTypedTerm(integerConstant, COL_TYPE.INTEGER);
					  
					}
					break;
				case 2 :
					// TurtleOBDA.g:703:5: DOUBLE_NEGATIVE
					{
					DOUBLE_NEGATIVE51=(Token)match(input,DOUBLE_NEGATIVE,FOLLOW_DOUBLE_NEGATIVE_in_numericNegative1038); 

					   ValueConstant doubleConstant = TERM_FACTORY.getConstantLiteral((DOUBLE_NEGATIVE51!=null?DOUBLE_NEGATIVE51.getText():null), COL_TYPE.LITERAL);
					   value = TERM_FACTORY.getTypedTerm(doubleConstant, COL_TYPE.DOUBLE);
					  
					}
					break;
				case 3 :
					// TurtleOBDA.g:707:5: DECIMAL_NEGATIVE
					{
					DECIMAL_NEGATIVE52=(Token)match(input,DECIMAL_NEGATIVE,FOLLOW_DECIMAL_NEGATIVE_in_numericNegative1047); 

					  ValueConstant decimalConstant = TERM_FACTORY.getConstantLiteral((DECIMAL_NEGATIVE52!=null?DECIMAL_NEGATIVE52.getText():null), COL_TYPE.LITERAL);
					  value = TERM_FACTORY.getTypedTerm(decimalConstant, COL_TYPE.DECIMAL);
					  
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



	public static final BitSet FOLLOW_directiveStatement_in_parse58 = new BitSet(new long[]{0x0040000000000200L,0x0000000000000018L});
	public static final BitSet FOLLOW_triplesStatement_in_parse67 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000018L});
	public static final BitSet FOLLOW_triplesStatement_in_parse80 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000018L});
	public static final BitSet FOLLOW_EOF_in_parse87 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_directive_in_directiveStatement100 = new BitSet(new long[]{0x0008000000000000L});
	public static final BitSet FOLLOW_PERIOD_in_directiveStatement102 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_triples_in_triplesStatement119 = new BitSet(new long[]{0x0008000000000000L,0x0000000000001000L});
	public static final BitSet FOLLOW_WS_in_triplesStatement121 = new BitSet(new long[]{0x0008000000000000L,0x0000000000001000L});
	public static final BitSet FOLLOW_PERIOD_in_triplesStatement124 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_base_in_directive139 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_prefixID_in_directive145 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_AT_in_base158 = new BitSet(new long[]{0x0000000000000800L});
	public static final BitSet FOLLOW_BASE_in_base160 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_uriref_in_base162 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_AT_in_prefixID180 = new BitSet(new long[]{0x0020000000000000L});
	public static final BitSet FOLLOW_PREFIX_in_prefixID182 = new BitSet(new long[]{0x0000200000010000L});
	public static final BitSet FOLLOW_namespace_in_prefixID185 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_defaultNamespace_in_prefixID191 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_uriref_in_prefixID196 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_subject_in_triples215 = new BitSet(new long[]{0x0040000000000000L,0x0000000000002008L});
	public static final BitSet FOLLOW_predicateObjectList_in_triples219 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_verb_in_predicateObjectList245 = new BitSet(new long[]{0x00400070439C0000L,0x0000000000000158L});
	public static final BitSet FOLLOW_objectList_in_predicateObjectList251 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000001L});
	public static final BitSet FOLLOW_SEMI_in_predicateObjectList260 = new BitSet(new long[]{0x0040000000000000L,0x0000000000002008L});
	public static final BitSet FOLLOW_verb_in_predicateObjectList264 = new BitSet(new long[]{0x00400070439C0000L,0x0000000000000158L});
	public static final BitSet FOLLOW_objectList_in_predicateObjectList268 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000001L});
	public static final BitSet FOLLOW_predicate_in_verb292 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_77_in_verb300 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_object_in_objectList326 = new BitSet(new long[]{0x0000000000020002L});
	public static final BitSet FOLLOW_COMMA_in_objectList331 = new BitSet(new long[]{0x00400070439C0000L,0x0000000000000158L});
	public static final BitSet FOLLOW_object_in_objectList335 = new BitSet(new long[]{0x0000000000020002L});
	public static final BitSet FOLLOW_resource_in_subject357 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variable_in_subject365 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_resource_in_predicate386 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_resource_in_object405 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_literal_in_object413 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_typedLiteral_in_object422 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variable_in_object430 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_uriref_in_resource451 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_qname_in_resource460 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_WITH_BRACKET_in_uriref485 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PREFIXED_NAME_in_qname504 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_nodeID_in_blank519 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BLANK_in_blank525 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_WITH_CURLY_BRACKET_in_variable542 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_resource_in_function563 = new BitSet(new long[]{0x0000020000000000L});
	public static final BitSet FOLLOW_LPAREN_in_function565 = new BitSet(new long[]{0x00400070439C0000L,0x0000000000000158L});
	public static final BitSet FOLLOW_terms_in_function567 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_RPAREN_in_function569 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variable_in_typedLiteral588 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_AT_in_typedLiteral590 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000810L});
	public static final BitSet FOLLOW_language_in_typedLiteral592 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variable_in_typedLiteral600 = new BitSet(new long[]{0x0800000000000000L});
	public static final BitSet FOLLOW_REFERENCE_in_typedLiteral602 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_resource_in_typedLiteral604 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_languageTag_in_language623 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variable_in_language631 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_term_in_terms657 = new BitSet(new long[]{0x0000000000020002L});
	public static final BitSet FOLLOW_COMMA_in_terms662 = new BitSet(new long[]{0x00400070439C0000L,0x0000000000000158L});
	public static final BitSet FOLLOW_term_in_terms666 = new BitSet(new long[]{0x0000000000020002L});
	public static final BitSet FOLLOW_function_in_term687 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variable_in_term695 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_literal_in_term703 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_stringLiteral_in_literal722 = new BitSet(new long[]{0x0000000000000202L});
	public static final BitSet FOLLOW_AT_in_literal725 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000810L});
	public static final BitSet FOLLOW_language_in_literal727 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dataTypeString_in_literal737 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_numericLiteral_in_literal745 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_booleanLiteral_in_literal753 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_stringLiteral772 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_stringLiteral_in_dataTypeString792 = new BitSet(new long[]{0x0800000000000000L});
	public static final BitSet FOLLOW_REFERENCE_in_dataTypeString794 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_resource_in_dataTypeString796 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_numericUnsigned_in_numericLiteral812 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_numericPositive_in_numericLiteral820 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_numericNegative_in_numericLiteral828 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BLANK_PREFIX_in_nodeID843 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
	public static final BitSet FOLLOW_name_in_nodeID845 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_URI_in_relativeURI859 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NAMESPACE_in_namespace872 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_COLON_in_defaultNamespace887 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VARNAME_in_name900 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VARNAME_in_languageTag913 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TRUE_in_booleanLiteral930 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FALSE_in_booleanLiteral939 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGER_in_numericUnsigned958 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOUBLE_in_numericUnsigned966 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DECIMAL_in_numericUnsigned975 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGER_POSITIVE_in_numericPositive994 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOUBLE_POSITIVE_in_numericPositive1002 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DECIMAL_POSITIVE_in_numericPositive1011 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGER_NEGATIVE_in_numericNegative1030 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOUBLE_NEGATIVE_in_numericNegative1038 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DECIMAL_NEGATIVE_in_numericNegative1047 = new BitSet(new long[]{0x0000000000000002L});
}
