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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthenticatedTaskAssignmentApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void authenticatedTaskAssignmentFlowStillWorks() throws Exception {
        MvcResult aliceRegister = register("Alice User", "alice@example.com", "password123");
        long aliceId = readLong(aliceRegister, "$.id");
        MvcResult bobRegister = register("Bob User", "bob@example.com", "password123");
        long bobId = readLong(bobRegister, "$.id");
        String token = login("alice@example.com", "password123");

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Alpha Project",
                                  "description": "First project"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Beta Project",
                                  "description": "Second project"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/projects/1/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Write docs",
                                  "description": "Assigned task",
                                  "dueDate": "2099-05-01",
                                  "assigneeId": %d
                                }
                                """.formatted(aliceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.project.id").value(1))
                .andExpect(jsonPath("$.assignee.id").value(aliceId))
                .andExpect(jsonPath("$.assignee.email").value("alice@example.com"));

        mockMvc.perform(get("/api/tasks/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project.name").value("Alpha Project"))
                .andExpect(jsonPath("$.assignee.name").value("Alice User"));

        mockMvc.perform(put("/api/tasks/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Write better docs",
                                  "description": "Move it and reassign it",
                                  "status": "TODO",
                                  "dueDate": "2099-06-01",
                                  "projectId": 2,
                                  "assigneeId": %d
                                }
                                """.formatted(bobId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project.id").value(2))
                .andExpect(jsonPath("$.assignee.id").value(bobId))
                .andExpect(jsonPath("$.assignee.email").value("bob@example.com"));

        mockMvc.perform(patch("/api/tasks/1/status")
                        .header("Authorization", "Bearer " + token)
                        .param("value", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());

        mockMvc.perform(delete("/api/tasks/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    private MvcResult register(String name, String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(name, email, password)))
                .andExpect(status().isCreated())
                .andReturn();
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
        return readString(result, "$.accessToken");
    }

    private long readLong(MvcResult result, String path) throws Exception {
        JsonPathExpectationsHelper helper = new JsonPathExpectationsHelper(path);
        Object value = helper.evaluateJsonPath(result.getResponse().getContentAsString());
        return ((Number) value).longValue();
    }

    private String readString(MvcResult result, String path) throws Exception {
        JsonPathExpectationsHelper helper = new JsonPathExpectationsHelper(path);
        return String.valueOf(helper.evaluateJsonPath(result.getResponse().getContentAsString()));
    }
}
