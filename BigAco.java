//******************************************************************************
//Name:Gioia Wehbe.
//Student's number: 200801378.
//Project Name: Double ACO
//Last modified on: 3/12/2011
//Describtion: A class that performs all the tasks of the Big ACO such as perturb
// that calls the small ACO. It creates the final out put which is the global rule
// set.
//******************************************************************************

import java.io.*;
import java.util.*;

public class BigAco
{
    private RuleSet global_rule_set;// the final rule set extracted by com binig good rules from all the rule sets
    private SmallAco[][] matrix; //the big matrix - it holds the rule sets that the ACO will modify
    private int matrix_width; //initialized with matrix
    private int matrix_height;//initialized with matrix
    private SmallAco small_aco;//used to perturb the rule set
    private double optimal_objective_function;//accuracy of the global rule set
    private LinkedList locations;//initialize randomly. size = num_of_ants
    private int [] march_kinds;
    private LinkedList updated_locations=new LinkedList(); //holds all the locations that have getPheromone.

   

    //parameters input before
     private int big_aco_num_of_ants;
     private File[] rule_sets;//stores the rule sets input at run time. How are we going to specify this?
     private int big_aco_iterations;//number of iterations
     //Ant characteristics
     private int [] current_location; // always of size 2. It keeps track of the i,j indices of the current ant
     private int current_march_kind;
     private double rule_quality_factor1,rule_quality_factor2, threshold_variable;
     private File data_set_file;
     private LinkedList output_list;
     private RuleSet complete_ruleset;
     private boolean voting;

     //-------------------------
     // Constructor
     //-------------------------

     public BigAco(int big_aco_num_of_ants, int small_aco_num_of_ants, int big_aco_iterations,
             int small_aco_iterations,File[] rule_sets, File data_set_file, double rule_quality_factor1,
             double rule_quality_factor2,double threshold_variable, boolean voting)
    {
     global_rule_set = null;
     small_aco=null;
     optimal_objective_function=0;
     this.big_aco_num_of_ants=big_aco_num_of_ants;
     this.big_aco_iterations=big_aco_iterations;
     this.rule_sets=rule_sets;
     this.rule_quality_factor1=rule_quality_factor1;
     this.rule_quality_factor2=rule_quality_factor2;
     this.threshold_variable=threshold_variable;
     this.data_set_file=data_set_file;
     this.voting=voting;
     initializeMatrixDimensions();
     initializeLocations();
     initializeMarchKinds();
     initializeMatrix(rule_sets, small_aco_num_of_ants, small_aco_iterations);
     global_rule_set=initializeGlobalRuleSet(matrix, rule_quality_factor1, rule_quality_factor2,  threshold_variable);
     optimal_objective_function = global_rule_set.getAccuracy();
     output_list=new LinkedList();
     complete_ruleset = null;
    }//BigAco()


     public void runBigAco(double big_aco_improvement_factor, double small_aco_improvement_factor, double big_aco_evaporation_rate,
              double small_aco_evaporation_rate, int period)
    {//initializes the original global rule set's values and starts building from it new solutions
        for(int j=0; j<big_aco_iterations; j++)
        {
            System.out.println("Iteration number "+j+" of the Big ACO");
            for(int i=0; i<big_aco_num_of_ants;i++)
            {
                System.out.println("Ant number "+i+" of the Big ACO");
                current_location = (int[])locations.get(i);
                current_march_kind=march_kinds[i];
                buildSolution(small_aco_improvement_factor,big_aco_improvement_factor,small_aco_evaporation_rate);
                locations.set(i, current_location);
                march_kinds[i]=current_march_kind;
            }
            evaporatePheromone(big_aco_evaporation_rate);
        if(j%period==0)
        {//to store results every 'period' iterations in order to output them later on
        output_list.add(global_rule_set.getAccuracy());
        output_list.add(global_rule_set.getJindex());
        output_list.add(global_rule_set.getPrecision());
        output_list.add(global_rule_set.getRecall());
        output_list.add(global_rule_set.getSensitivity());
        output_list.add(global_rule_set.getSpecificity());
        }
        }
        complete_ruleset = global_rule_set.duplicate();
        pruneRules(global_rule_set);
        output_list.add(global_rule_set.getAccuracy());
        output_list.add(global_rule_set.getJindex());
        output_list.add(global_rule_set.getPrecision());
        output_list.add(global_rule_set.getRecall());
        output_list.add(global_rule_set.getSensitivity());
        output_list.add(global_rule_set.getSpecificity());
    }

