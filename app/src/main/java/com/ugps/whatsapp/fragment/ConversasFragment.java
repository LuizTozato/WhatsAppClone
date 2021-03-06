package com.ugps.whatsapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.ugps.whatsapp.R;
import com.ugps.whatsapp.activity.ChatActivity;
import com.ugps.whatsapp.adapter.ConversasAdapter;
import com.ugps.whatsapp.config.ConfiguracaoFirebase;
import com.ugps.whatsapp.helper.RecyclerItemClickListener;
import com.ugps.whatsapp.helper.UsuarioFirebase;
import com.ugps.whatsapp.model.Conversa;
import com.ugps.whatsapp.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class ConversasFragment extends Fragment {

    private RecyclerView recyclerViewConversas;
    private List<Conversa> listaConversas = new ArrayList<>();
    private ConversasAdapter adapter;
    private DatabaseReference database;
    private DatabaseReference conversasRef;
    private ChildEventListener childEventListenerConversas;

    public ConversasFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_conversas, container, false);

        recyclerViewConversas = view.findViewById( R.id.recyclerListaConversas );

        //Configurar adapter
        adapter = new ConversasAdapter( listaConversas , getActivity() );

        //Configurar RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( getActivity() );
        recyclerViewConversas.setLayoutManager( layoutManager );
        recyclerViewConversas.setHasFixedSize( true );
        recyclerViewConversas.setAdapter( adapter );

        //Configurar evento de clique
        recyclerViewConversas.addOnItemTouchListener(

                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewConversas,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                List<Conversa> listaConversasAtualizada = adapter.getConversas();
                                Conversa conversaSelecionada = listaConversasAtualizada.get( position );

                                if( conversaSelecionada.getIsGroup().equals("true") ) {

                                    Intent i = new Intent( getActivity() , ChatActivity.class );
                                    i.putExtra("chatGrupo", conversaSelecionada.getGrupo());
                                    startActivity( i );

                                } else {

                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatContato", conversaSelecionada.getUsuarioExibicao());
                                    startActivity(i);

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

        //Configura conversasRef
        String identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        database = ConfiguracaoFirebase.getFirebaseDatabase();
        conversasRef = database.child("conversas")
                               .child( identificadorUsuario );



        return view;

    }

    @Override
    public void onStart() {
        super.onStart();

        recuperarConversas();

    }

    @Override
    public void onStop() {
        super.onStop();

        conversasRef.removeEventListener( childEventListenerConversas );

    }

    public void pesquisarConversas( String texto ){

        //Vamos percorrer a lista de Conversa existente e abastecer uma nova lista que fa??a par com a pesquisa
        List<Conversa> listaConversasBusca = new ArrayList<>();

        for( Conversa conversa : listaConversas ){

            if ( conversa.getUsuarioExibicao() != null ){
                //AQUI ?? UMA CONVERSA CONVENCIONAL

                String nome = conversa.getUsuarioExibicao().getNome().toLowerCase();
                String ultimaMsg = conversa.getUltimaMensagem().toLowerCase();

                if( nome.contains( texto ) || ultimaMsg.contains( texto ) ){
                    listaConversasBusca.add( conversa );
                }

            } else {
                //AQUI ?? UMA CONVERSA DE GRUPO

                String nome = conversa.getGrupo().getNome().toLowerCase();
                String ultimaMsg = conversa.getUltimaMensagem().toLowerCase();

                if( nome.contains( texto ) || ultimaMsg.contains( texto ) ){
                    listaConversasBusca.add( conversa );
                }

            }


            adapter = new ConversasAdapter( listaConversasBusca , getActivity() );
            recyclerViewConversas.setAdapter( adapter );
            adapter.notifyDataSetChanged();

        }

    }

    public void recarregarConversas(){

        adapter = new ConversasAdapter( listaConversas , getActivity() );
        recyclerViewConversas.setAdapter( adapter );
        adapter.notifyDataSetChanged();

    }

    public void recuperarConversas(){

        listaConversas.clear(); //EVITA A DUPLICIDADE DOS DADOS NA LISTA

         childEventListenerConversas = conversasRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                //Recuperar conversas
                Conversa conversa = snapshot.getValue( Conversa.class );
                listaConversas.add( conversa );
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