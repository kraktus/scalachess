package chess
package format.pgn

import cats.data.Validated

object Reader:

  sealed trait Result:
    def valid: Validated[String, Replay]

  object Result:
    case class Complete(replay: Replay) extends Result:
      def valid = Validated.valid(replay)
    case class Incomplete(replay: Replay, failure: String) extends Result:
      def valid = Validated.invalid(failure)

  def full(pgn: String, tags: Tags = Tags.empty): Validated[String, Result] =
    fullWithSans(pgn, identity, tags)

  def moves(sans: Iterable[SanStr], tags: Tags): Validated[String, Result] =
    movesWithSans(sans, identity, tags)

  def fullWithSans(pgn: String, op: Sans => Sans, tags: Tags = Tags.empty): Validated[String, Result] =
    Parser.full(cleanUserInput(pgn)) map { parsed =>
      makeReplay(makeGame(parsed.tags ++ tags), op(parsed.sans))
    }

  def fullWithSans(parsed: ParsedPgn, op: Sans => Sans): Result =
    makeReplay(makeGame(parsed.tags), op(parsed.sans))

  def movesWithSans(sans: Iterable[SanStr], op: Sans => Sans, tags: Tags): Validated[String, Result] =
    Parser.moves(sans) map { moves =>
      makeReplay(makeGame(tags), op(moves))
    }

  // remove invisible byte order mark
  def cleanUserInput(str: String) = str.replace(s"\ufeff", "")

  private def makeReplay(game: Game, sans: Sans): Result =
    // println(s"makeReplay $game $sans")
    sans.value.foldLeft[Result](Result.Complete(Replay(game))) {
      case (Result.Complete(replay), san) =>
        // println(s"replay ${replay.moves.size}")
        san(replay.state.situation).fold(
          err => Result.Incomplete(replay, err),
          move => Result.Complete(replay addMove move)
        )
      case (r: Result.Incomplete, _) => r
    }

  private def makeGame(tags: Tags) =
    val g = Game(
      variantOption = tags(_.Variant) flatMap chess.variant.Variant.byName,
      fen = tags.fen
    )
    g.copy(
      startedAtPly = g.ply,
      clock = tags.clockConfig map Clock.apply
    )
