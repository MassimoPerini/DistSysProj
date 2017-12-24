package operator;

import java.util.List;

/**
 * Created by massimo on 22/12/17.
 */
public class Sum extends Operator {

    public Sum(int size, int slide) {
        this.size = size;
        this.slide = slide;
    }

    public double execute(List<Double> inputData) {
        if (inputData.size() < this.size || inputData.size() > this.size){
            throw new IllegalArgumentException();
        }
        double result = 0;
        for (Double inputDatum : inputData) {
            result +=inputDatum;
        }
        return result;
    }

    public int getSize() {
        return this.size;
    }

    public int getSlide() {
        return this.slide;
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
