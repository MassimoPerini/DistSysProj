package operator.types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by higla on 06/03/18.
 *
 * This class represents the "Max" operator
 *
 */
public class Max extends OperatorType {

    /**
     * @param size The number of items that will be summed
     * @param slide The slide of the sum
     */
    public Max(int size, int slide, @Nullable SocketRepr source, @NotNull List<SocketRepr> destination) {
        super(destination, size, slide, source);
    }

    protected double operationType(List<Double> streamDatas){
        return streamDatas.stream().reduce(Double::max).orElse(0.0);
    }


    @Override
    public String toString() {
        return "Max{" +
                "size=" + size +
                ", slide=" + slide +
                '}';
    }

}
