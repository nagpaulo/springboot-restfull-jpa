package br.com.modelo.backend.api.controllers;

import br.com.modelo.backend.api.dtos.CadastroPFDto;
import br.com.modelo.backend.api.entities.Empresa;
import br.com.modelo.backend.api.entities.Funcionario;
import br.com.modelo.backend.api.enums.PerfilEnum;
import br.com.modelo.backend.api.response.Response;
import br.com.modelo.backend.api.services.EmpresaService;
import br.com.modelo.backend.api.services.FuncionarioService;
import br.com.modelo.backend.api.utils.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@RestController
@RequestMapping("api/cadastrar-pf")
@CrossOrigin(origins = "*")
public class CadastroPFController {
    private static final Logger log = LoggerFactory.getLogger(CadastroPFController.class);

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private FuncionarioService funcionarioService;

    public CadastroPFController() {
    }

    /**
     * Cadastra uma pessoa jurídica no sistema
     *
     * @param cadastroPFDto
     * @param result
     * @return ResponseEntity<ResponseEntity<CadastroPFDto>>
     * @throws NoSuchAlgorithmException
     * */
    @PostMapping
    public ResponseEntity<Response<CadastroPFDto>> cadastrar(@Valid @RequestBody CadastroPFDto cadastroPFDto, BindingResult result)
        throws NoSuchAlgorithmException
    {
        log.info("Cadastrando PF: {}", cadastroPFDto.toString());
        Response<CadastroPFDto> response = new Response<CadastroPFDto>();

        validarDadosExistentes(cadastroPFDto, result);
        Funcionario funcionario = this.converterDtoParaFuncionario(cadastroPFDto, result);

        if(result.hasErrors()){
            log.error("Erro validando dados de cadastro PF: {}", result.getAllErrors());
            result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
            return ResponseEntity.badRequest().body((response));
        }

        Optional<Empresa> empresa = this.empresaService.buscarPorCnpj(cadastroPFDto.getCnpj());
        empresa.ifPresent(emp -> funcionario.setEmpresa(emp));
        this.funcionarioService.persistir(funcionario);

        response.setData(this.converterCadastroPFDto(funcionario));
        return ResponseEntity.ok(response);
    }

    /**
     * Verifica se o funcionário já existe no banco de dados.
     *
     * @param cadastroPFDto
     * @param result
     * */
    private void validarDadosExistentes(CadastroPFDto cadastroPFDto, BindingResult result){
        Optional<Empresa> empresa = this.empresaService.buscarPorCnpj(cadastroPFDto.getCnpj());
        if(!empresa.isPresent()){
            result.addError(new ObjectError("Empresa","Empresa com CNPJ: "+cadastroPFDto.getCnpj()+", não encontrada."));
        }

        this.funcionarioService.buscarPorCpf(cadastroPFDto.getCpf())
                .ifPresent(funcionario -> result.addError(new ObjectError("Funcionario", "Funcionario com CPF "+cadastroPFDto.getCpf()+" já existente.")));
        this.funcionarioService.buscarPorEmail(cadastroPFDto.getEmail())
                .ifPresent(funcionario -> result.addError(new ObjectError("Funcionário", "Funcionario com Email "+cadastroPFDto.getEmail()+" já existente.")));

    }

    /**
     * Converte os dados de DTO para funcionario.
     *
     * @param cadastroPFDto
     * @param result
     * @return Funcionario
     * @throws NoSuchAlgorithmException
     * */
    private Funcionario converterDtoParaFuncionario(CadastroPFDto cadastroPFDto, BindingResult result) throws NoSuchAlgorithmException{
        Funcionario funcionario = new Funcionario();
        funcionario.setNome(cadastroPFDto.getNome());
        funcionario.setEmail(cadastroPFDto.getEmail());
        funcionario.setCpf(cadastroPFDto.getCpf());
        funcionario.setPerfil(PerfilEnum.ROLE_USUARIO);
        funcionario.setSenha(PasswordUtils.gerarBCrypt(cadastroPFDto.getSenha()));
        cadastroPFDto.getQtdHorasAlmoco().ifPresent(qtdHorasAlmoco -> funcionario.setQtdHorasAlmoco(Float.valueOf(qtdHorasAlmoco)));
        cadastroPFDto.getQtdHorasTrabalhaDia().ifPresent(qtdHorasTrabalhoDia -> funcionario.setQtdHorasTrabalhoDia(Float.valueOf(qtdHorasTrabalhoDia)));
        cadastroPFDto.getValorHora().ifPresent(valorHora -> funcionario.setValorHora(new BigDecimal(valorHora)));

        return funcionario;
    }


    private CadastroPFDto converterCadastroPFDto(Funcionario funcionario){
        CadastroPFDto cadastroPFDto = new CadastroPFDto();
        cadastroPFDto.setId(funcionario.getId());
        cadastroPFDto.setNome(funcionario.getNome());
        cadastroPFDto.setEmail(funcionario.getEmail());
        cadastroPFDto.setCpf(funcionario.getCpf());
        cadastroPFDto.setCnpj(funcionario.getEmpresa().getCnpj());
        funcionario.getQtdHorasAlmocoOpt().ifPresent(qtdHorasAlmoco -> cadastroPFDto.setQtdHorasAlmoco(Optional.of(Float.toString(qtdHorasAlmoco))));
        funcionario.getQtdHorasTrabalhoDiaOpt().ifPresent(qtdHorasTrabalhoDia -> cadastroPFDto.setQtdHorasTrabalhaDia(Optional.of(Float.toString(qtdHorasTrabalhoDia))));
        funcionario.getValorHoraOpt().ifPresent(qtdValorHora -> cadastroPFDto.setValorHora(Optional.of(qtdValorHora.toString())));

        return cadastroPFDto;
    }
}
