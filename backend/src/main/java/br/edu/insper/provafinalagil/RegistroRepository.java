package br.edu.insper.provafinalagil;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroRepository extends JpaRepository<Registro, Long> {
    List<Registro> findAllByOrderByDataDescIdDesc();
}
