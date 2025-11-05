package com.catchy;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.catchy.model.User;
import com.catchy.repository.UserRepository;
import com.catchy.repository.VerificationTokenRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class IntegrationSmokeTests {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Test
    void smokeTestIndexAndProductsAndSignup() throws Exception {
        String base = "http://localhost:" + port;

        // index
        ResponseEntity<String> index = restTemplate.getForEntity(base + "/", String.class);
        assertThat(index.getStatusCode().is2xxSuccessful()).isTrue();

        // index page is reachable
        assertThat(index.getStatusCode().is2xxSuccessful()).isTrue();

        // signup (unique email)
        String email = "smoke+" + System.currentTimeMillis() + "@example.com";
        Map<String, Object> signup = Map.of("name", "Smoke Tester", "email", email, "password", "password123");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(signup, headers);

        ResponseEntity<String> signupResp = restTemplate.postForEntity(base + "/api/auth/signup", req, String.class);
        assertThat(signupResp.getStatusCode().is2xxSuccessful()).isTrue();

        // user persisted
        User user = userRepository.findByEmail(email).orElse(null);
        assertThat(user).isNotNull();

        // verification token created
        boolean tokenExists = verificationTokenRepository.findAll().stream().anyMatch(t -> t.getUser().getId().equals(user.getId()));
        assertThat(tokenExists).isTrue();
    }
}
