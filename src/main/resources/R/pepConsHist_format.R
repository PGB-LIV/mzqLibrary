#plot the histogram for the feature number in each pepCon
#parameters: id, directory

#directory <- "C:\\Manchester\\work\\Report\\action"

#id <- "assays_per_feature_format"

#col: feature number in the outcome
#setwd("C:\\Manchester\\work\\Code\\R\\Function")
#source("pepConsHist_Simon_format.R")
#pepConsHist(id,directory)
pepConsHist <- function(fullFilePath) {
	data  	  <- read.csv(fullFilePath,header=TRUE)
	barplot(data[,2], names.arg=data[,1],xlab = "Assay Count", ylab = "Frequency")

}