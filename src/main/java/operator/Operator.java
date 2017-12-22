package operator;

import java.util.List;

/**
 * Created by massimo on 22/12/17.
 */
public interface Operator {

    double execute(List<Double> inputData);
    int getSize();
    int getSlide();

}
