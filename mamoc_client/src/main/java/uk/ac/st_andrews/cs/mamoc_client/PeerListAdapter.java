package uk.ac.st_andrews.cs.mamoc_client;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import uk.ac.st_andrews.cs.mamoc_client.Model.MobileNode;

public class PeerListAdapter extends RecyclerView.Adapter<PeerListAdapter.ViewHolder> {

    private final List<MobileNode> mDevices;
    private final PeerListFragment.OnListFragmentInteractionListener mListener;

    public PeerListAdapter(List<MobileNode> devices, PeerListFragment.OnListFragmentInteractionListener
            listener) {
        mDevices = devices;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.deviceDTO = mDevices.get(position);
        holder.mContentView.setText(mDevices.get(position).getManufacturer() + " - " + mDevices.get(position).getIp());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.deviceDTO);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public MobileNode deviceDTO;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = (TextView) view;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}

