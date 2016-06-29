import scala.None
import scala.util.parsing.combinator._
import scala.util.parsing.input.CharSequenceReader

class FeatureGraphParser extends RegexParsers {
  def parse(input: String): FeatureGraph = {
    val reader: CharSequenceReader = new CharSequenceReader(input)

    parseAll(featureGroups, reader) match {
      case Success(groups, _) => processParseResult(groups)
      case Failure(message, _) => throw FeatureGraphParseException(s"Could not parse graph: $message")
      case Error(message, _) => throw new Exception(s"Error during parsing: $message")
    }
  }

  private def processParseResult(groups: List[FeatureGroup]): FeatureGraph = {
    checkForDuplicateFeatureNames(groups.flatMap(g => g.featureNames))

    val namedGroups = groups
      .zipWithIndex
      .map { case (group, index) => ((index + 1).toString, group) }
      .toMap

    val features = namedGroups
      .flatMap { case (groupName, group) => createFeatures(group.featureNames, groupName) }
      .toSet

    val dependencies = createDependencies(namedGroups, features)

    FeatureGraph(features, dependencies)
  }

  private def checkForDuplicateFeatureNames(featureNames: FeatureNameList): Unit = {
    val duplicates = featureNames.groupBy(identity).collect { case (featureName, xs) if xs.size > 1 => featureName }

    duplicates.foreach(featureName => throw InvalidFeatureGraphException(s"The feature '$featureName' is repeated multiple times."))
  }

  private def createFeatures(featureNames: FeatureNameList, groupName: String): Iterable[Feature] = {
    featureNames.map(featureName => Feature(featureName, groupName))
  }

  private def createDependencies(namedGroups: Map[String, FeatureGroup], features: Set[Feature]): Map[Feature, Set[Feature]] = {
    val defaultValues = features.map(f => f -> Set[Feature]()).toMap
    val allDependencies = namedGroups.flatMap { case (groupName, group) => createDependencies(group, groupName, features) }

    defaultValues ++ dependencyListToGroupedMap(allDependencies)
  }

  private def createDependencies(group: FeatureGroup, groupName: String, features: Set[Feature]): Iterable[Tuple2[Feature, Feature]] = {
    group.dependencies.map(dep => {
      if (!group.featureNames.contains(dep.from)) {
        throw InvalidFeatureGraphException(s"The feature '${dep.from}' referenced by a dependency in group $groupName is not a member of that group.")
      }

      val resolvedFrom = features.find(f => f.name == dep.from).get

      features.find(f => f.name == dep.to) match {
        case Some(resolvedTo) => resolvedFrom -> resolvedTo
        case None => throw InvalidFeatureGraphException(s"The feature '${dep.to}' referenced by a dependency in group $groupName does not exist.")
      }
    })
  }

  private def dependencyListToGroupedMap(dependencyList: Iterable[Tuple2[Feature, Feature]]): Map[Feature, Set[Feature]] = {
    dependencyList.groupBy(_._1).mapValues(_.map(_._2).toSet)
  }

  private case class Dependency(from: String, to: String)
  private case class FeatureGroup(featureNames: FeatureNameList, dependencies: DependencyList)
  private type FeatureNameList = List[String]
  private type DependencyList = List[Dependency]

  override def skipWhitespace: Boolean = false
  private def optionalWhiteSpace = """\s*""".r

  private def featureName: Parser[String] = optionalWhiteSpace ~> """([A-Za-z0-9_])+""".r <~ optionalWhiteSpace ^^ { _.toString }
  private def featureNameList: Parser[FeatureNameList] = optionalWhiteSpace ~> '(' ~> repsep(featureName, ',') <~ ')' <~ optionalWhiteSpace

  private def dependency: Parser[Dependency] = featureName ~ ("->" ~> featureName) ^^ { case from ~ to => Dependency(from, to) }
  private def dependencyList: Parser[DependencyList] = '[' ~> optionalWhiteSpace ~> repsep(dependency, ',') <~ ']'

  private def featureGroup: Parser[FeatureGroup] = featureNameList ~ opt(dependencyList) ^^
    { case featureNames ~ dependencies => FeatureGroup(featureNames, dependencies.getOrElse(List())) }

  private def featureGroups: Parser[List[FeatureGroup]] = rep(featureGroup)
}
