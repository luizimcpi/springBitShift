package com.example.bitshift.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

/**
 * Teste de integração do controller com H2 real: valida o fluxo HTTP
 * completo e a persistência do bitmask de ponta a ponta.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UsuarioControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void fluxoCompletoCrudDevePersistirBitmask() throws Exception {
        // CREATE: ativo=true, verificado=false → flags=1
        String criado = mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana","ativo":true,"verificado":false}"""))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.verificado").value(false))
                .andExpect(jsonPath("$.flags").value(1))
                .andExpect(jsonPath("$.flagsBinario").value("00000001"))
                .andReturn().getResponse().getContentAsString();
        long id = new tools.jackson.databind.ObjectMapper().readTree(criado).get("id").asLong();

        // READ
        mvc.perform(get("/api/usuarios/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana"));

        // UPDATE: liga o segundo bit → flags=3
        mvc.perform(put("/api/usuarios/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana Souza","ativo":true,"verificado":true}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flags").value(3))
                .andExpect(jsonPath("$.flagsBinario").value("00000011"));

        // LIST com filtro bitwise
        mvc.perform(get("/api/usuarios").param("ativo", "true").param("verificado", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Ana Souza"));

        // DELETE
        mvc.perform(delete("/api/usuarios/" + id))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/usuarios/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void criarSemNomeDeveRetornar400() throws Exception {
        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","ativo":false,"verificado":false}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarInexistenteDeveRetornar404() throws Exception {
        mvc.perform(get("/api/usuarios/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
