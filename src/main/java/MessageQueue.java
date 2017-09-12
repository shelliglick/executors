import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class MessageQueue {

    private BlockingQueue<String> queue;

    public MessageQueue() {
        this.queue = new ArrayBlockingQueue<String>(5);
    }

    public static void main(String[] args) {
        MessageQueue messageQueue = new MessageQueue();
        messageQueue.test();
    }

    public void test() {
        ExecutorService consumersPool = Executors.newFixedThreadPool(2);
        ExecutorService producersPool = Executors.newFixedThreadPool(2);

        consume[] consumer = new consume[]{
                new consume(), new consume()
        };

        Future<String> consumed1 = consumersPool.submit(consumer[0]);
        Future<String> consumed2 = consumersPool.submit(consumer[1]);

        Scanner in = new Scanner(System.in);
        while (true) {
            if (in.hasNext()) {
                String msg = in.next();
                producersPool.execute(new produce(msg));
                if (msg.equals("exit")) {
                    break;
                }
            }
        }


        consumer[0].terminate();
        consumer[1].terminate();

        try {
            System.out.println(consumed1.get());
            System.out.println(consumed2.get());
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        }


        consumersPool.shutdown();
        try {
            if (!consumersPool.awaitTermination(3, TimeUnit.SECONDS)) {
                consumersPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        producersPool.shutdown();
        try {
            if (!producersPool.awaitTermination(3, TimeUnit.SECONDS)) {
                producersPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class produce implements Runnable {

        String msg;

        produce(String msg) {
            this.msg = msg;
        }

        public void run() {
            try {
                System.out.println("producing " + msg);
                queue.offer(msg, 500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class consume implements Callable<String> {

        private volatile boolean running = true;

        private void terminate() {
            running = false;
        }

        public String call() throws Exception {
            List<String> all = new ArrayList<String>();
            while (running || queue.size() > 0) {
                String msg = queue.poll(3, TimeUnit.SECONDS);
                if (msg == null) {
                    continue;
                }
                System.out.println("message #: " + msg);
                all.add(msg);
            }
            return all.toString();
        }

    }

}
