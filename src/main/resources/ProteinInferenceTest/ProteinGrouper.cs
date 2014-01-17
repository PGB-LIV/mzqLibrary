using ProgenesisPostProcessor.Struct;
using System.Collections.Generic;
using System.Linq;

namespace ProgenesisPostProcessor
{
    class ProteinGrouper
    {
        private Dictionary<string, HashSet<string>> _proteinToPeptide = new Dictionary<string, HashSet<string>>();
        private Dictionary<string, HashSet<string>> _peptideToProtein = new Dictionary<string, HashSet<string>>();

        public ProteinGrouper(Dictionary<uint, Feature> features)
        {
            foreach (Feature f in features.Values)
            {
                foreach (Identification i in f.Identifications)
                {
                    if (!_proteinToPeptide.ContainsKey(i.Protein.Accession))
                        _proteinToPeptide[i.Protein.Accession] = new HashSet<string>();

                    if (!_peptideToProtein.ContainsKey(i.Peptide.Sequence))
                        _peptideToProtein[i.Peptide.Sequence] = new HashSet<string>();

                    _proteinToPeptide[i.Protein.Accession].Add(i.Peptide.Sequence);
                    _peptideToProtein[i.Peptide.Sequence].Add(i.Protein.Accession);
                }
            }
        }

        public HashSet<string> GetUniquePeptides()
        {
            HashSet<string> uniquePeptides = new HashSet<string>();

            foreach (KeyValuePair<string, HashSet<string>> entry in _peptideToProtein)
            {
                if (entry.Value.Count == 1)
                    uniquePeptides.Add(entry.Key);
            }

            return uniquePeptides;
        }


        public HashSet<string> GetSameSetPeptides()
        {
            HashSet<string> sameSet = new HashSet<string>();

            // We require one protein to be complete. The rest must have no unique vertices
            foreach (KeyValuePair<string, HashSet<string>> entry in _peptideToProtein)
            {
                if (entry.Value.Count == 1)
                    continue;

                HashSet<string> peptides = new HashSet<string>();
                foreach (string protein in entry.Value)
                {
                    foreach (string peptide in _proteinToPeptide[protein])
                    {
                        peptides.Add(peptide);
                    }
                }

                bool isComplete = true;
                foreach (string peptide in peptides)
                {
                    foreach (string protein in entry.Value)
                    {
                        if (_peptideToProtein[peptide].Contains(protein))
                            continue;

                        isComplete = false;
                        break;
                    }

                }

                if (isComplete)
                    sameSet.Add(entry.Key);
            }

            return sameSet;
        }


        public HashSet<string> GetSubsetPeptides()
        {
            HashSet<string> subSet = new HashSet<string>();

            // We require one protein to be complete. The rest must have no unique peptides
            foreach (KeyValuePair<string, HashSet<string>> entry in _peptideToProtein)
            {
                string largestProtein = entry.Value.First();
                HashSet<string> peptides = new HashSet<string>();
                HashSet<string> proteins = new HashSet<string>();
                proteins.Add(largestProtein);
                int peptideCount = 0;
                int proteinCount = 0;
                do
                {
                    peptideCount = peptides.Count;
                    proteinCount = proteins.Count;

                    foreach (string protein in proteins)
                    {
                        if (_proteinToPeptide[protein].Count > _proteinToPeptide[largestProtein].Count)
                            largestProtein = protein;

                        foreach (string peptide in _proteinToPeptide[protein])
                        {
                            peptides.Add(peptide);
                        }
                    }

                    foreach (string peptide in peptides)
                    {
                        foreach (string protein in _peptideToProtein[peptide])
                        {
                            proteins.Add(protein);
                        }
                    }
                }
                while (peptideCount != peptides.Count || proteinCount != proteins.Count);

                if (proteins.Count == 1)
                    continue;

                // Largest must be complete
                bool isComplete = true;
                foreach (string peptide in peptides)
                {
                    if (_proteinToPeptide[largestProtein].Contains(peptide))
                        continue;

                    isComplete = false;
                    break;
                }

                if (!isComplete)
                    continue;

                // At least one protein must not be complete
                isComplete = true;
                foreach (string peptide in peptides)
                {
                    foreach (string protein in proteins)
                    {
                        if (_peptideToProtein[peptide].Contains(protein))
                            continue;

                        isComplete = false;
                        break;
                    }
                }

                if (isComplete)
                    continue;

                subSet.Add(entry.Key);
            }

            return subSet;
        }

        public bool IsProtein(string vertex)
        {
            return _proteinToPeptide.ContainsKey(vertex);
        }
    }
}
