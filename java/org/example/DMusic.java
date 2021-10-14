package org.example;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.AudioProvider;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.*;
import discord4j.rest.util.Color;
import java.time.Instant;

public class DMusic {
    public static final AudioPlayerManager PLAYER_MANAGER;
    static GuildAudioManager manager;
    static AudioProvider provider;
    static VoiceConnection connection;
    static ArrayList<String> song_queue = new ArrayList<>();

    static {
        PLAYER_MANAGER = new DefaultAudioPlayerManager();
        PLAYER_MANAGER.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(PLAYER_MANAGER);
        AudioSourceManagers.registerLocalSource(PLAYER_MANAGER);
    }

    private static final Map<String, Command> commands = new HashMap<>();

    public static void main(String[] args) {
        final GatewayDiscordClient client = DiscordClientBuilder.create(args[0]).build()
                .login()
                .block();

        assert client != null;
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .flatMap(event -> Mono.just(event.getMessage().getContent())
                        .flatMap(content -> Flux.fromIterable(commands.entrySet())
                                .filter(entry -> content.startsWith('!' + entry.getKey()))
                                .flatMap(entry -> entry.getValue().execute(event))
                                .next()))
                .subscribe();


        client.onDisconnect().block();

    }

    static {
        commands.put("join", event -> {
            final Member member = event.getMember().orElse(null);
            if (member != null) {
                final VoiceState voiceState = member.getVoiceState().block();
                if (voiceState != null) {
                    final VoiceChannel channel = voiceState.getChannel().block();
                    if (channel != null) {
                        manager = GuildAudioManager.of(channel.getGuildId());
                        provider = manager.getProvider();
                        connection = channel.join(spec -> spec.setProvider(provider)).block();
                        channel.join(spec -> spec.setProvider(provider)).block();
                    }
                }
            }

            return Mono.empty();
        });



        commands.put("play", event -> {
            String search_term = returnFormattedSearch(event.getMessage().getContent().substring(5));
            String url = "";

            if (KeyTermsToUrlMap.getDataFromFile().containsKey(search_term)) {
                url = KeyTermsToUrlMap.getDataFromFile().get(search_term);
                System.out.println("Retrieved from file: " + url);
            } else {
                url = "https://youtube.com/watch?v=" + YoutubeSearchAPI.SearchWithKeyword(search_term);

                Map<String, String> values = new HashMap<>();
                values.put(search_term, url);

                KeyTermsToUrlMap.writeToFile(values);
                System.out.println("Written to file");
            }

            PLAYER_MANAGER.loadItemOrdered(manager, url, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack audioTrack) {
                    song_queue.add(audioTrack.getInfo().title + " - " + audioTrack.getInfo().author);
                    System.out.println(song_queue.get(song_queue.size()-1));

                    manager.getScheduler().play(audioTrack);
                }

                @Override
                public void playlistLoaded(AudioPlaylist audioPlaylist) {

                }

                @Override
                public void noMatches() {

                }

                @Override
                public void loadFailed(FriendlyException e) {

                }
            });

            return Mono.empty();
        });


        commands.put("queue", event -> {
            final MessageChannel channel = event.getMessage().getChannel().block();
            assert channel != null;
            channel.createEmbed(spec -> spec.setColor(Color.RED)
                    .setTitle("The Chad's Repertoire")
                    .setDescription(returnFormattedQueue(song_queue))
                    .setTimestamp(Instant.now())).block();

            return Mono.empty();
        });

        commands.put("skip", event -> {

            if (!song_queue.isEmpty()) {
                manager.getScheduler().skip();
                song_queue.remove(0);
            }

            return Mono.empty();
        });

        commands.put("loop", event -> {
            final MessageChannel channel = event.getMessage().getChannel().block();

            if (manager.getScheduler().isLooping == false) {
                manager.getScheduler().isLooping = true;

                assert channel != null;
                channel.createEmbed(spec -> spec.setColor(Color.RED)
                        .setTitle("Looping the queue")).block();

            } else {
                manager.getScheduler().isLooping = false;

                channel.createEmbed(spec -> spec.setColor(Color.RED)
                        .setTitle("Un-Looping the queue")).block();
            }

            return Mono.empty();
        });

    }

    static String returnFormattedQueue(ArrayList<String> list) {
        StringBuilder result = new StringBuilder();
        for (String str : list) {
            result.append(str).append("\n");
        }

        return result.toString();
    }

    static String returnFormattedSearch(String str) {
        String result = str;
        return result.replaceAll("\\s+","+");
    }
}

interface Command {
    Mono<Void> execute(MessageCreateEvent event);
}