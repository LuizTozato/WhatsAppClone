package com.ugps.whatsapp.model;

import com.google.firebase.database.DatabaseReference;
import com.ugps.whatsapp.config.ConfiguracaoFirebase;

public class Conversa {

    private String idRemetente;
    private String idDestinatario;
    private String ultimaMensagem;
    private Usuario usuarioExibicao;


    public Conversa() {
    }

    public String getIdRemetente() {
        return idRemetente;
    }

    public void setIdRemetente(String idRemetente) {
        this.idRemetente = idRemetente;
    }

    public String getIdDestinatario() {
        return idDestinatario;
    }

    public void setIdDestinatario(String idDestinatario) {
        this.idDestinatario = idDestinatario;
    }

    public String getUltimaMensagem() {
        return ultimaMensagem;
    }

    public void setUltimaMensagem(String ultimaMensagem) {
        this.ultimaMensagem = ultimaMensagem;
    }

    public Usuario getUsuarioExibicao() {
        return usuarioExibicao;
    }

    public void setUsuarioExibicao(Usuario usuarioExibicao) {
        this.usuarioExibicao = usuarioExibicao;
    }

    public void salvar(){

        DatabaseReference databaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference conversaRef = databaseRef.child("conversas");

        conversaRef.child( this.getIdRemetente() ) //"pasta"
                   .child( this.getIdDestinatario() ) //"pasta"
                   .setValue( this ); //aqui estou salvando esse objeto inteiro

    }

}
