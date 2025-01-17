package solver;
import java.util.*;

public class SokoBot {

    /*|************************************************************
                              Main Method
    ***************************************************************/
    /**
        ` Solves a Sokoban puzzle using an A* heuristics algorithm. 
    */
	public String solveSokobanPuzzle( int width, int height, char[][] mapData, char[][] itemsData ) {
        State state = new State( width, height, mapData, itemsData );   
        state.detectSimpleDeadlock();      
        String solutionString = A_Star(state);
        System.out.printf( solutionString );
        return solutionString;  
	}

    /*|************************************************************
                           A* Search Algorithm
    ***************************************************************/
    /**
        ` Performs the A* search algorithm to find the solution to the Sokoban game
    */
	private String A_Star( State state ) {

        // - PriorityQueue with custom Comparator for Nodes
        // The overriden compare method arranges the nodes via the lowest priority
        PriorityQueue<Node> pQueue = new PriorityQueue<>(
            new Comparator<Node>() {  
                @Override public int compare( Node n1, Node n2 ) {
                    return Integer.compare( n1.prio, n2.prio );
                }
            }
        );

        // - Create a set to store visited states
        Set<State> visited = new HashSet<>();    
        Node initial = new Node(state, 0, calcManhattanDist(state), null); 
        pQueue.add(initial);

        // - Similar to detecting deadlock, PriorityQueue will keep exploring all possible states
        while( !pQueue.isEmpty() ) {          
            // - Pop the node with the lowest priority from the priority queue 
            Node currNode = pQueue.poll();    
            State currState = currNode.state; 
            
            // - Check if the current state is the goal state and construct the solution.
            if( currState.isGoalState() ) {       
                StringBuilder solution = new StringBuilder();    
                for( String moves : backtrack(currNode) ) {
                    solution.append(moves);                      
                }                                  
                return solution.toString();
            }

            visited.add(currState);

            // - Generate successor states and explore the nodes
            for( State successor : generateSState(currState, visited) ) {        
                if( successor.is_in(visited) ) {
                    continue;
                }                                   

                // - Calculate the cost and heuristic for the successor node
                int succCost = currNode.cost + 1;   // 1 cost per move
                int succHeuristic = calcManhattanDist(successor);
                            
                // - Create the successor node and add it to the priority queue.
                Node sNode = new Node(successor, succCost, succHeuristic, currNode);   
                pQueue.add(sNode);          // add to priority queue to loop
                visited.add(successor);     // add successor state to visited; already explored in generateSState()
            }
        }
        return null;
	}

    /**
        ` Generates successor states by simulating player movements in four possible 
        directions and checks their validity.
    */
	private List<State> generateSState( State currState, Set<State> visited ) {
        List<State> succStates = new ArrayList<>();   // list of states to be generated
        Coordinates player = currState.player;

        int[] dRow = { -1, 1, 0, 0 };
        int[] dCol = { 0, 0, -1, 1 };

        // - Iterate through the four possible directions (up, down, left, right)
        for( int i = 0; i < 4; i++ ) {
            Coordinates dest = new Coordinates(player.row + dRow[i], player.col + dCol[i]);

            // - Check if the destination is a valid move based on the current state and direction.
            if( isValid(currState, dest, i) ) {      
                State nextState = new State(currState);  

                // - If the move is valid, move the player and add it to the list of successor states
                if( nextState.movePlayer(dest, i) ) {      
                    succStates.add(nextState);      
                }
            }

            /*
                NOTE: The commented print statements are for debugging and can be used to visualize the state transitions.
                    currState.printState(currState.gameState);
                    try{
                        Thread.sleep(100);
                    } catch (InterruptedException e){}
            */ 
        }
        return succStates;    //return the state
	}

