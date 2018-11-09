package com.epiccrown.map.minimap.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.epiccrown.map.minimap.MapsActivity;
import com.epiccrown.map.minimap.R;
import com.epiccrown.map.minimap.UserInfo;
import com.epiccrown.map.minimap.databaseStuff.DatabaseDataGetter;
import com.epiccrown.map.minimap.databaseStuff.DatabaseOpenHelper;
import com.epiccrown.map.minimap.helpers.RESTfulHelper;
import com.epiccrown.map.minimap.helpers.UsefulStaticMethods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Favorites extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout error_message;
    private RelativeLayout hint_message;
    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton floatingActionButton;
    private ArrayList<UserInfo> users = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        assignVariables(view);
        assignJob();
        adjustRefresh();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.favs_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.favs_delete_favs){
            DatabaseOpenHelper openHelper = new DatabaseOpenHelper(getContext());
            DatabaseDataGetter dataGetter = new DatabaseDataGetter(openHelper);
            dataGetter.deleteFavs();
            new SearchFavsJSON().execute();
        }
        return super.onOptionsItemSelected(item);
    }

    private void adjustRefresh() {
        if (users.size() > 0)
            swipeRefreshLayout.setEnabled(true);
        else
            swipeRefreshLayout.setEnabled(false);
    }

    private void assignJob() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new SearchFavsJSON().execute();
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        new SearchFavsJSON().execute();
        swipeRefreshLayout.setRefreshing(true);

        floatingActionButton.hide();
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.smoothScrollToPosition(0);
                floatingActionButton.hide();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    floatingActionButton.show();
                } else {
                    int visibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                    if (visibleItem < 4)
                        floatingActionButton.hide();
                }
            }
        });
    }

    private void assignVariables(View view) {
        recyclerView = view.findViewById(R.id.fav_recycler);
        swipeRefreshLayout = view.findViewById(R.id.fav_swipe_refresh);
        error_message = view.findViewById(R.id.fav_hint_error_tab);
        hint_message = view.findViewById(R.id.fav_hint_tab);
        coordinatorLayout = view.findViewById(R.id.fav_coordinator);
        floatingActionButton = view.findViewById(R.id.fav_fb);
        linearLayoutManager = new LinearLayoutManager(getContext());
    }

    private void setUpAdapter() {
        if (recyclerAdapter == null)
            recyclerAdapter = new RecyclerAdapter(getContext());
        recyclerAdapter.setUsersList(users);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemHolder> {

        private Context context;
        private ArrayList<UserInfo> usersList;

        public RecyclerAdapter(Context context, ArrayList<UserInfo> usersList) {
            this.context = context;
            this.usersList = usersList;
        }

        public RecyclerAdapter(Context context) {
            this.context = context;
        }

        public void setUsersList(ArrayList<UserInfo> usersList) {
            this.usersList = usersList;
        }

        @NonNull
        @Override
        public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View v = layoutInflater.inflate(R.layout.home_user_info_item, parent, false);
            return new ItemHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
            UserInfo user = usersList.get(position);
            holder.bindItem(user, position);
        }

        @Override
        public int getItemCount() {
            return usersList.size();
        }

        class ItemHolder extends RecyclerView.ViewHolder {
            TextView username;
            TextView lat;
            TextView longt;
            TextView last_update;
            TextView user_has_no_position;
            CardView card;
            ConstraintLayout button;
            ImageView fav;

            ItemHolder(View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.user_username);
                lat = itemView.findViewById(R.id.user_latitude);
                longt = itemView.findViewById(R.id.user_longitude);
                last_update = itemView.findViewById(R.id.user_last_update);
                card = itemView.findViewById(R.id.card);
                button = itemView.findViewById(R.id.item_locate);
                user_has_no_position = itemView.findViewById(R.id.user_has_no_position);
                fav = itemView.findViewById(R.id.fav_button);
            }

            void bindItem(final UserInfo user, final int id) {
                if (user.getLatitude().equals("has_no_position")) {
                    button.setBackground(getResources().getDrawable(R.drawable.home_disabled_button));
                    user_has_no_position.setVisibility(View.VISIBLE);
                    lat.setVisibility(View.GONE);
                    longt.setVisibility(View.GONE);
                    last_update.setVisibility(View.GONE);
                    fav.setVisibility(View.GONE);
                    username.setText(user.getUsername());
                } else {
                    button.setBackground(getResources().getDrawable(R.drawable.home_onclick_button));
                    user_has_no_position.setVisibility(View.GONE);
                    lat.setVisibility(View.VISIBLE);
                    longt.setVisibility(View.VISIBLE);
                    fav.setVisibility(View.VISIBLE);
                    last_update.setVisibility(View.VISIBLE);

                    String date = UsefulStaticMethods.getDate(Long.parseLong(user.getLastupdate()), "dd/MM/yyyy HH:mm");
                    username.setText(user.getUsername());
                    lat.setText("Latitude: " + user.getLatitude());
                    longt.setText("Longitude: " + user.getLongitude());
                    last_update.setText(date);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getMap(user);
                        }
                    });

                    setFavResource(user);

                    fav.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DatabaseOpenHelper openHelper = new DatabaseOpenHelper(getContext());
                            DatabaseDataGetter dataGetter = new DatabaseDataGetter(openHelper, getContext());
                            if (user.isFav()) {
                                dataGetter.deleteFav(user.getUsername());
                                user.setFav(false);
                            }
                            //setFavResource(user);
                            usersList.remove(id);
                            recyclerAdapter.notifyItemRemoved(id);
                            recyclerAdapter.notifyItemRangeChanged(id, usersList.size());
                            setSnackbar(user, id, dataGetter);

                            if (usersList.isEmpty()) {
                                hint_message.setVisibility(View.VISIBLE);
                                error_message.setVisibility(View.GONE);
                            }
                        }
                    });
                }


            }

            private void setSnackbar(final UserInfo user, final int pos, final DatabaseDataGetter dataGetter) {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.favorites_snackbar_deleted_message, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.favorites_snackbar_deleted_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dataGetter.insertFav(user.getUsername());
                        user.setFav(true);
                        usersList.add(pos, user);
                        recyclerAdapter.notifyItemInserted(pos);
                        recyclerAdapter.notifyItemRangeChanged(pos, usersList.size());
                        setFavResource(user);
                        if(!usersList.isEmpty()){
                            error_message.setVisibility(View.GONE);
                            hint_message.setVisibility(View.GONE);
                        }
                    }
                });
                snackbar.show();
            }

            private void setFavResource(UserInfo user) {
                if (user.isFav())
                    fav.setImageResource(R.drawable.ic_star_black_24dp);
                else
                    fav.setImageResource(R.drawable.ic_star_border_black_24dp);
            }

            private void getMap(UserInfo user) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("target", user);
                intent.putExtra("target_bundle", bundle);
                try {
                    getActivity().startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private class SearchFavsJSON extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            DatabaseOpenHelper openHelper = new DatabaseOpenHelper(getContext());
            DatabaseDataGetter dataGetter = new DatabaseDataGetter(openHelper);
            ArrayList<String> users = dataGetter.getFavs();
            if (users.size() > 0) {
                JSONArray jsonArray = new JSONArray();
                String jsonString = "";
                for (String user : users) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("username", user);
                        jsonArray.put(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                jsonString = jsonArray.toString();
                if (!jsonString.equals(""))
                    return new RESTfulHelper().searchByJSONList(jsonString);
                else return "error";
            } else
                return "no favs";
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null) {
                swipeRefreshLayout.setRefreshing(false);
                users.clear();
                if (s.trim().equals("Empty")) {
                    error_message.setVisibility(View.VISIBLE);
                    hint_message.setVisibility(View.GONE);
                } else if (s.equals("no favs")) {
                    error_message.setVisibility(View.GONE);
                    hint_message.setVisibility(View.VISIBLE);
                } else {
                    try {
                        JSONArray array = new JSONArray(s);
                        DatabaseOpenHelper openHelper = new DatabaseOpenHelper(getContext());
                        DatabaseDataGetter dataGetter = new DatabaseDataGetter(openHelper, getContext());
                        ArrayList<String> favs = dataGetter.getFavs();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            UserInfo userInfo = new UserInfo();
                            userInfo.setFamily(jsonObject.getString("family"));
                            userInfo.setLastupdate(jsonObject.getString("lastupdate"));
                            userInfo.setLatitude(jsonObject.getString("latitude"));
                            userInfo.setLongitude(jsonObject.getString("longitude"));
                            userInfo.setUsername(jsonObject.getString("username"));
                            for (String fav : favs)
                                if (fav.equals(userInfo.getUsername()))
                                    userInfo.setFav(true);
                            users.add(userInfo);
                        }
                        error_message.setVisibility(View.GONE);
                        hint_message.setVisibility(View.GONE);

                    } catch (JSONException e) {
                        error_message.setVisibility(View.VISIBLE);
                        hint_message.setVisibility(View.GONE);
                        e.printStackTrace();
                    }

                }

                if (!UsefulStaticMethods.isNetworkAvailable(getContext()))
                    Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                setUpAdapter();
                adjustRefresh();

            }
        }
    }
}
