package solver;

public class Node {
    protected State state;   // saved state
    protected int cost;      // number of moves to reach this state
    protected int heuristic; // Manhattan Distance
    protected Node parent;   // Reference to the parent node
    protected int prio;      // cost + heuristic

    public Node( State state, int cost, int heuristic, Node parent ) {
        this.state = state;
        this.cost = cost;
        this.heuristic = heuristic;
        this.parent = parent;
        this.prio = cost + heuristic;
    }
}
