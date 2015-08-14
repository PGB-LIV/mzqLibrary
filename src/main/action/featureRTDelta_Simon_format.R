#calculate the rt-delta between rt in each assay and the rt_centroid.
#calculate Lowess Values of rt-delta for each feature
#plot rt-delta for each assay
#
#id <- "rt_format"
#directory <- "C:\\Manchester\\work\\Report\\action"
#output the plots
#output file with the MAD results in terms of rt delta

#setwd("C:\\Manchester\\work\\Code\\R\\Function")
#source("featureRTDelta_Simon_format.R")
#featureRTDelta(id,directory)


featureRTDelta <- function(id, directory) {
assNo		<- 0
preNo 	<- 0
centrNo	<- 0
data  	<- read.csv(paste(directory,"\\",id,".csv",sep=""))

#find assNo
col_names <- colnames(data);
for (i in 1:(length(col_names))) {
	if (length(grep("Assay",col_names[i]))) {
		assNo = assNo + 1;
	} else {
		preNo = preNo + 1;
	}
      
	if (length(grep("Centroid",col_names[i]))) {
          centrNo = i;
	}
}

col		<- preNo + assNo
#rt		<- data[(preNo+1):col]
rows		<- nrow(data)
axis_x 	<- c(1:rows)


#calculate the difference between rt and rt_centroid
rtDelta <- data[preNo+1] - data[centrNo]
for (i in 2:assNo) {			
	rtDelta <- cbind(rtDelta, data[preNo+i] - data[centrNo])
}
rtDelta <- as.matrix(rtDelta)

#M.A.D
for (i in 1:rows) {
	rtD <- as.numeric(rtDelta[i,])
	MAD <- mad(as.numeric(rtD), constant = 1.4826, na.rm = FALSE)
	data[i,col+1] <- MAD

	lo <- lowess(as.numeric(rtD))
	predict.rtD <- lo$y
	MAD.lo <- mad(as.numeric(predict.rtD), constant = 1.4826, na.rm = FALSE)
	data[i,col+2] <- MAD.lo

	lo_rt_abs <- lowess(as.numeric(abs(rtD)))
	predict.rtDabs <- lo_rt_abs$y
	MAD.lo.rtabs <- mad(as.numeric(predict.rtDabs), constant = 1.4826, na.rm = FALSE)
	data[i,col+3] <- MAD.lo.rtabs
}
colnames(data)[col+1] <- "MAD"
colnames(data)[col+2] <- "MAD-lowess-rt-delta"
colnames(data)[col+3] <- "MAD-lowess-rt-delta-abs"

#plot rt_delta for each assay
for (i in 1:assNo) {

	scatter.smooth(axis_x,rtDelta[,i],xlab="Number", ylab="rt_delta", col="#CCCCCC");
	tit <- paste("Assay",i-1,"_rt_delta")
	title(tit);
	fig_name <- paste(tit,".jpg");
	dev.copy(jpeg,filename=fig_name);
	dev.off();
}

#plot rt_delta for each assay vs. MAD-lowess-rt-delta
#for (i in 1:assNo) {
#	plot(rtDelta[,i], data[,col+2])
#}


#save MADs in original data file
df<- data.frame(t(sapply(data,c)))
df <- t(df)

write.csv(df, "C:\\Manchester\\work\\Report\\action\\rt_format_mad.csv", row.names=FALSE)

}