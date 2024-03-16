package tensor.util.itertools;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Генератор наборов индексов
 * для итерации по многоиндексным объектам
 */
public class IndexFactory {
    /**
     * Генерация всех наборов требуемой длины в указанном диапазоне
     */
    public static ArrayList<int[]> generateAll(final int size, final int range) {
        ArrayList<int[]> all = new ArrayList<>();
        generateAllImpl(all, new int[size], 0, range, false);
        return all;
    }

    /**
     * Генерация всех монотонных наборов требуемой длины в указанном диапазоне
     */
    public static ArrayList<int[]> generateMonotonic(final int size, final int range) {
        if (range < size) {
            throw new IllegalArgumentException("Unable to create monotonic index set");
        }
        ArrayList<int[]> all = new ArrayList<>();
        generateAllImpl(all, new int[size], 0, range, true);
        return all;
    }

    /**
     * Генерация всех монотонных по некоторому подмножеству позиций
     * наборов требуемой длины в указанном диапазоне
     */
    public static ArrayList<int[]> generateMonotonic(
            boolean[] mask, final int range) {
        int ordered = 0;
        for (boolean b : mask) {
            if (b) ordered++;
        }
        if (range < ordered) {
            throw new IllegalArgumentException("Unable to create monotonic index subset");
        }
        ArrayList<int[]> all = new ArrayList<>();
        generateMonotonicImpl(all, new int[mask.length],
                0, range, mask, -1);
        return all;
    }

    /**
     * Код, реализующий генерацию всех наборов
     * с указанными параметрами (размер, диапазон, монотонность)
     */
    private static void generateAllImpl(
            ArrayList<int[]> all, int[] cur,
            final int border, final int range, boolean monotonic) {
        if (border == cur.length) {
            all.add(Arrays.copyOf(cur, cur.length));
            return;
        }
        final int from = monotonic && border > 0 ? cur[border - 1] + 1 : 0;
        for (int i = from; i < range; i++) {
            cur[border] = i;
            generateAllImpl(all, cur, border + 1, range, monotonic);
        }
    }

    /**
     * Код, реализующий генерацию всех наборов,
     * монотонных по указанному подмножеству индексов
     */
    private static void generateMonotonicImpl(
            ArrayList<int[]> all, int[] cur, final int border,
            final int range, boolean[] mask,
            int prevChangeable) {
        if (border == cur.length) {
            all.add(Arrays.copyOf(cur, cur.length));
            return;
        }
        final int from = mask[border] ? prevChangeable + 1 : 0;
        for (int i = from; i < range; i++) {
            cur[border] = i;
            generateMonotonicImpl(
                    all, cur, border + 1,
                    range, mask,
                    mask[border] ? i : prevChangeable
            );
        }
    }
}
