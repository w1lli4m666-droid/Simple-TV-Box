package win.movie8.tv;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class SearchActivity extends RemoteActivity {
    private final ExecutorService searches=Executors.newSingleThreadExecutor(),hints=Executors.newSingleThreadExecutor();
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final SearchRequests resultRequests=new SearchRequests(),hintRequests=new SearchRequests();
    private Future<?> searchTask,hintTask;
    private Runnable pendingHint;
    private MovieAdapter adapter;
    private NavigationBar navigation;
    private EditText input;
    private Button searchButton;
    private DpadGridView grid;
    private LinearLayout suggestions;
    private LinearLayout historyPanel;
    private GridLayout historyCards;
    private ScrollView historyScroll;
    private ImageButton clearHistory;
    private ProgressBar spinner;
    private TextView status;
    private boolean changingQuery;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(10),dp(6),dp(10),0);root.setBackgroundColor(Color.rgb(16,16,16));root.setClipChildren(false);
        navigation=new NavigationBar(this,"search",route->{if("search".equals(route)){input.requestFocus();showKeyboard();}else NavigationBar.open(this,route,!getWindow().getDecorView().isInTouchMode());});
        root.addView(navigation,new LinearLayout.LayoutParams(-1,dp(48)));
        LinearLayout bar=new LinearLayout(this);
        input=new EditText(this);input.setId(View.generateViewId());input.setSingleLine(true);input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);input.setHint(R.string.search);input.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        bar.addView(input,new LinearLayout.LayoutParams(0,dp(48),1));
        searchButton=button(getString(R.string.search));searchButton.setId(View.generateViewId());
        bar.addView(searchButton,new LinearLayout.LayoutParams(dp(100),dp(48)));root.addView(bar);
        navigation.setDownTarget(input.getId());
        input.setNextFocusUpId(navigation.buttons.findViewWithTag("search").getId());
        buildHistory(root);
        suggestions=new LinearLayout(this);suggestions.setOrientation(LinearLayout.VERTICAL);suggestions.setVisibility(View.GONE);
        root.addView(suggestions,new LinearLayout.LayoutParams(-1,-2));
        status=new TextView(this);status.setTextColor(Color.LTGRAY);status.setVisibility(View.GONE);
        root.addView(status,new LinearLayout.LayoutParams(-1,-2));
        FrameLayout results=new FrameLayout(this);
        grid=new DpadGridView(this);grid.setId(View.generateViewId());grid.setPadding(0,dp(5),0,0);
        grid.setLayoutManager(new GridLayoutManager(this,5));grid.setContainHorizontalFocus(true);adapter=new MovieAdapter(this,this::open);grid.setAdapter(adapter);
        grid.setOnTopRowUpListener(()->searchButton.requestFocus());results.addView(grid,new FrameLayout.LayoutParams(-1,-1));
        spinner=new ProgressBar(this);spinner.setIndeterminate(true);spinner.setFocusable(false);
        spinner.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(Color.rgb(246,168,0)));
        spinner.setContentDescription(getString(R.string.searching));spinner.setVisibility(View.GONE);
        results.addView(spinner,new FrameLayout.LayoutParams(dp(48),dp(48),Gravity.CENTER));
        root.addView(results,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
        searchButton.setOnClickListener(view->search());
        input.setOnEditorActionListener((view,action,event)->{if(event==null||event.getAction()==KeyEvent.ACTION_DOWN)search();return true;});
        input.setOnKeyListener((view,key,event)->{
            if(key==KeyEvent.KEYCODE_DPAD_DOWN&&event.getAction()==KeyEvent.ACTION_DOWN){hideKeyboard();if(suggestions.getChildCount()>0&&suggestions.getVisibility()==View.VISIBLE)suggestions.getChildAt(0).requestFocus();else if(historyPanel.getVisibility()==View.VISIBLE&&historyCards.getChildCount()>0)historyCards.getChildAt(0).requestFocus();else focusResults();return true;}return false;
        });
        input.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){}public void afterTextChanged(Editable text){if(!changingQuery)queryChanged();}});
        renderHistory();
        if(getIntent().getBooleanExtra("navigation_focus",false))navigation.post(navigation::focusCurrent);
        else{input.requestFocus();input.post(this::showKeyboard);}
    }

    private void buildHistory(LinearLayout root){
        historyPanel=new LinearLayout(this);historyPanel.setOrientation(LinearLayout.VERTICAL);
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=new TextView(this);title.setText(R.string.search_history);title.setTextColor(Color.LTGRAY);title.setTextSize(14);
        title.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(title,new LinearLayout.LayoutParams(0,dp(40),1));
        clearHistory=new ImageButton(this);clearHistory.setId(View.generateViewId());clearHistory.setImageResource(R.drawable.ic_delete);
        clearHistory.setContentDescription(getString(R.string.clear_search_history));clearHistory.setBackgroundResource(R.drawable.focus_button);
        clearHistory.setNextFocusUpId(input.getId());header.addView(clearHistory,new LinearLayout.LayoutParams(dp(44),dp(40)));
        historyPanel.addView(header);historyScroll=new ScrollView(this);historyCards=new GridLayout(this);
        historyScroll.addView(historyCards);historyPanel.addView(historyScroll,new LinearLayout.LayoutParams(-1,dp(48)));
        root.addView(historyPanel,new LinearLayout.LayoutParams(-1,-2));
        clearHistory.setOnClickListener(view->new android.app.AlertDialog.Builder(this).setMessage(R.string.confirm_clear_search_history).setNegativeButton(R.string.cancel,null).setPositiveButton(R.string.clear,(dialog,which)->{SearchHistory.clear(this);renderHistory();input.requestFocus();}).show());
    }
    private void renderHistory(){
        List<String> terms=SearchHistory.items(this);historyCards.removeAllViews();
        historyPanel.setVisibility(terms.isEmpty()||!input.getText().toString().trim().isEmpty()?View.GONE:View.VISIBLE);
        int columns=Math.max(2,Math.min(5,getResources().getDisplayMetrics().widthPixels/dp(170)));
        historyCards.setColumnCount(columns);
        for(int i=0;i<terms.size();i++){
            final String term=terms.get(i);Button card=button(term);card.setId(View.generateViewId());card.setTextSize(14);
            card.setSingleLine(true);card.setEllipsize(android.text.TextUtils.TruncateAt.END);
            GridLayout.LayoutParams params=new GridLayout.LayoutParams();params.width=0;params.height=dp(44);
            params.rowSpec=GridLayout.spec(i/columns);params.columnSpec=GridLayout.spec(i%columns,GridLayout.FILL,1f);params.setMargins(dp(2),dp(2),dp(2),dp(2));
            historyCards.addView(card,params);
            card.setOnClickListener(view->{changingQuery=true;input.setText(term);input.setSelection(term.length());changingQuery=false;search();});
            card.setOnLongClickListener(view->{new android.app.AlertDialog.Builder(this).setMessage(R.string.confirm_delete_search_history).setNegativeButton(R.string.cancel,null).setPositiveButton(R.string.delete,(dialog,which)->{SearchHistory.delete(this,term);renderHistory();if(historyCards.getChildCount()>0)historyCards.getChildAt(0).requestFocus();else input.requestFocus();}).show();return true;});
            if(i<columns)card.setNextFocusUpId(i==Math.min(columns,terms.size())-1?clearHistory.getId():input.getId());
        }
        for(int i=0;i<terms.size();i++){
            View card=historyCards.getChildAt(i);
            card.setNextFocusLeftId(historyCards.getChildAt(HorizontalGridFocus.target(i,terms.size(),columns,-1)).getId());
            card.setNextFocusRightId(historyCards.getChildAt(HorizontalGridFocus.target(i,terms.size(),columns,1)).getId());
        }
        // Empty columns keep a short history from stretching into full-width rows.
        int slots=((terms.size()+columns-1)/columns)*columns;
        for(int i=terms.size();i<slots;i++){
            View spacer=new View(this);spacer.setFocusable(false);
            GridLayout.LayoutParams params=new GridLayout.LayoutParams();params.width=0;params.height=dp(44);
            params.rowSpec=GridLayout.spec(i/columns);params.columnSpec=GridLayout.spec(i%columns,GridLayout.FILL,1f);params.setMargins(dp(2),dp(2),dp(2),dp(2));
            historyCards.addView(spacer,params);
        }
        if(!terms.isEmpty())clearHistory.setNextFocusDownId(historyCards.getChildAt(Math.min(columns,terms.size())-1).getId());
        historyScroll.getLayoutParams().height=dp(48*Math.min(3,Math.max(1,(terms.size()+columns-1)/columns)));
        historyScroll.requestLayout();
    }

    private void queryChanged(){
        resultRequests.next();if(searchTask!=null)searchTask.cancel(true);
        spinner.setVisibility(View.GONE);status.setVisibility(View.GONE);adapter.replace(java.util.Collections.emptyList());
        clearHints();final String query=input.getText().toString().trim();renderHistory();if(query.isEmpty())return;
        final int request=hintRequests.next();
        pendingHint=()->{
            if(isDestroyed()||isFinishing())return;
            hintTask=hints.submit(()->{
                try{
                    List<Movie> data=WencaiClient.suggest(query);ArrayList<String> names=new ArrayList<>();for(Movie movie:data)names.add(movie.name);
                    List<String> titles=SuggestionTitles.select(names,query,5);
                    runOnUiThread(()->{if(!isDestroyed()&&hintRequests.accepts(request)&&query.equals(input.getText().toString().trim()))showHints(titles);});
                }catch(Exception ignored){ }
            });
        };
        handler.postDelayed(pendingHint,400);
    }
    private void clearHints(){
        hintRequests.next();if(pendingHint!=null)handler.removeCallbacks(pendingHint);if(hintTask!=null)hintTask.cancel(true);
        suggestions.removeAllViews();suggestions.setVisibility(View.GONE);
    }
    private void showHints(List<String> titles){
        suggestions.removeAllViews();suggestions.setVisibility(titles.isEmpty()?View.GONE:View.VISIBLE);
        for(String title:titles){
            Button item=button(title);item.setId(View.generateViewId());item.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
            item.setSingleLine(true);item.setEllipsize(android.text.TextUtils.TruncateAt.END);item.setTextSize(14);
            item.setOnClickListener(view->{changingQuery=true;input.setText(title);input.setSelection(title.length());changingQuery=false;search();});
            suggestions.addView(item,new LinearLayout.LayoutParams(-1,dp(40)));
        }
        for(int i=0;i<suggestions.getChildCount();i++){
            View item=suggestions.getChildAt(i);item.setNextFocusUpId(i==0?input.getId():suggestions.getChildAt(i-1).getId());
            item.setNextFocusDownId(i+1<suggestions.getChildCount()?suggestions.getChildAt(i+1).getId():searchButton.getId());
        }
    }
    private void search(){
        final String query=input.getText().toString().trim();if(query.isEmpty())return;
        SearchHistory.record(this,query);renderHistory();
        clearHints();hideKeyboard();searchButton.requestFocus();
        final int request=resultRequests.next();if(searchTask!=null)searchTask.cancel(true);
        adapter.replace(java.util.Collections.emptyList());status.setVisibility(View.GONE);spinner.setVisibility(View.VISIBLE);
        searchTask=searches.submit(()->{
            try{
                List<Movie> data=WencaiClient.search(query);
                runOnUiThread(()->{
                    if(isDestroyed()||!resultRequests.accepts(request))return;
                    spinner.setVisibility(View.GONE);adapter.replace(data);
                    if(data.isEmpty()){status.setText(R.string.no_search_results);status.setVisibility(View.VISIBLE);}
                    else grid.post(()->{if(resultRequests.accepts(request)&&searchButton.hasFocus()&&hasWindowFocus())focusResults();});
                });
            }catch(Exception error){runOnUiThread(()->{if(isDestroyed()||!resultRequests.accepts(request))return;spinner.setVisibility(View.GONE);status.setText(getString(R.string.load_failed,error.getMessage()));status.setVisibility(View.VISIBLE);});}
        });
    }
    private void focusResults(){androidx.recyclerview.widget.RecyclerView.ViewHolder item=grid.findViewHolderForAdapterPosition(0);if(item!=null)item.itemView.requestFocus();else searchButton.requestFocus();}
    private Button button(String title){Button button=new Button(this);button.setText(title);button.setTextColor(Color.WHITE);button.setAllCaps(false);button.setFocusable(true);button.setFocusableInTouchMode(false);button.setBackgroundResource(R.drawable.focus_button);button.setMinHeight(0);button.setMinimumHeight(0);return button;}
    private void showKeyboard(){if(!isDestroyed()&&input.hasFocus())((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(input,InputMethodManager.SHOW_IMPLICIT);}
    private void hideKeyboard(){((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(input.getWindowToken(),0);}
    @Override public void onBackPressed(){
        boolean remote=!getWindow().getDecorView().isInTouchMode();
        if((remote&&!navigation.hasFocus())||grid.computeVerticalScrollOffset()>dp(5)||historyScroll.getScrollY()>0||suggestions.getVisibility()==View.VISIBLE){
            hideKeyboard();clearHints();
            ((GridLayoutManager)grid.getLayoutManager()).scrollToPositionWithOffset(0,dp(5));
            historyScroll.scrollTo(0,0);navigation.focusCurrent();return;
        }
        super.onBackPressed();
    }
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);resultRequests.next();hintRequests.next();if(searchTask!=null)searchTask.cancel(true);if(hintTask!=null)hintTask.cancel(true);searches.shutdownNow();hints.shutdownNow();super.onDestroy();}
    private void open(Movie movie){Intent intent=new Intent(this,PlayerActivity.class);intent.putExtra("id",movie.id);intent.putExtra("name",movie.name);intent.putExtra("poster",movie.poster);intent.putExtra("remarks",movie.remarks);startActivity(intent);}
    private int dp(int value){return(int)(value*getResources().getDisplayMetrics().density+.5f);}
}
