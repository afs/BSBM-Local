==== BSBM-Jena

Berlin SPARQL Benchmark with additions for testing Jena local stores.

Adds new endpoint formats:

* "jena:assembler" where "assembler" is the file name of a Jena assembler
description for the store.

* "sesame:directory" to connect to a Seame repository.


== Get the system and setup

git clone git://github.com/afs/BSBM-Local.git
mkdir Data
mkdir src-test

# Get the dependent jars
ivy -settings ivysettings.xml -ivy ivy.xml \
    -retrieve 'lib-sys/[artifact]-[revision](-[classifier]).[ext]' \
    -sync retrieve


# Compile the system
ant jar

Either use Eclipse (the project setup is included) or use "ant jar" which
places the jar in build/.

==  Directories

Data/data-SIZE          -- generated data
TDB-DB/DB-SIZE/         -- TDB 
Sesame-DB/Repo-SIZE     -- Sesame

== Process - for TDB backed stores.

# Choose sizes wanted
genAll

# Run the tdbloader, create stats files and assembler files.           
loadAll 

# Run the performance tests
# Results go into "Results/"
runPerf

# Generate the results, naming the results directory.
# NB The BSBM formatter knows the possible store sizes 
runResults Results 

== Process - Sesame

loadSesame	
runPerfSesame

== Questions?

If about BSBM generaly, ask the authors, see 
http://www4.wiwiss.fu-berlin.de/bizer/BerlinSPARQLBenchmark/

If about teh Jena customization, please send questions to:

    jena-dev@groups.yahoo.com

