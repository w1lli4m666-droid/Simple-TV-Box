package win.movie8.tv;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends RemoteActivity {
    private final ExecutorService network=Executors.newCachedThreadPool(); private MovieAdapter adapter; private LinearLayout tabs; private DpadGridView grid; private ProgressBar loadingSpinner; private String selected="recent"; private int page,requestGeneration; private boolean loading,hasMore=true; private long lastExitBack;
    private NavigationBar navigation;
    private boolean navigationOpening;
    private boolean startupPreview;
    public void onCreate(Bundle state){super.onCreate(state);build();String section=getIntent().getStringExtra("section");select(section==null?"recent":section);if(getIntent().getBooleanExtra("navigation_focus",false))navigation.post(navigation::focusCurrent);}
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);String section=intent.getStringExtra("section");if(section!=null)select(section);navigation.post(navigation::focusCurrent);}
    @Override protected void onResume(){super.onResume();if(navigationOpening){navigationOpening=false;navigation.post(navigation::focusCurrent);}}
    private void navigate(String route){if("search".equals(route)||"history".equals(route)){if(navigationOpening)return;navigationOpening=true;NavigationBar.open(this,route,!getWindow().getDecorView().isInTouchMode());}else select(route);}
    private void showStartupPreview(String section){List<Movie> cached=ListPreviewCache.read(section);startupPreview=!cached.isEmpty();if(startupPreview)adapter.replace(cached);}
    private void build(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.rgb(16,16,16));root.setClipChildren(false);
        navigation=new NavigationBar(this,"recent",this::navigate);tabs=navigation.buttons;root.addView(navigation,new LinearLayout.LayoutParams(-1,dp(48)));
        grid=new DpadGridView(this);grid.setPadding(0,dp(5),0,0);grid.setLayoutManager(new GridLayoutManager(this,5));adapter=new MovieAdapter(this,this::open);grid.setAdapter(adapter);grid.setOnNearEndListener(this::loadMore);grid.setOnTopRowUpListener(()->{View tab=tabs.findViewWithTag(selected);if(tab!=null)tab.requestFocus();});grid.addOnScrollListener(new RecyclerView.OnScrollListener(){@Override public void onScrolled(RecyclerView view,int dx,int dy){GridLayoutManager manager=(GridLayoutManager)grid.getLayoutManager();if(manager.findLastVisibleItemPosition()>=adapter.getItemCount()-10)loadMore();}});
        FrameLayout results=new FrameLayout(this);results.addView(grid,new FrameLayout.LayoutParams(-1,-1));loadingSpinner=new ProgressBar(this);loadingSpinner.setIndeterminate(true);loadingSpinner.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(Color.rgb(246,168,0)));loadingSpinner.setContentDescription(getString(R.string.loading));loadingSpinner.setFocusable(false);loadingSpinner.setVisibility(View.GONE);results.addView(loadingSpinner,new FrameLayout.LayoutParams(dp(48),dp(48),Gravity.CENTER));root.addView(results,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
    }
    private void select(String section){navigation.setCurrent(section);startupPreview=false;requestGeneration++;selected=section;page=0;loading=false;hasMore=true;adapter.replace(java.util.Collections.emptyList());for(int i=0;i<tabs.getChildCount();i++){View v=tabs.getChildAt(i);if(v instanceof Button)v.setSelected(section.equals(v.getTag()));}showStartupPreview(section);loadMore();}
    private void loadMore(){
        if(loading||!hasMore)return;
        loading=true;
        final int nextPage=++page,generation=requestGeneration;
        final String section=selected;
        final boolean replacingPreview=startupPreview&&nextPage==1;
        if(nextPage==1&&!replacingPreview)loadingSpinner.setVisibility(View.VISIBLE);
        network.execute(()->{
            try{
                List<Movie> data=WencaiClient.load(section,nextPage);
                runOnUiThread(()->{
                    if(isDestroyed()||generation!=requestGeneration||!section.equals(selected))return;
                    loading=false;loadingSpinner.setVisibility(View.GONE);
                    if(replacingPreview){
                        // Keep the user's position and focus while fresh data replaces the preview.
                        View focused=getCurrentFocus();
                        RecyclerView.ViewHolder holder=focused==null?null:grid.findContainingViewHolder(focused);
                        String focusedId=holder==null?null:adapter.idAt(holder.getAdapterPosition());
                        android.os.Parcelable layout=grid.getLayoutManager().onSaveInstanceState();
                        if(!data.isEmpty())adapter.replace(data);
                        grid.getLayoutManager().onRestoreInstanceState(layout);
                        if(focusedId!=null)grid.post(()->{
                            if(isDestroyed()||generation!=requestGeneration)return;
                            int position=adapter.positionOf(focusedId);
                            RecyclerView.ViewHolder restored=grid.findViewHolderForAdapterPosition(position);
                            if(restored!=null)restored.itemView.requestFocus();
                        });
                        startupPreview=false;
                    }else adapter.append(data);
                    hasMore=data.size()>=25;
                    if(nextPage==1&&!replacingPreview&&hasWindowFocus()&&!navigationOpening&&grid.getChildCount()>0)grid.getChildAt(0).requestFocus();
                });
            }catch(Exception error){
                runOnUiThread(()->{
                    if(isDestroyed()||generation!=requestGeneration||!section.equals(selected))return;
                    if(replacingPreview){startupPreview=false;hasMore=false;}else page--;
                    loading=false;loadingSpinner.setVisibility(View.GONE);
                    if(nextPage==1)Toast.makeText(getApplicationContext(),getString(R.string.retry_category),Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    private void open(Movie m){Intent i=new Intent(this,PlayerActivity.class);i.putExtra("id",m.id);i.putExtra("name",m.name);i.putExtra("poster",m.poster);i.putExtra("remarks",m.remarks);startActivity(i);}
    @Override public void onBackPressed(){if(grid.computeVerticalScrollOffset()>dp(5)){returnToActiveTab();return;}if(!getWindow().getDecorView().isInTouchMode()&&!isTopNavigationFocused()){returnToActiveTab();return;}if(System.currentTimeMillis()-lastExitBack<1800){super.onBackPressed();return;}lastExitBack=System.currentTimeMillis();Toast.makeText(getApplicationContext(),getString(R.string.press_back_exit),Toast.LENGTH_SHORT).show();}
    private void returnToActiveTab(){((GridLayoutManager)grid.getLayoutManager()).scrollToPositionWithOffset(0,dp(5));View tab=tabs.findViewWithTag(selected);if(tab!=null)tab.requestFocus();}
    private boolean isTopNavigationFocused(){View focused=getCurrentFocus();for(View current=focused;current!=null;){if(current==tabs)return true;if(!(current.getParent() instanceof View))return false;current=(View)current.getParent();}return false;}
    @Override protected void onDestroy(){network.shutdownNow();super.onDestroy();}
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
}