     private RuleSet initializeGlobalRuleSet( SmallAco [][] matrix, double rule_quality_factor1, double rule_quality_factor2,
             double threshold_variable)
        {//Selects the good rule from all the input rule sets and creates a global rule set
         LinkedList rules = new LinkedList();
         String [] labels=null;
         double [] label_counter=null;
         for(int i =0; i<matrix_height; i++)
             for(int j=0; j<matrix_width;j++)
             {
                SmallAco current_small_aco =matrix[i][j];                
                 if(current_small_aco!=null)
                 {
                      RuleSet current_rule_set= current_small_aco.getRulSet();
                 LinkedList current_rules = current_rule_set.getRules();
                 labels = current_rule_set.getLabels();
                 label_counter = new double[labels.length];
                 for(int k=0; k<current_rules.size()-1;k++)
                 {
                     Rule rule = (Rule)current_rules.get(k);
                     if(rule.isGood())
                     {
                         rules.add(rule);
                         int label = Integer.parseInt(rule.getClassLabel());
                         label_counter[label]=label_counter[label]+rule.getAccuracy();
                     }
                 }
                 }
             }
         LinkedList conditions = new LinkedList();
          String majority_classifier="";
          double max = label_counter[0];
             int max_label=0;
             for(int j =0; j<label_counter.length; j++)
             {
                 double current=label_counter[j];
                 if(current>max)
                 {
                     max=current;
                     max_label=j;
                 }
             }
             majority_classifier = labels[max_label];
         Rule default_class = new Rule(conditions,majority_classifier );
         rules.add(default_class);
         String file_name = "RS"+data_set_file.getName()+".txt";
         RuleSet global_rule_set = new RuleSet(rules, data_set_file, rule_quality_factor1, rule_quality_factor2,  threshold_variable, file_name);
         global_rule_set.setVoting(voting);
         return global_rule_set;
     }
     
     private void initializeMatrixDimensions()
    {//initializes the matrix dimentions according to the  number of rule sets inputed
         int num_rule_sets=rule_sets.length;
         double square_root= Math.sqrt(num_rule_sets);
         int casted_num=(int)square_root;
         int ceiling = (int)Math.ceil(square_root);
         if(casted_num==ceiling)//if the number is a complete square...
             {
             matrix_width=casted_num;
             matrix_height=casted_num;
            }
         else
         {
             int rounded_num = Math.round((float)square_root);
             matrix_width = rounded_num;
             if(casted_num<rounded_num)
             {
                 matrix_height=matrix_width;
             }
             else if(casted_num==rounded_num)
             {
                 matrix_height=matrix_width+1;
             }
         }
     }

     private void initializeMatrix(File [] rule_sets, int small_aco_num_of_ants, int small_aco_num_of_iterations)
     {//Initialize a small aco object for each input ruleset in each cell of the global matrix
         matrix = new SmallAco [matrix_height][matrix_width];
         for(int i =0; i<matrix_height; i++)
             for(int j=0; j<matrix_width;j++)
             {
                 int index=j+i*matrix_width;
                 if(index<rule_sets.length)
                 {
                     File current_file = rule_sets[index];
                     RuleSet current_rule_set= new RuleSet(current_file, index,
                             data_set_file,rule_quality_factor1, rule_quality_factor2, threshold_variable);
                     current_rule_set.setVoting(voting);
                     SmallAco current_small_aco = new SmallAco(current_rule_set, small_aco_num_of_ants,
                             small_aco_num_of_iterations);
                     current_rule_set.setPheromone(0);
                     matrix[i][j]=current_small_aco;
                 }
            }
    }

     public SmallAco [][] duplicateMatrix(SmallAco [][] matrix)
    {//Makes a copy of the global matrix of all rule sets
         SmallAco [][] matrix_copy=new SmallAco[matrix_height][matrix_width];
         for(int i =0; i<matrix_height; i++)
             for(int j=0; j<matrix_width;j++)
             {
                 SmallAco current_small_aco = matrix[i][j];
                 if(current_small_aco!=null)
                 {
                 SmallAco small_aco_copy=current_small_aco.duplicate();
                 matrix_copy[i][j]=small_aco_copy;
                 }
             }
         return matrix_copy;
     }

     private void initializeLocations()
    {// Initializes the original locations of the big Ants on the global matrix in a random manner
         locations = new LinkedList();
         int [] location=new int [2];
        Random rand = new Random(System.currentTimeMillis());
            for(int i=0; i<big_aco_num_of_ants;i++)
            {
                boolean exists=false;
                do
                {
                location = randLocation(rand);
                if(locations.size()==0)
                    locations.add(i,location);
                else
                {
                    exists=exists(locations, location);
                    if(exists==false)//if the random location is not found in the locations array...
                        locations.add(i, location);
                }
                }while(exists==true);//if the random location already exists in the array...
                //(i.e if there is another ant in this location...)
            }
     }

     private int [] randLocation(Random rand)
    {//returns a random location for an ant according to the number of getRules and there max Size
     //N.B: ant might start form an empty cell
        int cond_index=rand.nextInt(matrix_width);
        int rule_index = rand.nextInt(matrix_height);
        int [] new_location = new int [2];
        new_location[0]=rule_index;
        new_location [1]=cond_index;
        return new_location;
    }

     public boolean exists(LinkedList locations,int [] location)
    {//Checks if a certain location exists in a list of locations
        boolean exists = false;
        for(int j=0; j<locations.size(); j++)
                {  
                    int [] loc =(int[])locations.get(j);
                    if(loc!=null)
                    {
                    if(Arrays.equals(location, loc)==false)
                        exists = false;
                    else
                    {
                         exists = true;
                         break;
                    }
                    }
                }
        return exists;
    }

     private void initializeMarchKinds()
    {//Initializes the original marchkind (direction in other words) of the ants at start
        march_kinds = new int [big_aco_num_of_ants];
         Random rand = new Random(System.currentTimeMillis());
        for(int i=0; i<march_kinds.length; i++)
        {
              int march_kind = rand.nextInt(8)+1;//ants can start with the same march kind!!!
                march_kinds[i]=march_kind;
        }
     }

     //-------------------------------------------------
     // Getter & Setter Methods
     //-------------------------------------------------

