package pedroMartinsMJ.MyStreaming.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil — Testes de token JWT")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UUID userId;
    private String validToken;

    @BeforeEach
    void setUp() {
        // Usa o secret default para testes (mesmo valor do application.yaml)
        jwtUtil = new JwtUtil("test_secret_key_for_unit_tests_only_12345", 24);
        userId = UUID.randomUUID();
        validToken = jwtUtil.generateToken(userId, "testuser", "VIEWER");
    }

    // ==================== GENERATE TOKEN ====================

    @Nested
    @DisplayName("generateToken()")
    class GenerateTokenTests {

        @Test
        @DisplayName("Deve gerar token não nulo e não vazio")
        void shouldGenerateNonEmptyToken() {
            assertNotNull(validToken);
            assertFalse(validToken.isBlank());
        }

        @Test
        @DisplayName("Deve gerar tokens diferentes para usuários diferentes")
        void shouldGenerateDifferentTokensForDifferentUsers() {
            UUID otherUserId = UUID.randomUUID();
            String otherToken = jwtUtil.generateToken(otherUserId, "otheruser", "ADMIN");

            assertNotEquals(validToken, otherToken);
        }

        @Test
        @DisplayName("Deve gerar token válido para o mesmo usuário")
        void shouldGenerateValidTokenForSameUser() {
            assertTrue(jwtUtil.isTokenValid(validToken));
        }
    }

    // ==================== EXTRACT CLAIMS ====================

    @Nested
    @DisplayName("extractClaims() e extractUserId()/extractUsername()")
    class ExtractClaimsTests {

        @Test
        @DisplayName("Deve extrair claims corretamente do token válido")
        void shouldExtractClaimsFromValidToken() {
            Claims claims = jwtUtil.extractClaims(validToken);

            assertNotNull(claims);
            assertEquals(userId.toString(), claims.getSubject());
            assertEquals("testuser", claims.get("username", String.class));
            assertEquals("VIEWER", claims.get("role", String.class));
        }

        @Test
        @DisplayName("Deve extrair userId do token")
        void shouldExtractUserId() {
            assertEquals(userId.toString(), jwtUtil.extractUserId(validToken));
        }

        @Test
        @DisplayName("Deve extrair username do token")
        void shouldExtractUsername() {
            assertEquals("testuser", jwtUtil.extractUsername(validToken));
        }

        @Test
        @DisplayName("Deve lançar exceção para token inválido")
        void shouldThrowForInvalidToken() {
            assertThrows(Exception.class, () -> jwtUtil.extractClaims("invalid.token.here"));
        }
    }

    // ==================== IS TOKEN VALID ====================

    @Nested
    @DisplayName("isTokenValid()")
    class IsTokenValidTests {

        @Test
        @DisplayName("Deve retornar true para token válido")
        void shouldReturnTrueForValidToken() {
            assertTrue(jwtUtil.isTokenValid(validToken));
        }

        @Test
        @DisplayName("Deve retornar false para token nulo")
        void shouldReturnFalseForNullToken() {
            assertFalse(jwtUtil.isTokenValid(null));
        }

        @Test
        @DisplayName("Deve retornar false para string vazia")
        void shouldReturnFalseForEmptyString() {
            assertFalse(jwtUtil.isTokenValid(""));
        }

        @Test
        @DisplayName("Deve retornar false para token malformado")
        void shouldReturnFalseForMalformedToken() {
            assertFalse(jwtUtil.isTokenValid("not.a.valid.jwt.token"));
        }

        @Test
        @DisplayName("Deve retornar false para token com secret diferente")
        void shouldReturnFalseForDifferentSecret() {
            // Secret deve ter >= 256 bits (32+ caracteres) para HMAC-SHA
            JwtUtil otherJwt = new JwtUtil("a_different_secret_key_that_is_long_enough_for_hmac_sha_256", 24);
            String tokenWithOtherSecret = otherJwt.generateToken(userId, "testuser", "VIEWER");

            assertFalse(jwtUtil.isTokenValid(tokenWithOtherSecret));
        }

        @Test
        @DisplayName("Deve retornar false para token expirado")
        void shouldReturnFalseForExpiredToken() {
            // Cria um JwtUtil com expiração de 0 horas (já expirado)
            JwtUtil expiredJwt = new JwtUtil("test_secret_key_for_unit_tests_only_12345", 0);
            String expiredToken = expiredJwt.generateToken(userId, "testuser", "VIEWER");

            // Dá um pequeno delay para garantir que o tempo passou
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

            assertFalse(expiredJwt.isTokenValid(expiredToken));
        }
    }
}