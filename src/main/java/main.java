import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.*;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class main {
    private static volatile Map<String, Float> currencies = new HashMap<>();
    static DbHandler dbHandler = new DbHandler();
    private static Map<String, Float> getCurrenciesFromXml(URL url) throws ParserConfigurationException, IOException, SAXException {
        Map<String, Float> currencies = new HashMap<>();
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(url.toString());
        Node root = document.getDocumentElement();
        NodeList gesmesList = root.getChildNodes();

        for (int i = 0; i < gesmesList.getLength(); i++) {
            NodeList cubeList = gesmesList.item(i).getChildNodes();
            if (cubeList.getLength() != 0) {
                for(int j = 0; j < cubeList.getLength(); j++) {
                    NodeList cubeChild = cubeList.item(j).getChildNodes();
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
        AtomicReference<Float> curr = new AtomicReference<>((float) 0);
        // Создаем сервер, куда будут приходить сообщения от клиента
        server.createContext("/api/currency", (exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                String str = exchange.getRequestURI().getQuery();
                try {
                    curr.set(dbHandler.getCurrency(str));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                exchange.sendResponseHeaders(200, curr.toString().getBytes().length);
                OutputStream output = exchange.getResponseBody();
                output.write(curr.toString().getBytes());
                output.flush();
            }
            exchange.close();
        }));
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        URL url = new URL("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");
        DbHandler.connect();
        dbHandler.createTable();
        httpServerInit(8001);
        Thread runRead = new Thread(() -> {
            while(true){
                try {
                    dbHandler.clearTable();
                    currencies.clear();
                    currencies = getCurrenciesFromXml(url);
                    currencies.forEach(dbHandler::addCurrency);
                    Thread.sleep(1800000);
                } catch (SAXException | ParserConfigurationException | IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        runRead.start();
    }
}
