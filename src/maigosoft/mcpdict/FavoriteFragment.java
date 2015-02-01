package maigosoft.mcpdict;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

@SuppressLint("UseSparseArrays")
public class FavoriteFragment extends ListFragment implements RefreshableFragment {

    private View selfView;
    private View header;
    private TextView textTotal;
    private Button buttonClear;
    private ListView listView;
    private TextView textEmpty;
    private FavoriteCursorAdapter adapter;
    private boolean hasNewItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // A hack to avoid nested fragments from being inflated twice
        // Reference: http://stackoverflow.com/a/14695397
        if (selfView != null) {
            ViewGroup parent = (ViewGroup) selfView.getParent();
            if (parent != null) parent.removeView(selfView);
            return selfView;
        }

        // Inflate the fragment view
        selfView = inflater.inflate(R.layout.favorite_fragment, container, false);

        // Get references to some child views
        header = selfView.findViewById(R.id.favorite_header);
        textTotal = (TextView) selfView.findViewById(R.id.text_total);
        listView = (ListView) selfView.findViewById(android.R.id.list);
        textEmpty = (TextView) selfView.findViewById(android.R.id.empty);

        // Set up the "clear all" button
        buttonClear = (Button) selfView.findViewById(R.id.button_clear);
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavoriteDialogs.deleteAll();
            }
        });

        return selfView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up the adapter
        if (adapter == null) {
            adapter = new FavoriteCursorAdapter(getActivity(), R.layout.favorite_item, null, this);
            setListAdapter(adapter);
        }
    }

    @Override
    public void onListItemClick(final ListView list, final View view, final int position, long id) {
        // When a favorite item is clicked, display a SearchResultFragment below it
        //   to show the details about the character in the item, or hide the
        //   SearchResultFragment if it is already displayed

        // Find the Chinese character in the view being clicked
        TextView text = (TextView) view.findViewById(R.id.text_hz);
        String hanzi = text.getText().toString();
        final char unicode = hanzi.charAt(0);

        if (adapter.isItemExpanded(unicode)) {
            adapter.collapseItem(unicode, view, list);
        }
        else {
            adapter.expandItem(unicode, view, list);
        }
    }

    @Override
    public void refresh() {
        if (adapter == null) return;
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... params) {
                return UserDatabase.selectAllFavorites();
            }
            @Override
            protected void onPostExecute(Cursor data) {
                adapter.changeCursor(data);
                if (data.getCount() == 0) {
                    header.setVisibility(View.GONE);
                    textEmpty.setText(R.string.favorite_empty);
                }
                else {
                    header.setVisibility(View.VISIBLE);
                    textTotal.setText(String.format(getString(R.string.favorite_total), data.getCount()));
                }
            }
        }.execute();
        if (hasNewItem) {
            listView.setSelectionAfterHeaderView();
            hasNewItem = false;
        }
    }

    public void notifyAddItem() {
        hasNewItem = true;
    }
}
