package br.com.modelo.backend.api.services.impl;

import br.com.modelo.backend.api.entities.Lancamento;
import br.com.modelo.backend.api.repositories.LancamentoRepository;
import br.com.modelo.backend.api.services.LancamentoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LancamentoServiceImpl implements LancamentoService{

    private static final Logger log = LoggerFactory.getLogger(LancamentoServiceImpl.class);

    @Autowired
    private LancamentoRepository lancamentoRepository;

    @Override
    public Page<Lancamento> buscarPorFuncionarioId(Long funcionarioId, PageRequest pageRequest) {
        log.info("Bucando lançamentos para o funcionario ID {}", funcionarioId);
        return this.lancamentoRepository.findByFuncionarioId(funcionarioId, pageRequest);
    }

    @Override
    public Optional<Lancamento> buscarPorId(Long id) {
        log.info("Bucando um lançamento por funcionario ID {}", id);
        return this.lancamentoRepository.findById(id);
    }

    @Override
    public Lancamento persistir(Lancamento lancamento) {
        log.info("Persistindo o lancamento: {}", lancamento);
        return this.lancamentoRepository.save(lancamento);
    }

    @Override
    public void remover(Long id) {
        log.info("Removendo lancamento por ID: {}", id);
        this.lancamentoRepository.deleteById(id);
    }
}
