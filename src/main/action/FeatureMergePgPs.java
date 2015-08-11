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
import static uk.ac.man.pipelineconsistencytest.FeatureMergeMqPg.assayNo;

/**
 *
 * @author man-mqbsshz2
 */
public class FeatureMergePgPs {

    final static String SEPARATOR = ",";
    final static int assayNo = 10;
    final static int rt_win = 10;

    public static void main(String[] args) throws IOException {

//        String filePs = "C:/Manchester/work/Puf3Study/AnalysisResult/ProteoSuiteOutcomes"
//                + "/StatisticResult/feature_qvalue001.csv";

        int ps_assay_start = assayNo*3 + 7; 
        
//        String filePs = "C:/Manchester/work/ProteoSuite/proteosuite-0.3.5/puf3/"
//                + "unlabeled_result_FLUQT__mapped_v0.3.5_feature_with_pepCon.csv";
        String filePs = "C:/Manchester/result/RO_Puf3_yeast_WT_s6-s10/puf3_0.3.5_unlabeled_result_FLUQT_mapped_feature.csv";
        String filePg = "C:/Manchester/work/Puf3Study/AnalysisResult/ProgenesisOutcomes/12Aug2014/"
                + "feature.csv";

        //output
//        File fmergePgPs = new File("C:/Manchester/work/Puf3Study/AnalysisResult/Result/"
//                + "Mq_Pg_Ps/featureMergePgPs001.csv");
        
        File fmergePgPs = new File("C:/Manchester/work/ProteoSuite/proteosuite-0.3.5/puf3/"
                + "PS0.3.5_featureMergePgPs.csv");
        
        
//        File fmergePsPg = new File("C:/Manchester/work/Puf3Study/AnalysisResult/Result/"
//                + "Mq_Pg_Ps/featureMergePsPg001_abuEq.csv");
        StringBuilder sbFeature = new StringBuilder();

//        CSVReader csvReaderPs = new CSVReader(new StringReader(filePs), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, 1);
//        CSVReader csvReaderPg = new CSVReader(new StringReader(filePg), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, 3);
        CSVReader csvReaderPs = new CSVReader(new FileReader(filePs), ',', '\'', 1);
        CSVReader csvReaderPg = new CSVReader(new FileReader(filePg), ',', '\'', 3);

//        CSV csv = CSV.create();
//        csvPs = csv.read(filePs, new CSVReadProc());
//        System.out.println("output: ");
        String[] rowPs = null;
        String[] rowPg = null;
        String ps_assay;
        String pg_assay;
        //two decimal places
        DecimalFormat df = new DecimalFormat("#.##");

        sbFeature.append("PsSequence");
        sbFeature.append(SEPARATOR);
        sbFeature.append("PgSequence");
        sbFeature.append(SEPARATOR);
        sbFeature.append("psmzmean");
        sbFeature.append(SEPARATOR);
        sbFeature.append("psrtmean");
        sbFeature.append(SEPARATOR);
        sbFeature.append("charge");
        sbFeature.append(SEPARATOR);
        sbFeature.append("PsValidAssays");
        sbFeature.append(SEPARATOR);
        sbFeature.append("pgmz");
        sbFeature.append(SEPARATOR);
        sbFeature.append("pgrt");
        sbFeature.append(SEPARATOR);
        sbFeature.append("pgcharge");
        sbFeature.append(SEPARATOR);

        for (int i = 0; i < assayNo; i++) {
            ps_assay = "PsAssay";
            ps_assay = ps_assay + Integer.toString(i);
            sbFeature.append(ps_assay);
            sbFeature.append(SEPARATOR);
        }

        for (int i = 0; i < assayNo; i++) {
            pg_assay = "PgAssay";
            pg_assay = pg_assay + Integer.toString(i);
            sbFeature.append(pg_assay);
            sbFeature.append(SEPARATOR);
        }
        sbFeature.append("\n");

        List<String[]> psrows = csvReaderPs.readAll();
        List<String[]> pgrows = csvReaderPg.readAll();

        String[] ps_ass = new String[assayNo];
        String[] pg_ass = new String[assayNo];
        for (String[] pgrow : pgrows) {
            rowPg = pgrow;

            String dfpgmz = df.format(Double.valueOf(rowPg[1].substring(1, rowPg[1].length() - 1)));
            double pgmz = Double.valueOf(dfpgmz);
            double pgrt = 60 * Double.valueOf(rowPg[2].substring(1, rowPg[2].length() - 1));
            String pgcharge = rowPg[5].substring(1, rowPg[5].length() - 1);
            String pgseq0 = rowPg[56];
            String pgseq = null;
            if (!pgseq0.isEmpty()) {
                pgseq = pgseq0.substring(1, pgseq0.length() - 1);

                for (int i = 0; i < assayNo; i++) {
//                    pg_ass = "pgass";
//                    pg_ass = pg_ass + Integer.toString(i);
                    pg_ass[i] = rowPg[21 + i];
                }

                for (String[] psrow : psrows) {
                    rowPs = psrow;
                    String psseq = rowPs[1];

//                if (psseq.equalsIgnoreCase("LSVQDLDLK")) {
//                    System.out.println("first time ps sequence: " + psseq);
//                }
                    String validAssays = rowPs[4];
                    String charge = rowPs[6];

                    String dfpsmz = df.format(Double.valueOf(rowPs[2]));
                    double psmzmean = Double.valueOf(dfpsmz);
                    double psrtmean = Double.valueOf(rowPs[3]);

                    for (int i = 0; i < assayNo; i++) {
//                        ps_ass = "psass";
//                        ps_ass = ps_ass + Integer.toString(i);
                        ps_ass[i] = rowPs[ps_assay_start + i];
                    }

                    if (!psseq.equalsIgnoreCase("null")) {

                        if ((pgmz == psmzmean) && (pgrt <= psrtmean + rt_win) && (pgrt > psrtmean - rt_win)) {
//                        if (pgseq.equalsIgnoreCase(psseq)) {
//                            System.out.println("output: ");
                            sbFeature.append(psseq);
                            sbFeature.append(SEPARATOR);
                            sbFeature.append(pgseq);
                            sbFeature.append(SEPARATOR);
                            sbFeature.append(psmzmean);
                            sbFeature.append(SEPARATOR);
                            sbFeature.append(psrtmean);
                            sbFeature.append(SEPARATOR);
                            sbFeature.append(charge);
                            sbFeature.append(SEPARATOR);
                            sbFeature.append(validAssays);
                            sbFeature.append(SEPARATOR);
                            sbFeature.append(pgmz);
                            sbFeature.append(SEPARATOR);
                            sbFeature.append(pgrt);
                            sbFeature.append(SEPARATOR);
                            sbFeature.append(pgcharge);
                            sbFeature.append(SEPARATOR);

                            for (int i = 0; i < assayNo; i++) {
//                                ps_ass = "psass";
//                                ps_ass = ps_ass + Integer.toString(i);
                                sbFeature.append(ps_ass[i]);
                                sbFeature.append(SEPARATOR);
                            }

                            for (int i = 0; i < assayNo; i++) {
//                                pg_ass = "pgass";
//                                pg_ass = pg_ass + Integer.toString(i);
                                sbFeature.append(pg_ass[i]);
                                sbFeature.append(SEPARATOR);
                            }

                            sbFeature.append("\n");
                        }

                    }
                }
            }
        }
        csvReaderPg.close();
        csvReaderPs.close();

        ///////////////////////////////////////////////////////////////
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fmergePgPs));
            out.append(sbFeature.toString());
            out.close();
        } catch (IOException e) {
            System.out.println("Problems while closing file " + fmergePgPs + "!\n" + e);
        }
    }

}
