package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import ui.EscapeSequences;
import java.util.Collections;
import java.util.Set;

public class BoardPrinter {
    public static void printBoard(ChessGame game, boolean whitePerspective){
        //this will print the board highlighted.
        printBoardWithHighlights(game, whitePerspective, Collections.emptySet());
    }
    public static void printBoardWithHighlights(ChessGame game, boolean whitePerspective, Set<ChessPosition> highlights) {
        ChessBoard board = game.getBoard();
        System.out.println();

        if (whitePerspective) {
            printColumnLabelsWhite();
            for (int row = 8; row >= 1; row--) {
                printRow(board, row, true, highlights);
            }
            printColumnLabelsWhite();
        } else {
            printColumnLabelsBlack();
            for (int row = 1; row <= 8; row++) {
                printRow(board, row, false, highlights);
            }
            printColumnLabelsBlack();
        }
        System.out.println(EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR);
    }

    private static void printRow(ChessBoard board, int row, boolean whitePerspective, Set<ChessPosition> highlights) {
        System.out.print(EscapeSequences.SET_BG_COLOR_WHITE);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        System.out.print(" " + row + " ");

        if (whitePerspective) {
            for (int col = 1; col <= 8; col++) {
                printSquare(board, row, col, highlights);
            }
        } else {
            for (int col = 8; col >= 1; col--) {
                printSquare(board, row, col, highlights);
            }
        }
        System.out.print(EscapeSequences.SET_BG_COLOR_WHITE);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        System.out.print(" " + row + " ");
        System.out.println();
    }

    private static void printSquare(ChessBoard board, int row, int col, Set<ChessPosition> highlights) {
        ChessPosition position = new ChessPosition(row, col);
        boolean highlighted = highlights.contains(position);
        boolean lightSquare = (row + col) % 2 == 0;

        if (highlighted) {
            System.out.print(EscapeSequences.SET_BG_COLOR_YELLOW);
        } else if (lightSquare) {
            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        } else {
            System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN);
        }

        ChessPiece piece = board.getPiece(position);

        if (piece == null) {
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);
            System.out.print(EscapeSequences.EMPTY);
            return;
        }

        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
        } else {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        }

        System.out.print(getPieceSymbol(piece));
    }

    private static String getPieceSymbol(ChessPiece piece){
        return switch (piece.getPieceType()){
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
        };
    }

    private static void printColumnLabelsWhite(){
        System.out.print(EscapeSequences.SET_BG_COLOR_WHITE);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        System.out.println("   a   b   c   d   e   f   g   h   ");
    }

    private static void printColumnLabelsBlack(){
        System.out.print(EscapeSequences.SET_BG_COLOR_WHITE);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        System.out.println("   h   g   f   e   d   c   b   a   ");
    }
}