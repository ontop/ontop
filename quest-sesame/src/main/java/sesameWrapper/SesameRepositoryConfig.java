package sesameWrapper;
/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */


import java.io.File;

import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryImplConfigBase;
import org.openrdf.repository.config.RepositoryRegistry;
import org.openrdf.repository.config.RepositoryConfig;

import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORYIMPL;
import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORYTYPE;



public class SesameRepositoryConfig extends RepositoryImplConfigBase {

	public static final String NAMESPACE = "http://inf.unibz.it/krdb/obda/quest#";

    /** <tt>http://inf.unibz.it/krdb/obda/quest#quest_type</tt> */
    public final static URI QUEST_TYPE;

    /** <tt>http://inf.unibz.it/krdb/obda/quest#name</tt> */
    public final static URI NAME;

    /** <tt>http://inf.unibz.it/krdb/obda/quest#owlfile/tt> */
    public final static URI OWLFILE;

    /** <tt>http://inf.unibz.it/krdb/obda/quest#obdafile</tt> */
    public final static URI OBDAFILE;
    
    public final static URI EXISTENTIAL;
    
    public final static URI REWRITING;
    
    static {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        QUEST_TYPE = factory.createURI(NAMESPACE, "quest_type");
        NAME = factory.createURI(NAMESPACE, "repo_name");
        OWLFILE = factory.createURI(NAMESPACE, "owlfile");
        OBDAFILE = factory.createURI(NAMESPACE, "obdafile");
        EXISTENTIAL = factory.createURI(NAMESPACE, "existential");
        REWRITING = factory.createURI(NAMESPACE, "rewriting");
    }
    
    
	private String quest_type;
    private String name;
    private String owlfile;
    private String obdafile;
    private boolean existential;
    private String rewriting;

    /**
     * Create a new RepositoryConfigImpl.
     */
    public SesameRepositoryConfig() {
    	super(SesameRepositoryFactory.REPOSITORY_TYPE);
    }

  
    
    public String getQuestType() {
        return quest_type;
    }

    public void setQuestType(String quest_type) {
        this.quest_type = quest_type;
    }
    
    public String getName()
    {
    	return name;
    }
    
    public void setName(String name)
    {
    	this.name = name;
    }
    
    public String getOwlFile()
    {
    	return owlfile;
    }

    public void setOwlFile(String file)
    {
    	this.owlfile = file;
    }
    
    public String getObdaFile()
    {
    	return obdafile;
    }
    
    public void setObdaFile(String file)
    {
    	this.obdafile = file;
    }

    public boolean getExistential()
    {
    	return existential;
    }
    
    public void setExistential(boolean ex)
    {
    	this.existential = ex;
    }
    
    public String getRewriting()
    {
    	return this.rewriting;
    }
    
    public void setRewriting(String rew)
    {
    	this.rewriting = rew;
    }

    @Override
    public void validate()
        throws RepositoryConfigException
    {
        if (quest_type == null || quest_type.isEmpty()) {
            throw new RepositoryConfigException("No type specified for repository implementation");
        }
        if (owlfile == null || owlfile.isEmpty()){
        	throw new RepositoryConfigException("No Owl file specified for repository creation!");
        }
        if (quest_type.contentEquals("quest-virtual"))
        		if (obdafile ==null || obdafile.contentEquals("obdafile") ){
        	throw new RepositoryConfigException("No OBDA file specified for repository creation!");
        }
    }

    @Override
    public Resource export(Graph graph) {
    	Resource implNode = super.export(graph);
    	
        ValueFactory vf = graph.getValueFactory();

        if (quest_type != null) {
            graph.add(implNode, QUEST_TYPE, vf.createLiteral(quest_type));
        }
        if (name != null) {
            graph.add(implNode, NAME, vf.createLiteral(name));
        }
        if (owlfile != null) {
            graph.add(implNode, OWLFILE, vf.createLiteral(owlfile));
        }
        if (obdafile != null) {
            graph.add(implNode, OBDAFILE, vf.createLiteral(obdafile));
        }
        if (existential == false || existential == true) {
        	graph.add(implNode, EXISTENTIAL, vf.createLiteral(existential));
        }
        if (rewriting != null) {
            graph.add(implNode, REWRITING, vf.createLiteral(rewriting));
        }
      
        return implNode;
    }

    @Override
    public void parse(Graph graph, Resource implNode)
            throws RepositoryConfigException
    {
    	super.parse(graph, implNode);
            try {
                Literal typeLit = GraphUtil.getOptionalObjectLiteral(graph, implNode, REPOSITORYTYPE);
                if (typeLit != null) {
                    setType(typeLit.getLabel());
                }
                Literal qtypeLit = GraphUtil.getOptionalObjectLiteral(graph, implNode, QUEST_TYPE);
                if (qtypeLit != null) {
                    setQuestType(qtypeLit.getLabel());
                }
                Literal name = GraphUtil.getOptionalObjectLiteral(graph, implNode, NAME);
                if (name != null) {
                    setName(name.getLabel());
                }
                Literal owlfile = GraphUtil.getOptionalObjectLiteral(graph, implNode, OWLFILE);
                if (owlfile != null) {
                    setOwlFile(owlfile.getLabel());
                }
                Literal obdafile = GraphUtil.getOptionalObjectLiteral(graph, implNode, OBDAFILE);
                if (obdafile != null) {
                    setObdaFile(obdafile.getLabel());
                }
                Literal existl = GraphUtil.getOptionalObjectLiteral(graph, implNode, EXISTENTIAL);
                if (existl != null) {
                    setExistential(existl.booleanValue());
                }
                Literal rewr = GraphUtil.getOptionalObjectLiteral(graph, implNode, REWRITING);
                if (rewr != null) {
                    setRewriting(rewr.getLabel());
                }
                
            }
            catch (GraphUtilException e) {
                throw new RepositoryConfigException(e.getMessage(), e);
            }
    }

}
