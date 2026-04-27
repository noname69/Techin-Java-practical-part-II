package lt.techin.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.JsonPathExpectationsHelper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerLoginAndDuplicateEmailUseExpectedStatusCodes() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Alice Student",
                                  "email": "alice@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Alice Student"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Alice Duplicate",
                                  "email": "alice@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isConflict());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "alice@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andReturn();

        assertFalse(readJson(loginResult, "$.accessToken").isBlank(), "Login should return a non-blank accessToken.");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "alice@example.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointsRequireTokenAndUseRoleAuthorization() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());

        String userToken = registerAndLogin("Bob User", "bob@example.com", "password123");

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Authenticated Project",
                                  "description": "Created with a USER token"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        String adminToken = login("admin@example.com", "admin123");
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("admin@example.com"));
    }

    private String registerAndLogin(String name, String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(name, email, password)))
                .andExpect(status().isCreated());
        return login(email, password);
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return readJson(result, "$.accessToken");
    }

    private String readJson(MvcResult result, String path) throws Exception {
        JsonPathExpectationsHelper helper = new JsonPathExpectationsHelper(path);
        return String.valueOf(helper.evaluateJsonPath(result.getResponse().getContentAsString()));
    }
}
