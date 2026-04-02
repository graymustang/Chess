package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private boolean gameOver = false;
    private ChessBoard board;
    private TeamColor teamTurn;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
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
        if (piece == null) {
            return null;
        }

        Collection<ChessMove> rawMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> legalMoves = new ArrayList<>();

        for (ChessMove m : rawMoves) {
            ChessBoard copy = deepCopyBoard(board);
            applyMove(copy, m);
            if (!isInCheckOnBoard(copy, piece.getTeamColor())) {
                legalMoves.add(m);
            }
        }

        return legalMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (gameOver){
            throw new InvalidMoveException("Game is over");
        }
        if (move == null) {
            throw new InvalidMoveException("Move cannot be null");
        }

        ChessPosition start = move.getStartPosition();
        ChessPiece piece = board.getPiece(start);

        if (piece == null) {
            throw new InvalidMoveException("No piece at start position");
        }

        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("Not your turn");
        }

        Collection<ChessMove> legal = validMoves(start);
        boolean found = false;
        if (legal != null) {
            for (ChessMove m : legal) {
                if (m.equals(move)) {
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            throw new InvalidMoveException("Illegal move");
        }
        applyMove(board, move);
        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheckOnBoard(board, teamColor);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && !hasAnyLegalMove(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && !hasAnyLegalMove(teamColor);
    }

    private boolean hasAnyLegalMove(TeamColor teamColor){
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece piece = board.getPiece(pos);

                if (piece == null){
                    continue;
                }
                if(piece.getTeamColor() != teamColor){
                    continue;
                }

                Collection<ChessMove> moves = validMoves(pos);
                if (moves != null && !moves.isEmpty()){
                    return true;
                }
            }
        }
        return false;
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


    private boolean isInCheckOnBoard(ChessBoard b, TeamColor teamColor) {
        ChessPosition kingPos = findKing(b, teamColor);
        if (kingPos == null) {
            return false;
        }

        TeamColor enemy = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition from = new ChessPosition(r, c);
                ChessPiece p = b.getPiece(from);
                if (p == null || p.getTeamColor() != enemy) {
                    continue;
                }
                if (p.getPieceType() == ChessPiece.PieceType.PAWN) {
                    int dir = (enemy == TeamColor.WHITE) ? 1 : -1;
                    ChessPosition a1 = new ChessPosition(r + dir, c - 1);
                    ChessPosition a2 = new ChessPosition(r + dir, c + 1);
                    if (inBounds(a1) && a1.equals(kingPos)) {
                        return true;
                    }
                    if (inBounds(a2) && a2.equals(kingPos)) {
                        return true;
                    }
                    continue;
                }

                Collection<ChessMove> moves = p.pieceMoves(b, from);
                for (ChessMove m : moves) {
                    if (m.getEndPosition().equals(kingPos)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private ChessPosition findKing(ChessBoard b, TeamColor teamColor) {
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece p = b.getPiece(pos);
                if (p != null && p.getTeamColor() == teamColor && p.getPieceType() == ChessPiece.PieceType.KING) {
                    return pos;
                }
            }
        }
        return null;
    }


    private void applyMove(ChessBoard b, ChessMove move) {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();

        ChessPiece moving = b.getPiece(start);
        b.addPiece(start, null);
        if (move.getPromotionPiece() != null) {
            moving = new ChessPiece(moving.getTeamColor(), move.getPromotionPiece());
        }
        b.addPiece(end, moving);
    }

    private ChessBoard deepCopyBoard(ChessBoard original) {
        ChessBoard copy = new ChessBoard();
        copy.squares = new ChessPiece[8][8];

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                ChessPiece p = original.squares[r][c];
                if (p == null) {
                    copy.squares[r][c] = null;
                } else {
                    copy.squares[r][c] = new ChessPiece(p.getTeamColor(), p.getPieceType());
                }
            }
        }

        return copy;
    }

    private boolean inBounds(ChessPosition pos) {
        int r = pos.getRow();
        int c = pos.getColumn();
        return r >= 1 && r <= 8 && c >= 1 && c <= 8;
    }

    public boolean isGameOver(){
        return gameOver;
    }

    public void setGameOver(boolean gameOver){
        this.gameOver = gameOver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ChessGame)) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && teamTurn == chessGame.teamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamTurn);
    }
}
