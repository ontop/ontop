package org.semanticweb.ontop.owlrefplatform.injection;

import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.owlrefplatform.core.Quest;
import org.semanticweb.ontop.sql.DBMetadata;

import javax.annotation.Nullable;
import java.util.Properties;

public interface QuestComponentFactory {

    public Quest create(Ontology tbox, @Nullable OBDAModel mappings, @Nullable DBMetadata metadata,
                        Properties config);
}