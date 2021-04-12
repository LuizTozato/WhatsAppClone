package com.ugps.whatsapp.helper;

import android.util.Base64;

public class Base64Custom {

    public static String codificarBase64(String texto){
        return Base64.encodeToString(texto.getBytes(), Base64.DEFAULT).replaceAll("(\\n|\\r)","");
        //O replaceAll serve para substituir caracteres inválidos como quebra de linha (\\n) e retorno de carro (\\r)
    }

    public static String decodificarBase64(String textoCodificado){
        return new String(Base64.decode(textoCodificado, Base64.DEFAULT));
    }

}
