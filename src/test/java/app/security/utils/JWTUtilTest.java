package app.security.utils;

import app.exceptions.TokenVerificationException;
import app.security.dtos.UserSecurityDTO;
import app.security.enums.Role;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JWTUtilTest
{
    // validate() is not tested — a missing JWT_SECRET causes class load failure before any test runs,
    // making it impossible to test in isolation without the environment variable already being present.

    private static final Long USER_ID = 1L;
    private static final String USERNAME = "john";
    private static final Set<Role> ROLES = Set.of(Role.USER);

    private String token;

    @BeforeEach
    void setUp()
    {
        token = JWTUtil.createToken(USER_ID, USERNAME, ROLES);
    }

    // EXPIRY is private with no public setter, so buildExpiredToken() constructs a pre-expired token
    // directly via Nimbus, signed with the same secret, to test parseToken()'s expiry check.
    private String buildExpiredToken() throws Exception
    {
        String secret = System.getenv("JWT_SECRET");
        String issuer = System.getenv("JWT_ISSUER");

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(USERNAME)
                .issuer(issuer)
                .issueTime(new Date(System.currentTimeMillis() - 2000))
                .claim("id", USER_ID)
                .claim("roles", List.of(Role.USER.name()))
                .expirationTime(new Date(System.currentTimeMillis() - 1000))
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner(secret));
        return jwt.serialize();
    }

    @Nested
    @DisplayName("createToken")
    class CreateToken
    {
        @Test
        @DisplayName("Should return a non-blank token string")
        void returnsNonBlankToken()
        {
            assertNotNull(token);
            assertFalse(token.isBlank());
        }

        @Test
        @DisplayName("Should return a token with three JWT segments")
        void hasThreeSegments()
        {
            assertEquals(3, token.split("\\.").length);
        }
    }

    @Nested
    @DisplayName("parseToken")
    class ParseToken
    {
        @Test
        @DisplayName("Should recover correct id from token")
        void recoversId()
        {
            UserSecurityDTO dto = JWTUtil.parseToken(token);

            assertEquals(USER_ID, dto.id());
        }

        @Test
        @DisplayName("Should recover correct username from token")
        void recoversUsername()
        {
            UserSecurityDTO dto = JWTUtil.parseToken(token);

            assertEquals(USERNAME, dto.username());
        }

        @Test
        @DisplayName("Should recover correct roles from token")
        void recoversRoles()
        {
            UserSecurityDTO dto = JWTUtil.parseToken(token);

            assertEquals(ROLES, dto.roles());
        }

        @Test
        @DisplayName("Should throw TokenVerificationException on malformed token")
        void throwsOnMalformedToken()
        {
            assertThrows(TokenVerificationException.class, () -> JWTUtil.parseToken("not.a.token"));
        }

        @Test
        @DisplayName("Should throw TokenVerificationException on tampered payload")
        void throwsOnTamperedPayload()
        {
            String[] parts = token.split("\\.");
            String tampered = parts[0] + ".tamperedpayload" + "." + parts[2];
            assertThrows(TokenVerificationException.class, () -> JWTUtil.parseToken(tampered));
        }

        @Test
        @DisplayName("Should throw TokenVerificationException on tampered signature")
        void throwsOnTamperedSignature()
        {
            String[] parts = token.split("\\.");
            String tampered = parts[0] + "." + parts[1] + ".invalidsignature";

            assertThrows(TokenVerificationException.class, () -> JWTUtil.parseToken(tampered));
        }

        @Test
        @DisplayName("Should throw TokenVerificationException on expired token")
        void throwsOnExpiredToken() throws Exception
        {
            String expiredToken = buildExpiredToken();

            assertThrows(TokenVerificationException.class, () -> JWTUtil.parseToken(expiredToken));
        }
    }
}