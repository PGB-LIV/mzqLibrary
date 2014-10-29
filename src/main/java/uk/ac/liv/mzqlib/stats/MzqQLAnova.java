package uk.ac.liv.mzqlib.stats;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import uk.ac.liv.jmzqml.MzQuantMLElement;
import uk.ac.liv.jmzqml.model.mzqml.AnalysisSummary;
import uk.ac.liv.jmzqml.model.mzqml.AssayList;
import uk.ac.liv.jmzqml.model.mzqml.AuditCollection;
import uk.ac.liv.jmzqml.model.mzqml.BibliographicReference;
import uk.ac.liv.jmzqml.model.mzqml.Column;
import uk.ac.liv.jmzqml.model.mzqml.ColumnDefinition;
import uk.ac.liv.jmzqml.model.mzqml.CvList;
import uk.ac.liv.jmzqml.model.mzqml.CvParamRef;
import uk.ac.liv.jmzqml.model.mzqml.DataMatrix;
import uk.ac.liv.jmzqml.model.mzqml.DataProcessingList;
import uk.ac.liv.jmzqml.model.mzqml.FeatureList;
import uk.ac.liv.jmzqml.model.mzqml.GlobalQuantLayer;
import uk.ac.liv.jmzqml.model.mzqml.IdOnly;
import uk.ac.liv.jmzqml.model.mzqml.InputFiles;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.jmzqml.model.mzqml.ProteinGroupList;
import uk.ac.liv.jmzqml.model.mzqml.ProteinList;
import uk.ac.liv.jmzqml.model.mzqml.QuantLayer;
import uk.ac.liv.jmzqml.model.mzqml.Row;
import uk.ac.liv.jmzqml.model.mzqml.SmallMoleculeList;
import uk.ac.liv.jmzqml.model.mzqml.SoftwareList;
import uk.ac.liv.jmzqml.model.mzqml.StudyVariableList;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLMarshaller;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;

/**
 *
 * @author Da Qi
 * @institute University of Liverpool
 * @time 23-Oct-2014 11:32:27
 */
public class MzqQLAnova {

    private final MzQuantMLUnmarshaller mzqUm;
    private final String listType;
    private final List<List<String>> assayIDsGroup;
    private final String qlDataType;

    /**
     * Constructor
     *
     * @param um            the input MzQuantMLUnmarshaller
     * @param listType      type of the list, e.g. "ProteinGroup", "Protein", or "Peptide"
     * @param assayIDsGroup list of assay list to be in ANOVA calculation
     * @param qlDataType    CV accession identifying data type of QuantLayer
     */
    public MzqQLAnova(MzQuantMLUnmarshaller um, String listType,
                      List<List<String>> assayIDsGroup,
                      String qlDataType) {
        this.mzqUm = um;
        this.assayIDsGroup = assayIDsGroup;
        this.listType = listType;
        this.qlDataType = qlDataType;
    }

    /**
     * Constructor
     *
     * @param mzqFile       the input mzQuantML file
     * @param listType      type of the list, e.g. "ProteinGroup", "Protein", or "Peptide"
     * @param assayIDsGroup list of assay list to be in ANOVA calculation
     * @param qlDataType    CV accession identifying data type of QuantLayer
     */
    public MzqQLAnova(File mzqFile, String listType,
                      List<List<String>> assayIDsGroup,
                      String qlDataType) {
        this(new MzQuantMLUnmarshaller(mzqFile), listType, assayIDsGroup, qlDataType);
    }

    /**
     * Constructor
     *
     * @param mzqFileName   the input mzQuantML file name
     * @param listType      type of the list, e.g. "ProteinGroup", "Protein", or "Peptide"
     * @param assayIDsGroup list of assay list to be in ANOVA calculation
     * @param qlDataType    CV accession identifying data type of QuantLayer
     */
    public MzqQLAnova(String mzqFileName, String listType,
                      List<List<String>> assayIDsGroup,
                      String qlDataType) {
        this(new File(mzqFileName), listType, assayIDsGroup, qlDataType);
    }

