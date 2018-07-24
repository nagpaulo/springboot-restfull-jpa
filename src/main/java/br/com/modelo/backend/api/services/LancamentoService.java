package br.com.modelo.backend.api.services;

import br.com.modelo.backend.api.entities.Lancamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

public interface LancamentoService {


    /**
     * Retorna uma lista paginada de lancamento de um determinado funcionario.
     *
     * @param funcionarioId
     * @param pageRequest
     * @return Page<Lancamento>
     */
    Page<Lancamento> buscarPorFuncionarioId(Long funcionarioId, PageRequest pageRequest);

    /**
     * Retorna um lançamento por ID.
     *
     * @param id
     * @return Optional<Lancamento>
     */
    Optional<Lancamento> buscarPorId(Long id);


    /**
     * Retorna uma Lancamento na base de dados.
     *
     * @param lancamento
     * @return Lancamento
     */
    Lancamento persistir(Lancamento lancamento);


    /**
     * Remove um lancamento da base de dados.
     *
     * @param id
     */
    void remover(Long id);

}
