package it.unibz.inf.ontop.cli;

public enum Compression {
    gzip(".gz"),
    zip(".zip"),
    no_compression("");

    private final String extension;

    Compression(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}
