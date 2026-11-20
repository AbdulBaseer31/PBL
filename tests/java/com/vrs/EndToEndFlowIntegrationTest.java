package com.vrs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class EndToEndFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void resetDatabase() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM Bill");
            stmt.executeUpdate("DELETE FROM Reservation");
            stmt.executeUpdate("DELETE FROM Vehicle");
            stmt.executeUpdate("DELETE FROM Customer WHERE Role != 'ADMIN'");
        }
    }

    @Test
    void fullApplicationFlow() throws Exception {
        // 1. Register Customer 1
        String regJson1 = """
                {
                  "name": "Customer One",
                  "age": 25,
                  "address": "Hyderabad",
                  "contactNumber": "9876500001",
                  "password": "password123"
                }
                """;
        MvcResult regRes1 = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(regJson1))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode regNode1 = mapper.readTree(regRes1.getResponse().getContentAsString());
        String custId1 = regNode1.get("customerId").asText();

        // 2. Register Customer 2
        String regJson2 = """
                {
                  "name": "Customer Two",
                  "age": 28,
                  "address": "Bangalore",
                  "contactNumber": "9876500002",
                  "password": "password123"
                }
                """;
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(regJson2))
                .andExpect(status().isCreated());

        // 3. Login as Admin
        String adminLogin = """
                {
                  "contactNumber": "9999999999",
                  "password": "admin123"
                }
                """;
        MvcResult adminRes = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminLogin))
                .andExpect(status().isOk())
                .andReturn();
        String adminToken = mapper.readTree(adminRes.getResponse().getContentAsString()).get("token").asText();

        // 4. Admin adds a vehicle
        String vehicleJson = """
                {
                  "numberplate": "TS09E2E001",
                  "type": "SUV",
                  "brand": "Toyota",
                  "basePrice": 3000.00
                }
                """;
        mockMvc.perform(post("/vehicles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehicleJson))
                .andExpect(status().isCreated());

        // 5. Customer 1 logs in
        String cust1Login = """
                {
                  "contactNumber": "9876500001",
                  "password": "password123"
                }
                """;
        MvcResult cust1Res = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cust1Login))
                .andExpect(status().isOk())
                .andReturn();
        String cust1Token = mapper.readTree(cust1Res.getResponse().getContentAsString()).get("token").asText();

        // 6. Customer 1 searches vehicles
        mockMvc.perform(get("/vehicles?type=SUV")
                        .header("Authorization", "Bearer " + cust1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numberplate").value("TS09E2E001"));

        // 7. Customer 1 makes a reservation
        String resJson = """
                {
                  "numberplate": "TS09E2E001",
                  "reservDate": "2026-12-01",
                  "reservPeriod": 4
                }
                """;
        MvcResult resResult = mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + cust1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resJson))
                .andExpect(status().isCreated())
                .andReturn();
        String reservationId = mapper.readTree(resResult.getResponse().getContentAsString()).get("reservationId").asText();

        // 8. Customer 1 generates bill (3000 * 4 = 12000)
        String billJson = String.format("{\"reservationId\":\"%s\"}", reservationId);
        MvcResult billResult = mockMvc.perform(post("/bills")
                        .header("Authorization", "Bearer " + cust1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(billJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalCost").value(12000.00))
                .andReturn();
        String billId = mapper.readTree(billResult.getResponse().getContentAsString()).get("billId").asText();

        // 9. Customer 1 reads the bill
        mockMvc.perform(get("/bills/" + billId)
                        .header("Authorization", "Bearer " + cust1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.billId").value(billId));

        // 10. Customer 2 logs in and tries to access Customer 1's bill -> Returns 404
        String cust2Login = """
                {
                  "contactNumber": "9876500002",
                  "password": "password123"
                }
                """;
        MvcResult cust2Res = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cust2Login))
                .andExpect(status().isOk())
                .andReturn();
        String cust2Token = mapper.readTree(cust2Res.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(get("/bills/" + billId)
                        .header("Authorization", "Bearer " + cust2Token))
                .andExpect(status().isNotFound());
    }
}

