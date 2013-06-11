package sesameWrapper;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;
import info.aduna.iteration.Iteration;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBStatement;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.Update;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;

public class RepositoryConnection implements org.openrdf.repository.RepositoryConnection {

	private SesameAbstractRepo repository;
	private QuestDBConnection questConn;
    private boolean isOpen;
    private boolean autoCommit;
    private  RDFParser rdfParser;
    private QuestDBStatement questStm;

	
	public RepositoryConnection(SesameAbstractRepo rep, QuestDBConnection connection) throws OBDAException
	{
		this.repository = rep;
		this.questConn = connection;
		this.isOpen = true;
		this.autoCommit = connection.getAutoCommit();
		
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
			throw new RepositoryException(e);
		} finally{
        autoCommit();
		}
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
        	 throw new RepositoryException(e);
		} finally {
             setAutoCommit(autoCommit);
             autoCommit();
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
        	  throw new RepositoryException(e);
		} finally {
              setAutoCommit(autoCommit);
              autoCommit();
          }

	}


	public void add(File file, String baseURI, RDFFormat dataFormat, Resource... contexts)
			throws IOException, RDFParseException, RepositoryException {
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
		// Adds the RDF data that can be found at the specified URL to the
		// repository,
		// optionally to one or more named contexts.
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

        rdfParser = Rio.createParser(dataFormat,
                getRepository().getValueFactory());

        rdfParser.setVerifyData(true);
        rdfParser.setStopAtFirstError(true);
        rdfParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);
        

        boolean autoCommit = isAutoCommit();
        setAutoCommit(false);
	
        
        SesameRDFIterator rdfHandler = new SesameRDFIterator();
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
            
           // System.out.println("Parsing... ");
                    
            questStm = questConn.createStatement();
    		
            Thread insert = new Thread(new Insert(rdfParser, (InputStream)inputStreamOrReader, baseURI));
            Thread process = new Thread(new Process(rdfHandler, questStm));
            
           
          
            //start threads
            insert.start();
            process.start();
            
            insert.join();
            process.join();
            
            questStm.close();
                     
     
        } catch (RuntimeException e) {
        	//System.out.println("exception, rolling back!");
        	
        	
            if (autoCommit) {
                rollback();
            }
            throw new RepositoryException(e);
        } catch (OBDAException e)
        {
        	
        	 if (autoCommit) {
                 rollback();
             }
        	 throw new RepositoryException(e);
        } catch (InterruptedException e) {
			 if (autoCommit) {
	                rollback();
	            }
			 
			 throw new RepositoryException(e);
		} finally {
            setAutoCommit(autoCommit);
            autoCommit();
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
				} catch (Exception e) {
throw new RuntimeException(e);
				}
        	  }
        	  
          }
          
          private class Process implements Runnable{
        	  private SesameRDFIterator iterator;
        	  private QuestDBStatement questStmt;
        	  public Process(SesameRDFIterator iterator, QuestDBStatement qstm) throws OBDAException
        	  {
        		  this.iterator = iterator;
        		  this.questStmt = qstm;
        	  }
        	  
        	  public void run()
        	  {
        		    try {
						questStmt.add(iterator, boolToInt(autoCommit), 5000);
        		    	
					} catch (SQLException e) {
						throw new RuntimeException(e);
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
 
    	SesameRDFIterator it = new SesameRDFIterator(stmIterator);
    	
    	//insert data   useFile=false, batch=0
    	try {	
    		questStm = questConn.createStatement();
			questStm.add(it);
		} catch (SQLException e) {
			throw new RepositoryException(e);
		}
    	finally{
    		questStm.close();
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
    	
    	throw new RepositoryException("Removal not supported!");
    }



	public void clear(Resource... contexts) throws RepositoryException {
		//Removes all statements from a specific contexts in the repository. 
        remove(null, null, null, contexts);
	}

	public void clearNamespaces() throws RepositoryException {
		//Removes all namespace declarations from the repository. 
		remove(null, null, null,(Resource[]) null);
		
	}

	public void close() throws RepositoryException {
		//Closes the connection, freeing resources. 
		//If the connection is not in autoCommit mode, 
		//all non-committed operations will be lost. 
			try {
				questConn.close();
			} catch (Exception e) {
				throw new RepositoryException(e);
			}
	} 
	

	public void commit() throws RepositoryException {
		//Commits all updates that have been performed as part of this connection sofar. 
		try {
			//System.out.println("QuestConn commit..");
			questConn.commit();
		} catch (OBDAException e) {
			throw new RepositoryException(e);
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
		RepositoryResult<Statement> stms = getStatements(subj, pred, obj, includeInferred, contexts);

		handler.startRDF();
		// handle
		if (stms != null) {
			while (stms.hasNext()) {
				{
					Statement st = stms.next();
					if (st!=null)
						handler.handleStatement(st);
				}
			}
		}
		handler.endRDF();

	}

	public RepositoryResult<Resource> getContextIDs()
			throws RepositoryException {
		//Gets all resources that are used as content identifiers. 
		//Care should be taken that the returned RepositoryResult 
		//is closed to free any resources that it keeps hold of. 
		List<Resource> contexts = new LinkedList<Resource>();
		return new RepositoryResult<Resource>(new CloseableIteratorIteration<Resource, RepositoryException>(contexts.iterator()));
	}

	public String getNamespace(String prefix) throws RepositoryException {
		//Gets the namespace that is associated with the specified prefix, if any. 
		return repository.getNamespace(prefix);
	}

	public RepositoryResult<Namespace> getNamespaces()
			throws RepositoryException {
		//Gets all declared namespaces as a RepositoryResult of Namespace objects. 
		//Each Namespace object consists of a prefix and a namespace name. 
		Set<Namespace> namespSet = new HashSet<Namespace>();
		Map<String, String> namesp = repository.getNamespaces();
		Set<String> keys = namesp.keySet();
		for (String key : keys)
		{
			//convert into namespace objects
			namespSet.add(new NamespaceImpl(key, namesp.get(key)));
		}
		return new RepositoryResult<Namespace>(new CloseableIteratorIteration<Namespace, RepositoryException>(
                namespSet.iterator()));
	}

	public ParserConfig getParserConfig() {
		//Returns the parser configuration this connection uses for Rio-based operations. 
		return rdfParser.getParserConfig();
	}

	public Repository getRepository() {
		//Returns the Repository object to which this connection belongs. 
		return this.repository;
	}

	public RepositoryResult<Statement> getStatements(Resource subj, org.openrdf.model.URI pred,
			Value obj, boolean includeInferred, Resource... contexts)
			throws RepositoryException {
		//Gets all statements with a specific subject, 
		//predicate and/or object from the repository.
		//The result is optionally restricted to the specified set of named contexts. 
		
		//construct query for it
		String queryString = "CONSTRUCT {";
		String s="", p="", o="";
		if (subj == null)
			s = "?s ";
		else {		
			s = subj.toString();
			if (subj instanceof URI) {
				s = "<" + s + ">";
			}
		}
		
		if (pred == null)
			p = " ?p ";
		else 
			p = "<" + pred.stringValue()  + ">";
		if (obj == null)
			o = " ?o ";
		else {
			if (obj instanceof URI) {
				o = "<" + obj.stringValue() + ">";
			} else {
				o = obj.stringValue();
			}
		}
		queryString+= s+p+o+"} WHERE {"+s+p+o+"}";	
		
		//execute construct query
		try {
			List<Statement> list = new LinkedList<Statement>();
			
			if (contexts.length == 0 || (contexts.length > 0 && contexts[0] == null)) {
					GraphQuery query = prepareGraphQuery(QueryLanguage.SPARQL,
							queryString);
					GraphQueryResult result = query.evaluate();

					// System.out.println("result: "+result.hasNext());
					while (result.hasNext())
						list.add(result.next());
					// result.close();
			}
			CloseableIteration<Statement, RepositoryException> iter = new CloseableIteratorIteration<Statement, RepositoryException>(
					list.iterator());
			RepositoryResult<Statement> repoResult = new RepositoryResult<Statement>(iter);

			return repoResult;
		} catch (MalformedQueryException e) {
			throw new RepositoryException(e);

		} catch (QueryEvaluationException e) {
			throw new RepositoryException(e);

		}
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
			throw new MalformedQueryException("SPARQL query expected!");

		return new SesameBooleanQuery(queryString, baseURI, questConn);
		
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
		if (ql != QueryLanguage.SPARQL)
			throw new MalformedQueryException("SPARQL query expected!");

		return new SesameGraphQuery(queryString, baseURI, questConn);
			
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
		if (ql != QueryLanguage.SPARQL)
			throw new MalformedQueryException("SPARQL query expected! ");
		
		ParsedQuery q = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, queryString, baseURI);
		
		if (q instanceof ParsedTupleQuery)
			return prepareTupleQuery(ql,queryString, baseURI);
		else if (q instanceof ParsedBooleanQuery)
			return prepareBooleanQuery(ql, queryString, baseURI);
		else if (q instanceof ParsedGraphQuery)
			return prepareGraphQuery(ql, queryString, baseURI);
		else 
			throw new MalformedQueryException("Unrecognized query type. " + queryString);
		
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
			throw new MalformedQueryException("SPARQL query expected!");

			return new SesameTupleQuery(queryString, baseURI, questConn);

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

	public void removeNamespace(String key) throws RepositoryException {
		//Removes a namespace declaration by removing the association between a prefix and a namespace name. 
		repository.removeNamespace(key);
		
	}

	public void rollback() throws RepositoryException {
		//Rolls back all updates that have been performed as part of this connection sofar. 
		try {
			this.questConn.rollBack();
		} catch (OBDAException e) {
			throw new RepositoryException(e);

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
			throw new RepositoryException(e);

		}
         
         // if we are switching from non-autocommit to autocommit mode, commit any
         // pending updates
         if (autoCommit) {
             commit();
         }

		
	}

	public void setNamespace(String key, String value)
			throws RepositoryException {
		//Sets the prefix for a namespace. 
		repository.setNamespace(key, value);
		
	}

	public void setParserConfig(ParserConfig config) {
		//Set the parser configuration this connection should use for RDFParser-based operations. 
		rdfParser.setParserConfig(config);
		
	}

	public long size(Resource... contexts) throws RepositoryException {
		//Returns the number of (explicit) statements that are in the specified contexts in this repository. 
		return 0;
	}



	


}
