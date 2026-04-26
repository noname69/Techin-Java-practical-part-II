package lt.techin.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static lt.techin.taskmanager.rest.TestResponseAssertions.assertJsonInt;
import static lt.techin.taskmanager.rest.TestResponseAssertions.assertJsonText;
import static lt.techin.taskmanager.rest.TestResponseAssertions.expectStatus;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createListGetUpdateAndDeleteWorkForUsers() throws Exception {
        MvcResult createResult = expectStatus(mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Alice Teacher",
                                  "email": "alice@example.com"
                                }
                                """)),
                201,
                "POST /api/users should create a user and return 201 Created."
        );
        assertJsonInt(createResult, "/id", 1, "Created user response should include the generated id.");
        assertJsonText(createResult, "/name", "Alice Teacher", "Created user response should include the submitted name.");
        assertJsonText(createResult, "/email", "alice@example.com", "Created user response should include the submitted email.");

        MvcResult listResult = expectStatus(
                mockMvc.perform(get("/api/users")),
                200,
                "GET /api/users should return 200 OK."
        );
        assertJsonText(listResult, "/0/email", "alice@example.com", "GET /api/users should list the created user.");

        MvcResult getResult = expectStatus(
                mockMvc.perform(get("/api/users/1")),
                200,
                "GET /api/users/{id} should return 200 OK for an existing user."
        );
        assertJsonText(getResult, "/name", "Alice Teacher", "GET /api/users/{id} should return the stored user.");

        MvcResult updateResult = expectStatus(mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Alice Updated",
                                  "email": "alice.updated@example.com"
                                }
                                """)),
                200,
                "PUT /api/users/{id} should update the user and return 200 OK."
        );
        assertJsonText(updateResult, "/name", "Alice Updated", "Updated user response should show the new name.");
        assertJsonText(updateResult, "/email", "alice.updated@example.com", "Updated user response should show the new email.");

        expectStatus(mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Disposable User",
                                  "email": "disposable@example.com"
                                }
                                """)),
                201,
                "POST /api/users should allow creating another user for delete testing."
        );

        expectStatus(
                mockMvc.perform(delete("/api/users/2")),
                204,
                "DELETE /api/users/{id} should return 204 No Content for an existing user."
        );
    }
}
