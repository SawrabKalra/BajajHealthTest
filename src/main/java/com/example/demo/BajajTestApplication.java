package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class BajajTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(BajajTestApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) {
        return args -> {
            
            String name = "Sawrab Kalra";     
            String regNo = "22BCE2126";    
            String email = "sawrab.kalra@gmail.com"; 
            
            String solvedSqlQuery = "SELECT d.DEPARTMENT_NAME, AVG(TIMESTAMPDIFF(YEAR, e.DOB, CURDATE())) AS AVERAGE_AGE, SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) ORDER BY e.FIRST_NAME ASC SEPARATOR ', '), ', ', 10) AS EMPLOYEE_LIST FROM DEPARTMENT d JOIN EMPLOYEE e ON d.DEPARTMENT_ID = e.DEPARTMENT WHERE e.EMP_ID IN (SELECT DISTINCT p.EMP_ID FROM PAYMENTS p WHERE p.AMOUNT > 70000) GROUP BY d.DEPARTMENT_ID, d.DEPARTMENT_NAME ORDER BY d.DEPARTMENT_ID DESC"; 

            System.out.println("--- Starting Bajaj Finserv Health Assessment Task ---");

            // Send Initial POST Request
            String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("name", name);
            requestBody.put("regNo", regNo);
            requestBody.put("email", email);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            try {
                System.out.println("Sending request to: " + generateUrl);
                ResponseEntity<Map> response = restTemplate.postForEntity(generateUrl, entity, Map.class);
                
                Map<String, Object> responseBody = response.getBody();
                
                if (responseBody != null) {
                    String webhookUrl = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";
                    String accessToken = (String) responseBody.get("accessToken");

                    System.out.println("Using Hardcoded Webhook URL: " + webhookUrl);
                
                    
                    // We extract numbers from the regNo to find the last two digits.
                    String digitsOnly = regNo.replaceAll("\\D+", ""); // Remove non-digits
                    if (digitsOnly.length() >= 2) {
                        int lastTwoDigits = Integer.parseInt(digitsOnly.substring(digitsOnly.length() - 2));
                        boolean isEven = (lastTwoDigits % 2 == 0);
                        
                        System.out.println("RegNo Last Two Digits: " + lastTwoDigits);
                        System.out.println("Category: " + (isEven ? "EVEN -> Question 2" : "ODD -> Question 1"));
                        System.out.println("Please ensure 'solvedSqlQuery' variable contains the solution for the " + (isEven ? "Even" : "Odd") + " question.");
                    }

                    
                    // According to requirements: "Sends the solution... to the returned webhook URL"
                    
                    if (solvedSqlQuery == null || solvedSqlQuery.contains("PLACEHOLDER")) {
                         System.err.println("ERROR: Please paste your SQL solution in the 'solvedSqlQuery' variable!");
                         return;
                    }

                    Map<String, String> finalSubmissionMap = new HashMap<>();
                    finalSubmissionMap.put("finalQuery", solvedSqlQuery);

                    HttpHeaders authHeaders = new HttpHeaders();
                    authHeaders.setContentType(MediaType.APPLICATION_JSON);
                   
                    authHeaders.set("Authorization", accessToken); 

                    HttpEntity<Map<String, String>> submissionEntity = new HttpEntity<>(finalSubmissionMap, authHeaders);

                    System.out.println("Submitting solution to Webhook...");
                    ResponseEntity<String> submissionResponse = restTemplate.postForEntity(webhookUrl, submissionEntity, String.class);
                    
                    System.out.println("Submission Response: " + submissionResponse.getBody());
                    System.out.println("--- Task Completed ---");

                } else {
                    System.err.println("Failed to get a valid response from generateWebhook.");
                }

            } catch (Exception e) {
                System.err.println("Error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}