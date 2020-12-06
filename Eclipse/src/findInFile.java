import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.*;
import jxl.write.Number;
import static java.lang.StrictMath.round;

// get Name, Group, SubGroup where there is chromosome:NC_
public class findInFile {

    public static final List<List> findInFile(final Path file, final String pattern, final int flags, final int file_type)
            throws IOException {
        final Pattern p = Pattern.compile(pattern, flags);
        final List<String> list_Group = new ArrayList<>();
        final List<String> list_Name = new ArrayList<>();
        final List<String> list_SubGroup = new ArrayList<>();
        final List<List> list_charac = new ArrayList<>();

        String line;
        String line_parsed[];
        String name_parsed[];
        String name_parsed1[];
        String name_parsed2[];
        String name_parsed3[];
        String last_name = "";

        Matcher m;


        try (
                final BufferedReader br = Files.newBufferedReader(file);
        ) {
            while ((line = br.readLine()) != null) {
                m = p.matcher(line);
                // find NC_
                switch (file_type) {
                    case 0: // Eukaryote
                        if (m.find()) {
                            line_parsed = line.split("\t", 7);

                            list_Group.add(line_parsed[4]);
                            list_SubGroup.add(line_parsed[5]);
                            name_parsed = line_parsed[0].split(" ", 3);
                            list_Name.add(name_parsed[0] + " " + name_parsed[1]);
                        }
                        break;
                    case 1: // Prokaryote
                        if (m.find()) {
                            line_parsed = line.split("\t", 8);
                            list_Group.add(line_parsed[4].replaceAll("/", "_"));
                            list_SubGroup.add(line_parsed[5].replaceAll("/", "_"));
                            name_parsed = line_parsed[0].split("-", 2);
                            name_parsed1 = name_parsed[0].split("\\s\\(", 2);
                            name_parsed2 = name_parsed1[0].split("/", 2);
                            name_parsed3 = name_parsed2[0].split("\\s\\[", 2);
                            list_Name.add(name_parsed3[0]);
                        }
                        break;
                    case 2: // Plasmids
                        if (m.find()) {
                            line_parsed = line.split("\t", 7);
                            if(line_parsed[0].split(" ").length>2){
                                if( !last_name.equals(line_parsed[0])) {
                                    list_Group.add(line_parsed[2].replaceAll("/", "_"));
                                    list_SubGroup.add(line_parsed[3].replaceAll("/", "_"));
                                    list_Name.add(line_parsed[0]);
                                    last_name = line_parsed[0];
                                }
                            }
                        }
                        break;
                    case 3: // Viruses
                        if (m.find()) {
                            line_parsed = line.split("\t", 8);
                            list_Group.add(line_parsed[4].replaceAll("/", "_"));
                            list_SubGroup.add(line_parsed[5].replaceAll("/", "_"));
                            name_parsed = line_parsed[0].split("-", 2);
                            name_parsed1 = name_parsed[0].split("\\s\\(", 2);
                            name_parsed2 = name_parsed1[0].split("/", 2);
                            name_parsed3 = name_parsed2[0].split("\\s\\[", 2);
                            list_Name.add(name_parsed3[0]);
                        }
                        break;
                }
            }
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        list_charac.add(list_Name);
        list_charac.add(list_Group);
        list_charac.add(list_SubGroup);
        return list_charac;
    }

    // find path for download the correct gbff.gz file to get DNA
    public static final String findPathInFile(final Path file, final String Name_pattern, final int flags)
            throws IOException {

        final Pattern p = Pattern.compile(Name_pattern, flags);
        final Pattern path = Pattern.compile("ftp://.*?(?=$)", flags);
        final StringBuilder Path = new StringBuilder();

        String line;

        Matcher m;
        Matcher mPath;

        try (
                final BufferedReader br = Files.newBufferedReader(file);
        ) {
            while ((line = br.readLine()) != null) {

                m = p.matcher(line);
                if (m.find()) {
                    mPath = path.matcher(line);
                    if (mPath.find()) {
                        Path.append(mPath.group());
                        break;
                    }
                }

            }
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return Path.toString();
    }

    // Change space to underscore in a String
    public static String underscoreName(String string, int partition, int partitionkeeped)
            throws IOException {
        String correctName = "";
        String[] arrOfStr = string.split(" ", partition);
        for (int i = 0; i < partitionkeeped; i++) {
            correctName = correctName + "_" + arrOfStr[i];
        }
        return correctName;
    }

    // Get the sequences of each CDS by NC on a Organism
    // ex : -> complementary(join(7533..7654,8321..8452))
    public static final List<List> getSeqNC(final Path file, final int flags)
            throws IOException {

        stat stat = new stat();
        final Pattern p_Accession = Pattern.compile("ACCESSION  ", flags);
        final Pattern p_NC = Pattern.compile("NC", flags);
        final Pattern p_CDS = Pattern.compile("CDS      ", flags);
        final Pattern p_end_seq = Pattern.compile("/", flags);
        //final Pattern p_End_CDS = Pattern.compile("LOCUS       ", flags);
        final Pattern p_End_CDS = Pattern.compile("ORIGIN      ", flags);

        List<String> list_sb_Seq = new ArrayList<>();
        List<List> list_by_NC = new ArrayList<>();

        String name_seq;
        String line;
        String[] part_line;

        Matcher m_Accesion;
        Matcher m_NC;
        Matcher m_CDS;
        Matcher m_end_seq;
        Matcher m_End_CDS;

        try {
            final BufferedReader br = Files.newBufferedReader(file);
            while ((line = br.readLine()) != null) {
                m_Accesion = p_Accession.matcher(line);
                // get position of each "ACCESION  "
                if (m_Accesion.find()) {
                    m_NC = p_NC.matcher(line);
                    // find NC
                    if (m_NC.find()) {
                        part_line = line.split("   ",2);
                        list_sb_Seq.add(part_line[1]);
                        //list_sb_Seq.add(line);
                        while ((line = br.readLine()) != null) {
                            m_CDS = p_CDS.matcher(line);
                            //find CDS
                            if (m_CDS.find()) {
                                part_line = line.split("CDS             ",2);
                                name_seq = part_line[1];
                                //name_seq = line;
                                while ((line = br.readLine()) != null) {
                                    m_end_seq = p_end_seq.matcher(line);
                                    // find /gene
                                    if (m_end_seq.find()) {
                                        break;
                                    }
                                    part_line = line.split("                     ",2);
                                    if (name_seq.matches("\\d*\\.\\.\\d*")){
                                        name_seq = name_seq + part_line[1];
                                    }
                                }
                                int mode = stat.cds_mode(name_seq);
                                boolean test = stat.test_cds(name_seq,mode);
                                if(test) {
                                    list_sb_Seq.add(name_seq);
                                }
                            }
                            m_End_CDS = p_End_CDS.matcher(line);
                            // get AGCTGCTCG not Done yet
                            if(m_End_CDS.find())
                            {
                                list_by_NC.add(list_sb_Seq);
                                list_sb_Seq = new ArrayList<>();
                                break;
                            }
                        }
                    }
                }

            }

            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return list_by_NC;
    }


    // Create an xls file
    public WritableWorkbook create_xls_file(String file_path) {
        // Create an Excel file
        WritableWorkbook xls_file = null;
        try {
            xls_file = Workbook.createWorkbook(new File(file_path));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return xls_file;
    }

    // Write in xls sheet file
    public WritableWorkbook write_in_xlsFile(WritableWorkbook xls_file, String sheetName, String list_trinucl[], int tabP0[], int tabP1[], int tabP2[], int nb_trinucl, int nb_inv_CDS,int nb_valid_CDS) throws WriteException {
        try{
            //create an Excel sheet
            WritableSheet excelSheet = xls_file.createSheet(sheetName, 0);
            //add something into the Excel sheet
            int sum_P0 = 0;
            double sum_FrequenceP0 = 0;
            int sum_P1 = 0;
            double sum_FrequenceP1 = 0;
            int sum_P2 = 0;
            double sum_FrequenceP2 = 0;

            Label label = new Label(0, 0, "Tri");
            excelSheet.addCell(label);
            label = new Label(1, 0, "P0");
            excelSheet.addCell(label);
            label = new Label(2, 0, "Frequence");
            excelSheet.addCell(label);
            label = new Label(3, 0, "P1");
            excelSheet.addCell(label);
            label = new Label(4, 0, "Frequence");
            excelSheet.addCell(label);
            label = new Label(5, 0, "P2");
            excelSheet.addCell(label);
            label = new Label(6, 0, "Frequence");
            excelSheet.addCell(label);
            Number number;
            for (int i = 0 ; i < 64 ; i++){
                label = new Label(0, i+1, list_trinucl[i]);
                excelSheet.addCell(label);
                number = new Number(1, i+1, tabP0[i]);
                excelSheet.addCell(number);
                sum_P0 = sum_P0 + tabP0[i];
                number = new Number(2, i+1, (double) tabP0[i]/(nb_trinucl-1));
                excelSheet.addCell(number);
                sum_FrequenceP0 = sum_FrequenceP0 + (double) tabP0[i]/(nb_trinucl-1);
                number = new Number(3, i+1, tabP1[i]);
                excelSheet.addCell(number);
                sum_P1 = sum_P1 + tabP1[i];
                number = new Number(4, i+1, (double) tabP1[i]/(nb_trinucl-1));
                excelSheet.addCell(number);
                sum_FrequenceP1 = sum_FrequenceP1 + (double) tabP1[i]/(nb_trinucl-1);
                number = new Number(5, i+1, tabP2[i]);
                excelSheet.addCell(number);
                sum_P2 = sum_P2 + tabP2[i];
                number = new Number(6, i+1, (double) tabP2[i]/(nb_trinucl-1));
                excelSheet.addCell(number);
                sum_FrequenceP2 = sum_FrequenceP2 + (double) tabP2[i]/(nb_trinucl-1);
            }
            label = new Label(0, 65, "Total");
            excelSheet.addCell(label);
            number = new Number(1, 65, sum_P0);
            excelSheet.addCell(number);
            number = new Number(2, 65, round(sum_FrequenceP0));
            excelSheet.addCell(number);
            number = new Number(3, 65, sum_P1);
            excelSheet.addCell(number);
            number = new Number(4, 65, round(sum_FrequenceP1));
            excelSheet.addCell(number);
            number = new Number(5, 65, sum_P2);
            excelSheet.addCell(number);
            number = new Number(6, 65, round(sum_FrequenceP2));
            excelSheet.addCell(number);
            label = new Label(0, 67, "Number of invalid CDS");
            excelSheet.addCell(label);
            label = new Label(0, 68, "Number of CDS sequences");
            excelSheet.addCell(label);
            number = new Number(1, 67, nb_inv_CDS);
            excelSheet.addCell(number);
            number = new Number(1, 68, nb_valid_CDS);
            excelSheet.addCell(number);
        } catch (WriteException e) {
            e.printStackTrace();
        }
        return xls_file;
    }

    // Write General Information in front of each xls file that resume all data treated in this file
    public WritableWorkbook write_xlsFile_GI(WritableWorkbook xls_file, String NameOrganism, int nb_ValidCDS_seq,int nb_InvCDS_seq,int nb_seq) {
        try{
            //create an Excel sheet
            WritableSheet excelSheet = xls_file.createSheet("General Information",0);
            //add something into the Excel sheet
            Label label = new Label(0, 0, "Information");
            excelSheet.addCell(label);
            label = new Label(0, 2, "Name");
            excelSheet.addCell(label);
            label = new Label(1, 2, NameOrganism);
            excelSheet.addCell(label);
            label = new Label(0, 4, "Number of CDS sequences");
            excelSheet.addCell(label);
            Number number = new Number(1, 4, nb_ValidCDS_seq);
            excelSheet.addCell(number);
            label = new Label(0, 6, "Number of Invalid CDS");
            excelSheet.addCell(label);
            number = new Number(1, 6, nb_InvCDS_seq);
            excelSheet.addCell(number);
            label = new Label(0, 8, "Number of genomes");
            excelSheet.addCell(label);
            number = new Number(1, 8, nb_seq);
            excelSheet.addCell(number);
        } catch (WriteException e) {
            e.printStackTrace();
        }
        return xls_file;
    }

    // NEW sam 17h50

    // Write General Information in front of each xls file that resume all data treated in the folder
    public WritableWorkbook write_xlsFile_GI_Folder(WritableWorkbook xls_file, String NameGroup) {
        try{
            //create an Excel sheet
            WritableSheet excelSheet = xls_file.createSheet("General Information",0);
            //add something into the Excel sheet
            Label label = new Label(0, 0, "Information");
            excelSheet.addCell(label);
            label = new Label(0, 2, "Group Name");
            excelSheet.addCell(label);
            label = new Label(1, 2, NameGroup);
            excelSheet.addCell(label);
            label = new Label(0, 4, "Number of CDS sequences");
            excelSheet.addCell(label);
            Number number = new Number(1, 4, 0);
            excelSheet.addCell(number);
            label = new Label(0, 6, "Number of Invalid CDS");
            excelSheet.addCell(label);
            number = new Number(1, 6, 0);
            excelSheet.addCell(number);
            label = new Label(0, 8, "Number of genomes");
            excelSheet.addCell(label);
            number = new Number(1, 8, 0);
            excelSheet.addCell(number);
        } catch (WriteException e) {
            e.printStackTrace();
        }
        return xls_file;
    }

    // Update General Information in front of each xls file that resume all data treated in the folder
    public int update_xlsFile_GI_Folder(String pathname, int nb_ValidCDS_seq, int nb_InvCDS_seq, int nb_seq) {
        try{
            //open file
            Workbook wk = Workbook.getWorkbook(new File(pathname));
            WritableWorkbook wkr = Workbook.createWorkbook(new File(pathname), wk);
            WritableSheet excelSheet = wkr.getSheet(0);
            // get value and update
            WritableCell getcl = excelSheet.getWritableCell(1, 4);
            Number nb = ( Number ) getcl ;
            Number number = new Number(1, 4, nb_ValidCDS_seq+nb.getValue());
            excelSheet.addCell(number);
            getcl = excelSheet.getWritableCell(1, 6);
            nb = ( Number ) getcl ;
            number = new Number(1, 6, nb_InvCDS_seq+nb.getValue());
            excelSheet.addCell(number);
            getcl = excelSheet.getWritableCell(1, 8);
            nb = ( Number ) getcl ;
            number = new Number(1, 8, nb_seq+nb.getValue());
            excelSheet.addCell(number);
            // close file
            wkr.write();
            wkr.close();
        } catch (WriteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // END NEW sam 17h50

    // get all the sequence of a genome "ACGTGCACGTAGAC...."
    public List<String> get_seq(final Path file,List<List> list_by_NC, final int flags) {
        final Pattern p_origin = Pattern.compile("ORIGIN  ", flags);
        final Pattern p_end_seq = Pattern.compile("//", flags);
        Pattern p_NC;
        final List<String> list_sb_Seq = new ArrayList<>();
        StringBuffer sb_SubSeq = new StringBuffer();


        String line;

        Matcher m;
        Matcher end_seq;
        Matcher nc_seq;

        try {
            final BufferedReader br = Files.newBufferedReader(file);
            line = br.readLine();
            //search sequence for each NC number
            for (int l=0; l<list_by_NC.size(); l++) {
                if (line == null) {
                    break;
                }
                p_NC = Pattern.compile((String) list_by_NC.get(l).get(0), flags);
                nc_seq = p_NC.matcher(line);
                //search for NC
                while(!nc_seq.find()) {
                    line = br.readLine();
                    if (line == null)
                        break;
                    nc_seq = p_NC.matcher(line);
                }
                if (line == null)
                    break;
                m = p_origin.matcher(line);
                //Once NV find search for the begining of the sequence
                while (!m.find()) {
                    line = br.readLine();
                    m = p_origin.matcher(line);
                }
                line = br.readLine();
                end_seq = p_end_seq.matcher(line);
                //Once the begining find search for the end of the sequence
                while (!end_seq.find()) {
                    end_seq = p_end_seq.matcher(line);
                    String Seq = "";
                    //for each line until the end delete of space and number before each line
                    if (!line.equals("//")) {
                        String[] arrOfStr = line.split("[0-9]", 2);
                        String[] arrOfStr2 = arrOfStr[1].split(" ");
                        for (int j = 1; j<arrOfStr2.length; j++ ) {
                            Seq += arrOfStr2[j];
                        }
                        sb_SubSeq.append(Seq);
                    }
                    line =br.readLine();
                }
                if(sb_SubSeq.length()!=0) {
                      list_sb_Seq.add(sb_SubSeq.toString().toUpperCase());

                }
                /// 1 400 000 000 - RIP
                sb_SubSeq.delete(0, sb_SubSeq.length());
                line = br.readLine();
            }
            try {
                if (br != null)
                    br.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list_sb_Seq;
    }

    // Get sequence "ACGGTCGAT"
    public List<List> create_seq_cds(List<String> listseq, List<List> list_by_NC) {
        stat s = new stat();
        List<List> list_seq_cds = new ArrayList<>();
        List<List> list_seqCDS_invCDS = new ArrayList<>();
        List<String> seq_cds_byNC = new ArrayList<>();
        List<Integer> List_invalide_CDS_NC = new ArrayList<>();

        int invalide_cds_NC;
        String subseq = "";
        String subseq_bis = "";

        for (int i = 0; i<listseq.size(); i++) {
            invalide_cds_NC = 0;
            for (int j=1; j<list_by_NC.get(i).size(); j++) {
                int taille_seq;
                subseq = "";
                int m = s.cds_mode((String) list_by_NC.get(i).get(j));
                List<Integer> list_number = s.get_listnumber((String) list_by_NC.get(i).get(j), m);
                if (m == 0 ) {
                    subseq = listseq.get(i).substring(list_number.get(0)-1, list_number.get(1));
                    taille_seq = subseq.length();
                    if (s.test_gene(subseq, taille_seq) == -1) {
                        invalide_cds_NC++;
                        continue;
                    }
                    seq_cds_byNC.add(subseq);
                }
                else if(m==1) {
                    for (int l = 0; l<list_number.size(); l+=2) {
                        subseq += (listseq.get(i).substring(list_number.get(l)-1, list_number.get(l+1)));
                    }
                    taille_seq = subseq.length();
                    if (s.test_gene(subseq, taille_seq) == -1) {
                        invalide_cds_NC++;
                        continue;
                    }
                    seq_cds_byNC.add(subseq);
                }
                else if(m==2) {
                    subseq = listseq.get(i).substring(list_number.get(0)-1, list_number.get(0));
                    taille_seq = subseq.length();
                    if (s.test_gene(subseq, taille_seq) == -1) {
                        invalide_cds_NC++;
                        continue;
                    }
                    subseq_bis = s.inverseString(subseq);
                    seq_cds_byNC.add(subseq_bis);
                }
                else {
                    for (int l = 0; l<list_number.size(); l+=2) {
                        subseq += (listseq.get(i).substring(list_number.get(l)-1, list_number.get(l+1)));
                    }
                    taille_seq = subseq.length();
                    if (s.test_gene(subseq, taille_seq) == -1) {
                        invalide_cds_NC++;
                        continue;
                    }
                    subseq_bis = s.inverseString(subseq);
                    seq_cds_byNC.add(subseq_bis);
                }
            }
            List_invalide_CDS_NC.add(invalide_cds_NC);
            list_seq_cds.add(seq_cds_byNC);
            seq_cds_byNC = new ArrayList<>();
        }
        list_seqCDS_invCDS.add(list_seq_cds);
        list_seqCDS_invCDS.add(List_invalide_CDS_NC);
        return list_seqCDS_invCDS;
    }
}

