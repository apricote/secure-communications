package QueueHandler;

import Implementations.Message;

import java.util.concurrent.BlockingQueue;

public class MessageQueueHandler extends Thread {
    private BlockingQueue<Message> generalMessageQueue;
    private BlockingQueue<Message> keyGenerationMessageQueue;
    private BlockingQueue<Message> userMessageQueue;

    public MessageQueueHandler(BlockingQueue<Message> generalMessageQueue,
                               BlockingQueue<Message> keyGenerationMessageQueue,
                               BlockingQueue<Message> userMessageQueue) {
        this.generalMessageQueue = generalMessageQueue;
        this.keyGenerationMessageQueue = keyGenerationMessageQueue;
        this.userMessageQueue = userMessageQueue;
    }

    @Override
    public void run() {
        while(true) {
            try {
                Message oldestMessage = generalMessageQueue.take();

                if (oldestMessage.isText) {
                    userMessageQueue.put(oldestMessage);
                } else {
                    keyGenerationMessageQueue.put(oldestMessage);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
