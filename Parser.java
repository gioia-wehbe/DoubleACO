//******************************************************************************
//Name:Gioia Wehbe.
//Student's number: 200801378.
//Project Name: Double ACO
//Last modified on: 3/12/2011
//Describtion: A classes the includes methods for parsing ruleset text file.
// These methods are then used in order to create rule set objects.
//******************************************************************************

import java.io.*;
import java.util.LinkedList;

public class Parser
{
    private String line="";
    private LinkedList rule_list=new LinkedList();

        public LinkedList ruleSet(String file_name)
    {
        try
        {
            FileReader fr = null;
            fr = new FileReader(file_name);
            BufferedReader br = new BufferedReader(fr);
            line = br.readLine();
            while((line!=null)&&(line.startsWith("Final rules from tree")==false))
                line = br.readLine();
            if(line!=null)
            {
                while((line!=null)&&(line.startsWith("Rule")==false))
                    line = br.readLine();
            String rule_element = line;//get number of 1st rule
            String rule_line="";
            while(!rule_element.startsWith("Default"))
            {
            rule_element=br.readLine();//get first element (condition or label) of the rule
            while((!rule_element.startsWith("Rule"))&&(!rule_element.startsWith("Default")))
            {
                 rule_element = rule_element.trim();
                if(rule_element.compareTo("")!=0)
                {
                    if(rule_element.contains("class"))
                    {
                        rule_element=rule_element.substring(rule_element.indexOf("c"),rule_element.indexOf('['));
                        rule_element = rule_element.trim();
                    }
                     rule_line =rule_line+rule_element+",";
                }

                rule_element=br.readLine();
            }
            rule_list.add(rule_line);
            rule_line="";
            }
            rule_list.add(rule_element);
                    return rule_list;
            }
            else
                 return null;
        }
        catch (IOException ex)
        {
            System.out.println("file not found");
            return null;
        }
        }
        
        public String [] getRule(LinkedList rule_set,int rule_index)
        {
            if(rule_index<rule_set.size()-1)
            {
                 String rule = (String)rule_set.get(rule_index);
                String [] rule_array=rule.split(",");
                return rule_array;
            }
            else
                return null;
        }

        public String getDefaultClassLabel(LinkedList rule_set)
        {
                 String def_class = (String)rule_set.get(rule_set.size()-1);
                String [] def_class_array=def_class.split(" ");
                return def_class_array[2];
        }

        public String [] getCondition(String [] rule, int cond_index)
        {
            String cond;
            if(cond_index<rule.length-1)
            {
                cond = rule[cond_index];
                String [] cond_array = cond.split(" ");
                return cond_array;
            }
            else return null;
        }

        public String getClassLabel(String [] rule)
        {
            String label;
            label = rule[rule.length-1];
            String [] label_array = label.split(" ");
            return label_array[1];
        }


        public String getOperand(String [] condition)
        {
            if(condition!=null)
            {
                String operand = condition[0];
                operand = operand.trim();
                return operand;
            }
            else return null;
        }

        public String getOperator(String [] condition)
        {
            if(condition!=null)
            {
                String operator = condition[1];
                operator = operator.trim();
                return operator;
            }
            else return null;

        }

        public String getValue(String [] condition)
        {
            if(condition!=null)
            {
                String value = condition[2];
                value = value.trim();
                return value;
            }
            else return null;

        }

}
