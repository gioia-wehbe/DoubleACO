//******************************************************************************
//Name:Gioia Wehbe.
//Student's number: 200801378.
//Project Name: Double ACO
//Last modified on: 3/12/2011
//Describtion:an ADT to create Rule Set objects. It also includes methods
//to compute all the parameters related to a ruleset (accuracy,precision...)
//******************************************************************************


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class RuleSet
{
    private LinkedList rules=new LinkedList();  //List of rules
    private int size;     //Size of the rule set = number of Rules including the class label
    private double accuracy;                                                     
    private double precision;                                                      
    private double recall;
    private double j_index;
    private double specificity;
    private double sensitivity;
    private double pheromone;                                                       
    private double threshold_quality;   //The rule quality value above which a rule is considered a good one.
    private boolean voting;
    private String [] labels; //array of class labels corresponding to the rule set
    private File ruleset_file;   //file from which the rule set is extracted...
    private String file_name;   //the name of this file
    private double rule_quality_factor1, rule_quality_factor2, threshold_variable; //constants inputed as parameters in order to varry the value of rule quality and threshold quality
    private double dataset_num_cases;   //number of cases (rows/lines) in the dataset
    private static String DEFAULT_CLASS_PREFIX="Default class: ";// Prefix to add to the line that shows the default class label. All objects of class RuleSet share it.
    private double primary_threshold_quality;//a preliminary threshold value upon which the final threshold value is chosen
    private Parser parser = new Parser();   //the object used to parse the rule set text files into rule set objects
    private File data_set_file;
    private int[][] confusion_matrix;

    //--------------------------------------------------------
    // Class Constructors
    //--------------------------------------------------------
    public RuleSet(LinkedList rules,int size, int [][]confusion_matrix, double accuracy, double precision, double recall,double j_index, 
            double spacificity, double sensitivity,  double pheromone, double threshold_quality,  String [] labels,/* TestRuleSet test_rule_set,*/ File ruleset_file,
            String file_name,double rule_quality_factor1,double rule_quality_factor2,double threshold_variable, double
            dataset_num_cases, double primary_threshold_quality, Parser parser, File data_set_file,  int[][] IndiMatrix, boolean voting)
    {//Constructor used to create copies of a rule set object
        this.accuracy=accuracy;
        this.confusion_matrix=confusion_matrix;
        this.data_set_file=data_set_file;
        this.dataset_num_cases=dataset_num_cases;
        this.file_name=file_name;
        this.labels=labels;
        this.parser=parser;
        this.pheromone=pheromone;
        this.precision=precision;
        this.primary_threshold_quality=primary_threshold_quality;
        this.recall=recall;
        this.j_index=j_index;
        this.specificity=spacificity;
        this.sensitivity=sensitivity;
        this.rule_quality_factor1=rule_quality_factor1;
        this.rule_quality_factor2=rule_quality_factor2;
        this.rules=rules;
        this.ruleset_file=ruleset_file;
        this.size=size;
        this.threshold_quality=threshold_quality;
        this.threshold_variable=threshold_variable;
        this.confusion_matrix = IndiMatrix;
        this.voting=voting;
    }

    public RuleSet(File ruleset_file, int file_index, File data_set_file, double rule_quality_factor1, double rule_quality_factor2, double threshold_variable)
    {//Generic constructor used to create rule set objects
        this.ruleset_file=ruleset_file;
        this.rule_quality_factor1=rule_quality_factor1;
        this.rule_quality_factor2=rule_quality_factor2;
        this.threshold_variable = threshold_variable;
        this.file_name = ruleset_file.getName();
        initializeRulesList(ruleset_file);
        initializeLabels();
        size = rules.size();
        this.data_set_file=data_set_file;
        int Matrix_Size = labels.length;
        confusion_matrix= new int[Matrix_Size][Matrix_Size];
        initializeRuleSetVars();
    }

     public RuleSet(LinkedList rules, File data_set_file, double rule_quality_factor1, double rule_quality_factor2, double threshold_variable, String file_name)
    {//contructor used to create a rule set by passing a predefined list of rules as a parameter
         //it is used to construct the global rule set
        this.rules=rules;
        this.rule_quality_factor1=rule_quality_factor1;
        this.rule_quality_factor2=rule_quality_factor2;
        this.threshold_variable = threshold_variable;
        this.data_set_file=data_set_file;
        this.file_name=file_name;
        initializeLabels();
        size = rules.size();
        int Matrix_Size = labels.length;
        confusion_matrix= new int[Matrix_Size][Matrix_Size];
        initializeRuleSetVars();
    }

     //--------------------------------------------------------
     // A method used to create a copy of a rule set object
     //--------------------------------------------------------

    public RuleSet duplicate()
    {
        LinkedList new_rules = new LinkedList();
        for(int i=0; i<rules.size(); i++)
        {
            Rule rule = (Rule) rules.get(i);
            Rule new_rule = rule.duplicate();
            new_rules.add(new_rule);
        }
        RuleSet copy = new RuleSet(new_rules,size, confusion_matrix,  accuracy, precision,  recall,
                j_index, specificity,sensitivity,pheromone,threshold_quality, labels, ruleset_file,
                file_name, rule_quality_factor1, rule_quality_factor2,threshold_variable,
                dataset_num_cases, primary_threshold_quality, parser, data_set_file,confusion_matrix,voting);
        return copy;
    }

    //-----------------------------------------------------
    //Getter methods
    //-----------------------------------------------------

    public LinkedList getRules()
    {
        return rules;
    }

    public int getLongestRuleSize()
    {//returns the size of the longest rule in the rule set.
        Rule R;
        int length, max_length =0;
        for(int i=0; i<size; i++)
        {
            R= (Rule)(rules.get(i));
            length=R.getLength();
            if(length>max_length)
                max_length=length;
        }
        return max_length;
    }

    public File getDataSetFile()
    {
        return data_set_file;
    }
    
    public double getAccuracy()
    {
        return accuracy;
    }
    
     public double getPrecision()
    {
        return precision;
    }

    public double getRecall()
    {
        return recall;
    }
    
    public double getJindex()
    {
        return j_index;
    }
    
    public double getSpecificity()
    {
        return specificity;
    }
    
    public double getSensitivity()
    {
        return sensitivity;
    }

    public double getPheromone()
    {
        return pheromone;
    }

    public int getSize()
    {
        return size;
    }
    
    public double getThresholdQuality()
    {
        return threshold_quality;
    }

    public File getFile()
    {
        return ruleset_file;
    }
    
    public boolean getVoting()
    {
        return voting;
    }

    public Object getElementAt(int rule_index, int cond_index)
    {//return the element (condition or label) of index: "cond_index",
     //that is in rule number: "rule_index".
     //If this element doesn't exist,it returns null.
        Object element;
        if((rule_index<rules.size())&&(rule_index>=0))
        {
            Rule R = (Rule)rules.get(rule_index);
            if((cond_index<R.getConditions().size())&&(cond_index>=0))
            {
                element = (R.getConditions()).get(cond_index);
                return element;   //it is either a condition or a label or null
            }
        }
        return null;//if there is no such rule or condition, return null.
    }

     public String [] getLabels()
    {//returns the array of all possible labels used in the rule set.
        return labels;
    }

     public int [][] getConfusionMatrix()
    {
        return confusion_matrix;
    }


//      public TestRuleSet getTestRuleSet()
//    {
//          return test_rule_set;
//      }

    //--------------------------------------------------------------------------
    //Setter methods
    //--------------------------------------------------------------------------

      public void setRules(LinkedList rules)
    {
        this.rules=rules;
    }

    public void setAccuracy(double accuracy)
    {
        this.accuracy=accuracy;
    }

     public void setPrecision(double precision)
    {
        this.precision=precision;
    }

    public void setRecall(double recall)
    {
        this.recall=recall;
    }

    public void setPheromone(double pheromone)
    {
        this.pheromone=pheromone;
    }

    public void setSize(int size)
    {
        this.size=size;
    }

    public void setThresholdQuality(double threshold_quality)
    {
        this.threshold_quality=threshold_quality;
    }

     public void setFile(File ruleset_file)
    {
        this.ruleset_file=ruleset_file;
    }

     public String getFileName()
    {
         return file_name;
     }

      public void setFileName(String file_name)
    {
         this.file_name=file_name;
     }
      
      public void setVoting(boolean voting)
    {
         this.voting=voting;
     }

     public void setLabels(String [] labels)
    {//returns the array of all possible labels used in the rule set.
        this.labels=labels;
    }

     public void setElementAt(int rule_index, int cond_index, Object element)
    {//Replaces element number 'cond_index' (whether it's a condition or a label) to the "element"
     //parameter passed to the methods as arguement
        Rule R = (Rule)rules.get(rule_index);
        R.getConditions().set(cond_index, element);       
        if(element instanceof String )
        {//If the element is a class label
            R.setClassLabel((String)element);
        }
    }

     public void removeRule(Rule rule)
    {
         rules.remove(rule);
         size=rules.size();
     }


    //--------------------------------------------------------------------------
    //  Initialization Methods
    //--------------------------------------------------------------------------

      private void initializeLabels()
    {//initializes the labels list from the text file "classes" which is an input file
        String labels_path = Program.getClassification();
        File labels_file = new File(labels_path);
        LinkedList labels_list = new LinkedList();
        try
        {
        Scanner scan = new Scanner(labels_file);
        while(scan.hasNext())
        {
           String line = scan.next();
           labels_list.add(line);
        }
        labels=new String [labels_list.size()];
        for(int i = 0; i<labels.length; i++)
        {
            labels[i]=(String)labels_list.get(i);
        }
        }
        catch(IOException e)
        {
            System.out.println("io exception in initialize labels");
        }
    }

    private void initializeRulesList(File rule_set_file)
    {//Initializes the "rules" list of all rules using parser class methods
        String path = rule_set_file.getPath();
        String file_name = rule_set_file.getName();
        LinkedList rule_set = parser.ruleSet(path);
        if(rule_set!=null)
        {
        String [] condition,rule;
        String operand,operator,value,class_label, default_label;
        Condition cond_obj;
        Rule rule_obj,default_rule;
        for(int i=0; i<rule_set.size()-1; i++)
        {
            rule = parser.getRule(rule_set, i);
            LinkedList conditions = new LinkedList();
            for(int j=0; j<rule.length-1;j++)//initialize conditions List
            {
                condition = parser.getCondition(rule, j);
                operand = parser.getOperand(condition);
                operator = parser.getOperator(condition);
                value = parser.getValue(condition);
                cond_obj = new Condition (operand,operator,value);
                cond_obj.setPheromone(0);
                conditions.add(cond_obj);
            }
            class_label=parser.getClassLabel(rule);
            rule_obj=new Rule(conditions,class_label);
            rule_obj.setLabelPheromone(0);
            rules.add(rule_obj);
            }
        default_label = parser.getDefaultClassLabel(rule_set);
        LinkedList empty_cond= new LinkedList();
        default_rule = new Rule(empty_cond,default_label);
        rules.add(default_rule);
        }
    }

    public void initializeRuleSetVars()
    {//This method performs all the initializations of rule set parameters that
        //require its evaluation against the data set fold (Confusion matrix,Accuracy,Coverage...)

        //Initialize all rule vars
        for(int i=0; i<rules.size()-1; i++)
        {
            //initializations corresponding to rules...
            Rule rule = (Rule)rules.get(i);
            computeRuleConfusionArray(rule);
            computeRuleCoverage(rule);  
            computeRuleAccuracy(rule);  
            computeRuleQuality(rule);  
        }
            computePrimaryThresholdQuality(threshold_variable);  
            primarySelectGoodRules();
            computeThresholdQuality(threshold_variable);
            selectGoodRules();
            sortRules();
        computeConfusionMatrix(voting);   
        computeAccuracy(confusion_matrix);      
        computeJindex(confusion_matrix);
        if(labels.length==2)
        {
             computePrecision(confusion_matrix);
             computeRecall(confusion_matrix);
             computeSpecificity(confusion_matrix);
             computeSensitivity(confusion_matrix);
        }
        else
            {
                precision = -1;
                recall = -1;
                specificity = -1;
                sensitivity = -1;
            }        
    }
    
   

    //--------------------------------------------------------------------------
    //Computational methods                         
    //--------------------------------------------------------------------------

     
     //Rule set computations
     //-------------------------------------------

    public void resetConfusionMatrix()
    {//borrowed from Khaled's code
         for(int i=0; i<confusion_matrix.length; i++)
             for(int j=0; j<confusion_matrix[i].length; j++)
                 confusion_matrix[i][j]= 0;
     }

    
    public void computeConfusionMatrix(boolean voting)
    {//Borrowed from Khaled's code
        try
        {
          resetConfusionMatrix();
           Scanner scan= new Scanner(data_set_file);
           scan.useDelimiter("\n");
           while (scan.hasNext())
           {
               String line = scan.next().trim();
               if(!line.isEmpty())
               {
                   if(voting==false)
                        test(line);
                   else if(voting==true)
                       votingTest(line);
               }
           }
     }
        catch(IOException e)
        {
            System.out.println("exception in computeConfusionMatrix!!!, file not found"+e.getLocalizedMessage());
        }
    }

    public void test(String line)
    {//Borrowed from Khaled's code
          Rule r;
          int classification=0;
          boolean match= false;
          for(int i=0; i<rules.size()-1; i++)
          {
              r= (Rule)rules.get(i);
              match= r.test(line);
              if(match== true)
              {
                  classification= Integer.parseInt(r.getClassLabel());
                  i=rules.size()-1; //break out of loop
              }
               if(!match)
             {
                   String default_class = ((Rule)rules.get(rules.size()-1)).getClassLabel();
              classification= Integer.parseInt(default_class);
              }
          }
           int LineClass= getLineClass(line);
           confusion_matrix[LineClass][classification]+= 1;
    }
    
    public void votingTest(String line)
    {
          Rule r;
          int classification=0;
          boolean match= false;
          int [] class_voting=new int [2];
          for(int i=0; i<rules.size()-1; i++)
          {
              r= (Rule)rules.get(i);
              match= r.test(line);
              if(match==true)
              {
                  classification= Integer.parseInt(r.getClassLabel());
                  class_voting[classification]++;
              }
          }
          if((class_voting[0]==0)&&(class_voting[1]==0))
          {
              String default_class = ((Rule)rules.get(rules.size()-1)).getClassLabel();
              classification= Integer.parseInt(default_class);
          }
          else
          {
              if(class_voting[0] > class_voting[1])
                  classification=0;
              else if(class_voting[1]> class_voting[0])
                  classification=1;
              else if(class_voting[0]==class_voting[1])
              {
                  Random rand = new Random();
                  int choice = rand.nextInt(2);
                  classification=choice;
              }
          }
           int LineClass= getLineClass(line);
           confusion_matrix[LineClass][classification]+= 1;
    }

     private int getLineClass(String line)
    {//Borrowed from Khaled's code
          Scanner scan= new Scanner(line);
          scan.useDelimiter(",");
          int temp= 0;
           while(scan.hasNext())
            {
                temp++;
                scan.next();
           }
          scan= new Scanner(line);
          scan.useDelimiter(",");
          for(int k=0; k<temp-1; k++)
                     scan.next();
              return Integer.parseInt(scan.next().trim());
          }

      public void computeAccuracy(int[][] Matrix)
    {//Borrowed from Khaled's code
              double sum1=0, sum2=0;
              for(int i=0; i<Matrix.length; i++)
              {
                  sum1 += Matrix[i][i];
                  for(int j=0; j<Matrix[i].length; j++)
                    sum2 += Matrix[i][j];
              }
              if(sum2==0)
                  accuracy = 0;
              else
                accuracy = sum1/sum2;
    }


    public void computePrecision(int[][] Matrix)
    {//Borrowed from Khaled's code
     if(Matrix[0].length==2)
        {
            if((Matrix[0][0]+Matrix[0][1])==0)
              {
                precision=0;
            }
            else
            {
                precision= Matrix[0][0]/((Matrix[0][0]+Matrix[0][1])*1.0);

            }
        }
    else
        precision= -1;
    }

     public void computeRecall(int[][] Matrix)
    {//Borrowed from Khaled's code
        if(Matrix[0].length==2)
        {
            if((Matrix[0][0]+Matrix[1][0])==0)
            {
                recall=0;
            }
            else
            {
                recall= Matrix[0][0]/((Matrix[0][0]+Matrix[1][0])*1.0);
            }
        }
    else
        recall= -1;
    }

     public void computeSpecificity(int[][] Matrix)
    {//Borrowed from Khaled's code
        if(Matrix[0].length==2)
        {
            if((Matrix[1][1]+Matrix[0][1])==0)
                specificity=0;
            else
                specificity= Matrix[1][1]/((Matrix[1][1]+Matrix[0][1])*1.0);
        }
    else
        specificity= -1;
    }

     public void computeSensitivity(int[][] Matrix)
    {//Borrowed from Khaled's code
        if(Matrix[0].length==2)
        {
            if((Matrix[0][0]+Matrix[1][0])==0)
                sensitivity=0;
            else
                sensitivity =Matrix[0][0]/((Matrix[0][0]+Matrix[1][0])*1.0);
        }
    else
        sensitivity= -1;
    }

      public void computeJindex(int[][] Matrix)
    {//Borrowed from Khaled's code
              double denomenator= 0.0;
              double k= Matrix.length*1.0;
              double sum=0.0;
              for(int i=0; i<Matrix.length; i++)
              {
                  for(int j=0; j<Matrix.length; j++)
                    denomenator+= Matrix[i][j];
                  if(denomenator ==0)
                      sum=0;
                  else
                    sum+= Matrix[i][i]/denomenator;
                denomenator=0;
              }
              if(k==0)
                  j_index=0;
              else
                j_index= sum/k;
          }

    //Rule computations                       
    //---------------------------------------------

      public boolean classifies(int rule_index,String data_set_line)
    {//Borrowed from Khaled's code
         Rule rule=(Rule)rules.get(rule_index);
         if(rule_index<rules.size()-1)
            return rule.test(data_set_line);
         else
         {
             return false;

             }
     }

      public boolean correctlyClassifies(int rule_index,String data_set_line)
    {//Makes sure that the attributes, as well as the label of a certain case maches the rule
         Rule rule=(Rule)rules.get(rule_index);
         boolean match=rule.test(data_set_line);
         int rule_class=Integer.parseInt(rule.getClassLabel());
         int case_class= getLineClass(data_set_line);
         boolean result=false;
         if((match)&&(rule_class==case_class))
             result= true;
         if((match)&&(rule_class!=case_class))
             result = false;
         return result;
     }

     public void computeRuleConfusionArray(Rule rule)
    {//Initializes an array of size 3       
         //index=0  ---->  The number of cases correctly classified by this rule
         //index=1  ---->  The number of cases classified but not correctly by this rule (different class label)
         //index=2  ---->  The number of cases not classified at all by this rule (attribute do not match)
         double [] confusion_array = new double[3];
         try{  //rule whether correctly or not
         int rule_index = rules.indexOf(rule);
         String case_line="";
         double num_cases=0;//total number of cases in the dataset
         Scanner scan= new Scanner(data_set_file);
         scan.useDelimiter("\n");
         while (scan.hasNext())
         {
             case_line=scan.next().trim();
             if(!case_line.isEmpty())
             {
                 num_cases++;
                 if(!(classifies(rule_index, case_line)))
                     confusion_array[2]++;//doesn't classify at all (no match at all
                 else
                 {
                     if(correctlyClassifies(rule_index, case_line))
                         confusion_array[0]++;//correctly classifies
                     else
                         confusion_array[1]++;//wrongly classifies
                 }
             }
         }
         rule.setConfusionArray(confusion_array);
         dataset_num_cases=confusion_array[0]+confusion_array[1]+confusion_array[2];
         }
         catch(Exception e)
         {
             System.out.println("exception in computeRuleConfusionArray!!!"+e.getMessage());
         }
    }
     
     public void computeRuleCoverage(Rule rule)
    {  //Computes the rule coverage accordiing to the rule confusion array
         double [] confusion_array=rule.getConfusionArray();
         double classifications = confusion_array[0]+confusion_array[1];
         double coverage = classifications/dataset_num_cases;
         rule.setCoverage(coverage);
     }

     public void computeRuleAccuracy(Rule rule)
    {  //Computes rule accuracy according to the rule confusion array
         double [] confusion_array=rule.getConfusionArray();
         double correct_classifications = confusion_array[0];
         double total_classifications = confusion_array[0]+confusion_array[1];
         if(total_classifications==0)
             total_classifications=1;//to replace the NaN possible output by 0!!!                       
         double rule_accuracy = correct_classifications/total_classifications;
         rule.setAccuracy(rule_accuracy);
     }
    
    public void computeRuleQuality(Rule rule)
    {//Computes the rule Quality according to rule accuracy, rule coverage & two
      //factors inputed as parameters
       double quality = rule_quality_factor1*rule.getAccuracy()+ rule_quality_factor2*rule.getCoverage();
       rule.setQuality(quality);
    }

    public void computePrimaryThresholdQuality(double variable)
    {//initializes the preliminary threshold Quality as a function of the average Quality of all rule sets
        //multiplied by a parameter variable
        double rule_quality=0;
        double total_quality=0;
        for(int i = 0; i<rules.size()-1; i++)
        {
            Rule rule=(Rule)rules.get(i);
            rule_quality=rule.getQuality();
            total_quality+=rule_quality;
        }
        double average_quality=total_quality/(rules.size()-1);
        primary_threshold_quality=variable*average_quality;
    }

     public void computeThresholdQuality(double variable)
    {//initializes the threshold Quality as a function of the average Quality of GOOD Rules
         // multiplied by a parameter varialble
        double rule_quality=0;
        double total_quality=0;
        int num_good_rules=0;
        for(int i = 0; i<rules.size()-1; i++)
        {

            Rule rule=(Rule)rules.get(i);
            if(rule.isGood())
            {
                 num_good_rules++;
                 rule_quality=rule.getQuality();
                 total_quality+=rule_quality;
            }

        }
        double average_quality=total_quality/num_good_rules;
        threshold_quality=variable*average_quality;
    }

      public void primarySelectGoodRules()
    {//it compare the getQuality of each rule to the threshold getQuality.
        for(int i=0; i<rules.size(); i++)
        {  //System.out.println("inside loop of select good rules");
            Rule rule = (Rule)rules.get(i);
            if(rule.getQuality()<primary_threshold_quality)
                rule.setIsGood(false);//if it is less than threshold it sets its is_good parameter to false
            else
                 rule.setIsGood(true);//else it sets it to true
        }
    }

    public void selectGoodRules()
    {//it compare the getQuality of each rule to the threshold getQuality.
        for(int i=0; i<rules.size(); i++)
        {  //System.out.println("inside loop of select good rules");
            Rule rule = (Rule)rules.get(i);
            if(rule.getQuality()<threshold_quality)
                rule.setIsGood(false);//if it is less than threshold it sets its is_good parameter to false
            else
                 rule.setIsGood(true);//else it sets it to true
        }
    }
    
     public void sortRules()
    {
        try
        {
//            FileWriter fr= new FileWriter("Output/"+file_name);
//            BufferedWriter br = new BufferedWriter(fr);
//            br.write("Before Soting\n");
//            br.write(this.toString()+"\n");
            Collections.sort(rules.subList(0, size-1));
//            br.write("After Soting\n");
//            br.write(this.toString()+"\n");
//            br.close();
        }
        catch(Exception e)
        {
            System.out.println("Exception in sorting: "+e.getLocalizedMessage());
        }
    }

    //Condition Computations
    //----------------------------------------------------

    public void computeConditionCutPoints(Condition cond)
    {//initializes the cutpoints list of the conditions...
        LinkedList sorted_rules = new LinkedList();
        String operand = cond.getOperand();
        LinkedList cut_points=new LinkedList();
        for(int i=0; i<rules.size()-1; i++)
        {  
            Rule rule = (Rule)rules.get(i);//get first rule
            Condition atr_cond=rule.hasAttribute(operand);//select the condition with the same attribute as the
                                                            //indicated condition if it exists
            if(atr_cond!=null)
            {//if the condition with the same attribute of the parameter condition is found in the rule...
                double value = Double.parseDouble(atr_cond.getValue());//get the getValue of the 
                                                                        //condition with that attribute
                if (sorted_rules.isEmpty())
                {//if the list is still empty add the Value to the sorted_rules list
                    sorted_rules.add(rule);
                }
                else
                {//if the list already has some getRules, compare the current rule's 
                    //Value and insert it in the right place to sort the Rules
                     for(int j=0; j<sorted_rules.size();j++)
                {
                    Rule current_rule=(Rule)sorted_rules.get(j);
                    Condition current_cond = current_rule.hasAttribute(operand);
                    Double current_val = Double.parseDouble(current_cond.getValue());
                    if(current_val>=value)
                    {//if we find a rule with graeter or equal getValue to the current rule's getValue... 
                        //insert the current rule before the
                        sorted_rules.add(j, rule);//rule found in the list...
                        break;//and then go to another rule to sort...
                    }
                    else if(j==sorted_rules.size()-1)
                    {//if we reach end os sorted list in comparisons, this means that the current rule has the 
                        //largest getValue and we then add it to to the end of the sorted list...
                        sorted_rules.add(rule);
                    }
                }
                }
            }
        }
        for(int i=0; i<sorted_rules.size(); i++)
        {//then we itterate through the sorted getRules in increasing order...
            if(i!=(sorted_rules.size()-1))
            {//if we haven't reached the rule before the last one...
                Rule rule1=(Rule)sorted_rules.get(i);//get the current rule ant index i
                Rule rule2=(Rule)sorted_rules.get(i+1);//and get the rule directly after it at index i+1
                String label1=rule1.getClassLabel();//get the first rule's class label...
                String label2=rule2.getClassLabel();// and get the second rule's class label...
                if(!(label1.equals(label2)))
                {//if they are different i.e: we encountered a shift in label (a cut pint)
                    Condition cond1=rule1.hasAttribute(operand);// then get the first rule's condition with the specified attribute...
                    Condition cond2= rule2.hasAttribute(operand);// and get the 2nd rule's condition with the specified attribute...
                    Double val1=Double.parseDouble(cond1.getValue());//get the firs't condition's getValue....
                    Double val2=Double.parseDouble(cond2.getValue());// get the 2nd condition's getValue...
                    double cut_point=(val1+val2)/2;//calculate the cutpoint getValue which is the average of the two previous values...
                    if(cut_points.isEmpty())
                        cut_points.add(cut_point); //add the obtained cutpoint to the cutpoints list... an check the following rule and the one after it, similarilly...
                    else if(!cut_points.contains(cut_point))
                         cut_points.add(cut_point);
                }                              
            }
        }
        cond.setCutpoints(cut_points);//initialize the Condition obj attribute "cutpoints" to the obtained list...
    }

    public void computeConditionValueArray(Condition cond)
    {//in the case of discrete attributes... combine all possible discrete values in one list...
       LinkedList values = new LinkedList();
        String attribute=cond.getOperand();
        for (int i=0; i<rules.size(); i++)
        { 
            Rule rule = (Rule)rules.get(i);
            Condition hit_cond = rule.hasAttribute(attribute);//condition with the same attribute as the specified one...
            if(hit_cond!=null)
            {//if the hit-condition exists...
                String value = hit_cond.getValue();//get its Value...
                if(values.isEmpty())
                    values.add(value);//add it to the values list...
                else if(!values.contains(value))
                {
                    values.add(value);
                }

            }
        }
        cond.setValues(values);//initialize the values list in the Condition obj...
    }

    //--------------------------------------
    // Printing Methods
    //--------------------------------------
    
    public String createRuleSetFormat()
        {
            String rule_set="";
            if(rules!=null)
                {
                    for (int i=0; i<rules.size()-1; i++)
                    {
//                        Rule rule =(Rule)rules.get(i);
                        //double [] confusion_array = rule.getConfusionArray();
                        rule_set=rule_set+"Rule "+i+":\n"+((Rule)rules.get(i)).toString();
                        //rule_set = rule_set + "confusion array: "+confusion_array[0]+" - "+confusion_array[1]+" - "+
                         //           confusion_array[2]+"\n";
                    }
                }
            String default_class="";
            if(size!=0)
                default_class = ((Rule)rules.get(size-1)).getClassLabel();
            rule_set=rule_set+DEFAULT_CLASS_PREFIX+default_class+"\n";
            return rule_set;
        }

        @Override
         public String toString()
         {//it overrides the toString method in order to print the ruleset obj in the correct format
            return createRuleSetFormat();
         }
}
