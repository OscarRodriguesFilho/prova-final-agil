package br.edu.insper.provafinalagil;

/** Lance esta exceção no Service para devolver 400 com uma mensagem própria. */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String message) {
        super(message);
    }
}
