package io.github.geancgbr.orders.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.geancgbr.orders.TestcontainersConfiguration;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class OpenApiContractTest {

    private static final Path DOCUMENTED = Path.of("docs/api/openapi.json");
    private static final Path GENERATED = Path.of("target/openapi.json");

    private final JsonMapper json =
            JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();

    @Autowired
    private WebApplicationContext context;

    @Test
    void the_documented_contract_matches_the_published_one() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
        String body = mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode published = json.readTree(body);
        Files.createDirectories(GENERATED.getParent());
        Files.writeString(GENERATED, json.writeValueAsString(published) + System.lineSeparator());

        assertThat(DOCUMENTED)
                .as("contrato em %s; gere com make openapi", DOCUMENTED)
                .exists();
        assertThat(json.readTree(Files.readString(DOCUMENTED)))
                .as("o contrato em %s está desatualizado; rode make openapi", DOCUMENTED)
                .isEqualTo(published);
    }
}
