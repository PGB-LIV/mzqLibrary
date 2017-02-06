/*
 * Date: 06-Feb-2017
 * Author: Da Qi
 * File: uk.ac.liv.pgb.mzqlib.progenesis.converter.util.Utils.java
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



package uk.ac.liv.pgb.mzqlib.progenesis.converter.util;

import java.util.ArrayList;
import java.util.List;

import uk.ac.liv.pgb.jmzqml.model.mzqml.Assay;
import uk.ac.liv.pgb.jmzqml.model.mzqml.Cv;
import uk.ac.liv.pgb.jmzqml.model.mzqml.CvParam;

public final class Utils {
    private Utils() {
    }

    // create a CVParamType instance
    public static CvParam createCvParam(final String name, final String cvRef, final String accession) {
        CvParam cp = new CvParam();

        cp.setName(name);

        Cv cv = new Cv();

        cv.setId(cvRef);
        cp.setCv(cv);
        cp.setAccession(accession);

        return cp;
    }

    /**
     * Get assay id from a list of assay.
     * @param assays assay list
     * @return a list of id string
     */
    public static List<String> getAssayIdList(final List<Assay> assays) {
        List<String> assayIds = new ArrayList<>();

        assays.stream().forEach((assay) -> {
                assayIds.add(assay.getId());
            });

        return assayIds;
    }
}
//~ Formatted by Jindent --- http://www.jindent.com
