package uk.ac.standrews.cs.emap;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

class PeerListAdapter extends RecyclerView.Adapter<PeerListAdapter.ViewHolder> {
    public final List<MamocNode> mNodes;
    private PeerListFragment.OnListFragmentInteractionListener mListener;

    public PeerListAdapter(List<MamocNode> nodes, PeerListFragment.OnListFragmentInteractionListener fragmentListener) {
        mNodes = nodes;
        mListener = fragmentListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.device_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PeerListAdapter.ViewHolder viewHolder, int i) {
        viewHolder.node = mNodes.get(i);
        viewHolder.mContentView.setText(mNodes.get(i).getNodeName() + " - " + mNodes.get(i).getOsVersion());
        
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    mListener.onListFragmentInteraction(viewHolder.node);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.v("count", String.valueOf(mNodes.size()));
        return mNodes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public MamocNode node;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            mContentView = (TextView) itemView;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
