package win.movie8.tv;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public final class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.Holder> {
    public interface Listener { void onMovie(Movie movie); }
    private final List<Movie> items = new ArrayList<>(); private final Listener listener; private final Context context;
    public MovieAdapter(Context context, Listener listener) { this.context=context; this.listener=listener; }
    public void replace(List<Movie> values) { items.clear(); items.addAll(values); notifyDataSetChanged(); }
    public void append(List<Movie> values) { int start=items.size(); for(Movie value:values){boolean exists=false;for(Movie current:items)if(current.id.equals(value.id)){exists=true;break;}if(!exists)items.add(value);}if(items.size()>start)notifyItemRangeInserted(start,items.size()-start); }
    public int getItemCount() { return items.size(); }
    String idAt(int position) { return position>=0&&position<items.size()?items.get(position).id:null; }
    int positionOf(String id) { for(int i=0;i<items.size();i++)if(items.get(i).id.equals(id))return i;return -1; }
    public Holder onCreateViewHolder(ViewGroup parent, int type) {
        int gap=dp(2);int width=(context.getResources().getDisplayMetrics().widthPixels-dp(24))/5;int posterHeight=Math.max(dp(120),(int)((width-gap*2)*1.22f));FrameLayout root=new FrameLayout(context);root.setPadding(gap,gap,gap,gap);root.setFocusable(true);root.setFocusableInTouchMode(false);root.setBackgroundResource(R.drawable.focus_item);root.setOnFocusChangeListener((v,focused)->v.animate().scaleX(focused?1.055f:1f).scaleY(focused?1.055f:1f).setDuration(120).start());
        ImageView poster=new ImageView(context);poster.setScaleType(ImageView.ScaleType.CENTER_CROP);poster.setBackgroundResource(R.drawable.poster_round);poster.setClipToOutline(true);root.addView(poster,new FrameLayout.LayoutParams(-1,posterHeight));
        LinearLayout chips=new LinearLayout(context);chips.setOrientation(LinearLayout.VERTICAL);chips.setGravity(Gravity.LEFT);FrameLayout.LayoutParams chipParams=new FrameLayout.LayoutParams(-2,-2,Gravity.LEFT|Gravity.BOTTOM);chipParams.setMargins(dp(4),0,dp(4),dp(30));root.addView(chips,chipParams);
        TextView quality=chip(11,Color.rgb(42,145,77));chips.addView(quality);TextView genre=chip(11,Color.rgb(75,75,75));chips.addView(genre);TextView locale=chip(11,Color.rgb(75,75,75));chips.addView(locale);
        TextView title=label(13);title.setMaxLines(1);title.setGravity(Gravity.CENTER_VERTICAL);title.setPadding(dp(5),0,dp(5),0);title.setBackgroundColor(0xbb000000);root.addView(title,new FrameLayout.LayoutParams(-1,dp(28),Gravity.BOTTOM));
        return new Holder(root,poster,quality,genre,locale,title);
    }
    public void onBindViewHolder(Holder h,int pos){final Movie m=items.get(pos);h.title.setText(m.name);setChip(h.quality,m.remarks);setChip(h.genre,m.type);String place=join(m.year,m.area);setChip(h.locale,place);ImageLoader.load(h.poster,m.poster);h.itemView.setOnClickListener(v->listener.onMovie(m));}
    private void setChip(TextView view,String value){view.setText(value);view.setVisibility(value==null||value.isEmpty()?View.GONE:View.VISIBLE);}
    private String join(String year,String area){if(area==null||area.isEmpty())return year==null?"":year;if(year==null||year.isEmpty())return area;return area+"/"+year;}
    public static final class Holder extends RecyclerView.ViewHolder { final ImageView poster;final TextView quality,genre,locale,title;Holder(View v,ImageView p,TextView q,TextView g,TextView l,TextView t){super(v);poster=p;quality=q;genre=g;locale=l;title=t;} }
    private TextView chip(int size,int color){TextView v=label(size);v.setTextColor(Color.WHITE);android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(color);bg.setCornerRadius(dp(3));v.setBackground(bg);v.setPadding(dp(5),dp(2),dp(5),dp(2));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);p.setMargins(0,0,0,dp(2));v.setLayoutParams(p);return v;}
    private TextView label(int size){TextView v=new TextView(context);v.setTextColor(Color.WHITE);v.setTextSize(size);return v;}
    private int dp(int n){return(int)(n*context.getResources().getDisplayMetrics().density+.5f);}
}
