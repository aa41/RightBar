package com.xiaoma.mzrightbar.rightbar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.xiaoma.mzrightbar.R;
import com.xiaoma.mzrightbar.rightbar.model.AppDao;
import com.xiaoma.mzrightbar.rightbar.model.AppInfo;
import com.xiaoma.mzrightbar.rightbar.model.FavoriteModel;
import com.xiaoma.mzrightbar.rightbar.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * author: mxc
 * date: 2018/4/26.
 */

public class FavoriteActivity extends AppCompatActivity {
    private List<FavoriteModel> models = new ArrayList<>();
    private Realm mRealm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        mRealm = Realm.getDefaultInstance();
        RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(FavoriteActivity.this));
        List<AppInfo> packageList = Utils.getPackageList(FavoriteActivity.this);
        RealmResults<AppDao> all = mRealm.where(AppDao.class).findAll();
        OUT:
        for (AppInfo info : packageList) {
            FavoriteModel model = new FavoriteModel();
            model.setAppName(info.getAppName());
            model.setIcon(info.getIcon());
            model.setPackageName(info.getPackageName());
            model.setFirstLetter(info.getFirstLetter());
            IN:
            for (int i = 0; i < all.size(); i++) {
                String packageName = all.get(i).getPackageName();
                if (packageName.equals(model.getPackageName())) {
                    model.setChecked(true);
                    break IN;
                }
            }
            models.add(model);
        }
        rv.setAdapter(new A(models, mRealm));


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    static class A extends RecyclerView.Adapter<A.VH> {

        public List<FavoriteModel> mData = new ArrayList<>();
        private Realm mRealm;

        public A(List<FavoriteModel> mData, Realm mRealm) {
            this.mData = mData;
            this.mRealm = mRealm;
        }


        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite, parent, false));
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            final FavoriteModel model = mData.get(position);
            holder.check.setChecked(model.isChecked());
            holder.tv.setText(model.getAppName());
            holder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                AppDao dao = mRealm.createObject(AppDao.class);
                                dao.setAppName(model.getAppName());
                                dao.setPackageName(model.getPackageName());
                                dao.setFirstLetter(model.getFirstLetter());

                            }
                        });
                    } else {
                        final RealmResults<AppDao> name = mRealm.where(AppDao.class).equalTo("packageName", model.getPackageName()).findAll();
                        mRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                for (int i=0;i<name.size();i++){
                                    name.get(i).deleteFromRealm();
                                }
                            }
                        });
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            private CheckBox check;
            private TextView tv;


            public VH(View itemView) {
                super(itemView);
                check = (CheckBox) itemView.findViewById(R.id.check);
                tv = (TextView) itemView.findViewById(R.id.tv);
            }
        }
    }

}
