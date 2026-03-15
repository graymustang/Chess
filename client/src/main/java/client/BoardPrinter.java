package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import ui.EscapeSequences;

public class BoardPrinter {
    public static void printBoard(ChessGame game, boolean whitePerspective){
        ChessBoard board = game.getBoard();
        System.out.println();

        if(whitePerspective){
            printColumnLabelsWhite();
            for (int row = 8; row >= 1; row--){
                printRow(board, row, true);
            }
            printColumnLabelsWhite();
        } else {
            printColumnLabelsBlack();
            for (int row = 1; row <= 8; row++){
                printRow(board, row, false);
            }
            printColumnLabelsBlack();
        }
        System.out.println(EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR);
    }

    private static void printRow(ChessBoard board, int row, boolean whitePerspective){
        System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        System.out.print(" " + row + " ");

        if(whitePerspective){
            for (int col = 1; col <= 8; col++){
                printSquare(board, row, col);
            }
        } else {
            for (int col = 8; col >= 1; col--){
                printSquare(board, row, col);
            }
        }
        System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        System.out.print(" " + row + " ");
        System.out.println();

    }

    private static void printSquare(ChessBoard board, int row, int col){
        boolean lightsquare = (row + col) % 2 == 0;

        if (lightsquare){
            System.out.print(EscapeSequences.SET_BG_COLOR_WHITE);
        } else{
            System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN);
        }

        ChessPiece piece = board.getPiece(new ChessPosition(row, col));

        if (piece == null) {
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);
            System.out.print("   ");
            return;
        }

        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE){
            System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
        } else{
            System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
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
        System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        System.out.println("   a  b  c  d  e  f  g  h   ");
    }

    private static void printColumnLabelsBlack(){
        System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        System.out.println("   h  g  f  e  d  c  b  a   ");
    }
}