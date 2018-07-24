package br.com.modelo.backend.api.repositories;

import br.com.modelo.backend.api.entities.Empresa;
import br.com.modelo.backend.api.entities.Funcionario;
import br.com.modelo.backend.api.entities.Lancamento;
import br.com.modelo.backend.api.enums.PerfilEnum;
import br.com.modelo.backend.api.enums.TipoEnum;
import br.com.modelo.backend.api.utils.PasswordUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class LancamentoRepositoryTest {

    @Autowired
    private LancamentoRepository lancamentoRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    private Long funcionarioId;

    @Before
    public void setUp() throws Exception{
        Empresa empresa = this.empresaRepository.save(obterDadosEmpresa());

        Funcionario funcionario = this.funcionarioRepository.save(obterDadosFuncionario(empresa));
        this.funcionarioId = funcionario.getId();

        this.lancamentoRepository.save(obterDadosLancamentos(funcionario));
        this.lancamentoRepository.save(obterDadosLancamentos(funcionario));
    }

    @After
    public void tearDown() throws Exception{
        this.empresaRepository.deleteAll();
    }

    @Test
    public void testBuscarLancamentosPorFuncionarioId(){
        List<Lancamento> lancamentos = this.lancamentoRepository.findByFuncionarioId(funcionarioId);
        assertEquals(2,lancamentos.size());
    }


    @Test
    public void testBuscarLancamentosPorFuncionarioIdPaginado(){
        PageRequest pageRequest = PageRequest.of(0,10);
        Page<Lancamento> lancamentos = this.lancamentoRepository.findByFuncionarioId(funcionarioId,pageRequest);

        assertEquals(2,lancamentos.getTotalElements());
    }

    private Lancamento obterDadosLancamentos(Funcionario funcionario){
        Lancamento lancamento = new Lancamento();
        lancamento.setData(new Date());
        lancamento.setTipo(TipoEnum.INICIO_ALMOCO);
        lancamento.setFuncionario(funcionario);
        lancamento.setLocalizacao("Matriz");
        lancamento.setDescricao("Lançamento Teste");
        return lancamento;
    }

    private Funcionario obterDadosFuncionario(Empresa empresa) throws NoSuchAlgorithmException {
        Funcionario funcionario = new Funcionario();
        funcionario.setNome("Fulano de Tal");
        funcionario.setPerfil(PerfilEnum.ROLE_USUARIO);
        funcionario.setSenha(PasswordUtils.gerarBCrypt("123456"));
        funcionario.setCpf("24291173474");
        funcionario.setEmail("email@email.com");
        funcionario.setQtdHorasAlmoco(2f);
        funcionario.setQtdHorasTrabalhoDia(8f);
        funcionario.setEmpresa(empresa);
        funcionario.setValorHora(new BigDecimal(20.5));

        return funcionario;
    }

    private Empresa obterDadosEmpresa(){
        Empresa empresa = new Empresa();
        empresa.setRazaoSocial("Empresa de exemplo");
        empresa.setCnpj("51463645000100");

        return empresa;
    }

}
