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

            case KNIGHT -> addKnightMoves(board, myPosition, moves);
            case KING -> addKingMoves(board, myPosition, moves);
            case PAWN -> addPawnMoves(board, myPosition, moves);





            default -> { }
        }

        return moves;
    }

    private void addPawnMoves(ChessBoard board, ChessPosition start, Collection<ChessMove> moves){
        int direction;
        int startRow;

        if (this.getTeamColor() == ChessGame.TeamColor.WHITE){
            direction =1;
            startRow= 2;
        } else {
            direction = -1;
            startRow = 7;
        }
        int r = start.getRow();
        int c = start.getColumn();

        ChessPosition oneForward = new ChessPosition(r+ direction, c);
        if (inBounds(oneForward) && board.getPiece(oneForward) == null){
           addPawnMoveOrPromotions(start, oneForward, moves);

            ChessPosition twoForward = new ChessPosition(r+2 * direction, c);
            if(r == startRow && inBounds(twoForward) && board.getPiece(twoForward) == null){
                addPawnMoveOrPromotions(start, twoForward, moves);
            }
        }

        ChessPosition diagLeft= new ChessPosition(r +direction, c-1);
        if (inBounds(diagLeft)){
            ChessPiece target = board.getPiece(diagLeft);
            if (target != null && target.getTeamColor() !=this.getTeamColor()){
                addPawnMoveOrPromotions(start, diagLeft, moves);
            }
        }

        ChessPosition diagRight = new ChessPosition(r +direction, c+1);
        if (inBounds(diagRight)){
            ChessPiece target = board.getPiece(diagRight);
            if(target != null && target.getTeamColor()!= this.getTeamColor()){
                addPawnMoveOrPromotions(start, diagRight, moves);
            }
        }


    }
    private void addPawnMoveOrPromotions(ChessPosition start, ChessPosition end, Collection<ChessMove> moves){
        int endRow = end.getRow();
        if( endRow == 8 || endRow == 1){
            moves.add(new ChessMove(start, end, PieceType.QUEEN));
            moves.add(new ChessMove(start, end, PieceType.ROOK));
            moves.add(new ChessMove(start, end, PieceType.BISHOP));
            moves.add(new ChessMove(start, end, PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(start, end, null));
        }
    }
    private boolean inBounds(ChessPosition pos){
        return pos.getRow() >= 1 && pos.getRow() <= 8 && pos.getColumn() >= 1 &&pos.getColumn()<= 8;
    }

    private void addKnightMoves(ChessBoard board, ChessPosition start, Collection<ChessMove> moves){
        int[][] offsets = {
                {2, 1}, {2, -1},
                {-2, 1}, {-2, -1},
                {1, 2}, {1, -2},
                {-1, 2}, {-1, -2}
        };
        for (int[] off : offsets) {
            int r = start.getRow() + off[0];
            int c = start.getColumn() + off[1];

            if ( r < 1 || r > 8 || c < 1 || c > 8){
                continue;
            }
            ChessPosition next = new ChessPosition(r, c);
            ChessPiece onSquare = board.getPiece(next);

            if (onSquare == null || onSquare.getTeamColor() != this.getTeamColor()){
                moves.add(new ChessMove(start, next, null));
            }
        }

    }
    private void addKingMoves(ChessBoard board, ChessPosition start, Collection<ChessMove> moves) {
        int[][] offsets = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] off : offsets) {
            int r = start.getRow() + off[0];
            int c = start.getColumn() + off[1];
            if (r < 1 || r > 8 || c < 1 || c > 8) {
                continue;
            }

            ChessPosition next = new ChessPosition(r, c);
            ChessPiece onSquare = board.getPiece(next);

            if (onSquare == null || onSquare.getTeamColor() != this.getTeamColor()) {
                moves.add(new ChessMove(start, next, null));
            }
        }
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
                    break; //stop after hitting into a piece
                }

                r += dir[0];
                c += dir[1];
            }
        }
    }


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
