package me.oriley.vistasample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

@SuppressWarnings("WeakerAccess")
public final class GridViewFragment extends Fragment {

    @NonNull
    private GridView mGridView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grid_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGridView = (GridView) view.findViewById(R.id.list_view);
        mGridView.setAdapter(new Adapter());
    }

    @Override
    public void onDestroyView() {
        mGridView.setAdapter(null);
        super.onDestroyView();
    }

    private static final class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView != null) {
                return convertView;
            } else {
                return LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_grid_item, parent, false);
            }
        }

    }
}