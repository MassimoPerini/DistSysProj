package operator.types;

import operator.communication.message.MessageData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supervisor.Position;
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
    public Min(int size, int slide, @Nullable Position source, @NotNull List<Position> destination) {
        super(destination, size, slide, source);
    }

    protected float operationType(List<Float> streamDatas){
        return streamDatas.stream().reduce(Float::min).orElse((float)0.0);
    }


    @Override
    public String toString() {
        return "Min{" +
                "size=" + size +
                ", slide=" + slide +
                '}';
    }

}
