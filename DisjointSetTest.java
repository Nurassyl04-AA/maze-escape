/**
 * Checks of Mission 0. Run:  java -cp out DisjointSetTest
 * Add your own checks below: a large chain, a union of a set with itself, count after many unions.
 */
public class DisjointSetTest {

    static int failed = 0;

    static void check(boolean ok, String what) {
        System.out.println((ok ? "ok    " : "FAIL  ") + what);
        if (!ok) failed++;
    }

    public static void main(String[] args) {
        DisjointSet d = new DisjointSet(6);
        check(d.count() == 6, "6 sets at the start");
        check(!d.connected(0, 1), "0 and 1 are apart at the start");
        check(d.union(0, 1), "union(0, 1) joins two sets");
        check(d.connected(0, 1), "0 and 1 are together");
        check(!d.union(1, 0), "union(1, 0) again returns false");
        check(d.count() == 5, "5 sets after one union");
        d.union(2, 3); d.union(1, 3);
        check(d.connected(0, 2), "0 and 2 are together through 1 and 3");
        check(d.count() == 3, "3 sets: {0,1,2,3} {4} {5}");
        check(d.find(4) == 4 && d.find(5) == 5, "a single cell is its own root");

        // my own checks
        DisjointSet big = new DisjointSet(1_000_000);
        for (int i = 0; i + 1 < 1_000_000; i++) big.union(i, i + 1);   // one long chain
        check(big.count() == 1, "a chain of 1,000,000 unions gives one set");
        check(big.connected(0, 999_999), "the ends of the long chain are connected (no stack overflow)");

        DisjointSet e = new DisjointSet(4);
        check(!e.union(2, 2), "union of a cell with itself returns false");
        check(e.count() == 4, "count does not change after union(2, 2)");
        check(e.connected(3, 3), "a cell is connected to itself");

        DisjointSet c = new DisjointSet(10);
        int joined = 0;
        for (int i = 0; i < 9; i++) if (c.union(i, (i * 7 + 3) % 10)) joined++;
        check(c.count() == 10 - joined, "count = n - number of successful unions");

        DisjointSet s = new DisjointSet(8);
        s.union(0, 1); s.union(2, 3); s.union(4, 5); s.union(0, 2); s.union(0, 4);
        check(s.find(1) == s.find(5) && s.find(3) == s.find(4), "unions of unions end in one root");
        check(!s.connected(0, 6) && !s.connected(6, 7), "untouched cells stay apart");

        System.out.println(failed == 0 ? "All checks passed." : failed + " checks failed.");
    }
}
