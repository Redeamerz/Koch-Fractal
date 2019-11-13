/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calculate;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import fun3kochfractalfx.FUN3KochFractalFX;
import javafx.application.Platform;
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
    public volatile AtomicInteger count = new AtomicInteger(0);

    private Thread t1;
    private Thread t2;
    private Thread t3;
    private KochCalculator calcLeft;
    private KochCalculator calcBottom;
    private KochCalculator calcRight;
    ExecutorService threadpool;

    public KochManager(FUN3KochFractalFX application) {
        this.application = application;
        this.tsCalc = new TimeStamp();
        this.tsDraw = new TimeStamp();
        this.edges = new ArrayList<Edge>();
    }

    private ArrayList<Edge> leftEdge = new ArrayList<>();
    private ArrayList<Edge> bottomEdge = new ArrayList<>();
    private ArrayList<Edge> rightEdge = new ArrayList<>();

    public void changeLevel(int nxt) {
        cancelThreadsIfRunning();
        edges.clear();

        tsCalc.init();


        calcLeft = new KochCalculator(this, Side.LEFT, nxt);
        calcBottom = new KochCalculator(this, Side.BOTTOM, nxt);
        calcRight = new KochCalculator(this, Side.RIGHT, nxt);

        threadpool = Executors.newFixedThreadPool(3);
        application.bindCalcProgressToProgressBar(calcLeft, calcBottom, calcRight);

        tsCalc.setBegin("Begin calculating");

        threadpool.submit(calcLeft);
        threadpool.submit(calcBottom);
        threadpool.submit(calcRight);

        count.set(0);
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

    private void cancelThreadsIfRunning(){
        if(calcLeft != null || calcBottom != null || calcRight != null) {
            calcLeft.cancel();
            calcRight.cancel();
            calcBottom.cancel();
        }
    }

    public int getNrOfEdges(){
        return (int) (3 * Math.pow(4, level - 1));
    }

    public void threadDone() throws ExecutionException, InterruptedException {
        count.incrementAndGet();

        if(count.get() == 3){
            count.set(0);
            edges.clear();
            edges.addAll(calcLeft.get());
            edges.addAll(calcBottom.get());
            edges.addAll(calcRight.get());

            tsCalc.setEnd("End Calculating");

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    application.setTextCalc(tsCalc.toString());
                }
            });
        }
        application.requestDrawEdges();

        stop();
    }

    public void stop(){
        threadpool.shutdown();
    }
}
