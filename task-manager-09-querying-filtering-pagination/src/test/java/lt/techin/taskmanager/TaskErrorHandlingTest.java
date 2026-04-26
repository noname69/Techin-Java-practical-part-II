package lt.techin.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static lt.techin.taskmanager.rest.TestResponseAssertions.assertJsonArrayEmpty;
import static lt.techin.taskmanager.rest.TestResponseAssertions.assertJsonInt;
import static lt.techin.taskmanager.rest.TestResponseAssertions.assertJsonText;
import static lt.techin.taskmanager.rest.TestResponseAssertions.expectStatus;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void missingProjectOnProjectScopedQueryStillReturns404() throws Exception {
        MvcResult result = expectStatus(mockMvc.perform(get("/api/projects/99/tasks")
                        .param("page", "0")
                        .param("size", "5")),
                404,
                "GET /api/projects/{id}/tasks should return 404 Not Found when the project does not exist."
        );
        assertJsonInt(result, "/status", 404, "Error JSON for missing project should include status 404.");
        assertJsonText(result, "/error", "Not Found", "Error JSON for missing project should include error='Not Found'.");
        assertJsonText(result, "/message", "Project with id 99 was not found.", "Error JSON for missing project should explain which project id was not found.");
    }

    @Test
    void emptyGlobalQueryResultReturns200WithEmptyContent() throws Exception {
        MvcResult result = expectStatus(mockMvc.perform(get("/api/tasks")
                        .param("status", "DONE")
                        .param("page", "0")
                        .param("size", "10")),
                200,
                "GET /api/tasks with valid filters should still return 200 OK even when nothing matches."
        );
        assertJsonArrayEmpty(result, "/content", "Empty global task queries should return an empty content array.");
        assertJsonInt(result, "/totalElements", 0, "Empty global task queries should report totalElements=0.");
    }

    @Test
    void duplicateUserEmailRuleStillWorks() throws Exception {
        expectStatus(mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Alice",
                                  "email": "alice@example.com"
                                }
                                """)),
                201,
                "POST /api/users should create the first user before duplicate-email validation is checked."
        );

        MvcResult result = expectStatus(mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Another Alice",
                                  "email": "alice@example.com"
                                }
                                """)),
                409,
                "POST /api/users should return 409 Conflict when email is already used by another user."
        );
        assertJsonInt(result, "/status", 409, "Duplicate user email error JSON should include status 409.");
        assertJsonText(result, "/message", "User with email 'alice@example.com' already exists.", "Duplicate user email error JSON should explain that the email must be unique.");
    }
}
