package br.edu.insper.provafinalagil;

/** Lance esta exceção no Service para devolver 404 (ex.: buscar/excluir um id que não existe). */
public class NaoEncontradoException extends RuntimeException {
    public NaoEncontradoException(String message) {
        super(message);
    }
}
