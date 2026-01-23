package chess;
import java.util.Arrays;
import java.util.Objects;
/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {


    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow()-1][position.getColumn()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {

        return squares[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        //White pieces set
        squares = new ChessPiece[8][8];
        place(1, 1, ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        place(1, 2, ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        place(1, 3, ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        place(1, 4, ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        place(1, 5, ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        place(1, 6, ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        place(1, 7, ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        place(1, 8, ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);

        for (int col = 1; col <= 8; col++){
            place(2, col, ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        }

        //black pieces set
        for (int col = 1; col <= 8; col++){
            place(7, col, ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        }
        place(8, 1, ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        place(8, 2, ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        place(8, 3, ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        place(8, 4, ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
        place(8, 5, ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
        place(8, 6, ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        place(8, 7, ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        place(8, 8, ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);



    }
}
