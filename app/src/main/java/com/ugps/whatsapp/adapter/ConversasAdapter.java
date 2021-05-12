package com.ugps.whatsapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ugps.whatsapp.R;
import com.ugps.whatsapp.model.Conversa;
import com.ugps.whatsapp.model.Usuario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversasAdapter extends RecyclerView.Adapter<ConversasAdapter.MyViewHolder> {

    private List<Conversa> conversas;
    private Context context;

    //Construtor
    public ConversasAdapter(List<Conversa> conversas, Context c) {

        this.conversas = conversas;
        this.context = c;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemLista = LayoutInflater.from( parent.getContext() )
                                       .inflate(R.layout.adapter_contatos , parent , false);

        return new MyViewHolder( itemLista );

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Conversa conversa = conversas.get( position );

        holder.ultimaMensagem.setText( conversa.getUltimaMensagem() ); //setando ultima mensagem

        Usuario usuario = conversa.getUsuarioExibicao();
        holder.nome.setText( usuario.getNome() ); //setando nome

        if( usuario.getFoto() != null ){

            Uri uri = Uri.parse( usuario.getFoto() );
            Glide.with( context ).load( uri ).into( holder.foto ); //"em um contexto, carregue este endereço neste local"

        } else {

            holder.foto.setImageResource( R.drawable.padrao );

        }

    }

    @Override
    public int getItemCount() {

        return conversas.size();

    }

    //Inner Class ===========================
    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView foto;
        TextView nome , ultimaMensagem;

        public MyViewHolder(View itemView) {
            super(itemView);

            foto = itemView.findViewById( R.id.imageViewFotoContato );
            nome = itemView.findViewById( R.id.textNomeContato );
            ultimaMensagem = itemView.findViewById( R.id.textEmailContato );

        }

    }

}
