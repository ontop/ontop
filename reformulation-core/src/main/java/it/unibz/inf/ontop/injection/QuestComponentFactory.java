package it.unibz.inf.ontop.injection;

import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlrefplatform.core.DBConnector;
import it.unibz.inf.ontop.owlrefplatform.core.IQuest;
import it.unibz.inf.ontop.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;

import java.util.Optional;

public interface QuestComponentFactory {

    IQuest create(Ontology inputTBox, @Assisted Optional<OBDAModel> inputMappings,
                  @Assisted Optional<DBMetadata> inputMetadata, ExecutorRegistry executorRegistry);

    NativeQueryGenerator create(DBMetadata metadata);
    NativeQueryGenerator create(DBMetadata metadata, SemanticIndexURIMap uriRefIds);

    DBConnector create(IQuest questInstance);
}