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
                    System.out.print(lines[i]);
                    System.out.print("\n");
                    System.out.print(BayesBall(definitions,lines[i]));
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
        String finish = vars.split("\\-")[1];





        return BayesBallAlgo(net,evidence,start,finish, "all",null);
    }

    public static boolean BayesBallAlgo(HashMap<String,ArrayList<String>> net,ArrayList<String> evidence, String start, String finish, String could_go, String came_from){
        System.out.print("NEW CALL");
        System.out.print("\n");
        System.out.print("EVIDENCE: " + evidence);
        System.out.print("\n");
        System.out.print("START: " + start);
        System.out.print("\n");
        System.out.print("FINISH: " + finish);
        System.out.print("\n");
        System.out.print("COULD GO TO : " + could_go);
        System.out.print("\n");
        for(Map.Entry var: net.entrySet()){
            System.out.print("CURRENT KEY: " + var.getKey());
            System.out.print("\n");
            if (Objects.equals(start, finish)){
                System.out.print("FOUND THEY ARE NOT INDEPENDENT");
                return false;
            }
            System.out.print("ARRAY LIST OF CURRENT KEY : " + net.get(var.getKey()));
            System.out.print("\n");
            System.out.print("DOES THE CURRENT KEY CONTAIN THE START? : " + net.get(var.getKey()).contains(start));
            System.out.print("\n");
            if (net.get(var.getKey()).contains(start) && !evidence.contains(start) && !Objects.equals(could_go, "parent")){
                System.out.print("GOING TO CHILD");
                System.out.print("\n");
                if (evidence.contains((String) var.getKey())){
                    return BayesBallAlgo(net,evidence,(String) var.getKey(),finish,"parent",start);
                }
                else {
                    return BayesBallAlgo(net,evidence,(String) var.getKey(),finish,"child",start);
                }
            }
            if (!Objects.equals(could_go, "child")){
                for(int i = 0; i < net.get(start).size(); i++){
                    System.out.print("TRYING TO GO TO PARENT");
                    System.out.print("\n");
                    if (net.get(start).get(i).length() == 1 && !evidence.contains(net.get(start).get(i))){
                        System.out.print("GOING TO PARENT: " + net.get(start).get(i));
                        System.out.print("\n");
                        return BayesBallAlgo(net,evidence,net.get(start).get(i),finish,"all",start);
                    }
                }
            }

        }
        //return BayesBallAlgo(net,evidence,came_from,finish,"child",start);
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
