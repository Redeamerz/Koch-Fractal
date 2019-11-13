package calculate;

public class KochThread implements Runnable {
    KochFractal koch;
    public KochThread(int level) {
        koch = new KochFractal(this);
        koch.setLevel(level);
    }

    @Override
    public void run() {

    }

    public void addEdge(Edge e){}

    public void cancel(){
        koch.cancel();
    }
}
