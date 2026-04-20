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
class TaskErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void missingTaskReturns404WithConsistentErrorJson() throws Exception {
        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task with id 99 was not found."))
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }

    @Test
    void repeatedDoneTruePatchReturns409WithConsistentErrorJson() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Finish task",
                                  "description": "Mark it done once",
                                  "dueDate": "2099-05-01"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/tasks/1/done").param("value", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.done").value(true));

        mockMvc.perform(patch("/api/tasks/1/done").param("value", "true"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Task with id 1 is already done."))
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }

    @Test
    void deleteMissingTaskAlsoReturns404ErrorJson() throws Exception {
        mockMvc.perform(delete("/api/tasks/55"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task with id 55 was not found."));
    }
}
