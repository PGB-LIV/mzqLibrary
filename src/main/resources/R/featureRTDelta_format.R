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


featureRTDelta <- function(fullFilePath) {
	assNo		<- 0
	preNo 	<- 0
	centrNo	<- 0
	data  	<- read.csv(fullFilePath, header=TRUE)

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
		
		# Calculate MAD of signed and unsigned deltas.
		deltas_signed <- as.numeric(rtDelta[i,])
		deltas_unsigned <- abs(deltas_signed)
		deltas_signed_mad <- mad(deltas_signed, constant = 1.4826, na.rm = FALSE)
		deltas_unsigned_mad <- mad(deltas_unsigned, constant = 1.4826, na.rm = FALSE)
		data[i,col+1] <- deltas_signed_mad
		data[i,col+2] <- deltas_unsigned_mad

		# Calculate Lowess of both signed and unsigned data.
		lowess_unsigned <- lowess(deltas_unsigned)
		lowess_signed <- lowess(deltas_signed)
		
		# Calculate differences of deltas from Lowess.
		differences_from_lowess_signed <- deltas_signed - lowess_signed$y
		differences_from_lowess_unsigned <- deltas_unsigned - lowess_unsigned$y
		
		# Calculate MAD of differences from Lowess signed, with input as signed values.
		differences_from_lowess_signed_mad_signed <- mad(differences_from_lowess_signed, constant = 1.4826, na.rm = FALSE)
		
		# Calculate MAD of differences from Lowess signed, with input as unsigned values.
		differences_from_lowess_signed_mad_unsigned <- mad(abs(differences_from_lowess_signed), constant = 1.4826, na.rm = FALSE)
		
		# Calculate MAD of differences from Lowess unsigned, with input as signed values.
		differences_from_lowess_unsigned_mad_signed <- mad(differences_from_lowess_unsigned, constant = 1.4826, na.rm = FALSE)
		
		# Calculate MAD of differences from Lowess unsigned, with input as unsigned values.
		differences_from_lowess_unsigned_mad_unsigned <- mad(abs(differences_from_lowess_unsigned), constant = 1.4826, na.rm = FALSE)
		
		data[i,col+3] <- differences_from_lowess_signed_mad_signed
		data[i,col+4] <- differences_from_lowess_signed_mad_unsigned
		data[i,col+5] <- differences_from_lowess_unsigned_mad_signed
		data[i,col+6] <- differences_from_lowess_unsigned_mad_unsigned	
	}
	colnames(data)[col+1] <- "MAD_signed_deltas"
	colnames(data)[col+2] <- "MAD_unsigned_deltas"
	colnames(data)[col+3] <- "MAD_signed_deltas_diff_from_lowess_signed"
	colnames(data)[col+4] <- "MAD_signed_deltas_diff_from_lowess_unsigned"
	colnames(data)[col+5] <- "MAD_unsigned_deltas_diff_from_lowess_signed"
	colnames(data)[col+6] <- "MAD_unsigned_deltas_diff_from_lowess_unsigned"

	plot(na.omit(data[,15]),xlab="Features",ylab="Median absolute deviation",main="MAD of Signed RT Deltas")
	plot(na.omit(data[,16]),xlab="Features",ylab="Median absolute deviation",main="MAD of Unsigned RT Deltas")
	plot(na.omit(data[,17]),xlab="Features",ylab="Median absolute deviation",main="MAD of Signed Differences From Lowess of Signed RT Deltas")
	plot(na.omit(data[,18]),xlab="Features",ylab="Median absolute deviation",main="MAD of Unsigned Differences From Lowess of Signed RT Deltas")
	plot(na.omit(data[,19]),xlab="Features",ylab="Median absolute deviation",main="MAD of Signed Differences From Lowess of Unsigned RT Deltas")
	plot(na.omit(data[,20]),xlab="Features",ylab="Median absolute deviation",main="MAD of Unsigned Differences From Lowess of Unsigned RT Deltas")
}