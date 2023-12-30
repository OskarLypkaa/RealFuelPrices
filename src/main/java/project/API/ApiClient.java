package project.API;

import project.exceptions.APIStatusException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;



public abstract class ApiClient {

    protected static final Logger logger = Logger.getLogger(ApiClient.class.getName());

    static {
        try {
            File logsFolder = new File("Logs");
            if (!logsFolder.exists()) {
                logsFolder.mkdirs();
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String logFileName = String.format("Logs/%s_%s.log", ApiClient.class.getSimpleName(), dateFormat.format(new Date()));
            FileHandler fileHandler = new FileHandler(logFileName);

            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected Logger getLogger() {
        return logger;
    }


    public abstract Map<String, List<String>> fetchData() throws APIStatusException;

    protected HttpResponse<String> sendHttpRequest(String apiUrl, String apiToken, String apiParameters)
            throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + apiToken + apiParameters))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
    
