package com.ugps.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.ugps.whatsapp.R;
import com.ugps.whatsapp.config.ConfiguracaoFirebase;
import com.ugps.whatsapp.fragment.ContatosFragment;
import com.ugps.whatsapp.fragment.ConversasFragment;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        searchView = findViewById(R.id.materialSearchPrincipal);
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("WhatsApp");
        setSupportActionBar( toolbar );

        //Configurar abas
        final FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                .add("Conversas", ConversasFragment.class) //como é a primeira página, recebe o índice 0
                .add("Contatos", ContatosFragment.class)   // essa página recebe o índice 1
                .create()
        );
        final ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter( adapter ); //os fragmentos ficam dentro do viewpager

        SmartTabLayout viewPagerTab = findViewById(R.id.viewPagerTab);
        viewPagerTab.setViewPager( viewPager );

        //Configuração do search view
        searchView = findViewById( R.id.materialSearchPrincipal );

        //Listener para o search view
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //aqui é para quando o usuário abre o searchView
            }

            @Override
            public void onSearchViewClosed() {

                ConversasFragment fragment = (ConversasFragment) adapter.getPage( 0 );
                fragment.recarregarConversas();

            }
        });

        //Listener para caixa de texto
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //aqui é executado quando ele termina de escrever e clica na lupinha do teclado
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //aqui é executando em tempo de escrita

                //O fragment CONVERSAS é o número 0 e o fragment CONTATOS é o número 1
                switch( viewPager.getCurrentItem() ) {
                    case 0: //CONVERSAS
                        ConversasFragment conversasFragment = (ConversasFragment) adapter.getPage(0);
                        if( newText != null && newText.isEmpty()){
                            conversasFragment.pesquisarConversas( newText.toLowerCase() ); //passa o texto digitado tudo em minúsculas
                        } else {
                            //Aqui é caso o usuário não tenha digitado nada, portanto a caixa de texto está vazia
                            conversasFragment.recarregarConversas();
                        }
                        break;
                    case 1: //CONTATOS
                        ContatosFragment contatosFragment = (ContatosFragment) adapter.getPage(1);
                        if( newText != null && newText.isEmpty()){
                            contatosFragment.pesquisarContatos( newText.toLowerCase() ); //passa o texto digitado tudo em minúsculas
                        } else {
                            //Aqui é caso o usuário não tenha digitado nada, portanto a caixa de texto está vazia
                            contatosFragment.recarregarContatos();
                        }
                        break;
                }

                return true;
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        //Configurar botao de pesquisa
        MenuItem item = menu.findItem( R.id.menuPesquisa ); //peguei a lupinha
        searchView.setMenuItem( item );

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuSair:
                deslogarUsuario();
                finish();
                break;
            case R.id.menuConfiguiracoes:
                abrirConfiguracoes();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void deslogarUsuario(){

        try{
            autenticacao.signOut();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public void abrirConfiguracoes(){
        Intent intent = new Intent(MainActivity.this, ConfiguracoesActivity.class);
        startActivity( intent );
    }

}