     public RuleSet getGlobalRuleSet()
    {
         return global_rule_set;
     }


     public void setGlobalRuleSet(RuleSet rs)
    {
        global_rule_set = rs;
    }
    public SmallAco[][] getMatrix()
    {
            return matrix;
    }
    public void setMatrix(SmallAco[][] m)
    {
        matrix=m;
    }

    public SmallAco getSmallAco()
    {
        return small_aco;
    }

    public void setSmallAco(SmallAco small_aco)
    {
        this.small_aco=small_aco;
    }

    public int getMarchKind()
    {
        return current_march_kind;
    }

    public void setMarchKind(int march_kind)
    {this.current_march_kind=march_kind;}

     public double getOptimalObjectiveFunction()
    {
        return optimal_objective_function;
     }
     public void setOptimalObjectiveFunction(double optimal)
            { this.optimal_objective_function=optimal;
}

    public int getNumOfAnts()
    {
            return big_aco_num_of_ants;
    }

    public void setNumOfAnts(int ants)
    { 
        big_aco_num_of_ants=ants;
    }

    public File[] getRuleSets()
    {
        return   rule_sets;}

    public void setRuleSets(File[] rule_sets)
    {
            this. rule_sets=rule_sets;
    }

    public LinkedList getOutputList()
    {
        return output_list;
    }

    public RuleSet getCompleteRuleSet()
    {
        return complete_ruleset;
    }

    private void buildSolution(double small_aco_improvement_factor,double big_aco_improvement_factor,double small_aco_evaporation_rate)
    {//The work each ant does to build the solution
        double improvement;
      SmallAco [][] matrix_copy = duplicateMatrix(matrix);
            int rule_index=current_location[0];
            int cond_index=current_location[1];
            int [] next_location=new int[2];
            next_location[0]=rule_index;
            next_location[1]=cond_index;
            SmallAco current_small_aco = matrix[rule_index][cond_index];
            if(current_small_aco!=null)
            {//Do the ant work on the current location
               RuleSet updated_global_rule_set=perturbCell(matrix_copy,current_location,small_aco_improvement_factor,small_aco_evaporation_rate);
                double f_prime = updated_global_rule_set.getAccuracy();
                double delta_x =f_prime-optimal_objective_function;
                if(delta_x>0) //if there is an improvement
                {
                    improvement=delta_x;
                    global_rule_set=updated_global_rule_set;
                    matrix = matrix_copy;
                    optimal_objective_function=f_prime;
                    updatePheromone(current_location,improvement, big_aco_improvement_factor);
                        if(updated_locations.isEmpty())
                            updated_locations.add(current_location);
                        if(contains(updated_locations,current_location)<0)
                            updated_locations.add(current_location);
                }
                  
		next_location = getLargestAdjacentPheromone(rule_index, cond_index, matrix);  //find adjacent location with highest getPheromone
		if(next_location!=null) //if there is pheromone on adjacent cells
                {
                    next_location=checkEncounters(locations,next_location);//check if the new location has an ant and if yes, go left or right randomly.
                    if(next_location!=null)//if the ant is surrounded by ants
                        current_location= next_location; //do not change current location, the ant stays in its place until next itteration waiting for other ants to move.
                }
                    else
                {//else if there is no Pheromone on adjacent locations,go to next location of the S-March
                    next_location=checkEncounters(locations,S_march(current_location));
                    if(next_location!=null)//if not surrounded by ants, set its new location to next_location, otherwise, stay where it were until next itteration where
                        current_location=next_location;//other ants would have moved...
                }
            }
            else
            {
                next_location = getLargestAdjacentPheromone(rule_index, cond_index, matrix);  //find adjacent location with highest getPheromone
		if(next_location!=null) //if there is pheromone on adjacent cells
                {
                    next_location=checkEncounters(locations,next_location);//check if the new location has an ant and if yes, go left or right randomly.
                    if(next_location!=null)//if the ant is surrounded by ants
                        current_location= next_location; //do not change current location, the ant stays in its place until next itteration waiting for other ants to move.
                }
                    else
                {//else if there is no Pheromone on adjacent locations,go to next location of the S-March
                    next_location=checkEncounters(locations,S_march(current_location));
                    if(next_location!=null)//if not surrounded by ants, set its new location to next_location, otherwise, stay where it were until next itteration where
                        current_location=next_location;//other ants would have moved...
                }

            }
    }

    public RuleSet perturbCell(SmallAco [][] matrix, int [] current_location, double improvement_factor,double evaporation_rate)
    {
        int rule_index = current_location[0];
        int cond_index = current_location[1];
        SmallAco current_small_aco = matrix[rule_index][cond_index];
        current_small_aco.runSmallAco(improvement_factor,evaporation_rate);
        matrix[rule_index][cond_index]=current_small_aco;
        RuleSet global_rule_set = initializeGlobalRuleSet(matrix,rule_quality_factor1, rule_quality_factor2, threshold_variable);
        return global_rule_set;
    }

