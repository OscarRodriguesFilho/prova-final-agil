package br.edu.insper.provafinalagil;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RegistroController.ROTA)
public class RegistroController {
    /** Nome da rota. Trocar aqui muda o controller E as regras de segurança (SecurityConfig usa esta constante). */
    public static final String ROTA = "/prova-final-agil";

    private final RegistroService service;
    private final String emailClaim;

    public RegistroController(RegistroService service, @Value("${app.auth0.email-claim}") String emailClaim) {
        this.service = service;
        this.emailClaim = emailClaim;
    }

    /** POST /prova-final-agil: somente ADMIN (regra em SecurityConfig). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Registro cadastrar(@Valid @RequestBody RegistroRequest request, @AuthenticationPrincipal Jwt jwt) {
        String emailDoToken = jwt != null ? jwt.getClaimAsString(emailClaim) : null;
        return service.cadastrar(request, emailDoToken);
    }

    /** GET /prova-final-agil: ADMIN e USER (regra em SecurityConfig). */
    @GetMapping
    public List<Registro> listar() {
        return service.listar();
    }
}
