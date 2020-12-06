import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class stat {

    // inverse String
    public static String inverseString(String chaine) {
        StringBuilder result = new StringBuilder();
        char c;
        for (int i = chaine.length() - 1 ; i >= 0; i--) {
            c = complementary(chaine.charAt(i));
            result.append(c);
        }
        return result.toString();
    }

    // Make the complementary
    public static char complementary(char c){
        char r = 'n';
        switch (c) {
            case 'A' :
                r = 'T';
                break;
            case 'C' :
                r = 'G';
                break;
            case 'G' :
                r = 'C';
                break;
            case 'T' :
                r = 'A';
                break;
            case 'N' :
                r = 'N';
        }
        return r;
    }

    public static int test_gene(String gene, int taille_gene){
        String t_init[] =  {"ATG", "CTG", "TTG", "GTG", "ATA", "ATC", "ATT", "TTA"};
        String t_stop[] =  {"TAA", "TAG", "TGA", "TTA"};

        // test taille
        if (taille_gene % 3 != 0) {
            return -1;
        }

        // test debut
        boolean valid = false;
        String debut = gene.substring(0,3);
        for(int i = 0 ; i < 8 ; i++) {
            if (t_init[i].equals(debut)) {
                valid = true;
            }
        }
        if (!valid) {
            return -1;
        }

        // test fin
        valid = false;
        String fin = gene.substring(taille_gene-3, taille_gene);
        for(int i = 0 ; i < 4 ; i++) {
            if (t_stop[i].equals(fin)) {
                valid = true;
            }
        }
        if (!valid) {
            return -1;
        }
        return 0;
    }

    public static void init_tab(int tab[], int size) {
        for (int i = 0 ; i < size ; i++){
            tab[i] = 0;
        }
    }

    // Get the number of occurence for each trinucleotide of a gene
    public static int occurence (String liste[], int tabP0[],int tabP1[], int tabP2[], String gene, int nb_tri){
        StringBuffer tri0 = new StringBuffer();
        StringBuffer tri1 = new StringBuffer();
        StringBuffer tri2 = new StringBuffer();
        int ind0;
        int ind1;
        int ind2;

        for(int i = 0 ; i < nb_tri-1 ; i++) {
            tri0.replace(0, 3, gene.substring(0 + 3*i, 0 + 3 + 3*i));
            tri1.replace(0, 3, gene.substring(1 + 3*i, 1 + 3 + 3*i));
            tri2.replace(0, 3, gene.substring(2 + 3*i, 2 + 3 + 3*i));
            ind0 = indice(liste, tri0.toString());
            ind1 = indice(liste, tri1.toString());
            ind2 = indice(liste, tri2.toString());
            if (ind0 == -1 || ind1 == -1 || ind2 == -1) {
                return -1;
            }
            else {
                tabP0[ind0]++;
                tabP1[ind1]++;
                tabP2[ind2]++;
            }
        }
        return 0;
    }

    public static int indice(String list[], String tri) {
        for (int i = 0 ; i < 64 ; i++) {
            if (list[i].equals(tri)) {
                return i;
            }
        }
        return -1;
    }

    public static int total(int tab[], int size){
        int tot = 0;
        for (int i = 0 ; i < size ; i++) {
            tot += tab[i];
        }
        return tot;
    }

    public static int cds_mode(String name_seq) {
        String c;
        int mode;
        c = name_seq.substring(0,1);
        if(c.equals("c")) {
            c = name_seq.substring(11,12);
            if(c.equals("j")) {
                // complement + join
                mode = 3;
            }
            else {
                // complement
                mode = 2;
            }
        }
        else if(c.equals("j")) {
            // join
            mode = 1;
        }
        else {
            // nothing
            mode = 0;
        }
        return mode;
    }

    // Verify if a String is Numeric or not
    public static boolean is_Numeric(String strNum) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }

    // Parse the String sequences to a list of number
    // ex : (3679..3862,4321..4423)
    // to ->  [3679,3862,4362,4423]
    public static List<Integer> get_listnumber(String name_seq, int mode) {
        String[] part;
        part = name_seq.split("(?<=\\D)(?=\\()");
        List<Integer> list_number_seq = new ArrayList<>();

        // complement + join
        if(mode == 3) {
            part = part[2].split("(?<=\\D)|(?=\\D)");
            int step = 3;
            for (int i = 1; i < part.length; i +=step) {
                if(part[i].equals(")")) {
                    break;
                }
                // check ".."
                if (step == 3) {
                    list_number_seq.add(Integer.parseInt(part[i]));
                    if (part[i+1].equals(".")) {
                        step=3;
                    }
                    else {
                        step=2;
                    }
                }
                else{
                    step = 3;
                    list_number_seq.add(Integer.parseInt(part[i]));
                }
            }
        }

        // complement
        else if(mode == 2) {
            part = part[1].split("(?<=\\D)|(?=\\D)");
            int step = 3;
            for (int i = 1; i < part.length; i +=step) {
                if (part[i].equals(")")) {
                    break;
                }
                // check ".."
                if (step == 3) {
                    list_number_seq.add(Integer.parseInt(part[i]));
                    if (part[i + 1].equals(".")) {
                        step = 3;
                    } else {
                        step = 2;
                    }
                }
                else{
                    step = 3;
                    list_number_seq.add(Integer.parseInt(part[i]));
                }
            }
        }

        // join
        else if(mode == 1) {
            part = part[1].split("(?<=\\D)|(?=\\D)");
            int step = 3;
            for (int i = 1; i < part.length; i +=step) {
                if (part[i].equals(")")) {
                    break;
                }
                // check ".."
                if (step == 3) {
                    list_number_seq.add(Integer.parseInt(part[i]));
                    if (part[i + 1].equals(".")) {
                        step = 3;
                    } else {
                        step = 2;
                    }
                }
                else{
                    step = 3;
                    list_number_seq.add(Integer.parseInt(part[i]));
                }
            }
        }

        // nothing
        else{
            part = part[0].split("(?<=\\D)|(?=\\D)");
            try{
                list_number_seq.add(Integer.parseInt(part[0]));
                list_number_seq.add(Integer.parseInt(part[3]));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return list_number_seq;
    }




    // Verify if test_cds correct
    public static boolean test_cds(String name_seq , int mode)
    {
        String[] part;
        part = name_seq.split("(?<=\\D)(?=\\()");
        List<Integer> list_number_seq = new ArrayList<>();

        // complement + join
        if(mode == 3) {
            part = part[2].split("(?<=\\D)|(?=\\D)");
            int step = 3;
            for (int i = 1; i < part.length; i +=step) {
                if(part[i].equals(")")) {
                    break;
                }
                // check ".."
                if (step == 3) {
                    if (part[i+1].equals(".")) {
                        if(part[i+2].equals(".")) {
                            if(part[i+3].equals(">")) {
                                return false;
                            }
                            if(!is_Numeric(part[i])) {
                                //System.out.println("False not a number mode = 3");
                                return false;
                            }
                            list_number_seq.add(Integer.parseInt(part[i]));
                        }
                        else{
                            //System.out.println("faux .. mode = 3");
                            return false;
                        }
                    }
                    else{
                        //System.out.println("faux . mode = 3");
                        return false;
                    }
                    step = 2;
                }
                else {
                    step = 3;
                }
            }
        }
        // complement
        else if(mode == 2) {
            part = part[1].split("(?<=\\D)|(?=\\D)");
            int step = 3;
            for (int i = 1; i < part.length; i +=step) {
                if(part[i].equals(")")) {
                    break;
                }
                // check ".."
                if (step == 3) {
                    if (part[i+1].equals(".")) {
                        if(part[i+2].equals(".")) {
                            if (part[i+3].equals(">")) {
                                return false;
                            }
                            if(!is_Numeric(part[i])) {
                                return false;
                            }
                            list_number_seq.add(Integer.parseInt(part[i]));
                        }
                        else{
                            return false;
                        }
                    }
                    else{
                        return false;
                    }
                    step = 2;
                }
                else {
                    step = 3;
                }
            }
        }
        // join
        else if(mode == 1) {
            part = part[1].split("(?<=\\D)|(?=\\D)");
            int step = 3;
            for (int i = 1; i < part.length; i +=step) {
                if(part[i].equals(")")) {
                    break;
                }
                // check ".."
                if (step == 3) {
                    if (part[i+1].equals(".")) {
                        if(part[i+2].equals(".")) {
                            if(part[i+3].equals(">")) {
                                return false;
                            }
                            if(!is_Numeric(part[i])) {
                                return false;
                            }
                            list_number_seq.add(Integer.parseInt(part[i]));
                        }
                        else{
                            return false;
                        }
                    }
                    else{
                        return false;
                    }
                    step = 2;
                }
                else {
                    step = 3;
                }
            }
        }
        // nothing
        else{
            part = part[0].split("(?<=\\D)|(?=\\D)");
            if (part[1].equals(".") ) {
                if(part[2].equals(".")) {
                    if(part[3].equals(">")) {
                        return false;
                    }
                    if(!is_Numeric(part[0])||!is_Numeric(part[3])) {
                        return false;
                    }
                    list_number_seq.add(Integer.parseInt(part[0]));
                    list_number_seq.add(Integer.parseInt(part[3]));
                }
                else{
                    return false;
                }
            }
            else{
                return false;
            }
        }

        for(int i = 0; i < list_number_seq.size()-1; i++) {
            if (list_number_seq.get(i)>=list_number_seq.get(i+1)) {
                return false;
            }
        }
        return true;
    }
}
