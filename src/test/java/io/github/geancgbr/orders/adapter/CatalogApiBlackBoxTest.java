package io.github.geancgbr.orders.adapter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import io.github.geancgbr.orders.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CatalogApiBlackBoxTest {

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
    void registers_searches_and_deactivates_a_product() throws Exception {
        String keyboard = postJson("/products", """
                {"name": "Teclado do catálogo", "price": 249.90}""", 201);
        String keyboardId = JsonPath.read(keyboard, "$.id");
        postJson("/products", """
                {"name": "Mouse do catálogo", "price": 89.90}""", 201);

        mvc.perform(get("/products").param("name", "TECLADO DO CAT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(keyboardId))
                .andExpect(jsonPath("$[0].price").value(249.90))
                .andExpect(jsonPath("$[0].active").value(true));

        mvc.perform(post("/products/" + keyboardId + "/deactivate")).andExpect(status().isNoContent());

        mvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')].active".formatted(keyboardId))
                        .value(false));

        String order = postJson("/orders", """
                {"customerId": "cliente-catalogo"}""", 201);
        String orderId = JsonPath.read(order, "$.id");

        mvc.perform(post("/orders/" + orderId + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId": "%s", "quantity": 1}""".formatted(keyboardId)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.detail").value("Produto indisponível: Teclado do catálogo"));
    }
}
