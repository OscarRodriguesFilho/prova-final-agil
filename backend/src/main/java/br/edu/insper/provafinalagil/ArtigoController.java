package br.edu.insper.provafinalagil;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ArtigoController.ROTA)
public class ArtigoController {
    public static final String ROTA = "/artigos";

    private final ArtigoService service;
    private final String emailClaim;

    public ArtigoController(ArtigoService service, @Value("${app.auth0.email-claim}") String emailClaim) {
        this.service = service;
        this.emailClaim = emailClaim;
    }

    /** POST /artigos: somente ADMIN (regra em SecurityConfig). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Artigo criar(@Valid @RequestBody ArtigoRequest request, @AuthenticationPrincipal Jwt jwt) {
        String emailDoToken = jwt != null ? jwt.getClaimAsString(emailClaim) : null;
        return service.criar(request, emailDoToken);
    }

    /** GET /artigos: ADMIN e USER (regra em SecurityConfig). */
    @GetMapping
    public List<Artigo> listar() {
        return service.listar();
    }

    /** DELETE /artigos/{id}: somente ADMIN (regra em SecurityConfig). */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
