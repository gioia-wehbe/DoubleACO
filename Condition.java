//******************************************************************************
//Name:Gioia Wehbe.
//Student's number: 200801378.
//Project Name: Double ACO
//Last modified on: 3/12/2011
//Describtion:an ADT to create condition objects. It also includes methods
//describing computations that will be performed on conditions.
//******************************************************************************


import java.util.LinkedList;
import java.util.Random;


public class Condition
{
    private String operand;
    private String operator;
    private String value;
    private double pheromone;
    private LinkedList cut_points;    //list of all the cut-point values for a certain continuous attribute, needed to modify conditions in
                                      //the small ACO
    private LinkedList values;     //list of all the values for a certain discrete attribute, needed to modify conditions in the small ACO

    //---------------------------------
    //Constructors
    //---------------------------------
    
    public Condition(String operand, String operator,String value)
    {//class constructor
        this.operand = operand;
        this.operator = operator;
        this.value = value;
    }

    public Condition(String operand, String operator,String value,double pheromone)
    {//class constructor
        this.operand = operand;
        this.operator = operator;
        this.value = value;
        this.pheromone = pheromone;
    }

    public Condition(String operand, String operator,String value,double pheromone,
            LinkedList values, LinkedList cut_points)
    {//class constructor used to create dublicates of the condition object
        this.operand = operand;
        this.operator = operator;
        this.value = value;
        this.pheromone = pheromone;
        this.values=values;
        this.cut_points=cut_points;
    }

    public Condition duplicate()
    {//A method used to create copies of a condition object
        Condition copy = new Condition(operand,operator,value,pheromone,values,cut_points);
        return copy;
    }
    
    //--------------------------------------------------------------------------
    //getter methods
    //--------------------------------------------------------------------------
    public String getOperator()
    {
        return operator;
    }

    public String getOperand()
    {
        return operand;
    }

    public String getValue()
    {
        return value;
    }

     public double getPheromone()
    {
        return pheromone;
    }


    //--------------------------------------------------------------------------
    //setter methods
    //--------------------------------------------------------------------------
    public void setOperator(String operator)
    {
        this.operator = operator;
    }
    public void setOperand(String operand1)
    {
       this.operand = operand1;
    }

    public void setValue(String value)
    {
        this.value=value;
    }

     public void setPheromone(double pheromone)
    {
        this.pheromone=pheromone;
    }

     public void setCutpoints(LinkedList cut_points)
    {
        this.cut_points=cut_points;
    }

     public void setValues(LinkedList values)
    {
        this.values=values;
    }

    //--------------------------------------------------------------------------
    //Compution methods                             
    //--------------------------------------------------------------------------

    public boolean isContinuous()
    {//checks if a certain attribute is continuous or discrete
        if(operator.compareTo("=")==0)
        {
            return false;
        }
        else
            return true;
    }

    public boolean hasAtribute(String str)
    {//checks if a certain condition has an attribute str
        return operand.equals(str);
    }

    //--------------------------------------------------------------------------
    //modification methods
    //--------------------------------------------------------------------------                                                                                            //??????????????????????????????
                                                                                            
    public Condition modifyOperator()                                                       
    {//checks the current Operator of the condition and returns a new condition
     //with the inverse Operator.
        String new_operator="";                                                             
        if(isContinuous())
        { 
            if(operator.compareTo("<")==0)
                new_operator=">=";
            else if(operator.compareTo(">")==0)
                new_operator="<=";
            else if(operator.compareTo("<=")==0)
                new_operator=">";
            else if(operator.compareTo(">=")==0)
                new_operator="<";
            return new Condition(operand,new_operator,value,pheromone);
        }
        else return null;
    }

    public Condition modifyValue()
    {//returns a new condition with a new getValue chosen randomly from the cut_point values list(if continuous) and from the values list
        //(if discrete)
       int rand_num;
        String new_value;
        if(isContinuous())
        {
                if(cut_points.size()<=1)
                {
                    return this;
                    }
                else
                {
                    if(cut_points.size()==2)
                         {
                             new_value = ""+(Double)cut_points.get(0);
                             if(value.compareToIgnoreCase(new_value)==0)
                                 new_value = ""+(Double)cut_points.get(1);
                         }
                        else
                         {
                             Random rand = new Random(System.currentTimeMillis());
                         do
                        {
                            
                            rand_num=rand.nextInt(cut_points.size());
                            new_value=""+(Double)cut_points.get(rand_num);
                        }while(value.compareToIgnoreCase(new_value)==0);
                        }
                     return new Condition(operand,operator, new_value,pheromone);
                }                       
        }
        else   //if discrete
        {
            if(values.size()<=1)
            {
                return this;
                }
            else
            {
                 if(values.size()==2)
                         {
                             new_value = ""+values.get(0);
                             if(value.compareToIgnoreCase(new_value)==0)
                                 new_value = ""+values.get(1);
                         }
                else
                 {
                     Random rand = new Random(System.currentTimeMillis());
                     do
                 {
                     rand_num=rand.nextInt((values.size()));
                     new_value=""+(String)values.get(rand_num);
                 }while(value.compareToIgnoreCase(new_value)==0);
                }
                 return new Condition(operand,operator, new_value,pheromone);
            }
        }
    }
    
