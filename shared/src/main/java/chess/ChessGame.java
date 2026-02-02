package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    public ChessGame() {
        board.resetBoard();
        teamTurn = teamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return getTeamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) return null;

        Collection<ChessMove> pieceMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> legal = new ArrayList<>();

        for (ChessMove move : pieceMoves){
            ChessBoard copy = copyBoard(board);
            applyMoveOnBoard(copy, move, piece.getTeamColor());
            if (!isInCheck(piece.getTeamColor(), copy)){
                legal.add(move);
            }
        }

        return legal;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (move == null) throw new InvalidMoveException("Move cannot be null");

        ChessPosition start = move.getStartPosition();
        ChessPiece piece = getPiece(start);
        if (piece == null) throw new InvalidMoveException("No piece at starting position");
        if (piece.getTeamColor() != teamTurn) throw new InvalidMoveException("Not your turn");

        Collection<ChessMove> legal = validMoves(start);
        if (legal == null || !legal.contains(move)){
            throw new InvalidMoveException("Illegal move");
        }

        applyMoveOnBoard(board, move, piece.getTeamColor());
        teamTurn() = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(teamColor, board);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) return false;

        for (int r = 1; r <= 8; r++){
            for (int c = 1; c<= 8; c++){
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece p = board.getPiece(pos);
                if (p == null) continue;
                if (p.getTeamColor() != teamColor) continue;

                Collection<ChessMove> legal = validMove(pos);
                if (legal != null && !legal.isEmpty()) return false;
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) return false;

        for (int r = 1; r <= 8; r++){
            for (int c = 1; c <= 8; c++){
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece p = new board.getPiece(pos);
                if (p == null) continue;
                if (p.getTeamColor() != teamColor) continue;

                Collection<ChessMove> legal = validMoves(pos);
                if (legal != null && !legal.isEmpty()) return false;
            }
        }
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    private boolean isInCheck(TeamColor teamColor, ChessBoard onBoard){
        ChessPosition kingPos = findKing(teamCOlor, onBoard);
        if(kingPos == null) return false;

        TeamColor  enemy = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor. WHITE;
        for (int r = 1; r <= 8; r++){
            for (int c = 1; c <= 8; c++){
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece p = onBoard.getPiece(pos);
                if (p == null) continue;
                if (p.getTeamColor() != enemy) continue;

                for (ChessMove m : p.pieceMoves(onBoard, pos)){
                    if (kingPos.equals(m.getEndPosition())){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private ChessPosition findKing(TeamColor color, ChessBoard onBoard){
        for (int r = 1; r <= 8; r++){
            for (int c = 1; c <= 8; c++){
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece p = onBoard.getPiece(pos);
                if (p == null) continue;
                if (p.getTeamColor() == color && p.getPieceType() == ChessPiece.PieceType.KING){
                    return pos;
                }
            }
        }
        return null;
    }


}
