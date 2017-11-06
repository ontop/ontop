package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.answering.reformulation.input.translation.InputQueryTranslator;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.answering.reformulation.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;
import it.unibz.inf.ontop.answering.reformulation.rewriting.SameAsRewriter;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;

/**
 * Following the Guice AssistedInject pattern
 */
public interface TranslationFactory {

    QueryUnfolder create(Mapping mapping);

    NativeQueryGenerator create(DBMetadata metadata);

    InputQueryTranslator createInputQueryTranslator(UriTemplateMatcher uriTemplateMatcher);

    SameAsRewriter createSameAsRewriter(Mapping saturatedMapping);
}
