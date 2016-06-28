import scala.util.parsing.combinator._
import scala.util.parsing.input.CharSequenceReader

class FeatureGraphParser extends RegexParsers {
  def parse(input: String): Set[Feature] = {
    val reader: CharSequenceReader = new CharSequenceReader(input)

    parseAll(featureGroups, reader) match {
      case Success(groups, _) => processParseResult(groups)
      case Failure(message, _) => throw FeatureGraphParseException(s"Could not parse graph: $message")
      case Error(message, _) => throw new Exception(s"Error during parsing: $message")
    }
  }

  private def processParseResult(groups: List[FeatureNameSet]): Set[Feature] = {
    groups
      .zipWithIndex
      .flatMap { case (group, index) => processParsedGroup(group, (index + 1).toString) }
      .toSet
  }

  private def processParsedGroup(featureNames: FeatureNameSet, groupName: String): Set[Feature] = {
    featureNames.map(featureName => Feature(featureName, groupName, Set()))
  }

  override def skipWhitespace: Boolean = false

  type FeatureNameSet = Set[String]

  private def optionalWhiteSpace = """\s*""".r
  private def featureName: Parser[String] = optionalWhiteSpace ~> """([A-Za-z0-9_])+""".r <~ optionalWhiteSpace ^^ { _.toString }
  private def featureGroup: Parser[FeatureNameSet] = optionalWhiteSpace ~> '(' ~> repsep(featureName, ',') <~ ')' <~ optionalWhiteSpace ^^ { _.toSet }
  private def featureGroups: Parser[List[FeatureNameSet]] = rep(featureGroup)
}
