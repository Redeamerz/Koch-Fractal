/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calculate;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import fun3kochfractalfx.FUN3KochFractalFX;
import timeutil.TimeStamp;

/**
 *
 * @author Nico Kuijpers
 * Modified for FUN3 by Gertjan Schouten
 */
public class KochManager {

    private ArrayList<Edge> edges;
    private FUN3KochFractalFX application;
    private TimeStamp tsCalc;
    private TimeStamp tsDraw;
    private int level = 0;
    private volatile AtomicInteger count = new AtomicInteger(0);

    private Thread t1;
    private Thread t2;
    private Thread t3;
    private KochThread runnable1;
    private KochThread runnable2;
    private KochThread runnable3;

    public KochManager(FUN3KochFractalFX application) {
        this.application = application;
        this.tsCalc = new TimeStamp();
        this.tsDraw = new TimeStamp();
        this.edges = new ArrayList<Edge>();
    }

    private ArrayList<Edge> leftEdge = new ArrayList<Edge>();
    private ArrayList<Edge> bottomEdge = new ArrayList<Edge>();
    private ArrayList<Edge> rightEdge = new ArrayList<Edge>();

    public void changeLevel(int nxt) {
        cancelThreads();
        level = nxt;
        edges.clear();

        tsCalc.init();
        tsCalc.setBegin("Begin calculating");

        t1 = new Thread(runnable1 = new KochThread(level) {
            @Override
            public void run() {
                koch.generateLeftEdge();
                count.addAndGet(1);
            }
            public void addEdge(Edge e) {
                leftEdge.add(e);
            }
        });

        t2 = new Thread(runnable2 = new KochThread(level) {
            @Override
            public void run() {
                koch.generateBottomEdge();
                count.addAndGet(1);
            }
            public void addEdge(Edge e) {
                bottomEdge.add(e);
            }
        });

        t3 = new Thread(runnable3 = new KochThread(level) {
            @Override
            public void run() {
                koch.generateRightEdge();
                count.addAndGet(1);
            }
            public void addEdge(Edge e) {
                rightEdge.add(e);
            }
        });

        t1.start();
        t2.start();
        t3.start();

        while(count.get() != 3){}

        finishEdgeArray();

        tsCalc.setEnd("End calculating");
        application.setTextNrEdges("" + getNrOfEdges());
        application.setTextCalc(tsCalc.toString());
        drawEdges();
    }

    public void drawEdges() {
        tsDraw.init();
        tsDraw.setBegin("Begin drawing");
        application.clearKochPanel();
        for (Edge e : edges) {
            application.drawEdge(e);
        }
        tsDraw.setEnd("End drawing");
        application.setTextDraw(tsDraw.toString());
    }

    private void finishEdgeArray(){
        edges.addAll(leftEdge);
        edges.addAll(bottomEdge);
        edges.addAll(rightEdge);

        leftEdge.clear();
        bottomEdge.clear();
        rightEdge.clear();

        count.set(0);
    }

    private void cancelThreads(){
        if(t1 != null){
            runnable1.cancel();
        }
        if(t2 != null){
            runnable2.cancel();
        }
        if(t2 != null){
            runnable3.cancel();
        }
    }

    public int getNrOfEdges(){
        return (int) (3 * Math.pow(4, level - 1));
    }
}
