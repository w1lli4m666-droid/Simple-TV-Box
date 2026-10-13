package win.movie8.tv;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONObject;
import java.util.List;

public final class HistoryActivity extends RemoteActivity {
    private LinearLayout list; private ScrollView scroll; private ImageButton clear;
    public void onCreate(Bundle state){
        super.onCreate(state);LinearLayout root=new LinearLayout(this);root.setPadding(dp(12),dp(12),dp(12),dp(12));root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.rgb(16,16,16));
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);TextView title=new TextView(this);title.setText(getString(R.string.history));title.setTextSize(20);title.setTextColor(Color.WHITE);header.addView(title,new LinearLayout.LayoutParams(0,-2,1));clear=new ImageButton(this);clear.setImageResource(R.drawable.ic_delete);clear.setContentDescription(getString(R.string.clear_history));clear.setBackgroundResource(R.drawable.focus_button);header.addView(clear,new LinearLayout.LayoutParams(dp(52),dp(44)));root.addView(header);
        scroll=new ScrollView(this);list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);clear.setOnClickListener(v->new android.app.AlertDialog.Builder(this).setMessage(getString(R.string.confirm_clear_history)).setNegativeButton(getString(R.string.cancel),null).setPositiveButton(getString(R.string.clear),(d,w)->{HistoryStore.clear(this);render();}).show());render();
    }
    private void render(){list.removeAllViews();List<JSONObject> records=HistoryStore.items(this);if(records.isEmpty()){TextView empty=new TextView(this);empty.setText(getString(R.string.empty_history));empty.setTextColor(Color.GRAY);list.addView(empty);}for(JSONObject item:records)add(item);}
    private void add(JSONObject item){TextView row=new TextView(this);row.setFocusable(true);row.setFocusableInTouchMode(false);row.setBackgroundResource(R.drawable.focus_item);row.setTextColor(Color.WHITE);row.setTextSize(15);row.setPadding(dp(6),dp(15),dp(6),dp(15));row.setText(item.optString("name")+" - "+item.optString("episode"));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(6));list.addView(row,p);row.setOnClickListener(v->open(item));row.setOnLongClickListener(v->{new android.app.AlertDialog.Builder(this).setMessage(getString(R.string.confirm_delete_history)).setNegativeButton(getString(R.string.cancel),null).setPositiveButton(getString(R.string.delete),(d,w)->{HistoryStore.delete(this,item);render();}).show();return true;});}
    @Override public void onBackPressed(){if(scroll.getScrollY()>0){scroll.fullScroll(View.FOCUS_UP);clear.requestFocus();return;}super.onBackPressed();}
    private boolean isListChild(View focused){for(View parent=focused;parent!=null;){if(parent==list)return true;if(!(parent.getParent() instanceof View))return false;parent=(View)parent.getParent();}return false;}
    private void open(JSONObject item){Intent i=new Intent(this,PlayerActivity.class);i.putExtra("id",item.optString("id"));i.putExtra("name",item.optString("name"));i.putExtra("poster",item.optString("poster"));i.putExtra("remarks",item.optString("remarks"));i.putExtra("nid",item.optString("nid"));i.putExtra("position",item.optLong("position"));startActivity(i);}
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
}
