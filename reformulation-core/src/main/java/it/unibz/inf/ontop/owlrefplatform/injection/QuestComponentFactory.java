package it.unibz.inf.ontop.owlrefplatform.injection;

import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlrefplatform.core.DBConnector;
import it.unibz.inf.ontop.owlrefplatform.core.IQuest;
import it.unibz.inf.ontop.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import it.unibz.inf.ontop.model.DataSourceMetadata;

import java.util.Optional;

public interface QuestComponentFactory {

    IQuest create(Ontology inputTBox, @Assisted Optional<OBDAModel> inputMappings,
                  @Assisted Optional<DataSourceMetadata> inputMetadata);

    NativeQueryGenerator create(DataSourceMetadata metadata, OBDADataSource dataSource);
    NativeQueryGenerator create(DataSourceMetadata metadata, OBDADataSource dataSource,
                                       SemanticIndexURIMap uriRefIds);

    DBConnector create(OBDADataSource obdaDataSource, IQuest questInstance);
}