      private int [] checkEncounters(LinkedList locations, int [] location)
    {//cheks if the location has another ant and if it does it moves either left or right randomly without going out of bound of the matrix and
        //making sure that the next location doesn't also have another ant. If the current location, the left location and the right location allm have ants,
        //the method returns null, otherwise, it returns the new location.
        if(exists(locations, location)) //if there is another ant in this location
                {
                System.out.println("But location "+location[0]+","+location[1]+" has another ant on it so we might move left or right!!.");

                    do
                            {
                             Random rand = new Random(System.currentTimeMillis());
                         if(current_march_kind%2==0)//if horizontal marches
                          {
                             int [] left_location={location[0]-1,location[1]};
                             int [] right_location={location[0]+1,location[1]};
                             if((exists(locations,left_location))&&(exists(locations,right_location)))
                                     return null;
                             if(location[0] == matrix_height - 1)//if horizontal marches and on the last rule....
                                location[0]=location[0]-1; //move left
                              else if(location[0]==0)//if horizontal marches and on the first rule....
                            location[0]=location[0]+1;//move right
                             else
                         {

                            int rand_num = rand.nextInt();
                            if(rand_num%2==0) //Move left
                                location[0]=location[0]-1;
                            else//Move right
                               location[0]=location[0]+1;
                         }
                           }
                         else if(current_march_kind%2!=0)
                          {
                              int [] left_location={location[0],location[1]-1};
                             int [] right_location={location[0],location[1]+1};
                             if((exists(locations,left_location))&&(exists(locations,right_location)))
                                     return null;
                              if(location[1] == 0)//if vertical march and in the first condition
                                    location[1]=location[1]+1;//move right
                              else if((current_march_kind%2!=0)&&(location[1]==matrix_width-1))//if vertical march and in the last condition
                                    location[1]=location[1]-1;//move right
                               else
                            {
                            int rand_num = rand.nextInt();
                            if(rand_num%2==0) //Move left
                                    location[1]=location[1]-1;
                            else//Move right
                                    location[1]=location[1]+1;
                         }
                         }
                    }while(exists(locations,location));
                }
        return location;
    }

      private int contains(LinkedList locations, int [] location)
    {//checks if an arry of locations contains a certain location...
        for(int i=0; i<locations.size(); i++)
        {
            int [] current_loc=(int [])locations.get(i);
            if ((current_loc[0]==location[0])&&(current_loc[1]==location[1]))
                return locations.indexOf(current_loc);
        }
        return -1;
    }

    public int [] S_march(int [] current_location)
    {
        //returns next location according to current smarch kind
        int [] new_location= new int [2];
        int rule_index=current_location[0];
        int cond_index=current_location[1];
        if(current_march_kind==1)
            new_location = upperLeftVerticalMarch(rule_index,cond_index);
        else if(current_march_kind==2)
            new_location=lowerRightHorizontalMarch(rule_index,cond_index);
        else if(current_march_kind==4)
            new_location=upperRightHorizontalMarch(rule_index,cond_index);
        else if(current_march_kind==3)
            new_location=upperRightVerticalMarch(rule_index,cond_index);
        else if(current_march_kind==5)
            new_location=lowerRightVerticalMarch(rule_index,cond_index);
        else if(current_march_kind==7)
            new_location=lowerLeftVerticalMarch(rule_index,cond_index);
        else if(current_march_kind==6)
            new_location=lowerLeftHorizontalMarch(rule_index,cond_index);
        else if(current_march_kind==8)
            new_location=upperLeftHorizontalMarch(rule_index,cond_index);
        return new_location;
    }
//------------------------------------------------------------------------------
//The implimentation of the 8 kinds of marches. These marches can be classified
//into 2 classes: 1-horizontal marches           2-vertical march
//then each of these classes can be defined into 4 marches that are destiguished
//by taking the starting point as reference where it can be on one of the four
//corners of the matrix.
//------------------------------------------------------------------------------

    private int [] upperLeftVerticalMarch(int rule_index,int cond_index)
    {//The vertical march with the starting point at the upper left
        int[] location=new int [2];
        current_march_kind = 1;   //set march kind to 1 which won't change unless the ant moved into a new kind of march
        if((cond_index==matrix_width-1)&&(rule_index==matrix_height-1)&&(matrix_width%2!=0)) //if ant reached final location and it is at the lower right corner
            location=lowerRightHorizontalMarch(rule_index,cond_index);                //then move into a new kind of march
        else if((cond_index==matrix_width-1)&&(rule_index==0)&&(matrix_width%2==0))  //if ant reached final location it is at upper right corner
            location=upperRightHorizontalMarch(rule_index,cond_index);            //then move into a new kind of march
        else if((cond_index%2==0)&&(rule_index!=matrix_height-1))//the ant is on an even column and not at its end...
        {
            rule_index=rule_index+1;// then Move down
        }
        else if((cond_index%2==0)&&(rule_index==matrix_height-1))//if ant is on an even column but on its last cell...
             {
                cond_index=cond_index+1;     //then Move right
             }
        else if((cond_index%2!=0)&&(rule_index!=0))//if ant is on an odd column and not on its last cell...
             {
                rule_index=rule_index-1; //then Move up
             }
        else if((cond_index%2!=0)&&(rule_index==0))//if ant is on an odd column and on its last cell...
             {
                cond_index=cond_index+1;     //Move right
             }
        location[0]=rule_index;
        location[1]=cond_index;
        return location;
    }

