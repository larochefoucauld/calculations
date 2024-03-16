package tensor.util.itertools;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Герератор перестановок целочисленных массивов
 */
public class Permutations {

    /**
     * Генерация всех перестановок первых n
     * неотрицательных целых чисел чисел
     */
    public static ArrayList<int[]> generateAll(final int size) {
        ArrayList<int[]> all = new ArrayList<>();
        boolean[] mask = new boolean[size];
        Arrays.fill(mask, true);
        generateImpl(all, new int[size], 0, Arrays.copyOf(mask, mask.length), mask);
        return all;
    }

    /**
     * Вычисляет чётность перестановки первых n
     * неотрицательных целых чисел
     */
    public static int getParity(int[] permutation) {
        int res = 0;
        for (int i = 0; i < permutation.length; i++) {
            for (int j = 0; j < i; j++) {
                if (permutation[j] > permutation[i]) {
                    res++;
                }
            }
        }
        return res % 2 == 0 ? 1 : -1;
    }

    /**
     * Генерация всех перестановок первых n
     * неотрицательных целых чисел на некотором
     * подмножестве позиций (числа на остальных
     * позициях зафиксированы)
     */
    public static ArrayList<int[]> generate(boolean[] mask) {
        ArrayList<int[]> all = new ArrayList<>();
        generateImpl(all, new int[mask.length], 0,
                Arrays.copyOf(mask, mask.length), mask);
        return all;
    }

    /**
     * Вычисляет чётность перестановки первых n
     * неотрицательных целых чисел на некотором
     * подмножестве позиций
     */
    public static int getParity(int[] permutation, boolean[] mask) {
        int res = 0;
        for (int i = 0; i < permutation.length; i++) {
            if (!mask[i]) continue;
            for (int j = 0; j < i; j++) {
                if (mask[j] && permutation[j] > permutation[i]) {
                    res++;
                }
            }
        }
        return res % 2 == 0 ? 1 : -1;
    }

    /**
     * Переставляет элементы произвольного числового массива
     * в соответствии с переданной перестановкой первых n
     * неотрицательных целых чисел
     */
    public static int[] substitute(int[] permutation, final int[] names) {
        for (int i = 0; i < permutation.length; i++) {
            permutation[i] = names[permutation[i]];
        }
        return permutation;
    }

    /**
     * Код, реализующий генерацию всех перестановок
     * первых n неотрицательных целых чисел по некоторому
     * подмножеству позиций
     */
    private static void generateImpl(
            ArrayList<int[]> all, int[] cur,
            int border, boolean[] useable, final boolean[] mask) {
        if (border == cur.length) {
            all.add(Arrays.copyOf(cur, cur.length));
            return;
        }
        if (!mask[border]) {
            cur[border] = border;
            generateImpl(all, cur, border + 1, useable, mask);
        }
        for (int i = 0; i < cur.length; i++) {
            if (!useable[i]) {
                continue;
            }
            cur[border] = i;
            useable[i] = false;
            generateImpl(all, cur, border + 1, useable, mask);
            useable[i] = true;
        }
    }
}