    //-------------------------------------------------------
    // pruning methods
    //-------------------------------------------------------

    public boolean equals(Condition cond)
    {
        if((operand.equals(cond.getOperand()))&&(operator.equals(cond.getOperator()))
                &&(Double.parseDouble(value)==Double.parseDouble(cond.getValue())))
            return true;
        else
            return false;
    }

    public boolean implies(Condition cond)//if this implies cond, cond must be removed
{//written by Anthony Nasser, updated by me.
        // 0 -> A doesn't imply B (remove none)
        // 1 -> A implies B (romove B)
        // 2 -> A implies B except for the "=" case (replace B by "=" condition)
    String cond_operand = cond.getOperand();
    String cond_operator = cond.getOperator();
    String cond_value = cond.getValue();
    double numeric_cond_value = Double.parseDouble(cond_value);
    double numeric_this_value = Double.parseDouble(value);
    if(operand.compareTo(cond_operand)==0)
    {
        if(operator.equals(">"))
        {
            if(cond_operator.equals(">"))
            {
                if(numeric_this_value >= numeric_cond_value)
                    return true;//remove argument (smaller value)!!!
                else if(numeric_this_value < numeric_cond_value)
                    return false; //don't remove anything
            }
            else if(cond_operator.equals(">="))
            {
                if(numeric_this_value >= numeric_cond_value)
                    return true;//replace cond by an "=" condition
                else if(numeric_this_value < numeric_cond_value)
                    return false; //don't remove anything
            }
        }
         else if(operator.equals(">="))
            {
                if(cond_operator.equals(">="))
                {
                    if(numeric_this_value >= numeric_cond_value)
                    return true;//replace cond by an "=" condition
                else if(numeric_this_value < numeric_cond_value)
                    return false; //don't remove anything
                }
                else if(cond_operator.equals(">"))
                {
                    if(numeric_this_value >= numeric_cond_value)
                        return true;//remove argument cond
                    else if(numeric_this_value < numeric_cond_value)
                        return false; //don't remove anything
                }
            }
            else if(operator.equals("<"))
            {
                if(cond_operator.equals("<"))
            {
                if(numeric_this_value > numeric_cond_value)
                    return false;//do not remove anything
                else if(numeric_this_value <= numeric_cond_value)
                    return true; //remove argument cond (Remove greater value)
            }
            else if(cond_operator.equals("<="))
            {
                if(numeric_this_value > numeric_cond_value)
                    return false;//do not remove anything
                else if(numeric_this_value <= numeric_cond_value)
                    return true; //replace cond by an "=" condition
            }
            }
            else if(operator.equals("<="))
            {
                if(cond_operator.equals("<"))
            {
                if(numeric_this_value > numeric_cond_value)
                    return false;//do not remove anything
                else if(numeric_this_value <= numeric_cond_value)
                    return true; //remove argument cond
            }
            else if(cond_operator.equals("<="))
            {
                if(numeric_this_value > numeric_cond_value)
                    return false;//do not remove anything
                else if(numeric_this_value <= numeric_cond_value)
                    return true; //replace cond by an "=" condition
            }
            }
        else if(operator.equals("="))
        {
            if(cond_operator.equals("="))
            {
                if(numeric_this_value == numeric_cond_value)
                    return true;//remove argument cond
                else return false;
            }
            else return false;
        }

    }
    return false; // no implication, don't remove anything...
}

    public boolean impliesCondIn(Rule rule)
{//Written by Anthony Naser
    boolean dummy = false;
    for(int i = 0; i < rule.getConditions().size()-1;i++)
    {
        if(this.implies((Condition)rule.getConditions().get(i)))
            return true;
    }
    return dummy;
}

    public boolean contradicts(Condition cond)
   {// a method that checks if one condition is contradicting with another in the same rule
        String cond_operand = cond.getOperand();
        String cond_operator = cond.getOperator();
        String cond_value = cond.getValue();
        double numeric_cond_value = Double.parseDouble(cond_value);
        double numeric_this_value = Double.parseDouble(value);
        if(operand.compareTo(cond_operand)==0)
        {
            if((operator.equals("<"))||(operator.equals("<=")))
            {
                if((cond_operator.equals(">"))||(cond_operator.equals(">=")))
                    if(numeric_this_value>numeric_cond_value)
                        return false;
                    else return true;
            }
            else if((operator.equals(">"))||(operator.equals(">=")))
            {
                if((cond_operator.equals("<"))||(cond_operator.equals("<=")))
                    if(numeric_this_value<numeric_cond_value)
                        return false;
                    else return true;
        }
        }
        return false;
    }

    //---------------------------------------------------
    // printing methods
    //---------------------------------------------------

    @Override
    public String toString()
    {
        return operand+" "+operator+" "+value;
    }

    
}
