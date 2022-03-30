//******************************************************************************
//Name:Gioia Wehbe.
//Student's number: 200801378.
//Course: Capstone Project
//Last modified on:
//Describtion:an ADT to create Rule objects. It also includes methods
//describing computations that will be performed on rules.
//******************************************************************************


import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.io.*;

public class Rule implements Comparable
{

    private LinkedList conditions;
    private String class_label;
    private int length; //getLength of the rule, geCconditions + class labael
    private double coverage;
    private double quality;
    private double accuracy;
    private boolean is_good; //label indicating good rules if it is true.
    private double label_pheromone; //the getValue of getPheromone for rule class label
    private double[] confusion_array;
    private static String IMPLICATION_STRING="-> "; // the string that indicates "implies" in a rule.
    private String [] attributes;

    //-------------------------------------------
    //Constructors
    //-------------------------------------------

    public Rule(LinkedList conditions, String class_label)
    {//Class constructor. a rule is a list of conditions and a class label
        this.class_label = class_label;
        conditions.add(class_label); //Add class label to geCconditions list
        this.conditions = conditions;
        length = conditions.size();  //getLength of rule includes class label
        attributes = getAttributes(Program.getMetrics());
    }

    public Rule(LinkedList conditions,String class_label,int length, double coverage, double quality, double accuracy,
             boolean is_good, double label_pheromone, double [] confusion_array, String [] attributes)
    {//constructor used to create copies of a rule object
        this.conditions=conditions;
        this.class_label=class_label;
        this.length=length;
        this.coverage=coverage;
        this.quality=quality;
        this.accuracy=accuracy;
        this.is_good=is_good;
        this.label_pheromone=label_pheromone;
        this.confusion_array=confusion_array;
        this.attributes= attributes;
    }

    public Rule duplicate()
    {//Method used to create copies of a rule object
         LinkedList conditions_copy = new LinkedList();
        for(int i=0; i<conditions.size()-1; i++)
        {
            Condition cond = (Condition)conditions.get(i);
            Condition new_cond = cond.duplicate();
            conditions_copy.add(new_cond);
        }
        conditions_copy.add(class_label);
        Rule copy = new Rule(conditions_copy,class_label,length, coverage, quality, accuracy, is_good, label_pheromone,
                confusion_array,attributes);
        return copy;
    }

    //--------------------------------------------------------------------------
    //Getter methods
    //--------------------------------------------------------------------------
    public LinkedList getConditions() {
        return conditions;
    }

    public String getClassLabel() {
        return class_label;
    }

    public int getLength() {
        return length;
    }

    public double getCoverage() {
        return coverage;
    }

