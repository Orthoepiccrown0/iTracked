package com.epiccrown.map.minimap.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.epiccrown.map.minimap.MapsActivity;
import com.epiccrown.map.minimap.R;
import com.epiccrown.map.minimap.UserInfo;
import com.epiccrown.map.minimap.helpers.RESTfulHelper;
import com.epiccrown.map.minimap.helpers.UsefulStaticMethods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Home extends Fragment {

    private boolean started = false;
    private UserInfoAdapter mAdapter;
    private RecyclerView mRecycler;
    private SearchView searchView;
    private ProgressBar progressBar;
    private RelativeLayout searchHint;
    private RelativeLayout searchFailed;
    private RelativeLayout noConnection;
    private List<UserInfo> users = new ArrayList<>();
    private String usersQuery = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(getResources().getString(R.string.app_name));
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        mRecycler = v.findViewById(R.id.users_list_recycler);
        searchView = v.findViewById(R.id.simpleSearchView);
        progressBar = v.findViewById(R.id.home_progressbar);
        searchHint = v.findViewById(R.id.home_search_hint);
        searchFailed = v.findViewById(R.id.home_search_failed);
        noConnection = v.findViewById(R.id.home_search_no_connection);
        setUpSearch();
        searchHint.setVisibility(View.VISIBLE);
        return v;
    }

    private void setUpSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.trim().length() > 0) {
                    searchHint.setVisibility(View.GONE);
                    searchFailed.setVisibility(View.GONE);
                    usersQuery = newText;
                    if(UsefulStaticMethods.isNetworkAvailable(getActivity()))
                        new SearchTrackers().execute();
                    else
                        noConnection.setVisibility(View.VISIBLE);
                }else{
                    searchFailed.setVisibility(View.GONE);
                    noConnection.setVisibility(View.GONE);
                    searchHint.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    users.clear();
                    setAdapter();
                }
                return false;
            }
        });
    }


    private void setAdapter() {
        mAdapter = new UserInfoAdapter(getActivity());
        mAdapter.setItems(users);
        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycler.setAdapter(mAdapter);
    }

    private void setFakeUsers(int fakeUsers) {
        for (int i = 0; i < fakeUsers; i++) {
            UserInfo userInfo = new UserInfo();
            userInfo.setFamily("None");
            userInfo.setLastupdate("12:00");
            userInfo.setLatitude("Test");
            userInfo.setLongitude("Test");
            userInfo.setUsername("Username");

            users.add(userInfo);
        }
    }

    //Adapter for user info

    class UserInfoAdapter extends RecyclerView.Adapter<UserInfoAdapter.ItemHolder> {
        private List<UserInfo> items;
        private Context mContext;

        public UserInfoAdapter(Context mContext) {
            this.mContext = mContext;
        }

        public void setItems(List<UserInfo> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public UserInfoAdapter.ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View v = inflater.inflate(R.layout.user_info_item, parent, false);

            return new UserInfoAdapter.ItemHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull UserInfoAdapter.ItemHolder holder, int position) {
            UserInfo user = items.get(position);
            holder.bindItem(user);

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ItemHolder extends RecyclerView.ViewHolder {
            TextView username;
            TextView lat;
            TextView longt;
            TextView last_update;
            CardView card;
            ConstraintLayout button;

            ItemHolder(View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.user_username);
                lat = itemView.findViewById(R.id.user_latitude);
                longt = itemView.findViewById(R.id.user_longitude);
                last_update = itemView.findViewById(R.id.user_last_update);
                card = itemView.findViewById(R.id.card);
                button = itemView.findViewById(R.id.item_locate);
            }

            void bindItem(final UserInfo user) {
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


    class SearchTrackers extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            RESTfulHelper helper = new RESTfulHelper();
            return helper.search(usersQuery, usersQuery);
        }

        @Override
        protected void onPostExecute(String s) {
            users.clear();
            resetAllHints();
            if (s.trim().equals("Empty")) {
                searchFailed.setVisibility(View.VISIBLE);
            }else {
                try {
                    JSONArray array = new JSONArray(s);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonObject = array.getJSONObject(i);
                        UserInfo userInfo = new UserInfo();
                        userInfo.setFamily(jsonObject.getString("family"));
                        userInfo.setLastupdate(jsonObject.getString("lastupdate"));
                        userInfo.setLatitude(jsonObject.getString("latitude"));
                        userInfo.setLongitude(jsonObject.getString("longitude"));
                        userInfo.setUsername(jsonObject.getString("username"));
                        users.add(userInfo);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            progressBar.setVisibility(View.GONE);
            if(users.size()!=0)
            setAdapter();

        }

        private void resetAllHints() {
            searchHint.setVisibility(View.GONE);
            searchFailed.setVisibility(View.GONE);
            noConnection.setVisibility(View.GONE);
        }

    }
}
