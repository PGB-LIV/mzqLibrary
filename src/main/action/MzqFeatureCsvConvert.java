/*
 * convert the info. in feature mzq file to csv file
 */
package uk.ac.man.mzqcsvparser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import uk.ac.liv.jmzqml.MzQuantMLElement;
import uk.ac.liv.jmzqml.model.mzqml.DataMatrix;
import uk.ac.liv.jmzqml.model.mzqml.EvidenceRef;
import uk.ac.liv.jmzqml.model.mzqml.Feature;
import uk.ac.liv.jmzqml.model.mzqml.FeatureList;
import uk.ac.liv.jmzqml.model.mzqml.IdOnly;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensus;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.jmzqml.model.mzqml.Protein;
import uk.ac.liv.jmzqml.model.mzqml.ProteinList;
import uk.ac.liv.jmzqml.model.mzqml.QuantLayer;
import uk.ac.liv.jmzqml.model.mzqml.Row;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;

/**
 *
 * @author man-mqbsshz2
 */
public class MzqFeatureCsvConvert {

    final static String SEPARATOR = ",";
    final static int assayNo = 10; //puf3: 2*5=10; CPTAC: B,C,D,E, 4*3=12

    @SuppressWarnings("empty-statement")
    public static void main(String[] args) {
//        Timer timer = new Timer(true);
//        System.out.println("TimerTask begins! :" + new Date());

//        String refNo = "RefAssay2";
        String filter = "qvalue001";
//        String prot_aql_id = "PGL_Pep_AQL.1"; //Progenesis: protein normalised abundance
//        String pep_aql_id = "Pep_AQL.1"; //Normalised peptide abundance
//        StringBuilder sb = new StringBuilder();
//        StringBuilder sb_pep = new StringBuilder();
//        StringBuilder sb_grp = new StringBuilder();
        StringBuilder sbFeature = new StringBuilder();

//        ArrayList<QuantitationLevel> proteinGroups = new ArrayList<QuantitationLevel>();
//        File mzqFile = new File("C:\\Manchester\\work\\Puf3Study\\ProteoSuite\\IdenOutcomes\\Filtered_AJ_" + filter
//                + "\\mzq\\unlabeled_result_FLUQT_mapped_qvalue001.mzq"); //rest
//        String outputFile = "C:\\Manchester\\work\\Puf3Study\\AnalysisResult\\ProteoSuiteOutcomes\\StatisticResult\\"
//                + "feature_" + filter + "_protein_id.csv";
//        File mzqFile = new File("C:\\Manchester\\work\\ProteoSuite\\proteosuite-0.3.4\\puf3\\unlabeled_result_FLUQT_mapped.mzq");
//        String outputFile = "C:\\Manchester\\work\\ProteoSuite\\proteosuite-0.3.4\\puf3\\unlabeled_result_FLUQT__mapped_feature.csv";
//        File mzqFile = new File("C:\\Manchester\\work\\ProteoSuite\\proteosuite-0.3.4\\CPTAC_unlabeled_result_FLUQT_mapped.mzq");
//        String outputFile = "C:\\Manchester\\work\\ProteoSuite\\proteosuite-0.3.4\\CPTAC_unlabeled_result_FLUQT__mapped_feature_with_pepCon.csv";
//        File mzqFile = new File("C:\\Manchester\\work\\ProteoSuite\\proteosuite-0.3.5\\puf3\\unlabeled_result_FLUQT_mapped_v0.3.5.mzq");
        File mzqFile = new File("C:\\Manchester\\result\\RO_Puf3_yeast_WT_s6-s10\\puf3_0.3.5_unlabeled_result_FLUQT_mapped.mzq");
//        String outputFile = "C:\\Manchester\\work\\ProteoSuite\\proteosuite-0.3.5\\puf3\\unlabeled_result_FLUQT_mapped_v0.3.5_feature_with_pepCon.csv";
        String outputFile = "C:\\Manchester\\result\\RO_Puf3_yeast_WT_s6-s10\\puf3_0.3.5_unlabeled_result_FLUQT_mapped_feature.csv";

        MzQuantMLUnmarshaller um = new MzQuantMLUnmarshaller(mzqFile);
//        MzQuantML mzq = new MzQuantML();

//        ProteinGroupList protGroupList = um.unmarshal(MzQuantMLElement.ProteinGroupList);
//        Iterator<ProteinGroupList> protGrpListIter = um.unmarshalCollectionFromXpath(MzQuantMLElement.ProteinGroupList);
        ProteinList protList = um.unmarshal(MzQuantMLElement.ProteinList);
        List<Protein> proteins = protList.getProtein();
//        PeptideConsensusList pepConsensusList = um.unmarshal(MzQuantMLElement.PeptideConsensusList);
        Iterator<PeptideConsensusList> pepConListIter = um.unmarshalCollectionFromXpath(MzQuantMLElement.PeptideConsensusList);

        //convert Iterator to a list for calling efficiently and saving time when using in searching the feature
        Iterator<FeatureList> featureListIter = um.unmarshalCollectionFromXpath(MzQuantMLElement.FeatureList);
        List<FeatureList> featureLists = new ArrayList<FeatureList>();
        while (featureListIter.hasNext()) {
            FeatureList fl = featureListIter.next();
            featureLists.add(fl);
        }

//        List<FeatureList> featureLists = um.unmarshal(MzQuantMLElement.FeatureList);
//        Iterator<FeatureList> feaListIter = um.unmarshalCollectionFromXpath(MzQuantMLElement.FeatureList);
//        List<QuantLayer> assayQuantLayers = protGroupList.getAssayQuantLayer();
//        List<QuantLayer> protAssayQuantLayers = protList.getAssayQuantLayer();
//        int protAQLNo = protGroupList.getAssayQuantLayer().size();
//        System.out.println("protein quant layer number: " + protAQLNo);
        String mz_assay;
        String rt_assay;
        String charge_assay;
        String assay_position;

        sbFeature.append("PepCon");
        sbFeature.append(SEPARATOR);
        sbFeature.append("Sequence");
        sbFeature.append(SEPARATOR);
        sbFeature.append("mz_mean");
        sbFeature.append(SEPARATOR);
        sbFeature.append("rt_mean");
        sbFeature.append(SEPARATOR);
        sbFeature.append("No of valid assays");
        sbFeature.append(SEPARATOR);
        sbFeature.append("Identifications");
        sbFeature.append(SEPARATOR);
        sbFeature.append("PepCharge");
        sbFeature.append(SEPARATOR);

        for (int i = 0; i < assayNo; i++) {
            mz_assay = "mz_";
            rt_assay = "rt_";
            charge_assay = "Charge_";

            mz_assay = mz_assay + Integer.toString(i);
            rt_assay = rt_assay + Integer.toString(i);
            charge_assay = charge_assay + Integer.toString(i);
//        sbFeature.append("mz_0");
            sbFeature.append(mz_assay);
            sbFeature.append(SEPARATOR);
//        sbFeature.append("rt_0");
            sbFeature.append(rt_assay);
            sbFeature.append(SEPARATOR);
//        sbFeature.append("Charge_0");
            sbFeature.append(charge_assay);
            sbFeature.append(SEPARATOR);
        }
//        sbFeature.append("mz_1");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("rt_1");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Charge_1");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("mz_2");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("rt_2");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Charge_2");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("mz_3");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("rt_3");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Charge_3");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("mz_4");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("rt_4");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Charge_4");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("mz_5");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("rt_5");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Charge_5");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("mz_6");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("rt_6");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Charge_6");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("mz_7");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("rt_7");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Charge_7");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("mz_8");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("rt_8");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Charge_8");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("mz_9");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("rt_9");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Charge_9");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("mz_10");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("rt_10");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Charge_10");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("mz_11");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("rt_11");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Charge_11");
//        sbFeature.append(SEPARATOR);

        for (int i = 0; i < assayNo; i++) {
            assay_position = "Assay";
            assay_position = assay_position + Integer.toString(i);
            sbFeature.append(assay_position);
            sbFeature.append(SEPARATOR);
        }
//        sbFeature.append("Assay1");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Assay2");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Assay3");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Assay4");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Assay5");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Assay6");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Assay7");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Assay8");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Assay9");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Assay10");
//        sbFeature.append(SEPARATOR);
//        sbFeature.append("Assay11");
//        sbFeature.append(SEPARATOR);

        sbFeature.append("Protein");
        sbFeature.append(SEPARATOR);
        sbFeature.append("\n");

//        int arrNo = 30; //attributes to save for 10 fold of each assay
//        int assayNo = 12; //CPTAC: B,C,D,E, 4*3=12
//        int k = 0;
        while (pepConListIter.hasNext()) {
            PeptideConsensusList pepConList = pepConListIter.next();
            List<PeptideConsensus> pepCons = pepConList.getPeptideConsensus();
            List<QuantLayer<IdOnly>> pepAssayQuantLayers = pepConList.getAssayQuantLayer();
            for (PeptideConsensus pepCon : pepCons) {
                boolean protSignal = false;
                String pepConProtein = null;
                List<String> pepChargeTmp = pepCon.getCharge();
                String pepCharge = pepChargeTmp.toString().substring(1, 2);

                String pepConId = pepCon.getId(); //pepCon_1
                //obtain protein ID
                for (Protein prot : proteins) {
                    String accession = prot.getAccession();
                    List<String> pepConRefs = prot.getPeptideConsensusRefs();
                    for (String pepConRef : pepConRefs) {
                        if (pepConRef.equalsIgnoreCase(pepConId)) {
                            pepConProtein = accession; //YDR158W
                            protSignal = true;
                            break;
                        }
                    }
                    if (protSignal) {
                        break;
                    }

                }

//                System.out.println("pepCon ID: " + pepConId);
                List<EvidenceRef> eviRefs = pepCon.getEvidenceRef();
                String sequence = pepCon.getPeptideSequence();

                String chargeArray[] = new String[assayNo];
                double[] mzArray = new double[assayNo];
                double[] rtArray = new double[assayNo];
                for (int i = 0; i < assayNo; i++) {
                    chargeArray[i] = "";
                }
                for (int i = 0; i < assayNo; i++) {
                    mzArray[i] = 0;
                }
                for (int i = 0; i < assayNo; i++) {
                    rtArray[i] = 0;
                }

//                String chargeArray[] = new String[]{"", "", "", "", "", "", "", "", "", "", "", ""};
//                double[] mzArray = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//                double[] rtArray = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                double mzmean = 0;
                double rtmean = 0;
                int number_valid = 0;
                int number_id = 0;
//            String[] idArray = new String[arrNo];
                //assign the initial values to the arrays

                for (EvidenceRef eviRef : eviRefs) {
                    String featureRef = eviRef.getFeatureRef();

//                    System.out.println("feture_ref: " + pepConId + " : " + featureRef);
                    String featureIdenFileRef = eviRef.getIdentificationFileRef();

//                    List<String> featureIdRefs = eviRef.getIdRefs();
                    List<String> featureAssRefs = eviRef.getAssayRefs();

//                    String assNo = featureAssRefs.toString().substring(5, 6);
                    int assRefsLength = featureAssRefs.get(0).length();
                    String assNo = null;
                    if (assRefsLength == 5) {
                        assNo = featureAssRefs.get(0).substring(4, 5);
                    } else if (assRefsLength == 6) {
                        assNo = featureAssRefs.get(0).substring(4, 6);
                    }
//System.out.println("assay No 0: " + featureAssRefs.get(0).substring(4, 5));
//System.out.println("assay No 1: " + featureAssRefs.get(1).substring(4, 5));
                    String featureCharge = null;
                    double featureMz = 0;
                    double featureRt = 0;
                    String featureId = null;
                    int signal = 0;

//                    System.out.println("feature iden file ref: " + featureIdenFileRef);
                    if (featureIdenFileRef != null) {
                        number_id++;
                    }
//                    Iterator<FeatureList> featureListIter = um.unmarshalCollectionFromXpath(MzQuantMLElement.FeatureList);
//LinkedList lList = new LinkedList(featureListIter);
//                    Iterator<FeatureList> featureListIter = feaListIter;
                    for (FeatureList featureList : featureLists) {
//                    while (featureListIter.hasNext()) {
//                        FeatureList featureList = featureListIter.next();
//                        String rawFileGroupRef = featureList.getRawFilesGroupRef();
                        String featureListId = featureList.getId();
                        String featureListNo = null;
                        if (featureListId.length() == 7) {
                            featureListNo = featureListId.substring(6, 7);
                        } else if (featureListId.length() == 8) {
                            featureListNo = featureListId.substring(6, 8);
                        }

                        int number = Integer.valueOf(featureListNo);
                        List<Feature> features = featureList.getFeature();
                        if (featureListNo.equalsIgnoreCase(assNo)) {
                            for (Feature feature : features) {
                                featureCharge = feature.getCharge();
                                featureMz = feature.getMz();

                                featureRt = Double.valueOf(feature.getRt());
                                featureId = feature.getId();
//                                System.out.println("feature Ref: " + featureRef);
//                                    System.out.println("feature ID: " + featureId);
                                if (featureId.equalsIgnoreCase(featureRef)) {
                                    mzArray[number] = featureMz;
//                                    System.out.println("Rt: " + featureRt);
//                                    System.out.println("Mz: " + featureMz);
                                    rtArray[number] = featureRt;
                                    chargeArray[number] = featureCharge;
//                            idArray[Integer.valueOf(featureListNo)] = featureId;
                                    mzmean = mzmean + mzArray[number];
                                    rtmean = rtmean + rtArray[number];
                                    signal = 1;

                                    number_valid = number_valid + 1;
                                    break;

                                }
                            }
                            if (signal == 1) {
//                                featureListIter.remove();
                                break;
                            }
                        }
                    }
                }
//System.out.println("identification no.: " + number_id);
                mzmean = mzmean / number_valid;
                rtmean = rtmean / number_valid;

                sbFeature.append(pepConId);
                sbFeature.append(SEPARATOR);
                sbFeature.append(sequence);
                sbFeature.append(SEPARATOR);
                sbFeature.append(mzmean);
                sbFeature.append(SEPARATOR);
                sbFeature.append(rtmean);
                sbFeature.append(SEPARATOR);
                sbFeature.append(number_valid);
                sbFeature.append(SEPARATOR);
                sbFeature.append(number_id);
                sbFeature.append(SEPARATOR);
                sbFeature.append(pepCharge);
                sbFeature.append(SEPARATOR);

                for (int i = 0; i < assayNo; i++) {
                    sbFeature.append(mzArray[i]);
                    sbFeature.append(SEPARATOR);
                    sbFeature.append(rtArray[i]);
                    sbFeature.append(SEPARATOR);
                    sbFeature.append(chargeArray[i]);
                    sbFeature.append(SEPARATOR);
                }

                for (QuantLayer pepAssayQuantLayer : pepAssayQuantLayers) {
                    String aslAccession = pepAssayQuantLayer.getDataType().getCvParam().getAccession();
                    DataMatrix dm = pepAssayQuantLayer.getDataMatrix();
                    List<Row> rows = dm.getRow();
                    if (aslAccession.equalsIgnoreCase("MS:1001840")) {
                        for (Row row : rows) {
                            String objRef = row.getObjectRef();
                            List<String> values = row.getValue();
                            if (pepConId.equalsIgnoreCase(objRef)) {
                                for (String value : values) {
                                    sbFeature.append(value);
                                    sbFeature.append(SEPARATOR);
                                }
                                break;
                            }
                        }
                        break;
                    }

                }

//                k = k + 1;
                sbFeature.append(pepConProtein);
                sbFeature.append(SEPARATOR);
                sbFeature.append("\n");
//                if ((k % 10000) == 0) {
//                    System.out.println("feature number is over: " + k);
////                    timer.cancel();
////                    System.out.println("TimerTask cancelled! :" + new Date());
////                    break;
//                }

            }

        }

        ///////////////////////////////////////////////////////////////
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
            out.append(sbFeature.toString());
            out.close();
        } catch (IOException e) {
            System.out.println("Problems while closing file " + outputFile + "!\n" + e);
        }

    }
}
