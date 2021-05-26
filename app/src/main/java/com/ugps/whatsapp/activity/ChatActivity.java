package com.ugps.whatsapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ugps.whatsapp.R;
import com.ugps.whatsapp.adapter.MensagensAdapter;
import com.ugps.whatsapp.config.ConfiguracaoFirebase;
import com.ugps.whatsapp.helper.Base64Custom;
import com.ugps.whatsapp.helper.UsuarioFirebase;
import com.ugps.whatsapp.model.Conversa;
import com.ugps.whatsapp.model.Grupo;
import com.ugps.whatsapp.model.Mensagem;
import com.ugps.whatsapp.model.Usuario;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView textViewNome;
    private CircleImageView circleImageViewFoto;
    private EditText editMensagem;
    private ImageView imageCamera;
    private Usuario usuarioDestinatario;
    private Usuario usuarioRemetente;
    private DatabaseReference database;
    private StorageReference storage;
    private DatabaseReference mensagensRef;
    private ChildEventListener childEventListenerMensagens;

    //identificador usuarios remetente e destinatatio
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;
    private Grupo grupo;

    private RecyclerView recyclerMensagens;
    private MensagensAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();
    private static final int SELECAO_CAMERA = 100;

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
        imageCamera = findViewById(R.id.imageCamera);

        //Recuperar dados do usuário remetente
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();
        usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();

        //Recuperar dados do usuário destinatario
        Bundle bundle = getIntent().getExtras();
        if( bundle != null ){

            if( bundle.containsKey("chatGrupo") ){

                //recebendo dados de conversa de grupo
                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                idUsuarioDestinatario = grupo.getId();
                textViewNome.setText( grupo.getNome() );

                String foto = grupo.getFoto();
                if ( foto != null ){

                    Uri url = Uri.parse( foto );
                    Glide.with( ChatActivity.this )
                            .load( url )
                            .into( circleImageViewFoto );

                } else {
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }

            } else {

                //recebendo dados de uma conversa convencional
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
        storage = ConfiguracaoFirebase.getFirebaseStorage();
        mensagensRef = database.child("mensagens")
                .child( idUsuarioRemetente )
                .child( idUsuarioDestinatario );

        //Evento de clique na camera
        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if( intent.resolveActivity( getPackageManager() ) != null ){
                    startActivityForResult(intent, SELECAO_CAMERA);
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( resultCode == RESULT_OK ) {

            Bitmap imagem = null;

            try {
                switch (requestCode) {

                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;
                }

                if ( imagem != null ){

                    //Recuperar dados da imgem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //Criar o nome da imagem
                    String nomeImagem = UUID.randomUUID().toString();

                    //Configurar as referências ao firebase
                    final StorageReference imagemRef = storage.child("imagens")
                            .child("fotos")
                            .child( idUsuarioRemetente )
                            .child( nomeImagem );

                    UploadTask uploadTask = imagemRef.putBytes( dadosImagem );
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(ChatActivity.this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {

                                    //recuperando a url da imagem
                                    String downloadUrl = task.getResult().toString();

                                    //Configurando enviar imagem em grupos
                                    if( usuarioDestinatario != null ){
                                        //MENSAGEM NORMAL

                                        Mensagem mensagem = new Mensagem();
                                        mensagem.setIdUsuario( idUsuarioRemetente );
                                        mensagem.setMensagem("imagem.jpeg");
                                        mensagem.setImagem( downloadUrl );

                                        //Salvar mensagem para o remetente
                                        salvarMensagem( idUsuarioRemetente , idUsuarioDestinatario , mensagem );

                                        //Salvar mensagem para o destinatario
                                        salvarMensagem( idUsuarioDestinatario , idUsuarioRemetente , mensagem );


                                    } else {
                                        //MENSAGEM EM GRUPO

                                        for ( Usuario membro: grupo.getMembros() ){
                                            //PARA CADA MEMBRO DO GRUPO, VOU CRIAR UMA CONVERSA

                                            String idRemetenteGrupo = Base64Custom.codificarBase64( membro.getEmail() );
                                            String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                                            Mensagem mensagem = new Mensagem();
                                            mensagem.setIdUsuario( idUsuarioLogadoGrupo );
                                            mensagem.setMensagem( "imagem.jpg" );
                                            mensagem.setNome( usuarioRemetente.getNome() );
                                            mensagem.setImagem( downloadUrl );

                                            //salvar mensagem para o membro
                                            salvarMensagem( idRemetenteGrupo, idUsuarioDestinatario, mensagem );

                                            //salvar conversa para o grupo
                                            salvarConversa( idRemetenteGrupo, idUsuarioDestinatario, usuarioDestinatario, mensagem , true );

                                        }


                                    }

                                    Toast.makeText(ChatActivity.this, "Sucesso ao enviar imagem!", Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                    });

                }

            } catch ( Exception e ){

                e.printStackTrace();

            }
        }
    }

    public void enviarMensagem(View view){

        String textoMensagem = editMensagem.getText().toString();

        if( !textoMensagem.isEmpty() ){

            if ( usuarioDestinatario != null ){
            //AQUI É O ENVIO DE UMA MENSAGEM CONVENCIONAL
                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario( idUsuarioRemetente );
                mensagem.setMensagem( textoMensagem );

                //salvar a mensagem para o remetente
                salvarMensagem( idUsuarioRemetente , idUsuarioDestinatario , mensagem );

                //salvar a mensagem para o destinatário
                salvarMensagem( idUsuarioDestinatario , idUsuarioRemetente , mensagem );

                //salvar conversa para o remetente
                salvarConversa( idUsuarioRemetente, idUsuarioDestinatario, usuarioDestinatario ,mensagem , false );

                //salvar conversa para um único destinatário
                Usuario usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();
                salvarConversa( idUsuarioDestinatario, idUsuarioRemetente, usuarioRemetente, mensagem , false );

            } else {
            //AQUI É O ENVIO DE UMA MENSAGEM DE GRUPO

                for ( Usuario membro: grupo.getMembros() ){
                    //PARA CADA MEMBRO DO GRUPO, VOU CRIAR UMA CONVERSA

                    String idRemetenteGrupo = Base64Custom.codificarBase64( membro.getEmail() );
                    String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario( idUsuarioLogadoGrupo );
                    mensagem.setMensagem( textoMensagem );
                    mensagem.setNome( usuarioRemetente.getNome() );

                    //salvar mensagem para o membro
                    salvarMensagem( idRemetenteGrupo, idUsuarioDestinatario, mensagem );

                    //salvar conversa para o grupo
                    salvarConversa( idRemetenteGrupo, idUsuarioDestinatario, usuarioDestinatario, mensagem , true );

                }
            }

        } else {
            Toast.makeText(this, "Erro, texto vazio!", Toast.LENGTH_SHORT).show();
        }

    }

    private void salvarConversa(String idRemetente, String idDestinatario, Usuario usuarioExibicao, Mensagem msg , boolean isGroup ){

        //Salvar conversa remetente
        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente( idUsuarioRemetente );
        conversaRemetente.setIdDestinatario( idUsuarioDestinatario );
        conversaRemetente.setUltimaMensagem( msg.getMensagem() );

        if ( isGroup ) { //CONVERSA DE GRUPO

            //Conversa de grupo
            conversaRemetente.setIsGroup("true");
            conversaRemetente.setGrupo( grupo );

        } else { //CONVERSA NORMAL

            //Conversa convencional
            conversaRemetente.setUsuarioExibicao( usuarioExibicao ); //sempre com quem estou conversando
            conversaRemetente.setIsGroup( "false" ); //não precisaria pois já é default do construtor

        }

        conversaRemetente.salvar();

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

        mensagens.clear(); //isso evita a duplicidade dos valores caso o usuário volte pelo "voltar" do android e não do app

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