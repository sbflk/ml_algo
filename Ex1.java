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
        System.out.println(qs);
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

            System.out.print(variables);
            System.out.print("\n");
            System.out.print(definitions);

            for(int i = 1; i < lines.length; i++){
                System.out.print("\n");
                if (lines[i].charAt(1) == 'P'){
                    continue;
                }
                else{
                    boolean ans = BayesBall(definitions,lines[i]);
                    if (ans){System.out.print("yes");}
                    else{System.out.print("no");}
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
           //System.out.print("CURRENT V: " + v);
           //System.out.print("\n");
           //System.out.print("CURRENT STATE: " + state);
           //System.out.print("\n");
           if (Objects.equals(v, finish)){
               return false;
           }
           for (Map.Entry var: net.entrySet()){
               //System.out.print("CURRENT KEY: " + var.getKey());
               //System.out.print("\n");
               if (net.get(var.getKey()).contains(v) && !evidence.contains(v) && !Objects.equals(state, "parent")){
                   if (!q.contains(var.getKey()) && !explored.contains(var.getKey())){
                       //System.out.print("CURRENT KEY: " + var.getKey() + " IS ADDED TO Q");
                       //System.out.print("\n");
                       q.add((String) var.getKey());
                       if (evidence.contains((String) var.getKey())){
                           states.add("parent");
                           //System.out.print("PARENT STATE");
                           //System.out.print("\n");
                       }
                       else {
                           states.add("child");
                           //System.out.print("CHILD STATE");
                           //System.out.print("\n");
                       }
                   }
               }

           }
            if (!Objects.equals(state, "child")){
                //System.out.print("LOOKING FOR PARENTS");
                //System.out.print("\n");
                for(int i = 0; i < net.get(v).size(); i++){
                    if (net.containsKey(net.get(v).get(i)) && !evidence.contains(net.get(v).get(i)) && Collections.frequency(explored,net.get(v).get(i)) != 2){
                        //System.out.print("PARENT ADDED TO THE Q: " + net.get(v).get(i));
                        //System.out.print("\n");
                        explored.add(net.get(v).get(i));
                        q.add(net.get(v).get(i));
                        states.add("all");
                    }
                }
            }
        }
        return true;
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
