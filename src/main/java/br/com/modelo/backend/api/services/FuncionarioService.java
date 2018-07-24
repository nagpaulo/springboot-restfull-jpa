package br.com.modelo.backend.api.services;

import br.com.modelo.backend.api.entities.Funcionario;

import java.util.Optional;

public interface FuncionarioService {


    /**
     * Persisti um funcionario na base de dados.
     *
     * @param funcionario
     * @return Funcionario
     * @author Paulo Roberto Mesquita da Silva
     */
    Funcionario persistir(Funcionario funcionario);


    /**
     * Busca e retorna um funcionario dado um CPF.
     *
     * @param cpf
     * @return Optional<Funcionario>
     * @author Paulo Roberto Mesquita da Silva
     */
    Optional<Funcionario> buscarPorCpf(String cpf);


    /**
     * Busca e retorna um funcionario dado um email.
     *
     * @param email
     * @return Optional<Funcionario>
     * @author Paulo Roberto Mesquita da Silva
     */
    Optional<Funcionario> buscarPorEmail(String email);


    /**
     * Busca e retorna um funcionario por ID.
     *
     * @param id
     * @return Optional<Funcionario>
     * @author Paulo Roberto Mesquita da Silva
     */
    Optional<Funcionario> buscarPorId(Long id);

}
