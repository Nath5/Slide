package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SantitizeField;
import me.ccrama.redditslide.Visuals.Palette;

public class SideListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private static final int TYPE_HEADER_LOGGED_OUT = 0;
    private static final int TYPE_HEADER_LOGGED_IN = 1;
    private static final int TYPE_ITEM = 2;

    private final List<String> subs;
    public List<String> filteredSubs;
    private List<SubWrapper> subWrappers = new ArrayList<>();
    public boolean openInSubView = true;
    private Context context;
    private boolean loggedIn;
    private Integer backHeaderColor;
    private String currentSortText = "";
    private int currentMsgCount = 0;

    private HeaderActionListener headerActionListener;

    public SideListAdapter(Context context, ArrayList<String> subNames, HeaderActionListener headerActionListener) {
        this.subs = subNames;
        for (String subName : subNames) {
            subWrappers.add(new SubWrapper(subName));
        }
        updateCurrent(0);
        //filter = new SubFilter();
        filteredSubs = new ArrayList<>(subNames);
        this.context = context;
        this.headerActionListener = headerActionListener;
        backHeaderColor = Palette.getColor("alsdkfjasld");
    }

    public void setLoggedInState(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public void setCurrentSortText(String newText) {
        this.currentSortText = newText;
        // notifyDataSetChanged();
    }

    public void setBackHeaderColor(int color) {
        this.backHeaderColor = color;
        //notifyDataSetChanged();
    }

    public void setCurrentMsgCount(int count) {
        this.currentMsgCount = count;
    }

    /**
     * Updates the currently selected sub by name
     *
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
        //notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new SubFilter();
    }

    static class SideListItem extends RecyclerView.ViewHolder {
        LinearLayout baseLayout;
        View color;
        View colorActive;
        TextView name;

        public SideListItem(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            colorActive = itemView.findViewById(R.id.color_active);
            color = itemView.findViewById(R.id.color);
            baseLayout = (LinearLayout) itemView.findViewById(R.id.settings_defaultcolor);
        }
    }

    class SideListHeaderLoggedIn extends SideListHeader {
        TextView name;
        LinearLayout multi;
        TextView reorder;
        TextView sync;
        TextView logOut;
        TextView addAccount;
        LinearLayout profClick;
        LinearLayout profileExpandedLayout;
        RelativeLayout inbox;
        LinearLayout mod;
        TextView msgCount;
        TextView profile;

        public SideListHeaderLoggedIn(View itemView) {
            super(itemView);
            multi = (LinearLayout) itemView.findViewById(R.id.multi);
            profClick = (LinearLayout) itemView.findViewById(R.id.prof_click);
            profileExpandedLayout = (LinearLayout) itemView.findViewById(R.id.expand_profile);
            name = (TextView) itemView.findViewById(R.id.name);
            reorder = (TextView) itemView.findViewById(R.id.reorder);
            sync = (TextView) itemView.findViewById(R.id.sync);
            logOut = (TextView) itemView.findViewById(R.id.logout);
            logOut = (TextView) itemView.findViewById(R.id.logout);
            addAccount = (TextView) itemView.findViewById(R.id.add);
            inbox = (RelativeLayout) itemView.findViewById(R.id.inbox);
            mod = (LinearLayout) itemView.findViewById(R.id.mod);
            msgCount = (TextView) itemView.findViewById(R.id.count);
            profile = (TextView) itemView.findViewById(R.id.profile);
        }
    }

    class SideListHeaderLoggedOut extends SideListHeader {
        LinearLayout profile;

        public SideListHeaderLoggedOut(View itemView) {
            super(itemView);
            profile = (LinearLayout) itemView.findViewById(R.id.profile);
        }
    }

    class SideListHeader extends RecyclerView.ViewHolder {
        RelativeLayout back;
        LinearLayout support;
        LinearLayout goToProfile;
        LinearLayout settings;
        EditText sort;

        public SideListHeader(View itemView) {
            super(itemView);
            back = (RelativeLayout) itemView.findViewById(R.id.back);
            support = (LinearLayout) itemView.findViewById(R.id.support);
            goToProfile = (LinearLayout) itemView.findViewById(R.id.go_to_profile_layout);
            settings = (LinearLayout) itemView.findViewById(R.id.settings);
            sort = (EditText) itemView.findViewById(R.id.sort);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.subforsublist, parent, false);
            return new SideListItem(v);
        } else if (viewType == TYPE_HEADER_LOGGED_IN) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_loggedin, parent, false);
            return new SideListHeaderLoggedIn(v);
        } else if (viewType == TYPE_HEADER_LOGGED_OUT) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_loggedout, parent, false);
            return new SideListHeaderLoggedOut(v);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    public interface HeaderActionListener {
        void onReorderClick();

        void onMultiClick();

        void onProfileClick(boolean loggedIn);

        void onLogOutClick();

        void onAddAccountClick();

        void onInboxClick();

        void onModClick();

        void onSupportClick();

        void onGoToProfileClick();

        void onSettingsClick();

        boolean onSortEditorAction(TextView arg0, int arg1, KeyEvent arg2);

        void onSubredditClick();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof SideListHeaderLoggedIn) {
            final SideListHeaderLoggedIn sideListHeader = (SideListHeaderLoggedIn) holder;

            sideListHeader.name.setText(Authentication.name);

            sideListHeader.reorder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (headerActionListener != null) {
                        headerActionListener.onReorderClick();
                    }
                }
            });
            sideListHeader.multi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (headerActionListener != null) {
                        headerActionListener.onMultiClick();
                    }
                }
            });

            sideListHeader.profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (headerActionListener != null) {
                        headerActionListener.onProfileClick(loggedIn);
                    }
                }
            });
            sideListHeader.logOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (headerActionListener != null) {
                        headerActionListener.onLogOutClick();
                    }
                }
            });

            sideListHeader.profClick.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (sideListHeader.profileExpandedLayout.getVisibility() == View.GONE) {
                        sideListHeader.profileExpandedLayout.setVisibility(View.VISIBLE);
                    } else {
                        sideListHeader.profileExpandedLayout.setVisibility(View.GONE);
                    }
                }
            });

            sideListHeader.addAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (headerActionListener != null) {
                        headerActionListener.onAddAccountClick();
                    }
                }
            });

            sideListHeader.inbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (headerActionListener != null) {
                        headerActionListener.onInboxClick();
                    }
                }
            });

            if (Authentication.mod) {
                sideListHeader.mod.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (headerActionListener != null) {
                            headerActionListener.onModClick();
                        }
                    }
                });
            } else {
                sideListHeader.mod.setVisibility(View.GONE);
            }

            sideListHeader.sync.setVisibility(View.GONE);

            sideListHeader.msgCount.setVisibility(currentMsgCount == 0 ? View.GONE : View.VISIBLE);

            if (currentMsgCount > 0) {
                sideListHeader.msgCount.setText(String.valueOf(currentMsgCount));
            }
        } else if (holder instanceof SideListHeaderLoggedOut) {
            SideListHeaderLoggedOut sideListHeader = (SideListHeaderLoggedOut) holder;
            sideListHeader.profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (headerActionListener != null) {
                        headerActionListener.onProfileClick(loggedIn);
                    }
                }
            });

        }

        if (holder instanceof SideListHeader) {
            final SideListHeader sideListHeader = (SideListHeader) holder;

            if (Reddit.hideHeader) {
                sideListHeader.back.setVisibility(View.GONE);
            }
            if (Reddit.tabletUI) {
                sideListHeader.support.setVisibility(View.GONE);
            } else {
                sideListHeader.support.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (headerActionListener != null) {
                            headerActionListener.onSupportClick();
                        }
                    }
                });
            }

            sideListHeader.goToProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (headerActionListener != null) {
                        headerActionListener.onGoToProfileClick();
                    }
                }
            });

            sideListHeader.settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (headerActionListener != null) {
                        headerActionListener.onSettingsClick();
                    }
                }
            });

            sideListHeader.back.setBackgroundColor(backHeaderColor);

            sideListHeader.sort.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    return headerActionListener != null && headerActionListener.onSortEditorAction(v, actionId, event);
                }
            });

            sideListHeader.sort.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String result = sideListHeader.sort.getText().toString().replaceAll(" ", "");
                    getFilter().filter(result);
                    sideListHeader.sort.requestFocus();
                }
            });

            //sideListHeader.sort.setText(currentSortText);
        }

        if (holder instanceof SideListItem) {
            final int realPos = position - 1;
            SideListItem sideListItem = (SideListItem) holder;

            sideListItem.name.setText(filteredSubs.get(realPos));

            final String subreddit = SantitizeField.sanitizeString(filteredSubs.get(realPos).replace(context.getString(R.string.search_goto) + " ", ""));

            sideListItem.color.setBackgroundResource(R.drawable.circle);
            sideListItem.color.getBackground().setColorFilter(Palette.getColor(subreddit), PorterDuff.Mode.MULTIPLY);

            sideListItem.colorActive.setBackgroundResource(R.drawable.circle);
            sideListItem.colorActive.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);
            sideListItem.colorActive.setVisibility(subWrappers.get(realPos).isActive() ? View.VISIBLE : View.INVISIBLE);

            sideListItem.baseLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateCurrent(realPos);
                    if (filteredSubs.get(realPos).startsWith(context.getString(R.string.search_goto) + " ")) {
                        Intent intent = new Intent(context, SubredditView.class);
                        intent.putExtra("subreddit", subreddit);
                        ((Activity) context).startActivityForResult(intent, 4);
                    } else {
                        ((MainActivity) context).pager.setCurrentItem(((MainActivity) context).usedArray.indexOf(filteredSubs.get(realPos)));
                    }

                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    currentSortText = "";
                    if (headerActionListener != null) {
                        headerActionListener.onSubredditClick();
                    }
                }
            });
        }
    }

    //    need to override this method
    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return loggedIn ? TYPE_HEADER_LOGGED_IN : TYPE_HEADER_LOGGED_OUT;
        }
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    //increasing getItemcount to 1. This will be the row of header.
    @Override
    public int getItemCount() {
        return filteredSubs.size() + 1;
    }

    private class SubFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            String prefix = constraint.toString().toLowerCase();
            List<Integer> posRemoved = new ArrayList<>();
            if (prefix.isEmpty()) {
                ArrayList<String> list = new ArrayList<>(subs);
                results.values = list;
                results.count = list.size();
            } else {
                openInSubView = true;
                final ArrayList<String> list = new ArrayList<>(subs);
                final ArrayList<String> nlist = new ArrayList<>();

                for (int i = 0 ; i < list.size(); i++) {
                    String sub = list.get(i);
                    if (sub.contains(prefix)) {
                        nlist.add(sub);
                    }else{
                        posRemoved.add(i);
                    }
                    if (sub.equals(prefix))
                        openInSubView = false;

                }
                if (openInSubView) {
                    nlist.add(context.getString(R.string.search_goto) + " " + prefix);
                }

                results.values = nlist;
                results.count = nlist.size();
            }
            return results;
        }


        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            notifyItemRangeRemoved(1, getItemCount()-1);

            filteredSubs.clear();
            filteredSubs.addAll((List<String>)results.values);
            /*for(Integer index: ((FilterResultObject)results.values).posRemoved){
                notifyItemRemoved(index + 1);
            }*/
            for(int i = 1; i < getItemCount() - 1; i++){
                 notifyItemInserted(i);
            }
            // notifyDataSetChanged();
        }
    }

}