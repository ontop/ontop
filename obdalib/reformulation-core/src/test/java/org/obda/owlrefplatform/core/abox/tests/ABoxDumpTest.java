package org.obda.owlrefplatform.core.abox.tests;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.model.rdbms.impl.RDBMSsourceParameterConstants;
import inf.unibz.it.sql.JDBCConnectionManager;

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.obda.owlrefplatform.core.abox.ABoxToDBDumper;
import org.obda.owlrefplatform.core.abox.AboxFromDBLoader;
import org.obda.owlrefplatform.core.abox.URIIdentyfier;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;


public class ABoxDumpTest extends TestCase {

	
	private String file = "src/test/resources/test/ontologies/aboxgeneration/ABoxDumpTest.owl";	
	private OWLOntologyManager manager = null;
    private OWLOntology ontology = null;
	
    
    public void test_1() throws Exception{
		
		 manager = OWLManager.createOWLOntologyManager();
	     ontology = manager.loadOntologyFromPhysicalURI((new File(file)).toURI());

	     String driver = "org.h2.Driver";
         String url = "jdbc:h2:mem:aboxdump";
         String username = "sa";
         String password = "";

         DataSource source = new DataSource(URI.create("http://www.obda.org/ABOXDUMP"));
         source.setParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER, driver);
         source.setParameter(RDBMSsourceParameterConstants.DATABASE_PASSWORD, password);
         source.setParameter(RDBMSsourceParameterConstants.DATABASE_URL, url);
         source.setParameter(RDBMSsourceParameterConstants.DATABASE_USERNAME, username);
         source.setParameter(RDBMSsourceParameterConstants.IS_IN_MEMORY, "true");
         source.setParameter(RDBMSsourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
	     
 		 Connection conn = JDBCConnectionManager.getJDBCConnectionManager().getConnection(source);

         
	     ABoxToDBDumper dumper = new ABoxToDBDumper(source);
	     Set<OWLOntology> vex = new HashSet<OWLOntology>();
	     vex.add(ontology);
	     dumper.materialize(vex,true);
	     Map<URIIdentyfier, String> org_mapper = dumper.getMapper();
	     Collection<String> tables = dumper.getMapper().values();
	     
	     conn = JDBCConnectionManager.getJDBCConnectionManager().getConnection(source);
	     
	     Statement st = conn.createStatement();
	     
	     Iterator<String> it = tables.iterator();
	     while(it.hasNext()){
		     ResultSet set = st.executeQuery("Select * from " + it.next());
		     assertEquals(true, set.next());
	     }
	    
	     AboxFromDBLoader loader = new AboxFromDBLoader();
	     HashMap<URIIdentyfier, String> mapper = loader.getMapper(source);
	     Iterator<URIIdentyfier> id_it = mapper.keySet().iterator();
	     while(id_it.hasNext()){
	    	 URIIdentyfier id = id_it.next();
	    	 assertEquals(org_mapper.get(id), mapper.get(id));
	     }
	     
	}
}
