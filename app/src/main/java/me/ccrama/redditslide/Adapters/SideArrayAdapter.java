package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SantitizeField;
import me.ccrama.redditslide.Visuals.Palette;

public class SideArrayAdapter extends ArrayAdapter<String> {
    private final List<String> subs;
    public ArrayList<String> filteredSubs;
    private List<SubWrapper> subWrappers = new ArrayList<>();
    private Filter filter;
    public boolean openInSubView = true;
    private Context context;

    public SideArrayAdapter(Context context, ArrayList<String> subNames) {
        super(context, 0, subNames);
        this.subs = subNames;
        for (String subName : subNames) {
            subWrappers.add(new SubWrapper(subName));
        }
        updateCurrent(0);
        filter = new SubFilter();
        filteredSubs = new ArrayList<>(subNames);
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final SideArrayViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.subforsublist, parent, false);
            viewHolder = new SideArrayViewHolder();
            viewHolder.baseLayout = (LinearLayout) convertView.findViewById(R.id.settings_defaultcolor);
            viewHolder.name = ((TextView) convertView.findViewById(R.id.name));
            viewHolder.color = convertView.findViewById(R.id.color);
            viewHolder.colorActive = convertView.findViewById(R.id.color_active);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SideArrayViewHolder) convertView.getTag();
        }

        viewHolder.name.setText(filteredSubs.get(position));

        final String subreddit = SantitizeField.sanitizeString(filteredSubs.get(position).replace(getContext().getString(R.string.search_goto) + " ", ""));

        viewHolder.color.setBackgroundResource(R.drawable.circle);
        viewHolder.color.getBackground().setColorFilter(Palette.getColor(subreddit), PorterDuff.Mode.MULTIPLY);

        viewHolder.colorActive.setBackgroundResource(R.drawable.circle);
        viewHolder.colorActive.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);
        viewHolder.colorActive.setVisibility(subWrappers.get(position).isActive() ? View.VISIBLE : View.INVISIBLE);

        viewHolder.baseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCurrent(position);
                if (filteredSubs.get(position).startsWith(getContext().getString(R.string.search_goto) + " ")) {
                    Intent intent = new Intent(context, SubredditView.class);
                    intent.putExtra("subreddit", subreddit);
                    ((Activity) getContext()).startActivityForResult(intent, 4);
                } else {
                    ((MainActivity) getContext()).pager.setCurrentItem(((MainActivity) getContext()).usedArray.indexOf(filteredSubs.get(position)));
                }

                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
               // ((MainActivity) context).sortEditText.setText("");
                ((MainActivity) context).drawerLayout.closeDrawers();
            }
        });
        return convertView;
    }

    /**
     * Updates the currently selected sub by name
     * @param subName - name to mark as active
     */
    public void updateCurrent(String subName) {
        updateCurrent(subs.indexOf(subName));
    }

    public void updateCurrent(int pos) {
        for (SubWrapper subWrapper : subWrappers) {
            subWrapper.setActive(false);
        }
        subWrappers.get(pos).setActive(true);
        notifyDataSetChanged();
    }

    static class SideArrayViewHolder {
        LinearLayout baseLayout;
        View color;
        View colorActive;
        TextView name;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new SubFilter();
        }
        return filter;
    }

    private class SubFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            String prefix = constraint.toString().toLowerCase();
            if (prefix.isEmpty()) {
                ArrayList<String> list = new ArrayList<>(subs);
                results.values = list;
                results.count = list.size();
            } else {
                openInSubView = true;
                final ArrayList<String> list = new ArrayList<>(subs);
                final ArrayList<String> nlist = new ArrayList<>();

                for (String sub : list) {
                    if (sub.contains(prefix))
                        nlist.add(sub);
                    if (sub.equals(prefix))
                        openInSubView = false;
                }
                if (openInSubView) {
                    nlist.add(getContext().getString(R.string.search_goto) + " " + prefix);
                }

                results.values = nlist;
                results.count = nlist.size();
            }
            return results;
        }


        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredSubs = (ArrayList<String>) results.values;
            notifyDataSetChanged();
            clear();
            if (filteredSubs != null) {
                int count = filteredSubs.size();
                for (int i = 0; i < count; i++) {
                    add(filteredSubs.get(i));
                    notifyDataSetInvalidated();
                }
            }
        }
    }
}