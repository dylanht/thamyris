# Thamyris
The "Titanomachy" - a Greek tale of war in the heavens between the Titans and the Gods - was attributed to a blind Thracian bard named Thamyris (says Wikipedia). I often feel like a deaf-dumb-and-blind man trying to set up all this heavenly tech - could have used good ole' Thammy's third eye for this stuff many a time...

## Bulk Loadin' with Multi-properties and Xml

* **Pre-requisites**
..1. | Pre-requisite | Place that file from this repository into this directory in your incubator-tinkerpop source dir  |
  | ------------- | --- |
  | TitanBulkLoader | gremlin-core/src/main/java/org/apache/tinkerpop/gremlin/process/computer/bulkloading |
  | ScriptRecordReader | hadoop-gremlin/src/main/java/org/apache/tinkerpop/gremlin/hadoop/structure/io/script |

..2. Rename [this file](https://github.com/dkuppitz/openflights/blob/master/src/main/java/com/datastax/openflights/OpenflightsBulkLoaderVertexProgram.java "OpenflightsBulkLoaderVertexProgram.java") to BulkLoaderVertexProgram.java, then do a search and replace in that file to replace all occurences of "OpenflightsBulkLoaderVertexProgram" with "BulkLoaderVertexProgram", then drop the file that results from that operation into gremlin-core/src/main/java/org/apache/tinkerpop/gremlin/process/computer/bulkloading in your incubator-tinkerpop source directory.
..3. Rebuild the TinkerPop3 suite with mvn clean install -DskipTests after making the above changes, and you should be ready to go (let me know if not and I'll see where I went wrong).

For context, check out the repository that the Titan/TinkerPop guys just set up about data migration from TP2 and old Titan to TP3 and new Titan, which goes over how to use BulkLoaderVertexProgram with ScriptInputFormat to parse arbitrary file formats and load chunks of big files into your graph [using Hadoop/Spark or some such framework](https://github.com/dkuppitz/openflights "Openflights").

Building on that, this repository tries to generalize the customized OpenFlightsBulkLoader implementation over there which bequeaths the "set" cardinality to but one lucky property key in the source data. It does this by recognizing it's name when parsing the source data file, and hard-setting its cardinality based on that. We take a baby step further here, and ask you to prepend either "set:" or "list:" to the property key name if you desire a multi-property of the respective "set" or "list" cardinality - so in the parsing script, which in this repo is the file `script-tpclassic-xml.groovy`, it goes something like this:

```groovy
import groovy.xml.*

def parse(line, factory) {
  def xmlRoot = new XmlSlurper().parseText(line)
  // dig into the Xml hierarchy
  def oneSuchValue = xmlRoot.subTag1[0].subTag2[0].text()
  def someOtherValue = xmlRoot.subTtag1[0].@iamattributedahhhhh as String
  def myAnticipatedIdentifier = xmlRoot.@id as String
  def v1 = factory.vertex(myAnticipatedIdentifier, "someLabelForThisVertex")
  // I want to add to a set of properties keyed by the name mySetOfValues
  v1.property(set, "set:mySetOfValues", oneSuchValue)
  // I want to append to a list or properties keyed by the name myListOfValues
  v1.property(list, "list:myListOfValues", someOtherValue)
  return v1
```

Then, in the gremlin REPL when you're bulk loading similar to [Chapter 32](http://s3.thinkaurelius.com/docs/titan/1.0.0/titan-hadoop-tp3.html "Titan Docs") in the Titan Documentation, you can do this to define the schema and load the data from the tpclassic.xml file all in one shot to convince yourself this approach will work for your own Xml data, or for other formats that you want to map to multi-properties in your graph:

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

You should see something like this now in the REPL when you inspect the graph. Notice how the namelist multi-properties and meta-properties are all there as per the XML input file, but the nameset property replaced the first <name> Xml element that had the value "marko" when we addeed another one with value "marko" - you can tell by comparing the meta-properties "okram" and "nokram" between the set/list respectively, notice how in the nameset we see only "nokram" which is associated with the most recent <name> tag for the "marko" <person> Xml element in tpclassic.xml (the ordering is the same between the first and second result set, except for "age" down there at the bottom which doesn't have meta-properties, so is ommitted from the second result set):

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
