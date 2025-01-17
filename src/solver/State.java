package solver;

import java.util.*;

public class State{

    /*|************************************************************
                              Attributes
    ***************************************************************/
    protected int width;
    protected int height;
    protected char[][] gameState;
    protected char[][] cloneState;

    protected Coordinates player = null;
    protected ArrayList<Coordinates> goalCoords = new ArrayList<>();
    protected ArrayList<Coordinates> boxCoords = new ArrayList<>();
    protected ArrayList<Coordinates> validCoords = new ArrayList<>();

    /*|************************************************************
                          Constructor Methods
    ***************************************************************/
    /**
        ` A constructor that creates a new State object by copying the
        attributes of an existing state.
    */
    public State( State state ) {          
        this.width = state.width;
        this.height = state.height;
        this.gameState = new char[height][width];
        this.cloneState = new char[height][width];
        this.player = new Coordinates(state.player.row, state.player.col);

        this.goalCoords = new ArrayList<>(state.goalCoords.size());
        for( Coordinates goal : state.goalCoords ) {
            this.goalCoords.add(new Coordinates(goal.row, goal.col));
        }

        this.boxCoords = new ArrayList<>(state.boxCoords.size());
        for( Coordinates box : state.boxCoords ) {
            this.boxCoords.add(new Coordinates(box.row, box.col));
        }

        this.validCoords = new ArrayList<>(state.validCoords.size());
        for( Coordinates valid : state.validCoords ) {
            this.validCoords.add(new Coordinates(valid.row, valid.col));
        }

        for( int row = 0; row < height; row++ ) {
            for( int col = 0; col < width; col++ ) {
                this.gameState[row][col] = state.gameState[row][col];
                this.cloneState[row][col] = state.cloneState[row][col];
            }
        }
    }

    /**
        ` A constructor that creates a new State object from a provided 
        width, height, map data, and items data. 

        @param width        gameboard width
        @param height       gameboard height
        @param mapData      2D char array representing the map layout
        @param itemsData    2D char array representing items and player positions
    */
    public State( int width, int height, char[][] mapData, char[][] itemsData ) {     
        this.width = width;
        this.height = height;

        gameState = new char[height][width];

        // - The nested loop is used to iterate mapData and items at the same time (char[row][col])
        for( int row = 0; row < height; row++ ) {             
            for( int col = 0; col < width; col++ ) {         
                char item = itemsData[row][col];
                gameState[row][col] = mapData[row][col];
        
                if( item == '@' && gameState[row][col] == '.'){
                    gameState[row][col] = '+';
                    player = new Coordinates(row, col);
                    goalCoords.add(new Coordinates(row, col));
                }
                else if( item == '@' ){
                    gameState[row][col] = item;
                    player = new Coordinates(row, col);
                }
                else if( item == '$' && gameState[row][col] == '.' ){
                    gameState[row][col] = '*';
                    goalCoords.add(new Coordinates(row, col));
                    boxCoords.add(new Coordinates(row, col));
                }
                else if( item == '$' ){
                    gameState[row][col] = item;
                    boxCoords.add(new Coordinates(row, col));
                }
                else if( item == '.' || gameState[row][col] == '.' )
                    goalCoords.add(new Coordinates(row, col));
            }
        }
    }

    /*|************************************************************
                            Class Methods
    ***************************************************************/
    /**
        ` Creates a clone of the game state and puts all boxes from the gameState into 
        goal squares, while updating the cloneState. It also populates the provided `boxCoords` 
        ArrayList with the coordinates of the boxes on the game board.
     
        @param boxCoords  an ArrayList to store the coordinates of the boxes on the game board.
        @return a char[][] representing the cloned game state with boxes placed in goal squares.
     */
    private char[][] cloneGameState( ArrayList<Coordinates> boxCoords ) {   
        for( int row = 0; row < height; row++ ) {
            for( int col = 0; col < width; col++ ) {
                char object = gameState[row][col];

                if( object == '$' || object == '@') {
                    cloneState[row][col] = ' ';
                } else if( object == '.' || object == '*' || object == '+') {
                    boxCoords.add( new Coordinates(row,col) );
                } else {
                    cloneState[row][col] = object;
                }
            }
        }
        return cloneState;
    }

    /**
        ` Perform a breadth-first search (BFS) starting from the given box's position to mark 
        reachable squares and identify non-deadlock squares. This method helps detect simple 
        deadlock situations.
    */
    private void performPull( Coordinates box ) {     
        
        // - Create a queue for BFS
        Queue<Coordinates> queue = new LinkedList<>();
        queue.offer(box);

        // -  Define movement directions: { up, down, left, right }
        int[] dRow = { -1, 1, 0, 0 };
        int[] dCol = { 0, 0, -1, 1 };

        while( !queue.isEmpty() ){
            Coordinates curr = queue.poll();

            // - Check adjacent squares in all four directions
            for( int i = 0; i < 4; i++ ){
                Coordinates dest = new Coordinates( curr.row + dRow[i], curr.col + dCol[i] );

                // - Check if the destination is valid and has not been visited.
                if( isValidPosition(dest, i) && cloneState[dest.row][dest.col] != 'V' ){
                    validCoords.add(new Coordinates(dest.row, dest.col));   // save coordinates of non-deadlock squares
                    cloneState[dest.row][dest.col] = 'V';                   // mark as visited
                    queue.offer(dest);                                      // add that new coordinate to the queue
                }
            }
        }
        cloneState[box.row][box.col] = 'V';
    }

