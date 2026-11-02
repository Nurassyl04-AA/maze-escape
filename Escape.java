/**
 * Mission 3. Escape: DFS from cell 0 to the last cell (rows * cols - 1) in the maze of Mission 1.
 * Two neighbouring cells are connected when the wall between them is broken.
 * The neighbours are tried in this order: up, right, down, left (cell - cols, cell + 1, cell + cols, cell - 1).
 * The DFS is iterative (own stack in arrays), so it does not overflow the stack on a maze of 1000 by 1000 cells.
 */
public class Escape {

    /**
     * path: the cells of the way out, from 0 to the last cell.
     * visited: how many cells are marked as visited at the moment the exit is marked (the start and the exit are counted).
     * deadEnds: how many cells of the maze have exactly one open neighbour.
     */
    public record Result(int[] path, int visited, int deadEnds) {}

    public static Result escape(Input.Walls w, int[] broken) {
        int n = w.cells(), cols = w.cols();
        int exit = n - 1;
        int[][] open = Graphs.open(w, broken);

        // door[cell] has 4 bits: 1 = up, 2 = right, 4 = down, 8 = left
        byte[] door = new byte[n];
        int deadEnds = 0;
        for (int c = 0; c < n; c++) {
            if (open[c].length == 1) deadEnds++;
            for (int nb : open[c]) {
                if (nb == c - cols) door[c] |= 1;
                else if (nb == c + cols) door[c] |= 4;
                else if (nb == c + 1) door[c] |= 2;
                else door[c] |= 8;
            }
        }
        int[] delta = {-cols, 1, cols, -1};

        boolean[] marked = new boolean[n];
        int[] stack = new int[n];       // the cells of the current path
        byte[] nextDir = new byte[n];   // which direction to try next in this cell (0..4)
        int top = 0, visited = 0;
        stack[top++] = 0;
        marked[0] = true;
        visited = 1;

        boolean found = exit == 0;
        while (top > 0 && !found) {
            int c = stack[top - 1];
            if (nextDir[c] == 4) { top--; continue; }   // all directions tried: go back
            int d = nextDir[c]++;
            if ((door[c] & (1 << d)) == 0) continue;
            int nb = c + delta[d];
            if (marked[nb]) continue;
            marked[nb] = true;
            visited++;
            stack[top++] = nb;
            if (nb == exit) found = true;
        }
        // when the exit is found the stack holds exactly the path from the start to the exit
        int[] path = found ? java.util.Arrays.copyOf(stack, top) : new int[0];
        return new Result(path, visited, deadEnds);
    }
}
