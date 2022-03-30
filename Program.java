/**
 * Program class that contains all program information and parameters.
 * @author Khaled Bakhit
 * Modified by: Gioia Wehbe
 */

import java.io.File;

public class Program
{
   
    //Two parameters that can be changed
    public static final String classFile= "classes.txt";
    public static final String metricsFile="metrics.txt";

    //List of Input folders
    private static final String InputFolder="Input";
    private  static final String DataSet_Folder=InputFolder+"/DataSet";
    private  static final String RuleSet_Folder= InputFolder+"/RuleSet";
    private static final String Attributes_File= InputFolder+"/"+metricsFile; // locate attributes file
    private static final String Classifications_File= InputFolder+"/"+classFile;
    
    // Output folder
    private static final String OutputFolder="Output";


    /**
     * Program Constructor.
     */
    public static void createDirectories()
    {
        File file;
        file= new File(OutputFolder);
      if(!file.exists())
           file.mkdir();
       file= new File(InputFolder);
      if(!file.exists())
           file.mkdir();
       file= new File( DataSet_Folder);
       if(!file.exists())
           file.mkdir();
       file= new File(RuleSet_Folder);
       if(!file.exists())
           file.mkdir();
   }

    public static String getOutputFolder()
    {
        return OutputFolder;
    }

    public static String getRuleSetFolder()
    {
        return RuleSet_Folder;
    }

    public static String getMetrics()
    {
        return Attributes_File;
    }
    
    public static String getClassification()
    {
        return Classifications_File;
    }
    public static String getDataSetFolder()
    {
        return DataSet_Folder;
    }
}
