//******************************************************************************
//Name:Gioia Wehbe.
//Student's number: 200801378.
//Project Name: Double ACO
//Last modified on: 3/12/2011
//Describtion: a class that simulates all the 'Small Aco' computations and task
// in order to generate new 'good Rules'
//******************************************************************************

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

public class SmallAco
{
    private RuleSet rule_set;
    private final int small_aco_num_of_ants;  //It's Value is assigned according to experimentation
    private int march_kind;       //Decides the kind of march of the ant among 8 different path ways          
    //private int optimal_objective_function;    //where the highest getValue of the objective function will be stored for comparisons
    private int iterations;        //number of itterations until the ACO ends. getValue assigned according to experimentation
    private int longest_rule;   //Matrix width
    private int num_of_rules;   //Matrix height
    private int [] current_location;
    private LinkedList locations;   //the final location of each ant in after the most recent itteration...
    private int [] march_kinds; //the final pathway each ant is on after the most recent itteration...
    private LinkedList updated_locations=new LinkedList(); //holds all the locations that have getPheromone.
    private Random rand = new Random(System.currentTimeMillis());

    //----------------------------------------------
    // Constructors
    //----------------------------------------------

    public SmallAco(RuleSet rule_set, int num_of_ants, int march_kind, int iterations, int longest_rule, int num_of_rules, 
            int [] current_location, LinkedList locations, int [] march_kinds, LinkedList updated_locations, Random rand)
    {// Small ACO constructor used to create copies of the object
        this.current_location=current_location;
        this.iterations=iterations;
        this.locations=locations;
        this.longest_rule=longest_rule;
        this.march_kind=march_kind;
        this.march_kinds=march_kinds;
        this.small_aco_num_of_ants=num_of_ants;
        this.num_of_rules=num_of_rules;
        this.rand=rand;
        this.rule_set=rule_set;
        this.updated_locations=updated_locations;

    }

    public SmallAco(RuleSet rule_set,int num_of_ants, int itterations)
	{//class constructor
            this.rule_set=rule_set;
            this.small_aco_num_of_ants=num_of_ants;
            this.iterations=itterations;
            longest_rule=rule_set.getLongestRuleSize();
            num_of_rules=rule_set.getSize();
            current_location=new int [2];
            locations=initializeLocations(); // initialize random initial locations for each ant. this list is updated for each ant when it
                                                //moves to a new location.
            march_kinds = initializeMarchKinds();//initializes the starting march kind for each ant ranomly.
        }

    public SmallAco duplicate()
    {// Creates a copy of the small ACO object
        RuleSet new_ruleset=rule_set.duplicate();
        SmallAco copy = new SmallAco(new_ruleset,small_aco_num_of_ants, march_kind, iterations, longest_rule, num_of_rules, current_location,  locations,
                march_kinds, updated_locations,rand);
        return copy;
    }

    //----------------------------
    // Getter Methods
    //----------------------------

     public RuleSet getRulSet()
        {
            return rule_set;
        }

     //-----------------------------------------
     // SMall ACO major functions
     //-----------------------------------------

    public void runSmallAco(double improvement_factor,double evaporation_rate)
    {//initializes the original rule set's values and starts building from it new solutions
        for(int j=0; j<iterations; j++)
        {System.out.println("Iteration number "+j+" of the Small ACO");
            for(int i=0; i<small_aco_num_of_ants;i++)
            {  System.out.println("Ant number "+i+" of the Small ACO");
                current_location = (int[])locations.get(i);
                march_kind=march_kinds[i];
                buildSolution(improvement_factor);
                locations.set(i, current_location);
                march_kinds[i]=march_kind;
            }
            evaporatePheromone(evaporation_rate);
        }
        pruneConditions(rule_set);
    }

