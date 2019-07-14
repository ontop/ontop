package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;

public interface OntopOptimizationSettings extends OntopModelSettings {

    default int getMaxNbChildrenForLiftingDBFunctionSymbol() {
        String value = getProperty(MAX_NB_CHILDREN_LIFTING_DB_FS)
                .orElseThrow(() -> new InvalidOntopConfigurationException(
                        "Missing value for " + MAX_NB_CHILDREN_LIFTING_DB_FS));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new InvalidOntopConfigurationException(
                    MAX_NB_CHILDREN_LIFTING_DB_FS + " was expecting a number");
        }
    }

    //-------
    // Keys
    //-------

    String MAX_NB_CHILDREN_LIFTING_DB_FS = "ontop.maxNbChildrenLiftingDBFS";
}