    private int [] lowerRightHorizontalMarch(int rule_index,int cond_index)
    {//The horizontal march with the starting point at the lower right (Same concept as above.)
        int[] location=new int [2];
        current_march_kind = 2;
        if((cond_index==0)&&(rule_index==0)&&(matrix_height%2!=0)) //if final location is at the upper left corner
            location=upperLeftVerticalMarch(rule_index,cond_index);
        else if((cond_index==matrix_width-1)&&(rule_index==0)&&(matrix_height%2==0))  //if final location is at upper right corner
            location=upperRightVerticalMarch(rule_index,cond_index);
        else if (matrix_height % 2 == 0)
        {
            if((rule_index%2!=0)&&(cond_index!=0))
        {
            cond_index=cond_index-1; //Move left
        }
       else if((rule_index%2!=0)&&(cond_index==0))
        {
           rule_index=rule_index-1;//Move up
        }
        else if((cond_index!=matrix_width-1)&&(rule_index%2==0))
             {
                cond_index=cond_index+1;     //Move right
             }
         else if((cond_index==matrix_width-1)&&(rule_index%2==0))
             {
                rule_index=rule_index-1;//Move up
             }
        }
 else
        {
            if((rule_index%2==0)&&(cond_index!=0))
        {
            cond_index=cond_index-1; //Move left
        }
       else if((rule_index%2==0)&&(cond_index==0))
        {
           rule_index=rule_index-1;//Move up
        }
        else if((cond_index!=matrix_width-1)&&(rule_index%2!=0))
             {
                cond_index=cond_index+1;     //Move right
             }
         else if((cond_index==matrix_width-1)&&(rule_index%2!=0))
             {
                rule_index=rule_index-1;//Move up
             }

 }
        location[0]=rule_index;
        location[1]=cond_index;
        return location;
    }

     private int [] upperRightHorizontalMarch(int rule_index,int cond_index)
    {//The Horizontal march with the starting point at the upper right (Same concept)
        int[] location=new int [2];
        current_march_kind = 4;
       if((cond_index==matrix_width-1)&&(rule_index==matrix_height-1)&&(matrix_height%2==0)) //if final location is at the lower right corner
            location=lowerRightVerticalMarch(rule_index,cond_index);
        else if((cond_index==0)&&(rule_index==matrix_height-1)&&(matrix_height%2!=0))  //if final location is at lower left corner
            location=lowerRightVerticalMarch(rule_index,cond_index);
        else if((rule_index%2==0)&&(cond_index!=0))
        {
                //cond_index=cond_index+1;     //Move right
                 cond_index=cond_index-1;     //Move left
        }
       else if((rule_index%2==0)&&(cond_index==0))
        {
           rule_index=rule_index+1;//Move down
        }
        else if((cond_index!=matrix_width-1)&&(rule_index%2!=0))
             {
               // cond_index=cond_index-1;     //Move left
                cond_index=cond_index+1;     //Move right
             }
         else if((cond_index==matrix_width-1)&&(rule_index%2!=0))
             {
                rule_index=rule_index+1; //Move down
             }
        location[0]=rule_index;
        location[1]=cond_index;
        return location;
    }

     private int [] upperRightVerticalMarch(int rule_index,int cond_index)
    {//The Vertical march with the starting point at the upper right (Same concept)
        int[] location=new int [2];
        current_march_kind = 3;
       if((cond_index==0)&&(rule_index==matrix_height-1)&&(matrix_width%2!=0)) //if final location is at the lower left corner
            location=lowerLeftHorizontalMarch(rule_index,cond_index);
        else if((cond_index==0)&&(rule_index==0)&&(matrix_width%2==0))  //if final location is at upper left corner
            location=upperLeftHorizontalMarch(rule_index,cond_index);
        else if(matrix_width % 2 == 0)
        {
            if((rule_index!=matrix_height-1)&&(cond_index%2!=0))
        {
            rule_index=rule_index+1;//Move down
        }
      else if((rule_index==matrix_height-1)&&(cond_index%2!=0))
        {
                cond_index=cond_index-1;     //Move left
        }
        else if((rule_index!=0)&&(cond_index%2==0))
             {
                rule_index=rule_index-1;//Move up
             }
         else if((rule_index==0)&&(cond_index%2==0))
             {
                cond_index=cond_index-1;     //Move left
             }
        }
 else
       {
            if((rule_index!=matrix_height-1)&&(cond_index%2==0))
        {
            rule_index=rule_index+1;//Move down
        }
      else if((rule_index==matrix_height-1)&&(cond_index%2==0))
        {
                cond_index=cond_index-1;     //Move left
        }
        else if((rule_index!=0)&&(cond_index%2!=0))
             {
                rule_index=rule_index-1;//Move up
             }
         else if((rule_index==0)&&(cond_index%2!=0))
             {
                cond_index=cond_index-1;     //Move left
             }

        }
             location[0]=rule_index;
             location[1]=cond_index;
        return location;
    }


