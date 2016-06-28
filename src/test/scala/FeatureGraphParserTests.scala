import org.scalatest.{FunSpec, Matchers}

class FeatureGraphParserTests extends FunSpec with Matchers {
  describe("A FeatureGraphParser") {
    describe("when given an empty string to parse") {
      it("should return an empty feature graph") {
        val result = FeatureGraphParser.parse("")

        result shouldBe empty
      }
    }
  }
}
