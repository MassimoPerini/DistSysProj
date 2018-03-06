package operator.types;

import operator.communication.message.MessageData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.Debug;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by higla on 06/03/18.
 *
 * This class represents the "Min" operator
 *
 */
public class Min extends OperatorType {

    /***
     *
     * @param size The number of items that will be summed
     * @param slide The slide of the sum
     */
    public Min(int size, int slide, @Nullable SocketRepr source, @NotNull List<SocketRepr> destination) {
        super(destination, size, slide, source);
    }

    protected double operationType(List<Double> streamDatas){
        return streamDatas.stream().reduce(Double::min).orElse(0.0);
    }


    @Override
    public String toString() {
        return "Min{" +
                "size=" + size +
                ", slide=" + slide +
                '}';
    }

}
