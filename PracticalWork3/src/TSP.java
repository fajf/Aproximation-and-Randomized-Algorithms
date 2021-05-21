import java.util.Random;

public class TSP {
    // generates matrix for n-numbers of cities
    public static int[][] generateInstance(int n, int bound, int seed)
    {
        // if bound value is 0, set default 5000
        bound = bound == 0 ? 5000 : bound;

        Random random;
        if (seed != 0) {
            random = new Random(seed);
        } else {
            random = new Random();
        }

        int[][] matrix = new int[n][n];

        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++)
            {
                matrix[i][j] = random.nextInt(bound);
            }
        }

        return matrix;
    }
}
