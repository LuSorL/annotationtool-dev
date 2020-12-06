import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class thread implements Runnable {
    private String type;
    private Integer file_type;
    private  Interface I;

    public thread(String type, int file_type, Interface I) {
        this.type  = type;
        this.file_type = file_type;
        this.I  = I;
    }

    @Override
    public void run() {
        System.out.println("Thread:" + this.type + " " + this.file_type);
        //I.AddMsgLog("thread lancé pour " + type) ;
        findInFile s = new findInFile();
        Dezip u      = new Dezip();
        stat stat    = new stat();
        Create_treestructure createstruct = new Create_treestructure();

        //InputStream fna = null;
        String path;
        List<String> list_Name = new ArrayList<>();
        List<String> list_Group = new ArrayList<>();
        List<String> list_path = new ArrayList<>();
        List<String> list_SubGroup = new ArrayList<>();


        // for all type (Eukaryote ...) but only work for Eukaryote
        try {

            //System.out.println("file treated "+ Paths.get(this.type+".txt"));

            //finding NC in .txt files corresponding to the files we need to download
            List<List> list_charac = s.findInFile(Paths.get(this.type+".txt"), "NC_", 0,this.file_type);

            list_Name = list_charac.get(0);
            list_Group = list_charac.get(1);
            list_SubGroup = list_charac.get(2);

            //list_GCA = list_charac.get(3);
                /*
                System.out.println(Arrays.toString(list_Name.toArray()));
                System.out.println(Arrays.toString(list_Group.toArray()));
                System.out.println(Arrays.toString(list_SubGroup.toArray()));
                */

            //System.out.println(Arrays.toString(list_GCA.toArray()));
            String[] pathName1;
            String[] pathName;

            I.AddMsgLog("file downloading for " + type);

            for (String a : list_Name) {
                //looking for the path ot downloads in the summary.txt which contains all of informations wih a given name
                path = s.findPathInFile(Paths.get("summary.txt"), a, 0);
                //System.out.println("__________________" + list_Name.size());
                if (path.length() > 5) {
                    pathName1 = path.split("\t", 2);
                    pathName = pathName1[0].split("/", 10);
                    if (pathName.length > 6){
                        list_path.add(pathName1[0] + "/" + pathName[9] + "_genomic.gbff.gz");
                    }
                }
            }
            //System.out.println(Arrays.toString(list_path.toArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (int r = 0; r < list_path.size(); r++) {
            int avancement = file_type + (int)((r/list_path.size())/4);
            String Name_genome = list_Name.get(r).replaceAll("/", "_");
            //create folder
            String link = "./Results/" + this.type + "/" + list_Group.get(r) + "/" + list_SubGroup.get(r);
            createstruct.create_folder(link);
            // create xls file
            File exist = new File(link + "/" + Name_genome + ".xls");
            //System.out.println("create folder : " +exist);
            if (exist.exists()) {
                continue;
            }

            I.AddMsgLog("treating " + Name_genome);
                /*
                try {
                    fna = new URL(list_path.get(r)).openStream();
                    System.out.println(list_path.get(r));


                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            String filenameDNA = "DownloadDone" + file_type + ".txt";
            String DNA_source = "DNA_file" + file_type + ".gbff.gz";


            try {
                //System.out.println(list_path.get(r));
                FileUtils.copyURLToFile(
                        new URL(list_path.get(r)),
                        new File(Paths.get(DNA_source).toString()));
                //Files.copy(fna, Paths.get("DNA_file.gbff.gz"), StandardCopyOption.REPLACE_EXISTING);

            } catch (IOException e) {
                e.printStackTrace();
            }



            //NEW
            String[] link_name = link.split("/");
            String link_name_temp=link_name[0] + "/";
            for (int i=1;i<link_name.length;i++){
                link_name_temp = link_name_temp+"/"+link_name[i];
                File existe = new File(link_name_temp+ ".xls");
                if (existe.exists()) {
                    System.out.println("file already exist: " +exist);
                    continue;
                }
                WritableWorkbook xls_file_general = s.create_xls_file(link_name_temp+".xls");
                xls_file_general = s.write_xlsFile_GI_Folder(xls_file_general,link_name[i]);
                if (xls_file_general != null) {
                    try {
                        xls_file_general.write();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        xls_file_general.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (WriteException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("create file: " +exist);
            }
            //END NEW

            u.gunzipIt(DNA_source, filenameDNA);
            File file_to_delete = new File(DNA_source);
            file_to_delete.delete();

            String gene = null;
            Path filePath = Paths.get(filenameDNA);
            List<List> list_by_NC = new ArrayList<>();
            List<String> list_seq = new ArrayList<>();
            List<List> list_seq_cds_byNC = new ArrayList<>();
            List<List> list_seqCDS_invCDS_NC = new ArrayList<>();
            List<Integer> list_invCDS = new ArrayList<>();
            String name_sequence = null;
            try {
                //Read NC gene
                list_by_NC = s.getSeqNC(filePath, 0);
                list_seq = s.get_seq(filePath, list_by_NC, 0);
                list_seqCDS_invCDS_NC = s.create_seq_cds(list_seq, list_by_NC);
                list_seq_cds_byNC = list_seqCDS_invCDS_NC.get(0);
                list_invCDS = list_seqCDS_invCDS_NC.get(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            WritableWorkbook xls_file = s.create_xls_file(link + "/" + Name_genome + ".xls");
            boolean something_to_write = false;
            int nb_inv_CDS;
            int nb_ValidCDS_seq = 0;
            int nb_InvCDS_seq = 0;
            int nb_seq = 0;
            for (int sequence = 0; sequence < list_seq.size(); sequence += 1) {
                // get sequence
                try {
                    name_sequence = s.underscoreName((String) list_by_NC.get(sequence).get(0),
                            2, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                name_sequence = "DNA" + name_sequence;
                nb_inv_CDS = list_invCDS.get(sequence);

                // Tableau
                int occ0[] = new int[64];
                int occ1[] = new int[64];
                int occ2[] = new int[64];

                stat.init_tab(occ0, 64);
                stat.init_tab(occ1, 64);
                stat.init_tab(occ2, 64);

                int nb_trinucl_total = 0;
                String list[] = new String[64];

                // list trinucléotide
                char base[] = {'A', 'C', 'G', 'T'};
                StringBuffer trinucl = new StringBuffer("---");

                for (int i = 0; i < 4; i++) {
                    trinucl.setCharAt(0, base[i]);
                    for (int j = 0; j < 4; j++) {
                        trinucl.setCharAt(1, base[j]);
                        for (int k = 0; k < 4; k++) {
                            trinucl.setCharAt(2, base[k]);
                            list[k + 4 * j + 16 * i] = trinucl.toString();
                        }
                    }
                }

                for (int z = 0; z < list_seq_cds_byNC.get(sequence).size(); z++) {
                    gene = (String) list_seq_cds_byNC.get(sequence).get(z);
                    int taille_gene = gene.length();
                    // Calcul d'occurence
                    nb_trinucl_total += taille_gene / 3;
                    int nb_trinucl = taille_gene / 3;
                    if (stat.occurence(list, occ0, occ1, occ2, gene, nb_trinucl) == -1) {
                        continue;
                    }
                }

                try {
                    xls_file = s.write_in_xlsFile(xls_file, name_sequence, list, occ0, occ1, occ2, nb_trinucl_total,
                            nb_inv_CDS, list_by_NC.get(sequence).size() - 1);
                    nb_InvCDS_seq = nb_InvCDS_seq + nb_inv_CDS;
                    nb_ValidCDS_seq = nb_ValidCDS_seq + list_by_NC.get(sequence).size() - 1;
                    nb_seq++;
                } catch (WriteException e) {
                    e.printStackTrace();
                }
                something_to_write = true;
            }
            xls_file = s.write_xlsFile_GI(xls_file, Name_genome, nb_ValidCDS_seq,nb_InvCDS_seq, nb_seq);
            if (xls_file != null) {
                try {
                    if (something_to_write) {
                        s.update_xlsFile_GI_Folder("./Results/" + type +".xls",nb_ValidCDS_seq,nb_InvCDS_seq, nb_seq);
                        s.update_xlsFile_GI_Folder("./Results/" + type + "/" + list_Group.get(r)+".xls",nb_ValidCDS_seq,nb_InvCDS_seq, nb_seq);
                        s.update_xlsFile_GI_Folder(link+".xls",nb_ValidCDS_seq,nb_InvCDS_seq, nb_seq);
                        xls_file.write();
                    }
                    xls_file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (WriteException e) {
                    e.printStackTrace();
                }
            }
            file_to_delete = new File(filenameDNA);
            file_to_delete.delete();

            this.I.AddMsgLog("done with " + Name_genome);

            this.I.updatePercentage(1);



        }

        this.I.updatePercentage(-1);

    }
}