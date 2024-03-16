package tensor.util;

/**
 * Многоиндексная коллекция скаляров
 */
public abstract class MultiindexObject {
    public final int dimension; // количество индексов,
    // по которым адресуются элементы

    protected MultiindexObject(final int dimension) {
        this.dimension = dimension;
    }

    /**
     * Конструирует пустую (инициализированную нулевыми скалярами)
     * коллекцию указанной размерности
     */
    public static MultiindexObject deepConstruct(final int dimension, final int range) {
        if (dimension == 0) {
            return new Scalar(0);
        }
        Vector res = new Vector(dimension, range);
        for (int i = 0; i < range; i++) {
            res.setItem(i, deepConstruct(dimension - 1, range));
        }
        return res;
    }

    /**
     * Глубокая адресация (обращение к элементу коллекции по набору индексв)
     */
    public Scalar access(int ...indices) {
        if (indices.length != dimension) {
            throw new IllegalArgumentException("Inaccessible index");
        }
        return accessImpl(indices);
    }

    protected abstract Scalar accessImpl(int ...indices);

    /**
     * Умножить все элементы коллекции на скаляр
     */
    public abstract void multiply(Scalar value);
}
