import java.io.*;
import java.util.*;

public class ReinforcementLearningGame {
    private static final double ALPHA = 0.1; // Learning rate
    private static final double GAMMA = 0.9; // Discount factor
    private static final double EPSILON = 0.1; // Exploration rate
    private static final int EPISODES = 10000; // Number of training episodes

    private static final Map<String, Map<String, Double>> qTable = new HashMap<>(); // Q-table

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Input format: java ReinforcementLearningGame <input board file> <output board file> <depth>");
            return;
        }
        String inputPositions = args[0];
        String outputPositions = args[1];
        int depth = Integer.parseInt(args[2]);

        BufferedReader reader = new BufferedReader(new FileReader(inputPositions));
        String boardPosition = reader.readLine();
        reader.close();

        // Train the agent
        trainAgent();

        // Use the trained agent to play the game
        Result bestMove = findBestMove(boardPosition.toCharArray());

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputPositions));
        writer.write(bestMove.board);
        writer.close();

        System.out.println("Board Position: " + bestMove.board);
    }

    private static void trainAgent() {
        for (int episode = 0; episode < EPISODES; episode++) {
            char[] board = initializeBoard();
            boolean isWhite = false;
            while (!isGameOver(board)) {
                String state = new String(board);
                String action;
                if (Math.random() < EPSILON) {
                    // Exploration: choose a random action
                    action = getRandomAction(state, isWhite);
                } else {
                    // Exploitation: choose the best action based on Q-values
                    action = getBestAction(state, isWhite);
                }
                char[] newBoard = applyAction(board, action);
                String newState = new String(newBoard);
                int reward = getReward(newBoard, isWhite);
                updateQTable(state, action, reward, newState);
                board = newBoard;
                isWhite = !isWhite;
            }
        }
    }

    private static char[] initializeBoard() {
        // Initialize the board to the starting position
        return "xxxxxxxxxxxxxxxxxxxxx".toCharArray();
    }

    private static boolean isGameOver(char[] board) {
        // Check if the game is over
        int whiteCount = 0, blackCount = 0;
        for (char c : board) {
            if (c == 'W') whiteCount++;
            else if (c == 'B') blackCount++;
        }
        return whiteCount <= 2 || blackCount <= 2 || generateMoves(board, 'W').isEmpty() || generateMoves(board, 'B').isEmpty();
    }

    private static String getRandomAction(String state, boolean isWhite) {
        List<String> actions = getPossibleActions(state, isWhite);
        return actions.get(new Random().nextInt(actions.size()));
    }

    private static String getBestAction(String state, boolean isWhite) {
        Map<String, Double> actionValues = qTable.getOrDefault(state, new HashMap<>());
        if (actionValues.isEmpty()) return getRandomAction(state, isWhite);
        return actionValues.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(getRandomAction(state, isWhite));
    }

    private static List<String> getPossibleActions(String state, boolean isWhite) {
        // Generate all possible actions for the current state
        List<char[]> moves = generateMoves(state.toCharArray(), isWhite ? 'W' : 'B');
        List<String> actions = new ArrayList<>();
        for (char[] move : moves) {
            actions.add(new String(move));
        }
        return actions;
    }

    private static List<char[]> generateMoves(char[] board, char piece) {
        List<char[]> positions = new ArrayList<>();
        for (int location = 0; location < board.length; location++) {
            if (board[location] == piece) {
                int[] neighbors = neighbors(location);
                for (int j : neighbors) {
                    if (board[j] == 'x') {
                        char[] b = board.clone();
                        b[location] = 'x';
                        b[j] = piece;
                        positions.add(b);
                    }
                }
            }
        }
        return positions;
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

    private static char[] applyAction(char[] board, String action) {
        return action.toCharArray();
    }

    private static int getReward(char[] board, boolean isWhite) {
        if (isGameOver(board)) {
            int whiteCount = 0, blackCount = 0;
            for (char c : board) {
                if (c == 'W') whiteCount++;
                else if (c == 'B') blackCount++;
            }
            if (whiteCount <= 2) return isWhite ? -100 : 100;
            if (blackCount <= 2) return isWhite ? 100 : -100;
            if (generateMoves(board, 'W').isEmpty()) return isWhite ? -100 : 100;
            if (generateMoves(board, 'B').isEmpty()) return isWhite ? 100 : -100;
        }
        return 0;
    }

    private static void updateQTable(String state, String action, int reward, String newState) {
        double oldQValue = qTable.getOrDefault(state, new HashMap<>()).getOrDefault(action, 0.0);
        double maxFutureQValue = qTable.getOrDefault(newState, new HashMap<>()).values().stream().max(Double::compare).orElse(0.0);
        double newQValue = oldQValue + ALPHA * (reward + GAMMA * maxFutureQValue - oldQValue);
        qTable.computeIfAbsent(state, k -> new HashMap<>()).put(action, newQValue);
    }

    private static Result findBestMove(char[] board) {
        String state = new String(board);
        String bestAction = getBestAction(state, false);
        return new Result(bestAction, 0, 0); // Static estimate and positions not used in RL
    }
}

