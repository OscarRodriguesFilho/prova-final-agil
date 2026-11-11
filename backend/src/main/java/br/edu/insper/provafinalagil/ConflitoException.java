package br.edu.insper.provafinalagil;

/** Lance esta exceção no Service para devolver 409 (conflito, ex.: horário já ocupado, item duplicado). */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String message) {
        super(message);
    }
}
