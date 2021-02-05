Ontop Command Line Interface
============================

This README file is describing the ontop command line interface (CLI).

Ontop ships a shell script (ontop for Linux/OS X) and a bat file (ontop.bat for Windows).
The command line interface exposing the core functionality and several utilities through the command line interface. 
It is an easy way to get the system quickly set-up, tested for correct execution, and querying or materializing, etc, as needed.

Setup
-----

### PATH

Consider put the directory of ontop to your `PATH`.

### jdbc configuration

For jdbc drivers, you will need to manually download them and put them into the `jdbc` directory.


### Bash-completion

* First you'll need to make sure you have bash-completion installed:

Homebrew:

	$ brew install bash-completion

Then add the following lines to your ~/.bash_profile:

    if [ -f $(brew --prefix)/etc/bash_completion ]; then
        . $(brew --prefix)/etc/bash_completion
    fi

Ubuntu:

	$ sudo apt-get install bash-completion

Fedora:

	$ sudo yum install bash-completion

* Then, place `ontop-completion.sh` in your `bash_completion.d` folder, usually something like `/etc/bash_completion.d`, `/usr/local/etc/bash_completion.d`  or `~/bash_completion.d`.
Another approach is to copy it somewhere (e.g. ~/.ontop-completion.sh) and put the following in your `.bash_profile` file:

    source ~/.ontop-completion.sh
    
Usage
-----

```console
$ ./ontop
usage: ontop <command> [ <args> ]

Commands are:
    --version             Show version of ontop
    bootstrap             Bootstrap ontology and mapping from the database
    endpoint              Start a SPARQL endpoint powered by Ontop
    extract-db-metadata   Extract the DB metadata and serialize it into an output JSON file
    help                  Display help information
    materialize           Materialize the RDF graph exposed by the mapping and the OWL ontology
    query                 Query the RDF graph exposed by the mapping and the OWL ontology
    validate              Validate Ontology and Mappings
    mapping               Manipulate mapping files

See 'ontop help <command>' for more information on a specific command.
```

More information is available on the documentation:

https://ontop-vkg.org/guide/cli
