// package pathfinder.informed;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Specifies the Maze Grid pathfinding problem including the actions, transitions,
 * goal test, and solution test. Can be fed as an input to a Search algorithm to
 * find and then test a solution.
 */
public class MazeProblem {

    // Fields
    // -----------------------------------------------------------------------------
    private String[] maze;
    private int rows, cols;
    public final MazeState INITIAL_STATE, KEY_STATE;
    public final ArrayList<MazeState> GOAL_STATES;
    private static final Map<String, MazeState> TRANS_MAP = createTransitions();

    /**
     * @return Creates the transition map that maps String actions to
     * MazeState offsets, of the format:
     * { "U": (0, -1), "D": (0, +1), "L": (-1, 0), "R": (+1, 0) }
     */
    private static final Map<String, MazeState> createTransitions () {
        Map<String, MazeState> result = new HashMap<>();
        result.put("U", new MazeState(0, -1));
        result.put("D", new MazeState(0,  1));
        result.put("L", new MazeState(-1, 0));
        result.put("R", new MazeState( 1, 0));
        return result;
    }


    // Constructor
    // -----------------------------------------------------------------------------

    /**
     * Constructs a new MazeProblem from the given maze; responsible for finding
     * the initial and goal states in the maze, and storing in the MazeProblem state.
     *
     * @param maze An array of Strings in which characters represent the legal maze
     * entities, including:<br>
     * 'X': A wall, 'G': A goal, 'K': A key, 'M': A mud tile, 'I': The initial state, '.': an open spot
     * For example, a valid maze might look like:
     * <pre>
     * String[] maze = {
     *     "XXXXXXX",
     *     "X.....X",
     *     "XIX.X.X",
     *     "XX.X..X",
     *     "XG....X",
     *     "XXXXXXX"
     * };
     * </pre>
     */
    MazeProblem (String[] maze) {
        this.maze = maze;
        this.rows = maze.length;
        this.cols = (rows == 0) ? 0 : maze[0].length();
        MazeState foundInitial = null, foundKey = null;
        GOAL_STATES = new ArrayList<>();

        // Find the initial and goal state in the given maze, and then
        // store in fields once found
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                switch (maze[row].charAt(col)) {
                case 'I':
                    foundInitial = new MazeState(col, row); break;
                case 'G':
                    GOAL_STATES.add(new MazeState(col, row)); break;
                case 'K':
                    foundKey = new MazeState(col, row); break;
                case 'M':
                case '.':
                case 'X':
                    break;
                default:
                    throw new IllegalArgumentException("Maze formatted invalidly");
                }
            }
        }
        INITIAL_STATE = foundInitial;
        KEY_STATE = foundKey;
    }


    // Methods
    // -----------------------------------------------------------------------------

    /**
     * Returns cost acquired from stepping on input MazeState
     * @param state a MazeState calculate cost of
     * @return int cost of stepping onto MazeState
     */
    public int getCost (MazeState state) {
        return (isMudTile(state)) ? distance(this.INITIAL_STATE, state) + 2 : distance(this.INITIAL_STATE, state);
    }

    /**
     * Returns distance from input state to nearest goal state
     * @param state MazeState representing where to start calculation from
     * @return int distance to nearest goal state
     */
     public int distanceToGoal (MazeState state) {
         int minDistance = this.rows + this.cols;
         for ( int i = 0; i < GOAL_STATES.size(); i++) {
             int distance = Math.abs(state.col - GOAL_STATES.get(i).col) + Math.abs(state.row - GOAL_STATES.get(i).row);
             if ( distance < minDistance ) {
                 minDistance = distance;
             }
         }
         return minDistance;
     }

     /**
      * Returns distance from initial state to input state
      * @param state MazeState representing where to start calculation from
      * @return int distance to input state
      */
      public int distance (MazeState state1, MazeState state2) {
          return Math.abs(state1.col - state2.col) + Math.abs(state1.row - state2.row);
      }

    /**
     * Returns whether or not the given state is a Goal state.
     *
     * @param state A MazeState (col, row) to test
     * @return Boolean of whether or not the given state is a Goal.
     */
    public boolean isGoal (MazeState state) {
        for ( int i = 0; i < this.GOAL_STATES.size(); i++ ) {
            if ( state.equals(this.GOAL_STATES.get(i) ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether or not the given state is a Mud tile.
     *
     * @param state A MazeState (col, row) to test
     * @return Boolean of whether or not the given state is a Mud tile.
     */
    public boolean isMudTile (MazeState state) {
        return ( this.maze[state.row].charAt(state.col) == 'M' );
    }

    /**
     * Returns whether or not the given state is a Key state.
     *
     * @param state A MazeState (col, row) to test
     * @return Boolean of whether or not the given state is a Key.
     */
    public boolean isKey (MazeState state) {
        return state.equals(KEY_STATE);
    }

    /**
     * Returns a map of the states that can be reached from the given input
     * state using any of the available actions.
     *
     * @param state A MazeState (col, row) representing the current state
     * from which actions can be taken
     * @return Map A map of actions to the states that they lead to, of the
     * format, for current MazeState (c, r):<br>
     * { "U": (c, r-1), "D": (c, r+1), "L": (c-1, r), "R": (c+1, r) }
     */
    public Map<String, MazeState> getTransitions (MazeState state) {
        // Store transitions as a Map between actions ("U", "D", ...) and
        // the MazeStates that they result in from state
        Map<String, MazeState> result = new HashMap<>();

        // For each of the possible directions (stored in TRANS_MAP), test
        // to see if it is a valid transition
        for (Map.Entry<String, MazeState> action : TRANS_MAP.entrySet()) {
            MazeState actionMod = action.getValue(),
                      newState  = new MazeState(state.col, state.row);
            newState.add(actionMod);

            // If the given state *is* a valid transition (i.e., within
            // map bounds and no wall at the position)...
            if (newState.row >= 0 && newState.row < rows &&
                newState.col >= 0 && newState.col < cols &&
                maze[newState.row].charAt(newState.col) != 'X') {
                // ...then add it to the result!
                result.put(action.getKey(), newState);
            }
        }
        return result;
    }

    /**
     * Given a possibleSoln, tests to ensure that it is indeed a solution to this MazeProblem,
     * as well as returning the cost.
     *
     * @param possibleSoln A possible solution to test, which is a list of actions of the format:
     * ["U", "D", "D", "L", ...]
     * @return A 2-element array of ints of the format [isSoln, cost] where:
     * isSoln will be 0 if it is not a solution, and 1 if it is
     * cost will be an integer denoting the cost of the given solution to test optimality
     */
    public int[] testSolution (ArrayList<String> possibleSoln) {
        // Update the "moving state" that begins at the start and is modified by the transitions
        MazeState movingState = new MazeState(INITIAL_STATE.col, INITIAL_STATE.row);
        int cost = 0;
        boolean hasKey = false;
        int[] result = {0, -1};

        // For each action, modify the movingState, and then check that we have landed in
        // a legal position in this maze
        for (String action : possibleSoln) {
            MazeState actionMod = TRANS_MAP.get(action);
            movingState.add(actionMod);
            switch (maze[movingState.row].charAt(movingState.col)) {
            case 'X':
                return result;
            case 'K':
                hasKey = true; break;
            }
            cost += getCost(movingState);
        }
        result[0] = isGoal(movingState) && hasKey ? 1 : 0;
        result[1] = cost;
        return result;
    }


    public static void main(String args[]) {
        String[] maze = {
            "XXXXXXX",
            "XIM...X",
            "XMMK..X",
            "XMX.XGX",
            "XXXXXXX"
            // "XXXXXXX",
            // "XI....X",
            // "X.MMM.X",
            // "X..KXGX",
            // "XXXXXXX"
        };
        MazeProblem prob = new MazeProblem(maze);
        System.out.println( prob.getCost(new MazeState(2,1)));
        // String[] maze = {
        // "XXXXXXX",
        // "XI...GX",
        // "X.....X",
        // "X.X.XKX",
        // "XXXXXXX"
        // };
        // MazeProblem prob = new MazeProblem(maze);
        // System.out.println(prob.isKey(new MazeState(5,3)));
        //
        // String[] maze1 = {
        //     "XXXXXXX",
        //     "XI....X",
        //     "X.MMM.X",
        //     "X.XKXGX",
        //     "XXXXXXX"
        // };
        // MazeProblem prob1 = new MazeProblem(maze1);
        // System.out.println(prob1.isKey(new MazeState(3,3)));
        //
        // String[] maze2 = {
        //     "XXXXXXX",
        //     "XI.G..X",
        //     "X.MXMGX",
        //     "XKXXX.X",
        //     "XXXXXXX"
        //     };
        // MazeProblem prob2 = new MazeProblem(maze2);
        // System.out.println(prob2.isKey(new MazeState(1,3)));
        //
        // System.out.println( "Testing distance to GOAL");
        // System.out.println(prob2.distanceToGoal( new MazeState(1,2))); //should be 3
        // System.out.println(prob2.distanceToGoal( new MazeState(5,3))); //should be 1
        // System.out.println(prob2.distanceToGoal( new MazeState(5,2))); //should be 0
        // System.out.println(prob2.distanceToGoal( new MazeState(3,1))); //should be 0
        // System.out.println(prob2.distance(new MazeState(5,2), new MazeState(5,3))); //should be 1

        // System.out.println( "\nTesting distance from Initial");
        // System.out.println(prob2.distanceFromInitial( new MazeState(1,2))); //should be 1
        // System.out.println(prob2.distanceFromInitial( new MazeState(5,3))); //should be 6
        // System.out.println(prob2.distanceFromInitial( new MazeState(5,2))); //should be 5
        // System.out.println(prob2.distanceFromInitial( new MazeState(3,1))); //should be 2

        // System.out.println(prob2.isMudTile( new MazeState(4, 2)));
        // System.out.println(prob2.isMudTile( new MazeState(1, 2)));
        //
        // System.out.println(prob2.getCost( new MazeState(2, 2))); //should be 4
        // System.out.println(prob2.getCost( new MazeState(4, 2))); //should be 6
        // System.out.println(prob2.getCost( new MazeState(1, 2))); //should be 1
        // System.out.println(prob2.getCost( new MazeState(1, 1))); //should be 0

    }

}