    /**
        ` Attempt to move the player to the destination while handling box movement if applicable.

        @param destination  the destination coordinates to which the player intends to move 
        @param direction    the direction the player is facing( 0: up, 1: down, 2: left, 3: right )
    */
    public Boolean movePlayer( Coordinates destination, int direction ) {
        Coordinates playerDestination = new Coordinates(destination.row, destination.col);

        for( Coordinates box : boxCoords ) {
            Coordinates boxDestination = new Coordinates(box.row, box.col);

            Boolean isThereABox = boxDestination.compare(destination);
            if( isThereABox ) {
                switch( direction ) {
                    case 0: boxDestination.row--; break;
                    case 1: boxDestination.row++; break;
                    case 2: boxDestination.col--; break;
                    case 3: boxDestination.col++;
                }

                // - update gamestate and box coordinates
                if( isValid(boxDestination) ) {
                    gameState[box.row][box.col] = ' ';
                    gameState[boxDestination.row][boxDestination.col] = '$';
                    box.row = boxDestination.row;
                    box.col = boxDestination.col;
                } else {
                    return false;
                }

            }
        }

        // - update gamestate and player position
        gameState[destination.row][destination.col] = '@';            
        gameState[player.row][player.col] = ' ';
        player = playerDestination;           
        return true;
    }

   /*|************************************************************
                        Detection & Validation 
    ***************************************************************/
    /**
        ` Compare this State object with another State object to check if they are equivalent. 
        This comparison includes player positions and box positions.
    */
    public Boolean compare( State x ) {            
        int counter = 0;

        if( this.player.compare(x.player) ) {
            for( int i = 0; i < boxCoords.size(); i++ ) {
                if( this.boxCoords.get(i).compare(x.boxCoords.get(i)) ) {
                    counter++;
                }
            }
        }
        return counter == boxCoords.size();
    }

    /**
        ` Detect simple deadlock situations on the game board by checking for squares where
        the boxes would be stuck. This method identifies and marks non-deadlock coordinates
        based on box movements.
    */
    public void detectSimpleDeadlock() {            
        ArrayList<Coordinates> box = new ArrayList<>();
    
        cloneState = new char[height][width];
        cloneState = cloneGameState(box);           
        cloneGameState(box);
    
        for( int i = 0; i < boxCoords.size(); i++ ) {
            performPull(box.get(i));
        }

        for( Coordinates goal : goalCoords ) {
            validCoords.add(new Coordinates(goal.row, goal.col));
        }
    }

    /**
        ` Check if this State object is present in a given Set of State objects, similar to 
        the `Collection.contains` method.
    */
    public Boolean is_in( Set<State> list ) {      
        for( State state : list ) {
            if( state.compare(this) ) {
                return true;
            }
        }

        return false;
    }

    /**
        ` Check if the current game state is a goal state, meaning all boxes are on goal squares.
        This is done by checking if the amount of boxes on the target coordinates matches the 
        amount of goal coordinates.
    */
    public Boolean isGoalState() {     
        int goalCount = 0;
        for( Coordinates box : boxCoords ) {
            if( box.is_in(goalCoords) ) {
                goalCount++;
            }
        }
        return goalCount == goalCoords.size();
    }

    /**
        ` Check if the specified destination coordinates are a valid position for the player to move to.
    */
    private Boolean isValid( Coordinates destination ) {
        char object = gameState[destination.row][destination.col];
        Boolean isWithinRowBounds = destination.row >= 0 && destination.row < height;
        Boolean isWithinColBounds = destination.col >= 0 && destination.col < width;
        Boolean isFreeSpace = (object != '$') || (object != '#') || (object != '*');
        Boolean isNotDeadlock = destination.is_in(validCoords);

        return isWithinRowBounds && isWithinColBounds && isFreeSpace && isNotDeadlock;
    }
    
    /**
        ` Check if the specified destination coordinates are a valid position for a box to move to in a particular direction.
    */
    private Boolean isValidPosition( Coordinates destination, int move ) {
        Boolean isWithinRowBounds = destination.row >= 0 && destination.row < height;
        Boolean isWithinColBounds = destination.col >= 0 && destination.col < width;
        Boolean isFreeSpace = cloneState[destination.row][destination.col] == ' ';
    
        if( isWithinRowBounds && isWithinColBounds && isFreeSpace ) {
            int newRow = destination.row; 
            int newCol = destination.col;

            switch( move ) {
                case 0: newRow--; break;
                case 1: newRow++; break;
                case 2: newCol--; break;
                case 3: newCol++; break;
            }

            char object = cloneState[newRow][newCol];
            Boolean isValid = (object == ' ') || (object == 'V');
            return isValid;
        } 
        return false;
    }

    /*|************************************************************
                             Display Methods
    ***************************************************************/  
    public void printState( char[][] state ) {         
        for( int i = 0; i < state.length; i++ ) {
            for( int j = 0; j < state[0].length; j++ ) {
                System.out.print(state[i][j]);
            } System.out.println();
        } System.out.println();
    }

    public void printListCoords(ArrayList<Coordinates> coordsList){            
        for( int i = 0; i < coordsList.size(); i++ ) {
            System.out.println(coordsList.get(i).row + " " + coordsList.get(i).col);
        } System.out.println();
    }
}