package br.edu.insper.provafinalagil;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Corpo do POST /artigos. emailCriador é opcional: quando o token traz o email do usuário logado, ele tem prioridade.
 */
public record ArtigoRequest(
        @NotBlank(message = "é obrigatório") @Size(max = 200, message = "deve ter no máximo 200 caracteres") String titulo,
        @NotBlank(message = "é obrigatória") @Size(max = 2000, message = "deve ter no máximo 2000 caracteres") String descricao,
        @NotNull(message = "é obrigatória") Prioridade prioridade,
        @Email(message = "deve ser um email válido") String emailCriador) {
}
