package com.example.aimusicdispatcher.model.playlist;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayTask {
    private Long musicId;
    private String songName;
    private String introAudioPath; // 口播音频文件路径
    private String songFilePath;   // 歌曲文件路径
    private String requester;      // 点歌人（可选）
}
