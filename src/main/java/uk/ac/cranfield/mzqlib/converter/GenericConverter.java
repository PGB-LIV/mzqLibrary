package uk.ac.cranfield.mzqlib.converter;

/**
 *
 * @author Jun Fan@cranfield
 */
public class GenericConverter {
    String filename;
    public GenericConverter(String filename){
        this.filename = filename;
    }
    
    String getBaseFilename(){
        int idx = filename.lastIndexOf(".");
        return filename.substring(0,idx);
    }
    
    public void convert(){
    }
}
