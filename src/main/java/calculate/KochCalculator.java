package calculate;

import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;

public class KochCalculator extends Task<ArrayList<Edge>> implements Observer {
    private KochFractal fractal;
    private KochManager kochManager;
    private Side side;
    ArrayList<Edge> edges;
    int count = 0;
    private int nrOfEdges;

    public KochCalculator(KochManager kochManager, Side side, int level) {
        this.kochManager = kochManager;
        this.side = side;
        this.fractal = new KochFractal(kochManager, this);
        this.edges = new ArrayList<>();
        fractal.setLevel(level);
        nrOfEdges = fractal.getNrOfEdges() / 3;
    }



    public void addEdge(Edge e){}

    @Override
    protected ArrayList<Edge> call() throws Exception {
        switch (side){
            case LEFT:
                fractal.generateLeftEdge(edges);
                break;
            case BOTTOM:
                fractal.generateBottomEdge(edges);
                break;
            case RIGHT:
                fractal.generateRightEdge(edges);
                break;
        }
        return edges;
    }

    @Override
    public void done(){
        super.done();
        try{
            kochManager.threadDone();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(kochManager.count);
        System.out.println("Finished: " + this.getTitle());
    }

    public void cancelled(){
        super.cancel();
        fractal.cancel();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Edge){
            Edge edge = (Edge) arg;
            edges.add(edge);
            count++;
            updateProgress(count, nrOfEdges);
            updateMessage("Right: " + count);
        }
    }
}