    public double getQuality() {
        return quality;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public boolean isGood() {
        return is_good;
    }

    public double getLabelPheromone() {
        return label_pheromone;
    }

    public double[] getConfusionArray() {
        return confusion_array;
    }

    //--------------------------------------------------------------------------
    //Setter methods
    //--------------------------------------------------------------------------
    public void setConditions(LinkedList conditions) {
        this.conditions = conditions;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setClassLabel(String class_label) {
        this.class_label = class_label;
    }

    public void setCoverage(double coverage) {
        this.coverage = coverage;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public void setQuality(double quality) {
        this.quality = quality;
    }

    public void setIsGood(boolean value) {
        is_good = value;
    }

    public void setLabelPheromone(double pheromone) {
        label_pheromone = pheromone;
    }

    public void setConfusionArray(double[] confusion_array) {
        this.confusion_array = confusion_array;
    }

    //--------------------------------------------------------------------------
    //Computational methods                     
    //--------------------------------------------------------------------------
    public boolean hasCondition(Condition cond) 
    {//checks if the rule has the parameter condition
        for (int i = 0; i < conditions.size() - 1; i++)
        {
            if (cond.equals((Condition) conditions.get(i))) {
                return true;
            }
        }
        return false;
    }

    public Condition hasAttribute(String str) 
    {//checks if the rule has the parameter attribute and returns the condition with this attribute.
        for (int i = 0; i < conditions.size() - 1; i++) {
            Condition cond = (Condition) conditions.get(i);
            if (cond.hasAtribute(str)) {
                return cond;
            }
        }
        return null;
       }

     public boolean test(String line)
    {// Borrowed from Khaled Bakhit's code. modified to match my rule object
        Scanner scan= new Scanner(line);
        scan.useDelimiter(",");
        boolean match= true;
        String nbr;
        int index=0;
        Condition cond=new Condition("","","");
        for(int i=0; i<attributes.length; i++)
        {
            nbr= scan.next().trim();
            for(int j = 0; j<conditions.size()-1;j++)
            {
                cond = (Condition)conditions.get(j);
                if(attributes[i].equals(cond.getOperand()))
                {
                    index=j;
                    j=conditions.size()-1;//break out of loop
                    }
                else
                    index = -1;
            }
            if(index>=0)
            {
                match= analyze(nbr,cond.getOperator(),cond.getValue());
                if(!match)
                    break;
            }
        }
        return match;
    }

      private boolean analyze(String nbr, String operation, String value)
    {//Borrowed from Khalid Bakhit's code
        double number= Double.parseDouble(nbr);
        double val= Double.parseDouble(value.trim());
        if(operation.equalsIgnoreCase("="))
            return (number==val);
          else  if(operation.equalsIgnoreCase("<"))
            return (number<val);
          else  if(operation.equalsIgnoreCase("<="))
            return (number<=val);
          else  if(operation.equalsIgnoreCase(">"))
            return (number>val);
          else
            return (number>=val);
    }

    //--------------------------------------------------------------------------
    //Modificaton method
    //--------------------------------------------------------------------------
    public String modifyLabel(String[] labels) {//takes an array of labels and randomly chooses one
        //that is different from the initial label and it returns it
        int length = labels.length;

        int rand_index;
        String new_label;
         if(length==2)
        {
            new_label=labels[0];
            if(new_label.compareTo(class_label) == 0)
                new_label=labels[1];
        }
        else
         {
             Random rand = new Random(System.currentTimeMillis());
            do {
            rand_index = rand.nextInt(length);
            new_label = labels[rand_index];
        } while (new_label.compareTo(class_label) == 0); //repeat choosing random label as long as it is the same as initial one
        }
        return new_label;
    }

    //-----------------------------------------------
    // Printing Methods
    //-----------------------------------------------

    @Override
public String toString()
{
        String rule ="";
             for (int i = 0; i < conditions.size() - 1; i++)
             {
            rule = rule + ((Condition) conditions.get(i)).toString() + "\n";
        }
        rule = rule + IMPLICATION_STRING+" class "+class_label+"  [qu:"+quality+" acc:"+accuracy+" cov:"+coverage+"]"+"\n";
        return rule;

}

    //----------------------------------------------------
    //Pruning Methods
    //----------------------------------------------------
    
    public boolean equals(Rule rule)
    {//checks if 2 rules are equal
        if(length==rule.getLength())
        {
            if(class_label.equals(rule.getClassLabel()))
            {
                for(int i=0; i<length-1;i++)
                {
                    Condition this_cond = (Condition)conditions.get(i);
                    Condition rule_cond = (Condition)rule.getConditions().get(i);
                    if(!this_cond.equals(rule_cond))
                        return false;
                }
                return true;
            }
        }
        return false;
    }


public boolean implies(Rule rule)
{// Written by Anthothony Naser. Checks if one rule implies the other...
    boolean result = true;
    if(length == rule.getLength())
    {
        if(class_label.compareTo(rule.getClassLabel())==0)
        {
            boolean[] dummy = new boolean[conditions.size()];
            for(int i = 0; i < conditions.size()-1; i++)
            {
                dummy[i] = ((Condition)conditions.get(i)).impliesCondIn(rule);
            }
            for(int i = 0; i < dummy.length-1; i++)
            {
                if(!dummy[i])
                {
                    return false;
                }
            }
            return result;
        }
        else
        {
            return false;
        }
    }
    else
    {
        return false;
    }
}

public boolean contradicts(Rule rule)
    {// Checks if 2 rules are conradicting to each oher.
        if(length==rule.getLength())
        {
            if(!class_label.equals(rule.getClassLabel()))
            {
            for(int i=0; i<conditions.size()-1; i++)
            {
               Condition this_condition= (Condition)conditions.get(i);
               Condition rule_condition = (Condition)rule.getConditions().get(i);
               if(!this_condition.equals(rule_condition))
                   return false;
            }
            return true;
            }
        }
        return false;
    }

/**
     * Determines name of attributes involved
     * @param filename Name of file to look into (include path if needed)
     * @return String array containing names
     */
     public static String[] getAttributes(String filename)
    {//borrowed from Khaled
        try {
            Scanner scan = new Scanner(new File(filename));
            scan.useDelimiter("\n");
            int count=0;
            String m;
            while(scan.hasNext())
            {
                m= scan.next().trim();
                if(m.length()>1)
                    count++;
            }
           scan = new Scanner(new File(filename));
           scan.useDelimiter("\n");
           String[] array= new String[count];
           count=0;
            while(scan.hasNext() && count<array.length)
            {
                 m= scan.next().trim();
                if(m.length()>1)
                {
                array[count]=m;
                count++;
                }
            }
           return array;

        }
        catch (FileNotFoundException ex)
        {
            System.out.println(filename+" cannot be found");
            return null;
        }
    }

    public int compareTo(Object r) 
    {
        double this_quality=this.getQuality();
        double r_quality=((Rule)r).getQuality();
        if(this_quality>r_quality)
            return -1;
        else if(this_quality==r_quality)
             return 0;
        else
            return 1;
    }
}
