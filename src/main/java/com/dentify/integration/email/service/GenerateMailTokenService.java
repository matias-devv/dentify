package com.dentify.integration.email;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class GenerateMailTokenService {

    /**
     * Generate a unique token to confirm attendance
     */

        private static final SecureRandom RANDOM = new SecureRandom();

        public String generateConfirmationToken() {
            byte[] randomBytes = new byte[24]; // 192 bits
            RANDOM.nextBytes(randomBytes);

            return Base64.getUrlEncoder()
                         .withoutPadding()
                         .encodeToString(randomBytes);
        }
}
