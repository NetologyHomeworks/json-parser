import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";

        // CSV
        List<Employee> list = parseCSV(columnMapping, fileName);
        String json = listToJson(list);
        writeString(json, "data.json");

        // XML
        list = parseXML("data.xml");
        json = listToJson(list);
        writeString(json, "data2.json");

        // JSON
        json = readString("new_data.json");
        list = jsonToList(json);
        list.forEach(System.out::println);
    }

    private static List<Employee> parseCSV(String[] columns, String csvName) {
        try (CSVReader csvReader = new CSVReader(new FileReader(csvName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columns);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader).withMappingStrategy(strategy).build();
            return csv.parse();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static List<Employee> parseXML(String xmlFile) {
        List<Employee> result = new ArrayList<>();
        List<String> valueList = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            Document doc = factory.newDocumentBuilder().parse(new File(xmlFile));
            Node root = doc.getDocumentElement();
            NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (node.hasChildNodes()) {
                        NodeList nodeMap = node.getChildNodes();
                        Element element = (Element) nodeMap;

                        for (int j = 0; j < nodeMap.getLength(); j++) {
                            Node nodeTag = nodeMap.item(j);
                            if (nodeTag.getNodeType() == Node.ELEMENT_NODE) {
                                valueList.add(element.getElementsByTagName(nodeTag.getNodeName()).item(0).getTextContent());
                            }
                        }
                    }
                    result.add(new Employee(Long.parseLong(valueList.get(0)), valueList.get(1), valueList.get(2),
                            valueList.get(3), Integer.parseInt(valueList.get(4))));
                    valueList.clear();
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private static String listToJson(List<Employee> list) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(list, new TypeToken<List<Employee>>() {}.getType());
    }

    private static List<Employee> jsonToList(String jsonData) {
        List<Employee> result = new ArrayList<>();
        try {
            JSONArray jsonArray = (JSONArray) new JSONParser().parse(jsonData);
            Gson gson = new GsonBuilder().create();
            for (Object obj : jsonArray) {
                result.add(gson.fromJson(obj.toString(), Employee.class));
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private static String readString(String jsonName) {
        StringBuilder result = new StringBuilder();
        try (BufferedReader bufRead = new BufferedReader(new FileReader(jsonName))) {
            String str;
            while ((str = bufRead.readLine()) != null) {
                result.append(str).append("\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result.toString();
    }

    private static void writeString(String jsonData, String jsonName) {
        try (FileWriter file = new FileWriter(jsonName)) {
            file.write(jsonData);
            file.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
