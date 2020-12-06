import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.*;


class download implements Runnable{
    Interface I;

    public download(Interface I) {
        this.I = I;
    }
    @Override
    public void run() {
        this.I.progressBar.setVisible(true);

        /*
        Runnable runner = new cheat(this.I);
        Thread t = new Thread(runner);
        t.start();
        */

        List<String> list_type = new ArrayList<>();

        //Interface I = new Interface();

        //findInFile s = new findInFile();
        //Dezip u = new Dezip();
        //stat stat = new stat();
        //Create_treestructure createstruct = new Create_treestructure();

        //arborescence
        File dossier = new File("Results");
        File eukaryotes = new File("./Results/Eukaryotes");
        File prokaryotes = new File("./Results/Prokaryotes");
        File plasmids = new File("./Results/Plasmids");
        File viruses = new File("./Results/Viruses");

        // Create files
        dossier.mkdir();
        eukaryotes.mkdir();
        prokaryotes.mkdir();
        plasmids.mkdir();
        viruses.mkdir();

        InputStream euka = null;
        InputStream summar = null;
        InputStream plasmi = null;
        InputStream viruse = null;
        InputStream prok = null;
        try {
            //Download of .txt we need to parse Genbank if it's not already done.
            File eukar = new File("Eukaryotes.txt");
            File summary = new File("summary.txt");
            File plasm = new File("Plasmids.txt");
            File virus= new File("Viruses.txt");
            File proka = new File("Prokaryotes.txt");

            if(!eukar.exists()) {
                I.AddMsgLog("Downloading Eukaryote.txt...");
                euka = new URL("ftp://ftp.ncbi.nlm.nih.gov/genomes/GENOME_REPORTS/eukaryotes.txt").openStream();
                Files.copy(euka, Paths.get("Eukaryotes.txt"), StandardCopyOption.REPLACE_EXISTING);
            }
            if(!proka.exists()) {
                I.AddMsgLog("Downloading Prokaryote.txt...");
                prok = new URL("ftp://ftp.ncbi.nlm.nih.gov/genomes/GENOME_REPORTS/prokaryotes.txt").openStream();
                Files.copy(prok, Paths.get("Prokaryotes.txt"), StandardCopyOption.REPLACE_EXISTING);
            }
            if(!summary.exists()) {
                I.AddMsgLog("Downloading summary.txt...");
                summar = new URL("https://ftp.ncbi.nlm.nih.gov/genomes/refseq/assembly_summary_refseq.txt").openStream();
                Files.copy(summar, Paths.get("summary.txt"), StandardCopyOption.REPLACE_EXISTING);
            }
            if(!plasm.exists()) {
                I.AddMsgLog("Downloading Plasmids.txt...");
                plasmi = new URL("ftp://ftp.ncbi.nlm.nih.gov/genomes/GENOME_REPORTS/plasmids.txt").openStream();
                Files.copy(plasmi, Paths.get("Plasmids.txt"), StandardCopyOption.REPLACE_EXISTING);
            }
            if(!virus.exists()) {
                I.AddMsgLog("Downloading Viruses.txt...");
                viruse = new URL("ftp://ftp.ncbi.nlm.nih.gov/genomes/GENOME_REPORTS/viruses.txt").openStream();
                Files.copy(viruse, Paths.get("Viruses.txt"), StandardCopyOption.REPLACE_EXISTING);
            }

            Boolean multi_thread = Boolean.TRUE;

            if (multi_thread) {
                Map<String, Integer> dictionary = new HashMap<String, Integer>();
                dictionary.put("Eukaryotes", 0);
                dictionary.put("Prokaryotes", 1);
                dictionary.put("Plasmids", 2);
                dictionary.put("Viruses", 3);

                for (Map.Entry<String, Integer> e : dictionary.entrySet()) {
                    Runnable runner = new thread(e.getKey(), e.getValue(), this.I);
                    Thread t = new Thread(runner);
                    t.start();
                }
            }
            else {
                Runnable runner = new thread("Eukaryotes", 0, this.I);
                Thread t = new Thread(runner);
                t.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}