    /**
        ` Backtracks from a goal node to the initial node to reconstruct the sequence of 
        moves made to reach the solution. Basically, this returns the solution string.
    */
	private List<String> backtrack( Node goalNode ) {
        List<String> moves = new ArrayList<>();

        // - Traverse from goal node to the intial node
        while( goalNode != null ) {     
            Node parentNode = goalNode.parent;
            if( parentNode != null ) {
                String move = determineMove(goalNode.state, parentNode.state); 
                moves.add(0, move);
            }
            goalNode = parentNode;
        }
        return moves;
	}

    /**
        ` Determines the move the character perforemd based on the change in the player's
        position between two states.
    */
	private String determineMove( State preState, State postState ) {  
        Coordinates preMove = preState.player;                      
        Coordinates postMove = postState.player;

        int rowDiff = postMove.row - preMove.row;
        int colDiff = postMove.col - preMove.col;

        if( rowDiff == -1 && colDiff == 0 ) {
            return "d";
        } else if( rowDiff == 1 && colDiff == 0 ) {
            return "u";
        } else if( rowDiff == 0 && colDiff == 1 ) {
            return "l";
        } else if( rowDiff == 0 && colDiff == -1) {
            return "r";
        } else {
            return "";
        }
	}

    /*|************************************************************
                          State Validation
    ***************************************************************/
    /**
        ` Calculates the total Manhattan Distance between boxes and their respective nearest
        goals and the player to the boxes. 
    */
	private int calcManhattanDist( State state ) {
        int totalDistance = 0;
        for( Coordinates box : state.boxCoords ) {
            int minDistance = Integer.MAX_VALUE;    //set minDistance to max possible value of int
            
            // - If the box ever ends up in a deadlock state, set manhattan distance to max int
            if( !box.is_in(state.validCoords) ) {
                return minDistance;                   
            }

            for( Coordinates goal : state.goalCoords ) {
                // - Calculate the Manhattan Distance between the current box and each goal position.
                int distance = Math.abs(box.row - goal.row) + Math.abs(box.col - goal.col);     

                if( distance < minDistance ) {
                    minDistance = distance;
                }         
            }

            // - Calculate the Manhattan Distance between the player and the current box.
            int playerBoxDistance = Math.abs(state.player.row - box.row) + Math.abs(state.player.col - box.col);
            
            // - Add the box's Manhattan Distance and the player-to-box Manhattan Distance to the total.
            totalDistance += minDistance + playerBoxDistance;
        }
        return totalDistance;
	}

    /**
        ` Checks if a given destination is a valid move for the player within the current state.
    */
    private Boolean isValid( State state, Coordinates destination, int direction  ) {
        Boolean isWithinRowBounds = destination.row >= 0 && destination.row < state.height;
        Boolean isWithinColBounds = destination.col >= 0 && destination.col < state.width;
        Boolean isFreeSpace = state.gameState[destination.row][destination.col] != '#';
        Boolean isDestinationABox = destination.is_in(state.boxCoords);

        if( isWithinRowBounds && isWithinColBounds && isFreeSpace ) {
            if( isDestinationABox ) {
                Coordinates temp = new Coordinates( destination.row, destination.col );
                return processBox( state, temp, direction );
            } 
            return true;
        }
        return false;
    }
    
    /**
        ` Processes the validity of moving a box in the specified direction.
    */
    private Boolean processBox( State state, Coordinates destination, int direction ) {
        switch( direction ) {
            case 0: destination.row--; break;
            case 1: destination.row++; break;
            case 2: destination.col--; break;
            case 3: destination.col++; break;
        }

        char object = state.gameState[destination.row][destination.col];
        Boolean isObstacle = (object == '$') || (object == '*');

        if( isObstacle ) {
            return false;
        } 
        return true;
    }

	private void printDetails( State state ){     
        System.out.println("Box Coordinates:");
        state.printListCoords(state.boxCoords);

        System.out.println("Goal Coordinates:");
        state.printListCoords(state.goalCoords);

        System.out.println("Valid Coordinates: ");
        state.printListCoords(state.validCoords);

        System.out.println("Game State:");
        state.printState(state.gameState);

        System.out.println("Clone State:");
        state.printState(state.cloneState);
	}
}