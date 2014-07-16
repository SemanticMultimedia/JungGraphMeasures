JungGraphMeasures
=================

JungGraphMeasures - PageRank and HITS implementations for large RDF graphs 

This projects uses JUNG — the Java Universal Network/Graph Framework to compute PageRank and HITS scores. 
Jung is a software library that provides a common and extendible language for the modeling, analysis, and 
visualization of data that can be represented as a graph or network. 

##Settings


Parameters used while computing pagerank

```
PageRank damping factor: 0.85 //The probability at any step, that the person will continue
PageRank no of iterations: 100 //Number of iterations used before terminating
PageRank Tolerance: 0 //Minimum change from one step to the next
Alpha: 0.15 //Random jump probability, the probability of taking a random jump to an arbitrary vertex
```
Parameters used while computing HITS

```
No of iterations: 100 //Number of iterations used before terminating
Tolerance: 0 //Minimum change from one step to the next
Alpha: 0.15 //the probability of a hub giving some authority to all vertices, and of an authority increasing the score of   all hubs (not just those connected via links)
```

##Usage


PageRank and HITS are invoked by

```<<inputTurtleFilePath>> <<PageRank or HITS>>```

##Datasets

You can download the resulting datasets here  [DBpedia Pagerank](http://dbpedia.semanticmultimedia.org/3.9/en/pagerank_scores_en.ttl.bz2) and [DBpedia HITS](http://dbpedia.semanticmultimedia.org/3.9/en/hits_scores_en.ttl.bz2)

##Citation

If you are using this dataset please cite as:

```
{dbpedia-graphmeasures,
  Author = {Dinesh Reddy, Magnus Knuth, Harald Sack},
  Title = {DBpedia GraphMeasures},
  Location = {http://semanticmultimedia.org/node/6},
  Resource type = {dataset},
  Publisher = {Hasso Plattner Institute},
  Publication date = {July 2014},
}
```
