package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import it.unibz.inf.ontop.utils.VersionInfo;

@Command(name = "--version",
        description = "Show version of ontop")
public class OntopVersion implements OntopCommand{

    @Override
    public void run() {
        //String version = getClass().getPackage().getImplementationVersion();

        VersionInfo versionInfo = VersionInfo.getVersionInfo();

        System.out.println(String.format("ontop version %s", versionInfo.toString()));
    }
}
