def parse(line, factory) {
  def doKnows = { r, v1 ->
    if (!r.knows.isEmpty()) {
      r.knows.each { buddy ->
        def v2 = factory.vertex( buddy.guy[0].text(), r.name() )
        def e = factory.edge(v1, v2, buddy.name() )
        if (!buddy.weight.isEmpty())  {
          e.property(buddy.weight[0].name(), buddy.weight[0].text())
        }
      }
    }
  }
  def root = new XmlSlurper().parseText(line)
  def v1
  if (root.name() == "person") {
    v1 = factory.vertex( root.name[0].text(), root.name() )
    def p1 = v1.property(root.name.name() , root.name[0].text())
    root.name[0].@git.isEmpty() ?: p1.property('git', root.name[0].@git)
    root.name.each { n ->
      def p2 = v1.property(set, 'set:nameset', n.text())
      n.@git.isEmpty() ?: p2.property('git', n.@git)
      def p3 = v1.property(list, 'list:namelist', n.text())
      n.@git.isEmpty() ?: p3.property('git', n.@git)
    }
    v1.property(root.age.name(), root.age[0].text())
    doKnows(root, v1)
    if (!root.created.isEmpty()) {
      root.created.each { nice ->
        def v2 = factory.vertex( nice.thing[0].text(),
                                 nice.thing[0].name() )
        def e = factory.edge(v1, v2, root.created[0].name())
        if (!nice.weight.isEmpty()) {
          e.property(nice.weight.name(), nice.weight.text() as Float)
        }
      }
    }
  } else if (root.name() == "project") {
    v1 = factory.vertex( root.name[0].text(), root.name() )
    v1.property( root.lang[0].name(), root.lang[0].text() )
  }
  return v1
}
