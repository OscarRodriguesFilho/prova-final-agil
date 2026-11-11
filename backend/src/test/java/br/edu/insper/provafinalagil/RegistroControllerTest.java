package br.edu.insper.provafinalagil;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Testes de integração das rotas com tokens simulados (não precisa do Auth0 de verdade).
 * Ao trocar a entidade na prova, ajuste o CORPO_VALIDO e os jsonPath abaixo.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RegistroControllerTest {
    private static final String ROTA = RegistroController.ROTA;
    private static final String NS = "https://prova-final-agil";
    private static final Auth0RolesConverter ROLES = new Auth0RolesConverter(NS + "/roles");

    private static final String CORPO_VALIDO = """
            {
              "titulo": "Primeiro registro",
              "descricao": "Descrição de exemplo",
              "valor": 9,
              "data": "2026-11-10"
            }
            """;

    @Autowired private MockMvc mockMvc;
    @Autowired private RegistroRepository repository;

    @BeforeEach
    void limparBanco() {
        repository.deleteAll();
    }

    /** Simula um access token do Auth0 com as claims que a Action adiciona. */
    private static JwtRequestPostProcessor token(String email, String... roles) {
        return jwt()
                .jwt(j -> j.subject("auth0|" + email).claim(NS + "/roles", List.of(roles)).claim(NS + "/email", email))
                .authorities(ROLES);
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(get(ROTA)).andExpect(status().isUnauthorized());
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userNaoPodeCadastrar() throws Exception {
        mockMvc.perform(post(ROTA).with(token("user@empresa.com", "USER"))
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    void tokenSemRoleNaoPodeListar() throws Exception {
        mockMvc.perform(get(ROTA).with(token("visitante@empresa.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCadastraEUserLista() throws Exception {
        mockMvc.perform(post(ROTA).with(token("admin@empresa.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.emailCriador").value("admin@empresa.com"))
                .andExpect(jsonPath("$.titulo").value("Primeiro registro"))
                .andExpect(jsonPath("$.valor").value(9))
                .andExpect(jsonPath("$.data").value("2026-11-10"));

        mockMvc.perform(get(ROTA).with(token("user@empresa.com", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao").value("Descrição de exemplo"));
    }

    @Test
    void emailDoTokenTemPrioridadeEDataPadraoEHoje() throws Exception {
        mockMvc.perform(post(ROTA).with(token("admin@empresa.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"emailCriador": "outra@empresa.com", "titulo": "Sem data",
                                 "descricao": "Teste", "valor": 7}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emailCriador").value("admin@empresa.com"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    void valorForaDoIntervaloRetorna400() throws Exception {
        mockMvc.perform(post(ROTA).with(token("admin@empresa.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": "X", "descricao": "Teste", "valor": 11}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.valor").value("deve estar entre 0 e 10"));
    }

    @Test
    void camposObrigatoriosRetornam400() throws Exception {
        mockMvc.perform(post(ROTA).with(token("admin@empresa.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.titulo").exists())
                .andExpect(jsonPath("$.campos.descricao").exists())
                .andExpect(jsonPath("$.campos.valor").exists());
    }
}
