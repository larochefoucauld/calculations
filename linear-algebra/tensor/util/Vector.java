package tensor.util;

import java.util.Arrays;

/**
 * Одномерная строка, хранящая многоиндексные объекты
 */
public class Vector extends MultiindexObject{
    private MultiindexObject[] items;

    public Vector(final int dimension, final int range) {
        super(dimension);
        items = new MultiindexObject[range];
    }

    /**
     * Одномерная адресация (изменение i-го объекта вектора)
     */
    public void setItem(final int i, MultiindexObject item) {
        if (item.dimension != dimension - 1) {
            throw new IllegalArgumentException("Inconsistent dimensions");
        }
        items[i] = item;
    }

    /**
     * Одномерная адресация (получение i-го объекта вектора)
     */
    protected MultiindexObject getItem(final int i) {
        return items[i];
    }

    @Override
    protected Scalar accessImpl(int... indices) {
        return items[indices[0]].accessImpl(
                Arrays.copyOfRange(
                        indices, 1, indices.length
                )
        );
    }

    /**
     * Реализация умножения на скаляр для вектора
     */
    @Override
    public void multiply(Scalar value) {
        for (MultiindexObject item : items) {
            item.multiply(value);
        }
    }
}
