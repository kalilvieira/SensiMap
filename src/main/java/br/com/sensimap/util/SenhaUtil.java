package br.com.sensimap.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.*;
import java.util.*;

/** PBKDF2 disponível na JDK; não registra nem devolve a senha original. */
public final class SenhaUtil {
    public static final int ITERACOES=120_000;
    private static final int SALT_BYTES=16;
    private static final int HASH_BITS=256;
    private static final SecureRandom ALEATORIO=new SecureRandom();
    private SenhaUtil(){ }

    public static SenhaProtegida proteger(String senha) {
        validarFormato(senha);
        byte[] salt=new byte[SALT_BYTES];ALEATORIO.nextBytes(salt);
        return new SenhaProtegida(base64(derivar(senha.toCharArray(),salt,ITERACOES)),base64(salt),ITERACOES);
    }

    public static boolean corresponde(String senha,String hashBase64,String saltBase64,int iteracoes) {
        if(senha==null||hashBase64==null||saltBase64==null||iteracoes<100_000)return false;
        try {
            byte[] esperado=Base64.getDecoder().decode(hashBase64),salt=Base64.getDecoder().decode(saltBase64);
            return MessageDigest.isEqual(esperado,derivar(senha.toCharArray(),salt,iteracoes));
        }catch(IllegalArgumentException e){return false;}
    }

    public static void validarFormato(String senha) {
        if(senha==null||senha.length()<8||senha.length()>80)throw new IllegalArgumentException("A senha deve ter entre 8 e 80 caracteres.");
    }

    private static byte[] derivar(char[] senha,byte[] salt,int iteracoes) {
        PBEKeySpec spec=new PBEKeySpec(senha,salt,iteracoes,HASH_BITS);
        try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}
        catch(GeneralSecurityException e){throw new IllegalStateException("PBKDF2 indisponível.",e);}
        finally{spec.clearPassword();Arrays.fill(senha,'\0');}
    }
    private static String base64(byte[] valor){return Base64.getEncoder().encodeToString(valor);}
    public record SenhaProtegida(String hash,String salt,int iteracoes){ }
}
