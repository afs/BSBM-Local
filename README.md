# BSBM-Jena

This is the Berlin SPARQL Benchmark with additions for testing local Jena stores.
BSBM runs all the queries over the SPARQL Protocol whereas can also the
stores directly, so removing protocol overhead.  For the smaller datasets
and simpler queries, the overheads can be a significant part of the cost so
evaluation of stores can be skewed.

BSBM-Local adds new endpoint formats as pseudo-URI schemes:

* "jena:<assembler>" where "assembler" is the file name of a Jena assembler
description for the store.

## Get the system and setup

git clone git://github.com/afs/BSBM-Local.git
mvn clean package

# Create directories.
mkdir -p Data TDB-DB Results

Edit "cp" this is the classpath.  You need to change the location of
you maven repository.

##  Directories

Data/data-SIZE          -- generated data
TDB-DB/DB-SIZE/         -- TDB 

## Process - for TDB backed stores.

# Choose sizes wanted - edit this script.
genAll

# Run the tdbloader, create stats files and assembler files.           
loadAll 

# Run the performance tests
# Results go into "Results/"
runPerf

# Generate the results, naming the results directory.
# NB The BSBM formatter knows the possible store sizes 
runResults Results 

## Questions?

If about BSBM generaly, ask the authors, see 
http://wifo5-03.informatik.uni-mannheim.de/bizer/berlinsparqlbenchmark/

If about the local customization, please send questions to:

    users@jena.apache.org

(subscribe to the list before sending)
