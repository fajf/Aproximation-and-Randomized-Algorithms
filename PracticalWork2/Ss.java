import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Ss {

    public static void main(String[] args) throws IOException {
        boolean dyn = false, exh = false, greedy = true, fptas = false; // runtime limiters
        int stEpsilonov = 1;
        int fptasTotalTimeLimit = 1500;
        int dynTimeLimit = 0, exhTimeLimit = 60, greedyTimeLimit = 30, fptasTimeLimit = 60;

        Writer dynWriter = null;
        Writer exhWriter = null;
        Writer greedyWriter = null;
        Writer fptasWriter = null;
        boolean writer = true;
        if (writer) {     // ali zapiši v datoteke
            dynWriter = new FileWriter(new File("D:\\Sola\\4.letnik\\ANA\\PrakticnoDelo2\\src\\DYN.csv"), false);
            dynWriter.write("n,k,miliseconds\n");
            exhWriter = new FileWriter(new File("D:\\Sola\\4.letnik\\ANA\\PrakticnoDelo2\\src\\EXH.csv"), false);
            exhWriter.write("n,k,miliseconds\n");
            greedyWriter = new FileWriter(new File("D:\\Sola\\4.letnik\\ANA\\PrakticnoDelo2\\src\\GREEDY_random.csv"), false);
            greedyWriter.write("n,k,miliseconds\n");
            fptasWriter = new FileWriter(new File("D:\\Sola\\4.letnik\\ANA\\PrakticnoDelo2\\src\\FPTAS.csv"), false);
            fptasWriter.write("n,k,epsilon,miliseconds\n");
        }

        for (int x = (int) Math.pow(2, 10); x <= (int) Math.pow(2, 24); x = x * 2){          // n
            for (int y = x; y <= x; y = (int) Math.round((double)y * 1.25)) {     // k
                String generatedPath = "D:\\Sola\\4.letnik\\ANA\\PrakticnoDelo2\\src\\problems\\" + x + "_" + y + ".txt";

                // poljubno število ponovitev z istimi parametri
                for (int i = 0; i < 10; i++) {
                    generirajProblem(x, y, generatedPath);
                    //generirajWorstProblemEXH(x, y, generatedPath);
                    //generirajWorstProblemGREEDY(x,y, generatedPath);

                    //File file = new File("D:\\Sola\\4.letnik\\ANA\\PrakticnoDelo2\\src\\ss2.txt");
                    File file = new File(generatedPath);
                    Scanner scanner = new Scanner(file);

                    ArrayList<Integer> read = new ArrayList<>();
                    final int n, k;

                    // preberi n & k
                    n = scanner.nextInt();
                    k = scanner.nextInt();

                    System.out.println("n: " + n + ", k: " + k);

                    while (scanner.hasNextInt()) {
                        read.add(scanner.nextInt());
                    }

                    final ArrayList<Integer> a = new ArrayList<>(read);

                    try {
                        boolean print = true;
                        if (dyn) {
                            if (izvediDYN(n, k, a, print, dynWriter, writer, dynTimeLimit) == -1) dyn = false;
                        }
                        if (exh) {
                            if (izvediEXH(k, a, print, exhWriter, writer, exhTimeLimit) == -1) exh = false;
                        }
                        if (greedy) {
                            if (izvediGREEDY(k, a, print, greedyWriter, writer, greedyTimeLimit) == -1) greedy = false;
                        }
                        if (fptas) {
                            Instant stop, start = Instant.now();
                            for (int epsilon = 1; epsilon <= 250; epsilon++) {

                                stop = Instant.now();
                                if (Duration.between(start, stop).toSeconds() > fptasTotalTimeLimit)
                                    fptas = false; // time limit set in fptasTotalTimeLimit

                                if (izvediFPTAS(k, a, epsilon, print, fptasWriter, writer, fptasTimeLimit) == -1) {
                                    fptas = false;
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                        if (writer) {
                            dynWriter.close();
                            exhWriter.close();
                            greedyWriter.close();
                            fptasWriter.close();
                        }
                    }
                }
            }
        }

        if (writer) {
            dynWriter.close();
            exhWriter.close();
            greedyWriter.close();
            fptasWriter.close();
        }
    }

    private static void generirajProblem(int n, int k, String path) throws IOException {
        File file = new File(path);
        Writer writer = new FileWriter(file, false);

        writer.write(n + "\n");
        writer.write(k + "\n");

        Random random = new Random();
        for (int i = 0; i < n; i++) {
            writer.write(random.nextInt(k+1) + "\n");
        }

        writer.close();
    }

    private static void generirajWorstProblemEXH(int n, int k, String path) throws IOException {
        File file = new File(path);
        Writer writer = new FileWriter(file, false);

        writer.write(n + "\n");
        writer.write(k + "\n");

        for (int i = 0; i < n; i++){
            writer.write((int) Math.pow(2, i) + "\n");
        }

        writer.close();
    }

    private static void generirajWorstProblemGREEDY(int n, int k, String path) throws IOException {
        File file = new File(path);
        Writer writer = new FileWriter(file, false);

        writer.write(n + "\n");
        writer.write(k + "\n");

        ArrayList<Integer> arr = MergeWorstCase.generate(n);

        for (int i : arr){
            writer.write(i + "\n");
        }

        writer.close();
    }

    private static int izvediFPTAS(int k, ArrayList<Integer> a, double epsilon, boolean print, Writer writer, boolean write, int timeLimit) {
        Instant start = Instant.now();
        int rezultat = fptas(k, a, epsilon, timeLimit);
        if (rezultat < 0) return rezultat;
        Instant stop = Instant.now();
        long elapsed = Duration.between(start,stop).toMillis();
        if (print)
            System.out.println("FPTAS: " + rezultat + ", Time: " + elapsed + " ms");

        if (write) {
            try {
                writer.write(a.size() + "," + k + "," + epsilon + "," + elapsed + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    private static int izvediGREEDY(int k, ArrayList<Integer> a, boolean print, Writer writer, boolean write, int timeLimit) {
        Instant start = Instant.now();

        int rezultat = greedy(k, a, timeLimit);
        if (rezultat == -1) return rezultat;

        Instant stop = Instant.now();
        long elapsed = Duration.between(start,stop).toMillis();

        if (print)
            System.out.println("GREEDY: " + rezultat + ", Time: " + elapsed + " ms");

        if (write) {
            try {
                writer.write(a.size() + "," + k + "," + elapsed + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    private static int izvediEXH(int k, ArrayList<Integer> a, boolean print, Writer writer, boolean write, int timeLimit) {
        System.out.println("Dolzina seznama: 2^" + Math.log10(a.size()) / Math.log10(2));

        Instant start = Instant.now();
        int rezultat = exh(k, a, timeLimit);
        if (rezultat == -1) return rezultat;
        Instant stop = Instant.now();
        long elapsed = Duration.between(start,stop).toMillis();
        if (print)
            System.out.println("EXH: " + rezultat + ", Time: " + elapsed + " ms");

        if (write) {
            try {
                writer.write(a.size() + "," + k + "," + elapsed + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    private static int izvediDYN(int n, int k, ArrayList<Integer> a, boolean print, Writer writer, boolean write, int timeLimit) {
        Instant start = Instant.now();
        int rezultat = dinamicnoProgramiranje(n,k,a,timeLimit);
        Instant stop = Instant.now();
        if (rezultat == -1) return rezultat;  // max runtime is 1 minute
        long elapsed = Duration.between(start,stop).toMillis();
        if (print)
            System.out.println("DYN: " + rezultat + ", Time: " + elapsed + " ms");

        if (write) {
            try {
                writer.write(n + "," + k + "," + elapsed + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    private static int fptas(int k, ArrayList<Integer> a, double epsilon, int timeLimit) {
        Instant stop, start = Instant.now();

        Set<Integer> prviSeznam = new HashSet<>();
        // če sortiramo na začetku prihranimo čas pri sortiranju znotraj trim
        Collections.sort(a);

        prviSeznam.add(0);

        for (int element : a) {
            stop = Instant.now();
            if (Duration.between(start, stop).toSeconds() > timeLimit) return -1;

            Set<Integer> drugiSeznam = new HashSet<>();
            for (int x : prviSeznam) {
                int sum = x + element;
                if (sum <= k) {
                    drugiSeznam.add(sum);
                }
            }
            prviSeznam.addAll(drugiSeznam);

            // sedaj izvedemo še trim - edina razlika od alg. EXH
            prviSeznam = trim(prviSeznam, (epsilon / (2*prviSeznam.size())));
        }

        return Collections.max(prviSeznam);
    }

    private static Set<Integer> trim(Set<Integer> prviSeznam, double delta) {
        ArrayList<Integer> list = new ArrayList<>(prviSeznam);
        Collections.sort(list);

        Set<Integer> listNov = new HashSet<>();
        listNov.add(list.get(0));
        int last = list.get(0);

        for (int element : list) {
            if (element > (last * (1 + delta))) {
                listNov.add(element);
                last = element;
            }
        }

        return listNov;
    }

    private static int greedy(int k, ArrayList<Integer> b, int timeLimit) {
        Instant stop, start = Instant.now();

        ArrayList<Integer> A = new ArrayList<>(b);
        // uredimo padajoče
        Collections.sort(A, (o1,o2) -> {return Integer.compare(o2, o1);});

        int g = 0;  // trenutna vsota elementov
        for (int a : A){
            stop = Instant.now();
            if (Duration.between(start,stop).toSeconds() > timeLimit) return -1;

            if (a <= k-g){
                g += a;
            }
        }

        return g;
    }

    private static int exh(int k, ArrayList<Integer> a, int timeLimit) {
        Instant start = Instant.now();
        Instant stop;
        Set<Integer> prviSeznam = new HashSet<>();
        prviSeznam.add(0);

        for (int element : a) {
            Set<Integer> drugiSeznam = new HashSet<>();
            for (int x : prviSeznam) {
                stop = Instant.now();
                if (Duration.between(start,stop).toSeconds() > timeLimit) return -1;

                int sum = x + element;
                if (sum <= k) {
                    drugiSeznam.add(sum);
                }
            }
            prviSeznam.addAll(drugiSeznam);
        }

        return Collections.max(prviSeznam);
    }

    private static int dinamicnoProgramiranje(int n, int k, ArrayList<Integer> b, int timeLimit){
        Instant start = Instant.now();
        Instant stop;

        ArrayList<Integer> a = new ArrayList<>(b);
        Collections.sort(a);

        ArrayList<Integer> prva = new ArrayList<Integer>(Collections.nCopies(k+1, 0));

        for (int i = 0; i < n; i++) {
            ArrayList<Integer> druga = new ArrayList<Integer>(Collections.nCopies(k+1, 0));

            for (int j = 1; j <= k; j++) {
                stop = Instant.now();
                if (Duration.between(start,stop).toSeconds() > timeLimit) return -1;

                int cetrtiIndeks = j - a.get(i);

                if (cetrtiIndeks >= 0) {
                    druga.set(j, Math.max(
                            prva.get(j),
                            prva.get(cetrtiIndeks) + a.get(i)));
                } else {
                    druga.set(j, prva.get(j));
                }
            }

            prva = druga;
        }

        return prva.get(k);
    }

}


/*
Programska koda razreda "MergeWorstCase" je pridobljena iz spletne strani na naslovu:
https://stackoverflow.com/questions/24594112/when-will-the-worst-case-of-merge-sort-occur
Dostopano 21.4.2021
Avtor je uporabnik "Jerky".

Programska koda je bila za namene te naloge delno spremenjena.
 */
class MergeWorstCase
{
    private static void print(int arr[])
    {
        System.out.println();
        for(int i=0;i<arr.length;i++)
            System.out.print(arr[i]+" ");
        System.out.println();
    }
    private static void merge(int[] arr, int[] left, int[] right) {
        int i,j;
        for(i=0;i<left.length;i++)
            arr[i]=left[i];
        for(j=0;j<right.length;j++,i++)
            arr[i]=right[j];
    }

    //Pass a sorted array here
    private static void seperate(int[] arr) {

        if(arr.length<=1)
            return;

        if(arr.length==2)
        {
            int swap=arr[0];
            arr[0]=arr[1];
            arr[1]=swap;
            return;
        }

        int i,j;
        int m = (arr.length + 1) / 2;
        int left[] = new int[m];
        int right[] = new int[arr.length-m];

        for(i=0,j=0;i<arr.length;i=i+2,j++) //Storing alternate elements in left subarray
            left[j]=arr[i];

        for(i=1,j=0;i<arr.length;i=i+2,j++) //Storing alternate elements in right subarray
            right[j]=arr[i];

        seperate(left);
        seperate(right);
        merge(arr, left, right);
    }

    public static ArrayList<Integer> generate(int n){
        int[] arr = new int[n];
        for (int i = 0; i < n; i++){
            arr[i] = i;
        }
        seperate(arr);

        ArrayList<Integer> a = new ArrayList<Integer>(n);
        for (int i : arr) {
            a.add(i);
        }

        return a;
    }
}