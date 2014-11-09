package org.semanticweb.ontop.owlrefplatform.injection;

import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.owlrefplatform.core.Quest;
import org.semanticweb.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import org.semanticweb.ontop.sql.DBMetadata;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Properties;

public interface QuestComponentFactory {

    public Quest create(Ontology tBox, @Nullable OBDAModel mappings, @Nullable DBMetadata metadata,
                        Properties config);

    public NativeQueryGenerator create(DBMetadata metadata, OBDADataSource dataSource);
    public NativeQueryGenerator create(DBMetadata metadata, OBDADataSource dataSource, Map<String, Integer> uriRefIds);
}