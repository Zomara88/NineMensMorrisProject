import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MinimaxOpeningImproved {
    public static int positionsEvaluated = 0; // Instantiate evaluated positions to 0

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Input format: java MinimaxOpeningImproved <input board file> <output board file> <depth>");
            return;
        }

        String inputPositions = args[0]; // Takes first argument as input board
        String outputPositions = args[1]; // Takes second argument as output board
        int depth = Integer.parseInt(args[2]); // Parses the third argument as int for depth

        BufferedReader reader = new BufferedReader(new FileReader(inputPositions)); // Instantiate new reader
        String boardPosition = reader.readLine(); // Reads first argument string as starting board position
        reader.close();

        Result bestMove = minimax(boardPosition.toCharArray(), depth, false); // Call minimax algorithm, assuming white makes the first move

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputPositions)); // Instantiate new writer
        writer.write(bestMove.board); // Outputs the result of minimax to the output board file
        writer.close();

        System.out.println("Board Position: " + bestMove.board);
        System.out.println("Positions evaluated by static estimation: " + bestMove.positions);
        System.out.println("MINIMAX estimate: " + bestMove.estimate);
    }

    public static Result minimax(char[] board, int depth, boolean isWhite) {
        // Returns a Result class containing current board, estimate, and number of evaluated positions
        if (depth == 0) {
            positionsEvaluated++; // If the depth is 0, static estimation only evaluates one board position
            return new Result(new String(board), staticEstimationOpening(board), positionsEvaluated);
        }

        List<char[]> possibleMoves; // Create an empty list of character arrays
        Result bestResult;
        if (isWhite) { // If it is white's turn
            possibleMoves = GenerateMovesOpening(board);
            bestResult = new Result(null, Integer.MIN_VALUE, 0); // Maximize by setting estimate to the smallest int
        }
        else { // If it is black's turn
            possibleMoves = GenerateMovesOpeningBlack(board);
            bestResult = new Result(null, Integer.MAX_VALUE, 0); // Minimize by setting estimate to the largest int
        }
        for (char[] move : possibleMoves) { // For each possible move
            Result eval = minimax(move, depth - 1, !isWhite); // Recursively evaluate the level above
            if ((isWhite && eval.estimate > bestResult.estimate) || (!isWhite && eval.estimate < bestResult.estimate)) {
                // If white, then choose the estimate larger than bestResult, or else choose the one that is smaller
                bestResult = new Result(new String(move), eval.estimate, eval.positions);
            }
        }
        return bestResult;
    }

    public static List<char[]> GenerateMovesOpening(char[] board) { // Call GenerateAdd for white's turn
        return GenerateAdd(board);
    }

    public static List<char[]> GenerateMovesOpeningBlack(char[] board) {
        // Generate moves for black by inverting the board colors and using the same GenerateMovesOpening function
        char[] invertedBoard = invertBoardColors(board);
        List<char[]> moves = GenerateMovesOpening(invertedBoard);
        List<char[]> blackMoves = new ArrayList<>();
        for (char[] move : moves) {
            blackMoves.add(invertBoardColors(move));
        }
        return blackMoves;
    }

    public static char[] invertBoardColors(char[] board) { // Replaces all 'W' with 'B' and 'B' with 'W'
        char[] invertedBoard = new char[board.length];
        for (int i = 0; i < board.length; i++) {
            if (board[i] == 'W') {
                invertedBoard[i] = 'B';
            } else if (board[i] == 'B') {
                invertedBoard[i] = 'W';
            } else {
                invertedBoard[i] = board[i];
            }
        }
        return invertedBoard;
    }

    public static List<char[]> GenerateAdd(char[] board) {
        List<char[]> L = new ArrayList<>(); // Creates an empty arraylist

        for (int location = 0; location < board.length; location++) { // For each board location
            if (board[location] == 'x') { // If empty, duplicate the board and place a white piece at the location
                char[] b = board.clone();
                b[location] = 'W';

                if (closeMill(location, b)) { // If a mill can be closed for location, call GenerateRemove
                    GenerateRemove(b, L);
                } else {
                    L.add(b); // Or else add the board to the output list
                }
            }
        }

        return L;
    }

    public static void GenerateRemove(char[] board, List<char[]> L) {
        boolean added = false;
        for (int location = 0; location < board.length; location++) { // For each location on the board
            if (board[location] == 'B') {
                if (!closeMill(location, board)) { // If the piece is black and no mills can be closed
                    char[] b = board.clone();
                    b[location] = 'x'; // Mark the board location as empty and add to output list
                    L.add(b);
                    added = true;
                }
            }
        }
        if (!added) {
            L.add(board); // If no positions were added (all black pieces are in mills), add the input board position to L.
        }
    }

    public static boolean closeMill(int j, char[] b) {
        char c = b[j];
        if (c == 'x') { // Check to see if the location is empty
            return false;
        }
        return switch (j) {
            case 0 -> // a0
                    (b[2] == c && b[4] == c); // the mill is a0, b1, c2
            case 1 -> // g0
                    (b[3] == c && b[5] == c) || (b[8] == c && b[17] == c); // mill is g0, f1, e2 OR g0, g3, g6
            case 2 -> // b1
                    (b[0] == c && b[4] == c); // the mill is a0, b1, c2
            case 3 -> // f1
                    (b[1] == c && b[5] == c) || (b[7] == c && b[14] == c); // mill is g0, f1, e2 OR f1, f3, f5
            case 4 -> // c2
                    (b[0] == c && b[2] == c); // the mill is a0, b1, c2
            case 5 -> // e2
                    (b[0] == c && b[3] == c) || (b[6] == c && b[11] == c); // mill is g0, f1, e2 OR e2, e3, e4
            case 6 -> // e3
                    (b[5] == c && b[11] == c) || (b[7] == c && b[8] == c); // mill is e2, e3, e4 OR e3, f3, g3
            case 7 -> // f3
                    (b[3] == c && b[14] == c) || (b[6] == c && b[8] == c); // mill is f1, f3, f5 OR e3, f3, g3
            case 8 -> // g3
                    (b[1] == c && b[17] == c) || (b[6] == c && b[7] == c); // mill is g0, g3, g6 OR e3, f3, g3
            case 9 -> // c4
                    (b[12] == c && b[15] == c) || (b[10] == c && b[11] == c); // mill is c4, b5, a6 OR c4, d4, e4
            case 10 -> // d4
                    (b[9] == c && b[11] == c) || (b[13] == c && b[16] == c); // mill is d4, c4, e4 OR d4, d5, d6
            case 11 -> // e4
                    (b[9] == c && b[10] == c) || (b[5] == c && b[6] == c) || (b[14] == c && b[17] == c); // mill is e4, c4, d4 OR e4, e3, e2 OR e4, f5, g6
            case 12 -> // b5
                    (b[15] == c && b[9] == c) || (b[13] == c && b[14] == c); // mill is b5, a6, c4 OR b5, d5, f5
            case 13 -> // d5
                    (b[12] == c && b[14] == c) || (b[10] == c && b[16] == c); // mill is d5, b5, f5 OR d5, d4, d6
            case 14 -> // f5
                    (b[12] == c && b[13] == c) || (b[11] == c && b[17] == c) || (b[3] == c && b[7] == c); // mill is f5, b5, d5 OR f5, e4, g6 OR f5, f1, f3
            case 15 -> // a6
                    (b[12] == c && b[9] == c) || (b[17] == c && b[16] == c); // mill is a6, b5, c4 OR a6, d6, g6
            case 16 -> // d6
                    (b[15] == c && b[17] == c) || (b[10] == c && b[13] == c); // mill is d6, a6, g6 OR d6, d4, d5
            case 17 -> // g6
                    (b[15] == c && b[16] == c) || (b[1] == c && b[8] == c) || (b[11] == c && b[14] == c); // mill is g6, a6, d6 OR g6, g0, g3 OR g6, e4, f5
            default -> false;
        };
    }

    public static int staticEstimationOpening(char[] b) {
        int numWhitePieces = 0;
        int numBlackPieces = 0;
        int control = 0;
        int[] boardcenter = {4, 5, 6, 9, 10, 11}; // Positions around the board center
        for (int i = 0; i < b.length; i++) {
            char c = b[i];
            if (c == 'W') {
                numWhitePieces++;
                for (int center_position:boardcenter) { // Iterate through center positions
                    if (i == center_position) {
                        control += 3; // Center control for white
                    }
                }
            }
            if (c == 'B') {
                numBlackPieces++;
                for (int center_position:boardcenter) { // Iterate through center positions
                    if (i == center_position) {
                        control -= 3; // Center control for black
                    }
                }
            }
        }
        return (numWhitePieces - numBlackPieces) + control; // Factor center control into the output
    }
}

