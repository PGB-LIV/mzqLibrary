
package uk.ac.liv.mzqlib.idmapper.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.ac.liv.jmzqml.model.mzqml.Feature;

/**
 *
 * @author SPerkins
 */
public class FeatureSiiMatchManager {
  private Map<String,Map<Feature,List<SIIData>>> featureListFeatureMatches = new HashMap<>();
  private Map<String,Map<SIIData, List<Feature>>> featureListIdentificationMatches = new HashMap<>();  
  
  public void registerFeature(Feature feature, String featureListName) {
    Map<Feature,List<SIIData>> featureMatches = featureListFeatureMatches.get(featureListName);
    if (featureMatches ==  null) {
      featureMatches = new HashMap<>();
      featureListFeatureMatches.put(featureListName, featureMatches);
    }
    
    List<SIIData> featureMatch = featureMatches.get(feature);
    if (featureMatch == null) {
      featureMatch = new LinkedList<>();
      featureMatches.put(feature, featureMatch);
    }
  }
  
  public void registerIdentification(SIIData identification, String featureListName) {
    Map<SIIData,List<Feature>> identificationMatches = featureListIdentificationMatches.get(featureListName);
    if (identificationMatches ==  null) {
      identificationMatches = new HashMap<>();
      featureListIdentificationMatches.put(featureListName, identificationMatches);
    }
    
    List<Feature> identificationMatch = identificationMatches.get(identification);
    if (identificationMatch == null) {
      identificationMatch = new LinkedList<>();
      identificationMatches.put(identification, identificationMatch);
    }
  }
  
  public void registerMatch(Feature feature, String featureListName, SIIData data) {
    Map<Feature,List<SIIData>> featureMatches = featureListFeatureMatches.get(featureListName);
    if (featureMatches == null) {
      throw new RuntimeException("Feature must be registered using \"registerFeature\" before a match can be registered.");
    }
    
    List<SIIData> featureMatch = featureMatches.get(feature);
    if (featureMatch == null) {
      throw new RuntimeException("Feature must be registered using \"registerFeature\" before a match can be registered.");
    }    
      
    featureMatch.add(data);
    
    Map<SIIData,List<Feature>> identificationMatches = featureListIdentificationMatches.get(featureListName);
    if (identificationMatches == null) {
      throw new RuntimeException("Identification must be registered using \"registerIdentification\" before a match can be registered.");
    }
    
    List<Feature> identificationMatch = identificationMatches.get(data);
    if (identificationMatch == null) {
      throw new RuntimeException("Identification must be registered using \"registerIdentification\" before a match can be registered.");
    }  
    
    identificationMatch.add(feature);
  }
  
  public Map<String, List<SIIData>> getMatchMap() {
    return featureListFeatureMatches.entrySet().stream().flatMap(p -> p.getValue().entrySet().stream()).collect(Collectors.toMap(entry -> entry.getKey().getId(), entry -> entry.getValue(), mergeListsOperator));
  }
  
  public long getFeatureCount(String featureListName) {
    Map<Feature,List<SIIData>> featureMatches = featureListFeatureMatches.get(featureListName);
    if (featureMatches == null) {
      throw new RuntimeException("No such feature list name: " + featureListName);
    }
    
    return featureMatches.size();
  }
  
  public long getFeatureCount() {
    long featureCount = featureListFeatureMatches.entrySet().stream().flatMap(p -> p.getValue().keySet().stream()).distinct().count();
    return featureCount;
  }
  
  private <T, U extends Collection<?>> Stream<T> getStreamOfMappingToN(Map<T, U> map, int minimumMappingNumber) {
    return map.entrySet().stream().filter(p -> p.getValue().size() > (minimumMappingNumber - 1)).map(q -> q.getKey());
  }
  
  private <T, U extends Collection<?>> Stream<T> getStreamOfMergedDistinctMappingToN(Map<String, Map<T, U>> map, int minimumMappingNumber) {
    return map.entrySet().stream().flatMap(
            p -> getStreamOfMappingToN(p.getValue(), minimumMappingNumber)
    ).distinct();
  }
  
  private long getFeaturesMappingToN(String featureListName, int minimumMappingNumber) {
    Map<Feature,List<SIIData>> featureMatches = featureListFeatureMatches.get(featureListName);
    if (featureMatches == null) {
      throw new RuntimeException("No such feature list name: " + featureListName);
    }
    
    return getStreamOfMappingToN(featureMatches, minimumMappingNumber).count();  
  }
  
  private long getFeaturesMappingToN(int minimumMappingNumber) {
    return getStreamOfMergedDistinctMappingToN(featureListFeatureMatches, minimumMappingNumber).count();
  }
  
  public long getIdentificationsCount(String featureListName) {
    Map<SIIData,List<Feature>> identificationMatches = featureListIdentificationMatches.get(featureListName);
    if (identificationMatches == null) {
      throw new RuntimeException("No such feature list name: " + featureListName);
    }
    
    return identificationMatches.size();
  }
  
  public long getIdentificationsCount() {
    long identificationCount = featureListIdentificationMatches.entrySet().stream().flatMap(p -> p.getValue().keySet().stream()).distinct().count();
    return identificationCount;
  }
  
  public long getIdentificationsAssignedCount(String featureListName) {
    return getIdentificationsMappingToN(featureListName, 1);
  }
  
  public long getIdentificationsAssignedCount() {
    return getIdentificationsMappingToN(1);
  }
  
  public long getIdentificationsMultimappingCount() {
    return getIdentificationsMappingToN(2);
  }
  
  public long getIdentificationsMultimappingCount(String featureListName) {
    return getIdentificationsMappingToN(featureListName, 2);
  }
  
  private long getIdentificationsMappingToN(int minimumMappingNumber) {
    long identificationWithMatchCount = featureListIdentificationMatches.entrySet().stream().flatMap(
            p -> p.getValue().entrySet().stream().filter(entry -> entry.getValue().size() > (minimumMappingNumber - 1))
                    .map(q -> q.getKey())
    ).distinct().count();
    return identificationWithMatchCount;
  }
  
  private long getIdentificationsMappingToN(String featureListName, int minimumMappingNumber) {
    Map<SIIData,List<Feature>> identificationMatches = featureListIdentificationMatches.get(featureListName);
    if (identificationMatches == null) {
      throw new RuntimeException("No such feature list name: " + featureListName);
    }
    
    return identificationMatches.entrySet().stream().filter(p -> p.getValue().size() > (minimumMappingNumber - 1)).count();
  }
  
  public long getFeaturesWithMatchCount() {
    return getFeaturesMappingToN(1);    
  }
  
  public long getFeaturesWithMatchCount(String featureListName) {
    return getFeaturesMappingToN(featureListName, 1);    
  }
  
  public long getFeaturesMultimappingCount() {
    return getFeaturesMappingToN(2);
  }
  
  public long getFeaturesMultimappingCount(String featureListName) {
    return getFeaturesMappingToN(featureListName, 2);
  }
  
  private static final BinaryOperator<List<SIIData>> mergeListsOperator = new BinaryOperator<List<SIIData>>() {
    @Override
    public List<SIIData> apply(List<SIIData> t, List<SIIData> u) {
      List<SIIData> newList = new LinkedList<>();
      newList.addAll(t);
      newList.addAll(u);
      return newList;
    }
  };
}