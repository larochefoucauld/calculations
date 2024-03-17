package tensor;

import tensor.util.*;
import tensor.util.itertools.*;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Абстракция, реализующая основные опреации тензорной алгебры.
 * Координаты тензора хранятся в разработанной для этой цели
 * многоиндексной коллекции, обращение к ним осуществляется по набору индексов.
 * Перебор координат для выполнения вычислений осуществляется при помощи
 * дополнительных абстракций, позволяющих генерировать специфические
 * наборы индексов (монотонные последовательности, перестановки).
 */
public class Tensor {
    private MultiindexObject coordinates; // координаты тензора
    private final int p; // валентность
    private final int q; // валентность
    private final int arity; // p + q
    private final int n;  // размерность X

    /**
     * Конструирует тензор валентности (p, q) над линейным пространством
     * размерности n, координаты инициализируются нулями
     */
    public Tensor(final int p, final int q, final int n) {
        this.p = p;
        this.q = q;
        this.n = n;
        arity = p + q;
        coordinates = MultiindexObject.deepConstruct(arity, n);
    }

    /**
     * Строит тензор по переданной матрице координат. Матрица читается следующим образом:
     * 1-й индекс - строка, 2-й индекс - столбец, 3-й индекс - номер слоя 1-го уровня
     * по горизонтали, 4-й индекс - номер слоя 1-го уровня по вертикали, 5-й индекс -
     * номер слоя 2-го уровня по горизонтали и т. д.
     */
    public Tensor(final int[][] matrix, final int p, final int q, final int n) {
        this.p = p;
        this.q = q;
        this.n = n;
        arity = p + q;
        coordinates = fromMatrix(matrix, 0, 0, 0);
    }

    /**
     * Вычисляет значение полилинейной формы, определяемой
     * координатами тензора на переданном наборе векторов
     */
    public double applyAsMLF(final int[]... vectors) {
        if (vectors.length != arity) {
            throw new IllegalArgumentException("Invalid argument list");
        }
        double res = 0;
        for (int[] i : IndexFactory.generateAll(arity, n)) {
            res += multiplyComponents(i, vectors) * coordinates.access(i).value;
        }
        return res;
    }

    /**
     * Вычисляет для данных
     * <p>i_1 ... i_p, j_1, ... j_q = 1 ... n
     * <p>и x_1 ... x_p, y^1 ... y^q = 1 ... n
     * <p>(f^{i_1} * f^{i_2} * ... * f^{i_p} *
     * g_{j_1} * g_{j_2} * ... * g_{j_q}) (x_1, x_2, ..., x_p, y^1, ..., y^q),
     * <p>где {f^i} - базис X*,
     * {g_j} - базис X**
     */
    private double multiplyComponents(final int[] indices, final int[][] vectors) {
        double res = 1;
        // В цикле перебираются все векторы,
        // вычисляется произведение требуемых координат
        for (int i = 0; i < arity; i++) {
            res *= vectors[i][indices[i]];
        }
        return res;
    }

    /**
     * Находит разложение антисимметричного тензора по базису
     * подпространства антисимметричных тензоров данной валентности
     */
    public HashMap<int[], Double> decomposeAsym() {
        HashMap<int[], Double> res = new HashMap<>();
        // Перебираем только монотонные наборы индексов
        for (int[] i : IndexFactory.generateMonotonic(arity, n)) {
            // Коэффициенты в разложении по базису антисимметричных тензоров
            // совпадают с соответсвующими в разложении по базису
            // пространства тензоров - следствие из доказательства теоремы о базисе
            // пространства антисимметричных тензоров
            res.put(i, coordinates.access(i).value);
        }
        return res;
    }

    /**
     * Симметризует тензор по всем индексам
     */
    public Tensor sym() {
        boolean[] permutable = new boolean[arity];
        Arrays.fill(permutable, true);
        return sym(permutable);
    }

    /**
     * Симметризует тензор по определённому поднабору индексов
     */
    public Tensor sym(boolean[] permutable) {
        if (permutable.length != arity) {
            throw new IllegalArgumentException("Invalid symmetrizing mask");
        }
        Tensor res = new Tensor(p, q, n);
        // Перебираем все наборы индексов
        for (int[] i : IndexFactory.generateAll(arity, n)) {
            double cur = 0; // текущий результат
            // Перебираем все перестановки данного набора
            // по симметризуемым позициям
            for (int[] p : Permutations.generate(permutable)) {
                // Суммируем координаты исходного тензора
                cur += coordinates.access(
                        Permutations.substitute(p, i)
                ).value;
            }
            // Записываем посчитанное значение в результат по
            // соответствующим индексам
            res.coordinates.access(i).value = cur;
        }
        // Вычисляем коэффициент нормировки и домножаем на него результат
        int count = 0;
        for (boolean b : permutable) {
            if (b) count++;
        }
        res.multiply(new Scalar(1.0 / factorial(count)));
        return res;
    }

