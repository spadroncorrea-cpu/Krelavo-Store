package krelavo.store.seguridad;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Genera y verifica hashes PBKDF2 con sal aleatoria y parámetros versionados. */
public final class HashContrasenas {
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final String PREFIJO_HASH = "pbkdf2_sha256";
    private static final int ITERACIONES = 600000;
    private static final int LONGITUD_SAL = 16;
    private static final int LONGITUD_HASH_BITS = 256;
    private final SecureRandom generadorAleatorio = new SecureRandom();

    public String generarHash(char[] contrasena) {
        byte[] sal = new byte[LONGITUD_SAL];
        generadorAleatorio.nextBytes(sal);
        byte[] hash = derivarHash(contrasena, sal, ITERACIONES);
        return PREFIJO_HASH + "$" + ITERACIONES + "$"
                + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public boolean verificar(char[] contrasena, String hashAlmacenado) {
        if (contrasena == null || hashAlmacenado == null) {
            return false;
        }
        try {
            String[] partes = hashAlmacenado.split("\\$", -1);
            if (partes.length != 4 || !PREFIJO_HASH.equals(partes[0])) {
                return false;
            }
            int iteracionesHash = Integer.parseInt(partes[1]);
            if (iteracionesHash < 100000 || iteracionesHash > 2000000) {
                return false;
            }
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] hashEsperado = Base64.getDecoder().decode(partes[3]);
            if (sal.length < 16 || hashEsperado.length != LONGITUD_HASH_BITS / 8) {
                return false;
            }
            byte[] hashCalculado = derivarHash(contrasena, sal, iteracionesHash);
            return MessageDigest.isEqual(hashEsperado, hashCalculado);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private byte[] derivarHash(char[] contrasena, byte[] sal, int iteraciones) {
        PBEKeySpec especificacion = new PBEKeySpec(
                contrasena, sal, iteraciones, LONGITUD_HASH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO)
                    .generateSecret(especificacion).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo proteger la contraseña.", ex);
        } finally {
            especificacion.clearPassword();
        }
    }

    public static void limpiar(char[] contrasena) {
        if (contrasena != null) {
            Arrays.fill(contrasena, '\0');
        }
    }
}
