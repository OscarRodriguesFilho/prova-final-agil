package br.edu.insper.provafinalagil;

import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ArtigoService {
    private final ArtigoRepository repository;

    public ArtigoService(ArtigoRepository repository) {
        this.repository = repository;
    }

    public Artigo criar(ArtigoRequest request, String emailDoToken) {
        String email = temTexto(emailDoToken) ? emailDoToken : request.emailCriador();
        if (!temTexto(email)) {
            throw new RegraDeNegocioException("Não foi possível identificar o email do usuário"); // 400
        }
        Artigo artigo = new Artigo(
                request.titulo().trim(),
                request.descricao().trim(),
                request.prioridade(),
                email.trim().toLowerCase(Locale.ROOT));
        return repository.save(artigo);
    }

    public List<Artigo> listar() {
        return repository.findAllByOrderByIdDesc();
    }

    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new NaoEncontradoException("Artigo não encontrado"); // 404
        }
        repository.deleteById(id);
    }

    private static boolean temTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
