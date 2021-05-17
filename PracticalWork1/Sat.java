import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class Sat {
    private static String[] literal;
    private static String[][] povezave;
    private static int k;
    private static File outputFile;
    private static PrintWriter writer;
    private static ArrayList<ArrayList<String>> klavzule;
    private static ArrayList<String> vsiLiterali = new ArrayList<>();

    public static void main(String []args){
        String fileName = args[0];

        Scanner in = new Scanner(System.in);

        System.out.println("Vpiši spodnje število k: ");
        int spodnjiK = Integer.parseInt(in.nextLine());
        System.out.println("Vpiši zgornje število k: ");
        int zgornjiK = Integer.parseInt(in.nextLine());
        System.out.println("Vpiši inkrement: ");
        int inkrement = Integer.parseInt(in.nextLine());

        for (k = spodnjiK; k <= zgornjiK; k = k + inkrement) {
            outputFile = new File("D:\\Sola\\4.letnik\\ANA\\PrakticnoDelo\\grafi\\output" + String.valueOf(k) + ".cnf");
            try {
                writer = new PrintWriter(outputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            klavzule = new ArrayList<>();

            preberiDatoteko(fileName);

            tockaB();
            tockaC();
            izpisiKlavzule();
            
            writer.close();
            
            /*if (satSolver()){
                return;
            }*/
        }


    }

    private static boolean satSolver() {
        // generiraj tabelo binarnih števil dolžine n*k, kjer je natanko k enic
        int dolzina = vsiLiterali.size();

        System.out.println("satSolver: začenjam reševanje za k = " + k + ", max. value = " + Math.pow(2,dolzina));

        ArrayList<Boolean> vrednosti = new ArrayList<Boolean>();



        for (int i = 0; i < Math.pow(2,dolzina); i++){
            // ali je natanko k enic?
            if (Integer.bitCount(i) == k){
                //System.out.println(i);
                String binary = String.format("%" + dolzina + "s", Integer.toBinaryString(i)).replace(' ', '0');

                if (solve(klavzule, binary.toCharArray())){
                    System.out.println("Rešitev: " + Integer.toBinaryString(i));
                    System.out.println("Najdena rešitev za k = " + k);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean solve(ArrayList<ArrayList<String>> klavzule, char[] vrednosti) {
        //System.out.println("solve: " + vsiLiterali.size() + " " + vrednosti.length);

        boolean andStavek = true;
        for (ArrayList<String> klavzula : klavzule){
            //tukaj je or
            boolean orStavek = false;
            for (String var : klavzula){
                // če je spremenljivka NE-negirana
                if (var.charAt(0) != '-') {
                    if (vrednosti[vsiLiterali.indexOf(var)] == '1') {
                        orStavek = true;
                    }
                    /*else {
                        orStavek = orStavek || false;
                    }*/
                }
                // če je spremenljivka negirana
                else{
                    var = var.substring(1);
                    if (vrednosti[vsiLiterali.indexOf(var)] == '0') {
                        orStavek = true;
                    }
                }
            }
            andStavek = andStavek && orStavek;
            if (andStavek == false){
                return false;
            }
        }
        return true;
    }

    private static void izpisiKlavzule() {
        for (ArrayList<String> klavzula : klavzule){
            for (String var : klavzula){
                System.out.print(var + " ");
            }
            System.out.println("0");
        }
    }

    private static void tockaB() {
        // za vsako vozlisce zapisemo da je v M ali pa je v njej njegov sosed

        // vozlisce in sosede izpisemo v vrstico (povezani z OR)
        // za vsako vozlišče moramo zapisati 11, 12, 13, ..., 1k
        for (int i = 0; i < literal.length; i++){
            ArrayList<String> klavzula = new ArrayList<>();

            for (int x = 1; x <= k; x++) {
                writer.print(literal[i] + String.valueOf(x) + " ");
                //klavzula.add(literal[i] + String.valueOf(x));

                vsiLiterali.add(literal[i] + String.valueOf(x));
            }
            for (int j = 0; j < povezave.length; j++){
                // preverimo, če je literal na prvem ali drugem mestu v povezavi
                // vedno bo res samo ena izmed opcij
                if (povezave[j][0].equals(literal[i])){
                    for (int x = 1; x <= k; x++) {
                        writer.print(povezave[j][1] + String.valueOf(x) + " ");
                        //klavzula.add(povezave[j][1] + String.valueOf(x));
                    }
                }
                else if (povezave[j][1].equals(literal[i])){
                    for (int x = 1; x <= k; x++) {
                        writer.print(povezave[j][0] + String.valueOf(x) + " ");
                        //klavzula.add(povezave[j][0] + String.valueOf(x));
                    }
                }
            }
            //klavzule.add(klavzula);
            writer.println("0");
        }
    }

    private static void tockaC() {
        /*
        v vsaki vrstici in vsakem stolpcu naj bo največ en literal true

          1 2 ... k
        1
        2
        ...
        n

        not (or (11 in 12) (11 in 13) (12 in 13))
        and neg(11 in 12) neg(11 in 13) neg(12 in 13)
        and (neg(11) ali neg(12)) (neg(11) ali neg(13)) (neg(12) ali neg(13))
        */

        vsakaVrsticaNajvecEna();
        vsakStolpecNajvecEn();
    }

    private static void vsakStolpecNajvecEn() {
        // ideja: na eni poziciji v rešitvi je lahko kvečjemu en literal

        // gremo po stolpic
        for (int i = 1; i <= k; i++){
            // sedaj pa še znotraj stolpca
            for (int x = 0; x+1 < literal.length; x++){
                for (int y = x+1; y < literal.length; y++) {
                    writer.print("-" + literal[x] + String.valueOf(i) + " ");
                    writer.print("-" + literal[y] + String.valueOf(i) + " ");
                    writer.println("0");

                    //ArrayList<String> klavzula = new ArrayList<>();
                    //klavzula.add("-" + literal[x] + String.valueOf(i));
                    //klavzula.add("-" + literal[y] + String.valueOf(i));
                    //klavzule.add(klavzula);
                }
            }
        }
    }

    private static void vsakaVrsticaNajvecEna() {
        // ideja: en literal je lahko kvečjemu na eni poziciji v rešitvi

        // gremo vrsticah
        for (int i = 0; i < literal.length; i++){
            // sedaj pa še znotraj vrstice
            for (int x = 1; x < k; x++){
                for (int y = x+1; y <= k; y++) {
                    writer.print("-" + literal[i] + String.valueOf(x) + " ");
                    writer.print("-" + literal[i] + String.valueOf(y) + " ");
                    writer.println("0");

                    //ArrayList<String> klavzula = new ArrayList<>();
                    //klavzula.add("-" + literal[i] + String.valueOf(x));
                    //klavzula.add("-" + literal[i] + String.valueOf(y));
                    //klavzule.add(klavzula);
                }
            }
        }
    }

    private static void preberiDatoteko(String fileName){
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    "D:\\Sola\\4.letnik\\ANA\\PrakticnoDelo\\grafi\\" + fileName + ".txt"));
            String line = reader.readLine();
            int stVozlisc = Integer.parseInt(line.split(" ")[2]);
            int stPovezav = Integer.parseInt(line.split(" ")[3]);

            // inicializiramo tabeli, ki hranita podatke o grafu
            literal = new String[stVozlisc];
            povezave = new String[stPovezav][2];

            int dolzina = String.valueOf(literal.length).length();

            // napolnimo tabelo vozlišč
            for (int i = 0; i < stVozlisc; i++){
                // vse števke naj bodo f-dolge
                String padding = String.format("%" + dolzina + "s", String.valueOf(i+1)).replace(' ', '0');
                literal[i] = padding;
            }

            // preberemo vse povezave
            for (int i = 0; i < stPovezav; i++){
                line = reader.readLine();
                if (line == null) throw new IllegalArgumentException("Število povezav se razlikuje od podanega števila povezav v prvi vrstici!");

                povezave[i][0] = String.format("%" + dolzina + "s", line.split(" ")[1]).replace(' ', '0');
                povezave[i][1] = String.format("%" + dolzina + "s", line.split(" ")[2]).replace(' ', '0');
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}