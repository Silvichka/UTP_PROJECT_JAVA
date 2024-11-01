package Tests;


import main.Board;
import main.Cell;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

//-Djava.library.path=/Users/silvia/CProjects/Project/cmake-build-debug
public class Tests {

    static{
        System.load("/Users/silvia/CProjects/Project/cmake-build-debug/libProject.dylib");
    }

    private Board board;

    @BeforeEach
    void init(){
        board = new Board();
    }

    @Test
    void testInitializeBoardNotNull() {
        Cell[][] cells = board.displayBoardTest();

        for(Cell[] ver : cells){
            for(Cell hor : ver){
                Assertions.assertNotNull(hor);
            }
        }
    }

    @Test
    void testPlacingPieces() {
        Cell[][] initialBoard = board.displayBoardTest();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 1) {
                    Assertions.assertEquals('B', initialBoard[row][col].getColor(), "Black piece should be in initial position");
                }
            }
        }

        for (int row = 5; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 1) {
                    Assertions.assertEquals('W', initialBoard[row][col].getColor(), "White piece should be in initial position");
                }
            }
        }
    }

    @Test
    void testIsValidPieceToSelect() {

        Assertions.assertTrue(board.isValidPieceToSelect(5, 0));
        Assertions.assertFalse(board.isValidPieceToSelect(2, 1));
    }

    @Test
    void movingPiecesHorizontallyVerticallyForWhite(){

        HashMap<String, int[][]> predicts = board.predictedMoves(5,2);

        board.setSelectedButton(board.displayBoardTest()[5][2]);
        board.movePiece(board.displayBoardTest()[5][1], predicts);

        board.setSelectedButton(board.displayBoardTest()[5][2]);
        board.movePiece(board.displayBoardTest()[4][2], predicts);

        Assertions.assertAll(
                ()->assertEquals('.', board.displayBoardTest()[5][1].getColor()),
                ()->assertEquals('.', board.displayBoardTest()[4][2].getColor())


        );
    }

    @Test
    void movingPiecesHorizontallyVerticallyForBlack(){
        board.move(5,0,4,1,true);
        HashMap<String, int[][]> predicts = board.predictedMoves(2,1);

        //horizontally
        board.setSelectedButton(board.displayBoardTest()[2][1]);
        board.movePiece(board.displayBoardTest()[2][0], predicts);

        //vertically
        board.setSelectedButton(board.displayBoardTest()[2][1]);
        board.movePiece(board.displayBoardTest()[3][1], predicts);

        Assertions.assertAll(
                ()->assertEquals('.', board.displayBoardTest()[5][1].getColor()),
                ()->assertEquals('.', board.displayBoardTest()[4][2].getColor())


        );
    }

    @Test
    void testPredictedMovesForWhite() {
        HashMap<String, int[][]> moves = board.predictedMoves(5, 2);

        // Assuming typical opening moves, validate move options
        Assertions.assertTrue(moves.get("regular_moves").length == 2, "Piece at position [5][2] has two possible moves");
        Assertions.assertTrue(moves.get("captures").length == 0, "Piece at position [5][2] doesnt have capture moves");
    }

    @Test
    void testPredictedMovesForBlack() {
        board.move(5,0,4,1,true);
        HashMap<String, int[][]> moves = board.predictedMoves(2, 1);

        // Assuming typical opening moves, validate move options
        Assertions.assertTrue(moves.get("regular_moves").length == 2, "Piece at position [5][2] has two possible moves");
        Assertions.assertTrue(moves.get("captures").length == 0, "Piece at position [5][2] doesnt have capture moves");
    }

    @Test
    void checkingPositionsWhileBeating(){// W56 45  B25 34
        board.move(5,6,4,5,true);//white
        board.move(2,5,3,4,true);//black
        board.move(5,0,4,1,true);//white
        board.setSelectedButton(board.displayBoardTest()[3][4]);
        board.checkingCapturesRules(board.displayBoardTest()[5][6]);

        char[][] display = board.displaying();

        Assertions.assertAll(
                ()->assertEquals('B', display[5][6]),//checking position of black
                ()->assertEquals('.', display[4][5])//checking for removing white piece
        );
    }

    @Test
    void checkingPositionForMultipleCapturing(){
        board.move(5,2, 4,1,true);//white
        board.move(2,1,3,0,true);//black
        board.move(6,1,5,2,true);//white
        board.move(1,0,2,1,true);//black
        board.move(5,2,4,3,true);//white
        board.setSelectedButton(board.displayBoardTest()[3][0]);
        board.checkingCapturesRules(board.displayBoardTest()[5][2]);

        Assertions.assertTrue(board.captureRule(5, 3).length != 0);
        Assertions.assertFalse(board.getTurn());

    }

    @Test
    void checkingForFooki(){
        board.move(5,6,4,5,true);//white
        board.move(2,5,3,4,true);//black
        board.move(5,0,4,1,true);//white
        board.setSelectedButton(board.displayBoardTest()[3][4]);
        board.checkingCapturesRules(board.displayBoardTest()[4][3]);

        char[][] display = board.displaying();

        Assertions.assertAll(
                ()->assertEquals('.', display[4][3])//checking removing black, bc of fooki
        );

    }

    @Test
    void testWhiteWinsWhenNoBlackPiecesLeft() {
        for (Cell[] row : board.displayBoardTest()) {
            for (Cell cell : row) {
                if (cell.getColor() == 'B') {
                    cell.removeIcon();
                }
            }
        }

        assertEquals(2, board.winner());
    }

    @Test
    void testBlackWinsWhenNoWhitePiecesLeft() {
        for (Cell[] row : board.displayBoardTest()) {
            for (Cell cell : row) {
                if (cell.getColor() == 'W') {
                    cell.removeIcon();
                }
            }
        }

        assertEquals(1, board.winner());
    }

}
