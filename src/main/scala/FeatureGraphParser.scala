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

  private def processParseResult(groups: List[FeatureNameList]): Set[Feature] = {
    checkForDuplicateFeatureNames(groups.flatten)

    groups
      .zipWithIndex
      .flatMap { case (group, index) => processParsedGroup(group, (index + 1).toString) }
      .toSet
  }

  private def processParsedGroup(featureNames: FeatureNameList, groupName: String): Set[Feature] = {
    featureNames.map(featureName => Feature(featureName, groupName, Set())).toSet
  }

  private def checkForDuplicateFeatureNames(featureNames: FeatureNameList): Unit = {
    val duplicates = featureNames.groupBy(identity).collect { case (featureName, xs) if xs.size > 1 => featureName }

    duplicates.foreach(featureName => throw InvalidFeatureGraphException(s"The feature '$featureName' is repeated multiple times."))
  }

  override def skipWhitespace: Boolean = false

  type FeatureNameList = List[String]

  private def optionalWhiteSpace = """\s*""".r
  private def featureName: Parser[String] = optionalWhiteSpace ~> """([A-Za-z0-9_])+""".r <~ optionalWhiteSpace ^^ { _.toString }
  private def featureGroup: Parser[FeatureNameList] = optionalWhiteSpace ~> '(' ~> repsep(featureName, ',') <~ ')' <~ optionalWhiteSpace
  private def featureGroups: Parser[List[FeatureNameList]] = rep(featureGroup)
}
