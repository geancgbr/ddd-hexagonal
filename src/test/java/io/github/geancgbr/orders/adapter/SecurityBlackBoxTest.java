package io.github.geancgbr.orders.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.geancgbr.orders.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SecurityBlackBoxTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private void registerProduct(String name) throws Exception {
        mvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"%s\", \"price\": 10.00}".formatted(name)))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest(name = "busca com \"{0}\" não injeta SQL")
    @ValueSource(strings = {"' OR '1'='1", "'; DROP TABLE products; --", "%' UNION SELECT id, name FROM orders --"})
    void sql_injection_in_the_search_is_treated_as_plain_text(String payload) throws Exception {
        registerProduct("Monitor seguro");

        mvc.perform(get("/products").param("name", payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mvc.perform(get("/products").param("name", "monitor seguro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Monitor seguro"));
    }

    @ParameterizedTest(name = "nome \"{0}\" é recusado")
    @ValueSource(strings = {"<script>alert(1)</script>", "<img src=x onerror=alert(1)>", "{{7*7}}"})
    void html_and_script_in_the_product_name_are_rejected_without_echoing_the_input(String payload) throws Exception {
        String body = mvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"%s\", \"price\": 10.00}".formatted(payload.replace("\"", "\\\""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("name"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).doesNotContain("<", "{{");
    }

    @Test
    void malformed_json_does_not_leak_internal_details() throws Exception {
        String body = mvc.perform(
                        post("/orders").contentType(MediaType.APPLICATION_JSON).content("{\"customerId\":"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).doesNotContain("Exception", "jackson", "at io.", "java.");
    }

    @Test
    void customer_id_only_accepts_safe_characters() throws Exception {
        mvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\": \"cliente'; DELETE FROM orders; --\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void responses_carry_security_headers() throws Exception {
        mvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().exists("Cache-Control"));
    }

    @Test
    void actuator_exposes_only_health_and_info() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mvc.perform(get("/actuator/env")).andExpect(status().isNotFound());
        mvc.perform(get("/actuator/beans")).andExpect(status().isNotFound());
    }
}
