package com.ugps.whatsapp.model;

import com.google.firebase.database.DatabaseReference;
import com.ugps.whatsapp.config.ConfiguracaoFirebase;
import com.ugps.whatsapp.helper.Base64Custom;

import java.io.Serializable;
import java.util.List;

public class Grupo implements Serializable {
    //Serializable pois vamos transitar essa classe entre activities

    private String id;
    private String nome;
    private String foto;
    private List<Usuario> membros;

    //CONSTRUTOR
    public Grupo() {

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference grupoRef = database.child("grupos");

        String idGrupoFirebase = grupoRef.push().getKey();
        setId( idGrupoFirebase );

    }

    //GETTERS AND SETTERS
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public List<Usuario> getMembros() {
        return membros;
    }

    public void setMembros(List<Usuario> membros) {
        this.membros = membros;
    }

    public void salvar(){

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference grupoRef = database.child("grupos");
        grupoRef.child( getId() ).setValue( this ); //Salvando o grupo e suas variáveis

        //Aqui vou percorrer cada usuário do grupo e criar uma conversa para ele
        for( Usuario membro: getMembros() ) {

            String idRemetente = Base64Custom.codificarBase64( membro.getEmail() ); //o membro sobre quem estou trabalhando nesse looping
            String idDestinatario = getId(); //todo o grupo

            Conversa conversa = new Conversa();
            conversa.setIdRemetente( idRemetente );
            conversa.setIdDestinatario( idDestinatario );
            conversa.setUltimaMensagem( "" );
            conversa.setIsGroup( "true" );
            conversa.setGrupo( this );

            conversa.salvar();

        }

    }
}