    /**
     * Альтернирует тензор по всем индексам
     */
    public Tensor alt() {
        boolean[] permutable = new boolean[arity];
        Arrays.fill(permutable, true);
        return alt(permutable);
    }

    /**
     * Альтернирует тензор по определённому поднабору индексов
     */
    public Tensor alt(boolean[] permutable) {
        if (permutable.length != arity) {
            throw new IllegalArgumentException("Invalid alternating mask");
        }
        Tensor res = new Tensor(p, q, n);
        // Перебираем все монотонные (по альтернируемым позициям)
        // наборы индексов
        for (int[] i : IndexFactory.generateMonotonic(permutable, n)) {
            double cur = 0; // текущий результат
            // Перебираем все перестановки данного монотонного набора
            // по альтернируемым позициям
            for (int[] p : Permutations.generate(permutable)) {
                // Суммируем координаты исходного тензора, взятые со знаком,
                // соответствующим чётности перестановки
                cur += (double) Permutations.getParity(p, permutable) *
                        coordinates.access(
                                Permutations.substitute(p, i)
                        ).value;
            }
            // Выражаем координаты, индексируемые всеми
            // перестановками текущего монотонного
            // набора через <знак перестановки> * <вычисленное значение
            // для монотонного набора>, пользуясь антисимметричностью
            // альтернированного тензора
            for (int[] p : Permutations.generate(permutable)) {
                int parity = Permutations.getParity(p, permutable);
                res.coordinates.access(
                        Permutations.substitute(p, i)
                ).value = parity * cur;
            }
        }
        // Вычисляем коэффициент нормировки и домножаем на него результат
        int count = 0;
        for (boolean b : permutable) {
            if (b) count++;
        }
        res.multiply(new Scalar(1.0 / factorial(count)));
        return res;
    }

    /**
     * Перемножает два тензора
     */
    public Tensor tensorProduct(Tensor rhs) {
        if (n != rhs.n) {
            throw new IllegalArgumentException("Invalid dimension");
        }
        Tensor res = new Tensor(p + rhs.p, q + rhs.q, n);
        for (int[] i : IndexFactory.generateAll(arity, n)) {
            for (int[] j : IndexFactory.generateAll(rhs.arity, n)) {
                int[] resultInd = new int[arity + rhs.arity];
                // [i^1, ... i^q1, i_1, ..., i_p1], [j^1, ... j^q2, j_1, ... j_p2] ->
                // -> [i^1, ..., i^q1, j^1, ..., j^q2, i_1, ..., i_p1, j_1, ..., j_p2]
                System.arraycopy(i, 0, resultInd, 0, q);
                System.arraycopy(j, 0, resultInd, q, rhs.q);
                System.arraycopy(i, q, resultInd, q + rhs.q, p);
                System.arraycopy(j, rhs.q, resultInd, q + rhs.q + p, rhs.p);
                // T[i^1, ..., i^q1, j^1, ..., j^q2, i_1, ..., i_p1, j_1, ..., j_p2] =
                // = T1[i^1, ... i^q1, i_1, ..., i_p1] * T2[j^1, ... j^q2, j_1, ... j_p2]
                res.coordinates.access(resultInd).value =
                        coordinates.access(i).value * rhs.coordinates.access(j).value;
            }
        }
        return res;
    }

    /**
     * Вычисляет внешнее произведение тензоров.
     * Антисимметричность операндов не проверяется
     */
    public Tensor wedgeProduct(Tensor rhs) {
        if (n != rhs.n) {
            throw new IllegalArgumentException("Invalid dimension");
        }
        Tensor product = tensorProduct(rhs); // u * v
        // u ^ v = (p1 + p2)! / (p1! * p2!) * Alt(u * v)
        return product.alt().multiply(
                new Scalar((double) factorial(arity + rhs.arity) /
                        (factorial(arity) * factorial(rhs.arity))
                )
        );
    }

