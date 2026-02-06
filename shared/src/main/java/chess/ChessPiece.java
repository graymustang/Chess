package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ChessPiece {

    public enum PieceType {
        KING, QUEEN, BISHOP, KNIGHT, ROOK, PAWN
    }

    private final ChessGame.TeamColor teamColor;
    private final PieceType pieceType;

    public ChessPiece(ChessGame.TeamColor teamColor, PieceType type) {
        this.teamColor = teamColor;
        this.pieceType = type;
    }

    public ChessGame.TeamColor getTeamColor() {
        return teamColor;
    }

    public PieceType getPieceType() {
        return pieceType;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition start) {
        if (board == null || start == null || !inBounds(start)) return new ArrayList<>();



        Collection<ChessMove> moves = new ArrayList<>();

        switch (pieceType) {
            case KING:
                addKingMoves(board, start, moves);
                break;
            case QUEEN:
                addSlidingMoves(board, start, moves, new int[][]{{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}});
                break;
            case ROOK:
                addSlidingMoves(board, start, moves, new int[][]{{1,0},{-1,0},{0,1},{0,-1}});
                break;
            case BISHOP:
                addSlidingMoves(board, start, moves, new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}});
                break;
            case KNIGHT:
                addKnightMoves(board, start, moves);
                break;
            case PAWN:
                addPawnMoves(board, start, moves);
                break;
        }

        return moves;
    }

    private void addKingMoves(ChessBoard board, ChessPosition start, Collection<ChessMove> moves) {
        int r = start.getRow();
        int c = start.getColumn();
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                addMoveIfOk(board, start, r + dr, c + dc, moves);
            }
        }
    }

    private void addKnightMoves(ChessBoard board, ChessPosition start, Collection<ChessMove> moves) {
        int r = start.getRow();
        int c = start.getColumn();
        int[][] deltas = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};
        for (int[] d : deltas) {
            addMoveIfOk(board, start, r + d[0], c + d[1], moves);
        }
    }

    private void addSlidingMoves(ChessBoard board, ChessPosition start, Collection<ChessMove> moves, int[][] dirs) {
        int sr = start.getRow();
        int sc = start.getColumn();

        for (int[] dir : dirs) {
            int r = sr + dir[0];
            int c = sc + dir[1];

            while (r >= 1 && r <= 8 && c >= 1 && c <= 8) {
                ChessPosition next = new ChessPosition(r, c);
                ChessPiece onSquare = board.getPiece(next);

                if (onSquare == null) {
                    moves.add(new ChessMove(start, next, null));
                } else {
                    if (onSquare.teamColor != this.teamColor) {
                        moves.add(new ChessMove(start, next, null));
                    }
                    break; // blocked
                }

                r += dir[0];
                c += dir[1];
            }
        }
    }

    private void addPawnMoves(ChessBoard board, ChessPosition start, Collection<ChessMove> moves) {
        int r = start.getRow();
        int c = start.getColumn();

        int dir = (teamColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (teamColor == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promoteRow = (teamColor == ChessGame.TeamColor.WHITE) ? 8 : 1;

        // 1 forward
        ChessPosition oneForward = new ChessPosition(r + dir, c);
        if (inBounds(oneForward) && board.getPiece(oneForward) == null) {
            addPawnMoveWithPromotion(start, oneForward, promoteRow, moves);

            // 2 forward from start row
            ChessPosition twoForward = new ChessPosition(r + 2 * dir, c);
            if (r == startRow && inBounds(twoForward) && board.getPiece(twoForward) == null) {
                moves.add(new ChessMove(start, twoForward, null));
            }
        }

        // captures
        ChessPosition diagL = new ChessPosition(r + dir, c - 1);
        if (inBounds(diagL)) {
            ChessPiece p = board.getPiece(diagL);
            if (p != null && p.teamColor != this.teamColor) {
                addPawnMoveWithPromotion(start, diagL, promoteRow, moves);
            }
        }

        ChessPosition diagR = new ChessPosition(r + dir, c + 1);
        if (inBounds(diagR)) {
            ChessPiece p = board.getPiece(diagR);
            if (p != null && p.teamColor != this.teamColor) {
                addPawnMoveWithPromotion(start, diagR, promoteRow, moves);
            }
        }
    }

    private void addPawnMoveWithPromotion(ChessPosition start, ChessPosition end, int promoteRow, Collection<ChessMove> moves) {
        if (end.getRow() == promoteRow) {
            moves.add(new ChessMove(start, end, PieceType.QUEEN));
            moves.add(new ChessMove(start, end, PieceType.ROOK));
            moves.add(new ChessMove(start, end, PieceType.BISHOP));
            moves.add(new ChessMove(start, end, PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(start, end, null));
        }
    }

    private void addMoveIfOk(ChessBoard board, ChessPosition start, int r, int c, Collection<ChessMove> moves) {
        if (r < 1 || r > 8 || c < 1 || c > 8) return;

        ChessPosition end = new ChessPosition(r, c);
        ChessPiece onSquare = board.getPiece(end);

        if (onSquare == null) {
            moves.add(new ChessMove(start, end, null));
        } else if (onSquare.teamColor != this.teamColor) {
            moves.add(new ChessMove(start, end, null));
        }
    }

    private boolean inBounds(ChessPosition pos) {
        int r = pos.getRow();
        int c = pos.getColumn();
        return r >= 1 && r <= 8 && c >= 1 && c <= 8;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessPiece)) return false;
        ChessPiece chessPiece = (ChessPiece) o;
        return teamColor == chessPiece.teamColor && pieceType == chessPiece.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamColor, pieceType);
    }
}
