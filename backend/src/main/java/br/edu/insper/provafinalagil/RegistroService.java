package br.edu.insper.provafinalagil;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class RegistroService {
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private final RegistroRepository repository;

    public RegistroService(RegistroRepository repository) {
        this.repository = repository;
    }

    public Registro cadastrar(RegistroRequest request, String emailDoToken) {
        String emailCriador = temTexto(emailDoToken) ? emailDoToken : request.emailCriador();
        if (!temTexto(emailCriador)) {
            throw new RegraDeNegocioException("Não foi possível identificar o email do usuário");
        }

        // Exemplo de regra de negócio (devolve 400 com a mensagem):
        // if (request.valor() < 5 && request.descricao().length() < 20) {
        //     throw new RegraDeNegocioException("Valores abaixo de 5 precisam de uma descrição detalhada");
        // }

        LocalDate data = request.data() != null ? request.data() : LocalDate.now(FUSO);
        Registro registro = new Registro(
                emailCriador.trim().toLowerCase(Locale.ROOT),
                request.titulo().trim(),
                request.descricao().trim(),
                request.valor(),
                data);
        return repository.save(registro);
    }

    public List<Registro> listar() {
        return repository.findAllByOrderByDataDescIdDesc();
    }

    private static boolean temTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
