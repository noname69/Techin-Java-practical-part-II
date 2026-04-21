package lt.techin.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void projectScopedTaskEndpointsAndTaskByIdWork() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Alpha Project",
                                  "description": "First project"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Beta Project",
                                  "description": "Second project"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/projects/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Write docs",
                                  "description": "Describe the relationship task",
                                  "dueDate": "2099-05-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.project.id").value(1))
                .andExpect(jsonPath("$.project.name").value("Alpha Project"));

        mockMvc.perform(get("/api/projects/1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].project.id").value(1));

        mockMvc.perform(get("/api/projects/1/tasks").param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("TODO"));

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project.name").value("Alpha Project"));

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Write better docs",
                                  "description": "Move it to another project",
                                  "status": "DONE",
                                  "dueDate": "2099-06-01",
                                  "projectId": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.project.id").value(2))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void patchStatusStillWorksWithRelationshipModel() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Lifecycle Project",
                                  "description": "Test status updates"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/projects/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Finish assignment",
                                  "description": "Patch status later",
                                  "dueDate": "2099-07-01"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/tasks/1/status").param("value", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.completedAt").isNotEmpty())
                .andExpect(jsonPath("$.project.id").value(1));
    }
}
