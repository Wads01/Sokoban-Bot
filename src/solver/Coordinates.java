package solver;
import java.util.ArrayList;

public class Coordinates {
    protected int row;
    protected int col;

    public Coordinates( int row, int col ) {
        this.row = row;
        this.col = col;
    }

    /**
        ` Compares this Coordinates object with another Coordinates object for equality.
    */
    public Boolean compare( Coordinates x ) {       
        return this.row == x.row && this.col == x.col;
    }

    /**
        ` Checks if this Coordinates object is in a list of Coordinates
    */
    public Boolean is_in( ArrayList<Coordinates> list ) {          
        for( Coordinates coords : list ) {
            if( coords.compare(this) ) {
                return true;
            }
        } return false;
    }

    public void print() {
        System.out.println("row: " + this.row + "|" + "col: " + this.col);
    }
}
