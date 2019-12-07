package xyz.gianlu.librespot.player.codecs;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.gianlu.librespot.player.GeneralAudioStream;
import xyz.gianlu.librespot.player.NormalizationData;
import xyz.gianlu.librespot.player.Player;

import javax.sound.sampled.AudioFormat;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Gianlu
 */
public abstract class Codec implements Closeable {
    public static final int BUFFER_SIZE = 2048;
    private static final Logger LOGGER = Logger.getLogger(Codec.class);
    protected final InputStream audioIn;
    protected final float normalizationFactor;
    protected final int duration;
    private final AudioFormat dstFormat;
    protected volatile boolean closed = false;
    private AudioFormat format;
    private StreamConverter converter = null;

    Codec(@NotNull AudioFormat dstFormat, @NotNull GeneralAudioStream audioFile, @Nullable NormalizationData normalizationData, @NotNull Player.Configuration conf, int duration) {
        this.dstFormat = dstFormat;
        this.audioIn = audioFile.stream();
        this.duration = duration;
        if (conf.enableNormalisation())
            this.normalizationFactor = normalizationData != null ? normalizationData.getFactor(conf) : 1;
        else
            this.normalizationFactor = 1;
    }

    public final int readSome(@NotNull OutputStream out) throws IOException, CodecException {
        if (converter == null) return readInternal(out);

        int written = readInternal(converter);
        if (written == -1) return -1;

        converter.checkWritten(written);
        return converter.convertInto(out);
    }

    protected abstract int readInternal(@NotNull OutputStream out) throws IOException, CodecException;

    /**
     * @return Time in millis
     * @throws CannotGetTimeException If the codec can't determine the time. This condition is permanent for the entire playback.
     */
    public abstract int time() throws CannotGetTimeException;

    public final int remaining() throws CannotGetTimeException {
        return duration - time();
    }

    @Override
    public void close() throws IOException {
        closed = true;
        audioIn.close();
    }

    public void seek(int positionMs) {
        if (positionMs < 0) positionMs = 0;

        try {
            audioIn.reset();
            if (positionMs > 0) {
                int skip = Math.round(audioIn.available() / (float) duration * positionMs);
                if (skip > audioIn.available()) skip = audioIn.available();

                long skipped = audioIn.skip(skip);
                if (skip != skipped)
                    throw new IOException(String.format("Failed seeking, skip: %d, skipped: %d", skip, skipped));
            }
        } catch (IOException ex) {
            LOGGER.fatal("Failed seeking!", ex);
        }
    }

    @NotNull
    public final AudioFormat getAudioFormat() {
        if (format == null) throw new IllegalStateException();
        return format;
    }

    protected final void setAudioFormat(@NotNull AudioFormat format) {
        this.format = format;

        if (format.matches(dstFormat)) converter = null;
        else converter = StreamConverter.converter(format, dstFormat);
    }

    protected final int sampleSizeBytes() {
        return getAudioFormat().getSampleSizeInBits() / 8;
    }

    public final int duration() {
        return duration;
    }

    public static class CannotGetTimeException extends Exception {
        CannotGetTimeException() {
        }
    }

    public static class CodecException extends Exception {
        CodecException() {
        }
    }
}
