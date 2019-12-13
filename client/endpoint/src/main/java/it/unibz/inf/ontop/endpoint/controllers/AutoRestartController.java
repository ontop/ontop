package it.unibz.inf.ontop.endpoint.controllers;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.endpoint.OntopEndpointApplication;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;
import java.util.stream.Stream;

@RestController
@ConditionalOnExpression("${dev:false}")
public class AutoRestartController {

    private static final Logger log = LoggerFactory.getLogger(AutoRestartController.class);

    @Autowired
    public AutoRestartController(@Value("${mapping}") String mappingFile,
                                 @Value("${properties}") String propertiesFile,
                                 @Value("${ontology:#{null}}") String owlFile,
                                 @Value("${portal:#{null}}") String portalFile) {
        registerFileWatcher(mappingFile, owlFile, propertiesFile, portalFile);
    }

    @PostMapping("/ontop/restart")
    public void restart() {
        OntopEndpointApplication.restart();
    }

    private void registerFileWatcher(String mappingFile, @Nullable String owlFile, String propertiesFile, @Nullable String portalFile) {
        // this code assumes that the input files are under the same directory
        final Path path = FileSystems.getDefault().getPath(new File(mappingFile).getAbsolutePath()).getParent();

        ImmutableList<String> filesToWatch = Stream.of(mappingFile, owlFile, propertiesFile, portalFile)
                .filter(Objects::nonNull)
                .collect(ImmutableCollectors.toList());

        //System.out.println(path);
        new Thread(() -> {
            try {
                final WatchService watchService = FileSystems.getDefault().newWatchService();
                final WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                while (true) {
                    final WatchKey wk = watchService.take();
                    for (WatchEvent<?> event : wk.pollEvents()) {
                        //we only register "ENTRY_MODIFY" so the context is always a Path.
                        final Path changed = (Path) event.context();
                        System.out.println(changed + " changed detected!");
                        if (filesToWatch.stream().anyMatch(changed::endsWith)) {
                            log.info("RESTARTING Ontop!");
                            OntopEndpointApplication.restart();
                        }
                    }
                    // reset the key
                    boolean valid = wk.reset();
                    if (!valid) {
                        System.out.println("Key has been unregistered");
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


}
