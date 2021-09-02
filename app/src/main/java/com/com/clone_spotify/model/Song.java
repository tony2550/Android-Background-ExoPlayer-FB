package com.com.clone_spotify.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Song {

    private String imageUrl;
    private String mediaid;
    private String songUrl;
    private String subtitle;
    private String title;
}
