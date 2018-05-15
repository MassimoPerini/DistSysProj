package operator.types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supervisor.Position;

import java.util.List;
import java.util.Map;

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
    public Sum(int size, int slide, @Nullable Position source, @NotNull List<List<Position>> destination,
               List<Position> exactPosition) {
        super(destination, size, slide, source, exactPosition);
    }


    protected float operationType(List<Float> streamDatas){
        return streamDatas.stream().reduce(Float::sum).orElse((float)0.0);
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
