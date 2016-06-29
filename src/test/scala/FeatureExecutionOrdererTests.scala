import org.scalatest.{FunSpec, Matchers}

class FeatureExecutionOrdererTests extends FunSpec with Matchers {
  describe("A FeatureExecutionOrderer") {
    val orderer = new FeatureExecutionOrderer()

    describe("when given an empty graph") {
      it("should return an empty execution order") {
        val graph = FeatureGraph(Set(), Map())
        val result = orderer.calculateExecutionOrder(graph)

        result shouldBe empty
      }
    }

    describe("when given a graph with a single feature") {
      it("should return an execution order with just that feature") {
        val feature = Feature("A", "Group 1")
        val features = Set(feature)
        val dependencies = Map(feature -> Set[Feature]())
        val graph = FeatureGraph(features, dependencies)

        val result = orderer.calculateExecutionOrder(graph)

        result should contain theSameElementsInOrderAs List(Set(feature))
      }
    }

    describe("when given a graph with two independent features") {
      it("should return an execution order with both features in the same evaluation set") {
        val featureA = Feature("A", "Group 1")
        val featureB = Feature("B", "Group 1")
        val features = Set(featureA, featureB)

        val dependencies = Map(
          featureA -> Set[Feature](),
          featureB -> Set[Feature]()
        )

        val graph = FeatureGraph(features, dependencies)

        val result = orderer.calculateExecutionOrder(graph)

        result should contain theSameElementsInOrderAs List(Set(featureA, featureB))
      }
    }

    describe("when given a graph with two features, one dependent on the other") {
      it("should return an execution order with the dependent feature first") {
        val featureA = Feature("A", "Group 1")
        val featureB = Feature("B", "Group 1")
        val features = Set(featureA, featureB)

        val dependencies = Map(
          featureA -> Set[Feature](),
          featureB -> Set(featureA)
        )

        val graph = FeatureGraph(features, dependencies)

        val result = orderer.calculateExecutionOrder(graph)

        result should contain theSameElementsInOrderAs List(Set(featureA), Set(featureB))
      }
    }

    describe("when given a graph with three features, one dependent on another and an independent feature") {
      it("should return an execution order with the dependent and independent features first") {
        val featureA = Feature("A", "Group 1")
        val featureB = Feature("B", "Group 1")
        val featureC = Feature("C", "Group 1")
        val features = Set(featureA, featureB, featureC)

        val dependencies = Map(
          featureA -> Set[Feature](),
          featureB -> Set(featureA),
          featureC -> Set[Feature]()
        )

        val graph = FeatureGraph(features, dependencies)

        val result = orderer.calculateExecutionOrder(graph)

        result should contain theSameElementsInOrderAs List(
          Set(featureA, featureC),
          Set(featureB)
        )
      }
    }

    describe("when given the graph given in the problem statement") {
      it("should return the correct evaluation order") {
        val featureA = Feature("A", "Group 1")
        val featureB = Feature("B", "Group 1")
        val featureC = Feature("C", "Group 1")
        val featureD = Feature("D", "Group 2")
        val featureE = Feature("E", "Group 2")
        val featureF = Feature("F", "Group 2")
        val featureG = Feature("G", "Group 1")
        val featureH = Feature("H", "Group 1")
        val featureI = Feature("I", "Group 2")
        val featureJ = Feature("J", "Group 2")
        val features = Set(featureA, featureB, featureC, featureD, featureE, featureF, featureG, featureH, featureI, featureJ)

        val dependencies = Map(
          featureA -> Set[Feature](),
          featureB -> Set[Feature](),
          featureC -> Set[Feature](),
          featureD -> Set[Feature](),
          featureE -> Set[Feature](),
          featureF -> Set[Feature](),
          featureG -> Set(featureA),
          featureH -> Set(featureA, featureB),
          featureI -> Set(featureD, featureE, featureH),
          featureJ -> Set(featureF, featureI)
        )

        val graph = FeatureGraph(features, dependencies)

        val result = orderer.calculateExecutionOrder(graph)

        result should contain theSameElementsInOrderAs List(
          Set(featureA, featureB, featureC, featureD, featureE, featureF),
          Set(featureG, featureH),
          Set(featureI),
          Set(featureJ)
        )
      }
    }
  }
}
