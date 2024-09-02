import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MinimaxGameImproved {
    private static int positionsEvaluated = 0; // Instantiate evaluated positions to 0

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Input format: java MinimaxGame <input board file> <output board file> <depth>");
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

    private static Result minimax(char[] board, int depth, boolean isWhite) {
        // Returns a Result class containing current board, estimate, and number of evaluated positions
        if (depth == 0) {
            positionsEvaluated++; // If the depth is 0, static estimation only evaluates one board position
            return new Result(new String(board), staticEstimationMidgameEndgame(board), positionsEvaluated);
        }

        List<char[]> possibleMoves; // Create an empty list of character arrays
        if (isWhite) { // If it is white's turn
            possibleMoves = GenerateMovesMidEndGame(board, 'W');
        }
        else { // If it is black's turn
            possibleMoves = GenerateMovesMidEndGame(board, 'B');
        }
        Result bestResult = null;
        for (char[] move : possibleMoves) { // For each possible move
            Result eval = minimax(move, depth - 1, !isWhite); // Recursively evaluate the level above
            if (bestResult == null || (isWhite && eval.estimate > bestResult.estimate) || (!isWhite && eval.estimate < bestResult.estimate)) {
                // If white, then choose the estimate larger than bestResult, or else choose the one that is smaller
                bestResult = new Result(new String(move), eval.estimate, eval.positions);
            }
        }
        return bestResult;
    }

    private static int staticEstimationMidgameEndgame(char[] b) {
        int numWhitePieces = 0, numBlackPieces = 0;
        int whiteMills = 0, blackMills = 0;
        for (char c : b) { // Count the number of white and black pieces
            if (c == 'W') numWhitePieces++;
            else if (c == 'B') numBlackPieces++;
        }

        List<char[]> blackMoves = GenerateMovesMidEndGame(b, 'B'); // Get the number of black moves
        int numBlackMoves = blackMoves.size();
        List<char[]> whiteMoves = GenerateMovesMidEndGame(b, 'W'); // Get the number of white moves
        int numWhiteMoves = whiteMoves.size();

        for (int i = 0; i < b.length; i++) {
            if (b[i] == 'W' && closeMill(i,b)) { whiteMills++; }
            if (b[i] == 'B' && closeMill(i,b)) { blackMills++; }
        }
        positionsEvaluated++;

        if (numBlackPieces <= 2) return 10000; // White wins
        else if (numWhitePieces <= 2) return -10000; // Black wins
        else if (numBlackMoves == 0) return 10000; // White wins
        else if (numWhiteMoves == 0) return -10000; // Black wins
        else return 1000 * (numWhitePieces - numBlackPieces) + (numWhiteMoves - numBlackMoves) + 400 * (whiteMills - blackMills);
        // Factor in number of moves, as higher move count indicates greater control of the board
        // Factor in the number of mills each player made, and provide a weight of 400 since forming mills is contingent to capturing pieces
    }

    private static List<char[]> GenerateMovesMidEndGame(char[] board, char piece) {
        List<char[]> positions;
        int count = 0;
        for (char c : board) { // Count the number of pieces on the board
            if (c == piece) count++;
        }

        if (count == 3) { // If the board has three white pieces, call GenerateHopping. Or else call GenerateMove.
            positions = GenerateHopping(board, piece);
        } else {
            positions = GenerateMove(board, piece);
        }
        return positions;
    }

    private static List<char[]> GenerateMove(char[] board, char piece) {
        List<char[]> L = new ArrayList<>();

        for (int location = 0; location < board.length; location++) { // For each location on the board
            if (board[location] == piece) { // If the piece matches the designated color
                int[] neighbors = neighbors(location); // Get all of its neighbors
                for (int j : neighbors) { // For each neighbor
                    if (board[j] == 'x') { // If the spot is empty
                        char[] b = board.clone();
                        b[location] = 'x'; // Mark the location as empty and move the piece to new spot at 'j'
                        b[j] = piece;
                        if (closeMill(j, b)) { // If a mill can be closed, call GenerateRemove
                            GenerateRemove(b, L);
                        } else { // Or else add the board to L and return L.
                            L.add(b);
                        }
                    }
                }
            }
        }
        return L;
    }

    private static List<char[]> GenerateHopping(char[] board, char piece) {
        List<char[]> L = new ArrayList<>();

        for (int alpha = 0; alpha < board.length; alpha++) {
            if (board[alpha] == piece) { // If alpha matches the designated piece
                for (int beta = 0; beta < board.length; beta++) {
                    if (board[beta] == 'x') { // If any spot on the board is empty
                        char[] b = board.clone(); // Move the piece over to the empty spot and set original position to empty.
                        b[alpha] = 'x';
                        b[beta] = piece;
                        if (closeMill(beta, b)) {
                            GenerateRemove(b, L);
                        } else {
                            L.add(b);
                        }
                    }
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

    private static int[] neighbors(int location) { // Given a location, return all its neighbors
        return switch (location) {
            case 0 -> new int[]{1, 2, 15};
            case 1 -> new int[]{0, 3, 11};
            case 2 -> new int[]{0, 3, 4, 12};
            case 3 -> new int[]{1, 2, 5, 7};
            case 4 -> new int[]{2, 5, 9};
            case 5 -> new int[]{3, 4, 6};
            case 6 -> new int[]{5, 7, 11};
            case 7 -> new int[]{3, 6, 8, 14};
            case 8 -> new int[]{1, 7, 17};
            case 9 -> new int[]{4, 10, 12};
            case 10 -> new int[]{9, 11, 13};
            case 11 -> new int[]{6, 10, 14};
            case 12 -> new int[]{2, 9, 13, 15};
            case 13 -> new int[]{10, 12, 14, 16};
            case 14 -> new int[]{7, 11, 13, 17};
            case 15 -> new int[]{0, 12, 16};
            case 16 -> new int[]{13, 15, 17};
            case 17 -> new int[]{8, 14, 16};
            default -> new int[]{};
        };
    }

    public static boolean closeMill(int j, char[] b) { // Same function as in MinimaxOpening.java
        char c = b[j];
        if (c == 'x') {
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
}
