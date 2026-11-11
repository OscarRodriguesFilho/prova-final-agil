package br.edu.insper.provafinalagil;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

@SpringBootTest
@AutoConfigureMockMvc
class ArtigoControllerTest {
    private static final String ROTA = ArtigoController.ROTA;
    private static final String NS = "https://prova-final-agil";
    private static final Auth0RolesConverter ROLES = new Auth0RolesConverter(NS + "/roles");

    @Autowired private MockMvc mockMvc;
    @Autowired private ArtigoRepository repository;

    @BeforeEach
    void limparBanco() {
        repository.deleteAll();
    }

    private static JwtRequestPostProcessor token(String email, String... roles) {
        return jwt()
                .jwt(j -> j.subject("auth0|" + email).claim(NS + "/roles", List.of(roles)).claim(NS + "/email", email))
                .authorities(ROLES);
    }

    private static String corpo(String titulo, String prioridade) {
        return """
                {"titulo": "%s", "descricao": "Passo a passo para configurar a VPN", "prioridade": "%s"}
                """.formatted(titulo, prioridade);
    }

    private long criar(String json) throws Exception {
        String resposta = mockMvc.perform(post(ROTA).with(token("admin@empresa.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(resposta.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(get(ROTA)).andExpect(status().isUnauthorized());
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(corpo("VPN", "ALTA")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete(ROTA + "/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSemRoleNaoLista() throws Exception {
        mockMvc.perform(get(ROTA).with(token("visitante@empresa.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void userNaoCriaNemExclui() throws Exception {
        long id = criar(corpo("VPN", "ALTA"));
        mockMvc.perform(post(ROTA).with(token("user@empresa.com", "USER"))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo("Outro", "BAIXA")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete(ROTA + "/" + id).with(token("user@empresa.com", "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCriaEUserLista() throws Exception {
        mockMvc.perform(post(ROTA).with(token("admin@empresa.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo("Como configurar a VPN", "ALTA")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Como configurar a VPN"))
                .andExpect(jsonPath("$.descricao").value("Passo a passo para configurar a VPN"))
                .andExpect(jsonPath("$.prioridade").value("ALTA"))
                .andExpect(jsonPath("$.emailCriador").value("admin@empresa.com"));

        mockMvc.perform(get(ROTA).with(token("user@empresa.com", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("Como configurar a VPN"));
    }

    @Test
    void emailDoTokenTemPrioridadeSobreOCorpo() throws Exception {
        mockMvc.perform(post(ROTA).with(token("admin@empresa.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": "X", "descricao": "Y", "prioridade": "MEDIA", "emailCriador": "outra@empresa.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emailCriador").value("admin@empresa.com"));
    }

    @Test
    void camposObrigatoriosRetornam400() throws Exception {
        mockMvc.perform(post(ROTA).with(token("admin@empresa.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.titulo").exists())
                .andExpect(jsonPath("$.campos.descricao").exists())
                .andExpect(jsonPath("$.campos.prioridade").exists());
    }

    @Test
    void prioridadeForaDaListaRetorna400() throws Exception {
        mockMvc.perform(post(ROTA).with(token("admin@empresa.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo("VPN", "URGENTISSIMA")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Corpo da requisição inválido"));
    }

    @Test
    void adminExcluiEIdInexistenteRetorna404() throws Exception {
        long id = criar(corpo("VPN", "ALTA"));
        mockMvc.perform(delete(ROTA + "/" + id).with(token("admin@empresa.com", "ADMIN")))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(ROTA).with(token("admin@empresa.com", "ADMIN")))
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(delete(ROTA + "/" + id).with(token("admin@empresa.com", "ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Artigo não encontrado"));
    }
}
