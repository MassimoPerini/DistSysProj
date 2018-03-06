package operator.types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by Fulvio on 06/03/18.
 *
 * This class represents the "avg" operator
 *
 */
public class Avg extends OperatorType {

    /**
     * @param size The number of items that will be summed
     * @param slide The slide of the sum
     */
    public Avg(int size, int slide, @Nullable SocketRepr source, @NotNull List<SocketRepr> destination) {
        super(destination, size, slide, source);
    }

    protected double operationType(List<Double> streamDatas){
        return streamDatas.stream().reduce(Double::sum).orElse(0.0)/this.getSize();
    }


    @Override
    public String toString() {
        return "Avg{" +
                "size=" + size +
                ", slide=" + slide +
                '}';
    }

}
