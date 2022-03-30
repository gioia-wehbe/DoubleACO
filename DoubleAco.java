//******************************************************************************
//Name:Gioia Wehbe.
//Student's number: 200801378.
//Project Name: Double ACO
//Last modified on: 3/12/2011
//Describtion: The main class that captures the parameters the are inputted through
// the command prompt and and uses them to rune the double ACO by invoking Big ACO
// on each of the 10 folds to perform 10-fold cross-validation
//******************************************************************************


import java.io.*;
import java.util.LinkedList;

public class DoubleAco
{
    //**************************************************************************
    // The main function for running the Double ACO from DOS
    //**************************************************************************

     public static void main(String [] args)
    {
        Long start_time=System.currentTimeMillis();
        System.out.println("Arg 0: int, number of runs");
        System.out.println("Arg 1: int, number of ants for big ACOs");
        System.out.println("Arg 2: int, number of iterations for big ACOs");
        System.out.println("Arg 3: double, rule_quality_factor1");
        System.out.println("Arg 4: double, rule_quality_factor2");
        System.out.println("Arg 5: double, threshold variable");
        System.out.println("Arg 6: double, improvement factor for pheromone deposite in big ACO");
        System.out.println("Arg 7:double, evaporation rate of pheromone for big ACO");
        System.out.println("Arg 8:int, period of plot output");
        System.out.println("Arg 9: int, number of ants for small ACOs");
        System.out.println("Arg 10: int, number of iterations for small ACOs");
        System.out.println("Arg 11: double, improvement factor for pheromone deposite in small ACO");
        System.out.println("Arg 12:double, evaporation rate of pheromone for small ACO");
        System.out.println("Arg 13: boolean, true to do voting classification, false to do sequential classification");


        String num_runs=args[0];
        int num_of_runs = Integer.parseInt(num_runs);
        String big_aco_num_ants = args[1];
        int big_aco_num_of_ants = Integer.parseInt(big_aco_num_ants);
        String big_aco_num_iterations = args[2];
        int big_aco_iterations = Integer.parseInt(big_aco_num_iterations);
         String quality_factor_1 = args[3];
        double rule_quality_factor1 = Double.parseDouble(quality_factor_1);
        String quality_factor_2 = args[4];
        double rule_quality_factor2 = Double.parseDouble(quality_factor_2);
         String threshold_variable = args[5];
        double threshold_variable_double = Double.parseDouble(threshold_variable);
        String big_aco_improvement_factor = args[6];
        double big_aco_improvement_factor_double = Double.parseDouble(big_aco_improvement_factor);
        String big_aco_evap_factor = args[7];
        double big_aco_evap_factor_double = Double.parseDouble(big_aco_evap_factor);
        String period = args[8];
        int period_int = Integer.parseInt(period);

        String small_aco_num_ants = args[9];
        int small_aco_num_of_ants = Integer.parseInt(small_aco_num_ants);
        String small_aco_num_iterations = args[10];
        int small_aco_iterations = Integer.parseInt(small_aco_num_iterations);
        String small_aco_improvement_factor = args[11];
        double small_aco_improvement_factor_double = Double.parseDouble(small_aco_improvement_factor);
        String small_aco_evap_factor = args[12];
        double small_aco_evap_factor_double = Double.parseDouble(small_aco_evap_factor);
        String voting = args[13];
        boolean boolean_voting = Boolean.parseBoolean(voting);

        Program.createDirectories();
        String data_set_path=Program.getDataSetFolder();
        File data_set_folder=new File(data_set_path);
        File [] data_set_files = data_set_folder.listFiles();
        String rule_set_path=Program.getRuleSetFolder();
        File rule_set_folder=new File(rule_set_path);
        File [] rule_set_files=rule_set_folder.listFiles();
        int size_of_output = data_set_files.length;
        double [] accuracies_array = new double [size_of_output];
        String [] file_names = new String [size_of_output];
        Long duration;
        for(int j=0; j<num_of_runs; j++)
        {
         String path=Program.getOutputFolder();
         path=path+"/Run"+j;
         File current_run_folder= new File(path);
         current_run_folder.mkdir();
         String ruleset_path = path+"/global rule sets";
            File global_ruleset_folder= new File(ruleset_path);
                global_ruleset_folder.mkdir();
         String plot_output_path = path+"/Plot Results";
             File plot_file = new File(plot_output_path);
             if(!plot_file.exists())
                    plot_file.mkdir();
        //String matrix_path = path+"/ConfusionMatrices";
            // File matrix_file = new File(matrix_path);
             //if(!matrix_file.exists())
              //      matrix_file.mkdir();
            for(int i=0; i<data_set_files.length;i++)
        {
                System.out.println("inside for(int i=0; i<data_set_files.length;i++)");
            File current_data_set=data_set_files[i];
           BigAco big_aco = new BigAco(big_aco_num_of_ants,small_aco_num_of_ants, big_aco_iterations,small_aco_iterations, rule_set_files,
                   current_data_set, rule_quality_factor1, rule_quality_factor2,threshold_variable_double,boolean_voting);
           System.out.println("After creating the Big ACO object");
            big_aco.runBigAco(big_aco_improvement_factor_double,small_aco_improvement_factor_double,big_aco_evap_factor_double,
                    small_aco_evap_factor_double,period_int);
            System.out.println("After run big ACO");
            RuleSet final_ruleset = big_aco.getGlobalRuleSet();
            RuleSet complete_ruleset = big_aco.getCompleteRuleSet();
            String ruleset_name = final_ruleset.getFileName();
            outputRulesetFiles(final_ruleset,ruleset_path+"/"+ruleset_name);
            outputRulesetFiles(complete_ruleset,ruleset_path+"/C"+ruleset_name);
            //outputConfusionMatrixFiles(final_ruleset,matrix_path+"/"+i+ruleset_name);
            file_names[i]=ruleset_name;
            LinkedList plot_list=big_aco.getOutputList();
            String current_plot_path = plot_output_path+"/"+ruleset_name;
            outputPlotResults(plot_list,period_int,current_plot_path);
            double ruleset_accuracy = final_ruleset.getAccuracy();
            accuracies_array[i]=ruleset_accuracy;
        }
         String output_path = path+"/Final Results.txt";
         outputResults(file_names,accuracies_array, output_path);
        }
   
        System.out.println("The code ended!!"); Long end_time = System.currentTimeMillis();
        duration = end_time-start_time;
        duration = duration/60000;
        outputTime(duration,Program.getOutputFolder()+"/Time.txt");
        System.out.println("Duration in minutes: "+duration);
    }

