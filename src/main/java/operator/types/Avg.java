package operator.types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supervisor.Position;

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
    public Avg(int size, int slide, int parallelizationLevel, @Nullable Position source, @NotNull List<Position> destination,
               Position exactPosition) {
        super(destination, size, slide, parallelizationLevel, source, exactPosition);
    }

    protected float operationType(List<Float> streamDatas){
        return streamDatas.stream().reduce(Float::sum).orElse((float)0.0)/this.getSize();
    }


    @Override
    public String toString() {
        return "Avg{" +
                "size=" + size +
                ", slide=" + slide +
                '}';
    }

}
