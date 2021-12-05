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
import java.util.concurrent.Executor;

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
                            currencies.put(namedNodeMap.item(0).getNodeValue(), Float.parseFloat(namedNodeMap.item(1).getNodeValue()));
                        }
                    }
                }
            }
        }
        return currencies;
    }

    private static HttpServer httpServer = new HttpServer() {
        @Override
        public void bind(InetSocketAddress inetSocketAddress, int i) throws IOException {

        }

        @Override
        public void start() {

        }

        @Override
        public void setExecutor(Executor executor) {

        }

        @Override
        public Executor getExecutor() {
            return null;
        }

        @Override
        public void stop(int i) {

        }

        @Override
        public HttpContext createContext(String s, HttpHandler httpHandler) {
            return null;
        }

        @Override
        public HttpContext createContext(String s) {
            return null;
        }

        @Override
        public void removeContext(String s) throws IllegalArgumentException {

        }

        @Override
        public void removeContext(HttpContext httpContext) {

        }

        @Override
        public InetSocketAddress getAddress() {
            return null;
        }
    };

    public static void main(String[] args) throws IOException, JAXBException, SAXException, ParserConfigurationException, SQLException, ClassNotFoundException {
        URL url = new URL("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");

        int serverPort = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
        server.createContext("/data", (exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                String respText = "Hello!";
                exchange.sendResponseHeaders(200, respText.getBytes().length);
                OutputStream output = exchange.getResponseBody();
                output.write(respText.getBytes());
                output.flush();
            }
            exchange.close();
        }));
        server.setExecutor(null); // creates a default executor
        server.start();

        DbHandler dbHandler = new DbHandler();
        DbHandler.connect();

        Thread run = new Thread(() -> {
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
        run.start();
    }
}
