package it.unibz.inf.ontop.spec.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.ImplicitDBContraintException;
import it.unibz.inf.ontop.spec.dbschema.PreProcessedImplicitRelationalDBConstraintExtractor;
import it.unibz.inf.ontop.spec.dbschema.PreProcessedImplicitRelationalDBConstraintSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;

/**
 *
 * Moved from ImplicitDBContraintsReader (by Dag Hovland)
 *
 */
@Singleton
public class BasicPreProcessedImplicitRelationalDBConstraintExtractor implements PreProcessedImplicitRelationalDBConstraintExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicPreProcessedImplicitRelationalDBConstraintExtractor.class);

    @Inject
    private BasicPreProcessedImplicitRelationalDBConstraintExtractor() {
    }

    @Override
    public PreProcessedImplicitRelationalDBConstraintSet extract(@Nonnull File constraintFile)
            throws ImplicitDBContraintException {
        ImmutableList.Builder<String[]> ucBuilder = ImmutableList.builder();
        ImmutableList.Builder<String[]> fkBuilder = ImmutableList.builder();

        try (BufferedReader reader = new BufferedReader(new FileReader(constraintFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) { // Primary Key	/ Unique Constraint
                    ucBuilder.add(parts);
                }
                else if (parts.length == 4) { // Foreign Key
                    fkBuilder.add(parts);
                }
            }
        }
        catch (FileNotFoundException e) {
            LOGGER.warn("Could not find file " + constraintFile + " in directory " + System.getenv().get("PWD"));
            String currentDir = System.getProperty("user.dir");
            LOGGER.warn("Current dir using System:" + currentDir);
            throw new ImplicitDBContraintException("Constraint file " + constraintFile + " does not exist");
        }
        catch (IOException e) {
            LOGGER.warn("Problem reading keys from the constraint file " + constraintFile);
            LOGGER.warn(e.getMessage());
            throw new ImplicitDBContraintException(e);
        }

        return new BasicPreProcessedImplicitRelationalDBConstraintSet(ucBuilder.build(), fkBuilder.build());
    }
}
