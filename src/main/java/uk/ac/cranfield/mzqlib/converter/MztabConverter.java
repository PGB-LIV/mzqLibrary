package uk.ac.cranfield.mzqlib.converter;

/**
 *
 * @author Jun Fan@cranfield
 */
public class MztabConverter extends GenericConverter{
    public MztabConverter(String filename,String outputFile){
        super(filename,outputFile);
    }
    
    public void convert(){
        if(outfile.length()==0){
            outfile = getBaseFilename() + ".mztab";
        }
    }
}
