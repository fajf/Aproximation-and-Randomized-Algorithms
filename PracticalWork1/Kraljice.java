import java.util.LinkedList;
import java.util.ListIterator;

public class Kraljice {
    public static void main(String []args){
        int dimenzijaSahovnice = Integer.parseInt(args[0]);

        // generiraj sahovnico
        String[][] sahovnica = generirajSahovnico(dimenzijaSahovnice);

        LinkedList<Klavzula> koncniIzraz = new LinkedList<>();

        kraljicaVVsakiVrstici(koncniIzraz, dimenzijaSahovnice, sahovnica);
        najvecEnaKraljicaNaVrstico(koncniIzraz, dimenzijaSahovnice, sahovnica);
        najvecEnaKraljicaNaStolpec(koncniIzraz, dimenzijaSahovnice, sahovnica);
        najvecEnaKraljicaNaDiagonalo(koncniIzraz, dimenzijaSahovnice, sahovnica);

        narediKoncniIzpisNaloga(koncniIzraz, dimenzijaSahovnice*dimenzijaSahovnice);
    }

    private static void najvecEnaKraljicaNaDiagonalo(LinkedList<Klavzula> koncniIzraz, int dimenzijaSahovnice, String[][] sahovnica) {
        // na vsaki diagonali največ ena kraljica
        // not (or (and 11 22) (and 12 21))
        // and (or not(11) not(22)) (or not(12 21))

        // diagonale po vrstici
        for (int i = 0; i+1 < dimenzijaSahovnice; i++) {
            for (int x = 0; x+1+i < dimenzijaSahovnice; x++) {
                for (int y = x+1; y+i < dimenzijaSahovnice; y++) {
                    Klavzula klavzula = new Klavzula();
                    klavzula.operacija = "or";
                    klavzula.izraz.add(neg(sahovnica[x][x+i]));
                    klavzula.izraz.add(neg(sahovnica[y][y+i]));
                    koncniIzraz.add(klavzula);
                }
            }
        }
        // diagonale po stolpcu brez glavne diagonale
        for (int i = 1; i+1 < dimenzijaSahovnice; i++) {
            for (int x = 0; x+1+i < dimenzijaSahovnice; x++) {
                for (int y = x+1; y+i < dimenzijaSahovnice; y++) {
                    Klavzula klavzula = new Klavzula();
                    klavzula.operacija = "or";
                    klavzula.izraz.add(neg(sahovnica[x+i][x]));
                    klavzula.izraz.add(neg(sahovnica[y+i][y]));
                    koncniIzraz.add(klavzula);
                }
            }
        }
        // "inverzne" diagonale po vrstici
        for (int i = 0; i < dimenzijaSahovnice-1; i++) {
            for (int x = dimenzijaSahovnice-1; x > 0; x--) {
                for (int y = 0; x-1-y-i >= 0 && dimenzijaSahovnice-1-x+1+y+i < dimenzijaSahovnice; y++) {
                    Klavzula klavzula = new Klavzula();
                    klavzula.operacija = "or";
                    klavzula.izraz.add(neg(sahovnica[x][dimenzijaSahovnice-1-x+i]));
                    klavzula.izraz.add(neg(sahovnica[x-1-y][dimenzijaSahovnice-1-x+1+y+i]));
                    koncniIzraz.add(klavzula);
                }
            }
        }

        // "inverzne" diagonale po vrstici brez glavne diagonale
        for (int i = 1; i < dimenzijaSahovnice-1; i++) {
            for (int x = dimenzijaSahovnice-1; x-i > 0; x--) {
                for (int y = 0; x-1-y-i >= 0; y++) {
                    Klavzula klavzula = new Klavzula();
                    klavzula.operacija = "or";
                    klavzula.izraz.add(neg(sahovnica[x-i][dimenzijaSahovnice-1-x]));
                    klavzula.izraz.add(neg(sahovnica[x-1-y-i][dimenzijaSahovnice-1-x+1+y]));
                    koncniIzraz.add(klavzula);
                }
            }
        }
    }

