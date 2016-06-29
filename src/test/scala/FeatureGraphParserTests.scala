import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.{FunSpec, Matchers}

class FeatureGraphParserTests extends FunSpec with Matchers {
  describe("A FeatureGraphParser") {
    val parser = new FeatureGraphParser()

    describe("when given an empty string to parse") {
      it("should return an empty feature graph") {
        val result = parser.parse("")

        result.features shouldBe empty
        result.dependencies shouldBe empty
      }
    }

    describe("when given a single feature in a single group") {
      it("should return that feature") {
        val actual = parser.parse("(A)")
        val expectedFeatures = Set(Feature("A", "1"))

        actual.features should contain theSameElementsAs expectedFeatures
        actual.dependencies should beEmptyDependencyMapFor(expectedFeatures)
      }
    }

    Set(
      "(A,B)",
      "(A, B)",
      "(A,  B)",
      "(A, B )",
      "( A, B)",
      "( A,B)",
      "(A,B)[]",
      "(A,B) []",
      "(A,B)[ ]",
      "(A,B) [ ]"
    ).foreach(input => {
      describe(s"when given two features in a single group in the format '$input'") {
        it("should return those two features") {
          val actual = parser.parse(input)

          val expectedFeatures = Set(
            Feature("A", "1"),
            Feature("B", "1")
          )

          actual.features should contain theSameElementsAs expectedFeatures
          actual.dependencies should beEmptyDependencyMapFor(expectedFeatures)
        }
      }
    })

    describe("when given three features in a single group") {
      it("should return those three features") {
        val actual = parser.parse("(A,B,C)")

        val expectedFeatures = Set(
          Feature("A", "1"),
          Feature("B", "1"),
          Feature("C", "1")
        )

        actual.features should contain theSameElementsAs expectedFeatures
        actual.dependencies should beEmptyDependencyMapFor(expectedFeatures)
      }
    }

    Set(
      "(A)(B)",
      "(A) (B)",
      "(A)\n(B)",
      "(A)[](B)[]",
      "(A)[] (B)[]",
      "(A) [] (B) []"
    ).foreach(input => {
      describe(s"when given two features in individual groups in the format '$input'") {
        it("should return those two features") {
          val actual = parser.parse(input)

          val expectedFeatures = Set(
            Feature("A", "1"),
            Feature("B", "2")
          )

          actual.features should contain theSameElementsAs expectedFeatures
          actual.dependencies should beEmptyDependencyMapFor(expectedFeatures)
        }
      }
    })

    describe("when given features with special characters in the name") {
      it("should return those features") {
        val actual = parser.parse("(AB_12,_123,1,ab)")

        val expectedFeatures = Set(
          Feature("AB_12", "1"),
          Feature("_123", "1"),
          Feature("1", "1"),
          Feature("ab", "1")
        )

        actual.features should contain theSameElementsAs expectedFeatures
        actual.dependencies should beEmptyDependencyMapFor(expectedFeatures)
      }
    }

    Set(
      "(",
      ")",
      "A",
      "A,B",
      "(AB!)"
    ).foreach(input => {
      describe(s"when given the invalid input '$input'") {
        it("should throw an exception") {
          an [FeatureGraphParseException] should be thrownBy parser.parse(input)
        }
      }
    })

    describe("when given two features in a single group with the same name") {
      it("should throw an exception") {
        val exception = the [InvalidFeatureGraphException] thrownBy parser.parse("(A,A)")

        exception.message should equal ("The feature 'A' is repeated multiple times.")
      }
    }

    describe("when given two features in different groups with the same name") {
      it("should throw an exception") {
        val exception = the [InvalidFeatureGraphException] thrownBy parser.parse("(A)(A)")

        exception.message should equal ("The feature 'A' is repeated multiple times.")
      }
    }

    Set(
      "(A,B)[A->B]",
      "(A,B)[ A->B]",
      "(A,B)[A ->B]",
      "(A,B)[A-> B]",
      "(A,B)[A->B ]"
    ).foreach(input => {
      describe(s"when given two features in a single group with a single dependency between them in the format '$input'") {
        it("should return those two features and the dependency") {
          val actual = parser.parse(input)

          val expectedFeatures = Set(
            Feature("A", "1"),
            Feature("B", "1")
          )

          actual.features should contain theSameElementsAs expectedFeatures

          val featureA = actual.features.find(f => f.name == "A").get
          val featureB = actual.features.find(f => f.name == "B").get

          val expectedDependencies = Map(
            featureA -> Set(featureB),
            featureB -> Set()
          )

          actual.dependencies should contain theSameElementsAs expectedDependencies
        }
      }
    })

    Set(
      "(A,B,C)[A->B,B->C]",
      "(A,B,C)[A->B ,B->C]",
      "(A,B,C)[A->B, B->C]"
    ).foreach(input => {
      describe(s"when given three features and two dependencies between them in a single group in the format '$input'") {
        it("should return those three features and the dependencies") {
          val actual = parser.parse(input)

          val expectedFeatures = Set(
            Feature("A", "1"),
            Feature("B", "1"),
            Feature("C", "1")
          )

          actual.features should contain theSameElementsAs expectedFeatures

          val featureA = actual.features.find(f => f.name == "A").get
          val featureB = actual.features.find(f => f.name == "B").get
          val featureC = actual.features.find(f => f.name == "C").get

          val expectedDependencies = Map(
            featureA -> Set(featureB),
            featureB -> Set(featureC),
            featureC -> Set()
          )

          actual.dependencies should contain theSameElementsAs expectedDependencies
        }
      }
    })

    describe(s"when given two features in individual groups with a single dependency between them referencing a feature in a future group") {
      it("should return those two features and the dependency") {
        val actual = parser.parse("(A)[A->B] (B)[]")

        val expectedFeatures = Set(
          Feature("A", "1"),
          Feature("B", "2")
        )

        actual.features should contain theSameElementsAs expectedFeatures

        val featureA = actual.features.find(f => f.name == "A").get
        val featureB = actual.features.find(f => f.name == "B").get

        val expectedDependencies = Map(
          featureA -> Set(featureB),
          featureB -> Set()
        )

        actual.dependencies should contain theSameElementsAs expectedDependencies
      }
    }

    describe(s"when given two features in individual groups with a single dependency between them referencing a feature in a previous group") {
      it("should return those two features and the dependency") {
        val actual = parser.parse("(B)[]     (A)[A->B]")

        val expectedFeatures = Set(
          Feature("A", "2"),
          Feature("B", "1")
        )

        actual.features should contain theSameElementsAs expectedFeatures

        val featureA = actual.features.find(f => f.name == "A").get
        val featureB = actual.features.find(f => f.name == "B").get

        val expectedDependencies = Map(
          featureA -> Set(featureB),
          featureB -> Set()
        )

        actual.dependencies should contain theSameElementsAs expectedDependencies
      }
    }

    describe(s"when a dependency references a source feature that does not exist") {
      it("should throw an exception") {
        val exception = the [InvalidFeatureGraphException] thrownBy parser.parse("(A,B)[C->B]")

        exception.message should equal ("The feature 'C' referenced by a dependency in group 1 is not a member of that group.")
      }
    }

    describe(s"when a dependency references a source feature that is not in that group") {
      it("should throw an exception") {
        val exception = the [InvalidFeatureGraphException] thrownBy parser.parse("(A,B)[C->B](C)[]")

        exception.message should equal ("The feature 'C' referenced by a dependency in group 1 is not a member of that group.")
      }
    }

    describe(s"when a dependency references a dependent feature that does not exist") {
      it("should throw an exception") {
        val exception = the [InvalidFeatureGraphException] thrownBy parser.parse("(A,B)[B->C]")

        exception.message should equal ("The feature 'C' referenced by a dependency in group 1 does not exist.")
      }
    }
  }

  private def beEmptyDependencyMapFor(features: Set[Feature]) = new BeEmptyDependencyMapForMatcher(features)

  private class BeEmptyDependencyMapForMatcher(features: Set[Feature]) extends Matcher[Map[Feature, Set[Feature]]] {
    override def apply(left: Map[Feature, Set[Feature]]): MatchResult = {
      val allFeaturesPresent = left.keySet == features
      val allFeaturesHaveNoDependencies = left.values.forall(dependencyList => dependencyList.isEmpty)

      MatchResult(
        allFeaturesPresent && allFeaturesHaveNoDependencies,
        s"Dependency map $left either did not contain exactly the features $features, or had one or more features with dependencies. ($allFeaturesPresent, $allFeaturesHaveNoDependencies)",
        s"Dependency map $left contained exactly the features $features and all features had no dependencies.")
    }
  }
}
