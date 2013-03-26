package uk.ac.cranfield.mzqlib.converter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.cranfield.mzqlib.MzqLib;
import uk.ac.cranfield.mzqlib.Utils;
import uk.ac.cranfield.mzqlib.data.FeatureData;
import uk.ac.cranfield.mzqlib.data.MzqData;
import uk.ac.cranfield.mzqlib.data.PeptideData;
import uk.ac.cranfield.mzqlib.data.ProteinData;
import uk.ac.ebi.pride.jmztab.MzTabFile;
import uk.ac.ebi.pride.jmztab.MzTabParsingException;
import uk.ac.ebi.pride.jmztab.model.Modification;
import uk.ac.ebi.pride.jmztab.model.Param;
import uk.ac.ebi.pride.jmztab.model.Peptide;
import uk.ac.ebi.pride.jmztab.model.Protein;
import uk.ac.ebi.pride.jmztab.model.Subsample;
import uk.ac.ebi.pride.jmztab.model.Unit;
import uk.ac.liv.jmzqml.model.mzqml.Assay;
import uk.ac.liv.jmzqml.model.mzqml.Cv;
import uk.ac.liv.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.jmzqml.model.mzqml.ParamList;
import uk.ac.liv.jmzqml.model.mzqml.Software;

/**
 *
 * @author Jun Fan@cranfield
 */
public class MztabConverter extends GenericConverter {

    public MztabConverter(String filename, String outputFile) {
        super(filename, outputFile);
    }

