package it.unibz.inf.ontop.mapping.extraction;

/**
 * To be specialized per concrete mapping language (e.g. R2RML, Ontop Native format and so on)
 */
public interface PPTriplesMapProvenance {

    /**
     * To be displayed in exceptions
     */
    String getProvenanceInfo();
}
