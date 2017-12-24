package operator;

import java.util.List;

/**
 * Created by massimo on 22/12/17.
 */
public abstract class Operator {
    protected int size;
    protected int slide;

    abstract double execute(List<Double> inputData);
    abstract int getSize();
    abstract int getSlide();

}
