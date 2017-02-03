
package uk.ac.cranfield.mzqlib.converter;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.cranfield.mzqlib.MzqLib;
import uk.ac.cranfield.mzqlib.Utils;
import uk.ac.cranfield.mzqlib.data.FeatureData;
import uk.ac.cranfield.mzqlib.data.MzqData;
import uk.ac.cranfield.mzqlib.data.PeptideData;
import uk.ac.cranfield.mzqlib.data.ProteinData;
import uk.ac.cranfield.mzqlib.data.ProteinGroupData;
import uk.ac.cranfield.mzqlib.data.QuantitationLevel;
import uk.ac.ebi.pride.jmztab.model.AssayQuantificationMod;
import uk.ac.ebi.pride.jmztab.model.CV;
import uk.ac.ebi.pride.jmztab.model.CVParam;
import uk.ac.ebi.pride.jmztab.model.MZTabColumnFactory;
import uk.ac.ebi.pride.jmztab.model.MZTabDescription;
import uk.ac.ebi.pride.jmztab.model.Metadata;
import uk.ac.ebi.pride.jmztab.model.Modification;
import uk.ac.ebi.pride.jmztab.model.Modification.Type;
import uk.ac.ebi.pride.jmztab.model.MsRun;
import uk.ac.ebi.pride.jmztab.model.Param;
import uk.ac.ebi.pride.jmztab.model.Peptide;
import uk.ac.ebi.pride.jmztab.model.PeptideColumn;
import uk.ac.ebi.pride.jmztab.model.Protein;
import uk.ac.ebi.pride.jmztab.model.ProteinColumn;
import uk.ac.ebi.pride.jmztab.model.Section;
import uk.ac.ebi.pride.jmztab.model.SpectraRef;
import uk.ac.ebi.pride.jmztab.model.SplitList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.AnalysisSummary;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Assay;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Cv;
import uk.ac.liv.pgb.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.pgb.jmzqml.model.mzqml.ModParam;
import uk.ac.liv.pgb.jmzqml.model.mzqml.RawFile;
import uk.ac.liv.pgb.jmzqml.model.mzqml.RawFilesGroup;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Software;
import uk.ac.liv.pgb.jmzqml.model.mzqml.StudyVariable;

/**
 * MztabConverter is to convert mzq file to mzTab file.
 *
 * @author Jun Fan@qmul
 */
public class MztabConverter extends GenericConverter {

    /**
     * Number of seconds in one minute.
     */
    private final int SECONDS_PER_MINUTE = 60;

    /**
     * Constructor.
     *
     * @param filename   input mzq file name.
     * @param outputFile output mzTab file name.
     */
    public MztabConverter(final String filename, final String outputFile) {
        super(filename, outputFile);
    }

