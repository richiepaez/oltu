/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.labs.amber.signature.signers.hmac;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.labs.amber.signature.signers.AbstractMethodAlgorithm;
import org.apache.labs.amber.signature.signers.SignatureException;
import org.apache.labs.amber.signature.signers.SignatureMethod;

/**
 * HMAC-SHA1 Method implementation.
 *
 * @version $Id$
 */
@SignatureMethod("HMAC-SHA1")
public final class HmacSha1MethodAlgorithm extends AbstractMethodAlgorithm<HmacSha1Key, HmacSha1Key> {

    /**
     * The algorithm name.
     */
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String encode(HmacSha1Key signingKey,
            String secretCredential,
            String baseString) throws SignatureException {
        String key = new StringBuilder(percentEncode(signingKey.getValue()))
                .append('&')
                .append(percentEncode(secretCredential))
                .toString();

        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);

        Mac mac = null;
        try {
            mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureException("HMAC-SHA1 Algorithm not supported", e);
        }

        try {
            mac.init(secretKeySpec);
        } catch (InvalidKeyException e) {
            throw new SignatureException(new StringBuilder("Signing key '")
                    .append(key)
                    .append("' caused HMAC-SHA1 error")
                    .toString(), e);
        }

        byte[] rawHmac = mac.doFinal(baseString.getBytes());

        return base64Encode(rawHmac);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean verify(String signature,
            HmacSha1Key verifyingKey,
            String secretCredential,
            String baseString) throws SignatureException {
        String expectedSignature = this.encode(verifyingKey, secretCredential, baseString);

        if (this.getLog().isDebugEnabled()) {
            this.getLog().debug(new StringBuilder("Received signature {")
                    .append(signature)
                    .append("} expected signature {")
                    .append(expectedSignature)
                    .append('}')
                    .toString());
        }

        return expectedSignature.equals(signature);
    }

}