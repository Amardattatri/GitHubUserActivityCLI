package org.example;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GitHubUserActivityCLI {

    private static final String GITHUB_URL_API = "https://api.github.com/users/";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("USAGE: java GitHubUserActivityCLI <username> ");
            return;
        }

        String username = args[0];
        String eventApiUrl = GITHUB_URL_API + username + "/events";
        try {
            //fetching data from external git api
            String jsonResponse = fetchGitHubUserActivity(eventApiUrl);
            if (jsonResponse == null || jsonResponse.trim().equals("")) {
                System.out.println("No Activity found for user: " + username);
            }
            displayActivity(jsonResponse);

        } catch (Exception e) {
            System.out.println("Unexpected error : " + e.getMessage());
        }

    }

    private static void displayActivity(String jsonResponse) {
        try {
            JSONParser jsonParser = new JSONParser();
            JSONArray events = (JSONArray) jsonParser.parse(jsonResponse);
            System.out.println("Recent User Activity : ");
            // only 5 recent activity
            for (int i = 0; i < Math.min(events.size(), 5); i++) {
                JSONObject event = (JSONObject) events.get(i);
                JSONObject repo = (JSONObject) event.get("repo");
                String eventType = (String) event.get("type");
                String repoName = (String) repo.get("name");

                switch (eventType) {
                    case "PushEvent" :
                        System.out.println("- Pushed to " + repoName);
                        break;
                    case "WatchEvent" :
                        System.out.println("- Starred repository : " + repoName);
                        break;
                    case "IssueEvent" :
                        System.out.println("- Opened a new issue in " + repoName);
                    default:
                        System.out.println("- ... ");
                        break;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static String fetchGitHubUserActivity(String eventApiUrl) {
        try {
            URL url = new URL(eventApiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            } else if (responseCode == 404) {
                throw new IllegalArgumentException("User Not Found.");
            } else {
                throw new Exception("Failed to fetch user activity info: HTTP" + responseCode);
            }
        } catch (Exception e) {
            System.out.println("Connect Failed to git api : " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

}