     private int [] lowerRightVerticalMarch(int rule_index,int cond_index)
    {
        int[] location=new int [2];
        current_march_kind = 5;
       if((cond_index==0)&&(rule_index==matrix_height-1)&&(matrix_width%2==0)) //if final location is at the lower left corner
            location=lowerLeftHorizontalMarch(rule_index,cond_index);
        else if((cond_index==0)&&(rule_index==0)&&(matrix_width%2!=0))  //if final location is at upper left corner
            location=upperLeftHorizontalMarch(rule_index,cond_index);
        else if(matrix_width % 2 == 0)
        {
            if((rule_index!=0)&&(cond_index%2!=0))
        {
            rule_index=rule_index-1;//Move up
        }
      else if((rule_index==0)&&(cond_index%2!=0))
        {
                cond_index=cond_index-1;     //Move left
        }
        else if((rule_index!=matrix_height-1)&&(cond_index%2==0))
             {
                  rule_index=rule_index+1;//Move down
             }
         else if((rule_index==matrix_height-1)&&(cond_index%2==0))
             {
                cond_index=cond_index-1;     //Move left
             }
        }
        else
        {
            if((rule_index!=0)&&(cond_index%2==0))
        {
            rule_index=rule_index-1;//Move up
        }
      else if((rule_index==0)&&(cond_index%2==0))
        {
                cond_index=cond_index-1;     //Move left
        }
        else if((rule_index!=matrix_height-1)&&(cond_index%2!=0))
             {
                  rule_index=rule_index+1;//Move down
             }
         else if((rule_index==matrix_height-1)&&(cond_index%2!=0))
             {
                cond_index=cond_index-1;     //Move left
             }
        }

        location[0]=rule_index;
        location[1]=cond_index;
        return location;
    }

     private int [] lowerLeftVerticalMarch(int rule_index,int cond_index)
    {//The Vertical march with the starting point at the lower left(Same concept)
        int[] location=new int [2];
        current_march_kind = 7;
        if((cond_index==matrix_width-1)&&(rule_index==matrix_height-1)&&(matrix_width%2==0)) //if final location is at the lower right corner
            location=lowerRightHorizontalMarch(rule_index,cond_index);
        else if((cond_index==matrix_width-1)&&(rule_index==0)&&(matrix_width%2!=0))  //if final location is at upper right corner
            location=upperRightHorizontalMarch(rule_index,cond_index);
        else if((cond_index%2==0)&&(rule_index!=0))
        {
            rule_index=rule_index-1;//Move up
        }
        else if((cond_index%2==0)&&(rule_index==0))
             {
                cond_index=cond_index+1;     //Move right
             }
        else if((cond_index%2!=0)&&(rule_index!=matrix_height-1))
             {
                rule_index=rule_index+1; //Move down
             }
        else if((cond_index%2!=0)&&(rule_index==matrix_height-1))
             {
                cond_index=cond_index+1;     //Move right
             }
        location[0]=rule_index;
        location[1]=cond_index;

        return location;

    }

     private int [] lowerLeftHorizontalMarch(int rule_index,int cond_index)
    {//The Horizontal march with the starting point at the lower left (Same concept)
        int[] location=new int [2];
        current_march_kind = 6;
        if((cond_index==matrix_width-1)&&(rule_index==0)&&(matrix_height%2!=0))  //if final location is at upper right corner
            location=upperRightVerticalMarch(rule_index,cond_index);
        else if((cond_index==0)&&(rule_index==0)&&(matrix_height%2==0))  //if final location is at upper left corner
            location=upperLeftVerticalMarch(rule_index,cond_index);
        else if(matrix_height % 2 == 0)
        {
             if((cond_index!=matrix_width-1)&&(rule_index%2!=0))
             {
                cond_index=cond_index+1;     //Move right
             }
         else if((cond_index==matrix_width-1)&&(rule_index%2!=0))
             {
                 rule_index=rule_index-1; //Move up
             }
         else if((rule_index%2==0)&&(cond_index!=0))
        {
               cond_index=cond_index-1;     //Move left
        }
       else if((rule_index%2==0)&&(cond_index==0))
        {
            rule_index=rule_index-1; //Move up
        }
        }
 else
        {
            if((cond_index!=matrix_width-1)&&(rule_index%2==0))
             {
                cond_index=cond_index+1;     //Move right
             }
         else if((cond_index==matrix_width-1)&&(rule_index%2==0))
             {
                 rule_index=rule_index-1; //Move up
             }
         else if((rule_index%2!=0)&&(cond_index!=0))
        {
               cond_index=cond_index-1;     //Move left
        }
       else if((rule_index%2!=0)&&(cond_index==0))
        {
            rule_index=rule_index-1; //Move up
        }
        }
        location[0]=rule_index;
        location[1]=cond_index;
        return location;
    }


     private int [] upperLeftHorizontalMarch(int rule_index,int cond_index)
    {//The Horizontal march with the starting point at the upper left (Same concept)
        int[] location=new int [2];
        current_march_kind = 8;
        if((cond_index==matrix_width-1)&&(rule_index==matrix_height-1)&&(matrix_height%2!=0)) //if final location is at the lower right corner
            location=lowerRightVerticalMarch(rule_index,cond_index);
        else if((cond_index==0)&&(rule_index==matrix_height-1)&&(matrix_height%2==0))  //if final location is at lower left corner
            location=lowerLeftVerticalMarch(rule_index,cond_index);
         else if((cond_index!=0)&&(rule_index%2!=0))
             {
                    cond_index=cond_index-1;     //move left
             }
         else if((cond_index==0)&&(rule_index%2!=0))
             {
                rule_index=rule_index+1;   //Move down
             }
         else if((rule_index%2==0)&&(cond_index!=matrix_width-1))
        {
             cond_index=cond_index+1; //Move right
        }
       else if((rule_index%2==0)&&(cond_index==matrix_width-1))
        {
           rule_index=rule_index+1;     //Move down
        }
        location[0]=rule_index;
        location[1]=cond_index;
        return location;

    }

