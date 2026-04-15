package lt.techin.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

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
class TaskApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsTasksForcesDoneFalseAndListsThem() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Write README",
                                  "description": "Finish the assignment brief"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Write README"))
                .andExpect(jsonPath("$.description").value("Finish the assignment brief"))
                .andExpect(jsonPath("$.done").value(false));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].done").value(false));
    }

    @Test
    void getsTasksByIdAndFiltersByDone() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Prepare slides",
                                  "description": "Demo the API"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Review code",
                                  "description": "Check status codes",
                                  "done": false
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Prepare slides"))
                .andExpect(jsonPath("$.done").value(false));

        mockMvc.perform(get("/api/tasks/search").param("done", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Prepare slides"));
    }

    @Test
    void updatesTasksWithPutAndPatch() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Initial task",
                                  "description": "Before update",
                                  "done": false
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated task",
                                  "description": "After update",
                                  "done": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated task"))
                .andExpect(jsonPath("$.description").value("After update"))
                .andExpect(jsonPath("$.done").value(true));

        mockMvc.perform(patch("/api/tasks/1/done").param("value", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.done").value(false))
                .andExpect(jsonPath("$.title").value("Updated task"));
    }

    @Test
    void deletesTasksAndReturnsNotFoundForUnknownIds() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Delete me",
                                  "description": "Temporary task"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/tasks/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Missing",
                                  "description": "Missing",
                                  "done": false
                                }
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/tasks/99/done").param("value", "true"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/tasks/99"))
                .andExpect(status().isNotFound());
    }
}
