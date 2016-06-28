import org.scalatest.{FunSpec, Matchers}

class FeatureGraphParserTests extends FunSpec with Matchers {
  describe("A FeatureGraphParser") {
    val parser = new FeatureGraphParser()

    describe("when given an empty string to parse") {
      it("should return an empty feature graph") {
        val result = parser.parse("")

        result shouldBe empty
      }
    }

    describe("when given a single feature in a single group") {
      it("should return that feature") {
        val actual = parser.parse("(A)")
        val expected = Set(Feature("A", "1", Set()))

        actual should contain theSameElementsAs expected
      }
    }

    Set(
      "(A,B)",
      "(A, B)",
      "(A,  B)",
      "(A, B )",
      "( A, B)",
      "( A,B)"
    ).foreach(input => {
      describe(s"when given two features in a single group in the format '$input'") {
        it("should return those two features") {
          val actual = parser.parse(input)

          val expected = Set(
            Feature("A", "1", Set()),
            Feature("B", "1", Set())
          )

          actual should contain theSameElementsAs expected
        }
      }
    })

    describe("when given three features in a single group") {
      it("should return those three features") {
        val actual = parser.parse("(A,B,C)")

        val expected = Set(
          Feature("A", "1", Set()),
          Feature("B", "1", Set()),
          Feature("C", "1", Set())
        )

        actual should contain theSameElementsAs expected
      }
    }

    Set(
      "(A)(B)",
      "(A) (B)",
      "(A)\n(B)"
    ).foreach(input => {
      describe(s"when given two features in individual groups in the format '$input'") {
        it("should return those two features") {
          val actual = parser.parse(input)

          val expected = Set(
            Feature("A", "1", Set()),
            Feature("B", "2", Set())
          )

          actual should contain theSameElementsAs expected
        }
      }
    })

    describe("when given features with special characters in the name") {
      it("should return those features") {
        val actual = parser.parse("(AB_12,_123,1,ab)")

        val expected = Set(
          Feature("AB_12", "1", Set()),
          Feature("_123", "1", Set()),
          Feature("1", "1", Set()),
          Feature("ab", "1", Set())
        )

        actual should contain theSameElementsAs expected
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
  }
}
