package org.example;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AudioTrackScheduler extends AudioEventAdapter {
    private final List<AudioTrack> queue;
    private final AudioPlayer player;
    public boolean isLooping = false;

    public AudioTrackScheduler(final AudioPlayer player) {
        queue = Collections.synchronizedList(new LinkedList<>());
        this.player = player;
    }

    public List<AudioTrack> getQueue() {
        return queue;
    }

    public boolean play(final AudioTrack track) {
        return play(track, false);
    }

    public boolean play(final AudioTrack track, final boolean force) {
        final boolean playing = player.startTrack(track, !force);


        if (!playing) {
            queue.add(track);
        }

        return playing;
    }

    public boolean skip() {
        return !queue.isEmpty() && play(queue.remove(0), true);
    }


    @Override
    public void onTrackEnd(final AudioPlayer player, final AudioTrack track, final AudioTrackEndReason endReason) {
        if (endReason.mayStartNext && !isLooping) {
            skip();
        } else {
            System.out.println("Looping");
            play(track.makeClone());
        }
    }
}
