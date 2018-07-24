package br.com.modelo.backend.api.services;

import br.com.modelo.backend.api.entities.Empresa;

import java.util.Optional;

public interface EmpresaService {

    /**
     * Retorna uma empresa dado um CNPJ
     *
     * @param cnpj
     * @return Optional<Empresa>
     * @author Paulo Roberto Mesquita da Silva
     * */
    Optional<Empresa> buscarPorCnpj(String cnpj);

    /**
     * Cadastra uma empresa na base de dados
     *
     * @param empresa
     * @return Empresa
     * @author Paulo Roberto Mesquita da Silva
     *
     * */
    Empresa persistir(Empresa empresa);
}
