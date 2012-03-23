package sesameWrapper;
import info.aduna.iteration.Iteration;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Semaphore;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.*;
import org.openrdf.model.impl.*;
import org.openrdf.query.*;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.repository.*;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.*;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.*;
import it.unibz.krdb.obda.ontology.*;
import it.unibz.krdb.obda.ontology.impl.DataPropertyAssertionImpl;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.*;
import it.unibz.krdb.obda.owlrefplatform.core.abox.NTripleAssertionIterator;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.reformulation.tests.StockExchangeTest.TestQuery;

public class RepositoryConnection implements org.openrdf.repository.RepositoryConnection {

	private SesameAbstractRepo repository;
	private QuestDBConnection questConn;
    private boolean isOpen;
    private boolean autoCommit;

	
	public RepositoryConnection(SesameAbstractRepo rep) throws OBDAException
	{
		this.repository = rep;
		this.questConn = repository.getQuestConnection();
		this.isOpen = true;
		this.autoCommit = true;
	}

       
     

	
	public void add(Statement st, Resource... contexts)
			throws RepositoryException {
		//Adds the supplied statement to this repository, optionally to one or more named contexts. 
		OpenRDFUtil.verifyContextNotNull(contexts);
		 if (contexts != null && contexts.length == 0 && st.getContext() != null) {
	            contexts = new Resource[] { st.getContext() };
	        }
        try {
        	List<Statement> l = new ArrayList<Statement>();
    		l.add(st);
    		Iterator<Statement> iterator = l.iterator();
          
			addWithoutCommit(iterator, contexts);
			
			l=null;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
        autoCommit();

	}

	public void add(Iterable<? extends Statement> statements, Resource... contexts)
			throws RepositoryException {
		//Adds the supplied statements to this repository, optionally to one or more named contexts. 
		 OpenRDFUtil.verifyContextNotNull(contexts);

         boolean autoCommit = isAutoCommit();
         setAutoCommit(false);

         try {
                 addWithoutCommit((Iterator<Statement>) statements, contexts);
             
         } catch (RepositoryException e) {
             if (autoCommit) {
                 rollback();
             }
             throw e;
         } catch (RuntimeException e) {
             if (autoCommit) {
                 rollback();
             }
             throw e;
         } catch (Exception e) {
			e.printStackTrace();
		} finally {
             setAutoCommit(autoCommit);
         }

	}

	public <E extends Exception> void add(
			Iteration<? extends Statement, E> statementIter, Resource... contexts)
			throws RepositoryException, E {
		//Adds the supplied statements to this repository, optionally to one or more named contexts. 
		  OpenRDFUtil.verifyContextNotNull(contexts);

          boolean autoCommit = isAutoCommit();
          setAutoCommit(false);

          try {
                  addWithoutCommit((Iterator<Statement>) statementIter, contexts);
                  
          } catch (RepositoryException e) {
              if (autoCommit) {
                  rollback();
              }
              throw e;
          } catch (RuntimeException e) {
              if (autoCommit) {
                  rollback();
              }
              throw e;
          } catch (Exception e) {
			e.printStackTrace();
		} finally {
              setAutoCommit(autoCommit);
          }

	}






	public void add(File file, String baseURI, RDFFormat dataFormat, Resource... contexts)
			throws IOException, RDFParseException, RepositoryException {
		// TODO Auto-generated method stub
		//Adds RDF data from the specified file to a specific contexts in the repository. 
		
		
		   if (baseURI == null) {
               // default baseURI to file
               baseURI = file.toURI().toString();
           }

           InputStream in = new FileInputStream(file);

           try {
               add(in, baseURI, dataFormat, contexts);
           } finally {
               in.close();
           }

	}

	 public void add(URL url, String baseURI, RDFFormat dataFormat,
             Resource... contexts) throws IOException,
             RDFParseException, RepositoryException {
		//Adds the RDF data that can be found at the specified URL to the repository,
		//optionally to one or more named contexts. 
         if (baseURI == null) {
             baseURI = url.toExternalForm();
         }

         InputStream in = url.openStream();

         try {
             add(in, baseURI, dataFormat, contexts);
         } finally {
             in.close();
         }
     }

     public void add(InputStream in, String baseURI,
             RDFFormat dataFormat, Resource... contexts)
             throws IOException, RDFParseException, RepositoryException {
 		//Adds RDF data from an InputStream to the repository, optionally to one or more named contexts. 
         addInputStreamOrReader(in, baseURI, dataFormat, contexts);
     }

     public void add(Reader reader, String baseURI,
             RDFFormat dataFormat, Resource... contexts)
             throws IOException, RDFParseException, RepositoryException {
    	//Adds RDF data from a Reader to the repository, optionally to one or more 
 		//named contexts. Note: using a Reader to upload byte-based data means that 
 		//you have to be careful not to destroy the data's character encoding by 
 		//enforcing a default character encoding upon the bytes. \
 		//If possible, adding such data using an InputStream is to be preferred.
         addInputStreamOrReader(reader, baseURI, dataFormat, contexts);
     }

	public void add(Resource subject, org.openrdf.model.URI predicate, Value object, Resource... contexts)
			throws RepositoryException {
		//Adds a statement with the specified subject, predicate and object to this repository, 
		//optionally to one or more named contexts. 
		OpenRDFUtil.verifyContextNotNull(contexts);
		ValueFactory vf = new ValueFactoryImpl();
		
		Statement st = vf.createStatement(subject, vf.createURI(predicate.toString()), object);
		
		add(st, contexts);
		
        autoCommit();

	}
	
	 /**
     * Adds the data that can be read from the supplied InputStream or Reader to
     * this repository.
     * 
     * @param inputStreamOrReader
     *        An {@link InputStream} or {@link Reader} containing RDF data that
     *        must be added to the repository.
     * @param baseURI
     *        The base URI for the data.
     * @param dataFormat
     *        The file format of the data.
     * @param context
     *        The context to which the data should be added in case
     *        <tt>enforceContext</tt> is <tt>true</tt>. The value
     *        <tt>null</tt> indicates the null context.
     * @throws IOException
     * @throws UnsupportedRDFormatException
     * @throws RDFParseException
     * @throws RepositoryException
     */
    protected void addInputStreamOrReader(Object inputStreamOrReader,
            String baseURI, RDFFormat dataFormat, Resource... contexts)
            throws IOException, RDFParseException, RepositoryException {
    	
    	if ( repository.getType() == QuestConstants.VIRTUAL)
			throw new RepositoryException();
    	
        OpenRDFUtil.verifyContextNotNull(contexts);

        RDFParser rdfParser = Rio.createParser(dataFormat,
                getRepository().getValueFactory());

        rdfParser.setVerifyData(true);
        rdfParser.setStopAtFirstError(true);
        rdfParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

        boolean autoCommit = isAutoCommit();
        setAutoCommit(false);


    	Semaphore empty = new Semaphore(0);
		Semaphore full = new Semaphore(0);
		
        
        SesameRDFHandler rdfHandler = new SesameRDFHandler(empty, full);
        rdfParser.setRDFHandler(rdfHandler);
        
        
        try {

            
          
            
            if (inputStreamOrReader instanceof  InputStream) {
            	inputStreamOrReader = (InputStream) inputStreamOrReader;
            } 
            else if (inputStreamOrReader instanceof  Reader) {
            	inputStreamOrReader = (Reader) inputStreamOrReader;
            } 
            else {
                throw new IllegalArgumentException(
                        "inputStreamOrReader must be an InputStream or a Reader, is a: "
                                + inputStreamOrReader.getClass());
            }
            
            System.out.println("Parsing... ");
           
            
            Iterator<Assertion> iterator = rdfHandler.getAssertionIterator();   
            
            Thread insert = new Thread(new Insert(rdfParser, (InputStream)inputStreamOrReader, baseURI));
            Thread process = new Thread(new Process((SesameStatementIterator)iterator));
            
          
            //
            insert.start();
            process.start();
          //  process.join();
            insert.join();
            process.join();
                     
     
        } catch (RuntimeException e) {
        	System.out.println("exception, rolling back!");
        	e.printStackTrace();
        	
            if (autoCommit) {
                rollback();
            }
            throw e;
        } catch (OBDAException e)
        {
        	e.printStackTrace();
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            setAutoCommit(autoCommit);
        }
    }


            
          private class Insert implements Runnable{
        	  private RDFParser rdfParser;
        	  private InputStream inputStreamOrReader;
        	  private String baseURI;
        	  public Insert(RDFParser rdfParser, InputStream inputStreamOrReader, String baseURI)
        	  {
        		  this.rdfParser = rdfParser;
        		  this.inputStreamOrReader = inputStreamOrReader;
        		  this.baseURI = baseURI;
        	  }
        	  public void run()
        	  {
        		  try {
					rdfParser.parse((InputStream) inputStreamOrReader, baseURI);
				} catch (RDFParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (RDFHandlerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	  }
        	  
          }
          
          private class Process implements Runnable{
        	  private QuestDBStatement st;
        	  private SesameStatementIterator iterator;
        	  public Process(SesameStatementIterator iterator) throws OBDAException
        	  {
        		  st = questConn.createStatement();
        		  this.iterator = iterator;
        	  }
        	  
        	  public void run()
        	  {
        		    try {
						st.add(iterator, boolToInt(autoCommit), 5000);
        		    	
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        	  }
          }
            
        
             
               
      
  
    protected void addWithoutCommit(Iterator< Statement> stmIterator, Resource... contexts)
    throws RepositoryException, OBDAException, URISyntaxException {
    	
    	if ( repository.getType() == QuestConstants.VIRTUAL)
			throw new RepositoryException();
    	
    	
    	if (contexts.length == 0) {
    		contexts = new Resource[]{} ;
    	}
    	boolean currCommit = autoCommit;
    	autoCommit = false;
    	//create new quest statement
    	QuestDBStatement stm = questConn.createStatement();
 
    	SesameStatementIterator it = new SesameStatementIterator((ListIterator)stmIterator, null, null);
    	
    	//insert data   useFile=false, batch=0
    	try {
			stm.add(it);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		autoCommit = currCommit;
    }

    
   
    protected void autoCommit() throws RepositoryException {
        if (isAutoCommit()) {
            commit();
        }
    }
    
    
    private int boolToInt(boolean b)
    {
    	if(b) return 1;
    	return 0;
    }

    protected void removeWithoutCommit(Statement st,
    		Resource... contexts) throws RepositoryException {
    	if (contexts.length == 0 && st.getContext() != null) {
    		contexts = new Resource[] { st.getContext() };
    	}
    
    	removeWithoutCommit(st.getSubject(), st.getPredicate(), st.getObject(), contexts);
    }

    protected void removeWithoutCommit(Resource subject,
    		org.openrdf.model.URI predicate, Value object, Resource... contexts)
    	throws RepositoryException{
    	autoCommit = false;
    //	questConn.
    	autoCommit = true;
    
    }



	public void clear(Resource... contexts) throws RepositoryException {
		//Removes all statements from a specific contexts in the repository. 
        remove(null, null, null, contexts);
	}

	public void clearNamespaces() throws RepositoryException {
		// TODO Auto-generated method stub
		//Removes all namespace declarations from the repository. 
		
	}

	public void close() throws RepositoryException {
		// TODO Auto-generated method stub
		//Closes the connection, freeing resources. 
		//If the connection is not in autoCommit mode, 
		//all non-committed operations will be lost. 
		this.isOpen = false;
		try {
			questConn.close();
		} catch (OBDAException e) {
			e.printStackTrace();
		}
	}

	public void commit() throws RepositoryException {
		//Commits all updates that have been performed as part of this connection sofar. 
		try {
			questConn.commit();
		} catch (OBDAException e) {
			e.printStackTrace();
		}
	}

	public void export(RDFHandler handler, Resource... contexts)
			throws RepositoryException, RDFHandlerException {
		//Exports all explicit statements in the specified contexts to the supplied RDFHandler. 
        exportStatements(null, null, null, false, handler, contexts);
	}

	public void exportStatements(Resource subj,  org.openrdf.model.URI  pred, Value obj,
			boolean includeInferred, RDFHandler handler, Resource... contexts)
			throws RepositoryException, RDFHandlerException {
		//Exports all statements with a specific subject, predicate 
		//and/or object from the repository, optionally from the specified contexts. 
		getStatements(subj, pred, obj, includeInferred, contexts);
		
	}

	public RepositoryResult<Resource> getContextIDs()
			throws RepositoryException {
		//Gets all resources that are used as content identifiers. 
		//Care should be taken that the returned RepositoryResult 
		//is closed to free any resources that it keeps hold of. 
		return null;
	}

	public String getNamespace(String arg0) throws RepositoryException {
		// TODO Auto-generated method stub
		//Gets the namespace that is associated with the specified prefix, if any. 
		return null;
	}

	public RepositoryResult<Namespace> getNamespaces()
			throws RepositoryException {
		// TODO Auto-generated method stub
		//Gets all declared namespaces as a RepositoryResult of Namespace objects. 
		//Each Namespace object consists of a prefix and a namespace name. 
		return null;
	}

	public ParserConfig getParserConfig() {
		// TODO Auto-generated method stub
		//Returns the parser configuration this connection uses for Rio-based operations. 
		return null;
	}

	public Repository getRepository() {
		//Returns the Repository object to which this connection belongs. 
		return this.repository;
	}

	public RepositoryResult<Statement> getStatements(Resource subj, org.openrdf.model.URI pred,
			Value obj, boolean includeInferred, Resource... contexts)
			throws RepositoryException {
		// TODO Auto-generated method stub
		//Gets all statements with a specific subject, 
		//predicate and/or object from the repository.
		//The result is optionally restricted to the specified set of named contexts. 
		//construct query for it
		return null;
	}

	public ValueFactory getValueFactory() {
		//Gets a ValueFactory for this RepositoryConnection. 
		return new ValueFactoryImpl();
	}

	public boolean hasStatement(Statement st, boolean includeInferred, Resource... contexts)
			throws RepositoryException {
		//Checks whether the repository contains the specified statement,
		//optionally in the specified contexts. 
		return hasStatement(st.getSubject(), st.getPredicate(), st
                .getObject(), includeInferred, contexts);
	}

	public boolean hasStatement(Resource subj, org.openrdf.model.URI pred, Value obj,
			boolean includeInferred, Resource... contexts) throws RepositoryException {
		//Checks whether the repository contains statements with a specific subject, 
		//predicate and/or object, optionally in the specified contexts. 
		    RepositoryResult<Statement> stIter = getStatements(subj, pred,
                     obj, includeInferred, contexts);
             try {
                 return stIter.hasNext();
             } finally {
                 stIter.close();
             }
         }



	public boolean isAutoCommit() throws RepositoryException {
		//Checks whether the connection is in auto-commit mode. 
		return this.autoCommit;
	}

	public boolean isEmpty() throws RepositoryException {
		//Returns true if this repository does not contain any (explicit) statements. 
		return size() == 0;
	}

	public boolean isOpen() throws RepositoryException {
		//Checks whether this connection is open. 
		//A connection is open from the moment it is created until it is closed. 
		return this.isOpen;
	}

	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String query)
			throws RepositoryException, MalformedQueryException {
		//Prepares true/false queries. In case the query contains 
		//relative URIs that need to be resolved against an external base URI, 
		//one should use prepareBooleanQuery(QueryLanguage, String, String) instead. 
        return prepareBooleanQuery(ql, query, null);
    }

	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String queryString,
			String baseURI) throws RepositoryException, MalformedQueryException {
		//Prepares true/false queries. 
		if (ql != QueryLanguage.SPARQL)
			throw new MalformedQueryException();
		
		try {
			return new SesameBooleanQuery(queryString, baseURI, questConn.createStatement());
		} catch (OBDAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public GraphQuery prepareGraphQuery(QueryLanguage ql, String queryString)
			throws RepositoryException, MalformedQueryException {
		//Prepares queries that produce RDF graphs. In case the query 
		//contains relative URIs that need to be resolved against an 
		//external base URI, one should use prepareGraphQuery(QueryLanguage, String, String) instead. 
		return prepareGraphQuery(ql, queryString, null);
	}

	public GraphQuery prepareGraphQuery(QueryLanguage ql, String queryString,
			String baseURI) throws RepositoryException, MalformedQueryException {
		//Prepares queries that produce RDF graphs. 
		
		throw new MalformedQueryException("System does not support Graph Queries!");
		
	}

	public Query prepareQuery(QueryLanguage ql, String query)
			throws RepositoryException, MalformedQueryException {
		//Prepares a query for evaluation on this repository (optional operation).
		//In case the query contains relative URIs that need to be resolved against 
		//an external base URI, one should use prepareQuery(QueryLanguage, String, String) instead. 
        return prepareQuery(ql, query, null);
    }

	public Query prepareQuery(QueryLanguage ql, String queryString, String baseURI)
			throws RepositoryException, MalformedQueryException {
		// TODO Auto-generated method stub
		if (ql != QueryLanguage.SPARQL)
			throw new MalformedQueryException();
		
		
		if (queryString.startsWith("SELECT"))
			return prepareTupleQuery(ql,queryString, baseURI);
		else if (queryString.startsWith("ASK"))
			return prepareBooleanQuery(ql, queryString, baseURI);
		else if (queryString.startsWith("CONSTRUCT"))
			return prepareGraphQuery(ql, queryString, baseURI);
		else 
			throw new MalformedQueryException("Unrecognized query type!");
		
		//Prepares a query for evaluation on this repository (optional operation). 
		 //ParsedQuery parsedQuery = QueryParserUtil.parseQuery(ql, queryString, baseURI);
		 
	      /*  if (parsedQuery instanceof TupleQuery) {
	            return new SailTupleQuery((TupleQueryModel)parsedQuery, this);
	        }
	        else if (parsedQuery instanceof GraphQuery) {
	            return new SailGraphQuery((GraphQueryModel)parsedQuery, this);
	        }
	        else if (parsedQuery instanceof BooleanQuery) {
	            return new SailBooleanQuery((BooleanQueryModel)parsedQuery, this);
	        }
	        else {
	            throw new RuntimeException("Unexpected query type: " + parsedQuery.getClass());
	        }
	        */
	}

	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query)
			throws RepositoryException, MalformedQueryException {
		//Prepares a query that produces sets of value tuples. 
		//In case the query contains relative URIs that need to be 
		//resolved against an external base URI, one should use 
		//prepareTupleQuery(QueryLanguage, String, String) instead. 
        return this.prepareTupleQuery(ql, query, "");
    }

	public TupleQuery prepareTupleQuery(QueryLanguage ql, String queryString,
			String baseURI) throws RepositoryException, MalformedQueryException {
		//Prepares a query that produces sets of value tuples. 
		if (ql != QueryLanguage.SPARQL)
			throw new MalformedQueryException();
		
		try {
			return new SesameTupleQuery(queryString, baseURI, questConn.createStatement());
		} catch (OBDAException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Update prepareUpdate(QueryLanguage arg0, String arg1)
			throws RepositoryException, MalformedQueryException {
		// TODO Auto-generated method stub
		//Prepares an Update operation. 
		return null;
	}

	public Update prepareUpdate(QueryLanguage arg0, String arg1, String arg2)
			throws RepositoryException, MalformedQueryException {
		// TODO Auto-generated method stub
		//Prepares an Update operation. 
		return null;
	}

	public void remove(Statement st, Resource... contexts)
			throws RepositoryException {
		//Removes the supplied statement from the specified contexts in the repository. 
		   OpenRDFUtil.verifyContextNotNull(contexts);
           removeWithoutCommit(st, contexts);
           autoCommit();

	}

	public void remove(Iterable<? extends Statement> statements, Resource... contexts)
			throws RepositoryException {
		//Removes the supplied statements from the specified contexts in this repository. 
		 OpenRDFUtil.verifyContextNotNull(contexts);

         boolean autoCommit = isAutoCommit();
         setAutoCommit(false);

         try {
             for (Statement st : statements) {
                 remove(st, contexts);
             }
         } catch (RepositoryException e) {
             if (autoCommit) {
                 rollback();
             }
             throw e;
         } catch (RuntimeException e) {
             if (autoCommit) {
                 rollback();
             }
             throw e;
         } finally {
             setAutoCommit(autoCommit);
         }

	}

	public <E extends Exception> void remove(
			Iteration<? extends Statement, E> statementIter, Resource... contexts)
			throws RepositoryException, E {
		//Removes the supplied statements from a specific context in this repository, 
		//ignoring any context information carried by the statements themselves. 
		boolean autoCommit = isAutoCommit();
        setAutoCommit(false);

        try {
            while (statementIter.hasNext()) {
                remove(statementIter.next(), contexts);
            }
        } catch (RepositoryException e) {
            if (autoCommit) {
                rollback();
            }
            throw e;
        } catch (RuntimeException e) {
            if (autoCommit) {
                rollback();
            }
            throw e;
        } finally {
            setAutoCommit(autoCommit);
        }

	}

	public void remove(Resource subject, org.openrdf.model.URI predicate, Value object, Resource... contexts)
			throws RepositoryException {
		//Removes the statement(s) with the specified subject, predicate and object 
		//from the repository, optionally restricted to the specified contexts. 
		  OpenRDFUtil.verifyContextNotNull(contexts);
          removeWithoutCommit(subject, predicate, object, contexts);
          autoCommit();

	}

	public void removeNamespace(String arg0) throws RepositoryException {
		//Removes a namespace declaration by removing the association between a prefix and a namespace name. 
		
	}

	public void rollback() throws RepositoryException {
		//Rolls back all updates that have been performed as part of this connection sofar. 
		try {
			this.questConn.rollBack();
		} catch (OBDAException e) {
			e.printStackTrace();
		}
	}

	public void setAutoCommit(boolean autoCommit) throws RepositoryException {
		//Enables or disables auto-commit mode for the connection. 
		//If a connection is in auto-commit mode, then all updates 
		//will be executed and committed as individual transactions. 
		//Otherwise, the updates are grouped into transactions that are 
		//terminated by a call to either commit() or rollback(). 
		//By default, new connections are in auto-commit mode. 
		 if (autoCommit == this.autoCommit) {
             return;
         }

         this.autoCommit = autoCommit;
         try {
			this.questConn.setAutoCommit(autoCommit);
		} catch (OBDAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
         // if we are switching from non-autocommit to autocommit mode, commit any
         // pending updates
         if (autoCommit) {
             commit();
         }

		
	}

	public void setNamespace(String arg0, String arg1)
			throws RepositoryException {
		// TODO Auto-generated method stub
		//Sets the prefix for a namespace. 
	
		
	}

	public void setParserConfig(ParserConfig arg0) {
		// TODO Auto-generated method stub
		//Set the parser configuration this connection should use for RDFParser-based operations. 
		
	}

	public long size(Resource... contexts) throws RepositoryException {
		//Returns the number of (explicit) statements that are in the specified contexts in this repository. 
		return 0;
	}



	


}
