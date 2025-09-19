package me.winflix.vitalcore.addons.model.data;

public class EffectKeyframe {
    private final String channel; // "sound" o "particle"
    private final String effect;
    private final double time; // en segundos

    public EffectKeyframe(String channel, String effect, double time) {
        this.channel = channel;
        this.effect = effect;
        this.time = time;
    }

    public String getChannel() {
        return channel;
    }

    public String getEffect() {
        return effect;
    }

    public double getTime() {
        return time;
    }
}