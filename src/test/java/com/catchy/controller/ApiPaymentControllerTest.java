package com.catchy.controller;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.catchy.dto.PaymentIntentRequest;
import com.catchy.dto.PaymentResponse;
import com.catchy.model.Order;
import com.catchy.service.OrderService;
import com.catchy.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class ApiPaymentControllerTest {

    private MockMvc mvc;

    @org.mockito.Mock
    private PaymentService paymentService;

    @org.mockito.Mock
    private OrderService orderService;

    private com.catchy.config.PaymentConfig paymentConfig = new com.catchy.config.PaymentConfig();

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        ApiPaymentController controller = new ApiPaymentController();
        ReflectionTestUtils.setField(controller, "paymentService", paymentService);
        ReflectionTestUtils.setField(controller, "orderService", orderService);
        this.mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createIntent_missingOrderId_returnsBadRequest() throws Exception {
        mvc.perform(post("/api/payments/create-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createIntent_whenPaymentsDisabled_returnsJsonError() throws Exception {
        PaymentIntentRequest req = new PaymentIntentRequest(1L, new BigDecimal("100.00"));
        Order order = new Order();
        order.setId(1L);
        order.setTotalPrice(new BigDecimal("100.00"));

        when(orderService.getOrderById(1L)).thenReturn(order);
        when(paymentService.createPaymentIntentForOrder(any(), any())).thenReturn(new PaymentResponse(false, "Payments are disabled on this build"));

        mvc.perform(post("/api/payments/create-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Payments are disabled on this build"));
    }

    @Test
    void createIntent_stubbedSuccess_returnsClientSecret() throws Exception {
        PaymentIntentRequest req = new PaymentIntentRequest(2L, new BigDecimal("50.00"));
        Order order = new Order();
        order.setId(2L);
        order.setTotalPrice(new BigDecimal("50.00"));

        PaymentResponse stubResp = new PaymentResponse(true, "Payment intent created (stub)");
        stubResp.setClientSecret("pi_stub_client_secret");
        stubResp.setPaymentId(-1L);
        stubResp.setOrderId(2L);

        when(orderService.getOrderById(2L)).thenReturn(order);
        when(paymentService.createPaymentIntentForOrder(any(), any())).thenReturn(stubResp);

        mvc.perform(post("/api/payments/create-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.clientSecret").value("pi_stub_client_secret"));
    }

    @Test
    void confirm_forwardedToService_returnsJson() throws Exception {
        when(paymentService.confirmPaymentIntent("pi_123")).thenReturn(new PaymentResponse(false, "Payments are disabled on this build"));

        mvc.perform(post("/api/payments/confirm/pi_123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Payments are disabled on this build"));
    }
}
