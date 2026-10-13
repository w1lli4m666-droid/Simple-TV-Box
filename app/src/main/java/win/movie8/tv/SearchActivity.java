package win.movie8.tv;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SearchActivity extends RemoteActivity {
    private final ExecutorService network=Executors.newSingleThreadExecutor(); private MovieAdapter adapter; private EditText input; private Button searchButton; private DpadGridView grid;
    public void onCreate(Bundle state){
        super.onCreate(state);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(10),dp(6),dp(10),0);root.setBackgroundColor(Color.rgb(16,16,16));root.setClipChildren(false);
        LinearLayout bar=new LinearLayout(this);input=new EditText(this);input.setSingleLine(true);input.setTextColor(Color.WHITE);input.setHintTextColor(Color.GRAY);input.setHint(getString(R.string.search));bar.addView(input,new LinearLayout.LayoutParams(0,dp(48),1));searchButton=new Button(this);searchButton.setFocusable(true);searchButton.setBackgroundResource(R.drawable.focus_button);searchButton.setText(getString(R.string.search));bar.addView(searchButton,new LinearLayout.LayoutParams(dp(78),dp(48)));root.addView(bar);
        grid=new DpadGridView(this);grid.setPadding(0,dp(5),0,0);grid.setLayoutManager(new GridLayoutManager(this,5));adapter=new MovieAdapter(this,this::open);grid.setAdapter(adapter);root.addView(grid,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
        searchButton.setOnClickListener(v->search());input.setOnEditorActionListener((v,a,e)->{search();return true;});input.requestFocus();((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(input,InputMethodManager.SHOW_IMPLICIT);
    }
    private void search(){final String q=input.getText().toString().trim();if(q.isEmpty())return;network.execute(()->{try{List<Movie> data=WencaiClient.search(q);runOnUiThread(()->adapter.replace(data));}catch(Exception ignored){}});}
    @Override public void onBackPressed(){if(grid.computeVerticalScrollOffset()>dp(5)){((GridLayoutManager)grid.getLayoutManager()).scrollToPositionWithOffset(0,dp(5));searchButton.requestFocus();return;}super.onBackPressed();}
    private boolean isGridChild(View focused){for(View parent=focused;parent!=null;){if(parent==grid)return true;if(!(parent.getParent() instanceof View))return false;parent=(View)parent.getParent();}return false;}
    private void open(Movie m){Intent i=new Intent(this,PlayerActivity.class);i.putExtra("id",m.id);i.putExtra("name",m.name);i.putExtra("poster",m.poster);i.putExtra("remarks",m.remarks);startActivity(i);}
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
}
