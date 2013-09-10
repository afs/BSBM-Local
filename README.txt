==== BSBM-Jena

Berlin SPARQL Benchmark with additions for testing local stores.  BSBM runs
all the queries over the SPARQL Protocol whereas this version calls the
stores directly, so removing protocol overhead.  For the smaller datasets
and simpler queries, the overheads can be a significant part of the cost so
evaluation of stores can be skewed.

BSBM-Local adds new endpoint formats as pseudo-URI schemes:

* "jena:<assembler>" where "assembler" is the file name of a Jena assembler
description for the store.

== Get the system and setup

git clone git://github.com/afs/BSBM-Local.git
mvn package

# Create directories.
mkdir Data TDB-DB

==  Directories

Data/data-SIZE          -- generated data
TDB-DB/DB-SIZE/         -- TDB 

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

== Questions?

If about BSBM generaly, ask the authors, see 
http://www4.wiwiss.fu-berlin.de/bizer/BerlinSPARQLBenchmark/

If about the local customization, please send questions to:

    users@jena.apache.org

(subscribe to the list before sending)


