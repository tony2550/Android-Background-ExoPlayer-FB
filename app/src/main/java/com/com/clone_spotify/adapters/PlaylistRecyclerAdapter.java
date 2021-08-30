package com.com.clone_spotify.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.com.clone_spotify.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PlaylistRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final String TAG = "PlaylistRecyclerAdapter";
    private ArrayList<MediaMetadataCompat> mMediaList = new ArrayList<>();
    private Context mContext;
    private PlaylistRecyclerAdapter playlistRecyclerAdapter = this;
    private IMediaSelector mIMediaSelector;
    private int mSelectedIndex;

    public PlaylistRecyclerAdapter(Context context, ArrayList<MediaMetadataCompat> mediaList, IMediaSelector mediaSelector) {

        Log.d(TAG, "PlaylistRecyclerAdapter: called");
        this.mMediaList = mediaList;
        this.mContext = context;
        this.mIMediaSelector = mediaSelector;
        mSelectedIndex = -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.playlist_item, null);
        ViewHolder vh = new ViewHolder(view, mIMediaSelector);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ((ViewHolder)viewHolder).tvName.setText(mMediaList.get(position).getDescription().getTitle());
        ((ViewHolder)viewHolder).tvInfo.setText(mMediaList.get(position).getDescription().getSubtitle());

        RequestOptions requestOptions = new RequestOptions()
                .error(R.drawable.ic_launcher_background);
        if(position == mSelectedIndex){
            ((ViewHolder)viewHolder).tvName.setTextColor(ContextCompat.getColor(mContext, R.color.green));
        }
        else{
            ((ViewHolder)viewHolder).tvName.setTextColor(ContextCompat.getColor(mContext, R.color.white));
        }

        Glide.with(mContext)
                .setDefaultRequestOptions(requestOptions)
                .load(mMediaList.get(position).getDescription().getIconUri())
                .into(((ViewHolder)viewHolder).imgSong);

    }


    @Override
    public int getItemCount() {
        return mMediaList.size();
    }

    public void setSelectedIndex(int index){
        mSelectedIndex = index;
        notifyDataSetChanged();
    }

    public int getSelectedIndex(){
        return mSelectedIndex;
    }

    public int getIndexOfItem(MediaMetadataCompat mediaItem){
        for(int i = 0; i<mMediaList.size(); i++ ){
            if(mMediaList.get(i).getDescription().getMediaId().equals(mediaItem.getDescription().getMediaId())){
                return i;
            }
        }
        return -1;
    }

    public interface IMediaSelector{
        void onMediaSelected(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView imgSong;
        private TextView tvName , tvInfo;
        private IMediaSelector iMediaSelector;

        public ViewHolder(View itemView, IMediaSelector categorySelector) { // view를 들고와서 갈아 끼우는 역할 생성자
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            imgSong = itemView.findViewById(R.id.imgSong);
            iMediaSelector = categorySelector;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iMediaSelector.onMediaSelected(getAdapterPosition());
        }
    }
}
