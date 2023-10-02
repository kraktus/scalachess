package chess

import scala.language.implicitConversions
import Square.*

class BoardTest extends ChessTest:

  val board = makeBoard

  "a board" should:

    "position pieces correctly" in:
      board.pieces must havePairs(
        A1 -> (White - Rook),
        B1 -> (White - Knight),
        C1 -> (White - Bishop),
        D1 -> (White - Queen),
        E1 -> (White - King),
        F1 -> (White - Bishop),
        G1 -> (White - Knight),
        H1 -> (White - Rook),
        A2 -> (White - Pawn),
        B2 -> (White - Pawn),
        C2 -> (White - Pawn),
        D2 -> (White - Pawn),
        E2 -> (White - Pawn),
        F2 -> (White - Pawn),
        G2 -> (White - Pawn),
        H2 -> (White - Pawn),
        A7 -> (Black - Pawn),
        B7 -> (Black - Pawn),
        C7 -> (Black - Pawn),
        D7 -> (Black - Pawn),
        E7 -> (Black - Pawn),
        F7 -> (Black - Pawn),
        G7 -> (Black - Pawn),
        H7 -> (Black - Pawn),
        A8 -> (Black - Rook),
        B8 -> (Black - Knight),
        C8 -> (Black - Bishop),
        D8 -> (Black - Queen),
        E8 -> (Black - King),
        F8 -> (Black - Bishop),
        G8 -> (Black - Knight),
        H8 -> (Black - Rook)
      )

    "have pieces by default" in:
      board.allPieces must not beEmpty

    "have castling rights by default" in:
      board.history.castles == Castles.init

    "allow a piece to be placed" in:
      board.place(White - Rook, E3) must beSome:
        (_: Board)(E3) mustEqual Some(White - Rook)

    "allow a piece to be taken" in:
      board take A1 must beSome:
        (_: Board)(A1) must beNone

    "allow a piece to move" in:
      board.move(E2, E4) must beSome:
        (_: Board)(E4) mustEqual Some(White - Pawn)

    "not allow an empty position to move" in:
      board.move(E5, E6) must beNone

    "not allow a piece to move to an occupied position" in:
      board.move(A1, A2) must beNone

    "allow chaining actions" in:
      makeEmptyBoard.seq(
        _.place(White - Pawn, A2),
        _.place(White - Pawn, A3),
        _.move(A2, A4)
      ) must beSome:
        (_: Board)(A4) mustEqual Some(White - Pawn)

    "fail on bad actions chain" in:
      makeEmptyBoard.seq(
        _.place(White - Pawn, A2),
        _.place(White - Pawn, A3),
        _.move(B2, B4)
      ) must beNone
