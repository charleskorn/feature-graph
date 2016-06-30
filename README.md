# feature-graph

The June Shokunin challenge.

## Prerequisites

* Docker (I've tested it with the Docker for Mac beta, other things should work)

The first time a build command is run using the `./go.sh` script, the appropriate Docker container
will be pulled down and set up, and then half the internet downloaded for Scala.
(This can take some time, but should be near-instant on subsequent runs.)

## Testing

Run `./go.sh test` to build and run the tests.

## Building

Run `./go.sh build` to build the application.

## Running

Run `./go.sh run <input>` to build and run the application.
The input should be provided in the format provided in the problem statement, for example:

    ./go.sh run "(A,B,C,G,H)[G->A,H->A,H->B] (D,E,F,I,J)[I->D,I->E,J->F,J->I,I->H]"

Note that you'll need to enclose the input in quotation marks, otherwise your shell may try
to interpret it rather than just passing it through as-is.

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
