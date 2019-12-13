package it.unibz.inf.ontop.endpoint.beans;

import com.google.gson.Gson;
import com.moandjiezana.toml.Toml;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.io.FileNotFoundException;
import java.io.FileReader;

@Component
public class PortalConfigComponent {

    @Bean
    private PortalConfigSerialization setupPortal(@Value("${portal:#{null}}") String portalConfigFile) throws FileNotFoundException {
        String json;
        if (portalConfigFile != null) {
            Toml toml = new Toml().read(new FileReader(portalConfigFile));
            Gson gson = new Gson();
            json = gson.toJson(toml.toMap());
        } else {
            json = "{}";
        }
        return new PortalConfigSerialization(json);
    }

    public static class PortalConfigSerialization {
        public final String jsonString;

        public PortalConfigSerialization(@Nonnull String jsonString) {
            this.jsonString = jsonString;
        }
    }
}
