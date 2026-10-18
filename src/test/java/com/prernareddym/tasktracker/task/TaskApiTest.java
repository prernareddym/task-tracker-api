package com.prernareddym.tasktracker.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.prernareddym.tasktracker.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskApiTest extends AbstractIntegrationTest {

    private long createTask(String token, String json) throws Exception {
        String body = mockMvc.perform(post("/api/tasks").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        return node.get("id").asLong();
    }


    @Test
    void createAppliesDefaultsAndReturnsTheTask() throws Exception {
        String token = registerAndGetToken("a@example.com");

        mockMvc.perform(post("/api/tasks").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"  Write README  ","dueDate":"2026-12-01"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Write README"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.dueDate").value("2026-12-01"));
    }

    @Test
    void createRejectsBlankTitleAndUnknownEnum() throws Exception {
        String token = registerAndGetToken("a@example.com");

        mockMvc.perform(post("/api/tasks").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());

        mockMvc.perform(post("/api/tasks").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"x\",\"status\":\"NOPE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateChangesFieldsAndKeepsOmittedEnums() throws Exception {
        String token = registerAndGetToken("a@example.com");
        long id = createTask(token, "{\"title\":\"Old\",\"priority\":\"HIGH\"}");

        mockMvc.perform(put("/api/tasks/" + id).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New\",\"status\":\"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New"))
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void deleteRemovesTheTask() throws Exception {
        String token = registerAndGetToken("a@example.com");
        long id = createTask(token, "{\"title\":\"Temp\"}");

        mockMvc.perform(delete("/api/tasks/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/tasks/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void usersCannotSeeOrModifyEachOthersTasks() throws Exception {
        String alice = registerAndGetToken("alice@example.com");
        String bob = registerAndGetToken("bob@example.com");
        long aliceTask = createTask(alice, "{\"title\":\"Alice only\"}");

        mockMvc.perform(get("/api/tasks/" + aliceTask).header("Authorization", "Bearer " + bob))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/tasks/" + aliceTask).header("Authorization", "Bearer " + bob)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"hijack\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/tasks/" + aliceTask).header("Authorization", "Bearer " + bob))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer " + bob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
        mockMvc.perform(get("/api/tasks/" + aliceTask).header("Authorization", "Bearer " + alice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Alice only"));
    }

    @Test
    void listSupportsFiltersSearchAndPaging() throws Exception {
        String token = registerAndGetToken("a@example.com");
        createTask(token, "{\"title\":\"Fix login bug\",\"status\":\"IN_PROGRESS\",\"priority\":\"HIGH\"}");
        createTask(token, "{\"title\":\"Write docs\",\"status\":\"TODO\",\"priority\":\"LOW\"}");
        createTask(token, "{\"title\":\"Fix typo in docs\",\"status\":\"DONE\",\"priority\":\"LOW\"}");
        createTask(token, "{\"title\":\"100% coverage\",\"status\":\"TODO\"}");

        mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.totalElements").value(4))
                // newest first
                .andExpect(jsonPath("$.content[0].title").value("100% coverage"));
        mockMvc.perform(get("/api/tasks?status=TODO").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.totalElements").value(2));
        mockMvc.perform(get("/api/tasks?priority=LOW&status=DONE").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Fix typo in docs"));
        mockMvc.perform(get("/api/tasks?q=FIX").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.totalElements").value(2));
        // LIKE wildcards in user input are treated literally
        mockMvc.perform(get("/api/tasks").param("q", "%").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/tasks?size=2&page=1").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void summaryCountsPerStatus() throws Exception {
        String token = registerAndGetToken("a@example.com");
        mockMvc.perform(get("/api/tasks/summary").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.total").value(0));
        createTask(token, "{\"title\":\"a\"}");
        createTask(token, "{\"title\":\"b\",\"status\":\"DONE\"}");
        createTask(token, "{\"title\":\"c\",\"status\":\"DONE\"}");

        mockMvc.perform(get("/api/tasks/summary").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.todo").value(1))
                .andExpect(jsonPath("$.inProgress").value(0))
                .andExpect(jsonPath("$.done").value(2))
                .andExpect(jsonPath("$.total").value(3));
    }
}
