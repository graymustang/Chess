package chess;

import java.util.Collection;
import java.util.HashSet;
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

    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    public PieceType getPieceType() {
        return type;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();

        switch (type) {
            case BISHOP -> addSlidingMoves(board, myPosition, moves, new int[][]{
                    {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
            });
            case ROOK -> addSlidingMoves(board, myPosition, moves, new int[][]{
                    {1, 0}, {-1, 0}, {0, 1}, {0, -1}
            });
            case QUEEN -> addSlidingMoves(board, myPosition, moves, new int[][]{
                    {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
                    {1, 0}, {-1, 0}, {0, 1}, {0, -1}
            });
            case KNIGHT -> addKnightMoves(board, myPosition, moves);
            case KING -> addKingMoves(board, myPosition, moves);
            case PAWN -> addPawnMoves(board, myPosition, moves);
        }

        return moves;
    }

    private boolean inBounds(int r, int c) {
        return r >= 1 && r <= 8 && c >= 1 && c <= 8;
    }

    private void addMoveIfOk(ChessBoard board, ChessPosition start, int r, int c, Collection<ChessMove> moves) {
        if (!inBounds(r, c)) return;

        ChessPosition end = new ChessPosition(r, c);
        ChessPiece target = board.getPiece(end);

        if (target == null) {
            moves.add(new ChessMove(start, end, null));
        } else if (target.getTeamColor() != this.getTeamColor()) {
            moves.add(new ChessMove(start, end, null));
        }
    }

    private void addSlidingMoves(ChessBoard board, ChessPosition start, Collection<ChessMove> moves, int[][] dirs) {
        int sr = start.getRow();
        int sc = start.getColumn();

        for (int[] d : dirs) {
            int r = sr + d[0];
            int c = sc + d[1];

            while (inBounds(r, c)) {
                ChessPosition end = new ChessPosition(r, c);
                ChessPiece target = board.getPiece(end);

                if (target == null) {
                    moves.add(new ChessMove(start, end, null));
                } else {
                    if (target.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(start, end, null));
                    }
                    break; // blocked
                }

                r += d[0];
                c += d[1];
            }
        }
    }

    private void addKnightMoves(ChessBoard board, ChessPosition start, Collection<ChessMove> moves) {
        int[][] offsets = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };

        int sr = start.getRow();
        int sc = start.getColumn();

        for (int[] o : offsets) {
            addMoveIfOk(board, start, sr + o[0], sc + o[1], moves);
        }
    }

    private void addKingMoves(ChessBoard board, ChessPosition start, Collection<ChessMove> moves) {
        int sr = start.getRow();
        int sc = start.getColumn();

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                addMoveIfOk(board, start, sr + dr, sc + dc, moves);
            }
        }
    }

    private void addPawnMoves(ChessBoard board, ChessPosition start, Collection<ChessMove> moves) {
        int direction;
        int startRow;

        if (this.getTeamColor() == ChessGame.TeamColor.WHITE) {
            direction = 1;
            startRow = 2;
        } else {
            direction = -1;
            startRow = 7;
        }

        int r = start.getRow();
        int c = start.getColumn();

        // 1 forward
        int oneR = r + direction;
        if (inBounds(oneR, c)) {
            ChessPosition oneForward = new ChessPosition(oneR, c);
            if (board.getPiece(oneForward) == null) {
                addPawnMoveOrPromotions(start, oneForward, moves);

                // 2 forward from start row (only if 1 forward was clear)
                int twoR = r + 2 * direction;
                if (r == startRow && inBounds(twoR, c)) {
                    ChessPosition twoForward = new ChessPosition(twoR, c);
                    if (board.getPiece(twoForward) == null) {
                        moves.add(new ChessMove(start, twoForward, null));
                    }
                }
            }
        }

        // capture diag left
        int dlR = r + direction;
        int dlC = c - 1;
        if (inBounds(dlR, dlC)) {
            ChessPosition diagLeft = new ChessPosition(dlR, dlC);
            ChessPiece target = board.getPiece(diagLeft);
            if (target != null && target.getTeamColor() != this.getTeamColor()) {
                addPawnMoveOrPromotions(start, diagLeft, moves);
            }
        }

        // capture diag right
        int drR = r + direction;
        int drC = c + 1;
        if (inBounds(drR, drC)) {
            ChessPosition diagRight = new ChessPosition(drR, drC);
            ChessPiece target = board.getPiece(diagRight);
            if (target != null && target.getTeamColor() != this.getTeamColor()) {
                addPawnMoveOrPromotions(start, diagRight, moves);
            }
        }
    }

    private void addPawnMoveOrPromotions(ChessPosition start, ChessPosition end, Collection<ChessMove> moves) {
        int endRow = end.getRow();

        // promotion rank
        if (endRow == 8 || endRow == 1) {
            moves.add(new ChessMove(start, end, PieceType.QUEEN));
            moves.add(new ChessMove(start, end, PieceType.ROOK));
            moves.add(new ChessMove(start, end, PieceType.BISHOP));
            moves.add(new ChessMove(start, end, PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(start, end, null));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