     public int [] getLargestAdjacentPheromone(int rule_index, int cond_index, SmallAco [][] matrix)
    {//it returns the adjacent location that has the highest amount of getPheromone.
     //if there is no getPheromone around the indicated location, it returns null.
        SmallAco current_small_aco;
        int [] location = new int [2];
        double max_pheromone=0;
        double pheromone1=0, pheromone2=0, pheromone3=0,pheromone4=0,pheromone5=0,pheromone6=0,pheromone7=0,pheromone8=0;
        //Check getPheromone on the upper adjacent cell
        if((rule_index-1<matrix_height)&&(rule_index-1>=0)&&(cond_index<matrix_width))
        {
        current_small_aco= matrix[rule_index-1][cond_index];
        if(current_small_aco!=null)//if the location is not an empty one
        {
             pheromone1=current_small_aco.getRulSet().getPheromone();//get condition getPheromone
            if(pheromone1>max_pheromone)//if it has maximum getPheromone, save it
            {
                max_pheromone=pheromone1;
                location[0]=rule_index-1;
                location[1]=cond_index;
            }
        }
        }
        //check if getPheromone on the upper-right cell is Max.(same concept!!!! WRITE ONE METHOD FOR THE 8 CASES!!!!)
        if((rule_index-1<matrix_height)&&(rule_index-1>=0)&&(cond_index+1<matrix_width))
        {
        current_small_aco= matrix[rule_index-1][cond_index+1];
        if(current_small_aco!=null)
        {
            pheromone2=current_small_aco.getRulSet().getPheromone();//get condition getPheromone
            if(pheromone2>max_pheromone)//if it has maximum getPheromone, save it
            {
                max_pheromone=pheromone2;
                location[0]=rule_index-1;
                location[1]=cond_index+1;
            }
        }
        }
        //check if getPheromone on the right cell is Max.(same concept)
         if((rule_index<matrix_height)&&(cond_index+1<matrix_width))
         {
        current_small_aco= matrix[rule_index][cond_index+1];
        if(current_small_aco!=null)
        {

                pheromone3=current_small_aco.getRulSet().getPheromone();
            
            if(pheromone3>max_pheromone)
                {
                max_pheromone=pheromone3;
                location[0]=rule_index;
                location[1]=cond_index+1;
                }
        }
        }
        //check if getPheromone on the right cell is Max.(same concept)
         if((rule_index+1<matrix_height)&&(cond_index+1<matrix_width))
         {
        current_small_aco= matrix[rule_index+1][cond_index+1];
        if(current_small_aco!=null)
        {
           pheromone4=current_small_aco.getRulSet().getPheromone();
            if(pheromone4>max_pheromone)
            {
                max_pheromone=pheromone4;
                location[0]=rule_index+1;
                location[1]=cond_index+1;
            }
        }
        }
        //check if getPheromone on the lower cell is Max.(same concept)
         if((rule_index+1<matrix_height)&&(cond_index<matrix_width))
         {
        current_small_aco= matrix[rule_index+1][cond_index];
        if(current_small_aco!=null)
        {
            pheromone5=current_small_aco.getRulSet().getPheromone();
            if(pheromone5>max_pheromone)
            {
                max_pheromone=pheromone5;
                location[0]=rule_index+1;
                location[1]=cond_index;
            }
        }
        }
        //check if getPheromone on the lower-left cell is Max.(same concept)
         if((rule_index+1<matrix_height)&&(cond_index-1<matrix_width)&&(cond_index-1>=0))
         {
        current_small_aco= matrix[rule_index+1][cond_index-1];
        if(current_small_aco!=null)
        {
  
            pheromone6=current_small_aco.getRulSet().getPheromone();
            if(pheromone6>max_pheromone)
            {
                max_pheromone=pheromone6;
                location[0]=rule_index+1;
                location[1]=cond_index-1;
            }
        }
        }
        //check if getPheromone on the left cell is Max.(same concept)
         if((rule_index<matrix_height)&&(cond_index-1<matrix_width)&&(cond_index-1>=0))
         {
        current_small_aco= matrix[rule_index][cond_index-1];
        if(current_small_aco!=null)
        {
            pheromone7=current_small_aco.getRulSet().getPheromone();
            if(pheromone7>max_pheromone)
            {
                max_pheromone=pheromone7;
                location[0]=rule_index;
                location[1]=cond_index-1;
            }
        }
        }
        //check if getPheromone on the upper-left cell is Max.(same concept)
         if((rule_index-1<matrix_height)&&(rule_index-1>=0)&&(cond_index-1<matrix_width)&&(cond_index-1>=0))
         {
        current_small_aco= matrix[rule_index-1][cond_index-1];
        if(current_small_aco!=null)
        {
                pheromone8=current_small_aco.getRulSet().getPheromone();
            if(pheromone8>max_pheromone)
            {
                max_pheromone=pheromone8;
                location[0]=rule_index-1;
                location[1]=cond_index-1;
            }
        }
        }
        if((pheromone1==pheromone2)&&(pheromone2==pheromone3)&&(pheromone3==pheromone4)&&(pheromone4==pheromone5)
                &&(pheromone5==pheromone6)&&(pheromone6==pheromone7)&&(pheromone7==pheromone8))     //If we have a tie... go randomly to any
        {                                                                       //adjacent location (!!!CREATE A SEPARATE METHOD FOR THIS !!!!)
            Random rand = new Random(System.currentTimeMillis());
            int random_num=rand.nextInt(8);
            switch(random_num)
            {
                case 0:
                {
                     if((rule_index-1<matrix_height)&&(rule_index-1>=0)&&(cond_index<matrix_width))
                     {
                    location[0]=rule_index-1;
                    location[1]=cond_index;
                    break;
                    }
                }
                case 1:
                {
                    if((rule_index-1<matrix_height)&&(rule_index-1>=0)&&(cond_index+1<matrix_width))
                    {
                     location[0]=rule_index-1;
                    location[1]=cond_index+1;
                    break;
                    }
                }
                case 2:
                {
                     if((rule_index<matrix_height)&&(cond_index+1<matrix_width))
                     {
                location[0]=rule_index;
                location[1]=cond_index+1;
                break;
                    }
                }
                case 3:
                {
                    if((rule_index+1<matrix_height)&&(cond_index+1<matrix_width))
                    {
                location[0]=rule_index+1;
                location[1]=cond_index+1;
                break;
                    }
                }
                case 4:
                {
                    if((rule_index+1<matrix_height)&&(cond_index<matrix_width))
                    {
                location[0]=rule_index+1;
                location[1]=cond_index;
                break;
                    }
                }
                case 5:
                {

                    if((rule_index+1<matrix_height)&&(cond_index-1<matrix_width)&&(cond_index-1>=0))
                    {
                        location[0]=rule_index+1;
                        location[1]=cond_index-1;
                        break;
                    }
                }
                case 6:
                {
                    if((rule_index<matrix_height)&&(cond_index-1<matrix_width)&&(cond_index-1>=0))
                    {
                location[0]=rule_index;
                location[1]=cond_index-1;
                break;
                    }
                }
                case 7:
                {

                 if((rule_index-1<matrix_height)&&(cond_index-1<matrix_width)&&(cond_index-1>=0))
                 {
                location[0]=rule_index-1;
                location[1]=cond_index-1;
                break;
                    }
                }
             }
        }
        if(max_pheromone==0)//if all adjacent cells have no getPheromone...
            return null;    //return null in order to continue on the S-march in this case.
        else
        {
            return location;//return the location with the highest getPheromone
        }
    }

