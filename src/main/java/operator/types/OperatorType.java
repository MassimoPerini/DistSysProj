package operator.types;

import java.util.List;

/**
 * Created by massimo on 11/02/18.
 */
public abstract class OperatorType {

    int size;
    int slide;

    abstract double execute(List<Double> inputData);
    abstract int getSize();
    abstract int getSlide();

}
