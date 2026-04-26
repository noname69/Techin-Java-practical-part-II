package lt.techin.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void missingTaskProjectAndUserReturn404WithConsistentErrorJson() throws Exception {
        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task with id 99 was not found."));

        mockMvc.perform(get("/api/projects/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Project with id 99 was not found."));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with id 99 was not found."));
    }

    @Test
    void duplicateUserEmailReturns409() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Alice",
                                  "email": "alice@example.com"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Another Alice",
                                  "email": "alice@example.com"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("User with email 'alice@example.com' already exists."));
    }

    @Test
    void missingAssigneeReturns404AndCompletedTaskCannotBeReassigned() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Assignment Project",
                                  "description": "Task assignment checks"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Alice User",
                                  "email": "alice@example.com"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Bob User",
                                  "email": "bob@example.com"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/projects/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Missing assignee task",
                                  "description": "Should fail",
                                  "dueDate": "2099-05-01",
                                  "assigneeId": 99
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with id 99 was not found."));

        mockMvc.perform(post("/api/projects/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Assigned task",
                                  "description": "Will be completed",
                                  "dueDate": "2099-05-01",
                                  "assigneeId": 1
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/tasks/1/status").param("value", "DONE"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Assigned task",
                                  "description": "Try reassigning",
                                  "status": "DONE",
                                  "dueDate": "2099-05-01",
                                  "projectId": 1,
                                  "assigneeId": 2
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Task with id 1 is completed and cannot be reassigned."));
    }

    @Test
    void archivedProjectAndLifecycleRulesStillWork() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Open Project",
                                  "description": "Accepts tasks"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Archived Target",
                                  "description": "Will be archived"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/projects/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "archived": true
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/projects/2/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Blocked task",
                                  "description": "Should fail",
                                  "dueDate": "2099-05-01"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Project with id 2 is archived and cannot accept new tasks."));
    }
}