    /**
     * Convert method. Convert mzq file to mzTab file.
     * Override the method in GenericConverter.
     */
    @Override
    public void convert() {
        BufferedWriter out = null;
        try {
            if (outfile.length() == 0) {
                outfile = getBaseFilename() + ".mztab";
            }
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outfile), "UTF-8"));
//            Decide if we are working with protein groups (PG) or proteins:
//            Easiest done by checking if there any QLs for PG – if yes, we are in PG mode
//            Retrieve all the Protein accessions for each protein within each PG. For now, we arbitrarily select the first protein accession as the group leader, the rest are reported as ambiguity members separated by semi-colons I think:
//            This part will be improved later when we add CV terms to mzq describing which protein is group leader and which are same or sub-sets etc
            int proteinLevel = MzqData.PROTEIN;
            for (String quantityName : MzqLib.DATA.getQuantitationNames()) {
                if (MzqLib.DATA.control.isRequired(MzqData.PROTEIN_GROUP,
                                                   MzqData.ASSAY, quantityName)
                        || MzqLib.DATA.control.isRequired(MzqData.PROTEIN_GROUP,
                                                          MzqData.SV,
                                                          quantityName)) {
                    proteinLevel = MzqData.PROTEIN_GROUP;
                    break;
                }
            }
            if (MzqLib.DATA.control.isRequired(MzqData.PROTEIN_GROUP,
                                               MzqData.RATIO,
                                               MzqData.RATIO_STRING)) {
                proteinLevel = MzqData.PROTEIN_GROUP;
            }
            if (!MzqLib.DATA.control.getElements(MzqData.PROTEIN_GROUP,
                                                 MzqData.GLOBAL).isEmpty()) {
                proteinLevel = MzqData.PROTEIN_GROUP;
            }

            //quantitation name retrieved from QuantLayer and GlobalQuantLayer
            List<String> names = MzqLib.DATA.getQuantitationNames();
            CvParam proCvParam;
            if (proteinLevel == MzqData.PROTEIN_GROUP) {
                proCvParam = determineQuantitationUnit(MzqData.PROTEIN_GROUP,
                                                       names);
            } else {
                proCvParam = determineQuantitationUnit(MzqData.PROTEIN, names);
            }
            CvParam pepCvParam = determineQuantitationUnit(MzqData.PEPTIDE,
                                                           names);
            List<String> ratioIDs = MzqLib.DATA.getRatios();
            //mandatory fields in all cases: mzTab-version, mzTab-mode, mzTab-type
            MZTabDescription tabDesc;
            //boolean isComplete;
            //convert from mzq file, so assume quantification type all the time
            if (proCvParam == null && pepCvParam == null) {
                tabDesc = new MZTabDescription(MZTabDescription.Mode.Summary,
                                               MZTabDescription.Type.Quantification);
                //isComplete = false;
            } else {
                tabDesc = new MZTabDescription(MZTabDescription.Mode.Complete,
                                               MZTabDescription.Type.Quantification);
                //isComplete = true;
            }
            tabDesc.setVersion("1.0.0");
            //optional fields in all cases: mzTab-ID, title
            tabDesc.setId(MzqLib.DATA.getMzqID().replace("-", "_"));
            Metadata mtd = new Metadata(tabDesc);
            //need to determine the CV name for MS: either PSI-MS or MS
            String msCVstr = "";
            boolean hasPRIDEcv = false;
            //cvs
            //optional fields in all cases: cv[1-n]-label, cv[1-n]-full_name, cv[1-n]-version, cv[1-n]-url
            final List<Cv> cvs = MzqLib.DATA.getCvList();
            for (int i = 0; i < cvs.size(); i++) {
                Cv cv = cvs.get(i);
                CV tabCv = new CV(i + 1);
                tabCv.setFullName(cv.getFullName());
                tabCv.setLabel(cv.getId());
                if (cv.getId().equalsIgnoreCase("PSI-MS") || cv.getId().
                        equalsIgnoreCase("MS")) {
                    msCVstr = cv.getId();
                }
                if (cv.getId().equalsIgnoreCase("PRIDE")) {
                    hasPRIDEcv = true;
                }
                tabCv.setVersion(cv.getVersion());
                tabCv.setUrl(cv.getUri());
                mtd.addCV(tabCv);
            }
            if (msCVstr.length() == 0) {
                CV tabCv = new CV(cvs.size() + 1);
                tabCv.setLabel("MS");
                tabCv.setFullName(
                        "Proteomics Standards Initiative Mass Spectrometry Vocabularies");
                msCVstr = "MS";
                tabCv.setUrl(
                        "http://psidev.cvs.sourceforge.net/viewvc/*checkout*/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo");
                mtd.addCV(tabCv);
            }

            //mandatory fields in all cases: description
            if (MzqLib.DATA.getMzqName() == null) {
                mtd.setDescription("Artificial name created by mzq-lib");
            } else {
                mtd.setDescription(MzqLib.DATA.getMzqName());
            }
            //optional fields in all cases: sample_processing[1-n], instrument[1-n]-name, instrument[1-n]-source
            //optional fields in all cases: instrument[1-n]-analyzer[1-n], instrument[1-n]-detector

            //mandatory in complete mode: software
            //SoftwareList [1..1] in mzq
            int swCount = 1;
            for (Software software : MzqLib.DATA.getSoftwareList().getSoftware()) {
                for (CvParam cvParam : software.getCvParam()) {
                    mtd.addSoftwareParam(swCount, Utils.convertMztabParam(
                                         cvParam));
                }
                swCount++;
            }
            //optional fields in all cases: software[1-n]-setting[1-n]

            //mandatory fields in all cases: protein_search_engine_score[1-n] if protein section exists
            //mandatory fields in all cases: peptide_search_engine_score[1-n] if peptide section exists
            //as this value comes from identification, mzQuantML normally does not contain such info. use meaningless term for now
            mtd.addProteinSearchEngineScoreParam(1, new CVParam(msCVstr,
                                                                "MS:1001153",
                                                                "search engine specific score",
                                                                null));
            mtd.addPeptideSearchEngineScoreParam(1, new CVParam(msCVstr,
                                                                "MS:1001153",
                                                                "search engine specific score",
                                                                null));

            //mandatory fields in all cases: psm_search_engine_score[1-n] if PSM section exists
            //mandatory fields in all cases: smallmolecule_search_engine_score[1-n] if small molecule section exists
            //both of them very very unlikely: psm from identification, and small molecule for metabolomics, so ignored here
            //optional fields in all cases: false_discovery_rate
            //identification related, skipped here
            //optional fields in all cases: publication[1-n]
            //publication is not dealt with at the moment, will do in the future, corresponding mzQuantML element
            //<xsd:element name="BibliographicReference" type="BibliographicReferenceType" minOccurs="0" maxOccurs="unbounded"/>
            //optional fields in all cases: contact[1-n]-name, contact[1-n]-affiliation, contact[1-n]-email
            //for future development
            //<xsd:element name="AuditCollection" type="AuditCollectionType" minOccurs="0" maxOccurs="1"/>
            //optional fields in all cases: uri[1-n],A URI pointing to the file's source data
            //not sure how to deal with it, skipped
            //mandatory fields in all cases: fixed_mod[1-n], variable_mod[1-n]
            //optional fields in all cases: fixed_mod[1-n]-site, fixed_mod[1-n]-position, variable_mod[1-n]-site, variable_mod[1-n]-position
            //If no fixed or variable modifications are reported, then the following CV parameters MUST be used:            }
            //MS:1002453 (No fixed modifications searched) MS:1002454 (No variable modifications searched)            //input files
            //eg.  mtd.addFixedModParam(1, new CVParam("UNIMOD", "UNIMOD:4", "Carbamidomethyl", null));
            //modifications searched for in the MS/MS search engine, so from identification side
            //this may be only be able to be predicted by checking modification from every detected peptideconsensus
            //therefore will be dealt with within peptide section later
            Map<CvParam, Integer> modCount = new HashMap<>();

            //mandatory field only in complete and quantitation mode: quantification_method
            mtd.setQuantificationMethod(determineQuantitationMethod(MzqLib.DATA.
                    getAnalysisSummary()));

            //mandatory field in quantitation type: protein-quantification_unit if protein section exists
            //mandatory field in quantitation type: peptide-quantification_unit if peptide section exists
            //mandatory field in quantitation type: small_molecule-quantification_unit if small molecule section exists
            if (proCvParam != null) {
                mtd.setProteinQuantificationUnit(Utils.convertMztabParam(
                        proCvParam));
            }
            if (pepCvParam != null) {
                mtd.setPeptideQuantificationUnit(Utils.convertMztabParam(
                        pepCvParam));
            }

            //raw files group
            //optional field in all cases: ms_run[1-n]-format
            //mandatory field in all cases: ms_run[1-n]-location
            //optional field in all cases: ms_run[1-n]-id_format, ms_run[1-n]-fragmentation_method
            //optional field in all cases: ms_run[1-n]-hash, ms_run[1-n]-hash_method, custom[1-n]
            final List<RawFilesGroup> rfgs = MzqLib.DATA.getInputFiles().
                    getRawFilesGroup();
            HashMap<String, Integer> msruns = new HashMap<>();
            for (int i = 0; i < rfgs.size(); i++) {
                RawFilesGroup rfg = rfgs.get(i);
                MsRun msrun = new MsRun(i + 1);
                List<RawFile> rawFileList = rfg.getRawFile();
                StringBuilder sb = new StringBuilder();
                sb.append("file://");
                for (RawFile raw : rawFileList) {
                    sb.append(raw.getLocation());
                    sb.append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                msrun.setLocation(new URL(sb.toString()));
                mtd.addMsRun(msrun);
                msruns.put(rfg.getId(), msrun.getId());
            }

            //optional field in all cases: sample[1-n]-species[1-n], sample[1-n]-tissue[1-n], sample[1-n]-cell_type[1-n]
            //optional field in all cases: sample[1-n]-disease[1-n], sample[1-n]-description, sample[1-n]-custom[1-n]
            //skipped for now
            //mandatory field in quantitation type: assay[1-n]-quantification_reagent if assays reported
            //optional field in all cases: assay[1-n]-quantification_mod[1-n], assay[1-n]-quantification_mod[1-n]-site, assay[1-n]-quantification_mod[1-n]-position
            //optional field in all cases: assay[1-n]-sample_ref
            //mandatory field in all cases: assay[1-n]-ms_run_ref if assays reported
            //all optional fields will not be considered here except assay[1-n]-quantification_mod[1-n] in the case of MS1 labeling method
            //to represent the reagent
            //AssayList is mandatory in mzq, therefore mandatory here
            String methodAccession = mtd.getQuantificationMethod().
                    getAccession();
            //assays
            final List<Assay> assays = MzqLib.DATA.getAssays();
            HashMap<String, uk.ac.ebi.pride.jmztab.model.Assay> assayMap
                    = new HashMap<>();
            ArrayList<uk.ac.ebi.pride.jmztab.model.Assay> tabAssays
                    = new ArrayList<>();
//            boolean isSILAC = false;
            CVParam ms1Light = new CVParam("PRIDE", "PRIDE:0000326",
                                           "SILAC light", null);
            CVParam ms1Heavy = new CVParam("PRIDE", "PRIDE:0000325",
                                           "SILAC heavy", null);
//            CVParam ms1Medium;
            if (methodAccession.equals("MS:1002018") && !hasPRIDEcv) {
                CV tabCv = new CV(cvs.size() + 1);
//                  tabCv.setFullName();
                tabCv.setLabel("PRIDE");
                tabCv.setVersion("1.2");
                tabCv.setUrl(
                        "https://code.google.com/p/ebi-pride/source/browse/trunk/pride-core/schema/pride_cv.obo");
                mtd.addCV(tabCv);
            }
            for (int i = 0; i < assays.size(); i++) {
                Assay assay = assays.get(i);
                uk.ac.ebi.pride.jmztab.model.Assay tabAssay
                        = new uk.ac.ebi.pride.jmztab.model.Assay(i + 1);
                if (methodAccession.equals("MS:1001834") || methodAccession.
                        equals("MS:1001836")) { //label free
                    tabAssay.setQuantificationReagent(
                            new CVParam(msCVstr,
                                        "MS:1002038",
                                        "unlabeled sample",
                                        null));
                } else if (methodAccession.equals("MS:1002018")) { //MS1 labeled
//                    for (ModParam mod : assay.getLabel().getModification()) {
                    ModParam mod = assay.getLabel().getModification().get(0);
                    String acc = mod.getCvParam().getAccession();
                    //simplify here, assuming silac light/heavy situation
                    if (acc.equalsIgnoreCase("MS:1002038")) {
                        tabAssay.setQuantificationReagent(ms1Light);
                    } else {
                        tabAssay.setQuantificationReagent(ms1Heavy);
                        int m = 1;
                        for (ModParam mod1 : assay.getLabel().getModification()) {
                            CvParam modParam = mod1.getCvParam();
                            final Param assayMod = Utils.convertMztabParam(
                                    modParam);
                            AssayQuantificationMod assayQuantMod
                                    = new AssayQuantificationMod(tabAssay, m);
                            assayQuantMod.setParam(assayMod);
                            tabAssay.addQuantificationMod(assayQuantMod);
                            m++;
                        }
                    }
                } else { //MS2 tag
                    for (ModParam mod : assay.getLabel().getModification()) {
                        CvParam modParam = mod.getCvParam();
                        final Param assayMod = Utils.convertMztabParam(modParam);
                        tabAssay.setQuantificationReagent(assayMod);
                        AssayQuantificationMod assayQuantMod
                                = new AssayQuantificationMod(tabAssay, 1);
                        assayQuantMod.setParam(assayMod);
                        tabAssay.addQuantificationMod(assayQuantMod);
                    }
                }
                tabAssay.setMsRun(mtd.getMsRunMap().get(msruns.get(assay.
                        getRawFilesGroupRef())));
                mtd.addAssay(tabAssay);
                tabAssays.add(tabAssay);
                assayMap.put(assay.getId(), tabAssay);
            }

            //study variables
            //mandatory field in quantitation type: study_variable[1-n]-assay_refs if assays and SVs reported
            //optional field in all cases: study_variable[1-n]-sample_refs
            //mandatory field in all cases: study_variable[1-n]-description if SVs reported
            //a fake SV will be created if no SVs reported in the mzq file
            final List<StudyVariable> svs = MzqLib.DATA.getSvs();
            ArrayList<uk.ac.ebi.pride.jmztab.model.StudyVariable> tabSvs
                    = new ArrayList<>();
            if (svs.isEmpty()) { //no study variable found in the mzq file, create the study variable
                //one for all assays
                uk.ac.ebi.pride.jmztab.model.StudyVariable tabSv
                        = new uk.ac.ebi.pride.jmztab.model.StudyVariable(1);
                tabSv.setDescription("manually created study variable");
                for (int i = 0; i < assays.size(); i++) {
                    Assay assay = assays.get(i);
                    tabSv.addAssay(assayMap.get(assay.getId()));
                }
                mtd.addStudyVariable(tabSv);
                tabSvs.add(tabSv);
            } else {
                for (int i = 0; i < svs.size(); i++) {
                    StudyVariable studyVariable = svs.get(i);
                    uk.ac.ebi.pride.jmztab.model.StudyVariable tabSv
                            = new uk.ac.ebi.pride.jmztab.model.StudyVariable(i
                                    + 1);
                    tabSv.setDescription(studyVariable.getName());
                    for (Assay assay : studyVariable.getAssays()) {
                        tabSv.addAssay(assayMap.get(assay.getId()));
                    }
                    mtd.addStudyVariable(tabSv);
                    tabSvs.add(tabSv);
                }
            }

            //CVs have been dealt with before to determine msCVstr and potentially add PRIDE CV
            //optional field in all cases: colunit-protein, colunit-peptide, colunit-psm, colunit-small_molecule
            //END OF METADATA
//            String searchEngineStr = getSearchEngineString();
//            Param searchEngine = null;
//            String searchEngineScoreAccession = null;
//            String searchEngineScoreName = null;
//            String cvID = null;
//            if (searchEngineStr != null) {
//                String[] elements = searchEngineStr.split(";");
//                cvID = elements[0];
//                searchEngine = new uk.ac.ebi.pride.jmztab.model.CVParam(cvID, elements[3], elements[4], null);
//                searchEngineScoreAccession = elements[1];
//                searchEngineScoreName = elements[2];
//            }
            //add mandatory columns in the complete and quantitation combination
            MZTabColumnFactory proFactory = MZTabColumnFactory.getInstance(
                    Section.Protein);
            MZTabColumnFactory pepFactory = MZTabColumnFactory.getInstance(
                    Section.Peptide);

            proFactory.addBestSearchEngineScoreOptionalColumn(
                    ProteinColumn.BEST_SEARCH_ENGINE_SCORE, 1);
            pepFactory.addBestSearchEngineScoreOptionalColumn(
                    PeptideColumn.BEST_SEARCH_ENGINE_SCORE, 1);
            for (int i = 0; i < mtd.getMsRunMap().size(); i++) {
                MsRun msrun = mtd.getMsRunMap().get(i + 1);
                proFactory.addSearchEngineScoreOptionalColumn(
                        ProteinColumn.SEARCH_ENGINE_SCORE, 1, msrun);
                pepFactory.addSearchEngineScoreOptionalColumn(
                        PeptideColumn.SEARCH_ENGINE_SCORE, 1, msrun);
            }

            //assay and sv are mandatory 
            for (uk.ac.ebi.pride.jmztab.model.Assay tabAssay : tabAssays) {
                proFactory.addAbundanceOptionalColumn(tabAssay);
            }
            for (uk.ac.ebi.pride.jmztab.model.StudyVariable sv : tabSvs) {
                proFactory.addAbundanceOptionalColumn(sv);
            }
            if (proCvParam != null) { //means no assay quant layer for protein, no need to add more
                String proCvName = proCvParam.getName();
                for (String quantName : names) {
                    if (quantName.equals(proCvName)) {
                        continue;
                    }
                    if (MzqLib.DATA.control.isRequired(proteinLevel,
                                                       MzqData.ASSAY, quantName)) {
                        for (uk.ac.ebi.pride.jmztab.model.Assay tabAssay
                                : tabAssays) {
                            proFactory.addOptionalColumn(tabAssay, quantName,
                                                         String.class);
                        }
                    }
                    if (MzqLib.DATA.control.isRequired(proteinLevel, MzqData.SV,
                                                       quantName)) {
                        for (uk.ac.ebi.pride.jmztab.model.StudyVariable sv
                                : tabSvs) {
                            proFactory.addOptionalColumn(sv, quantName,
                                                         String.class);
                        }
                    }
                }
                if (MzqLib.DATA.control.isRequired(proteinLevel, MzqData.RATIO,
                                                   MzqData.RATIO_STRING)) {
                    for (String ratioID : ratioIDs) {
                        proFactory.addOptionalColumn(ratioID, String.class);
                    }
                }
                final Set<String> elements = MzqLib.DATA.control.
                        getElements(proteinLevel, MzqData.GLOBAL);
                if (!elements.isEmpty()) {
                    for (String str : elements) {
                        proFactory.addOptionalColumn(str, Double.class);
                    }
                }
            }

            for (uk.ac.ebi.pride.jmztab.model.Assay tabAssay : tabAssays) {
                pepFactory.addAbundanceOptionalColumn(tabAssay);
            }
            for (uk.ac.ebi.pride.jmztab.model.StudyVariable sv : tabSvs) {
                pepFactory.addAbundanceOptionalColumn(sv);
            }
            if (pepCvParam != null) {
                String pepCvName = pepCvParam.getName();
                for (String quantName : names) {
                    if (quantName.equals(pepCvName)) {
                        continue;
                    }
                    if (MzqLib.DATA.control.isRequired(MzqData.PEPTIDE,
                                                       MzqData.ASSAY, quantName)) {
                        for (uk.ac.ebi.pride.jmztab.model.Assay tabAssay
                                : tabAssays) {
                            pepFactory.addOptionalColumn(tabAssay, quantName,
                                                         String.class);
                        }
                    }
                    if (MzqLib.DATA.control.isRequired(MzqData.PEPTIDE,
                                                       MzqData.SV, quantName)) {
                        for (uk.ac.ebi.pride.jmztab.model.StudyVariable sv
                                : tabSvs) {
                            pepFactory.addOptionalColumn(sv, quantName,
                                                         String.class);
                        }
                    }
                }
                if (MzqLib.DATA.control.isRequired(MzqData.PEPTIDE,
                                                   MzqData.RATIO,
                                                   MzqData.RATIO_STRING)) {
                    for (String ratioID : ratioIDs) {
                        pepFactory.addOptionalColumn(ratioID, String.class);
                    }
                }
                final Set<String> elements = MzqLib.DATA.control.
                        getElements(MzqData.PEPTIDE, MzqData.GLOBAL);
                if (!elements.isEmpty()) {
                    for (String str : elements) {
                        pepFactory.addOptionalColumn(str, Double.class);
                    }
                }
            }

            for (MsRun msrun : mtd.getMsRunMap().values()) {
                proFactory.addOptionalColumn(ProteinColumn.SEARCH_ENGINE_SCORE,
                                             msrun);
                proFactory.
                        addOptionalColumn(ProteinColumn.NUM_PEPTIDES_DISTINCT,
                                          msrun);
                proFactory.addOptionalColumn(ProteinColumn.NUM_PEPTIDES_UNIQUE,
                                             msrun);
                pepFactory.addOptionalColumn(PeptideColumn.SEARCH_ENGINE_SCORE,
                                             msrun);
            }

            StringBuilder proSb = new StringBuilder();
            proSb.append(proFactory.toString());
            proSb.append("\n");
            StringBuilder pepSb = new StringBuilder();
            pepSb.append(pepFactory.toString());
            pepSb.append("\n");

            ArrayList<QuantitationLevel> proEntities = new ArrayList<>();
            if (proteinLevel == MzqData.PROTEIN_GROUP) {
                for (ProteinGroupData pg : MzqLib.DATA.getProteinGroups()) {
                    proEntities.add(pg);
                }
            } else {
                for (ProteinData protein : MzqLib.DATA.getProteins()) {
                    proEntities.add(protein);
                }
            }
            for (QuantitationLevel entity : proEntities) {
                String searchDatabase = "null";
                String searchDatabaseVersion = "null";
                Protein tabProt = new Protein(proFactory);
                ProteinData protein; //whe in PG mode, protein is the lead protein
                if (proteinLevel == MzqData.PROTEIN_GROUP) {
                    ProteinGroupData pg = (ProteinGroupData) entity;
                    protein = MzqLib.DATA.getProtein(pg.getAnchorProteinStr());
                    tabProt.setAmbiguityMembers(pg.getAmbiguityMemberStr());
                } else {
                    protein = (ProteinData) entity;
                }
                if (!protein.getAccession().equals(MzqData.ARTIFICIAL)) {
                    //mandatory field in all cases: accession
                    tabProt.setAccession(protein.getAccession());
                    //mandatory field in all cases: description, taxid, species
                    //which cannot be found any meaningful data in mzq design

                    //mandatory field in all cases: database, database_version
                    searchDatabase = protein.getSearchDatabase();
                    tabProt.setDatabase(searchDatabase);
                    searchDatabaseVersion = protein.getSearchDatabaseVersion();
                    tabProt.setDatabaseVersion(searchDatabaseVersion);

                    //mandatory field in all cases: search_engine, best_search_engine_score[1-n]
                    //mandatory field in complete mode: search_engine_score[1-n]_ms_run[1-n
                    //only header added, no value expected
                    //optional field in all cases: reliability
                    //optional field in quantitation mode: num_psms_ms_run[1-n], num_peptides_distinct_ms_run[1-n], num_peptides_unique_ms_run[1-n]
                    //mandatory field in all cases: ambiguity_members
                    //current version no ProteinGroup captured, so empty for now
                    //mandatory field in all cases: modifications
                    //optional field in all cases: uri, go_terms
                    //mandatory field in complete mode: protein_coverage
                    //mandatory field in quantification type: protein_abundance_assay[1-n], protein_abundance_study_variable[1-n]
                    //mandatory field in quantification type: protein_abundance_stdev_study_variable[1-n], protein_abundance_std_error_study_variable [1-n]
                    if (proCvParam != null) {
                        if (MzqLib.DATA.control.isRequired(proteinLevel,
                                                           MzqData.ASSAY,
                                                           proCvParam.getName())) {
                            for (int i = 0; i < assays.size(); i++) {
                                String assay = assays.get(i).getId();
                                final Double value = entity.getQuantity(
                                        proCvParam.getName(), assay);
                                if (value != null) {
                                    tabProt.setAbundanceColumnValue(assayMap.
                                            get(assay), value);
                                }
                            }
                        }

                        if (MzqLib.DATA.control.isRequired(proteinLevel,
                                                           MzqData.SV,
                                                           proCvParam.getName())) {
                            for (int i = 0; i < svs.size(); i++) {
                                StudyVariable sv = svs.get(i);
                                final Double value = entity.
                                        getStudyVariableQuantity(proCvParam.
                                                getName(), sv.getId());
                                if (value != null) {
                                    tabProt.setAbundanceColumnValue(tabSvs.
                                            get(i), value);
                                }
                            }
                        }
                        String proCvName = proCvParam.getName();
                        for (String quantName : names) {
                            if (quantName.equals(proCvName)) {
                                continue;
                            }
                            if (MzqLib.DATA.control.isRequired(proteinLevel,
                                                               MzqData.ASSAY,
                                                               quantName)) {
                                for (int i = 0; i < assays.size(); i++) {
                                    String assayID = assays.get(i).getId();
                                    final Double value = entity.getQuantity(
                                            quantName, assayID);
                                    if (value != null) {
                                        tabProt.setOptionColumnValue(tabAssays.
                                                get(i), quantName, String.
                                                                     valueOf(value));
                                    }
                                }
                            }
                            if (MzqLib.DATA.control.isRequired(proteinLevel,
                                                               MzqData.SV,
                                                               quantName)) {
                                for (int i = 0; i < svs.size(); i++) {
                                    StudyVariable sv = svs.get(i);
                                    final Double value = entity.
                                            getStudyVariableQuantity(quantName,
                                                                     sv.getId());
                                    if (value != null) {
                                        tabProt.setOptionColumnValue(tabSvs.get(
                                                i), quantName, String.valueOf(
                                                                             value));
                                    }
                                }
                            }
                        }
                        if (MzqLib.DATA.control.isRequired(proteinLevel,
                                                           MzqData.RATIO,
                                                           MzqData.RATIO_STRING)) {
                            for (String ratioID : ratioIDs) {
                                final Double ratio = entity.getRatio(ratioID);
                                tabProt.setOptionColumnValue(ratioID, String.
                                                             valueOf(ratio));
                            }
                        }
                        final Set<String> elements = MzqLib.DATA.control.
                                getElements(proteinLevel, MzqData.GLOBAL);
                        if (!elements.isEmpty()) {
                            for (String str : elements) {
                                final Double global = entity.getGlobal(str);
                                tabProt.setOptionColumnValue(str, global);
                            }
                        }
                    }
//                System.out.println(tabProt);
                    proSb.append(tabProt.toString());
                    proSb.append("\n");
                }
                for (PeptideData peptide : protein.getPeptides()) {
                    for (int charge : peptide.getCharges()) {
                        Peptide tabPep = new Peptide(pepFactory, mtd);
                        //mandatory field in quantification mode: sequence, accession, unique, database, database_version
                        tabPep.setSequence(peptide.getSeq());
                        tabPep.setAccession(protein.getAccession());
                        if (protein.getAccession().equals(MzqData.ARTIFICIAL)) {
                            tabPep.setAccession("null");
                        }
                        tabPep.setDatabase(searchDatabase);
                        tabPep.setDatabaseVersion(searchDatabaseVersion);
                        //mandatory field in quantification mode: search_engine, best_search_engine_score[1-n]
//                        if (searchEngine != null) {
//                            tabPep.setSearchEngine(searchEngine.getName());
//                            uk.ac.ebi.pride.jmztab.model.ParamList paramList = new uk.ac.ebi.pride.jmztab.model.ParamList();
//                            paramList.add(new Param(cvID, searchEngineScoreAccession, searchEngineScoreName, String.valueOf(peptide.getGlobal(searchEngineScoreName))));
//                            tabPep.setSearchEngineScore(paramList);
//                        }

                        //mandatory field in complete quantification mode: search_engine_score[1-n]_ms_run[1-n]
                        //optional field in all cases: reliability
                        //mandatory field in quantification mode: modification
                        final List<uk.ac.liv.pgb.jmzqml.model.mzqml.Modification> modifications
                                = peptide.getPeptide().getModification();
                        if (!modifications.isEmpty()) {
                            SplitList<Modification> mods = new SplitList<>('|');
                            for (int i = 0; i < modifications.size(); i++) {
                                uk.ac.liv.pgb.jmzqml.model.mzqml.Modification modification
                                        = modifications.get(i);
//                                //<xsd:element name="cvParam" type="CVParamType" minOccurs="1"
                                CvParam param = modification.getCvParam().get(0);
                                if (modCount.containsKey(param)) {
                                    int count = modCount.get(param);
                                    count++;
                                    modCount.put(param, count);
                                } else {
                                    modCount.put(param, 1);
                                }
                                Type type = Modification.findType(param.
                                        getCvRef());
//                                type = Modification.Type.UNKNOW;
//                                if(param.getCv().getId().equals("UNIMOD")){
//                                    type = Modification.Type.UNIMOD;
//                                }else if(param.getCv().getId().equals("PSI-MOD")){
//                                    type = Modification.Type.MOD;
//                                }
                                Modification mod = new Modification(
                                        Section.Peptide, type, param.
                                        getAccession());
                                mods.add(mod);
                            }
                            tabPep.setModifications(mods);
                        }
                        //mandatory field in quantification mode: retention_time, retention_time_window
                        List<Double> mzs = new ArrayList<>();
                        SplitList<Double> rts = new SplitList<>('|');
                        SplitList<SpectraRef> spectrum_refs = new SplitList<>(
                                '|');
                        double maxRt = 0;
                        double minRt = Double.MAX_VALUE;
                        for (FeatureData feature : peptide.
                                getFeaturesWithCharge(charge)) {
                            //<xsd:attribute name="mz" type="xsd:double" use="required">
                            mzs.add(feature.getFeature().getMz());
                            //<xsd:attribute name="rt" type="doubleOrNullType" use="required">
                            //on the retention time axis in minutes
                            //in mzTab retention_time Double List (“,”) Time points in seconds.
                            String rtStr = feature.getFeature().getRt();
                            if (rtStr != null && !rtStr.equalsIgnoreCase("null")) {
                                double rt = SECONDS_PER_MINUTE * Double.
                                        parseDouble(rtStr);
                                if (maxRt < rt) {
                                    maxRt = rt;
                                }
                                if (minRt > rt) {
                                    minRt = rt;
                                }
                                rts.add(rt);
                            }
                            String specRef = feature.getFeature().
                                    getSpectrumRefs();
                            if (specRef != null) {
                                int msrun_id = msruns.get(feature.
                                        getRawFilesGroupRef());
                                MsRun msrun = mtd.getMsRunMap().get(msrun_id);
                                SpectraRef ref = new SpectraRef(msrun, specRef);
                                spectrum_refs.add(ref);
                            }
                        }
                        tabPep.setRetentionTime(rts);
                        SplitList<Double> window = new SplitList<>('|');
                        window.add(minRt);
                        window.add(maxRt);
                        tabPep.setRetentionTimeWindow(window);
                        //mandatory field in quantification mode: charge, mass_to_charge
                        tabPep.setCharge(charge);
                        tabPep.setMassToCharge(Utils.mean(mzs));
                        //optional field in all cases: uri
                        //mandatory field in complete quantification mode MS2 only: spectrum_ref
                        //The reference must be in the format ms_run[1-n]:{SPECTRA_REF} 
                        //where SPECTRA_REF MUST follow the format defined in 5.2. 
                        //Multiple spectra MUST be referenced using a “|” delimited list.
                        if (!spectrum_refs.isEmpty()) {
                            tabPep.setSpectraRef(spectrum_refs);
                        }

                        //mandatory field in complete quantification mode: peptide_abundance_assay[1-n]
                        //mandatory field in quantification mode: peptide_abundance_study_variable[1-n], peptide_abundance_stdev_study_variable[1-n]
                        //mandatory field in quantification mode: peptide_abundance_std_error_study_variable[1-n]
                        if (pepCvParam != null) {
                            if (MzqLib.DATA.control.isRequired(MzqData.PEPTIDE,
                                                               MzqData.ASSAY,
                                                               pepCvParam.
                                                               getName())) {
                                for (int i = 0; i < assays.size(); i++) {
                                    String assay = assays.get(i).getId();
                                    final Double value = peptide.getQuantity(
                                            pepCvParam.getName(), assay);
                                    if (value != null) {
                                        tabPep.setAbundanceColumnValue(assayMap.
                                                get(assay), value);
                                    }
                                }
                            }
                            if (MzqLib.DATA.control.isRequired(MzqData.PEPTIDE,
                                                               MzqData.SV,
                                                               pepCvParam.
                                                               getName())) {
                                for (int i = 0; i < svs.size(); i++) {
                                    StudyVariable sv = svs.get(i);
                                    final Double value = peptide.
                                            getStudyVariableQuantity(pepCvParam.
                                                    getName(), sv.getId());
                                    if (value != null) {
                                        tabPep.setAbundanceColumnValue(tabSvs.
                                                get(i), value);
                                    }
                                }
                            }
                            String pepCvName = pepCvParam.getName();
                            for (String quantName : names) {
                                if (quantName.equals(pepCvName)) {
                                    continue;
                                }
                                if (MzqLib.DATA.control.isRequired(
                                        MzqData.PEPTIDE, MzqData.ASSAY,
                                        quantName)) {
                                    for (int i = 0; i < assays.size(); i++) {
                                        String assayID = assays.get(i).getId();
                                        final Double value = peptide.
                                                getQuantity(quantName, assayID);
                                        if (value != null) {
                                            tabPep.setOptionColumnValue(
                                                    tabAssays.get(i), quantName,
                                                    String.valueOf(value));
                                        }
                                    }
                                }
                                if (MzqLib.DATA.control.isRequired(
                                        MzqData.PEPTIDE, MzqData.SV, quantName)) {
                                    for (int i = 0; i < svs.size(); i++) {
                                        StudyVariable sv = svs.get(i);
                                        final Double value = peptide.
                                                getStudyVariableQuantity(
                                                        quantName, sv.getId());
                                        if (value != null) {
                                            tabPep.setOptionColumnValue(tabSvs.
                                                    get(i), quantName, String.
                                                                        valueOf(value));
                                        }
                                    }
                                }
                            }
                            if (MzqLib.DATA.control.isRequired(MzqData.PEPTIDE,
                                                               MzqData.RATIO,
                                                               MzqData.RATIO_STRING)) {
                                for (String ratioID : ratioIDs) {
                                    final Double ratio = peptide.getRatio(
                                            ratioID);
                                    tabPep.setOptionColumnValue(ratioID, String.
                                                                valueOf(ratio));
                                }
                            }
                            final Set<String> elements
                                    = MzqLib.DATA.control.getElements(
                                            MzqData.PEPTIDE, MzqData.GLOBAL);
                            if (!elements.isEmpty()) {
                                for (String str : elements) {
                                    final Double global = peptide.getGlobal(str);
                                    tabPep.setOptionColumnValue(str, global);
                                }
                            }
                        }
                        pepSb.append(tabPep);
                        pepSb.append("\n");
                    }
                }
            }

            int pepCount = MzqLib.DATA.getPeptides().size();
            int fixModCount = 1;
            int variableModCount = 1;
            if (modCount.isEmpty()) { //no modification found in peptide
//                String accession = mtd.getQuantificationMethod().getAccession();
                //try to find in the assaylist for labelled method
//                if (accession.equals("MS:1002023") || accession.equals("MS:1002018")){ //labelled quantitation method
//                    for (Assay assay: assays){
//                        for (ModParam mod : assay.getLabel().getModification()) {
//                            CvParam modParam = mod.getCvParam();
//                        }
//                    }
//                }else{
                mtd.addFixedModParam(1, new CVParam(msCVstr, "MS:1002453",
                                                    "No fixed modifications searched",
                                                    null));
                mtd.addVariableModParam(1, new CVParam(msCVstr, "MS:1002454",
                                                       "No variable modifications searched",
                                                       null));
//                }
            } else {
                for (Map.Entry<CvParam, Integer> entry : modCount.entrySet()) {
                    CvParam qParam = entry.getKey();
                    int count = entry.getValue();
                    if (count < pepCount) {
                        mtd.addVariableModParam(variableModCount, Utils.
                                                convertMztabParam(qParam));
                        variableModCount++;
                    } else {
                        mtd.addFixedModParam(fixModCount, Utils.
                                             convertMztabParam(qParam));
                        fixModCount++;
                    }
                }
            }
            out.append(mtd.toString());
            if (proCvParam != null) {
                out.append("\n");
                out.append(proSb.toString());
            }
            if (pepCvParam != null) {
                out.append("\n");
                out.append(pepSb.toString());
            }
        } catch (IOException ex) {
            Logger.getLogger(MztabConverter.class.getName()).log(Level.SEVERE,
                                                                 null, ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    Logger.getLogger(MztabConverter.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            }
        }
    }

    /*
     * Determine the quantitation unit for protein/protein group or peptide
     * No need to check for ratio list as the numerator and denominator must be
     * referenced
     * in either assay or study variable
     */
    private CvParam determineQuantitationUnit(final int level,
                                              final List<String> names) {
        for (String name : names) {
            if (MzqLib.DATA.control.isRequired(level, MzqData.ASSAY, name)) {
                return MzqLib.DATA.getQuantitationCvParam(name);
            }
            if (MzqLib.DATA.control.isRequired(level, MzqData.SV, name)) {
                return MzqLib.DATA.getQuantitationCvParam(name);
            }
        }
        final Set<String> global = MzqLib.DATA.control.getElements(level,
                                                                   MzqData.GLOBAL);
        if (!global.isEmpty()) {
            return MzqLib.DATA.getQuantitationCvParam(global.iterator().next());
        }
        return null;
    }

    private Param determineQuantitationMethod(
            final AnalysisSummary analysisSummary) {
        for (CvParam cvParam : analysisSummary.getCvParam()) {
            String accession = cvParam.getAccession();
            if (accession.contains("MS:1001834") //LC-MS label-free quantitation analysis
                    || accession.contains("MS:1001836") //spectral counting quantitation analysis
                    || accession.contains("MS:1002023") //MS2 tag-based analysis
                    || accession.contains("MS:1002018")) { //MS1 label-based analysis
                return Utils.convertMztabParam(cvParam);
            }
        }
        return null;
    }

}