    /**
     * Constructor
     *
     * @param um                  the input MzQuantMLUnmarshaller
     * @param listType            type of the list, e.g. "ProteinGroup", "Protein", or "Peptide"
     * @param assayIDsGroupString flat string of assay ids group to be in ANOVA calculation
     * @param qlDataType          CV accession identifying data type of QuantLayer
     */
    public MzqQLAnova(MzQuantMLUnmarshaller um, String listType,
                      String assayIDsGroupString, String qlDataType) {
        this(um, listType, assayIDsGroupConverter(assayIDsGroupString), qlDataType);
    }

    /**
     * Constructor
     *
     * @param mzqFile             the input mzQuantML file
     * @param listType            type of the list, e.g. "ProteinGroup", "Protein", or "Peptide"
     * @param assayIDsGroupString flat string of assay ids group to be in ANOVA calculation
     * @param qlDataType          CV accession identifying data type of QuantLayer
     */
    public MzqQLAnova(File mzqFile, String listType,
                      String assayIDsGroupString,
                      String qlDataType) {
        this(new MzQuantMLUnmarshaller(mzqFile), listType, assayIDsGroupConverter(assayIDsGroupString), qlDataType);
    }

    /**
     * Constructor
     *
     * @param mzqFileName         the input mzQuantML file name
     * @param listType            type of the list, e.g. "ProteinGroup", "Protein", or "Peptide"
     * @param assayIDsGroupString flat string of assay ids group to be in ANOVA calculation
     * @param qlDataType          CV accession identifying data type of QuantLayer
     */
    public MzqQLAnova(String mzqFileName, String listType,
                      String assayIDsGroupString,
                      String qlDataType) {
        this(new File(mzqFileName), listType, assayIDsGroupConverter(assayIDsGroupString), qlDataType);
    }

