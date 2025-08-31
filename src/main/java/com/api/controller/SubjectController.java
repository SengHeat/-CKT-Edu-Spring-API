package com.api.controller;


import com.api.entity.Subject;
import com.api.service.SubjectService;
import java.net.http.HttpRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import java.util.List;

@RequestMapping("/api/subjects")
@RestController
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public ResponseEntity<List<Subject>> findAll() {
        List<Subject> subjects = subjectService.findAll();
        // Optional: debug
        if (!subjects.isEmpty()) {
            System.out.println(subjects.getFirst().getMeta());
        }
        return ResponseEntity.ok(subjects);
    }

    @PostMapping
    public ResponseEntity<?> connect() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }
        }, new java.security.SecureRandom());

        HttpClient client = HttpClient.newBuilder()
                .sslContext(sslContext)
                .build();


        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.drsb-express.test/api/reports/customer-lifetime-orders?filter[group]=customer_relation&filter[filter_order_by_year]=2024&filter[referral_id]=125"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + "237794|yatsviAB1P2ikFASXtpdHJSc0g8P6INGcDPEq3s773a3f2ce")
                .GET()
                .build();

        HttpResponse<String> response = client.send((java.net.http.HttpRequest) request, HttpResponse.BodyHandlers.ofString());
        return ResponseEntity.ok(response.body());
    }

}
