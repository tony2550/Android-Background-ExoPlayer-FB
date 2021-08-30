package com.com.clone_spotify.adapters;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.com.clone_spotify.R;
import com.com.clone_spotify.repository.dto.Song;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SwipeAdapter extends RecyclerView.Adapter<SwipeAdapter.SliderViewHolder> {


    private List<MediaMetadataCompat> mMediaList = new ArrayList<>();
    private Context mContext;
    private ViewPager2 viewPager2;


    public SwipeAdapter(List<MediaMetadataCompat> mMediaList, Context context) {
        this.mMediaList = mMediaList;
        this.mContext = context;
    }


    @Override
    public SliderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_item, parent, false);
        SliderViewHolder viewHolder = new SliderViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SliderViewHolder holder, int position) {
//        ((SliderViewHolder)holder).tvName.setText(mMediaList.get(position).getDescription().getTitle());
//        ((SliderViewHolder)holder).tvInfo.setText(mMediaList.get(position).getDescription().getSubtitle());
        MediaMetadataCompat song = mMediaList.get(position);
        holder.setItem(song);
    }

    @Override
    public int getItemCount() {
        return mMediaList.size();
    }

    public class SliderViewHolder extends RecyclerView.ViewHolder {

            private TextView tvName , tvInfo;

            SliderViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvInfo = itemView.findViewById(R.id.tvInfo);
            }

        public void setItem(MediaMetadataCompat song) {
            tvName.setText(song.getDescription().getTitle());
            tvInfo.setText(song.getDescription().getSubtitle());
        }

        }
}
