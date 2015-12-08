# thamyris
The "Titanomachy" - a Greek tale of war in the heavens between the Titans and the Gods - was attributed to a blind Thracian bard named Thamyris (says Wikipedia). I often feel like a deaf-dumb-and-blind man trying to set up all this heavenly tech - could use good ole' Thammy's third eye round these parts...

## Bulk Loadin' with Multi-properties and XML

Check out the repository that the Titan/TinkerPop guys just set up about data migration from TP2 and old Titan to TP3 and new Titan, which goes over how to use BulkLoaderVertexProgram with ScriptInputFormat to parse arbitrary file formats and load chunks of big files into your graph [using Hadoop/Spark or some such framework](https://github.com/dkuppitz/openflights "Openflights").

Building on that, this repository tries to generalize the customized OpenFlightsBulkLoader implementation over there which gives a particular property the right cardinality by recognizing it's name and hard-setting it in the parsing script - we take a baby step further here, and ask you to prepend either "set:" or "list:" to the property key if you desire a multi-property of that cardinality - so in the parsing script, which in this repo is the file `script-tpclassic-xml.groovy`, it goes like:

```groovy
// I want a set property
v1.property(set, "set:mySetOfValues", oneSuchValue)
// I want a list property
v1.property(list, "list:myListOfValues", oneSuchValue)
```
And then, in the gremlin REPL when you're bulk loading similar to [Chapter 32](http://s3.thinkaurelius.com/docs/titan/1.0.0/titan-hadoop-tp3.html "Titan Docs") in the Titan Documentation, you can do this to define the schema and load the data from the tpclassic.xml file all in one shot:

```groovy
graph = TitanFactory.open('tpclassic.graph')
graph.close()
com.thinkaurelius.titan.core.util.TitanCleanup.clear(graph)
graph = TitanFactory.open('tpclassic.graph')
:load tinkerpop-classic-titan-schema.groovy
defineTinkerPopClassicSchema(graph)
hdfs.copyFromLocal('tpclassic.xml', 'data/tpclassic.xml')
hdfs.copyFromLocal('script-tpclassic-xml.groovy', 'script-tpclassic-xml.groovy')
graph = GraphFactory.open('tpclassic-load-projects.olap')
blvp = BulkLoaderVertexProgram.build().bulkLoader(TitanBulkLoader.class).intermediateBatchSize(2).writeGraph('tpclassic.graph').create(graph)
graph.compute(SparkGraphComputer).program(blvp).submit().get()
graph = GraphFactory.open('tpclassic-load-persons.olap')
blvp = BulkLoaderVertexProgram.build().bulkLoader(TitanBulkLoader.class).intermediateBatchSize(2).writeGraph('tpclassic.graph').create(graph)
graph.compute(SparkGraphComputer).program(blvp).submit().get()
```

You should see something like this now in the REPL when you inspect the graph. Notice how the namelist multi-properties and meta-properties are all there as per the XML input file, but the nameset property replaced the first <name> Xml element with the value "marko" when we addeed another "marko" - you can tell by comparing the meta-properties "okram" and "nokram" between the set/list respectively.
a
```groovy
gremlin> g.V().has("namelist", "marko").properties()
00:43:23 WARN  com.thinkaurelius.titan.graphdb.transaction.StandardTitanTx  - Query requires iterating over all vertices [(namelist = marko)]. For better performance, use indexes
==>vp[bulkLoader.vertex.id->marko]
==>vp[name->marko]
==>vp[namelist->marko]
==>vp[namelist->monstere sur la code]
==>vp[namelist->marko]
==>vp[nameset->marko]
==>vp[nameset->monstere sur la code]
==>vp[age->29]
gremlin> g.V().has("namelist", "marko").properties().properties()
00:43:26 WARN  com.thinkaurelius.titan.graphdb.transaction.StandardTitanTx  - Query requires iterating over all vertices [(namelist = marko)]. For better performance, use indexes
==>p[git->okram]
==>p[git->okram]
==>p[git->N/A]
==>p[git->nokram]
==>p[git->nokram]
==>p[git->N/A]
gremlin> 
```
