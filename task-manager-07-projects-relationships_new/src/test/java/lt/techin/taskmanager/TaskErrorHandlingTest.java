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
    void missingTaskAndMissingProjectReturn404WithConsistentErrorJson() throws Exception {
        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task with id 99 was not found."));

        mockMvc.perform(get("/api/projects/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Project with id 99 was not found."));
    }

    @Test
    void duplicateProjectNameReturns409() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Unique Name",
                                  "description": "First"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Unique Name",
                                  "description": "Second"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Project with name 'Unique Name' already exists."));
    }

    @Test
    void archivedProjectRejectsTaskCreationAndTaskMove() throws Exception {
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
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Project with id 2 is archived and cannot accept new tasks."));

        mockMvc.perform(post("/api/projects/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Movable task",
                                  "description": "Create first, move later",
                                  "dueDate": "2099-05-01"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Movable task",
                                  "description": "Try moving to archived project",
                                  "status": "TODO",
                                  "dueDate": "2099-05-01",
                                  "projectId": 2
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Project with id 2 is archived and cannot accept new tasks."));
    }

    @Test
    void repeatedDoneAndReverseTransitionStillReturn409() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Lifecycle Project",
                                  "description": "Lifecycle checks"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/projects/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Finish task",
                                  "description": "Mark it done once",
                                  "dueDate": "2099-05-01"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/tasks/1/status").param("value", "DONE"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/tasks/1/status").param("value", "DONE"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Task with id 1 is already done."));

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Illegal reverse",
                                  "description": "Try to go back",
                                  "status": "TODO",
                                  "dueDate": "2099-05-01",
                                  "projectId": 1
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Task with id 1 cannot move from DONE back to TODO."));
    }
}
