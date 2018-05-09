package it.unibz.inf.ontop.spec.mapping.serializer;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.parser.impl.OntopNativeMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import it.unibz.inf.ontop.temporal.mapping.OntopNativeTemporalMappingParser;
import it.unibz.inf.ontop.temporal.mapping.TemporalMappingInterval;
import it.unibz.inf.ontop.temporal.mapping.impl.SQLPPTemporalTriplesMapImpl;

import java.io.BufferedWriter;
import java.io.IOException;

public class OntopNativeTemporalMappingSerializer extends OntopNativeMappingSerializer{
    /**
     * TODO: may consider building it through Assisted Injection.
     *
     * @param ppMapping
     */
    public OntopNativeTemporalMappingSerializer(SQLPPMapping ppMapping) {
        super(ppMapping);
    }

    @Override
    protected void writeMappingDeclaration(BufferedWriter writer) throws IOException {

        writer.write(OntopNativeMappingParser.MAPPING_DECLARATION_TAG + " " + OntopNativeMappingParser.START_COLLECTION_SYMBOL);
        writer.write("\n");

        boolean needLineBreak = false;
        for (SQLPPTriplesMap axiom : this.ppMapping.getTripleMaps()) {

            SQLPPTemporalTriplesMapImpl temporalAxiom = (SQLPPTemporalTriplesMapImpl)axiom;

            if (needLineBreak) {
                writer.write("\n");
            }
            writer.write(OntopNativeMappingParser.Label.mappingId.name() + "\t" + axiom.getId() + "\n");

            ImmutableList<ImmutableFunctionalTerm> targetQuery = temporalAxiom.getTargetAtoms();
            writer.write(OntopNativeMappingParser.Label.target.name() + "\t\t" + printTargetQuery(targetQuery) + "\n");

            TemporalMappingInterval temporalMappingInterval = temporalAxiom.getTemporalMappingInterval();
            writer.write(OntopNativeTemporalMappingParser.Label.interval.name() + "\t" + printIntervalQuery(temporalMappingInterval) + "\n");

            OBDASQLQuery sourceQuery = temporalAxiom.getSourceQuery();
            writer.write(OntopNativeMappingParser.Label.source.name() + "\t\t" + printSourceQuery(sourceQuery) + "\n");
            needLineBreak = true;
        }
        writer.write(OntopNativeMappingParser.END_COLLECTION_SYMBOL);
        writer.write("\n\n");
    }

    protected String printIntervalQuery(TemporalMappingInterval query) {
        String sourceString = query.toString();
        String toReturn = convertTabToSpaces(sourceString);
        return toReturn.replaceAll("\n", "\n\t\t\t");
    }
}
