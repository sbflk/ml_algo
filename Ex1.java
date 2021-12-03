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


    public static float VariableElimination(HashMap<String,ArrayList<String>> variable_net,HashMap<String,ArrayList<String>> net1,ArrayList<String> e, String query, ArrayList<String> hidden){
        HashMap<String,ArrayList<String>> net = new HashMap<>(copy(net1));
        HashMap<String,ArrayList<String>> factors = new HashMap<>();
        HashMap<String,String> evidence = new HashMap<>();
        ArrayList<String> hidden_done = new ArrayList<>();
        ArrayList<String> eliminated_cpt = null;
        String cpt_key = "";
        HashMap<String,HashMap<String,ArrayList<String>>> bool_table = null;
        for(String s : e){
            String[] splited = s.split("=");
            evidence.put(splited[0],splited[1]);
        }
        System.out.print("STARTING NET: " + net);
        System.out.print("\n");
        for (Map.Entry var:net.entrySet()){
            System.out.print("CURRENT KEY: " + var.getKey());
            System.out.print("\n");
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

        System.out.print("DEALING WITH HIDDEN");
        System.out.print("\n");

        for (String h : hidden){
            System.out.print("DEALING WITH HIDDEN : " + h);
            System.out.print("\n");
            bool_table = new HashMap<>(create_bool_table(variable_net,net,evidence,hidden_done));
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
                        if (factor_by_size.get(i).size() == var_factor.size() && !added){
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
                        if (factor_by_size.get(i).size() > var_factor.size() && !added){
                            factor_by_size.add(i,var_factor);
                            keys_inside.add(i, (String) var.getKey());
                            added = true;
                        }
                    }
                    if (!added){
                        factor_by_size.add(var_factor);
                        keys_inside.add((String) var.getKey());
                    }
                }
            }

            System.out.print("FACTORS THAT INCLUDE HIDDEN: " + factor_by_size);
            System.out.print("\n");


            // joining factors
            ArrayList<String> cpt = new ArrayList<>(factor_by_size).get(0);
            factor_by_size.remove(0);
            cpt_key = keys_inside.remove(0);
            System.out.print("CPT KEY: " + cpt_key);
            System.out.print("\n");
            if (factor_by_size.size() > 0){
                while (factor_by_size.size() != 0){
                    ArrayList<String> cpt1 = new ArrayList<>(factor_by_size).get(0);
                    factor_by_size.remove(0);
                    String cpt1_key = keys_inside.remove(0);
                    System.out.print("CPT1 KEY: " + cpt1_key);
                    System.out.print("\n");
                    cpt = new ArrayList<>(join(cpt,cpt1,bool_table,cpt_key,cpt1_key));
                    cpt_key = cpt1_key;
                }
            }

            // eliminate

            System.out.print("CPT AFTER ALL JOINING: " + cpt);
            System.out.print("\n");
            System.out.print("CPT KEY: " + cpt_key);
            System.out.print("\n");
            System.out.print("CPT BOOL TABLE: " + bool_table.get(cpt_key));
            System.out.print("\n");
            int parents_number = bool_table.get(cpt_key).size()-1;
            eliminated_cpt = new ArrayList<>();
            double current_sum = 0;
            ArrayList<String> bool_options = variable_net.get(cpt_key);
            System.out.print("AMOUNT OF BOOL OPTIONS: " + bool_options.size());
            System.out.print("\n");
            int rows_in_eliminated = 1;
            for (int i = 0; i < bool_table.get(cpt_key).size();i++){
                String k = (String) bool_table.get(cpt_key).keySet().toArray()[i];
                if (!Objects.equals(k,h)){
                    rows_in_eliminated *= variable_net.get(k).size();
                }
            }
            System.out.print("ROWS IN ELIMINATED: " +  rows_in_eliminated);
            System.out.print("\n");


            for (int i = 0; i < cpt.size(); i+= (cpt.size()/rows_in_eliminated)){
                current_sum += Double.parseDouble(cpt.get(i));
                for (int j = i; j < cpt.size(); j++){
                    boolean should_add = true;
                    for (int l = 0; l < bool_table.get(cpt_key).size(); l++){
                        if (!Objects.equals(bool_table.get(cpt_key).get(bool_table.get(cpt_key).keySet().toArray()[l]).get(j), bool_table.get(cpt_key).get(bool_table.get(cpt_key).keySet().toArray()[l]).get(i))){
                            should_add = false;
                        }
                    }
                    if (should_add){
                        current_sum += Double.parseDouble(cpt.get(j));
                    }
                }
                eliminated_cpt.add(String.valueOf(current_sum));
                current_sum = 0;
            }


            System.out.print("ELIMINATED CPT: " + eliminated_cpt);
            System.out.print("\n");
            String eliminated_cpt_string = "";
            for (String s: eliminated_cpt){
                eliminated_cpt_string = eliminated_cpt_string.concat(s + " ");
            }
            eliminated_cpt_string = eliminated_cpt_string.substring(0,eliminated_cpt_string.length()-1);
            System.out.print("NET: " + net);
            System.out.print("\n");
            System.out.print("ELIMINATED CPT String: " + eliminated_cpt_string);
            System.out.print("\n");
            net.get(h).set(net.get(h).size()-1, eliminated_cpt_string);
            factors.put(h,eliminated_cpt);
            ArrayList<String> one_val_factors = new ArrayList<>();
            for (Map.Entry k: factors.entrySet()){
                if (factors.get(k.getKey()).size() == 1){
                    System.out.print("REMOVING FACTOR: " + factors.get(k.getKey()));
                    System.out.print("\n");
                    one_val_factors.add((String) k.getKey());
                }
            }
            System.out.print("ONE VAL FACTORS " + one_val_factors);
            System.out.print("\n");
            factors.keySet().removeAll(one_val_factors);
            net.keySet().removeAll(one_val_factors);
            hidden.removeAll(one_val_factors);
            System.out.print("NET AFTER: " + net);
            System.out.print("\n");
            System.out.print("FACTORS AFTER: " + factors);
            System.out.print("\n");
            hidden_done.add(h);
        }
        System.out.print("QUERY: " + query);
        System.out.print("\n");
        ArrayList<String> final_factor = new ArrayList<>(join(factors.get(query.split("=")[0]),eliminated_cpt,bool_table, query.split("=")[0], cpt_key));
        // normalize
        System.out.print("FINAL FACTOR: " + final_factor);
        System.out.print("\n");

        return 1;
    }

    public static ArrayList<String> join(ArrayList<String> cpt, ArrayList<String> cpt1, HashMap<String,HashMap<String,ArrayList<String>>> bool_table, String cpt_key, String cpt1_key){
        System.out.print("CPT: " + cpt);
        System.out.print("\n");
        System.out.print("CPT1: " + cpt1);
        System.out.print("\n");
        System.out.print("BOOL TABLE CPT: " + bool_table.get(cpt_key));
        System.out.print("\n");
        System.out.print("BOOL TABLE CPT1: " + bool_table.get(cpt1_key));
        System.out.print("\n");
        ArrayList<String> cpt_ret = new ArrayList<>(cpt1);

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
                    System.out.print("MULTIPLYING: " + cpt1.get(j) + " AND: " + cpt.get(i));
                    System.out.print("\n");
                    cpt_ret.set(j,String.valueOf(Double.parseDouble(cpt1.get(j)) * Double.parseDouble(cpt.get(i))));
                }
            }
        }
        return cpt_ret;
    }


    public static HashMap<String,HashMap<String,ArrayList<String>>> create_bool_table(HashMap<String,ArrayList<String>> variable_net,HashMap<String,ArrayList<String>> net,HashMap<String,String> evidence, ArrayList<String> hidden_done){
        HashMap<String,HashMap<String,ArrayList<String>>> bool_table = new HashMap<>();
        //System.out.print("VARIABLE NET: " + variable_net);
        //System.out.print("\n");
        //System.out.print("NET: " + net);
        //System.out.print("\n");
        for (Map.Entry var: net.entrySet()){
            HashMap<String,ArrayList<String>> var_bool_table = new HashMap<>();
            ArrayList<String> parents = new ArrayList<>(net.get(var.getKey()));
            ArrayList<String> values = new ArrayList<>(Arrays.asList(parents.remove(parents.size()-1).split(" ")));
            int table_size = values.size();
            for (int i = 0; i < parents.size(); i++){
                if (!evidence.containsKey(parents.get(i))){
                    ArrayList<String> parent_bool_values = new ArrayList<>();
                    double bool_group_size;
                    if (evidence.containsKey(var.getKey()) || hidden_done.contains(var.getKey())){
                        bool_group_size = Math.pow(2, parents.size()-i-1);
                    }
                    else{
                        bool_group_size = Math.pow(2, parents.size()-i);
                    }
                    //System.out.print("VARIABLE NET FOR PARENT: " + parents.get(i) + " " + variable_net.get(parents.get(i)));
                    //System.out.print("\n");
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
            }
            if (!evidence.containsKey(var.getKey()) && !hidden_done.contains(var.getKey())){
                ArrayList<String> var_bool_values = new ArrayList<>();
                ArrayList<String> bool_options = new ArrayList<>(variable_net.get(var.getKey()));
                String current_bool_value = bool_options.get(0);
                var_bool_values.add(current_bool_value);
                for (int j = 1; j < table_size; j++){
                    current_bool_value = bool_options.get(j%bool_options.size());
                    var_bool_values.add(current_bool_value);
                }
                var_bool_table.put((String) var.getKey(), var_bool_values);
            }
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


    public static HashMap<String,ArrayList<String>> copy(HashMap<String,ArrayList<String>> m){
        HashMap<String,ArrayList<String>> ret = new HashMap<>();

        for(Map.Entry<String,ArrayList<String>> var:m.entrySet()){
            ret.put(var.getKey(),new ArrayList<>(var.getValue()));
        }
        return ret;
    }
}
