def defineTinkerPopClassicSchema(titanGraph) {
  m = titanGraph.openManagement()
  // vertex labels
  person = m.makeVertexLabel("person").make()
  project = m.makeVertexLabel("project").make()
  // edge labels
  knows = m.makeEdgeLabel("knows").make()
  created = m.makeEdgeLabel("created").make()
  // v/e props
  name = m.makePropertyKey("name").dataType(String.class).make()
  namelist = m.makePropertyKey("namelist").dataType(String.class).cardinality(Cardinality.LIST).make()
  git = m.makePropertyKey("git").dataType(String.class).make()
  nameset = m.makePropertyKey("nameset").dataType(String.class).cardinality(Cardinality.SET).make()
  weight = m.makePropertyKey("weight").dataType(Float.class).make()
  age = m.makePropertyKey("age").dataType(Integer.class).make()
  lang = m.makePropertyKey("lang").dataType(String.class).make()
  // indices
  m.buildIndex("peopleByName", Vertex.class).addKey(name).indexOnly(person).buildCompositeIndex()
  m.buildIndex("projectsByName", Vertex.class).addKey(name).indexOnly(project).buildCompositeIndex()
  // vindices
  m.buildEdgeIndex(knows, "knowsByWeight", Direction.BOTH, Order.decr, weight)
  m.buildEdgeIndex(created, "createdByWeight", Direction.BOTH, Order.decr, weight)
  m.commit()
}
