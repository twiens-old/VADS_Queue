import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class Subject extends Thread {

    // time interval at which the onWakeUp method is called
    private static final int WAKE_UP_INTERVAL_MS = 100;

    private BlockingQueue<Object> messageQueue;
    private boolean running;

    public Subject() {
        messageQueue = new LinkedBlockingQueue<Object>();
        running = true;
    }

    protected static void print(String string) {
        synchronized (System.out) {
            System.out.print(string);
        }
    }

    protected static void println(String string) {
        synchronized (System.out) {
            System.out.println(string);
        }
    }

    public final void send(Object msg) {
        try {
            messageQueue.put(msg);
        } catch (InterruptedException e) {
            System.err.println("send interrupted");
            e.printStackTrace();
        }
    }

    public final void run() {
        init();
        long lastWakeUpTime = System.currentTimeMillis();

        while (isRunning()) {
            long timeSinceLastWakeUp = System.currentTimeMillis() - lastWakeUpTime;
            long waitTime = Math.max(WAKE_UP_INTERVAL_MS - timeSinceLastWakeUp, 0);

            if (waitTime <= 0) {
                onTimeout();
                lastWakeUpTime = System.currentTimeMillis();
                waitTime = WAKE_UP_INTERVAL_MS;
            }

            try {
                Object message = messageQueue.poll(WAKE_UP_INTERVAL_MS, TimeUnit.MILLISECONDS);
                if (message != null) {
                    onMessageReceived(message);
                }
            } catch (InterruptedException e) {
                System.err.println("receive interrupted");
                e.printStackTrace();
            }
        }
    }

    protected final synchronized void stopSubject() {
        running = false;
    }

    private synchronized boolean isRunning() {
        return running;
    }

    protected abstract void init();

    protected abstract void onMessageReceived(Object message);

    protected abstract void onTimeout();
}
