/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import static uk.ac.man.pipelineconsistencytest.FeatureMergeMqPg.assayNo;
import static uk.ac.man.pipelineconsistencytest.FeatureMergePgPs.assayNo;

/**
 *
 * @author man-mqbsshz2
 */
public class FeatureMergePsMq {

    final static String SEPARATOR = ",";
    final static int assayNo = 10;
    final static int rt_win = 10;
//    final int startLineMq = 2;

    public static void main(String[] args) throws IOException {

        int ps_assay_start = assayNo*3 + 7; 
        
        String fileEviMq = "C:/Manchester/work/Puf3Study/AnalysisResult/MaxQuantOutcomes/"
                + "txt_14Aug_fdr001/evidence.txt";
        String filePepMq = "C:/Manchester/work/Puf3Study/AnalysisResult/MaxQuantOutcomes/"
                + "txt_14Aug_fdr001/peptides.txt";

//        String filePs = "C:/Manchester/work/Puf3Study/AnalysisResult/ProteoSuiteOutcomes"
//                + "/StatisticResult/feature_qvalue001_protein.csv";

//        String filePs = "C:/Manchester/work/ProteoSuite/proteosuite-0.3.5/puf3/"
//                + "unlabeled_result_FLUQT__mapped_v0.3.5_feature_with_pepCon.csv";
        String filePs = "C:/Manchester/result/RO_Puf3_yeast_WT_s6-s10/"
                + "puf3_0.3.5_unlabeled_result_FLUQT_mapped_feature.csv";
        
        //output
//        File fmergePsMq = new File("C:/Manchester/work/Puf3Study/AnalysisResult/Result/"
//                + "Mq_Pg_Ps/featureMergePsMq_protein.csv");
        File fmergePsMq = new File("C:/Manchester/work/ProteoSuite/proteosuite-0.3.5/puf3/"
                + "PS0.3.5_featureMergePsMq_protein.csv");

        StringBuilder sbFeature = new StringBuilder();

        CSVReader csvReaderPs = new CSVReader(new FileReader(filePs), ',', '\'', 1);

        int startLineMq = 2;
        String[] rowPs = null;
//        String[] rowPg = null;
        String ps_assay;
        String mq_assay;
        //two decimal places
        DecimalFormat df = new DecimalFormat("#.##");

        sbFeature.append("MqSequence");
        sbFeature.append(SEPARATOR);
        sbFeature.append("PsSequence");
        sbFeature.append(SEPARATOR);
        sbFeature.append("MqProtein");
        sbFeature.append(SEPARATOR);
        sbFeature.append("PsProtein");
        sbFeature.append(SEPARATOR);

        sbFeature.append("MqMz");
        sbFeature.append(SEPARATOR);
        sbFeature.append("MqRt");
        sbFeature.append(SEPARATOR);
        sbFeature.append("MqCharge");
        sbFeature.append(SEPARATOR);

        sbFeature.append("PsMz");
        sbFeature.append(SEPARATOR);
        sbFeature.append("PsRt");
        sbFeature.append(SEPARATOR);
        sbFeature.append("PsCharge");
        sbFeature.append(SEPARATOR);
        sbFeature.append("PsValidAssays");
        sbFeature.append(SEPARATOR);

        for (int i = 0; i < assayNo; i++) {
            mq_assay = "MqAssay";
            mq_assay = mq_assay + Integer.toString(i);
            sbFeature.append(mq_assay);
            sbFeature.append(SEPARATOR);
        }

        for (int i = 0; i < assayNo; i++) {
            ps_assay = "PsAssay";
            ps_assay = ps_assay + Integer.toString(i);
            sbFeature.append(ps_assay);
            sbFeature.append(SEPARATOR);
        }

        sbFeature.append("\n");

        Scanner pepmq = new Scanner(new File(filePepMq));
        List<String[]> psrows = csvReaderPs.readAll();

        int countPepMq = 0;
        String[] mq_ass = new String[assayNo];
        String[] ps_ass = new String[assayNo];
        while (pepmq.hasNextLine()) {
            String rowPepMq = pepmq.nextLine();
            countPepMq++;

            if (countPepMq >= startLineMq) {
                String mqPepSeq = rowPepMq.split("\t")[0];
//                    String mqSeq = mqSeq0.split(";")[0];
                String mqPepScore = rowPepMq.split("\t")[37];
                String mqPepProt = rowPepMq.split("\t")[32];
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
//                        System.out.println("Evi seq: " + mqEviSeq);
//                        System.out.println("Pep seq: " + mqPepSeq);
                        if (mqEviSeq.equalsIgnoreCase(mqPepSeq) && mqEviScore.equalsIgnoreCase(mqPepScore)) {
//                            System.out.println("Evi seq: " + mqEviSeq);
//                            System.out.println("Pep seq: " + mqPepSeq);
                            for (String[] psrow : psrows) {
                                rowPs = psrow;
                                String psseq = rowPs[1];
//            String psseq;
//            if (psseq0 == null) {
//                psseq = "";
//            } else {
//                psseq = psseq0;
//            }
//                                if (psseq.equalsIgnoreCase("LSVQDLDLK")) {
//                                    System.out.println("first time ps sequence now: " + psseq);
//                                }
                                String validAssays = rowPs[4];
                                String pscharge = rowPs[6];

                                String dfpsmz = df.format(Double.valueOf(rowPs[2]));
                                double psmzmean = Double.valueOf(dfpsmz);
                                double psrtmean = Double.valueOf(rowPs[3]);
                                String psprot = rowPs[assayNo*3+7+assayNo];

                                for (int i = 0; i < assayNo; i++) {
//                                    ps_ass = "psass";
//                                    ps_ass = ps_ass + Integer.toString(i);
                                    ps_ass[i] = rowPs[ps_assay_start + i];
                                }

                                if (!psseq.equalsIgnoreCase("null")) {

                                    if ((mqEviMz == psmzmean) && (mqEviRt <= psrtmean + rt_win)
                                            && (mqEviRt > psrtmean - rt_win)) {
                                        //&& psass01 == pgass01 && psass11 == pgass11 && psass21 == pgass21) {
//                        if (pgseq.equalsIgnoreCase(psseq)) {

//                                        System.out.println("Mz: " + mqEviMz);
//                                        System.out.println("Rt: " + mqEviRt);
                                        sbFeature.append(mqEviSeq);
                                        sbFeature.append(SEPARATOR);
                                        sbFeature.append(psseq);
                                        sbFeature.append(SEPARATOR);
                                        sbFeature.append(mqPepProt);
                                        sbFeature.append(SEPARATOR);
                                        sbFeature.append(psprot);
                                        sbFeature.append(SEPARATOR);

                                        sbFeature.append(mqEviMz);
                                        sbFeature.append(SEPARATOR);
                                        sbFeature.append(mqEviRt);
                                        sbFeature.append(SEPARATOR);
                                        sbFeature.append(mqEviCharge);
                                        sbFeature.append(SEPARATOR);

                                        sbFeature.append(psmzmean);
                                        sbFeature.append(SEPARATOR);
                                        sbFeature.append(psrtmean);
                                        sbFeature.append(SEPARATOR);
                                        sbFeature.append(pscharge);
                                        sbFeature.append(SEPARATOR);
                                        sbFeature.append(validAssays);
                                        sbFeature.append(SEPARATOR);

                                        for (int i = 0; i < assayNo; i++) {
//                                            mq_ass = "mqass";
//                                            mq_ass = mq_ass + Integer.toString(i);
                                            sbFeature.append(mq_ass[i]);
                                            sbFeature.append(SEPARATOR);
                                        }

                                        for (int i = 0; i < assayNo; i++) {
//                                            ps_ass = "psass";
//                                            ps_ass = ps_ass + Integer.toString(i);
                                            sbFeature.append(ps_ass[i]);
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

        csvReaderPs.close();

        ///////////////////////////////////////////////////////////////
//        File fmergePsPg = new File("C:/Manchester/work/Puf3Study/AnalysisResult/Result/"
//                + "Mq_Pg_Ps/featureMergePsPg001_abuEq.csv");
//            BufferedWriter writerMerge = null; 
//            FileWriter finter = new FileWriter(fmergePsPg);
//            writerMerge = new BufferedWriter(finter);
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fmergePsMq));
            out.append(sbFeature.toString());
            out.close();
        } catch (IOException e) {
            System.out.println("Problems while closing file " + fmergePsMq + "!\n" + e);
        }
    }
}
