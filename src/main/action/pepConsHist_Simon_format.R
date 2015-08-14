#plot the histogram for the feature number in each pepCon
#parameters: id, directory

#directory <- "C:\\Manchester\\work\\Report\\action"

#id <- "assays_per_feature_format"

#col: feature number in the outcome
#setwd("C:\\Manchester\\work\\Code\\R\\Function")
#source("pepConsHist_Simon_format.R")
#pepConsHist(id,directory)
pepConsHist <- function(id, directory) {
	data  	  <- read.csv(paste(directory,"\\",id,".csv",sep=""))
      assay		  <- data[1]
	assay		  <- unlist(assay)
	featureNo     <- data[2]
	FeatureNo     <- unlist(featureNo)

	plot(assay,FeatureNo,xlab = "Assays_per_feature", ylab = "Frequency")

}