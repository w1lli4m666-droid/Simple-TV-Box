package win.movie8.tv;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.Holder> {
    public interface Listener { void onEpisode(Episode episode); }
    private final Context context; private final Listener listener; private final ArrayList<Episode> items=new ArrayList<>();
    public EpisodeAdapter(Context context,Listener listener){this.context=context;this.listener=listener;}
    public void replace(List<Episode> episodes,boolean descending){items.clear();items.addAll(episodes);if(descending)Collections.reverse(items);notifyDataSetChanged();}
    public int getItemCount(){return items.size();}
    public Holder onCreateViewHolder(ViewGroup parent,int viewType){Button button=new Button(context);button.setFocusable(true);button.setFocusableInTouchMode(false);button.setAllCaps(false);button.setTextColor(Color.WHITE);button.setTextSize(14);button.setBackgroundResource(R.drawable.focus_button);button.setPadding(dp(4),0,dp(4),0);RecyclerView.LayoutParams p=new RecyclerView.LayoutParams(-1,dp(40));p.setMargins(dp(3),dp(2),dp(3),dp(2));button.setLayoutParams(p);return new Holder(button);}
    public void onBindViewHolder(Holder holder,int position){Episode episode=items.get(position);holder.button.setText(episode.name);holder.button.setOnClickListener(v->listener.onEpisode(episode));}
    static final class Holder extends RecyclerView.ViewHolder{final Button button;Holder(Button button){super(button);this.button=button;}}
    private int dp(int n){return(int)(n*context.getResources().getDisplayMetrics().density+.5f);}
}
