package operator.types;

import java.util.List;

/**
 * Created by massimo on 11/02/18.
 *
 * Represents the generic type of operator
 *
 */
public abstract class OperatorType {

    int size;
    int slide;

    /***
     *
     * @param inputData source of data
     * @return result of the computation
     */

    abstract double execute(List<Double> inputData);
    abstract int getSize();
    abstract int getSlide();

}