    public void updatePheromone(int [] location, double improvement, double factor)
    {//Checks if the current location is label or a condition and updates the corresponding object's getPheromone relative to the improvement...
        int rule_index = location[0];
        int cond_index=location[1];
        SmallAco current_small_aco = matrix[rule_index][cond_index];
        RuleSet rule_set = current_small_aco.getRulSet();
        updateRuleSetPheromone(rule_set, improvement, factor);

    }

    public void updateRuleSetPheromone(RuleSet rule_set,double improvement, double factor)
    {//updates the condition object getPheromone according to improvement...           
        double initial_pheromone, updated_pheromone;
        initial_pheromone = rule_set.getPheromone();
        updated_pheromone = initial_pheromone+(factor * improvement);
        rule_set.setPheromone(updated_pheromone);
    }

    public void evaporatePheromone(double amount)                                   
    {//evaporates amonut from all cells that have getPheromone on them
        SmallAco current_small_aco;
        Rule rule;
            for(int i=0; i<updated_locations.size();i++)
            {  
                int [] location =(int []) updated_locations.get(i);
                int rule_index = location[0];
                int cond_index = location[1];
                current_small_aco=matrix[rule_index][cond_index];
                RuleSet current_rule_set = current_small_aco.getRulSet();
                if(current_rule_set!=null)
                {
                    double old_pheromone = current_rule_set.getPheromone();
                    double new_pheromone = old_pheromone-amount;
                        if(new_pheromone<=0)
                        {
                            new_pheromone=0;
                            current_rule_set.setPheromone(new_pheromone);
                            updated_locations.remove(i);
                            i--;
                        }
                        else
                            current_rule_set.setPheromone(new_pheromone);
                    }
               }
        }
    
    public RuleSet pruneRules(RuleSet rule_set)
    {
         LinkedList rules = rule_set.getRules();
         for(int test_index=0;test_index<rules.size()-1;test_index++ )
         {
             Rule test_rule = (Rule)rules.get(test_index);
             for(int comp_index=0; comp_index<rules.size()-1; comp_index++)
             {
                 if(test_index!=comp_index)
                 {
                     Rule comp_rule = (Rule)rules.get(comp_index);
                     if(comp_rule.implies(test_rule))
                     {
                         if(comp_rule.getQuality()<test_rule.getQuality())
                         {
                             rule_set.removeRule(comp_rule);
                             if(comp_index<test_index)
                                 test_index=test_index-1;
                             comp_index=rules.size();
                         }
                         else
                         {
                             rule_set.removeRule(test_rule);
                             test_index=test_index-1;
                             comp_index=rules.size();
                         }
                     }
                     else if(comp_rule.contradicts(test_rule))
                     {
                        if(comp_rule.getQuality()<test_rule.getQuality())
                         {
                             rule_set.removeRule(comp_rule);
                             if(comp_index<test_index)
                                 test_index=test_index-1;
                             comp_index=rules.size();
                         }
                         else
                         {
                             rule_set.removeRule(test_rule);
                             test_index=test_index-1;
                             comp_index=rules.size();
                         }
                     }
                 }
             }
         }
         rule_set.initializeRuleSetVars();
         return rule_set;
    }
}