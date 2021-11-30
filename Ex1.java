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
                    BayesBall(definitions,lines[i]);
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
        String vars = splited_q[0];
        String start = vars.split("\\-")[0];
        String finish = vars.split("\\-")[1];




        return true;
    }

    public static boolean BayesBallAlgo(HashMap<String,ArrayList<String>> net,ArrayList<String> evidence, String start, String finish){
        for(Map.Entry cpt: net.entrySet()){
            if (cpt.getKey() == finish){
                return true;
            }
            if (net.get(cpt.getKey()).contains(start) && !evidence.contains(cpt.getKey())){
                BayesBallAlgo(net,evidence,(String) cpt.getKey(),finish);
            }

        }
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
