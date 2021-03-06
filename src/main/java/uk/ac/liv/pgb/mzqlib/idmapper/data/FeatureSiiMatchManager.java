package uk.ac.liv.pgb.mzqlib.idmapper.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.ac.liv.pgb.jmzqml.model.mzqml.Feature;

/**
 *
 * @author SPerkins
 */
public class FeatureSiiMatchManager {
    private static final BinaryOperator<List<SIIData>> MERGE_LISTS_OPERATOR = new BinaryOperator<List<SIIData>>() {
        @Override
        public List<SIIData> apply(final List<SIIData> t, final List<SIIData> u) {
            List<SIIData> newList = new LinkedList<>();

            newList.addAll(t);
            newList.addAll(u);

            return newList;
        }
    };
    private final Map<String, Map<Feature, List<SIIData>>> featureListFeatureMatches        = new HashMap<>();
    private final Map<String, Map<SIIData, List<Feature>>> featureListIdentificationMatches = new HashMap<>();

    public final void registerFeature(final Feature feature, final String featureListName) {
        Map<Feature, List<SIIData>> featureMatches = featureListFeatureMatches.get(featureListName);

        if (featureMatches == null) {
            featureMatches = new HashMap<>();
            featureListFeatureMatches.put(featureListName, featureMatches);
        }

        List<SIIData> featureMatch = featureMatches.get(feature);

        if (featureMatch == null) {
            featureMatch = new LinkedList<>();
            featureMatches.put(feature, featureMatch);
        }
    }

    public final void registerIdentification(final SIIData identification, final String featureListName) {
        Map<SIIData, List<Feature>> identificationMatches = featureListIdentificationMatches.get(featureListName);

        if (identificationMatches == null) {
            identificationMatches = new HashMap<>();
            featureListIdentificationMatches.put(featureListName, identificationMatches);
        }

        List<Feature> identificationMatch = identificationMatches.get(identification);

        if (identificationMatch == null) {
            identificationMatch = new LinkedList<>();
            identificationMatches.put(identification, identificationMatch);
        }
    }

    public final void registerMatch(final Feature feature, final String featureListName, final SIIData data) {
        Map<Feature, List<SIIData>> featureMatches = featureListFeatureMatches.get(featureListName);

        if (featureMatches == null) {
            throw new IllegalStateException(
                "Feature must be registered using \"registerFeature\" before a match can be registered.");
        }

        List<SIIData> featureMatch = featureMatches.get(feature);

        if (featureMatch == null) {
            throw new IllegalStateException(
                "Feature must be registered using \"registerFeature\" before a match can be registered.");
        }

        featureMatch.add(data);

        Map<SIIData, List<Feature>> identificationMatches = featureListIdentificationMatches.get(featureListName);

        if (identificationMatches == null) {
            throw new IllegalStateException(
                "Identification must be registered using \"registerIdentification\" before a match can be registered.");
        }

        List<Feature> identificationMatch = identificationMatches.get(data);

        if (identificationMatch == null) {
            throw new IllegalStateException(
                "Identification must be registered using \"registerIdentification\" before a match can be registered.");
        }

        identificationMatch.add(feature);
    }

    public final long getFeatureCount() {
        long featureCount = featureListFeatureMatches.entrySet()
                                                     .stream()
                                                     .flatMap(p -> p.getValue().keySet().stream())
                                                     .distinct()
                                                     .count();

        return featureCount;
    }

    public final long getFeatureCount(final String featureListName) {
        Map<Feature, List<SIIData>> featureMatches = featureListFeatureMatches.get(featureListName);

        if (featureMatches == null) {
            throw new IllegalStateException("No such feature list name: " + featureListName);
        }

        return featureMatches.size();
    }

    private long getFeaturesMappingToN(final int minimumMappingNumber) {
        return getStreamOfMergedDistinctMappingToN(featureListFeatureMatches, minimumMappingNumber).count();
    }

