package com.graphbrain

/** Hypergraph Data Base.
  *
  * Implements and hypergraph database on top of a simple key/value store. 
  */
class HGDB(storeName: String) {
  val store = new Store(storeName)

  /** Gets Vertex by it's _id */
  def get(_id: String) = {
    val map = store.get(_id)
    val rid = map.getOrElse("_id", "").toString
    map("vtype") match {
      case "vertex" => Vertex(rid)
      case "node" => {
        val edges: Set[String] = map.getOrElse("edges", List[String]()).asInstanceOf[List[String]].toSet
        Node(rid, edges)
      }
      case "edge" => {
        val etype = map.getOrElse("etype", "").toString
        Edge(rid, etype)
      }
      case "edgeType" => {
        val label = map.getOrElse("label", "").toString
        EdgeType(rid, label)
      }
      case _  => Vertex(rid)
    }
  }

  /** Adds Vertex to database */
  def put(vertex: Vertex) = {
    store.put(vertex.toMap)
    vertex
  }

  /** Updates vertex on database */
  def update(vertex: Vertex) = {
    store.update(vertex._id, vertex.toMap)
    vertex
  }

  /** Removes vertex from database */
  def remove(vertex: Vertex) = store.remove(vertex._id)

  /** Adds relationship to database
    * 
    * An edge is creted and participant nodes are updated with a reference to that edge
    * in order to represent a new relationship on the database.
    */
  def addrel(edgeType: String, participants: Array[Node]) = {
    val edge = Edge(edgeType, participants)
    put(edge)

    for (node <- participants) node.addEdge(edge)

    edge
  }

  /** Deletes relationship from database
    * 
    * The edge defining the relationship is removed and participant nodes are updated 
    * to drop the reference to that edge.
    */
  def delrel(edgeType: String, participants: Array[Node]) = {
    val edge = Edge(edgeType, participants)
    remove(edge)

    for (node <- participants) node.delEdge(edge)

    edge
  }
}

object HGDB {
  def apply(storeName: String) = new HGDB(storeName)
}