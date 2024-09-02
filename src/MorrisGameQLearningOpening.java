import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MorrisGameQLearningOpening {

    private static final double LEARNING_RATE = 0.1;
    private static final double DISCOUNT_FACTOR = 0.9;
    private static final double EXPLORATION_RATE = 0.1;
    private static final int EPISODES = 10000;

    private static final Map<String, Map<String, Double>> qTable = new HashMap<>();

    public static void main(String[] args) {
        // Train the agent for the opening phase
        trainAgent();

        // Generate a single white move for the opening phase
        char[] initialBoard = "xxxxxxxxxxxxxxxxxx".toCharArray();
        char currentPlayer = 'W';
        generateSingleWhiteMove(initialBoard, currentPlayer);
    }

    private static void trainAgent() {
        for (int episode = 0; episode < EPISODES; episode++) {
            char[] board = "xxxxxxxxxxxxxxxxxx".toCharArray();
            char currentPlayer = (episode % 2 == 0) ? 'W' : 'B';

            int moves = 0;
            while (moves < 18) { // Only the first 18 moves are considered for the opening phase
                String state = new String(board);
                String action = selectAction(state, currentPlayer);
                char[] nextState = applyAction(board, action, currentPlayer);
                int reward = getReward(nextState, currentPlayer);

                String nextStateStr = new String(nextState);
                double oldQValue = qTable.getOrDefault(state, new HashMap<>()).getOrDefault(action, 0.0);
                double nextMaxQValue = getMaxQValue(nextStateStr);
                double newQValue = oldQValue + LEARNING_RATE * (reward + DISCOUNT_FACTOR * nextMaxQValue - oldQValue);

                qTable.computeIfAbsent(state, k -> new HashMap<>()).put(action, newQValue);
                board = nextState;
                currentPlayer = (currentPlayer == 'W') ? 'B' : 'W';
                moves++;
            }
        }
    }

    private static String selectAction(String state, char player) {
        if (Math.random() < EXPLORATION_RATE) {
            return getRandomAction(state, player);
        } else {
            return getBestAction(state, player);
        }
    }

    private static String getRandomAction(String state, char player) {
        List<String> possibleActions = getPossibleActions(state.toCharArray(), player);
        return possibleActions.get(ThreadLocalRandom.current().nextInt(possibleActions.size()));
    }

    private static String getBestAction(String state, char player) {
        Map<String, Double> actions = qTable.getOrDefault(state, new HashMap<>());
        return actions.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(Map.entry(getRandomAction(state, player), 0.0)).getKey();
    }

    private static List<String> getPossibleActions(char[] board, char player) {
        List<String> actions = new ArrayList<>();
        List<char[]> possibleBoards = GenerateAdd(board);
        for (char[] possibleBoard : possibleBoards) {
            actions.add(new String(possibleBoard));
        }
        return actions;
    }

    private static char[] applyAction(char[] board, String action, char player) {
        return action.toCharArray();
    }

    private static int getReward(char[] board, char player) {
        // Simplified reward function for the opening phase
        int whitePieces = countPieces(board, 'W');
        int blackPieces = countPieces(board, 'B');
        return whitePieces - blackPieces;
    }

    private static int countPieces(char[] board, char player) {
        int count = 0;
        for (char c : board) {
            if (c == player) count++;
        }
        return count;
    }

    private static double getMaxQValue(String state) {
        return qTable.getOrDefault(state, new HashMap<>()).values().stream().max(Double::compareTo).orElse(0.0);
    }

    private static void generateSingleWhiteMove(char[] board, char currentPlayer) {
        if (currentPlayer != 'W') {
            throw new IllegalArgumentException("Current player must be 'W' for a white move.");
        }

        String state = new String(board);
        String action = getBestAction(state, currentPlayer);
        board = applyAction(board, action, currentPlayer);

        System.out.println("Generated move for White: " + action);
        System.out.println("Board after White's move: " + new String(board));
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
}

