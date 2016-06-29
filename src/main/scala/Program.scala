object Program extends App {
  run(args)

  def run(args: Array[String]): Unit = {
    try {
      if (args.length != 1) {
        printHelp()
        return
      }

      val input = args(0)

      val parser = new FeatureGraphParser()
      val graph = parser.parse(input)

      val executionOrderer = new FeatureExecutionOrderer()
      val executionOrder = executionOrderer.calculateExecutionOrder(graph)

      val formattedOrder = executionOrder
        .map(executionSet => executionSet.map(feature => feature.name).mkString(","))
        .mkString("\n")

      println(formattedOrder)

    } catch {
      case e: Throwable => println(s"Error: ${e.getMessage}")
    }
  }

  private def printHelp(): Unit = {
    println("Usage: feature-graph <graph>")
    println()
    println("Example: 'feature-graph \"(A,B)[A->B] (C,D)[C->B,C->D]\"' will determine the execution order of features A, B, C and D given that A depends on B and C depends on both B and D")
  }
}
