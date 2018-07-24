package br.com.modelo.backend.api.controllers;

import br.com.modelo.backend.api.dtos.CadastroPJDto;
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
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("api/cadastrar-pj")
@CrossOrigin(origins = "*")
public class CadastroPJController {

    private static final Logger log = LoggerFactory.getLogger(CadastroPJController.class);

    @Autowired
    private FuncionarioService funcionarioService;

    @Autowired
    private EmpresaService empresaService;

    public CadastroPJController() {
    }

    /**
     * Cadastra uma pessoa jurídica no sistema
     *
     * @param cadastroPJDto
     * @param result
     * @return ResponseEntity<ResponseEntity<CadastroPJDto>>
     * @throws NoSuchAlgorithmException
     * */
    @PostMapping
    public ResponseEntity<Response<CadastroPJDto>> cadastrar(@Valid @RequestBody CadastroPJDto cadastroPJDto, BindingResult result)
        throws NoSuchAlgorithmException{

        log.info("Cadastrando PJ: {}", cadastroPJDto.toString());
        Response<CadastroPJDto> response = new Response<CadastroPJDto>();

        validaDadosExistentes(cadastroPJDto, result);
        Empresa empresa = this.converterDtoParaEmpresa(cadastroPJDto);
        Funcionario funcionario = this.converterDtoParaFuncionario(cadastroPJDto, result);

        if(result.hasErrors()){
            log.error("Erro validando dados de cadastro PJ: {}", result.getAllErrors());
            result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
            return ResponseEntity.badRequest().body((response));
        }

        this.empresaService.persistir(empresa);
        funcionario.setEmpresa(empresa);
        this.funcionarioService.persistir(funcionario);

        response.setData(this.converterCadastroPJDto(funcionario));
        return ResponseEntity.ok(response);
    }

    /**
     * Verifica se a empresa ou funcionário já existe no banco de dados.
     *
     * @param cadastroPJDto
     * @param result
     * */
    private void validaDadosExistentes(CadastroPJDto cadastroPJDto, BindingResult result){
        this.empresaService.buscarPorCnpj(cadastroPJDto.getCnpj())
                .ifPresent(empresa -> result.addError(new ObjectError("Empresa","Empresa "+cadastroPJDto.getRazaoSocial()+" já existente.")));
        this.funcionarioService.buscarPorCpf(cadastroPJDto.getCpf())
                .ifPresent(funcionario -> result.addError(new ObjectError("Funcionario", "Funcionario com CPF "+cadastroPJDto.getCpf()+" já existente.")));
        this.funcionarioService.buscarPorEmail(cadastroPJDto.getEmail())
                .ifPresent(funcionario -> result.addError(new ObjectError("Funcionário", "Funcionario com Email "+cadastroPJDto.getEmail()+" já existente.")));

    }

    /**
     * Converte os dados de DTO para empresa.
     *
     * @param cadastroPJDto
     * @return Empresa
     * */
    private Empresa converterDtoParaEmpresa(CadastroPJDto cadastroPJDto){
        Empresa empresa = new Empresa();
        empresa.setCnpj(cadastroPJDto.getCnpj());
        empresa.setRazaoSocial(cadastroPJDto.getRazaoSocial());

        return empresa;
    }

    /**
     * Converte os dados de DTO para funcionario.
     *
     * @param cadastroPJDto
     * @param result
     * @return Funcionario
     * @throws NoSuchAlgorithmException
     * */
    private Funcionario converterDtoParaFuncionario(CadastroPJDto cadastroPJDto, BindingResult result) throws NoSuchAlgorithmException{
        Funcionario funcionario = new Funcionario();
        funcionario.setNome(cadastroPJDto.getNome());
        funcionario.setEmail(cadastroPJDto.getEmail());
        funcionario.setCpf(cadastroPJDto.getCpf());
        funcionario.setPerfil(PerfilEnum.ROLE_ADMIN);
        funcionario.setSenha(PasswordUtils.gerarBCrypt(cadastroPJDto.getSenha()));

        return funcionario;
    }

    private CadastroPJDto converterCadastroPJDto(Funcionario funcionario){
        CadastroPJDto cadastroPJDto = new CadastroPJDto();
        cadastroPJDto.setId(funcionario.getId());
        cadastroPJDto.setNome(funcionario.getNome());
        cadastroPJDto.setEmail(funcionario.getEmail());
        cadastroPJDto.setCpf(funcionario.getCpf());
        cadastroPJDto.setRazaoSocial(funcionario.getEmpresa().getRazaoSocial());
        cadastroPJDto.setCnpj(funcionario.getEmpresa().getCnpj());

        return cadastroPJDto;
    }
}
