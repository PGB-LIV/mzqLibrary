/*
 * Date: 06-Feb-2017
 * Author: Da Qi
 * File: uk.ac.liv.pgb.mzqlib.progenesis.converter.TFeatureDoubleProcedure.java
 *
 * jmzquantml is Copyright 2017 University of Liverpool.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package uk.ac.liv.pgb.mzqlib.progenesis.converter;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableInt;

import gnu.trove.list.TDoubleList;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.procedure.TDoubleProcedure;

import uk.ac.liv.pgb.jmzqml.model.mzqml.Assay;
import uk.ac.liv.pgb.jmzqml.model.mzqml.AssayList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Feature;
import uk.ac.liv.pgb.jmzqml.model.mzqml.FeatureList;
import uk.ac.liv.pgb.jmzqml.model.mzqml.RawFilesGroup;
import uk.ac.liv.pgb.mzqlib.progenesis.converter.util.Utils;

class TFeatureDoubleProcedure implements TDoubleProcedure {
    private DecimalFormat              format;
    private int                        id;
    private List<FeatureList>          ftListList;
    private AssayList                  assayList;
    private MutableInt                 pos;
    private MutableInt                 keyId;
    private Map<String, List<Feature>> peptideFeaturesMap;
    private Map<String, List<Assay>>   peptideAssaysMap;
    private Map<String, FeatureList>   rgIdFeatureListMap;
    private Map<String, String>        featureAssNameMap;
    private Map<String, String>        assayNameIdMap;
    private Map<String, Feature>       featureMap;
    private Map<String, String>        rawFileNameIdMap;
    private TIntIntMap                 chrMap;
    private TIntDoubleMap              mzMap;
    private TIntDoubleMap              masterRtMap;
    private TIntDoubleMap              rtWinMap;
    private TIntObjectMap<String>      peptideDupMap;
    private TIntObjectMap<String>      flIndexMap;
    private TIntObjectMap<TDoubleList> retenMap;

    protected TFeatureDoubleProcedure() {
    }

    @Override
    public boolean execute(final double value) {
        Feature feature = new Feature();

        // Double normAb = nabV;
        double mz  = getMzMap().get(getId());
        int    chr = getChrMap().get(getId());

        // Double rawAb = rabMap.get(key).get(pos);
        double reten;

        // If the file doen't contain 'Sample retention time (min)' column, use the master retention time instead 'Retention time (min)'.
        if (!retenMap.isEmpty()) {
            reten = getRetenMap().get(getId()).get(getPos().intValue());
        } else {
            reten = getMasterRtMap().get(getId());
        }

        double    rtWin = getRtWinMap().get(getId());
        MassTrace mt    = new MassTrace(mz, chr, reten, rtWin, getFormat());

        feature.setMz(Double.valueOf(getFormat().format(mz)));
        feature.setRt(getFormat().format(reten));
        feature.setCharge(String.valueOf(chr));

        String ftId = "ft_" + getKeyId().toString();

        feature.setId(ftId);

        // add mass trace
        feature.getMassTrace().addAll(mt.getMassTraceDoubleList());
        getFeatureMap().put(ftId, feature);

        // create peptide id to feture HashMap: peptideFeaturesMap
        String pepSeq   = getPeptideDupMap().get(getId());
        String indexStr = getFlIndexMap().get(getId());
        String pepId    = "pep_" + pepSeq + "_" + chr + "_" + indexStr;

        if (pepSeq != null) {
            List<Feature> fList = getPeptideFeaturesMap().get(pepId);

            if (fList == null) {
                fList = new ArrayList<>();
                getPeptideFeaturesMap().put(pepId, fList);
            }

            fList.add(feature);
        }

        String assName = getAssayList().getAssay().get(getPos().intValue()).getName();
        String rgId    = "rg_" + getRawFileNameIdMap().get(assName + ".raw").substring(4);

        // create peptide sequence to assay id HashMap: peptideAssayMap
        String assayId = getAssayNameIdMap().get(assName);

        if (pepSeq != null) {
            List<Assay> aList = getPeptideAssaysMap().get(pepSeq);

            if (aList == null) {
                aList = new ArrayList<>();
                getPeptideAssaysMap().put(pepSeq, aList);
            }

            List<String> assayIds = Utils.getAssayIdList(aList);

            if (!assayIds.contains(assayId)) {
                Assay tempAssay = new Assay();

                tempAssay.setId(assayId);
                aList.add(tempAssay);
            }
        }

        FeatureList features = getRgIdFeatureListMap().get(rgId);

        if (features == null) {
            features = new FeatureList();

            RawFilesGroup rawFilesGroup = new RawFilesGroup();

            rawFilesGroup.setId(rgId);
            features.setRawFilesGroup(rawFilesGroup);

            String fListId = "Flist" + rgId.substring(3);

            features.setId(fListId);
            getRgIdFeatureListMap().put(rgId, features);
            features.getParamGroup()
                    .add(Utils.createCvParam("mass trace reporting: rectangles", "PSI-MS", "MS:1001826"));
            getFtListList().add(features);
        }

        features.getFeature().add(feature);
        getFeatureAssNameMap().put(feature.getId(), assName);
        getPos().add(1);
        getKeyId().add(1);

        return true;
    }

    /**
     * @return the assayList
     */
    public AssayList getAssayList() {
        return assayList;
    }

    /**
     * @param assayList the assayList to set
     */
    public void setAssayList(AssayList assayList) {
        this.assayList = assayList;
    }

    /**
     * @return the assayNameIdMap
     */
    public Map<String, String> getAssayNameIdMap() {
        return assayNameIdMap;
    }

    /**
     * @param assayNameIdMap the assayNameIdMap to set
     */
    public void setAssayNameIdMap(Map<String, String> assayNameIdMap) {
        this.assayNameIdMap = assayNameIdMap;
    }

    /**
     * @return the chrMap
     */
    public TIntIntMap getChrMap() {
        return chrMap;
    }

    /**
     * @param chrMap the chrMap to set
     */
    public void setChrMap(TIntIntMap chrMap) {
        this.chrMap = chrMap;
    }

    /**
     * @return the featureAssNameMap
     */
    public Map<String, String> getFeatureAssNameMap() {
        return featureAssNameMap;
    }

    /**
     * @param featureAssNameMap the featureAssNameMap to set
     */
    public void setFeatureAssNameMap(Map<String, String> featureAssNameMap) {
        this.featureAssNameMap = featureAssNameMap;
    }

    /**
     * @return the featureMap
     */
    public Map<String, Feature> getFeatureMap() {
        return featureMap;
    }

    /**
     * @param featureMap the featureMap to set
     */
    public void setFeatureMap(Map<String, Feature> featureMap) {
        this.featureMap = featureMap;
    }

    /**
     * @return the flIndexMap
     */
    public TIntObjectMap<String> getFlIndexMap() {
        return flIndexMap;
    }

    /**
     * @param flIndexMap the flIndexMap to set
     */
    public void setFlIndexMap(TIntObjectMap<String> flIndexMap) {
        this.flIndexMap = flIndexMap;
    }

    /**
     * @return the format
     */
    public DecimalFormat getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(DecimalFormat format) {
        this.format = format;
    }

    /**
     * @return the ftListList
     */
    public List<FeatureList> getFtListList() {
        return ftListList;
    }

    /**
     * @param ftListList the ftListList to set
     */
    public void setFtListList(List<FeatureList> ftListList) {
        this.ftListList = ftListList;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the keyId
     */
    public MutableInt getKeyId() {
        return keyId;
    }

    /**
     * @param keyId the keyId to set
     */
    public void setKeyId(MutableInt keyId) {
        this.keyId = keyId;
    }

    /**
     * @return the masterRtMap
     */
    public TIntDoubleMap getMasterRtMap() {
        return masterRtMap;
    }

    /**
     * @param masterRtMap the masterRtMap to set
     */
    public void setMasterRtMap(TIntDoubleMap masterRtMap) {
        this.masterRtMap = masterRtMap;
    }

    /**
     * @return the mzMap
     */
    public TIntDoubleMap getMzMap() {
        return mzMap;
    }

    /**
     * @param mzMap the mzMap to set
     */
    public void setMzMap(TIntDoubleMap mzMap) {
        this.mzMap = mzMap;
    }

    /**
     * @return the peptideAssaysMap
     */
    public Map<String, List<Assay>> getPeptideAssaysMap() {
        return peptideAssaysMap;
    }

    /**
     * @param peptideAssaysMap the peptideAssaysMap to set
     */
    public void setPeptideAssaysMap(Map<String, List<Assay>> peptideAssaysMap) {
        this.peptideAssaysMap = peptideAssaysMap;
    }

    /**
     * @return the peptideDupMap
     */
    public TIntObjectMap<String> getPeptideDupMap() {
        return peptideDupMap;
    }

    /**
     * @param peptideDupMap the peptideDupMap to set
     */
    public void setPeptideDupMap(TIntObjectMap<String> peptideDupMap) {
        this.peptideDupMap = peptideDupMap;
    }

    /**
     * @return the peptideFeaturesMap
     */
    public Map<String, List<Feature>> getPeptideFeaturesMap() {
        return peptideFeaturesMap;
    }

    /**
     * @param peptideFeaturesMap the peptideFeaturesMap to set
     */
    public void setPeptideFeaturesMap(Map<String, List<Feature>> peptideFeaturesMap) {
        this.peptideFeaturesMap = peptideFeaturesMap;
    }

    /**
     * @return the pos
     */
    public MutableInt getPos() {
        return pos;
    }

    /**
     * @param pos the pos to set
     */
    public void setPos(MutableInt pos) {
        this.pos = pos;
    }

    /**
     * @return the rawFileNameIdMap
     */
    public Map<String, String> getRawFileNameIdMap() {
        return rawFileNameIdMap;
    }

    /**
     * @param rawFileNameIdMap the rawFileNameIdMap to set
     */
    public void setRawFileNameIdMap(Map<String, String> rawFileNameIdMap) {
        this.rawFileNameIdMap = rawFileNameIdMap;
    }

    /**
     * @return the retenMap
     */
    public TIntObjectMap<TDoubleList> getRetenMap() {
        return retenMap;
    }

    /**
     * @param retenMap the retenMap to set
     */
    public void setRetenMap(TIntObjectMap<TDoubleList> retenMap) {
        this.retenMap = retenMap;
    }

    /**
     * @return the rgIdFeatureListMap
     */
    public Map<String, FeatureList> getRgIdFeatureListMap() {
        return rgIdFeatureListMap;
    }

    /**
     * @param rgIdFeatureListMap the rgIdFeatureListMap to set
     */
    public void setRgIdFeatureListMap(Map<String, FeatureList> rgIdFeatureListMap) {
        this.rgIdFeatureListMap = rgIdFeatureListMap;
    }

    /**
     * @return the rtWinMap
     */
    public TIntDoubleMap getRtWinMap() {
        return rtWinMap;
    }

    /**
     * @param rtWinMap the rtWinMap to set
     */
    public void setRtWinMap(TIntDoubleMap rtWinMap) {
        this.rtWinMap = rtWinMap;
    }
}
//~ Formatted by Jindent --- http://www.jindent.com
