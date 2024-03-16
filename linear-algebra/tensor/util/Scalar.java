package tensor.util;

/**
 * Обёртка над числовым типом
 * (многоиндексный объект нулевой размерности)
  */
public class Scalar extends MultiindexObject {
    public double value;

    public Scalar(double value) {
        super(0);
        this.value = value;
    }

    @Override
    protected Scalar accessImpl(int... indices) {
        return this;
    }

    @Override
    public void multiply(Scalar value) {
        this.value *= value.value;
    }
}
