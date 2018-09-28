package uk.ac.standrews.cs.emap;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class PeerListFragment extends Fragment {
    public static final String DEVICE_LIST = "device_list";
    private OnListFragmentInteractionListener fragmentListener;
    private List<MamocNode> nodes = null;
    private RecyclerView recyclerView;

    public PeerListFragment(){
        nodes = new ArrayList<>();
    }

    public static PeerListFragment newInstance(int columns){
        PeerListFragment fragment = new PeerListFragment();
        Bundle args = new Bundle();
        args.putInt(DEVICE_LIST, columns);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nodes = new ArrayList<>();
        if (getArguments() != null){
            nodes = (List<MamocNode>) getArguments().getSerializable(DEVICE_LIST);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_list, container, false);

        if (view instanceof RecyclerView){
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new PeerListAdapter(nodes, fragmentListener));
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            fragmentListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(MamocNode node);
    }
}
