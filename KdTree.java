/* *****************************************************************************
 *  My solution to Princeton's Kd-Trees problem using a Binary Search Tree with two-dimensional keys
 *  Problem spec can be found here: https://coursera.cs.princeton.edu/algs4/assignments/kdtree/specification.php
 *  Nearly all of the code below is my own
 **************************************************************************** */

import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.StdOut;

public class KdTree {
    private Node root;

    private static class Node {
        private Point2D p;
        private RectHV rect;
        private Node lb;
        private Node rt;

        public Node(Point2D p, RectHV rect) {
            this.p = p;
            this.rect = rect;
        }
    }

    private int n;
    private Stack<Point2D> stack;
    private Point2D nearestPoint;

    public KdTree() {
        n = 0;
    }

    public boolean isEmpty() {
        return n == 0;
    }

    public int size() {
        return n;
    }

    // Start the insert with a root whose rectangle covers the full grid.
    // Then call insert recursively
    public void insert(Point2D p) {
        if (p == null) throw new IllegalArgumentException("calls insert() with a null key");
        RectHV rect = new RectHV(0.0, 0.0, 1.0, 1.0);
        root = insert(root, p, true, rect);
    }

    // recursive insert that alternates betw dividing by left/right and top/bottom
    private Node insert(Node x, Point2D p, boolean xNow, RectHV rect) {
        if (x == null) {
            n++;
            return new Node(p, rect);
        }
        double cmp;
        RectHV rectL;
        RectHV rectR;
        if (xNow) {
            cmp = p.x() - x.p.x();
            rectL = new RectHV(rect.xmin(), rect.ymin(), x.p.x(), rect.ymax());
            rectR = new RectHV(x.p.x(), rect.ymin(), rect.xmax(), rect.ymax());
        }
        else {
            cmp = p.y() - x.p.y();
            rectL = new RectHV(rect.xmin(), rect.ymin(), rect.xmax(), x.p.y());
            rectR = new RectHV(rect.xmin(), x.p.y(), rect.xmax(), rect.ymax());
        }
        if (cmp < 0) x.lb = insert(x.lb, p, !xNow, rectL);
        else x.rt = insert(x.rt, p, !xNow, rectR);
        return x;
    }

    public boolean contains(Point2D p) {
        if (p == null) throw new IllegalArgumentException("argument to contains() is null");
        return contains(root, p, true);
    }

    private boolean contains(Node x, Point2D p, boolean xNow) {
        if (x == null) return false;
        double cmp;
        if (xNow) {
            cmp = p.x() - x.p.x();
        }
        else {
            cmp = p.y() - x.p.y();
        }
        if (cmp < 0) return contains(x.lb, p, !xNow);
        else if (cmp > 0) return contains(x.rt, p, !xNow);
        else return true;
    }

    public void draw() {
        draw(root, true);
    }

    // Draws red vertical lines for nodes whose children are sorted by y-coords, and
    // blue horizontal lines for nodes whose children are sorted by x-coords
    private void draw(Node x, boolean xNow) {
        if (x == null) {
            return;
        }
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setPenRadius(0.01);
        StdDraw.point(x.p.x(), x.p.y());
        if (xNow) {
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.setPenRadius();
            StdDraw.line(x.p.x(), x.rect.ymin(), x.p.x(), x.rect.ymax());
        }
        else {
            StdDraw.setPenColor(StdDraw.BLUE);
            StdDraw.setPenRadius();
            StdDraw.line(x.rect.xmin(), x.p.y(), x.rect.xmax(), x.p.y());
        }
        draw(x.lb, !xNow);
        draw(x.rt, !xNow);
    }

    // For checking which points are within a given rectangle
    public Iterable<Point2D> range(RectHV rect) {
        stack = new Stack<Point2D>();
        range(root, rect);
        return stack;
    }

