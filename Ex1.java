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
            ArrayList<String> h_factor = factors.get(h);
            for (Map.Entry var:net.entrySet()){
                if (net.get(var.getKey()).contains(h)){//h is a parent of var
                    ArrayList<String> var_factor = factors.get(var.getKey());
                }
            }
        }
        return 1;
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
