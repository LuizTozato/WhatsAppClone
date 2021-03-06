package com.ugps.whatsapp.activity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ugps.whatsapp.R;
import com.ugps.whatsapp.adapter.ContatosAdapter;
import com.ugps.whatsapp.adapter.GrupoSelecionadoAdapter;
import com.ugps.whatsapp.config.ConfiguracaoFirebase;
import com.ugps.whatsapp.helper.RecyclerItemClickListener;
import com.ugps.whatsapp.helper.UsuarioFirebase;
import com.ugps.whatsapp.model.Usuario;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GrupoActivity extends AppCompatActivity {

    private RecyclerView recyclerMembrosSelecionados, recyclerMembros;
    private ContatosAdapter contatosAdapter;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private List<Usuario> listaMembros = new ArrayList<>();
    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private ValueEventListener valueEventListenerMembros;
    private DatabaseReference usuariosRef;
    private FirebaseUser usuarioAtual;
    private Toolbar toolbar;
    private FloatingActionButton fabAvancarCadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_grupo);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo grupo");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        //Configuracoes iniciais
        recyclerMembros = findViewById( R.id.recyclerMembros );
        recyclerMembrosSelecionados = findViewById( R.id.recyclerMembrosSelecionados );
        fabAvancarCadastro = findViewById( R.id.fabSalvarGrupo);

        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        //Configurar adapter
        contatosAdapter = new ContatosAdapter( listaMembros , getApplicationContext() );

        //Configurar recyclerview para os contatos (Recycler view de baixo que fica na vertical)
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( getApplicationContext() );
        recyclerMembros.setLayoutManager( layoutManager );
        recyclerMembros.setHasFixedSize( true );
        recyclerMembros.setAdapter( contatosAdapter );

        recyclerMembros.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerMembros,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                Usuario usuarioSelecionado = listaMembros.get( position ); //peguei o usuario selecionado

                                //Remover usuario selecionado da lista
                                listaMembros.remove( usuarioSelecionado );
                                contatosAdapter.notifyDataSetChanged(); //aqui o usuario ter?? sumido da lista

                                //adicionar usuario na nova lista de selecinados
                                listaMembrosSelecionados.add( usuarioSelecionado );
                                grupoSelecionadoAdapter.notifyDataSetChanged();

                                //Atualizar o escrito na toolbar
                                atualizarMembrosToolbar();

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );
        //==================
        //Configurar recyclerView para os membros selecionados (Recycler view de cima que fica na horizontal)
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter( listaMembrosSelecionados , getApplicationContext() );

        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        recyclerMembrosSelecionados.setLayoutManager( layoutManagerHorizontal );
        recyclerMembrosSelecionados.setHasFixedSize( true );
        recyclerMembrosSelecionados.setAdapter( grupoSelecionadoAdapter );

        recyclerMembrosSelecionados.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerMembrosSelecionados,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                Usuario usuarioSelecionado = listaMembrosSelecionados.get( position );

                                //Remover da listagem de membros selecionados
                                listaMembrosSelecionados.remove( usuarioSelecionado );
                                grupoSelecionadoAdapter.notifyDataSetChanged();

                                //Adicionar ?? listagem de membros
                                listaMembros.add( usuarioSelecionado );
                                contatosAdapter.notifyDataSetChanged();

                                //Atualizar o escrito na toolbar
                                atualizarMembrosToolbar();

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

        //Configurar floating action button
        fabAvancarCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent( GrupoActivity.this, CadastroGrupoActivity.class );
                i.putExtra( "membros", (Serializable) listaMembrosSelecionados );

                startActivity( i );
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        recuperarContatos();
    }

    @Override
    protected void onStop() {
        super.onStop();
        usuariosRef.removeEventListener( valueEventListenerMembros );
    }

    public void recuperarContatos(){

        valueEventListenerMembros = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for( DataSnapshot dados :  snapshot.getChildren() ){

                    Usuario usuario = dados.getValue( Usuario.class );

                    String emailUsuarioAtual = usuarioAtual.getEmail();
                    if( !emailUsuarioAtual.equals( usuario.getEmail() ) ){

                        listaMembros.add( usuario );

                    }
                }

                contatosAdapter.notifyDataSetChanged();
                atualizarMembrosToolbar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    public void atualizarMembrosToolbar(){

        int totalSelecionados = listaMembrosSelecionados.size();
        int total = listaMembros.size() + listaMembrosSelecionados.size();

        toolbar.setSubtitle( totalSelecionados + " de " + total + " selecionados");

    }
}