    private static void najvecEnaKraljicaNaVrstico(LinkedList<Klavzula> koncniIzraz, int dimenzijaSahovnice, String[][] sahovnica){
        // v vsaki vrstici naj bo največ ena kraljica
        // not (or (1 in 2) (1 in 3) (2 in 3))
        // and neg(1 in 2) neg(1 in 3) neg(2 in 3)
        // and (neg(11) ali neg(12)) (neg(11) ali neg(13)) (neg(12) ali neg(13))
        for (int i = 0; i < dimenzijaSahovnice; i++){   // vsaka vrstica
            // ena vrstica
            for (int x = 0; x+1 < dimenzijaSahovnice; x++){
                for (int y = x+1; y < dimenzijaSahovnice; y++){
                    Klavzula klavzula = new Klavzula();
                    klavzula.operacija = "or";
                    klavzula.izraz.add(neg(sahovnica[i][x]));
                    klavzula.izraz.add(neg(sahovnica[i][y]));
                    koncniIzraz.add(klavzula);
                }
            }
        }
    }

    private static void najvecEnaKraljicaNaStolpec(LinkedList<Klavzula> koncniIzraz, int dimenzijaSahovnice, String[][] sahovnica){
        // v vsakem stolpcu naj bo največ ena kraljica
        // not (or (1 in 2) (1 in 3) (2 in 3))
        // and neg(1 in 2) neg(1 in 3) neg(2 in 3)
        // and (neg(11) ali neg(21)) (neg(11) ali neg(31)) (neg(21) ali neg(31))
        for (int i = 0; i < dimenzijaSahovnice; i++){   // vsak stolpec
            // en stolpec
            for (int x = 0; x+1 < dimenzijaSahovnice; x++){
                for (int y = x+1; y < dimenzijaSahovnice; y++){
                    Klavzula klavzula = new Klavzula();
                    klavzula.operacija = "or";
                    klavzula.izraz.add(neg(sahovnica[x][i]));
                    klavzula.izraz.add(neg(sahovnica[y][i]));
                    koncniIzraz.add(klavzula);
                }
            }
        }
    }

    private static void kraljicaVVsakiVrstici(LinkedList<Klavzula> koncniIzraz, int dimenzijaSahovnice, String[][] sahovnica){
        // v vsaki vrstici naj bo vsaj ena kraljica
        for (int i = 0; i < dimenzijaSahovnice; i++){
            // vsaka vrstica naj bo svoja klavzula
            Klavzula klavzula = new Klavzula();
            klavzula.operacija = "or";
            for (int j = 0; j < dimenzijaSahovnice; j++){
                klavzula.izraz.add(sahovnica[i][j]);
            }
            koncniIzraz.add(klavzula);
        }
    }

    private static void narediKoncniIzpisNaloga(LinkedList<Klavzula> koncniIzraz, int stSpremenljivk){
        int stKlavzul = koncniIzraz.size();
        System.out.println("p cnf " + String.valueOf(stSpremenljivk) + " " + stKlavzul);
        izpisiKoncniIzraz(koncniIzraz);
    }

    private static void izpisiKoncniIzraz(LinkedList<Klavzula> izraz){
        for (Klavzula klavzula : izraz) {
            ListIterator<String> iterator = klavzula.izraz.listIterator();
            while (iterator.hasNext()){
                System.out.print(iterator.next() + " ");
            }
            System.out.println("0");
        }
    }

    private static String neg(String n){
        return "-" + n;
    }

    /**
     * Generira tabelo velikost n krat n,
     * ki predstavlja sahovnico. Vrednost posameznega polja
     * tabele je ime spremenljivke, ki predstavlja to polje.
     *
     * @param n    dimenzija {@code int} ene stranice sahovnice
     * @return     tabela dimnezij n krat n {@code int[][]}, ki predstavlja sahovnico
     */
    private static String[][] generirajSahovnico(int n){
        String[][] sahovnica = new String[n][n];
        for (int i = 0; i < n; i++){
            for (int j = 0; j < n; j++){
                sahovnica[i][j] = String.valueOf(i+1) + String.valueOf(j+1);
            }
        }
        return sahovnica;
    }
}

class Klavzula {
    String operacija;
    LinkedList<String> izraz = new LinkedList<>();
}