    public static void outputRulesetFiles(RuleSet rule_set,String path)
    {
        try
        {
            FileWriter fw = new FileWriter(path);
            BufferedWriter bw = new BufferedWriter(fw);
            String rule_set_str= rule_set.toString();
            bw.write("------------------");
            bw.newLine();
            bw.write("Processing tree");
            bw.newLine();
            bw.write("Final rules from tree:");
            bw.newLine();
            bw.write(rule_set_str);
            bw.close();
        }
        catch(IOException e)
        {
            System.out.println("IOERROR!!! in output rule set files!!!"+e.getLocalizedMessage());
        }
    }

     public static void outputConfusionMatrixFiles(RuleSet rule_set,String path)
    {
        try
        {
            FileWriter fw = new FileWriter(path);
            BufferedWriter bw = new BufferedWriter(fw);
            int [][] matrix = rule_set.getConfusionMatrix();
            for(int i=0; i<matrix.length; i++)
                for(int j=0; j<matrix[i].length; j++)
                    bw.write("Location "+i+"-"+j+": "+matrix[i][j]+"\n");
                        bw.close();
        }
        catch(IOException e)
        {
            System.out.println("IOERROR!!! in output matrix files!!!"+e.getLocalizedMessage());
        }
    }

     public static void outputTime(Long time,String path)
    {
        try
        {
            FileWriter fw = new FileWriter(path);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(time+" minutes");
                        bw.close();
        }
        catch(IOException e)
        {
            System.out.println("IOERROR!!! in output time!!!"+e.getLocalizedMessage());
        }
    }

    public static void outputResults(String [] file_names, double [] accuracies,String output_path)
    {
        try
        {
            FileWriter fw = new FileWriter(output_path);
            BufferedWriter bw = new BufferedWriter(fw);
            
            bw.write("FINAL RESULTS");
            bw.newLine();
            double sum =0;
            for(int i=0; i<file_names.length; i++)
            {
                String current_name = file_names[i];
                String current_accuracy = accuracies[i]+"";
                sum=sum+accuracies[i];
                String line = current_name+":\t"+current_accuracy;
                bw.write(line);
                bw.newLine();
            }
            double average_accuracy = sum/accuracies.length;
            String average_line = "Average Accuracy:\t"+average_accuracy;
            bw.write(average_line);
            bw.close();
        }
        catch(IOException e)
        {
            System.out.println("IOERROR!!!");
        }
    }

    public static void outputPlotResults(LinkedList output_list, int period, String output_path)
    {
        try
        {
            FileWriter fw = new FileWriter(output_path);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("Plot Results\n\n");
            int num_of_iterations=0;
            for(int i=0; i<output_list.size(); i=i+6)
            {
                bw.write("Results after iteration: "+num_of_iterations+"\n");
                bw.write("----------------------------------------\n");
                bw.write("Accuracy:"+output_list.get(i)+"\n");
                bw.write("J-index:"+output_list.get(i+1)+"\n");
                bw.write("Precision:"+output_list.get(i+2)+"\n");
                bw.write("Recall:"+output_list.get(i+3)+"\n");
                bw.write("Sensitivity:"+output_list.get(i+4)+"\n");
                bw.write("Specificity:"+output_list.get(i+5)+"\n\n");
                num_of_iterations = num_of_iterations+period;
            }
            bw.close();
        }
        catch (IOException ex)
        {
           System.out.println("IO exception in PLOT output"+ex.getLocalizedMessage());
        }
    }
}
