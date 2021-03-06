package com.ugps.whatsapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ugps.whatsapp.R;
import com.ugps.whatsapp.activity.ChatActivity;
import com.ugps.whatsapp.activity.GrupoActivity;
import com.ugps.whatsapp.adapter.ContatosAdapter;
import com.ugps.whatsapp.adapter.ConversasAdapter;
import com.ugps.whatsapp.config.ConfiguracaoFirebase;
import com.ugps.whatsapp.helper.RecyclerItemClickListener;
import com.ugps.whatsapp.helper.UsuarioFirebase;
import com.ugps.whatsapp.model.Conversa;
import com.ugps.whatsapp.model.Usuario;

import java.util.ArrayList;
import java.util.List;


public class ContatosFragment extends Fragment {

    private RecyclerView recyclerViewListaContatos;
    private ContatosAdapter adapter;
    private ArrayList<Usuario> listaContatos = new ArrayList<>();
    private DatabaseReference usuariosRef;
    private ValueEventListener valueEventListenerContatos;
    private FirebaseUser usuarioAtual;

    public ContatosFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contatos, container, false);

        //Configurações iniciais
        recyclerViewListaContatos = view.findViewById(R.id.recyclerViewListaContatos);
        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        //Configurar o adapter
        adapter = new ContatosAdapter( listaContatos, getActivity() );

        //Configurar o recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( getActivity() );
        recyclerViewListaContatos.setLayoutManager( layoutManager );
        recyclerViewListaContatos.setHasFixedSize( true );
        recyclerViewListaContatos.setAdapter( adapter );

        //Configurar evento de clique no recyclerview
        recyclerViewListaContatos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewListaContatos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                List<Usuario> listaUsuariosAtualizada = adapter.getContatos();
                                Usuario usuarioSelecionado = listaContatos.get(position);

                                boolean cabecalho = usuarioSelecionado.getEmail().isEmpty(); //se eu clicar no "novo grupo", vou saber que é ele pois o e-mail está vazio

                                if( cabecalho ){

                                    Intent i = new Intent( getActivity() , GrupoActivity.class );
                                    startActivity( i );

                                } else {

                                    Intent i = new Intent( getActivity(), ChatActivity.class);
                                    i.putExtra("chatContato", usuarioSelecionado);
                                    startActivity( i );

                                }


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

        adicionarMenuNovoGrupo();

        return view;

    }

    @Override
    public void onStart() {
        super.onStart();

        //Anexando o listener
        recuperarContatos();

    }

    @Override
    public void onStop() {
        super.onStop();

        //Desanexar o listener
        usuariosRef.removeEventListener( valueEventListenerContatos );

    }

    public void recuperarContatos(){

        valueEventListenerContatos = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                limparListaContatos();

                for( DataSnapshot dados :  snapshot.getChildren() ){

                    Usuario usuario = dados.getValue( Usuario.class );

                    String emailUsuarioAtual = usuarioAtual.getEmail();
                    if( !emailUsuarioAtual.equals( usuario.getEmail() ) ){

                        listaContatos.add( usuario );

                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    public void pesquisarContatos( String texto ){

        //Vamos percorrer a lista de Conversa existente e abastecer uma nova lista que faça par com a pesquisa
        List<Usuario> listaContatosBusca = new ArrayList<>();

        for( Usuario usuario : listaContatosBusca ){

            String nome = usuario.getNome().toLowerCase();
            if( nome.contains(texto) ){
                listaContatosBusca.add(usuario);
            }
        }
            adapter = new ContatosAdapter( listaContatosBusca , getActivity() );
            recyclerViewListaContatos.setAdapter( adapter );
            adapter.notifyDataSetChanged();
    }

    public void recarregarContatos(){

        adapter = new ContatosAdapter( listaContatos , getActivity() );
        recyclerViewListaContatos.setAdapter( adapter );
        adapter.notifyDataSetChanged();

    }

    public void limparListaContatos(){

        listaContatos.clear();
        adicionarMenuNovoGrupo();

    }

    public void adicionarMenuNovoGrupo(){

        //Criando item para adicionar novo grupo que ficará no topo da lista
        Usuario itemGrupo = new Usuario();
        itemGrupo.setNome("Novo grupo");
        itemGrupo.setEmail("");

        listaContatos.add( itemGrupo );

    }

}