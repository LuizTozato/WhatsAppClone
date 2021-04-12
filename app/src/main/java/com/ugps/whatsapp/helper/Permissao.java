package com.ugps.whatsapp.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissao {

    public static boolean validarPermissoes(String[] permissoes, Activity activity, int requestCode){

        //só fazemos verificação se for maior que o Android Marshmellow (23)
        if(Build.VERSION.SDK_INT >= 23){

            List<String> listaPermissões = new ArrayList<>();

            for( String permissao : permissoes ){
                Boolean temPermissao = ContextCompat.checkSelfPermission(activity, permissao) == PackageManager.PERMISSION_GRANTED;
                if( !temPermissao ) listaPermissões.add(permissao);
            }

            //Caso a lista esteja vazia, não é necessário solicitar permissao, retorna true e interrompe o processo
            if( listaPermissões.isEmpty() ) return true;
            String[] novasPermissoes = new String[ listaPermissões.size() ];
            listaPermissões.toArray( novasPermissoes );

            //Solicita permissao
            ActivityCompat.requestPermissions( activity, novasPermissoes, requestCode );

        }

        return true;
    }


}
