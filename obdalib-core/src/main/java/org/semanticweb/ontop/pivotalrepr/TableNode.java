package org.semanticweb.ontop.pivotalrepr;

/**
 * SQL-specific extensional data node
 */
public interface TableNode extends ExtensionalDataNode {

    @Override
    TableNode clone();
}
