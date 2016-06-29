# feature-graph

The June Shokunin challenge.

## Assumptions

* Feature names can only contain:
  * lowercase or uppercase letters A-Z
  * digits 0-9
  * underscores
* Feature names are case-sensitive

## Notes

The graph parsing code makes use of Parser Combinators ([Wikipedia](https://en.wikipedia.org/wiki/Parser_combinators)).
There's more information on the Scala implementation I've used at:

* [https://wiki.scala-lang.org/display/SW/Parser+Combinators--Getting+Started](https://wiki.scala-lang.org/display/SW/Parser+Combinators--Getting+Started)
* [http://henkelmann.eu/2011/01/13/an_introduction_to_scala_parser_combinators](http://henkelmann.eu/2011/01/13/an_introduction_to_scala_parser_combinators)
