package com.catchy.payment;

import com.catchy.payment.dto.PaymentRequest;
import com.catchy.payment.dto.PaymentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void initiateAndGetPayment() throws Exception {
        PaymentRequest req = new PaymentRequest();
        req.setOrderId(100L);
        req.setUserId(1L);
        req.setAmount(new BigDecimal("99.00"));
        req.setCurrency("INR");
        req.setMethod(null);

        String json = mapper.writeValueAsString(req);
        var mvcRes = mockMvc.perform(post("/api/internal-payments/initiate").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn();

        String body = mvcRes.getResponse().getContentAsString();
        PaymentResponse resp = mapper.readValue(body, PaymentResponse.class);
        assertThat(resp.getPaymentId()).isNotNull();

        // fetch as admin
        mockMvc.perform(get("/api/admin/payments").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void adminManualSettle() throws Exception {
        // create payment
        PaymentRequest req = new PaymentRequest();
        req.setOrderId(200L);
        req.setUserId(2L);
        req.setAmount(new BigDecimal("199.00"));
        req.setCurrency("INR");
        req.setMethod(null);
        String json = mapper.writeValueAsString(req);
        var mvcRes = mockMvc.perform(post("/api/internal-payments/initiate").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn();
        PaymentResponse resp = mapper.readValue(mvcRes.getResponse().getContentAsString(), PaymentResponse.class);

        // manual settle
        mockMvc.perform(post("/api/admin/payments/manual-settle/"+resp.getPaymentId()).param("txn","MANUAL-TEST")).andExpect(status().isOk());
    }
}
