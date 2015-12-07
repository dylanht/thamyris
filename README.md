# thamyris
The "Titanomachy" - a Greek tale of war in the heavens between the Titans and the Gods - was attributed to a blind Thracian bard named Thamyris (says Wikipedia). I often feel like a deaf-dumb-and-blind man trying to set up all this heavenly tech - could use good ole' Thammy's third eye round these parts...

## Bulk Loadin'

Check out the repository that the Titan/TinkerPop guys just set up about data migration from TP2 and old Titan to TP3 and new Titan, which goes over how to use BulkLoaderVertexProgram with ScriptInputFormat to parse arbitrary file formats and load chunks of big files into your graph [using Hadoop/Spark or some such framework](https://github.com/dkuppitz/openflights "Openflights").

Building on that, this repository tries to generalize the customizedOpenFlightsBulkLoader implementation over there which gives a particular property the right cardinality by recognizing it's name and hard-setting it in the parsing script - we take a baby step further here, and ask you to prepend either "set:" or "list:" to the property key if you desire a multi-property of that cardinality - so in the parsing script, which in this repo is the file `script-tpclassic-xml.groovy`, it goes like:

```groovy
// I want a set property
v1.property(set, "set:mySetOfValues", oneSuchValue)
// I want a list property
v1.property(list, "list:myListOfValues", oneSuchValue)
```
And then, in the gremlin REPL when you're bulk loading similar to [Chapter 32](http://s3.thinkaurelius.com/docs/titan/1.0.0/titan-hadoop-tp3.html "Titan Docs") in the Titan Documentation, it goes like:

```groovy
hdfs.copyFromLocal('tpclassic.xml', 'data/tpclassic.xml')
hdfs.copyFromLocal('script-tpclassic-xml.groovy', 'script-tpclassic-xml.groovy')
graph = GraphFactory.open('tpclassic-load-projects.olap')
blvp = BulkLoaderVertexProgram.build().bulkLoader(TitanBulkLoader.class).intermediateBatchSize(2).writeGraph('tpclassic.graph').create(graph)
graph.compute(SparkGraphComputer).program(blvp).submit().get()
```

And then once more to load persons:

```groovy
graph = GraphFactory.open('tpclassic-load-persons.olap')
blvp = BulkLoaderVertexProgram.build().bulkLoader(TitanBulkLoader.class).intermediateBatchSize(2).writeGraph('tpclassic.graph').create(graph)
graph.compute(SparkGraphComputer).program(blvp).submit().get()
```

Check it out and let me know if it works - you should see something like this now in the REPL when you inspect the graph:

