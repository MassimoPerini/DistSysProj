package operator.types;

import operator.communication.message.MessageData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.Debug;

import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

/**
 * Created by massimo on 11/02/18.
 *
 * This class represents the "Sum" operator
 *
 */
public class Sum extends OperatorType {

    /***
     *
     * @param size The number of items that will be summed
     * @param slide The slide of the sum
     */
    public Sum(int size, int slide, @Nullable SocketRepr source, @NotNull List<SocketRepr> destination) {
        super(destination, size, slide, source);
    }

    public synchronized void execute() {
        while (true) {
            while (messageDatas.size() < this.size) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Debug.printDebug(e);
                }
            }

            System.out.println("Starting elaboration...");

            List<MessageData> currentMsg = messageDatas.subList(0,size);

            Stream<MessageData> currentM = currentMsg.stream();
            double result = currentM.mapToDouble(MessageData::getValue).sum();
            MessageData messageData = new MessageData(result);
            messageDatas.subList(0, slide).clear();

            executorService.submit(() -> sendMessage(messageData));

        }

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sum sum = (Sum) o;

        if (size != sum.size) return false;
        return slide == sum.slide;
    }

    @Override
    public int hashCode() {
        int result = size;
        result = 31 * result + slide;
        return result;
    }

    @Override
    public String toString() {
        return "Sum{" +
                "size=" + size +
                ", slide=" + slide +
                '}';
    }

}
