package io.github.geancgbr.orders.adapter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import io.github.geancgbr.orders.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class OrderApiBlackBoxTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private String postJson(String url, String body, int expectedStatus) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(expectedStatus))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    void creates_fills_and_places_an_order() throws Exception {
        String product = postJson("/products", """
                {"name": "Monitor", "price": 899.90}""", 201);
        String productId = JsonPath.read(product, "$.id");

        String order = postJson("/orders", """
                {"customerId": "cliente-42"}""", 201);
        String orderId = JsonPath.read(order, "$.id");

        postJson("/orders/" + orderId + "/items", """
                {"productId": "%s", "quantity": 2}""".formatted(productId), 200);
        postJson("/orders/" + orderId + "/coupon", """
                {"code": "bemvindo10"}""", 200);
        postJson("/orders/" + orderId + "/place", "", 200);

        mvc.perform(get("/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.couponCode").value("BEMVINDO10"))
                .andExpect(jsonPath("$.subtotal").value(1799.80))
                .andExpect(jsonPath("$.discount").value(179.98))
                .andExpect(jsonPath("$.total").value(1619.82))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void business_rule_violations_become_422_with_the_domain_message() throws Exception {
        String order = postJson("/orders", """
                {"customerId": "cliente-42"}""", 201);
        String orderId = JsonPath.read(order, "$.id");

        mvc.perform(post("/orders/" + orderId + "/place"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.detail").value("Pedido sem itens não pode ser fechado"));
    }

    @Test
    void unknown_order_is_404() throws Exception {
        mvc.perform(get("/orders/00000000-0000-0000-0000-000000000000")).andExpect(status().isNotFound());
    }

    @ParameterizedTest(name = "quantidade {0} → HTTP {1}")
    @CsvSource({"0, 422", "1, 200", "99, 200", "100, 422"})
    void quantity_boundary_values(int quantity, int expectedStatus) throws Exception {
        String product = postJson("/products", """
                {"name": "Cabo USB", "price": 19.90}""", 201);
        String productId = JsonPath.read(product, "$.id");
        String order = postJson("/orders", """
                {"customerId": "cliente-7"}""", 201);
        String orderId = JsonPath.read(order, "$.id");

        postJson("/orders/" + orderId + "/items", """
                {"productId": "%s", "quantity": %d}""".formatted(productId, quantity), expectedStatus);
    }
}
