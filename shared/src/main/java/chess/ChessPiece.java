package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        switch (getPieceType()) {
            case BISHOP -> addSlidingMoves(board, myPosition, moves, new int[][]{
                    {1,  1},
                    { 1, -1},
                    {-1,1},
                    {-1,-1}
            });

            case QUEEN -> addSlidingMoves(board, myPosition, moves, new int[][]{
                    {1, 0},
                    {-1, 0},
                    {0, 1},
                    {0, -1},
                    {1, 1},
                    {1, -1},
                    {-1, 1},
                    {-1, -1}
            });

            case ROOK -> addSlidingMoves(board, myPosition, moves, new int[][]{
                    {1, 0},
                    {-1, 0},
                    {0, 1},
                    {0, -1}
            });

            //Add other pieces later
            default -> { }
        }

        return moves;
    }

    private void addSlidingMoves(ChessBoard board, ChessPosition start, Collection<ChessMove> moves, int[][] directions) {
        for (int[] dir : directions) {
            int r = start.getRow() + dir[0];
            int c = start.getColumn() + dir[1];

            while (r >= 1 && r <= 8 && c >= 1 && c <= 8) {
                ChessPosition next = new ChessPosition(r, c);
                ChessPiece onSquare = board.getPiece(next);

                if (onSquare == null) {
                    moves.add(new ChessMove(start, next, null));
                } else {
                    if (onSquare.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(start, next, null));
                    }
                    break; // stop after hitting any piece
                }

                r += dir[0];
                c += dir[1];
            }
        }
    }

    // These often get tested too, so it’s good to add them now.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessPiece)) return false;
        ChessPiece chessPiece = (ChessPiece) o;
        return pieceColor == chessPiece.pieceColor && type == chessPiece.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
