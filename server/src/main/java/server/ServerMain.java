package server;

import chess.*;

public class ServerMain {
    public static void main(String[] args) {
        Server server = new Server();
        int port = server.run(8000);
        System.out.println("Server running on port " + port);
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
    }
}