    /**
     *
     * @param outputFileName
     *
     * @throws JAXBException
     */
    public void writeMzQuantMLFile(String outputFileName)
            throws JAXBException {
        // retrieve every attributes and elements from the mzQuantML file
        String mzqId = mzqUm.getMzQuantMLId();

        // three ways of unmarshalling an mzQuantML element: 
        CvList cvList = mzqUm.unmarshal(MzQuantMLElement.CvList);
        AuditCollection ac = mzqUm.unmarshal(MzQuantMLElement.AuditCollection);
        AnalysisSummary as = mzqUm.unmarshal(MzQuantMLElement.AnalysisSummary);
        InputFiles inputFiles = mzqUm.unmarshal(MzQuantMLElement.InputFiles);
        SoftwareList softList = mzqUm.unmarshal(MzQuantMLElement.SoftwareList);
        DataProcessingList dpList = mzqUm.unmarshal(MzQuantMLElement.DataProcessingList);
        Iterator<BibliographicReference> brIter
                = mzqUm.unmarshalCollectionFromXpath(MzQuantMLElement.BibliographicReference);
        AssayList assayList = mzqUm.unmarshal(MzQuantMLElement.AssayList);
        StudyVariableList svList = mzqUm.unmarshal(MzQuantMLElement.StudyVariableList);
        ProteinGroupList protGrpList = mzqUm.unmarshal(MzQuantMLElement.ProteinGroupList);
        ProteinList protList = mzqUm.unmarshal(MzQuantMLElement.ProteinList);
        Iterator<PeptideConsensusList> pepConListIter
                = mzqUm.unmarshalCollectionFromXpath(MzQuantMLElement.PeptideConsensusList);
        Iterator<FeatureList> ftListIter = mzqUm.unmarshalCollectionFromXpath(MzQuantMLElement.FeatureList);
        SmallMoleculeList smallMolList = mzqUm.unmarshal(MzQuantMLElement.SmallMoleculeList);

        // Add GlobaQuantLayer to designated list
        switch (listType) {
            case "ProteinGroup":
                if (protGrpList != null) {
                    protGrpList.getGlobalQuantLayer().add(getAnovaGlobalQuantLayer());
                }
                else {
                    throw new RuntimeException("There is no ProteinGroupList in the input mzQuantML file.");
                }
                break;
            case "Protein":
                if (protList != null) {
                    protList.getGlobalQuantLayer().add(getAnovaGlobalQuantLayer());
                }
                else {
                    throw new RuntimeException("There is no ProteinList in the input mzQuantML file.");
                }
                break;
            default:
                //TODO: do nothing or throw exception?
                throw new RuntimeException("Please provide valid name of list type such as \"ProtienGroup\" or \"Protein\".");
        }

        FileWriter writer = null;
        try {
            MzQuantMLMarshaller mzqMsh = new MzQuantMLMarshaller();
            writer = new FileWriter(outputFileName);

            // XML header
            writer.write(MzQuantMLMarshaller.createXmlHeader() + "\n");

            // mzQuantML start tag
            writer.write(MzQuantMLMarshaller.createMzQuantMLStartTag(mzqId) + "\n");

            if (cvList != null) {
                mzqMsh.marshall(cvList, writer);
                writer.write("\n");
            }
            if (ac != null) {
                mzqMsh.marshall(ac, writer);
                writer.write("\n");
            }
            if (as != null) {
                mzqMsh.marshall(as, writer);
                writer.write("\n");
            }
            if (inputFiles != null) {
                mzqMsh.marshall(inputFiles, writer);
                writer.write("\n");
            }
            if (softList != null) {
                mzqMsh.marshall(softList, writer);
                writer.write("\n");
            }
            if (dpList != null) {
                mzqMsh.marshall(dpList, writer);
                writer.write("\n");
            }
            if (brIter != null) {
                while (brIter.hasNext()) {
                    BibliographicReference bibRef = brIter.next();
                    mzqMsh.marshall(bibRef, writer);
                    writer.write("\n");
                }
            }
            if (assayList != null) {
                mzqMsh.marshall(assayList, writer);
                writer.write("\n");
            }
            if (svList != null) {
                mzqMsh.marshall(svList, writer);
                writer.write("\n");
            }
            if (protGrpList != null) {
                mzqMsh.marshall(protGrpList, writer);
                writer.write("\n");
            }
            if (protList != null) {
                mzqMsh.marshall(protList, writer);
                writer.write("\n");
            }
            if (pepConListIter != null) {
                while (pepConListIter.hasNext()) {
                    PeptideConsensusList pepConList = pepConListIter.next();
                    mzqMsh.marshall(pepConList, writer);
                    writer.write("\n");
                }
            }
            if (ftListIter != null) {
                while (ftListIter.hasNext()) {
                    FeatureList ftList = ftListIter.next();
                    mzqMsh.marshall(ftList, writer);
                    writer.write("\n");
                }
            }
            if (smallMolList != null) {
                mzqMsh.marshall(smallMolList, writer);
                writer.write("\n");
            }

            writer.write(MzQuantMLMarshaller.createMzQuantMLClosingTag());

        }
        catch (IOException ex) {
            Logger.getLogger(MzqQLAnova.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            try {
                writer.close();
            }
            catch (IOException ex) {
                Logger.getLogger(MzqQLAnova.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     *
     * @return GlobalQuantLayer for the anova p-value result
     *
     * @throws JAXBException
     */
    public GlobalQuantLayer getAnovaGlobalQuantLayer()
            throws JAXBException {

        GlobalQuantLayer globalQL = new GlobalQuantLayer();

        ColumnDefinition colDef = new ColumnDefinition();
        Column col = new Column();
        CvParamRef cpRef = new CvParamRef();
        cpRef.setCvParam(MzQuantMLMarshaller.createCvParam("ANOVA p-value", "PSI-MS", "MS:1001854"));
        col.setDataType(cpRef);
        col.setIndex(BigInteger.ZERO);
        colDef.getColumn().add(col);
        globalQL.setColumnDefinition(colDef);

        DataMatrix globalQLDataMatrix = new DataMatrix();
        TObjectDoubleMap<String> anovaPValueMap = getAnovaPValueMap();
        //globalQLDataMatrix.getRow();
        anovaPValueMap.forEachKey((String objectRef) -> {
            Row row = new Row();
            row.setObjectRef(objectRef);
            row.getValue().add(String.valueOf(anovaPValueMap.get(objectRef)));
            globalQLDataMatrix.getRow().add(row);
            return true;
        });
        globalQL.setDataMatrix(globalQLDataMatrix);

        return globalQL;
    }

    /**
     * Get Anova p-value map from the existing settings from the constructor
     *
     * @return map of object reference from each row to the calculated p-value (doulbe)
     *
     * @throws JAXBException
     */
    public TObjectDoubleMap<String> getAnovaPValueMap()
            throws JAXBException {
        TObjectDoubleMap<String> anovaPValueMap = new TObjectDoubleHashMap<>();
        boolean dataTypeFound = false;
        switch (listType) {
            case "ProteinGroup":
                ProteinGroupList protGrpList = this.mzqUm.unmarshal(MzQuantMLElement.ProteinGroupList);
                if (protGrpList != null) {
                    List<QuantLayer<IdOnly>> assayQLs = protGrpList.getAssayQuantLayer();

                    for (QuantLayer<IdOnly> assayQL : assayQLs) {
                        if (assayQL.getDataType().getCvParam().getAccession().equals(qlDataType)) {
                            dataTypeFound = true;
                            anovaPValueMap = CalculatePValueFromQL(assayQL);
                        }
                    }
                    if (!dataTypeFound) {
                        throw new RuntimeException("Cannot find the QuantLayer with data type of: " + qlDataType);
                    }
                }
                else {
                    throw new RuntimeException("There is no ProteinGroupList in the input mzQuantML file.");
                }
                break;
            case "Protein":
                ProteinList protList = this.mzqUm.unmarshal(MzQuantMLElement.ProteinList);
                if (protList != null) {
                    List<QuantLayer<IdOnly>> assayQLs = protList.getAssayQuantLayer();

                    for (QuantLayer<IdOnly> assayQL : assayQLs) {
                        if (assayQL.getDataType().getCvParam().getName().equals(qlDataType)) {
                            dataTypeFound = true;
                            anovaPValueMap = CalculatePValueFromQL(assayQL);
                        }
                    }
                    if (!dataTypeFound) {
                        throw new RuntimeException("Cannot find the QuantLayer with data type of: " + qlDataType);
                    }
                }
                else {
                    throw new RuntimeException("There is no ProteinList in the input mzQuantML file.");
                }
                break;
            case "Peptide":
                //TODO: do we need to calculate ANOVA on peptide level?
                break;
            default:
                //TODO: do nothing or throw exception?
                throw new RuntimeException("Please provide valid name of list type such as \"ProtienGroup\" or \"Protein\".");
        }

        return anovaPValueMap;
    }

    /**
     * Calculate the Anova p-value based on the existing setting from a single QuantLayer
     *
     * @param assayQL the QuantLayer (AssayQuantLayer) to be used for Anova p-value calculation
     *
     * @return map of object reference from each row to the calculated p-value (doulbe)
     */
    private TObjectDoubleMap<String> CalculatePValueFromQL(
            QuantLayer<IdOnly> assayQL) {

        TObjectDoubleMap<String> ret = new TObjectDoubleHashMap<>();

        // Transform the list of assay id list into a list of position list
        List<TIntList> assayPosListList = new ArrayList<>();

        for (List<String> assayIDs : assayIDsGroup) {
            TIntList assayPosList = new TIntArrayList();
            for (String assayID : assayIDs) {
                if (assayQL.getColumnIndex().indexOf(assayID) != -1) {
                    assayPosList.add(assayQL.getColumnIndex().indexOf(assayID));
                }
                else {
                    throw new RuntimeException("Cannot find " + assayID + " in ColumnIndex.");
                }
            }
            assayPosListList.add(assayPosList);
        }

        // Get each protein group row
        DataMatrix protGrpAQLDataMatrix = assayQL.getDataMatrix();
        for (Row row : protGrpAQLDataMatrix.getRow()) {
            List<String> valueList = row.getValue();
            List<double[]> doubleArrayList = new ArrayList<>();

            // Get doulbeArrayList for ANOVA calculation
            for (TIntList posList : assayPosListList) {
                TDoubleList doubleList = new TDoubleArrayList();

                posList.forEach((int pos) -> {
                    String valueString = valueList.get(pos);
                    if (NumberUtils.isNumber(valueString)) {
                        double valueD = Double.parseDouble(valueString);
                        // Do log on valueD
                        if (valueD == 0) {
                            valueD = 0.5;
                        }
                        doubleList.add(Math.log10(valueD));
                    }
                    else {
                        throw new RuntimeException(valueString + " is not a number.");
                    }

                    return true;
                });
                double[] doubleArray = doubleList.toArray();
                doubleArrayList.add(doubleArray);
            }

            OneWayAnova owa = new OneWayAnova();
            double pValue = owa.anovaPValue(doubleArrayList);

            // Add pValue to anovaPValueMap
            ret.put(row.getObjectRef(), pValue);

            //System.out.println("Ojbect_Ref: " + row.getObjectRef() + " --> " + pValue);
        }

        return ret;
    }

    /**
     * Convert input flat assay ids group into list of assay id list.
     * The whole string is divided into groups which are separated by ";" (semicolon)
     * and in each group, the member assay ids are separated by "," (comma).
     *
     * @param assayIDsGroupString the input string of assay ids groups
     *
     * @return the list of assay id list
     */
    private static List<List<String>> assayIDsGroupConverter(
            String assayIDsGroupString) {
        List<List<String>> ret = new ArrayList();
        String[] assayIDsGroupArray = assayIDsGroupString.split(";");

        for (String assayIDsString : assayIDsGroupArray) {
            String[] assayIDsArray = assayIDsString.split(",");
            List<String> assayIDsList = Arrays.asList(assayIDsArray);
            ret.add(assayIDsList);
        }
        return ret;
    }

    // test example
//    public static void main(String[] args)
//            throws JAXBException {
//        String inputFileName = "D:\\Users\\ddq\\Documents\\NetBeansProjects\\mzq-lib\\src\\main\\resources\\PipelinesOutcomePuf3\\Progenesis\\fdr001_12Aug2014.mzq";
//        List<String> aList = new ArrayList();
//        aList.add("ass_0"); // ass_1 ass_2 ass_3 ass_4");
//        aList.add("ass_1");
//        aList.add("ass_2");
//        aList.add("ass_3");
//        aList.add("ass_4");
//
//        List<String> bList = new ArrayList();
//        bList.add("ass_5");
//        bList.add("ass_6");
//        bList.add("ass_7");
//        bList.add("ass_8");
//        bList.add("ass_9");
//
//        List<List<String>> assayIdsGroup = new ArrayList<>();
//        assayIdsGroup.add(aList);
//        assayIdsGroup.add(bList);
//
//        String listType = "ProteinGroup";
//        String qlDataType = "MS:1002518";
//
//        MzqQLAnova mzqQLAnova = new MzqQLAnova(inputFileName, listType, assayIdsGroup, qlDataType);
//
//        //mzqQLAnova.getAnovaPValueMap();
//        mzqQLAnova.writeMzQuantMLFile(inputFileName.replace(".mzq", "_new.mzq"));
//    }

}
