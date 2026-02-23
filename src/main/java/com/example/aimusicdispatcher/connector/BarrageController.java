package com.example.aimusicdispatcher.connector;

import com.example.aimusicdispatcher.model.barrage.BarrageRequest;
import com.example.aimusicdispatcher.model.playlist.PlayTask;
import com.example.aimusicdispatcher.scheduler.PlaybackWorker;
import com.example.aimusicdispatcher.service.BarrageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/barrage")
public class BarrageController {

    private final BarrageService barrageService;
    private final PlaybackWorker playbackWorker;

    public BarrageController(BarrageService barrageService, PlaybackWorker playbackWorker) {
        this.barrageService = barrageService;
        this.playbackWorker = playbackWorker;
    }

    @PostMapping("/receive")
    public ResponseEntity<String> receiveBarrage(@RequestBody BarrageRequest barrageRequest) {
        barrageService.processBarrage(barrageRequest);
        return ResponseEntity.ok("Barrage received.");
    }

    @GetMapping("/queue/list")
    public ResponseEntity<List<Map<String, Object>>> getQueueList() {
        List<Map<String, Object>> queueList = new ArrayList<>();
        for (PlayTask task : playbackWorker.getPlayQueue()) {
            Map<String, Object> taskInfo = new HashMap<>();
            taskInfo.put("songName", task.getSongName());
            taskInfo.put("requester", task.getRequester());
            taskInfo.put("hasIntro", task.getIntroAudioPath() != null);
            queueList.add(taskInfo);
        }
        return ResponseEntity.ok(queueList);
    }
}
