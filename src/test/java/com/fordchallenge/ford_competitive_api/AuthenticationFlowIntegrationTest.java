package com.fordchallenge.ford_competitive_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fordchallenge.ford_competitive_api.users.entity.User;
import com.fordchallenge.ford_competitive_api.users.entity.UserRole;
import com.fordchallenge.ford_competitive_api.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração ponta a ponta, exercitando o filtro JWT real e as regras
 * de autorização do SecurityConfig (Sprint 3 - critérios "Autenticação e
 * Autorização" e "Testes Automatizados": cenários de sucesso, erro e acesso
 * não autorizado).
 *
 * Usa o perfil "test" (H2 em memória, ver application-test.properties).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void deveRegistrarELogarComSucesso() throws Exception {
        String registerBody = "{\"nome\":\"Victor Hugo\",\"email\":\"victor@test.com\",\"senha\":\"123456\"}";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("victor@test.com"))
                .andExpect(jsonPath("$.senhaHash").doesNotExist());

        String loginBody = "{\"email\":\"victor@test.com\",\"senha\":\"123456\"}";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void deveRejeitarRegistroDeEmailDuplicado_retorna409() throws Exception {
        String body = "{\"nome\":\"Victor\",\"email\":\"duplicado@test.com\",\"senha\":\"123456\"}";

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRejeitarLoginComSenhaErrada_retorna401() throws Exception {
        String registerBody = "{\"nome\":\"Victor\",\"email\":\"senhaerrada@test.com\",\"senha\":\"123456\"}";
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerBody));

        String loginBody = "{\"email\":\"senhaerrada@test.com\",\"senha\":\"errada123\"}";

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRejeitarCorpoInvalido_retorna400() throws Exception {
        String registerBodyInvalido = "{\"nome\":\"\",\"email\":\"nao-e-email\",\"senha\":\"123\"}";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBodyInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveNegarAcessoAoEndpointProtegidoSemToken_retorna401() throws Exception {
        String searchBody = "{\"marca\":\"Ford\",\"modelo\":\"Ranger\",\"ano\":2026,\"versao\":\"Raptor\"}";

        mockMvc.perform(post("/vehicles/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(searchBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirAcessoAEndpointPublicoDeLeituraSemToken() throws Exception {
        mockMvc.perform(get("/vehicles"))
                .andExpect(status().isOk());
    }

    @Test
    void deveAutorizarBuscaDeVeiculoComTokenValidoERetornar201SeCriado() throws Exception {
        String token = registerAndLogin("buscaok@test.com");

        String searchBody = "{\"marca\":\"Ford\",\"modelo\":\"Bronco\",\"ano\":2026,\"versao\":\"Wildtrak\"}";

        mockMvc.perform(post("/vehicles/search")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(searchBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.marca").value("Ford"));
    }

    @Test
    void deveNegarEndpointAdminParaUsuarioComum_retorna403() throws Exception {
        String token = registerAndLogin("comum@test.com");

        mockMvc.perform(get("/searches/history/all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirEndpointAdminParaUsuarioAdmin_retorna200() throws Exception {
        User admin = User.builder()
                .nome("Admin")
                .email("admin@test.com")
                .senhaHash(passwordEncoder.encode("123456"))
                .role(UserRole.ADMIN)
                .build();
        userRepository.save(admin);

        String loginBody = "{\"email\":\"admin@test.com\",\"senha\":\"123456\"}";

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(response).get("accessToken").asText();

        mockMvc.perform(get("/searches/history/all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void deveRejeitarTokenInvalidoEmEndpointProtegido_retorna401() throws Exception {
        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer token-invalido-e-mal-formado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornarBuscaDeVeiculoInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/vehicles/999999"))
                .andExpect(status().isNotFound());
    }

    private String registerAndLogin(String email) throws Exception {
        String registerBody = "{\"nome\":\"Teste\",\"email\":\"" + email + "\",\"senha\":\"123456\"}";
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerBody));

        String loginBody = "{\"email\":\"" + email + "\",\"senha\":\"123456\"}";
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("accessToken").asText();
    }
}
