package test.sample;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TestGraphGL {

//	public static void main(String[] args) throws IOException, InterruptedException {
//		execute();
//	}
	
	public static void execute() throws IOException, InterruptedException, JSONException {
		// utc to epoch
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate date = LocalDate.parse("2023-01-01", formatter);
		LocalDate tmr = date.plusDays(1);
		long s_epoch = date.toEpochDay() * 86400L;
		long e_epoch = tmr.toEpochDay() * 86400L;
		
		String query = String.format("query {reserveParamsHistoryItems(first:1000 orderBy: timestamp where: {timestamp_gt:%d, timestamp_lt:%d}){id, timestamp}}", s_epoch, e_epoch);

        // Define the GraphQL endpoint URL
        String url = "https://api.thegraph.com/subgraphs/name/aave/protocol-v2";

        // Create an HTTP client
        HttpClient httpClient = HttpClient.newHttpClient();

        // Create an HTTP request with POST method and JSON payload
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"query\":\"" + query + "\"}"))
                .build();

        // Send the HTTP request and get the response
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Check if the request was successful (status code 200)
        if (response.statusCode() == 200) {
            // Extract the response body as JSON
            String responseBody = response.body();
            // Process the response body as needed
            JSONObject obj = new JSONObject(responseBody);
            JSONObject data = obj.getJSONObject("data");
            JSONArray reserve = data.getJSONArray("reserveParamsHistoryItems");
            System.out.println(reserve.get(1).toString());
        } else {
            // Print an error message if the request failed
            System.out.println("GraphQL request failed with status code: " + response.statusCode());
        }
	}
}