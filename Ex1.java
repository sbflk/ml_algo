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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

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
            ArrayList<HashMap<String,String[]>> variables = new ArrayList<>();
            NodeList vars = doc.getElementsByTagName("VARIABLE");
            NodeList defs = doc.getElementsByTagName("DEFINITION");
            for(int i = 0; i < vars.getLength(); i++){
                Node v = vars.item(i);

                if(v.getNodeType() == Node.ELEMENT_NODE){
                    NodeList v_details = v.getChildNodes();
                    for (int j = 0; j < v_details.getLength(); j++){
                        Node d = v_details.item(j);
                        if (d.getNodeType() == Node.ELEMENT_NODE){
                            Element d_element = (Element)d;
                            //System.out.print(d_element.getTagName() + ": " + d_element.getTextContent());
                            variables[]

                        }
                    }
                }
            }
            for(int i = 0; i < defs.getLength(); i++){
                Node v = defs.item(i);
                //System.out.print(v.getTextContent());
                //System.out.print("\n");
            }
            ArrayList<HashMap<String,String[]>> variables = new ArrayList<HashMap<String,String[]>>();

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }


    }
}