    private long getFeaturesMappingToN(final String featureListName, final int minimumMappingNumber) {
        Map<Feature, List<SIIData>> featureMatches = featureListFeatureMatches.get(featureListName);

        if (featureMatches == null) {
            throw new IllegalStateException("No such feature list name: " + featureListName);
        }

        return getStreamOfMappingToN(featureMatches, minimumMappingNumber).count();
    }

    public final long getFeaturesMultimappingCount() {
        return getFeaturesMappingToN(2);
    }

    public final long getFeaturesMultimappingCount(final String featureListName) {
        return getFeaturesMappingToN(featureListName, 2);
    }

    public final long getFeaturesWithMatchCount() {
        return getFeaturesMappingToN(1);
    }

    public final long getFeaturesWithMatchCount(final String featureListName) {
        return getFeaturesMappingToN(featureListName, 1);
    }

    public final long getIdentificationsAssignedCount() {
        return getIdentificationsMappingToN(1);
    }

    public final long getIdentificationsAssignedCount(final String featureListName) {
        return getIdentificationsMappingToN(featureListName, 1);
    }

    public final long getIdentificationsCount() {
        long identificationCount = featureListIdentificationMatches.entrySet()
                                                                   .stream()
                                                                   .flatMap(p -> p.getValue().keySet().stream())
                                                                   .distinct()
                                                                   .count();

        return identificationCount;
    }

    public final long getIdentificationsCount(final String featureListName) {
        Map<SIIData, List<Feature>> identificationMatches = featureListIdentificationMatches.get(featureListName);

        if (identificationMatches == null) {
            throw new IllegalStateException("No such feature list name: " + featureListName);
        }

        return identificationMatches.size();
    }

    private long getIdentificationsMappingToN(final int minimumMappingNumber) {
        long identificationWithMatchCount = featureListIdentificationMatches.entrySet()
                                                                            .stream()
                                                                            .flatMap(p -> p.getValue()
                                                                                           .entrySet()
                                                                                           .stream()
                                                                                           .filter(
                                                                                               entry -> entry.getValue()
                                                                                                             .size() > (minimumMappingNumber
                                                                                                                        - 1))
                                                                                           .map(q -> q.getKey()))
                                                                            .distinct()
                                                                            .count();

        return identificationWithMatchCount;
    }

    private long getIdentificationsMappingToN(final String featureListName, final int minimumMappingNumber) {
        Map<SIIData, List<Feature>> identificationMatches = featureListIdentificationMatches.get(featureListName);

        if (identificationMatches == null) {
            throw new IllegalStateException("No such feature list name: " + featureListName);
        }

        return identificationMatches.entrySet()
                                    .stream()
                                    .filter(p -> p.getValue().size() > (minimumMappingNumber - 1))
                                    .count();
    }

    public final long getIdentificationsMultimappingCount() {
        return getIdentificationsMappingToN(2);
    }

    public final long getIdentificationsMultimappingCount(final String featureListName) {
        return getIdentificationsMappingToN(featureListName, 2);
    }

    public final Map<String, List<SIIData>> getMatchMap() {
        return featureListFeatureMatches.entrySet()
                                        .stream()
                                        .flatMap(p -> p.getValue().entrySet().stream())
                                        .collect(Collectors.toMap(entry -> entry.getKey().getId(),
                                                                  entry -> entry.getValue(),
                                                                  MERGE_LISTS_OPERATOR));
    }

    private <T, U extends Collection<?>> Stream<T> getStreamOfMappingToN(final Map<T, U> map,
                                                                         final int minimumMappingNumber) {
        return map.entrySet()
                  .stream()
                  .filter(p -> p.getValue().size() > (minimumMappingNumber - 1))
                  .map(q -> q.getKey());
    }

    private <T, U extends Collection<?>> Stream<T> getStreamOfMergedDistinctMappingToN(final Map<String,
                                                Map<T, U>> map, final int minimumMappingNumber) {
        return map.entrySet()
                  .stream()
                  .flatMap(p -> getStreamOfMappingToN(p.getValue(), minimumMappingNumber))
                  .distinct();
    }
}
//~ Formatted by Jindent --- http://www.jindent.com
