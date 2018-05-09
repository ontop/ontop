package it.unibz.inf.ontop.protege.core;

/*
 * #%L
 * ontop-protege
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.dbschema.JdbcTypeMapper;
import it.unibz.inf.ontop.dbschema.Relation2Predicate;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopTemporalSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.spec.datalogmtl.parser.impl.DatalogMTLSyntaxParserImpl;
import it.unibz.inf.ontop.spec.mapping.serializer.OntopNativeTemporalMappingSerializer;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import org.protege.editor.core.ui.util.UIUtil;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.prefix.PrefixUtilities;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class TemporalOBDAModelManager extends OBDAModelManager {
    private static final Logger log = LoggerFactory.getLogger(OBDAModelManager.class);
    // The temporal OBDA file extension
    private static final String TOBDA_EXT = ".tobda";
    // The temporal rules file extension
    private static final String RULE_EXT = ".dmtl";

    TemporalOBDAModelManager(OWLEditorKit owlEditorKit) {
        this.owlEditorKit = owlEditorKit;

        OntopTemporalSQLOWLAPIConfiguration configuration = OntopTemporalSQLOWLAPIConfiguration.defaultBuilder()
                .enableTemporalMode()
                .jdbcDriver("")
                .jdbcUrl("")
                .jdbcUser("")
                .jdbcPassword("")
                .build();

        SpecificationFactory specificationFactory = configuration.getInjector().getInstance(SpecificationFactory.class);
        SQLPPMappingFactory ppMappingFactory = configuration.getInjector().getInstance(SQLPPMappingFactory.class);

        atomFactory = configuration.getInjector().getInstance(AtomFactory.class);
        termFactory = configuration.getInjector().getInstance(TermFactory.class);
        typeFactory = configuration.getInjector().getInstance(TypeFactory.class);
        datalogFactory = configuration.getInjector().getInstance(DatalogFactory.class);
        relation2Predicate = configuration.getInjector().getInstance(Relation2Predicate.class);
        jdbcTypeMapper = configuration.getInjector().getInstance(JdbcTypeMapper.class);

        updateModelManagerListener(new OBDAPluginTemporalOWLModelManagerListener());

        owlEditorKit.getModelManager().getOWLOntologyManager().addOntologyChangeListener(new OntologyRefactoringListener());

        PrefixDocumentFormat prefixFormat = PrefixUtilities.getPrefixOWLOntologyFormat(owlEditorKit.getModelManager().getActiveOntology());

        setActiveModel(new TemporalOBDAModel(specificationFactory, ppMappingFactory,
                PrefixUtilities.getPrefixOWLOntologyFormat(owlEditorKit.getModelManager().getActiveOntology()),
                atomFactory, termFactory, typeFactory, datalogFactory, relation2Predicate, jdbcTypeMapper)
        );

        setConfigurationManager(new TemporalOntopConfigurationManager(
                ((OBDAModelManager) owlEditorKit.get(OBDAModelManager.class.getName())).getActiveOBDAModel(),
                getActiveOBDAModel(),
                (DisposableProperties) owlEditorKit.get(DisposableProperties.class.getName())));
    }


    public TemporalOBDAModel getActiveOBDAModel() {
        return (TemporalOBDAModel) super.getActiveOBDAModel();
    }

    private void saveRuleFile(String owlName) {
        File ruleFile = new File(URI.create(owlName + RULE_EXT));
        new DatalogMTLSyntaxParserImpl(getAtomFactory(), getTermFactory())
                .save(getActiveOBDAModel().getDatalogMTLProgram(), ruleFile);
        log.info("rules are saved to {} file", ruleFile);
    }

    private void saveTOBDAFile(String owlName) throws IOException {
        File tobdaFile = new File(URI.create(owlName + TOBDA_EXT));
        new OntopNativeTemporalMappingSerializer(getActiveOBDAModel().generatePPMapping())
                .save(tobdaFile);
        log.info("mappings are saved to {} file", tobdaFile);
    }

    private void loadTOBDAFile(String owlName) throws Exception {
        File tobdaFile = new File(URI.create(owlName + TOBDA_EXT));

        if (tobdaFile.exists()) {
            try {
                getActiveOBDAModel().parseMapping(tobdaFile, getConfigurationManager().snapshotProperties());
            } catch (Exception ex) {
                throw new Exception("Exception occurred while loading TOBDA file: " + tobdaFile + "\n\n" + ex.getMessage());
            }
        } else {
            log.warn("Temporal OBDA model couldn't be loaded because .tobda file doesn't exist in the same location of the .owl file");
        }
        //TODO validation of temporal mappings
        /*for (SQLPPTriplesMap mapping : getActiveOBDAModel().generatePPMapping().getTripleMaps()) {
            List<? extends Function> tq = mapping.getTargetAtoms();
            if (!TargetQueryValidator.validate(tq, getActiveOBDAModel().getCurrentVocabulary()).isEmpty()) {
                throw new Exception("Found an invalid target query: " + tq.toString());
            }
        }*/
    }

    private void loadRuleFile(String owlName) throws Exception {
        File ruleFile = new File(URI.create(owlName + RULE_EXT));
        if (ruleFile.exists()) {
            try {
                getActiveOBDAModel().setDatalogMTLProgram(
                        new DatalogMTLSyntaxParserImpl(getAtomFactory(), getTermFactory())
                                .parse(ruleFile)
                );
            } catch (Exception ex) {
                throw new Exception("Exception occurred while loading rule file: " + ruleFile + "\n\n" + ex.getMessage());
            }
        } else {
            log.warn("Temporal rules couldn't be loaded because .dmtl file doesn't exist in the same location of the .owl file");
        }
    }

    class OBDAPluginTemporalOWLModelManagerListener extends OBDAPluginOWLModelManagerListener {

        void handleNewActiveOntology() {
            super.handleNewActiveOntology();
        }

        void handleOntologyLoadedAndReLoaded(OWLModelManager owlModelManager, OWLOntology activeOntology) {
            loadingData = true;
            try {
                IRI documentIRI = owlModelManager.getOWLOntologyManager().getOntologyDocumentIRI(activeOntology);

                if (!UIUtil.isLocalFile(documentIRI.toURI())) {
                    return;
                }

                String owlDocumentIriString = documentIRI.toString();
                String owlName = owlDocumentIriString.substring(0, owlDocumentIriString.lastIndexOf("."));

                loadProperties(owlName);
                loadTOBDAFile(owlName);
                loadRuleFile(owlName);
            } catch (Exception e) {
                InvalidOntopConfigurationException ex = new InvalidOntopConfigurationException("An exception has occurred when loading input file.\nMessage: " + e.getMessage());
                DialogUtils.showQuickErrorDialog(null, ex, "Open file error");
                log.error(e.getMessage());
            } finally {
                loadingData = false;
                fireActiveOBDAModelChange();
            }
        }

        void handleOntologySaved(OWLModelManager owlModelManager, OWLOntology activeOntology) {
            try {
                IRI documentIRI = owlModelManager.getOWLOntologyManager().getOntologyDocumentIRI(activeOntology);
                String owlDocumentIriString = documentIRI.toString();

                if (!UIUtil.isLocalFile(documentIRI.toURI())) {
                    return;
                }

                int i = owlDocumentIriString.lastIndexOf(".");
                String owlName = owlDocumentIriString.substring(0, i);

                saveTOBDAFile(owlName);
                saveRuleFile(owlName);

            } catch (Exception e) {
                log.error(e.getMessage());
                Exception newException = new Exception(
                        "Error saving the OBDA file. Closing Protege now can result in losing changes in your data sources or mappings. Please resolve the issue that prevents saving in the current location, or do \"Save as..\" to save in an alternative location. \n\nThe error message was: \n"
                                + e.getMessage());
                DialogUtils.showQuickErrorDialog(null, newException, "Error saving OBDA file");
                triggerOntologyChanged();
            }
        }
    }

}
