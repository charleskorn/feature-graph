class FeatureExecutionOrderer {
  def calculateExecutionOrder(graph: FeatureGraph): List[Set[Feature]] = {
    var featuresLeftToExecute = graph.features
    var executionOrder = List[Set[Feature]]()

    while (featuresLeftToExecute.nonEmpty) {
      val featuresExecuted = executionOrder.flatten.toSet

      val featuresInThisEvaluationSet = featuresLeftToExecute
        .filter(feature => canBeExecuted(feature, featuresExecuted, graph))

      if (featuresInThisEvaluationSet.isEmpty) {
        throw new Exception("Circular dependency detected.")
      }

      executionOrder = executionOrder :+ featuresInThisEvaluationSet
      featuresLeftToExecute = featuresLeftToExecute -- featuresInThisEvaluationSet
    }

    executionOrder
  }

  private def canBeExecuted(feature: Feature, featuresExecuted: Set[Feature], graph: FeatureGraph): Boolean = {
    val dependencies = graph.dependencies(feature)

    dependencies.forall(dependency => featuresExecuted.contains(dependency))
  }
}
