package it.unibz.inf.ontop.model.type;

/**
 * IRIs and blank nodes
 */
public interface ObjectRDFType extends RDFTermType, NonLiteralRDFStarTermType {

    boolean isBlankNode();
}
