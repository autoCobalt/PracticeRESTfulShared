package org.example;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception{

        Transcript transcript = new Transcript();

        /* Alter the url to any downloadable mp3 or m4a with voice audio. It must be accessible without
            any additional login requirements. example audio file urls are in the Constants class.
            File used:
                https://github.com/autoCobalt/cautionary/blob/main/sneak.m4a?raw=true

            Should return:
                "You can choose to stop receiving quote, pre screened, end quote offers of credit from this
                    and other companies by calling toll free 1888-567-8688."
         */

        Scanner scanner = new Scanner(System.in);
        String api_key = getApiKey(scanner);
        String url_to_translate = getAudioUrl(scanner);

        transcript.setAudio_url(url_to_translate);
        Gson gson = new Gson();
        String jsonRequest = gson.toJson(transcript);
        System.out.println("JSON request being used:\n" + jsonRequest);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.assemblyai.com/v2/transcript"))
                .header("Authorization", api_key)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> postResponse =  httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

        transcript = gson.fromJson(postResponse.body(),Transcript.class);

        System.out.println("response ID: " + transcript.getId() + "\n");


        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.assemblyai.com/v2/transcript/" + transcript.getId()))
                .header("Authorization", api_key)
                .build();

        while (true) {

            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
            transcript = gson.fromJson(getResponse.body(), Transcript.class);

            System.out.println(transcript.getStatus());

            if ("completed".equals(transcript.getStatus()) || "error".equals(transcript.getStatus()))
                break;

            Thread.sleep(1000);
        }

        System.out.println("\nResponse text received:\n" + transcript.getText());

    }

    private static String getAudioUrl(Scanner scanner) {
        System.out.print("""
                Please provide a url for the audio to translate to text. Default options:
                1. Grapejuice audio
                2. Prescreened offers

                url:\s""");
        String response = scanner.nextLine();

        if("1".equals(response))
            return Constants.grape_juice;
        else if ("2".equals(response))
            return Constants.prescreened_offers;

        return response;
    }

    private static String getApiKey(Scanner scanner) {
        System.out.print("""
                Please provide your Assemblyai.com api key. If you do not
                 have a key, please sign up for a free key at Assemblyai.com.
                key:\s""");
        return scanner.nextLine();
    }
}