    /**
     * Умножает координаты тензора на скаляр
     */
    public Tensor multiply(Scalar value) {
        coordinates.multiply(value);
        return this;
    }

    public Tensor add(Tensor rhs) {
        if (rhs.n != n || rhs.p != p || rhs.q != q) {
            throw new IllegalArgumentException("Invalid operand");
        }
        for (int[] i : IndexFactory.generateAll(arity, n)) {
            coordinates.access(i).value += rhs.coordinates.access(i).value;
        }
        return this;
    }

    /**
     * Позволяет печатать координаты тензора в виде человекочитаемой матрицы
     */
    @Override
    public String toString() {
        double[][] matrix = toMatrix();
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length - 1; j++) {
                res.append((float) matrix[i][j]).append(", ");
            }
            if (matrix[0].length > 0) {
                res.append((float) matrix[i][matrix[0].length - 1]).append(";\n");
            }
        }
        return res.toString();
    }

    /**
     * Осуществляет "упаковку" координат тензора в матрицу
     */
    public double[][] toMatrix() {
        double[][] matrix = new
                double[(int) Math.pow(n, arity / 2)][(int) Math.pow(n, (arity + 1) / 2)];
        toMatrixImpl(matrix, 0, 0, 0, new int[arity]);
        return matrix;
    }

    /**
     * Код, реализуюший "упаковку" координат тензора в матрицу
     */
    private void toMatrixImpl(double[][] matrix, int iShift, int jShift, int depth, int[] indices) {
        if (depth == arity) {
            matrix[iShift][jShift] = coordinates.access(indices).value;
            return;
        }
        final int layerLength = (int) Math.pow(n, depth / 2);
        boolean iterateByI = iterateRows(depth);
        int shift = iterateByI ? iShift : jShift;
        for (int i = 0; i < n; i++, shift += layerLength) {
            indices[depth] = i;
            if (iterateByI) {
                toMatrixImpl(matrix, shift, jShift, depth + 1, indices);
            } else {
                toMatrixImpl(matrix, iShift, shift, depth + 1, indices);
            }
        }
    }


    /**
     * Код, реализуюший "распаковку" координат тензора из матрицы
     */
    private MultiindexObject fromMatrix(
            final int[][] matrix, int iShift,
            int jShift, int depth) {
        if (depth == arity) {
            return new Scalar(matrix[iShift][jShift]);
        }
        Vector v = new Vector(arity - depth, n);
        final int layerLength = (int) Math.pow(n, depth / 2);
        boolean iterateByI = iterateRows(depth);
        int shift = iterateByI ? iShift : jShift;
        for (int i = 0; i < n; i++, shift += layerLength) {
            if (iterateByI) {
                v.setItem(i, fromMatrix(matrix, shift, jShift, depth + 1));
            } else {
                v.setItem(i, fromMatrix(matrix, iShift, shift, depth + 1));
            }
        }
        return v;
    }

    /**
     * Позволяет печатать координаты тензора в одну строчку
     */
    public String toLine() {
        double[][] matrix = toMatrix();
        StringBuilder res = new StringBuilder("[");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length - 1; j++) {
                res.append((float) matrix[i][j]).append(", ");
            }
            res.append((float) matrix[i][matrix[0].length - 1]).append("; ");
        }
        res.replace(res.length() - 2, res.length(), "]");
        return res.toString();
    }

    /**
     * Позволяет печатать в понятной форме разложение
     * по базису пространства (антисимметричных) тензоров
     */
    public static String decompositionToString(HashMap<int[], Double> decomposition) {
        StringBuilder res = new StringBuilder();
        for (int[] ind : decomposition.keySet()) {
            if (decomposition.get(ind) == 0) {
                continue;
            }
            res.append(decomposition.get(ind));
            res.append(" ").append("[");
            for (int i = 0; i < ind.length - 1; i++) {
                res.append(ind[i] + 1).append(", ");
            }
            res.append(ind[ind.length - 1] + 1).append("]\n");
        }
        return res.toString();
    }

    /**
     * Вспомогательная функция для взаимодействия с матрицей координат.
     * Позволяет понять, по какой оси (строки/столбцы) осуществлять
     * чтение/запись на текущей размерности слоя
     */
    private boolean iterateRows(int indexPos) {
        return indexPos == 0 || (indexPos != 1 && indexPos % 2 != 0);
    }

    private int factorial(int n) {
        if (n == 0) {
            return 1;
        }
        return n * factorial(n - 1);
    }
}
