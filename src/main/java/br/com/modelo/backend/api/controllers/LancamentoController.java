package br.com.modelo.backend.api.controllers;

import br.com.modelo.backend.api.dtos.LancamentoDto;
import br.com.modelo.backend.api.entities.Funcionario;
import br.com.modelo.backend.api.entities.Lancamento;
import br.com.modelo.backend.api.enums.TipoEnum;
import br.com.modelo.backend.api.response.Response;
import br.com.modelo.backend.api.services.FuncionarioService;
import br.com.modelo.backend.api.services.LancamentoService;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

@RestController
@RequestMapping("/api/lancamentos")
@CrossOrigin(origins = "*")
public class LancamentoController {

    private static final Logger log = LoggerFactory.getLogger(LancamentoController.class);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private LancamentoService lancamentoService;

    @Autowired
    private FuncionarioService funcionarioService;

    @Value("${paginacao.qtd_por_pagina}")
    private int qtdPorPagina;

    public LancamentoController() {
    }


    /**
     * Retorna a listagem de lançamento de um funcionario.
     *
     * @param funcionarioId
     * @param pag
     * @param ord
     * @param dir
     * @return ResponseEntity<Response<LancamentoDto>>
     */
    @GetMapping(value = "/funcionario/{funcionarioId}")
    public ResponseEntity<Response<Page<LancamentoDto>>> listarPorFuncionarioId
            (
                    @PathVariable("funcionarioId") Long funcionarioId,
                    @RequestParam(value = "pag", defaultValue = "0") int pag,
                    @RequestParam(value = "ord", defaultValue = "id") String ord,
                    @RequestParam(value = "dir", defaultValue = "DESC") String dir
            )
    {
        log.info("Buscando lançamentos por ID do funcionário: {}, página: {}", funcionarioId, pag);

        Response<Page<LancamentoDto>> response = new Response<Page<LancamentoDto>>();

        PageRequest pageRequest = PageRequest.of(pag, this.qtdPorPagina, Sort.Direction.valueOf(dir), ord);
        Page<Lancamento> lancamentos = this.lancamentoService.buscarPorFuncionarioId(funcionarioId, pageRequest);
        Page<LancamentoDto> lancamentoDtos = lancamentos.map(lancamento -> this.converteLancamentoDto(lancamento));

        response.setData(lancamentoDtos);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Response<LancamentoDto>> listaPorId(@PathVariable("id") Long id) {
        log.info("Bucando lancamento por ID: {}", id);

        Response<LancamentoDto> response = new Response<LancamentoDto>();
        Optional<Lancamento> lancamento = this.lancamentoService.buscarPorId(id);

        if(!lancamento.isPresent()){
            log.info("Lancamento não encontrado para o id {}", id);
            response.getErrors().add("Lancamento não encontrado para o id  " + id);
            return ResponseEntity.badRequest().body(response);
        }

        response.setData(this.converteLancamentoDto(lancamento.get()));
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "{id}")
    public ResponseEntity<Response<LancamentoDto>> atualizar(@PathVariable("id") Long id
            , @Valid @RequestBody LancamentoDto lancamentoDto, BindingResult result) throws ParseException{

        log.info("Atualizando lancamento: {}", lancamentoDto.toString());
        Response<LancamentoDto> response = new Response<LancamentoDto>();
        validarFuncionario(lancamentoDto, result);
        lancamentoDto.setId(Optional.of(id));
        Lancamento lancamento = this.converteLancamentoDtoParaLancamento(lancamentoDto, result);

        if(result.hasErrors()){
            log.error("Erro validando lancamento: {}", result.getAllErrors());
            result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(response);
        }

        lancamento = this.lancamentoService.persistir(lancamento);
        response.setData(this.converteLancamentoDto(lancamento));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Response<LancamentoDto>> adicionar(@Valid @RequestBody LancamentoDto lancamentoDto, BindingResult result)
            throws ParseException
    {
        log.info("Adcionando lançamento: {}", lancamentoDto.toString());
        Response<LancamentoDto> response = new Response<LancamentoDto>();
        validarFuncionario(lancamentoDto, result);
        Lancamento lancamento = this.converteLancamentoDtoParaLancamento(lancamentoDto, result);

        if(result.hasErrors()){
            log.error("Erro validando lancamcento: {}", result.getAllErrors());
            result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(response);
        }

        lancamento = this.lancamentoService.persistir(lancamento);
        response.setData(this.converteLancamentoDto(lancamento));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Response<String>> remover(@PathVariable("id") Long id){
        log.info("Removendo lançamento: {}", id);
        Response<String> response = new Response<String>();
        Optional<Lancamento> lancamento = this.lancamentoService.buscarPorId(id);

        if(!lancamento.isPresent()){
            log.info("Erro ao remover devido ao lançamento ID: {} ser inválido.", id);
            response.getErrors().add("Erro ao remover lançamento. Registro não encontrado para o id "+id);
            return ResponseEntity.badRequest().body(response);
        }

        this.lancamentoService.remover(id);
        return ResponseEntity.ok(new Response<String>());
    }

    /**
     * Valida um funcionario, verificando se ele é existente e válido no sistema.
     *
     * @param lancamentoDto
     * @param result
     */
    private void validarFuncionario(LancamentoDto lancamentoDto, BindingResult result) {
        if (lancamentoDto.getFuncionarioId() == null){
            result.addError(new ObjectError("Funcionário", "Funcionário não informado."));
            return;
        }

        log.info("Validando funcionario id {}", lancamentoDto.getId());
        Optional<Funcionario> funcionario = this.funcionarioService.buscarPorId(lancamentoDto.getFuncionarioId());
        if(!funcionario.isPresent()){
            result.addError(new ObjectError("Funcionário", "Funcionário não encontrado. ID inexistente."));
        }
    }


    /**
     * @param lancamentoDto
     * @param result
     * @return
     * @throws ParseException
     */
    private Lancamento converteLancamentoDtoParaLancamento(LancamentoDto lancamentoDto, BindingResult result) throws ParseException {
        Lancamento lancamento = new Lancamento();

        if(lancamentoDto.getId().isPresent()){
            Optional<Lancamento> lanc = this.lancamentoService.buscarPorId(lancamentoDto.getId().get());
            if(lanc.isPresent()){
                lancamento = lanc.get();
            }else{
                result.addError(new ObjectError("Lançamento", "Lançamento nao encontrado."));
            }
        }else{
            lancamento.setFuncionario(new Funcionario());
            lancamento.getFuncionario().setId(lancamentoDto.getFuncionarioId());
        }

        lancamento.setDescricao(lancamentoDto.getDescricao());
        lancamento.setLocalizacao(lancamentoDto.getLocalizacao());
        lancamento.setData(this.dateFormat.parse(lancamentoDto.getData()));

        if(EnumUtils.isValidEnum(TipoEnum.class, lancamentoDto.getTipo())){
            lancamento.setTipo(TipoEnum.valueOf(lancamentoDto.getTipo()));
        }else{
            result.addError(new ObjectError("Tipo", "Tipo inválido."));
        }

        return lancamento;
    }


    /**
     * Converte um lançamento em um lancamento DTO.
     *
     * @param lancamento
     * @return
     */
    private LancamentoDto converteLancamentoDto(Lancamento lancamento) {
        LancamentoDto lancamentoDto = new LancamentoDto();
        lancamentoDto.setId(Optional.of(lancamento.getId()));
        lancamentoDto.setData(this.dateFormat.format(lancamento.getData()));
        lancamentoDto.setTipo(lancamento.getTipo().toString());
        lancamentoDto.setDescricao(lancamento.getDescricao());
        lancamentoDto.setLocalizacao(lancamento.getLocalizacao());
        lancamentoDto.setFuncionarioId(lancamento.getFuncionario().getId());

        return lancamentoDto;
    }
}
