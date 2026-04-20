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
    void createListGetUpdatePatchAndDeleteWorkWithDueDate() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Write tests",
                                  "description": "Cover controller behavior",
                                  "dueDate": "2099-05-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Write tests"))
                .andExpect(jsonPath("$.description").value("Cover controller behavior"))
                .andExpect(jsonPath("$.done").value(false))
                .andExpect(jsonPath("$.dueDate").value("2099-05-01"));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Write tests"))
                .andExpect(jsonPath("$[0].done").value(false))
                .andExpect(jsonPath("$[0].dueDate").value("2099-05-01"));

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Write tests"))
                .andExpect(jsonPath("$.dueDate").value("2099-05-01"));

        mockMvc.perform(get("/api/tasks/search").param("done", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].done").value(false))
                .andExpect(jsonPath("$[0].dueDate").value("2099-05-01"));

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Write better tests",
                                  "description": "Cover validation, dates, and errors",
                                  "done": true,
                                  "dueDate": "2099-06-15"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Write better tests"))
                .andExpect(jsonPath("$.description").value("Cover validation, dates, and errors"))
                .andExpect(jsonPath("$.done").value(true))
                .andExpect(jsonPath("$.dueDate").value("2099-06-15"));

        mockMvc.perform(patch("/api/tasks/1/done").param("value", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.done").value(false))
                .andExpect(jsonPath("$.dueDate").value("2099-06-15"));

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }
}
