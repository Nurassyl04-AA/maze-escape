/**
 * Mission 0. Disjoint set union (DSU).
 * Cells are numbered 0 .. n-1. Union by size (the smaller tree goes under the larger one)
 * and path compression in find.
 */
public class DisjointSet {

    private final int[] parent;
    private final int[] size;   // size[r] is valid only while r is a root
    private int sets;

    public DisjointSet(int n) {
        parent = new int[n];
        size = new int[n];
        for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
        sets = n;
    }

    /** The root of the set that contains x. Iterative, so a long chain cannot overflow the stack. */
    public int find(int x) {
        int root = x;
        while (parent[root] != root) root = parent[root];
        // path compression: every cell on the way now points straight to the root
        while (parent[x] != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    /** Joins the sets of a and b. Returns true if they were different sets, false if they were already one set. */
    public boolean union(int a, int b) {
        int ra = find(a), rb = find(b);
        if (ra == rb) return false;
        if (size[ra] < size[rb]) { int t = ra; ra = rb; rb = t; }   // ra is the larger tree
        parent[rb] = ra;
        size[ra] += size[rb];
        sets--;
        return true;
    }

    public boolean connected(int a, int b) {
        return find(a) == find(b);
    }

    /** Number of sets now. */
    public int count() {
        return sets;
    }
}