    private void range(Node x, RectHV rect) {
        if (x == null) {
            return;
        }
        if (intersect(x.rect, rect)) {
            if (rectContainsPoint(rect, x.p)) {
                stack.push(x.p);
            }
            range(x.lb, rect);
            range(x.rt, rect);
        }
    }

    // Checks if two rectangles intersect.  Could also be achieved via RectHVs intersects method
    private boolean intersect(RectHV a, RectHV b) {
        if (a.xmax() < b.xmin() || a.ymax() < b.ymin()) {
            return false;
        }
        if (a.xmin() > b.xmax() || a.ymin() > b.ymax()) {
            return false;
        }
        return true;
    }

    // Checks if a rectangle contains a given point.  Could also be achieved via RectHVs contains method
    private boolean rectContainsPoint(RectHV a, Point2D p) {
        if (a.xmin() <= p.x() && a.xmax() >= p.x() && a.ymin() <= p.y() && a.ymax() >= p.y()) {
            return true;
        }
        return false;
    }

    // Recursively finds the nearest point on the tree to the given point
    public Point2D nearest(Point2D p) {
        nearestPoint = new Point2D(root.p.x(), root.p.y());
        nearest(root, p, true);
        return nearestPoint;
    }

    // This function is made more efficient by the fact that we skip any nodes (and their respective subtrees)
    // whose rectangles are further from the given point than the thus far discovered nearest point.
    private void nearest(Node x, Point2D p, boolean xNow) {
        if (x == null) {
            return;
        }
        double nearestDistance = distance(nearestPoint, p);
        if (x.rect.distanceTo(p) > nearestDistance) {
            return;
        }
        double nearestNow = distance(x.p, p);
        if (nearestNow < nearestDistance) {
            nearestPoint = new Point2D(x.p.x(), x.p.y());
        }
        if (xNow) {
            if (p.x() < x.p.x()) {
                nearest(x.lb, p, !xNow);
                nearest(x.rt, p, !xNow);
            }
            else {
                nearest(x.rt, p, !xNow);
                nearest(x.lb, p, !xNow);
            }
        }
        else {
            if (p.y() < x.p.y()) {
                nearest(x.lb, p, !xNow);
                nearest(x.rt, p, !xNow);
            }
            else {
                nearest(x.rt, p, !xNow);
                nearest(x.lb, p, !xNow);
            }
        }

    }

    // Finds the distance betw two points.  Could also be achieved via Point2D's distanceTo method
    private double distance(Point2D a, Point2D b) {
        return (a.x() - b.x()) * (a.x() - b.x()) + (a.y() - b.y()) * (a.y() - b.y());
    }

    // User can add points to a tree via clicking on a generated square
    public static void main(String[] args) {
        RectHV rect = new RectHV(0.0, 0.0, 1.0, 1.0);
        StdDraw.enableDoubleBuffering();
        KdTree kdtree = new KdTree();
        Point2D pLast = new Point2D(0, 0);
        Point2D pNearest = new Point2D(0.50, 0.50);

        while (true) {
            if (StdDraw.isMousePressed()) {
                double x = StdDraw.mouseX();
                double y = StdDraw.mouseY();
                Point2D p = new Point2D(x, y);
                if (rect.contains(p) && p.x() != pLast.x()) {
                    System.out.println("---New Click---");
                    StdOut.printf("New Point: %8.6f %8.6f\n", x, y);
                    kdtree.insert(p);
                    StdDraw.clear();
                    kdtree.draw();
                    StdDraw.show();
                    pLast = p;
                    RectHV testRect = new RectHV(0.0, 0.0, 0.5, 0.5);
                    System.out.println("Points in lower left quadrant: ");
                    for (Point2D point : kdtree.range(testRect)) {
                        System.out.println(point.x() + ":" + point.y());
                    }
                    Point2D nearest = kdtree.nearest(pNearest);
                    System.out.println("Point nearest center: " + nearest.x() + ":" + nearest.y());
                }
            }
            StdDraw.pause(20);
        }

    }
}


