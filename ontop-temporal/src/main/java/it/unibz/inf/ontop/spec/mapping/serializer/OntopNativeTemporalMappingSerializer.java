package it.unibz.inf.ontop.spec.mapping.serializer;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.spec.TemporalTargetQueryRenderer;
import it.unibz.inf.ontop.spec.mapping.parser.impl.OntopNativeMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import it.unibz.inf.ontop.temporal.mapping.OntopNativeTemporalMappingParser;
import it.unibz.inf.ontop.temporal.mapping.TemporalMappingInterval;
import it.unibz.inf.ontop.temporal.mapping.impl.SQLPPTemporalTriplesMapImpl;

import java.io.BufferedWriter;
import java.io.IOException;

public class OntopNativeTemporalMappingSerializer extends OntopNativeMappingSerializer {
    /**
     * TODO: may consider building it through Assisted Injection.
     */
    public OntopNativeTemporalMappingSerializer(SQLPPMapping ppMapping) {
        super(ppMapping);
    }

    @Override
    protected void writeMappingDeclaration(BufferedWriter writer) throws IOException {

        writer.write(String.format("%s %s\n", OntopNativeMappingParser.MAPPING_DECLARATION_TAG, OntopNativeMappingParser.START_COLLECTION_SYMBOL));

        boolean needLineBreak = false;
        for (SQLPPTriplesMap axiom : this.ppMapping.getTripleMaps()) {

            SQLPPTemporalTriplesMapImpl temporalAxiom = (SQLPPTemporalTriplesMapImpl) axiom;

            if (needLineBreak) {
                writer.write("\n");
            }
            writer.write(String.format("%s\t%s\n",
                    OntopNativeMappingParser.Label.mappingId.name(),
                    axiom.getId())
            );

            writer.write(String.format("%s\t\t%s\n",
                    OntopNativeMappingParser.Label.target.name(),
                    printTargetQuery(temporalAxiom.getTargetAtoms()))
            );

            writer.write(String.format("%s\t%s\n",
                    OntopNativeTemporalMappingParser.Label.interval.name(),
                    printIntervalQuery(temporalAxiom.getTemporalMappingInterval()))
            );

            writer.write(String.format("%s\t\t%s\n",
                    OntopNativeMappingParser.Label.source.name(),
                    printSourceQuery(temporalAxiom.getSourceQuery()))
            );

            needLineBreak = true;
        }
        writer.write(String.format("%s\n\n",
                OntopNativeMappingParser.END_COLLECTION_SYMBOL)
        );
    }

    @Override
    protected String printTargetQuery(ImmutableList<ImmutableFunctionalTerm> query) {
        return TemporalTargetQueryRenderer.encode(query, ppMapping.getMetadata().getPrefixManager());
    }

    private String printIntervalQuery(TemporalMappingInterval query) {
        String toReturn = convertTabToSpaces(query.toString());
        return toReturn.replaceAll("\n", "\n\t\t\t");
    }
}
