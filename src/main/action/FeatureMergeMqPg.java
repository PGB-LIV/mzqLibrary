/*
 * for puf3 file, it may be changed in format if cptac outcome is used.
 */
package uk.ac.man.pipelineconsistencytest;

import au.com.bytecode.opencsv.CSVReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Scanner;
import static uk.ac.man.pipelineconsistencytest.FeatureMergePsPg.SEPARATOR;

/**
 *
 * @author man-mqbsshz2
 */
public class FeatureMergeMqPg {

    final static String SEPARATOR = ",";
//    final int startLineMq = 2;
    final static int assayNo = 10;
    final static int rt_win = 10;

    public static void main(String[] args) throws IOException {

        String fileEviMq = "C:/Manchester/work/Puf3Study/AnalysisResult/MaxQuantOutcomes/"
                + "txt_14Aug_fdr001/evidence.txt";
        String filePepMq = "C:/Manchester/work/Puf3Study/AnalysisResult/MaxQuantOutcomes/"
                + "txt_14Aug_fdr001/peptides.txt";

        String filePg = "C:/Manchester/work/Puf3Study/AnalysisResult/ProgenesisOutcomes/12Aug2014/"
                + "feature.csv";

        //output
        File fmergeMqPg = new File("C:/Manchester/work/Puf3Study/AnalysisResult/Result/"
                + "Mq_Pg_Ps/featureMergeMqPg_protein_2015.csv");

        StringBuilder sbFeature = new StringBuilder();

//        CSVReader csvReaderPs = new CSVReader(new FileReader(filePs), ',', '\'', 1);
        CSVReader csvReaderPg = new CSVReader(new FileReader(filePg), ',', '\'', 3);

        int startLineMq = 2;
        String[] rowPs = null;
        String[] rowPg = null;
        String mq_assay;
        String pg_assay;
        //two decimal places
        DecimalFormat df = new DecimalFormat("#.##");

        sbFeature.append("MqSequence");
        sbFeature.append(SEPARATOR);
        sbFeature.append("PgSequence");
        sbFeature.append(SEPARATOR);
        sbFeature.append("MqProtein");
        sbFeature.append(SEPARATOR);
        sbFeature.append("PgProtein");
        sbFeature.append(SEPARATOR);

        sbFeature.append("MqMz");
        sbFeature.append(SEPARATOR);
        sbFeature.append("MqRt");
        sbFeature.append(SEPARATOR);
        sbFeature.append("MqCharge");
        sbFeature.append(SEPARATOR);
        sbFeature.append("PgMz");
        sbFeature.append(SEPARATOR);
        sbFeature.append("PgRt");
        sbFeature.append(SEPARATOR);
        sbFeature.append("PgCharge");
        sbFeature.append(SEPARATOR);

        for (int i = 0; i < assayNo; i++) {
            mq_assay = "MqAssay";
            mq_assay = mq_assay + Integer.toString(i);
            sbFeature.append(mq_assay);
            sbFeature.append(SEPARATOR);
        }

        for (int i = 0; i < assayNo; i++) {
            pg_assay = "PgAssay";
            pg_assay = pg_assay + Integer.toString(i);
            sbFeature.append(pg_assay);
            sbFeature.append(SEPARATOR);
        }

        sbFeature.append("\n");

        Scanner pepmq = new Scanner(new File(filePepMq));
        List<String[]> pgrows = csvReaderPg.readAll();

        int countPepMq = 0;
        String[] mq_ass = new String[assayNo];
        String[] pg_ass = new String[assayNo];
        while (pepmq.hasNextLine()) {
            String rowPepMq = pepmq.nextLine();
            countPepMq++;

            if (countPepMq >= startLineMq) {
                String mqPepSeq = rowPepMq.split("\t")[0];
//                    String mqSeq = mqSeq0.split(";")[0];
                String mqPepProt = rowPepMq.split("\t")[32];
                String mqPepScore = rowPepMq.split("\t")[37];

//                    double mqPepScore = Double.parseDouble(mqScore0);
//                String mqMass0 = rowPepMq.split("\t")[30];
//                double mqPepMass = Double.parseDouble(mqMass0);
                for (int i = 0; i < assayNo; i++) {
//                    mq_ass = "mqass";
//                    mq_ass = mq_ass + Integer.toString(i);
                    mq_ass[i] = rowPepMq.split("\t")[61 + i];
                }

                Scanner evimq = new Scanner(new File(fileEviMq));
                int countEviMq = 0;

                while (evimq.hasNextLine()) {
                    String rowEviMq = evimq.nextLine();
                    countEviMq++;
                    if (countEviMq >= startLineMq) {
                        String mqEviSeq = rowEviMq.split("\t")[0];
                        String mqEviCharge = rowEviMq.split("\t")[17];
                        String dfmqEviMz = df.format(Double.valueOf(rowEviMq.split("\t")[18]));
                        double mqEviMz = Double.valueOf(dfmqEviMz);
//                        String mqEviMass = rowEviMq.split("\t")[19];
                        String mqEviRt0 = rowEviMq.split("\t")[25];
                        double mqEviRt = 60 * Double.valueOf(mqEviRt0);
                        String mqEviScore = rowEviMq.split("\t")[43];

//                    System.out.println("Evi seq: " + k);
//System.out.println("Evi seq: " + mqEviSeq);
//System.out.println("Pep seq: " + mqPepSeq);
                        if (mqEviSeq.equalsIgnoreCase(mqPepSeq) && mqEviScore.equalsIgnoreCase(mqPepScore)) {
//                            System.out.println("Evi seq: " + mqEviSeq);
//                            System.out.println("Pep seq: " + mqPepSeq);
                            for (String[] pgrow : pgrows) {
                                rowPg = pgrow;

                                String dfpgmz = df.format(Double.valueOf(rowPg[1].substring(1, rowPg[1].length() - 1)));
                                double pgmz = Double.valueOf(dfpgmz);
                                double pgrt = 60 * Double.valueOf(rowPg[2].substring(1, rowPg[2].length() - 1));
                                String pgcharge = rowPg[5].substring(1, rowPg[5].length() - 1);
                                String pgprot = rowPg[55];
                                String pgseq0 = rowPg[56];
                                String pgseq = null;
                                if (!pgseq0.isEmpty()) {
                                    pgseq = pgseq0.substring(1, pgseq0.length() - 1);

                                    for (int i = 0; i < assayNo; i++) {
//                                        pg_ass = "pgAss";
//                                        pg_ass = pg_ass + Integer.toString(i);
                                        pg_ass[i] = rowPepMq.split("\t")[21 + i];
                                    }

                                    if ((mqEviMz == pgmz) && (mqEviRt <= pgrt + rt_win)
                                            && (mqEviRt > pgrt - rt_win)) {
                                        //&& psass01 == pgass01 && psass11 == pgass11 && psass21 == pgass21) {
//                        if (pgseq.equalsIgnoreCase(psseq)) {

//                                        System.out.println("Mz: " + mqEviMz);
//                                        System.out.println("Rt: " + mqEviRt);
                                        sbFeature.append(mqEviSeq);
                                        sbFeature.append(SEPARATOR);
                                        sbFeature.append(pgseq);
                                        sbFeature.append(SEPARATOR);
                                        sbFeature.append(mqPepProt);
                                        sbFeature.append(SEPARATOR);
                                        sbFeature.append(pgprot);
                                        sbFeature.append(SEPARATOR);

                                        sbFeature.append(mqEviMz);
                                        sbFeature.append(SEPARATOR);
                                        sbFeature.append(mqEviRt);
                                        sbFeature.append(SEPARATOR);
                                        sbFeature.append(mqEviCharge);
                                        sbFeature.append(SEPARATOR);

                                        sbFeature.append(pgmz);
                                        sbFeature.append(SEPARATOR);
                                        sbFeature.append(pgrt);
                                        sbFeature.append(SEPARATOR);
                                        sbFeature.append(pgcharge);
                                        sbFeature.append(SEPARATOR);

                                        for (int i = 0; i < assayNo; i++) {
//                                            mq_ass = "mqass";
//                                            mq_ass = mq_ass + Integer.toString(i);
                                            sbFeature.append(mq_ass[i]);
                                            sbFeature.append(SEPARATOR);
                                        }
          
                                        for (int i = 0; i < assayNo; i++) {
//                                        pg_ass = "pgAss";
//                                        pg_ass = pg_ass + Integer.toString(i);
                                        sbFeature.append(pg_ass[i]);
                                        sbFeature.append(SEPARATOR);
                                    }
                                        
                                        sbFeature.append("\n");

                                    }

                                }
                            }
                            break;
                        }
                    }

                }
            }

        }
        csvReaderPg.close();

        ///////////////////////////////////////////////////////////////
//        File fmergePsPg = new File("C:/Manchester/work/Puf3Study/AnalysisResult/Result/"
//                + "Mq_Pg_Ps/featureMergePsPg001_abuEq.csv");
//            BufferedWriter writerMerge = null; 
//            FileWriter finter = new FileWriter(fmergePsPg);
//            writerMerge = new BufferedWriter(finter);
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fmergeMqPg));
            out.append(sbFeature.toString());
            out.close();
        } catch (IOException e) {
            System.out.println("Problems while closing file " + fmergeMqPg + "!\n" + e);
        }
    }

}
