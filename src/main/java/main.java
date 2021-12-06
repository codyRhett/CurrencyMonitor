import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.*;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class main {
    private static volatile Map<String, Float> currencies = new HashMap<>();

    private static Map<String, Float> getCurrenciesFromXml(URL url) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(url.toString());
        Node root = document.getDocumentElement();
        NodeList gesmesList = root.getChildNodes();
        Map<String, Float> currencies = new HashMap<>();

        for (int i = 0; i < gesmesList.getLength(); i++) {
            NodeList cube = gesmesList.item(i).getChildNodes();
            if (cube.getLength() != 0) {
                for(int j = 0; j < cube.getLength(); j++) {
                    NodeList cubeChild = cube.item(j).getChildNodes();
                    for(int k = 0; k < cubeChild.getLength(); k++) {
                        Node cubeNode = cubeChild.item(k);
                        if (cubeNode.getNodeType() != Node.TEXT_NODE) {
                            NamedNodeMap namedNodeMap = cubeNode.getAttributes();
                            String nameNode = namedNodeMap.item(0).getNodeValue();
                            if (nameNode.equals("RUB") || nameNode.equals("JPY") || nameNode.equals("USD")) {
                                currencies.put(namedNodeMap.item(0).getNodeValue(), Float.parseFloat(namedNodeMap.item(1).getNodeValue()));
                            }
                        }
                    }
                }
            }
        }
        return currencies;
    }

    private static void httpServerInit(int port) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/data", (exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                // ??
            }
            exchange.close();
        }));
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        URL url = new URL("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");

        httpServerInit(8000);

        DbHandler dbHandler = new DbHandler();
        DbHandler.connect();

        Thread runRead = new Thread(() -> {
            while(true){
                try {
                    dbHandler.clearTable();
                    currencies.clear();
                    currencies = getCurrenciesFromXml(url);
                    currencies.forEach((key, value) -> dbHandler.addCurrency(key, value));
                    Thread.sleep(3000);
                } catch (SAXException | ParserConfigurationException | IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        runRead.start();
    }
}
