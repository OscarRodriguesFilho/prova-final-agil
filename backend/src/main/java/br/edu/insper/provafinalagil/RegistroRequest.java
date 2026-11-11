package br.edu.insper.provafinalagil;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Corpo do POST. As anotações de validação geram 400 automaticamente (ver ApiExceptionHandler).
 * Outras úteis: @Positive, @PastOrPresent, @Future, @Pattern(regexp = "..."), @DecimalMin("0.0").
 *
 * emailCriador é opcional: quando o token traz o email do usuário logado, ele tem prioridade.
 * data é opcional: quando ausente, a API usa a data de hoje.
 */
public record RegistroRequest(
        @Email(message = "deve ser um email válido") String emailCriador,
        @NotBlank(message = "é obrigatório") @Size(max = 200, message = "deve ter no máximo 200 caracteres") String titulo,
        @NotBlank(message = "é obrigatória") @Size(max = 2000, message = "deve ter no máximo 2000 caracteres") String descricao,
        @NotNull(message = "é obrigatório") @Min(value = 0, message = "deve estar entre 0 e 10") @Max(value = 10, message = "deve estar entre 0 e 10") Integer valor,
        LocalDate data) {
}
