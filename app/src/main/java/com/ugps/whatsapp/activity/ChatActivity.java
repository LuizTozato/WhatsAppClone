package com.ugps.whatsapp.activity;

import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.ugps.whatsapp.R;
import com.ugps.whatsapp.adapter.MensagensAdapter;
import com.ugps.whatsapp.config.ConfiguracaoFirebase;
import com.ugps.whatsapp.helper.Base64Custom;
import com.ugps.whatsapp.helper.UsuarioFirebase;
import com.ugps.whatsapp.model.Mensagem;
import com.ugps.whatsapp.model.Usuario;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView textViewNome;
    private CircleImageView circleImageViewFoto;
    private EditText editMensagem;
    private Usuario usuarioDestinatario;
    private DatabaseReference database;
    private DatabaseReference mensagensRef;
    private ChildEventListener childEventListenerMensagens;

    //identificador usuarios remetente e destinatatio
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;

    private RecyclerView recyclerMensagens;
    private MensagensAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Configuração toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configuracoes iniciais
        textViewNome = findViewById(R.id.textViewNomeChat);
        circleImageViewFoto = findViewById(R.id.circleImageFotoChat);
        editMensagem = findViewById(R.id.editMensagem);
        recyclerMensagens = findViewById(R.id.recyclerMensagens);

        //Recuperar dados do usuário remetente
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();

        //Recuperar dados do usuário destinatario
        Bundle bundle = getIntent().getExtras();
        if( bundle != null ){

            usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
            textViewNome.setText( usuarioDestinatario.getNome() );

            String foto = usuarioDestinatario.getFoto();
            if ( foto != null ){

                Uri url = Uri.parse(usuarioDestinatario.getFoto());
                Glide.with( ChatActivity.this )
                        .load( url )
                        .into( circleImageViewFoto );

            } else {
                circleImageViewFoto.setImageResource(R.drawable.padrao);
            }

            //recuperar dados do usuario destinatário
            idUsuarioDestinatario = Base64Custom.codificarBase64( usuarioDestinatario.getEmail() );

        }

        //Configuração adapter
        adapter = new MensagensAdapter( mensagens, getApplicationContext() );

        //Configuração recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( getApplicationContext() );
        recyclerMensagens.setLayoutManager( layoutManager );
        recyclerMensagens.setHasFixedSize( true );
        recyclerMensagens.setAdapter( adapter );

        //Configurando referências ao Firebase
        database = ConfiguracaoFirebase.getFirebaseDatabase();
        mensagensRef = database.child("mensagens")
                .child( idUsuarioRemetente )
                .child( idUsuarioDestinatario );

    }

    public void enviarMensagem(View view){

        String textoMensagem = editMensagem.getText().toString();

        if( !textoMensagem.isEmpty() ){

            Mensagem mensagem = new Mensagem();
            mensagem.setIdUsuario( idUsuarioRemetente );
            mensagem.setMensagem( textoMensagem );

            //salvar a mensagem para o remetente
            salvarMensagem( idUsuarioRemetente , idUsuarioDestinatario , mensagem );

            //salvar a mensagem para o destinatário
            salvarMensagem( idUsuarioDestinatario , idUsuarioRemetente , mensagem );

        } else {
            Toast.makeText(this, "Erro, texto vazio!", Toast.LENGTH_SHORT).show();
        }

    }

    private void salvarMensagem(String idRemetente, String idDestinatario, Mensagem msg){

        database = ConfiguracaoFirebase.getFirebaseDatabase();
        mensagensRef = database.child("mensagens");

        mensagensRef.child( idRemetente )
                    .child( idDestinatario )
                    .push() //isso cria um identificador único para o firebase, evitando que as mensagens sejam sobrescritas
                    .setValue( msg );

        //Limpar texto
        editMensagem.setText("");

    }

    @Override
    protected void onStart() {
        super.onStart();

        //Anexando o listener
        recuperarMensagens();

    }

    @Override
    protected void onStop() {
        super.onStop();

        //Removendo o listener
        mensagensRef.removeEventListener( childEventListenerMensagens );

    }

    private void recuperarMensagens(){

        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                //cairá aqui quando uma mensagem for enviada, recebida ou excluída.
                Mensagem mensagem = snapshot.getValue( Mensagem.class );
                mensagens.add( mensagem );
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

}