import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Ex1 {
    public static void main(String[] args) {
        String qs = "";
        try {
            qs = Files.readString(Paths.get("input.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.print(qs);
        String[] lines = qs.split("\r");
        String net = lines[0];

        File f  = new File(net);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(f);
            doc.getDocumentElement().normalize();
            HashMap<String,ArrayList<String>> variables = new HashMap<>();
            HashMap<String,ArrayList<String>> definitions = new HashMap<>();

            NodeList vars = doc.getElementsByTagName("VARIABLE");
            NodeList defs = doc.getElementsByTagName("DEFINITION");

            variables = turn_to_hash(vars,"NAME");
            definitions = turn_to_hash(defs, "FOR");



            for(int i = 1; i < lines.length; i++){
                if (lines[i].charAt(1) == 'P'){
                    System.out.print(lines[i]);
                    System.out.print("\n");
                    String[] h = lines[i].split(" ")[1].split("-");
                    String query = lines[i].split("\\|")[0].split("\\(")[1];
                    String[] e = lines[i].split("\\|")[1].split("\\)")[0].split(",");
                    ArrayList<String> evidence = new ArrayList<>(Arrays.asList(e));
                    ArrayList<String> hidden = new ArrayList<>(Arrays.asList(h));
                    VariableElimination(variables,definitions,evidence,query,hidden);
                }
                else{
                    boolean ans = BayesBall(definitions,lines[i]);
                    if (ans){System.out.print("yes");}
                    else{System.out.print("no");}
                    System.out.print("\n");
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }


    }

    public static boolean BayesBall(HashMap<String,ArrayList<String>> net, String q){
        ArrayList<String> evidence = new ArrayList<>();
        String[] splited_q = q.split("\\|");
        if(splited_q.length == 2){
            String e = splited_q[1];
            String[] given = e.split("\\,");
            for (String s : given) {
                evidence.add(s.split("\\=")[0]);
            }
        }
        String vars = splited_q[0].substring(1);
        String start = vars.split("\\-")[0];
        String finish = vars.split( "\\-")[1];





        return BayesBallAlgo(net,evidence,start,finish);
    }

    public static boolean BayesBallAlgo(HashMap<String,ArrayList<String>> net,ArrayList<String> evidence, String start, String finish){
        ArrayList<String> explored = new ArrayList<>();
        Queue<String> q = new LinkedList<>();
        Queue<String> states = new LinkedList<>();
        q.add(start);
        states.add("all");
        while (!q.isEmpty()){
           String v = q.remove();
           String state = states.remove();
           explored.add(v);
           if (Objects.equals(v, finish)){
               return false;
           }
           for (Map.Entry var: net.entrySet()){
               if (net.get(var.getKey()).contains(v) && !evidence.contains(v) && !Objects.equals(state, "parent")){
                   if (!q.contains(var.getKey()) && !explored.contains(var.getKey())){
                       q.add((String) var.getKey());
                       if (evidence.contains((String) var.getKey())){
                           states.add("parent");
                       }
                       else {
                           states.add("child");
                       }
                   }
               }

           }
            if (!Objects.equals(state, "child")){
                for(int i = 0; i < net.get(v).size(); i++){
                    if (net.containsKey(net.get(v).get(i)) && !evidence.contains(net.get(v).get(i)) && Collections.frequency(explored,net.get(v).get(i)) != 2){
                        explored.add(net.get(v).get(i));
                        q.add(net.get(v).get(i));
                        states.add("all");
                    }
                }
            }
        }
        return true;
    }


    public static float VariableElimination(HashMap<String,ArrayList<String>> variable_net,HashMap<String,ArrayList<String>> net,ArrayList<String> e, String query, ArrayList<String> hidden){
        HashMap<String,ArrayList<String>> factors = new HashMap<>();
        HashMap<String,String> evidence = new HashMap<>();
        for(String s : e){
            String[] splited = s.split("=");
            evidence.put(splited[0],splited[1]);
        }
        for (Map.Entry var:net.entrySet()){
            ArrayList<String> cpt = net.get(var.getKey());
            cpt = new ArrayList<>(Arrays.asList(cpt.get(cpt.size() - 1).split(" ")));
            ArrayList<String> bool_options = variable_net.get(var.getKey());
            for (int i = 0; i < net.get(var.getKey()).size()-1; i ++){
                String parent = net.get(var.getKey()).get(i);
                if (evidence.containsKey(parent)){
                    String parent_bool_value = evidence.get(parent);
                    int cpt_value_start = bool_options.indexOf(parent_bool_value);
                    int group_bool_amount = net.get(var.getKey()).size()-i-1;//how many groups of the value we need to remove are there
                    double bool_group_size = Math.pow(2,group_bool_amount);//how many of the value are there in a row in the cpt
                    cpt_value_start *= bool_group_size;//in what index the value first appears
                    for (int j = 0; j < cpt.size(); j++){
                        double relative_index = j % (bool_options.size()*bool_group_size);
                        if (relative_index >= cpt_value_start && relative_index < cpt_value_start + bool_group_size){
                            continue;
                        }
                        else {
                            cpt.set(j,"none");
                        }
                    }
                }
            }
            if (evidence.containsKey(var.getKey())){
                String var_bool_value = evidence.get(var.getKey());
                int cpt_value_start = bool_options.indexOf(var_bool_value);
                for (int j = cpt_value_start; j < cpt.size(); j++){
                    if (cpt_value_start != j%bool_options.size()){
                        cpt.set(j,"none");
                    }
                }
            }
            ArrayList<String> new_cpt = new ArrayList<>();
            for (int j = 0; j < cpt.size(); j++){
                if (!Objects.equals(cpt.get(j), "none")){
                    new_cpt.add(cpt.get(j));
                }
            }
            if (new_cpt.size() > 1){
                factors.put((String) var.getKey(),new_cpt);
            }
        }
        System.out.print("FACTORS: " + factors);
        System.out.print("\n");

        // dealing with hidden variables


        for (String h : hidden){
            HashMap<String,HashMap<String,ArrayList<String>>> bool_table = new HashMap<>(create_bool_table(variable_net,factors));
            ArrayList<String> h_factor = factors.get(h);
            ArrayList<ArrayList<String>> factor_by_size = new ArrayList<>();//factors need to be joined from smallest to biggest
            ArrayList<String> keys_inside = new ArrayList<>();
            factor_by_size.add(h_factor);
            keys_inside.add(h);
            for (Map.Entry var:net.entrySet()){
                if (net.get(var.getKey()).contains(h)){//h is a parent of var
                    ArrayList<String> var_factor = factors.get(var.getKey());
                    Boolean added = false;
                    int size = factor_by_size.size();
                    for (int i = 0; i < size;i++){
                        if (factor_by_size.get(i).size() == var_factor.size()){
                            int var_sum = 0;
                            ArrayList<String> keys = new ArrayList<>(bool_table.get(var.getKey()).keySet());
                            for (int j = 0; j < keys.size(); j++){
                                var_sum += (int) keys.get(j).charAt(0);
                            }
                            int var1_sum = 0;
                            ArrayList<String> keys1 = new ArrayList<>(bool_table.get(keys_inside.get(i)).keySet());
                            for (int j = 0; j < keys1.size(); j++){
                                var1_sum += (int) keys1.get(j).charAt(0);
                            }
                            if (var_sum > var1_sum){
                                factor_by_size.add(i+1,var_factor);
                                keys_inside.add(i+1, (String) var.getKey());
                            }
                            else {
                                factor_by_size.add(i,var_factor);
                                keys_inside.add(i, (String) var.getKey());
                            }
                            added = true;

                        }
                        if (factor_by_size.get(i).size() > var_factor.size()){
                            factor_by_size.add(i,var_factor);
                            keys_inside.add(i, (String) var.getKey());
                        }
                    }
                    if (!added){
                        factor_by_size.add(factor_by_size.size()-1,var_factor);
                    }
                }
            }

            // joining factors
            ArrayList<String> cpt = new ArrayList<>(factor_by_size).get(0);
            factor_by_size.remove(0);
            String cpt_key = keys_inside.remove(0);
            if (factor_by_size.size() > 0){
                while (factor_by_size.size() != 0){
                    ArrayList<String> cpt1 = new ArrayList<>(factor_by_size).get(0);
                    factor_by_size.remove(0);
                    String cpt1_key = keys_inside.remove(0);
                    cpt = new ArrayList<>(join(cpt,cpt1,bool_table,cpt_key,cpt1_key));
                    cpt_key = cpt1_key;
                }
            }

            // eliminate

            for (int i = 0; i < cpt.size(); i++){

            }




        }
        return 1;
    }

    public static ArrayList<String> join(ArrayList<String> cpt, ArrayList<String> cpt1, HashMap<String,HashMap<String,ArrayList<String>>> bool_table, String cpt_key, String cpt1_key){
        ArrayList<String> cpt_keys = new ArrayList<>(bool_table.get(cpt_key).keySet());
        ArrayList<String> cpt1_keys = new ArrayList<>(bool_table.get(cpt1_key).keySet());
        ArrayList<String> shared = new ArrayList<>(cpt_keys.stream().filter(cpt1_keys::contains).collect(Collectors.toList()));
        for (int i = 0; i < cpt.size(); i++){
            for (int j = 0; j < cpt1.size(); j++){
                boolean should_join = true;
                for (int l = 0; l < shared.size(); l++){
                    if (!Objects.equals(bool_table.get(cpt_key).get(shared.get(l)).get(i), bool_table.get(cpt1_key).get(shared.get(l)).get(j))){
                        should_join = false;
                    }
                }
                if(should_join){
                    cpt1.set(j,String.valueOf(Integer.parseInt(cpt1.get(j)) * Integer.parseInt(cpt.get(i))));
                }
            }
        }
    }


    public static HashMap<String,HashMap<String,ArrayList<String>>> create_bool_table(HashMap<String,ArrayList<String>> variable_net,HashMap<String,ArrayList<String>> net){
        HashMap<String,HashMap<String,ArrayList<String>>> bool_table = new HashMap<>();
        for (Map.Entry var: net.entrySet()){
            HashMap<String,ArrayList<String>> var_bool_table = new HashMap<>();
            ArrayList<String> parents = new ArrayList<>(net.get(var.getKey()));
            ArrayList<String> values = new ArrayList<>(Arrays.asList(parents.remove(parents.size()-1).split(" ")));
            int table_size = values.size();
            for (int i = 0; i < parents.size(); i++){
                ArrayList<String> parent_bool_values = new ArrayList<>();
                double bool_group_size = Math.pow(2, parents.size()-i);
                ArrayList<String> bool_options = new ArrayList<>(variable_net.get(parents.get(i)));
                String current_bool_value = bool_options.get(0);
                for(int j = 0; j < table_size; j++){
                    if (j != 0 && j%bool_group_size == 0){
                        current_bool_value = bool_options.get((int)(j/bool_group_size)%bool_options.size());
                    }
                    parent_bool_values.add(current_bool_value);
                }
                var_bool_table.put(parents.get(i), parent_bool_values);
            }
            ArrayList<String> var_bool_values = new ArrayList<>();
            ArrayList<String> bool_options = new ArrayList<>(variable_net.get(var.getKey()));
            String current_bool_value = bool_options.get(0);
            var_bool_values.add(current_bool_value);
            for (int j = 1; j < table_size; j++){
                current_bool_value = bool_options.get(j%bool_options.size());
                var_bool_values.add(current_bool_value);
            }
            var_bool_table.put((String) var.getKey(), var_bool_values);
            bool_table.put((String) var.getKey(), var_bool_table);
        }
        return bool_table;
    }


    public static HashMap<String,ArrayList<String>> turn_to_hash(NodeList l, String tag){
        HashMap<String,ArrayList<String>> variables = new HashMap<>();
        for(int i = 0; i < l.getLength(); i++){
            ArrayList<String> outcomes = new ArrayList<>();
            Node v = l.item(i);
            String n = "";
            if(v.getNodeType() == Node.ELEMENT_NODE){
                NodeList v_details = v.getChildNodes();
                for (int j = 0; j < v_details.getLength(); j++){
                    Node d = v_details.item(j);
                    if (d.getNodeType() == Node.ELEMENT_NODE){
                        Element d_element = (Element)d;
                        if (d_element.getTagName() .equals(tag)){
                            n = d_element.getTextContent();
                        }
                        else{
                            outcomes.add(d_element.getTextContent());
                        }
                    }
                }
                variables.put(n,outcomes);
            }
        }
        return variables;
    }
}
