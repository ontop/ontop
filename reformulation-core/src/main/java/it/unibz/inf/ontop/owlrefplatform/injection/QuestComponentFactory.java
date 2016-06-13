package it.unibz.inf.ontop.owlrefplatform.injection;

import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlrefplatform.core.DBConnector;
import it.unibz.inf.ontop.owlrefplatform.core.IQuest;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import it.unibz.inf.ontop.sql.DBMetadata;
import it.unibz.inf.ontop.model.DataSourceMetadata;

import javax.annotation.Nullable;

public interface QuestComponentFactory {

    public IQuest create(Ontology tBox, @Nullable OBDAModel mappings, @Nullable DBMetadata metadata,
                        QuestPreferences config);

    public NativeQueryGenerator create(DataSourceMetadata metadata, OBDADataSource dataSource);
    public NativeQueryGenerator create(DataSourceMetadata metadata, OBDADataSource dataSource,
                                       SemanticIndexURIMap uriRefIds);

    public DBConnector create(OBDADataSource obdaDataSource, IQuest questInstance);
}