    public void buildSolution(double improvement_factor)
    {//marches each ant and does the work of each ant (purturb, compares objective function, deposit getPheromone, decide on next location...)
        double improvement;
        int [] next_location= new int [2];
        RuleSet initial_rule_set = rule_set.duplicate();   //changes will be performed on initial_rule_set, a clone of rule_set!!!
            int rule_index=current_location[0];
            int cond_index=current_location[1];
            Object element= initial_rule_set.getElementAt(rule_index, cond_index);
            next_location[0]=rule_index;
            next_location[1]=cond_index;
            if(element!=null)
            {//Do the ant work on the current location
                Rule initial_rule=(Rule)initial_rule_set.getRules().get(rule_index);
                double initial_rule_quality = initial_rule.getQuality();
                initial_rule_set = perturbCell(initial_rule_set, current_location);   //updates rule set by purterbing current location.
                Rule new_rule=(Rule)(initial_rule_set.getRules().get(rule_index));
                double new_rule_quality = new_rule.getQuality();
                double delta_x = new_rule_quality - initial_rule_quality;
                if(delta_x<=0) //if new rule is not better than old rule, choose randomly
                {
                    double probability = Math.pow(Math.E, (-1)*(delta_x/2)); //probability is E to the power minus delta_x over t 
                                                                                //where t is the maximum possible quality
                    probability = probability/Math.E;
                    double rand_num = rand.nextDouble();
                    if(rand_num<=probability)
                    {//apply the change but with no pheromone deposit, otherwise, do not perform any change.
                        rule_set=initial_rule_set;
                    }
                }
                else if(delta_x>0)
                {//Apply the change and deposit pheromone to indicate definite improvement.
                    improvement=delta_x;  
                    rule_set=initial_rule_set;
                    updatePheromone(current_location,improvement, improvement_factor);       
                        if(updated_locations.isEmpty())
                            updated_locations.add(current_location);
                        else if(contains(updated_locations,current_location)<0)
                            updated_locations.add(current_location);
                }
		next_location = getLargestAdjacentPheromone(rule_index, cond_index, rule_set);  //find adjacent location with highest getPheromone
		if(next_location!=null) //if there is pheromone on adjacent cells
                {
                    next_location=checkEncounters(locations,next_location);//check if the new location has an ant and if yes, go left or right randomly.
                    if(next_location!=null)//if the ant is surrounded by ants
                        current_location= next_location; //do not change current location, the ant stays in its place until next itteration waiting for other ants to move.
                }
                    else
                {//else if there is no Pheromone on adjacent locations,go to next location of the S-March
                    next_location=S_march(current_location);
                    next_location=checkEncounters(locations,next_location);
                    if(next_location!=null)//if not surrounded by ants, set its new location to next_location, otherwise, stay where it were until next itteration where
                        current_location=next_location;//other ants would have moved...
                }
            }
            else//if element is null don't do anything and let current ant jump to a new location then move to a new ant
            {
                next_location = getLargestAdjacentPheromone(rule_index, cond_index, rule_set);  //find adjacent location with highest getPheromone
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

    private int contains(LinkedList locations, int [] location)
    {
        for(int i=0; i<locations.size(); i++)
        {
            int [] current_loc=(int [])locations.get(i);
            if ((current_loc[0]==location[0])&&(current_loc[1]==location[1]))
                return locations.indexOf(current_loc);
        }
        return -1;
    }

    private int [] checkEncounters(LinkedList locations, int [] location)
    {//cheks if the location has another ant and if it does it moves either left or right randomly without going out of bound of the matrix and
        //making sure that the next location doesn't also have another ant. If the current location, the left location and the right location allm have ants,
        //the method returns null, otherwise, it returns the new location.
        if(exists(locations, location)) //if there is another ant in this location
                {
                    do
                            {
                             Random rand = new Random(System.currentTimeMillis());
                         if(march_kind%2==0)//if horizontal marches
                          {
                             int [] left_location={location[0]-1,location[1]};
                             int [] right_location={location[0]+1,location[1]};
                             if((exists(locations,left_location))&&(exists(locations,right_location)))
                                     return null;
                             if(location[0] == num_of_rules - 1)//if horizontal marches and on the last rule....
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
                         else if(march_kind%2!=0)
                          {
                              int [] left_location={location[0],location[1]-1};
                             int [] right_location={location[0],location[1]+1};
                             if((exists(locations,left_location))&&(exists(locations,right_location)))
                                     return null;
                              if(location[1] == 0)//if vertical march and in the first condition
                                    location[1]=location[1]+1;//move right
                              else if((march_kind%2!=0)&&(location[1]==longest_rule-1))//if vertical march and in the last condition
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
    

    private int [] initializeMarchKinds()
    {//initializes march_kinds array to a random number indicating one of the 8 march kinds for each ant
        int [] march_kinds = new int [small_aco_num_of_ants];
         Random rand = new Random(System.currentTimeMillis());
        for(int i=0; i<march_kinds.length; i++)
        {
              int march_kind = rand.nextInt(8)+1;
            march_kinds[i]=march_kind; 
        }
        return march_kinds;
    }

    public void evaporatePheromone(double amount)                                   
    {//evaporates amonut from all cells that have getPheromone on them
        Object element;
        Rule rule;

            for(int i=0; i<updated_locations.size();i++)
            {  
                int [] location =(int []) updated_locations.get(i);
                int rule_index = location[0];
                int cond_index = location[1];
                element=rule_set.getElementAt(rule_index, cond_index);
                if(element!=null)
                {
                    if(element instanceof Condition)
                    {
                        double old_pheromone = ((Condition)element).getPheromone();
                        double new_pheromone = old_pheromone - amount;
                        if(new_pheromone<=0)
                        {
                            new_pheromone=0;
                            ((Condition)element).setPheromone(new_pheromone);
                            updated_locations.remove(i);   //remove it to since no evaporation needed anymore!!!
                            i--;
                        }//if amount to evaporate greater than initial amount, set getPheromone to 0!!!
                        else
                        {
                            ((Condition)element).setPheromone(new_pheromone);
                        }
                    }
                    else
                    {
                        rule=(Rule)(rule_set.getRules()).get(rule_index);
                        double old_pheromone = rule.getLabelPheromone();
                        double new_pheromone = old_pheromone - amount;
                        if(new_pheromone<=0)
                        {
                            new_pheromone=0;
                            rule.setLabelPheromone(new_pheromone);
                            updated_locations.remove(i);
                            i--;
                        }//if amount to evaporate greater than initial amount, set getPheromone to 0!!!
                        else
                            rule.setLabelPheromone(new_pheromone);
                    }
               }
        }
    }

    private LinkedList initializeLocations()
    {//initializes the locations list to random locations for each ant. used at the very begining of the algorithm.
        int [] location=new int [2];
        LinkedList locations=new LinkedList();
        Random rand = new Random(System.currentTimeMillis());
            for(int i=0; i<small_aco_num_of_ants;i++)
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
        return locations;
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
                         j=locations.size();//break out of loop
                    }
                    }
                }
        return exists;
    }

    public double computeImprovement(double initial_function, double updated_function)
    {//computes the improvement of on the objective function (difference between initial objective function and the updated one...
        return updated_function-initial_function;
    }

    public void updatePheromone(int [] location, double improvement, double factor)
    {//Checks if the current location is label or a condition and updates the corresponding object's getPheromone relative to the improvement...
        int rule_index = location[0];
        int cond_index=location[1];
        Object element = rule_set.getElementAt(rule_index, cond_index);
        if(element instanceof Condition)
            updateConditionPheromone((Condition)element,improvement, factor);
        else
        {
            Rule rule = (Rule)((rule_set.getRules()).get(rule_index));
            updateLabelPheromone(rule,improvement, factor);
        }
    }

    public void updateConditionPheromone(Condition element,double improvement, double factor)
    {//updates the condition object Pheromone according to improvement...
        double initial_pheromone, updated_pheromone;
        initial_pheromone = element.getPheromone();
        updated_pheromone = initial_pheromone+(factor * improvement);
        element.setPheromone(updated_pheromone);
    }

    public void updateLabelPheromone(Rule rule,double improvement, double factor)
    {//Updates the label's pheromone according to improvement
        double initial_pheromone = rule.getLabelPheromone();
        double updated_pheromone= initial_pheromone + (factor * improvement);                      
        rule.setLabelPheromone(updated_pheromone);
    }

    private int [] randLocation(Random rand)
    {//returns a random location for an ant according to the number of getRules and there max getSize
     //N.B: ant might start form an empty cell
        int cond_index=rand.nextInt(longest_rule);
        int rule_index = rand.nextInt(num_of_rules);
        int [] new_location = new int [2];
        new_location[0]=rule_index;
        new_location [1]=cond_index;
        return new_location;
    }

    public int [] S_march(int [] current_location)
    {
        //returns next location according to current smarch kind
        int [] new_location= new int [2];
        int rule_index=current_location[0];
        int cond_index=current_location[1];
        if(march_kind==1)
            new_location = upperLeftVerticalMarch(rule_index,cond_index);
        else if(march_kind==2)
            new_location=lowerRightHorizontalMarch(rule_index,cond_index);
        else if(march_kind==4)
            new_location=upperRightHorizontalMarch(rule_index,cond_index);
        else if(march_kind==3)
            new_location=upperRightVerticalMarch(rule_index,cond_index);
        else if(march_kind==5)
            new_location=lowerRightVerticalMarch(rule_index,cond_index);
        else if(march_kind==7)
            new_location=lowerLeftVerticalMarch(rule_index,cond_index);
        else if(march_kind==6)
            new_location=lowerLeftHorizontalMarch(rule_index,cond_index);
        else if(march_kind==8)
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
        march_kind = 1;   //set march kind to 1 which won't change unless the ant moved into a new kind of march
        if((cond_index==longest_rule-1)&&(rule_index==num_of_rules-1)&&(longest_rule%2!=0)) //if ant reached final location and it is at the lower right corner
            location=lowerRightHorizontalMarch(rule_index,cond_index);                //then move into a new kind of march
        else if((cond_index==longest_rule-1)&&(rule_index==0)&&(longest_rule%2==0))  //if ant reached final location it is at upper right corner
            location=upperRightHorizontalMarch(rule_index,cond_index);            //then move into a new kind of march
        else if((cond_index%2==0)&&(rule_index!=num_of_rules-1))//the ant is on an even column and not at its end...
        {
            rule_index=rule_index+1;// then Move down
        }
        else if((cond_index%2==0)&&(rule_index==num_of_rules-1))//if ant is on an even column but on its last cell...
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
        march_kind = 2;
        if((cond_index==0)&&(rule_index==0)&&(num_of_rules%2!=0)) //if final location is at the upper left corner
            location=upperLeftVerticalMarch(rule_index,cond_index);
        else if((cond_index==longest_rule-1)&&(rule_index==0)&&(num_of_rules%2==0))  //if final location is at upper right corner
            location=upperRightVerticalMarch(rule_index,cond_index);
        else if(num_of_rules % 2 == 0)
        {
            if((rule_index%2!=0)&&(cond_index!=0))
        {
            cond_index=cond_index-1; //Move left
        }
       else if((rule_index%2!=0)&&(cond_index==0))
        {
           rule_index=rule_index-1;//Move up
        }
        else if((cond_index!=longest_rule-1)&&(rule_index%2==0))
             {
                cond_index=cond_index+1;     //Move right
             }
         else if((cond_index==longest_rule-1)&&(rule_index%2==0))
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
        else if((cond_index!=longest_rule-1)&&(rule_index%2!=0))
             {
                cond_index=cond_index+1;     //Move right
             }
         else if((cond_index==longest_rule-1)&&(rule_index%2!=0))
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
        march_kind = 4;
       if((cond_index==longest_rule-1)&&(rule_index==num_of_rules-1)&&(num_of_rules%2==0)) //if final location is at the lower right corner
            location=lowerRightVerticalMarch(rule_index,cond_index);
        else if((cond_index==0)&&(rule_index==num_of_rules-1)&&(num_of_rules%2!=0))  //if final location is at lower left corner
            location=lowerRightVerticalMarch(rule_index,cond_index);
        else if((rule_index%2==0)&&(cond_index!=0))
        {
                 cond_index=cond_index-1;     //Move left
        }
       else if((rule_index%2==0)&&(cond_index==0))
        {
           rule_index=rule_index+1;//Move down
        }
        else if((cond_index!=longest_rule-1)&&(rule_index%2!=0))
             {
                cond_index=cond_index+1;     //Move right
             }
         else if((cond_index==longest_rule-1)&&(rule_index%2!=0))
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
        march_kind = 3;
       if((cond_index==0)&&(rule_index==num_of_rules-1)&&(longest_rule%2!=0)) //if final location is at the lower left corner
            location=lowerLeftHorizontalMarch(rule_index,cond_index);
        else if((cond_index==0)&&(rule_index==0)&&(longest_rule%2==0))  //if final location is at upper left corner
            location=upperLeftHorizontalMarch(rule_index,cond_index);
        else if(longest_rule % 2 == 0)
        {
            if((rule_index!=num_of_rules-1)&&(cond_index%2!=0))
        {
            rule_index=rule_index+1;//Move down
        }
      else if((rule_index==num_of_rules-1)&&(cond_index%2!=0))
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
            if((rule_index!=num_of_rules-1)&&(cond_index%2==0))
        {
            rule_index=rule_index+1;//Move down
        }
      else if((rule_index==num_of_rules-1)&&(cond_index%2==0))
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
        march_kind = 5;
       if((cond_index==0)&&(rule_index==num_of_rules-1)&&(longest_rule%2==0)) //if final location is at the lower left corner
            location=lowerLeftHorizontalMarch(rule_index,cond_index);
        else if((cond_index==0)&&(rule_index==0)&&(longest_rule%2!=0))  //if final location is at upper left corner
            location=upperLeftHorizontalMarch(rule_index,cond_index);
        else if(longest_rule % 2 == 0)
        {
            if((rule_index!=0)&&(cond_index%2!=0))
        {
            rule_index=rule_index-1;//Move up
        }
      else if((rule_index==0)&&(cond_index%2!=0))
        {
                cond_index=cond_index-1;     //Move left
        }
        else if((rule_index!=num_of_rules-1)&&(cond_index%2==0))
             {
                  rule_index=rule_index+1;//Move down
             }
         else if((rule_index==num_of_rules-1)&&(cond_index%2==0))
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
        else if((rule_index!=num_of_rules-1)&&(cond_index%2!=0))
             {
                  rule_index=rule_index+1;//Move down
             }
         else if((rule_index==num_of_rules-1)&&(cond_index%2!=0))
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
        march_kind = 7;
        if((cond_index==longest_rule-1)&&(rule_index==num_of_rules-1)&&(longest_rule%2==0)) //if final location is at the lower right corner
            location=lowerRightHorizontalMarch(rule_index,cond_index);
        else if((cond_index==longest_rule-1)&&(rule_index==0)&&(longest_rule%2!=0))  //if final location is at upper right corner
            location=upperRightHorizontalMarch(rule_index,cond_index);
        else if((cond_index%2==0)&&(rule_index!=0))
        {
            rule_index=rule_index-1;//Move up
        }
        else if((cond_index%2==0)&&(rule_index==0))
             {
                cond_index=cond_index+1;     //Move right
             }
        else if((cond_index%2!=0)&&(rule_index!=num_of_rules-1))
             {
                rule_index=rule_index+1; //Move down
             }
        else if((cond_index%2!=0)&&(rule_index==num_of_rules-1))
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
        march_kind = 6;
        if((cond_index==longest_rule-1)&&(rule_index==0)&&(num_of_rules%2!=0))  //if final location is at upper right corner
            location=upperRightVerticalMarch(rule_index,cond_index);
        else if((cond_index==0)&&(rule_index==0)&&(num_of_rules%2==0))  //if final location is at upper left corner
            location=upperLeftVerticalMarch(rule_index,cond_index);
        else if(num_of_rules % 2 == 0)
        {
             if((cond_index!=longest_rule-1)&&(rule_index%2!=0))
             {
                cond_index=cond_index+1;     //Move right
             }
         else if((cond_index==longest_rule-1)&&(rule_index%2!=0))
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
            if((cond_index!=longest_rule-1)&&(rule_index%2==0))
             {
                cond_index=cond_index+1;     //Move right
             }
         else if((cond_index==longest_rule-1)&&(rule_index%2==0))
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
        march_kind = 8;
        if((cond_index==longest_rule-1)&&(rule_index==num_of_rules-1)&&(num_of_rules%2!=0)) //if final location is at the lower right corner
            location=lowerRightVerticalMarch(rule_index,cond_index);
        else if((cond_index==0)&&(rule_index==num_of_rules-1)&&(num_of_rules%2==0))  //if final location is at lower left corner
            location=lowerLeftVerticalMarch(rule_index,cond_index);
         else if((cond_index!=0)&&(rule_index%2!=0))
             {
                    cond_index=cond_index-1;     //move left
             }
         else if((cond_index==0)&&(rule_index%2!=0))
             {
                rule_index=rule_index+1;   //Move down
             }
         else if((rule_index%2==0)&&(cond_index!=longest_rule-1))
        {
             cond_index=cond_index+1; //Move right
        }
       else if((rule_index%2==0)&&(cond_index==longest_rule-1))
        {
           rule_index=rule_index+1;     //Move down
        }
        location[0]=rule_index;
        location[1]=cond_index;
        return location;
    }

    public int [] getLargestAdjacentPheromone(int rule_index, int cond_index, RuleSet R)
    {//it returns the adjacent location that has the highest amount of getPheromone.
     //if there is no getPheromone around the indicated location, it returns null.
        Object element;
        int [] location = new int [2];
        double max_pheromone=0;
        double pheromone1=0, pheromone2=0, pheromone3=0,pheromone4=0,pheromone5=0,pheromone6=0,pheromone7=0,pheromone8=0;
        //Check getPheromone on the upper adjacent cell
        element= R.getElementAt(rule_index-1, cond_index); 
        if(element!=null)//if the location is not an empty one
        {
            if(element instanceof Condition)//the cell is a condition..
            {
                pheromone1=((Condition)element).getPheromone();//get condition Pheromone
            }
            else  //if the cell is a label
            {
                Rule rule = (Rule)((R.getRules()).get(rule_index-1));//get the concerned rule
                pheromone1 = rule.getLabelPheromone();//get the Pheromone of this rule's label
            }
            if(pheromone1>max_pheromone)//if it has maximum Pheromone, save it
            {
                max_pheromone=pheromone1;
                location[0]=rule_index-1;
                location[1]=cond_index;
            }
        }
        //check if Pheromone on the upper-right cell is Max.(same concept!!!! WRITE ONE METHOD FOR THE 8 CASES!!!!)
        element= R.getElementAt(rule_index-1, cond_index+1); 
        if(element!=null)
        {
            if(element instanceof Condition)
            {
                pheromone2=((Condition)element).getPheromone();
            }
            else
            {
                Rule rule = (Rule)((R.getRules()).get(rule_index-1));
                pheromone2 = rule.getLabelPheromone();
            }
            if(pheromone2>max_pheromone)
            {
                max_pheromone=pheromone2;
                location[0]=rule_index-1;
                location[1]=cond_index+1;
            }
        }
        //check if Pheromone on the right cell is Max.(same concept)
        element= R.getElementAt(rule_index, cond_index+1); 
        if(element!=null)
        {
            if(element instanceof Condition)
            {
                pheromone3=((Condition)element).getPheromone();
            }
            else
            {
                Rule rule = (Rule)((R.getRules()).get(rule_index));
                pheromone3 = rule.getLabelPheromone();
            }
            if(pheromone3>max_pheromone)
                {
                max_pheromone=pheromone3;
                location[0]=rule_index;
                location[1]=cond_index+1;
                }
        }
        //check if Pheromone on the lower right cell is Max.(same concept)
        element= R.getElementAt(rule_index+1, cond_index+1); 
        if(element!=null)
        {
            if(element instanceof Condition)
            {
                pheromone4=((Condition)element).getPheromone();
            }
            else
            {
                Rule rule = (Rule)((R.getRules()).get(rule_index+1));
                pheromone4 = rule.getLabelPheromone();
            }
            if(pheromone4>max_pheromone)
            {
                max_pheromone=pheromone4;
                location[0]=rule_index+1;
                location[1]=cond_index+1;
            }
        }
        //check if getPheromone on the lower cell is Max.(same concept)
        element= R.getElementAt(rule_index+1, cond_index); 
        if(element!=null)
        {
            if(element instanceof Condition)
            {
                pheromone5=((Condition)element).getPheromone();
            }
            else
            {
                Rule rule = (Rule)((R.getRules()).get(rule_index+1));
                pheromone5 = rule.getLabelPheromone();
            }
            if(pheromone5>max_pheromone)
            {
                max_pheromone=pheromone5;
                location[0]=rule_index+1;
                location[1]=cond_index;
            }
        }
        //check if getPheromone on the lower-left cell is Max.(same concept)
        element= R.getElementAt(rule_index+1, cond_index-1); 
        if(element!=null)
        {
            if(element instanceof Condition)
            {
                pheromone6=((Condition)element).getPheromone();
            }
            else
            {
                Rule rule = (Rule)((R.getRules()).get(rule_index+1));
                pheromone6= rule.getLabelPheromone();
            }
            if(pheromone6>max_pheromone)
            {
                max_pheromone=pheromone6;
                location[0]=rule_index+1;
                location[1]=cond_index-1;
            }
        }
        //check if getPheromone on the left cell is Max.(same concept)
        element= R.getElementAt(rule_index, cond_index-1); 
        if(element!=null)
        {
            if(element instanceof Condition)
            {
                pheromone7=((Condition)element).getPheromone();
            }
            else
            {
                Rule rule = (Rule)((R.getRules()).get(rule_index));
                pheromone7 = rule.getLabelPheromone();
            }
            if(pheromone7>max_pheromone)
            {
                max_pheromone=pheromone7;
                location[0]=rule_index;
                location[1]=cond_index-1;
            }
        }
        //check if getPheromone on the upper-left cell is Max.(same concept)
        element= R.getElementAt(rule_index-1, cond_index-1);  
        if(element!=null)
        {
            if(element instanceof Condition)
            {
                pheromone8=((Condition)element).getPheromone();
            }
            else
            {
                Rule rule = (Rule)((R.getRules()).get(rule_index-1));
                pheromone8 = rule.getLabelPheromone();
            }
            if(pheromone8>max_pheromone)
            {
                max_pheromone=pheromone8;
                location[0]=rule_index-1;
                location[1]=cond_index-1;
            }
        }
        if((pheromone1==pheromone2)&&(pheromone2==pheromone3)&&(pheromone3==pheromone4)&&(pheromone4==pheromone5)
                &&(pheromone5==pheromone6)&&(pheromone6==pheromone7)&&(pheromone7==pheromone8))     //If we have a tie... go randomly to any
        {                                                                      
            Random rand = new Random(System.currentTimeMillis());
            int random_num=rand.nextInt(8);
            switch(random_num)
            {
                case 0:
                {
                    location[0]=rule_index-1;
                    location[1]=cond_index;
                    break;
                }
                case 1:
                {
                     location[0]=rule_index-1;
                    location[1]=cond_index+1;
                    break;
                }
                case 2:
                {
                location[0]=rule_index;
                location[1]=cond_index+1;
                break;
                }
                case 3:
                {
                location[0]=rule_index+1;
                location[1]=cond_index+1;
                break;
                }
                case 4:
                {
                location[0]=rule_index+1;
                location[1]=cond_index;
                break;
                }
                case 5:
                {
                location[0]=rule_index+1;
                location[1]=cond_index-1;
                break;
                }
                case 6:
                {
                location[0]=rule_index;
                location[1]=cond_index-1;
                break;
                }
                case 7:
                {
                location[0]=rule_index-1;
                location[1]=cond_index-1;
                break;
                }
             }
        }
        if(max_pheromone==0)//if all adjacent cells have no getPheromone...
            {
            return null;    //return null in order to continue on the S-march in this case.
        }
        else
        {
            return location;//return the location with the highest getPheromone
            }
    }

    public RuleSet perturbCell(RuleSet initial_rule_set, int []location)   
    {//takes the rule set and the specific location in it that needs to be perturbed. It updates the location according to a certain criteria.
        //and then it returns the updated rule set.

        int rule_index = location[0];
        int cond_index=location[1];
        Object element = initial_rule_set.getElementAt(rule_index, cond_index); // the element (condition or label) that needs to be perturbed
        Condition initial_cond, new_cond;
	if(element instanceof Condition)  //if the selected element is a Condition ...
        { 
            initial_cond= (Condition)element;
            if(initial_cond.isContinuous())//i.e: if the attribute in the condition is of a continuous type...and modifyOperator could be applied....
            {
                    initial_rule_set.computeConditionCutPoints(initial_cond);
                    new_cond=initial_cond.modifyValue();     //modify value
                    new_cond=new_cond.modifyOperator();     //modify operator
            }
            else   //if the condition doesn't have a continuous Value, we can only modify Value and not Operator.
                {
                    initial_rule_set.computeConditionValueArray(initial_cond);
                    new_cond=initial_cond.modifyValue();
		}
            initial_rule_set.setElementAt(rule_index, cond_index, new_cond);//we need to replace old condition in rule set with the new condition
             Rule new_rule=(Rule)initial_rule_set.getRules().get(rule_index);
            initial_rule_set.computeRuleConfusionArray(new_rule);
            initial_rule_set.computeRuleAccuracy(new_rule);
            initial_rule_set.computeRuleCoverage(new_rule);
            initial_rule_set.computeRuleQuality(new_rule);
	}
        else   //if element is a label
        {
            Rule R = (Rule)(initial_rule_set.getRules()).get(rule_index);  //get concerned rule..
            String [] labels = initial_rule_set.getLabels();    //get the array of all possible labels
            String new_label = R.modifyLabel(labels);     //return randomly a new label from the list of possible labels...
            initial_rule_set.setElementAt(rule_index, cond_index, new_label);   //replace the old label with the new label.
            R = (Rule)(initial_rule_set.getRules()).get(rule_index);
            if((R.getConditions()).size()==1)      //if the rule is the default class label rule, then there are no geCconditions
            {//since default class label is not good, initialize everything to 0
                R.setAccuracy(0);
                R.setCoverage(0);
                R.setQuality(0);
            }
            else    //if the rule is a normal rule and not the default class label...
            {
                initial_rule_set.computeRuleConfusionArray(R);
                initial_rule_set.computeRuleAccuracy(R);
                initial_rule_set.computeRuleCoverage(R);
                initial_rule_set.computeRuleQuality(R);
            }
        }
        initial_rule_set.selectGoodRules();
        return initial_rule_set;
    }

   public RuleSet pruneConditions(RuleSet rule_set)
    {
        LinkedList rules = rule_set.getRules();
        String path = Program.getOutputFolder();
        String file_name ="ConditionPruningLog"+rule_set.getFileName()+""+rule_set.getDataSetFile().getName();
        path=path+"/"+file_name;
       for(int i=0; i<rules.size()-1; i++)
        {
            Rule rule = (Rule)rules.get(i);
            LinkedList conditions = rule.getConditions();
            for(int j=0; j<conditions.size()-1; j++)//size - 2 since there should be at least 2 conditions (other than the class label) in the rule...
            {
                Condition tested_cond = (Condition)conditions.get(j);
                for(int k=0; k<conditions.size()-1; k++)
                {
                    if(k!=j)//so that we won't compare the tested condition with itself.
                    {
                        Condition current_cond = (Condition)conditions.get(k);
                    if(tested_cond.equals(current_cond))//(equal is also covered by implies... there is no need for this if!!!
                    {
                      conditions.remove(tested_cond);
                         rule.setLength(conditions.size());
                        j=j-1;
                        k=conditions.size();//to break out of loop of testing the deleted test_condition
                    }
                    else if((current_cond.contradicts(tested_cond))||(current_cond.implies(tested_cond)))
                    {
                        conditions.remove(current_cond);
                        rule_set.computeRuleConfusionArray(rule);
                        rule_set.computeRuleAccuracy(rule);
                        rule_set.computeRuleCoverage(rule);
                        rule_set.computeRuleQuality(rule);
                        double current_rule_quality=rule.getQuality();
                        conditions.add(k,current_cond);
                        conditions.remove(tested_cond);
                        rule_set.computeRuleConfusionArray(rule);
                        rule_set.computeRuleAccuracy(rule);
                        rule_set.computeRuleCoverage(rule);
                        rule_set.computeRuleQuality(rule);
                        double tested_rule_quality=rule.getQuality();
                        if(current_rule_quality>tested_rule_quality)
                        {
                         conditions.add(k,tested_cond);
                            conditions.remove(current_cond);
                            rule.setLength(conditions.size());
                            if(k<j)
                                j=j-1;
                            k=conditions.size();
                        }
                         else
                        {
                            j=j-1;
                             k=conditions.size();
                             rule.setLength(conditions.size());
                        }
                    }
                    }
                }
                if(conditions.size()==1)//if all conditions were removed some how, remove the whole rule.
                    {
                    rules.remove(rule);
                    }
                rule_set.setSize(rules.size());
            }
            }
        rule_set.initializeRuleSetVars();
        longest_rule=rule_set.getLongestRuleSize();
        num_of_rules=rules.size();
        return rule_set;
    }
}