    @Override
    public void convert() {
        try {
            BufferedWriter out = null;
            if (outfile.length() == 0) {
                outfile = getBaseFilename() + ".mztab";
            }
            MzTabFile mztab = new MzTabFile();
            Unit unit = new Unit();
            String unitID = MzqLib.data.getMzqID().replace("-", "_");
            unit.setUnitId(unitID);
            unit.setDescription(MzqLib.data.getMzqName());
            final ArrayList<Assay> assays = MzqLib.data.getAssays();
//            final ArrayList<String> assays = MzqLib.data.getAssayIDs();
            for (int i = 0; i < assays.size(); i++) {
                Assay assay = assays.get(i);
                Subsample sub = new Subsample(unitID, i + 1);
                sub.setDescription(assay.getName());
                unit.setSubsample(sub);
            }
            ArrayList<Param> softwares = new ArrayList<Param>();
            for (Software software : MzqLib.data.getSoftwareList().getSoftware()) {
                for (CvParam cvParam : software.getCvParam()) {
                    Param param = Utils.convertMztabParam(cvParam);
                    if (param != null) {
                        softwares.add(param);
                    }
                }
            }
            unit.setSoftware(softwares);
            ArrayList<String> names = MzqLib.data.getQuantitationNames();
            CvParam proCvParam = determineQuantitationUnit(MzqData.PROTEIN, names);
            CvParam pepCvParam = determineQuantitationUnit(MzqData.PEPTIDE, names);
            if (proCvParam != null) {
                unit.setProteinQuantificationUnit(Utils.convertMztabParam(proCvParam));
            }
            if (pepCvParam != null) {
                unit.setPeptideQuantificationUnit(Utils.convertMztabParam(pepCvParam));
            }
            unit.setQuantificationMethod(determineQuantitationMethod(MzqLib.data.getAnalysisSummary()));
            mztab.setUnit(unit);
            String searchEngineStr = getSearchEngineString();
            Param searchEngine = null;
            String searchEngineScoreAccession = null;
            String searchEngineScoreName = null;
            String cvID = null;
            if (searchEngineStr != null) {
                String[] elements = searchEngineStr.split(";");
                cvID = elements[0];
                searchEngine = new Param(cvID, elements[3], elements[4], null);
                searchEngineScoreAccession = elements[1];
                searchEngineScoreName = elements[2];
            }
            for (ProteinData protein : MzqLib.data.getProteins()) {
                Protein tabProt = new Protein();
                tabProt.setAccession(protein.getAccession());
                tabProt.setUnitId(unitID);
                final String searchDatabase = protein.getSearchDatabase();
                tabProt.setDatabase(searchDatabase);
                final String searchDatabaseVersion = protein.getSearchDatabaseVersion();
                tabProt.setDatabaseVersion(searchDatabaseVersion);
                if (proCvParam != null) {
                    for (int i = 0; i < assays.size(); i++) {
                        String assay = assays.get(i).getId();
                        tabProt.setAbundance(i + 1, protein.getQuantity(proCvParam.getName(), assay), Double.NaN, Double.NaN);
                    }
                }
                mztab.addProtein(tabProt);

                for (PeptideData peptide : protein.getPeptides()) {
                    for (int charge : peptide.getCharges()) {
                        Peptide tabPep = new Peptide();
                        tabPep.setSequence(peptide.getSeq());
                        tabPep.setAccession(protein.getAccession());
                        tabPep.setDatabase(searchDatabase);
                        tabPep.setDatabaseVersion(searchDatabaseVersion);
                        if (searchEngine != null) {
                            tabPep.setSearchEngine(searchEngine);
                            uk.ac.ebi.pride.jmztab.model.ParamList paramList = new uk.ac.ebi.pride.jmztab.model.ParamList();
                            paramList.add(new Param(cvID, searchEngineScoreAccession, searchEngineScoreName, String.valueOf(peptide.getGlobal(searchEngineScoreName))));
                            tabPep.setSearchEngineScore(paramList);
                        }
                        final List<uk.ac.liv.jmzqml.model.mzqml.Modification> modifications = peptide.getPeptide().getModification();
                        if(!modifications.isEmpty()){
                            ArrayList<Modification> mods = new ArrayList<Modification>();
                            for (int i = 0; i < modifications.size(); i++) {
                                uk.ac.liv.jmzqml.model.mzqml.Modification modification = modifications.get(i);
                                //<xsd:element name="cvParam" type="CVParamType" minOccurs="1"
                                Modification mod = new Modification(modification.getCvParam().get(0).getAccession(), modification.getLocation());
                                mods.add(mod);
                            }
                            tabPep.setModification(mods);
                        }
                        tabPep.setCharge(charge);
                        ArrayList<Double> mzs = new ArrayList<Double>();
                        ArrayList<Double> rts = new ArrayList<Double>();
                        for(FeatureData feature:peptide.getFeaturesWithCharge(charge)){
                            //<xsd:attribute name="mz" type="xsd:double" use="required">
                            mzs.add(feature.getFeature().getMz());
                            //<xsd:attribute name="rt" type="doubleOrNullType" use="required">
                            //on the retention time axis in minutes
                            //in mzTab retention_time Double List (“,”) Time points in seconds.
                            String rtStr = feature.getFeature().getRt();
                            if(rtStr!=null && !rtStr.equalsIgnoreCase("null")){
                                double rt = 60*Double.parseDouble(rtStr);
                                rts.add(rt);
                            }
                        }
                        tabPep.setMassToCharge(Utils.mean(mzs));
                        tabPep.setRetentionTime(rts);
                        if (pepCvParam != null) {
                            for (int i = 0; i < assays.size(); i++) {
                                String assay = assays.get(i).getId();
                                tabPep.setAbundance(i + 1, peptide.getQuantity(pepCvParam.getName(), assay), Double.NaN, Double.NaN);
                            }
                        }
                        mztab.addPeptide(tabPep);
                    }
                }
            }


            out = new BufferedWriter(new FileWriter(outfile));
            out.append(mztab.toMzTab());
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(MztabConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MzTabParsingException ex) {
            Logger.getLogger(MztabConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private CvParam determineQuantitationUnit(int level, ArrayList<String> names) {
        for (String name : names) {
            if (MzqLib.data.control.isRequired(level, MzqData.ASSAY, name)) {
                return MzqLib.data.getQuantitationCvParam(name);
            }
        }
        return null;
    }

    private Param determineQuantitationMethod(ParamList analysisSummary) {
        for (CvParam cvParam : analysisSummary.getCvParam()) {
            String accession = cvParam.getAccession();
            if (accession.contains("MS:1001834")
                    || accession.contains("MS:1001836")
                    || accession.contains("MS:1002023")
                    || accession.contains("MS:1002018")) {
                return Utils.convertMztabParam(cvParam);

            }
        }
        return null;
    }

    private String getSearchEngineString() {
        for (String name : MzqLib.data.control.getElements(MzqData.PEPTIDE, MzqData.GLOBAL)) {
            CvParam param = MzqLib.data.getQuantitationCvParam(name);
            String value = ((Cv) param.getCvRef()).getId();
            if (param != null) {
                String accession = param.getAccession();
                if (accession.contains("MS:1001171")) {
                    return value + ";MS:1001171;Mascot:score;MS:1001207;Mascot";
                }
                if (accession.contains("MS:1001390")) {
                    return value + ";MS:1001390;Phenyx:Score;MS:1001209;Phenyx";
                }
                if (accession.contains("MS:1001492")) {
                    return value + ";MS:1001492;percolator:score;MS:1001490;percolator";
                }
            }
        }
        return null;
    }
}
